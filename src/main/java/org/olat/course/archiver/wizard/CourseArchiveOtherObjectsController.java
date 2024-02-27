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
package org.olat.course.archiver.wizard;

import java.util.Collection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * 
 * Initial date: 16 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseArchiveOtherObjectsController extends StepFormBasicController {
	
	private static final String LOG_FILES = "logfiles";
	private static final String COURSE_RESULTS = "courseresults";
	private static final String COURSE_CHAT = "coursechat";
	
	private static final String LOG_AUTHORS = "a";
	private static final String LOG_USERS = "u";
	private static final String LOG_STATISTICS = "s";
	
	private DateChooser rangeEl;
	private MultipleSelectionElement othersEl;
	private MultipleSelectionElement logFilesOptionsEl;
	private FormLayoutContainer logsCont;
	
	private final CourseArchiveOptions archiveOptions;
	private final CourseArchiveContext archiveContext;

	
	public CourseArchiveOtherObjectsController(UserRequest ureq, WindowControl wControl,
			CourseArchiveContext archiveContext, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		this.archiveContext = archiveContext;
		archiveOptions = archiveContext.getArchiveOptions();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer othersCont = uifactory.addDefaultFormLayout("others", null, formLayout);

		SelectionValues othersPK = new SelectionValues();
		othersPK.add(SelectionValues.entry(LOG_FILES, translate("others.objects.log.files")));
		othersPK.add(SelectionValues.entry(COURSE_RESULTS, translate("others.objects.course.results")));
		othersPK.add(SelectionValues.entry(COURSE_CHAT, translate("others.objects.course.chat")));
		othersEl = uifactory.addCheckboxesVertical("others.objects", "others.objects", othersCont,
				othersPK.keys(), othersPK.values(), 1);
		othersEl.addActionListener(FormEvent.ONCHANGE);
		othersEl.select(LOG_FILES, archiveOptions.isLogFiles());
		othersEl.select(COURSE_RESULTS, archiveOptions.isCourseResults());
		othersEl.select(COURSE_CHAT, archiveOptions.isCourseChat());
		
		logsCont = uifactory.addDefaultFormLayout("logs.files", null, formLayout);
		logsCont.setFormTitle(translate("log.files.title"));
		
		SelectionValues logFilesOptionsPK = new SelectionValues();
		if(archiveContext.isAllowedLogAuthors()) {
			logFilesOptionsPK.add(SelectionValues.entry(LOG_AUTHORS, translate("log.files.authors")));
		}
		if(archiveContext.isAllowedLogUsers()) {
			logFilesOptionsPK.add(SelectionValues.entry(LOG_USERS, translate("log.files.users")));
		}
		if(archiveContext.isAllowedLogStatistics()) {
			logFilesOptionsPK.add(SelectionValues.entry(LOG_STATISTICS, translate("log.files.statistics")));
		}
		logFilesOptionsEl = uifactory.addCheckboxesVertical("log.files.options", "log.files.options", logsCont,
				logFilesOptionsPK.keys(), logFilesOptionsPK.values(), 1);
		if(archiveContext.isAllowedLogAuthors() && archiveOptions.isLogFilesAuthors()) {
			logFilesOptionsEl.select(LOG_AUTHORS, true);
		}
		if(archiveContext.isAllowedLogUsers() && archiveOptions.isLogFilesUsers()) {
			logFilesOptionsEl.select(LOG_USERS, true);
		}
		if(archiveContext.isAllowedLogStatistics() && archiveOptions.isLogFilesStatistics()) {
			logFilesOptionsEl.select(LOG_STATISTICS, true);
		}
		
		rangeEl = uifactory.addDateChooser("log.files.range", "log.files.range", null, logsCont);
		rangeEl.setSecondDate(true);
		updateUI();
	}
	
	private void updateUI() {
		Collection<String> othersLogs = othersEl.getSelectedKeys();
		logsCont.setVisible(othersLogs.contains(LOG_FILES));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == othersEl) {
			if(!logsCont.isVisible()) {
				// Replay the default
				logFilesOptionsEl.select(LOG_AUTHORS, archiveOptions.isLogFilesAuthors());
				logFilesOptionsEl.select(LOG_USERS, archiveOptions.isLogFilesUsers());
				logFilesOptionsEl.select(LOG_STATISTICS, archiveOptions.isLogFilesStatistics());
			}
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Collection<String> others = othersEl.getSelectedKeys();
		archiveOptions.setLogFiles(others.contains(LOG_FILES));
		archiveOptions.setCourseResults(others.contains(COURSE_RESULTS));
		archiveOptions.setCourseChat(others.contains(COURSE_CHAT));
		
		Collection<String> logFilesOptions = logFilesOptionsEl.getSelectedKeys();
		archiveOptions.setLogFilesAuthors(logFilesOptions.contains(LOG_AUTHORS));
		archiveOptions.setLogFilesUsers(logFilesOptions.contains(LOG_USERS));
		archiveOptions.setLogFilesStatistics(logFilesOptions.contains(LOG_STATISTICS));
		archiveOptions.setLogFilesStartDate(rangeEl.getDate());
		archiveOptions.setLogFilesEndDate(rangeEl.getSecondDate());
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
