/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.site.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.DisposedCourseRestartController;

/**
 * 
 * Initial date: 17.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DisposedCourseSiteRestartController extends BasicController {

	private VelocityContainer initialContent;
	private Link restartLink;

	public DisposedCourseSiteRestartController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setBasePackage(DisposedCourseRestartController.class);
		initialContent = createVelocityContainer("disposedcourserestart");
		restartLink = LinkFactory.createButton("course.disposed.command.restart", initialContent, this);
		putInitialPanel(initialContent);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == restartLink) {
			fireEvent(ureq, new Event("restart"));
		}
	}
}
