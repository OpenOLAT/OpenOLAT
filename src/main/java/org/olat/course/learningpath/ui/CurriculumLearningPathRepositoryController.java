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
package org.olat.course.learningpath.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumElementLearningPathController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumLearningPathRepositoryController extends BasicController {

	private Controller listCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private CurriculumService curriculumService;

	public CurriculumLearningPathRepositoryController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel stackPanel, CurriculumElement element, Identity participant) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CurriculumElementLearningPathController.class, getLocale(), getTranslator()));
		
		VelocityContainer mainVC = createVelocityContainer("identity_curriculum_element");
		
		String userDisplayName = userManager.getUserDisplayName(participant);
		mainVC.contextPut("user", userDisplayName);
		
		mainVC.contextPut("elementName", element.getDisplayName());
		mainVC.contextPut("elementIdentifier", element.getIdentifier());
		Formatter formatter = Formatter.getInstance(getLocale());
		if(element.getBeginDate() != null) {
			mainVC.contextPut("elementBegin", formatter.formatDate(element.getBeginDate()));
		}
		if(element.getEndDate() != null) {
			mainVC.contextPut("elementEnd", formatter.formatDate(element.getEndDate()));
		}
		
		Curriculum curriculum = element.getCurriculum();
		mainVC.contextPut("curriculumName", curriculum.getDisplayName());
		mainVC.contextPut("curriculumIdentifier", curriculum.getIdentifier());
		
		List<CurriculumElement> parentLine = curriculumService.getCurriculumElementParentLine(element);
		parentLine.remove(element);
		mainVC.contextPut("parentLine", parentLine);
		
		listCtrl = new CurriculumLearningPathRepositoryListController(ureq, wControl, stackPanel, element,
				participant);
		listenTo(listCtrl);
		mainVC.put("list", listCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
