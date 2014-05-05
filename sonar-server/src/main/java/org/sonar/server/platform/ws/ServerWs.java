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

package org.sonar.server.platform.ws;

import com.google.common.io.Resources;
import org.sonar.api.server.ws.RailsHandler;
import org.sonar.api.server.ws.WebService;

public class ServerWs implements WebService {

  @Override
  public void define(Context context) {
    NewController controller = context.createController("api/server")
      .setSince("2.10");

    defineSystemAction(controller);
    defineSetupAction(controller);

    controller.done();
  }

  private void defineSystemAction(NewController controller) {
    controller.createAction("system")
      .setDescription("Get the system properties, server info (Java, OS), database configuration, JVM statistics and installed plugins. Requires Administer System permission")
      .setSince("2.10")
      .setHandler(RailsHandler.INSTANCE)
      .setResponseExample(Resources.getResource(this.getClass(), "example-system.json"));
  }

  private void defineSetupAction(NewController controller) {
    controller.createAction("setup")
      .setDescription("Upgrade the SonarQube database")
      .setSince("2.10")
      .setPost(true)
      .setHandler(RailsHandler.INSTANCE)
      .setResponseExample(Resources.getResource(this.getClass(), "example-setup.json"));
  }

}
