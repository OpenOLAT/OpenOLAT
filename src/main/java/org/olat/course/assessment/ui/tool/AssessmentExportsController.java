/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.tool;

import org.olat.core.commons.services.export.ui.ExportsListController;
import org.olat.core.commons.services.export.ui.ExportsListSettings;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: Dec 12, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class AssessmentExportsController extends BasicController {

	private final Controller exportCtrl;
	private final ExportsListController exportListController;

	public AssessmentExportsController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv,
			CourseNode courseNode, AssessmentToolSecurityCallback secCallback, ControllerCreator exportCreator) {
		super(ureq, wControl);
		
		exportCtrl = exportCreator.createController(ureq, wControl);
		listenTo(exportCtrl);
		
		ExportsListSettings options = new ExportsListSettings(true);
		RepositoryEntry entry = courseEnv.getCourseGroupManager().getCourseEntry();
		exportListController = new ExportsListController(ureq, wControl, entry, courseNode.getIdent(), secCallback.isAdmin(), options, getTranslator());
		listenTo(exportListController);
		exportListController.loadModel();
		
		VelocityContainer mainVC = createVelocityContainer("export");
		mainVC.put("export", exportCtrl.getInitialComponent());
		mainVC.put("export.list", exportListController.getInitialComponent());
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (exportCtrl == source) {
			if (event == Event.DONE_EVENT) {
				exportListController.loadModel();
			}
		}
	}


}
