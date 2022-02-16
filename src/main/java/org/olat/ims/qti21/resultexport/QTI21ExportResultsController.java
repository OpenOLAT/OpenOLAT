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
package org.olat.ims.qti21.resultexport;

import org.olat.core.commons.services.export.ui.ExportsListController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 1 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21ExportResultsController extends BasicController {
	
	private final QTI21NewExportController newExportCtrl;
	private final ExportsListController exportListController;
	
	public QTI21ExportResultsController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv,
			IQTESTCourseNode courseNode, IdentitiesList identities, AssessmentToolSecurityCallback secCallback) {
		super(ureq, wControl);
		
		newExportCtrl = new QTI21NewExportController(ureq, wControl, courseEnv, courseNode, identities);
		listenTo(newExportCtrl);
		
		RepositoryEntry entry = courseEnv.getCourseGroupManager().getCourseEntry();
		exportListController = new ExportsListController(ureq, wControl, entry, courseNode.getIdent(), secCallback.isAdmin());
		listenTo(exportListController);
		
		VelocityContainer mainVC = createVelocityContainer("export");
		mainVC.put("new.export", newExportCtrl.getInitialComponent());
		mainVC.put("export.list", exportListController.getInitialComponent());
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(newExportCtrl == source) {
			if(event == Event.DONE_EVENT) {
				exportListController.loadModel();
			}
		}
	}

}
