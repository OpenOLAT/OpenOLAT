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
package org.olat.modules.fo.export;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.CourseModule;
import org.olat.modules.fo.Message;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;

/**
 * Initial Date: 15.02.2017
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class SelectCourseStepForm extends StepFormBasicController {
	
	private ReferencableEntriesSearchController searchCtrl;
	private CloseableModalController cmc;
	private SingleSelection chooseCourse;
	

	public SelectCourseStepForm(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext,
			String customLayoutPageName) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, customLayoutPageName);
		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == chooseCourse) {
			doStartOverIfHasReturned();
		} else if (isCourseChosen(source, event)) {
			if (chooseCourse.isSelected(0)) {
				doChooseCourse(ureq);
			} else if (chooseCourse.isSelected(1)) {
				addToRunContext(SendMailStepForm.COURSE_CHOSEN, Boolean.TRUE);
				fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
			} else {
				showInfo("radio.not.selected");
			}			
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source.equals(searchCtrl)) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				// repository search controller done
				RepositoryEntry courseEntry = searchCtrl.getSelectedEntry();
				//put as step run context
				addToRunContext(SendMailStepForm.COURSE, courseEntry);
				addToRunContext(SendMailStepForm.COURSE_CHOSEN, Boolean.TRUE);
				cleanUp();
				fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
			}
		}
		super.event(ureq, source, event);
	}


	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] threadOrComment;
		Message messageToMove = (Message)getFromRunContext(SendMailStepForm.MESSAGE_TO_MOVE);
		if (messageToMove != null && messageToMove.getThreadtop() == null) {
			threadOrComment = new String[] { translate("forum.thread") };
		} else {
			threadOrComment = new String[] { translate("forum.comment") };
		}	
		String[] theKeys = new String[]{"radio.foreign.course", "radio.same.course"};
		String[] theValues = new String[] { translate("radio.foreign.course", threadOrComment),
				translate("radio.same.course", threadOrComment) };
		
		chooseCourse = uifactory.addRadiosVertical("step.select.course", formLayout, theKeys, theValues);
		chooseCourse.addActionListener(FormEvent.ONCLICK);

	}
	
	private void doStartOverIfHasReturned() {
		addToRunContext(SendMailStepForm.COURSE_CHOSEN, Boolean.FALSE);	
	}
	
	private void doChooseCourse(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(searchCtrl);
		searchCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq,
				new String[] { CourseModule.ORES_TYPE_COURSE }, translate("step.select.course"));
		listenTo(searchCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), 
				searchCtrl.getInitialComponent(), true, translate("step.select.course"));
		cmc.activate();
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(searchCtrl);
		cmc.deactivate();
		cmc = null;
		searchCtrl = null;
	}
	
	private boolean isCourseChosen(FormItem source, FormEvent event) {
		Boolean courseChosen = (Boolean)getFromRunContext(SendMailStepForm.COURSE_CHOSEN);
		if (courseChosen == null) { 
			return false;
		}
		return "done".equals(event.getCommand()) && "next".equals(source.getName()) && !courseChosen;
	}

	
}
