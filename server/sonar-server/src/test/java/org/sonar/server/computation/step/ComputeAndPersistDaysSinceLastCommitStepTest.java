/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.server.computation.step;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.batch.protocol.Constants;
import org.sonar.batch.protocol.output.BatchReport;
import org.sonar.batch.protocol.output.BatchReportReader;
import org.sonar.batch.protocol.output.BatchReportWriter;
import org.sonar.core.component.ComponentDto;
import org.sonar.core.measure.db.MetricDto;
import org.sonar.core.persistence.DbTester;
import org.sonar.server.computation.ComputationContext;
import org.sonar.server.computation.measure.MetricCache;
import org.sonar.server.db.DbClient;
import org.sonar.server.measure.persistence.MeasureDao;
import org.sonar.server.source.index.SourceLineIndex;

import java.io.File;
import java.io.IOException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ComputeAndPersistDaysSinceLastCommitStepTest extends BaseStepTest {

  @ClassRule
  public static DbTester db = new DbTester();
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();
  File dir;

  ComputeAndPersistDaysSinceLastCommitStep sut;

  DbClient dbClient;
  SourceLineIndex sourceLineIndex;
  MetricCache metricCache;

  @Before
  public void setUp() throws Exception {
    dbClient = new DbClient(db.database(), db.myBatis(), new MeasureDao());
    sourceLineIndex = mock(SourceLineIndex.class);
    metricCache = mock(MetricCache.class);
    dir = temp.newFolder();

    sut = new ComputeAndPersistDaysSinceLastCommitStep(dbClient, sourceLineIndex, metricCache);
  }

  @Override
  protected ComputationStep step() throws IOException {
    return sut;
  }

  @Test
  public void persist_number_of_days_since_last_commit() throws Exception {
    when(metricCache.get(anyString())).thenReturn(new MetricDto().setId(1));
    BatchReportWriter reportWriter = initReportWithProjectAndFile();

    reportWriter.writeComponentScm(
      BatchReport.Scm.newBuilder()
        .setComponentRef(2)
        .addChangeset(
          BatchReport.Scm.Changeset.newBuilder()
          .setDate(123_456_789L)
        )
      .build()
    );
    ComputationContext context = new ComputationContext(new BatchReportReader(dir), new ComponentDto().setId(1L).setUuid("project-uuid"));

    sut.execute(context);

    db.assertDbUnit(getClass(), "insert-result.xml", "project_measures");
  }

  private BatchReportWriter initReportWithProjectAndFile() throws IOException {
    BatchReportWriter writer = new BatchReportWriter(dir);
    writer.writeMetadata(BatchReport.Metadata.newBuilder()
      .setRootComponentRef(1)
      .setSnapshotId(10)
      .build());

    writer.writeComponent(BatchReport.Component.newBuilder()
      .setRef(1)
      .setType(Constants.ComponentType.PROJECT)
      .setKey("project-key")
      .setUuid("project-uuid")
      .setSnapshotId(10L)
      .addChildRef(2)
      .build());
    writer.writeComponent(BatchReport.Component.newBuilder()
      .setRef(2)
      .setType(Constants.ComponentType.FILE)
      .setSnapshotId(11L)
      .build());

    return writer;
  }
}
