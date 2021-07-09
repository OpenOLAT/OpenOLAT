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
package org.olat.course.config.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.ui.courselayout.CourseLayoutGeneratorController;
import org.olat.course.disclaimer.ui.CourseDisclaimerController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.ui.RepositoryEntrySettingsController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseSettingsController extends RepositoryEntrySettingsController {
	
	private Link layoutLink;
	private Link toolbarLink;
	private Link optionsLink;
	private Link assessmentLink;
	private Link executionSettingsLink;
	private Link disclaimerLink;
	
	private CourseOptionsController optionsCtrl;
	private CourseToolbarController toolbarCtrl;
	private CourseLayoutGeneratorController layoutCtrl;
	private CourseAssessmentSettingsController assessmentSettingsCtrl;
	private CourseExecutionSettingsController executionSettingsCtrl;
	private CourseDisclaimerController courseDisclaimerCtrl;
	
	@Autowired
	private CourseModule courseModule;
	
	public CourseSettingsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, RepositoryEntry entry) {
		super(ureq, wControl, stackPanel, entry);
	}

	@Override
	protected void initAccessAndBooking() {
		executionSettingsLink = LinkFactory.createLink("details.execution", getTranslator(), this);
		executionSettingsLink.setElementCssClass("o_sel_execution");
		buttonsGroup.addButton(executionSettingsLink, false);
		
		super.initAccessAndBooking();
		
		if (courseModule.isDisclaimerEnabled()) {
			disclaimerLink = LinkFactory.createLink("course.disclaimer", getTranslator(), this);
			disclaimerLink.setElementCssClass("o_sel_disclaimer");
			buttonsGroup.addButton(disclaimerLink, false);
		}
	}

	@Override
	protected void initOptions() {
		layoutLink = LinkFactory.createLink("details.layout", getTranslator(), this);
		layoutLink.setElementCssClass("o_sel_layout");
		buttonsGroup.addButton(layoutLink, false);

		toolbarLink = LinkFactory.createLink("details.toolbar", getTranslator(), this);
		toolbarLink.setElementCssClass("o_sel_toolbar");
		buttonsGroup.addButton(toolbarLink, false);
		
		assessmentLink = LinkFactory.createLink("details.assessment", getTranslator(), this);
		assessmentLink.setElementCssClass("o_sel_assessment");
		buttonsGroup.addButton(assessmentLink, false);
		
		optionsLink = LinkFactory.createLink("details.options", getTranslator(), this);
		optionsLink.setElementCssClass("o_sel_options");
		buttonsGroup.addButton(optionsLink, false);
		
		super.initOptions();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		super.activate(ureq, entries, state);
		
		if(entries != null && !entries.isEmpty()) {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Execution".equalsIgnoreCase(type)) {
				doOpenExecutionSettings(ureq);
			} else if("Layout".equalsIgnoreCase(type)) {
				doOpenLayout(ureq);
			} else if("Toolbar".equalsIgnoreCase(type)) {
				doOpenToolbarSettings(ureq);
			} else if("Assessment".equalsIgnoreCase(type)) {
				doOpenAssessmentSettings(ureq);
			} else if("Options".equalsIgnoreCase(type)) {
				doOpenOptions(ureq);
			} else if("Disclaimer".equalsIgnoreCase(type)) {
				doOpenDisclaimer(ureq);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(optionsCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				doOpenOptions(ureq);
			}
		} else if(toolbarCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				doOpenToolbarSettings(ureq);
			}
		} else if(layoutCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				doOpenLayout(ureq);
			} else {
				fireEvent(ureq, event);
			}
		} else if(assessmentSettingsCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				doOpenAssessmentSettings(ureq);
			}
		} else if(executionSettingsCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				doOpenExecutionSettings(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(executionSettingsLink == source) {
			doOpenExecutionSettings(ureq);
		} else if(layoutLink == source) {
			doOpenLayout(ureq);
		} else if(toolbarLink == source) {
			doOpenToolbarSettings(ureq);
		} else if(assessmentLink == source) {
			doOpenAssessmentSettings(ureq);
		} else if(optionsLink == source) {
			doOpenOptions(ureq);
		} else if(disclaimerLink == source) {
			cleanUp();
			doOpenDisclaimer(ureq);
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void cleanUp() {
		super.cleanUp();
		
		removeAsListenerAndDispose(courseDisclaimerCtrl);
		removeAsListenerAndDispose(executionSettingsCtrl);
		removeAsListenerAndDispose(assessmentSettingsCtrl);
		removeAsListenerAndDispose(optionsCtrl);
		removeAsListenerAndDispose(toolbarCtrl);
		removeAsListenerAndDispose(layoutCtrl);
		courseDisclaimerCtrl = null;
		executionSettingsCtrl = null;
		assessmentSettingsCtrl = null;
		optionsCtrl = null;
		toolbarCtrl = null;
		layoutCtrl = null;
	}
	
	private void doOpenExecutionSettings(UserRequest ureq) {
		removeAsListenerAndDispose(executionSettingsCtrl);
		
		entry = repositoryService.loadByKey(entry.getKey());
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Execution"), null);
		executionSettingsCtrl = new CourseExecutionSettingsController(ureq, swControl, entry, readOnly);
		listenTo(executionSettingsCtrl);
		mainPanel.setContent(executionSettingsCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(executionSettingsLink);
	}
	
	private void doOpenLayout(UserRequest ureq) {
		removeControllerListener(layoutCtrl);
		
		ICourse course = CourseFactory.loadCourse(entry);
		boolean managedLayout = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.layout);
		CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig().clone();
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Layout"), null);
		layoutCtrl = new CourseLayoutGeneratorController(ureq, swControl, entry, courseConfig,
		  		course.getCourseEnvironment(), !managedLayout, readOnly);
		
		listenTo(layoutCtrl);
		mainPanel.setContent(layoutCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(layoutLink);
	}
	
	private void doOpenToolbarSettings(UserRequest ureq) {
		removeControllerListener(toolbarCtrl);
		
		ICourse course = CourseFactory.loadCourse(entry);
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Toolbar"), null);
		toolbarCtrl = new CourseToolbarController(ureq, swControl, entry, course, readOnly);
		listenTo(toolbarCtrl);
		mainPanel.setContent(toolbarCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(toolbarLink);
	}
	
	private void doOpenAssessmentSettings(UserRequest ureq) {
		removeControllerListener(assessmentSettingsCtrl);
		
		ICourse course = CourseFactory.loadCourse(entry);
		CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig().clone();
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Assessment"), null);
		assessmentSettingsCtrl = new CourseAssessmentSettingsController(ureq, swControl, entry, courseConfig, !readOnly);
		listenTo(assessmentSettingsCtrl);
		mainPanel.setContent(assessmentSettingsCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(assessmentLink);
	}
	
	private void doOpenOptions(UserRequest ureq) {
		removeControllerListener(optionsCtrl);
		
		ICourse course = CourseFactory.loadCourse(entry);
		CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig().clone();
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Options"), null);
		optionsCtrl = new CourseOptionsController(ureq, swControl, entry, courseConfig, !readOnly);
		listenTo(optionsCtrl);
		mainPanel.setContent(optionsCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(optionsLink);
	}
	
	private void doOpenDisclaimer(UserRequest ureq) {
		removeControllerListener(courseDisclaimerCtrl);
		
		entry = repositoryService.loadByKey(entry.getKey());
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Disclaimer"), null);
		courseDisclaimerCtrl = new CourseDisclaimerController(ureq, swControl, entry, readOnly);
		listenTo(courseDisclaimerCtrl);
		mainPanel.setContent(courseDisclaimerCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(disclaimerLink);
	}
}
