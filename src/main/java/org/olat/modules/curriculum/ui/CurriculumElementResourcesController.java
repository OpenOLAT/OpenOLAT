/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumSecurityCallback;

/**
 * 
 * Initial date: 14 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementResourcesController extends BasicController {
	
	private CurriculumElementTemplateListController templatesCtrl;
	private final CurriculumElementResourceListController resourcesCtrl;
	
	public CurriculumElementResourcesController(UserRequest ureq, WindowControl wControl,
			CurriculumElement curriculumElement, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);

		VelocityContainer mainVC = createVelocityContainer("resources");
		
		resourcesCtrl = new CurriculumElementResourceListController(ureq, wControl, curriculumElement, secCallback);
		listenTo(resourcesCtrl);
		mainVC.put("resources", resourcesCtrl.getInitialComponent());
		
		CurriculumElementType type = curriculumElement.getType();
		if(type != null && type.getMaxRepositoryEntryRelations() == 1) {
			templatesCtrl = new CurriculumElementTemplateListController(ureq, wControl, curriculumElement, secCallback);
			listenTo(templatesCtrl);
			mainVC.put("templates", templatesCtrl.getInitialComponent());
		}
		putInitialPanel(mainVC);
	}
	
	public void loadModel() {
		int linkedCourses = resourcesCtrl.loadModel();
		int linkedTemplates = 0;
		if(templatesCtrl != null) {
			linkedTemplates = templatesCtrl.loadModel();
		}
		
		resourcesCtrl.updateAddButtonAndEmptyMessages(linkedTemplates);
		if(templatesCtrl != null) {
			templatesCtrl.updateAddButtonAndEmptyMessages(linkedCourses);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(resourcesCtrl == source || templatesCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				loadModel();
			}
		}
	}
}
