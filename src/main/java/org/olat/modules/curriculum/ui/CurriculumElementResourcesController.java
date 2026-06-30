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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.event.CurriculumElementEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementResourcesController extends BasicController {
	
	private static final String CMD_OPEN_ELEMENT = "oelement";
	
	private final VelocityContainer mainVC;
	
	private CurriculumElement curriculumElement;
	
	private CurriculumElementTemplateListController templatesCtrl;
	private final CurriculumElementResourceListController resourcesCtrl;
	
	@Autowired
	private CurriculumService curriculumService;

	public CurriculumElementResourcesController(UserRequest ureq, WindowControl wControl,
			Curriculum curriculum, CurriculumElement curriculumElement, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		this.curriculumElement = curriculumElement;

		mainVC = createVelocityContainer("resources");
		
		resourcesCtrl = new CurriculumElementResourceListController(ureq, wControl, curriculum, curriculumElement, secCallback);
		listenTo(resourcesCtrl);
		mainVC.put("resources", resourcesCtrl.getInitialComponent());
		
		CurriculumElementType type = curriculumElement.getType();
		if(type != null && type.getMaxRepositoryEntryRelations() == 1) {
			templatesCtrl = new CurriculumElementTemplateListController(ureq, wControl, curriculum, curriculumElement, secCallback);
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
		if(source instanceof Link link && CMD_OPEN_ELEMENT.equals(link.getCommand())
				&& link.getUserObject() instanceof CurriculumElement element) {
			fireEvent(ureq, new CurriculumElementEvent(element, List.of()));
		}
	}
	
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(resourcesCtrl == source || templatesCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				loadModel();
			}
		}
	}
	
	public record Infos(String title, String date, String text, Link link) {
		//
	}
}
