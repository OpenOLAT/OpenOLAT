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
package org.olat.modules.curriculum.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.course.learningpath.ui.CurriculumElementLearningPathListController;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementLearningPathController extends BasicController {

	private Controller listController;
	
	@Autowired
	private CurriculumService curriculumService;

	public CurriculumElementLearningPathController(UserRequest ureq, WindowControl swControl,
			TooledStackedPanel stackPanel, CurriculumElement element) {
		super(ureq, swControl);
		
		VelocityContainer mainVC = createVelocityContainer("curriculum_learning_progress");
		
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
		
		listController = new CurriculumElementLearningPathListController(ureq, swControl, stackPanel,
				element);
		listenTo(listController);
		mainVC.put("list", listController.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
