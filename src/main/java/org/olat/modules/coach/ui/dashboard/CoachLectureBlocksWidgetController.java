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
package org.olat.modules.coach.ui.dashboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.lecture.ui.ConfigurationHelper;
import org.olat.modules.lecture.ui.LectureBlocksWidgetController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jan 8, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CoachLectureBlocksWidgetController extends LectureBlocksWidgetController {

	private static final String EVENTS_PATH = "[CoachSite:0][Events:0][Teacher:0][Teacher:0]";
	
	private FormLink openAbsencesLink;
	private final Map<Long, Boolean> entryKeyToRollCallEnabled = new HashMap<>();
	
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;

	public CoachLectureBlocksWidgetController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
		
		setUrl(showAllLink, EVENTS_PATH);
		setUrl(openAbsencesLink, EVENTS_PATH + "[Pending:0]");
	}
	
	@Override
	protected List<String> createFooterButtons(FormLayoutContainer widgetCont) {
		openAbsencesLink = uifactory.addFormLink("open.absences", "dashboard.widget.open.absences", null, widgetCont, Link.BUTTON);
		openAbsencesLink.setPrimary(true);
		openAbsencesLink.getComponent().setAriaRole(Link.ARIA_ROLE_LINK);
		
		List<String> buttonNames = new ArrayList<>();
		buttonNames.add(openAbsencesLink.getComponent().getComponentName());
		buttonNames.addAll(super.createFooterButtons(widgetCont));
		return buttonNames;
	}
	
	@Override
	public void reload() {
		entryKeyToRollCallEnabled.clear();
		super.reload();
		updateOpenAbsencesUI();
	}
	
	private void updateOpenAbsencesUI() {
		LecturesBlockSearchParameters searchParams = getSearchParams();
		searchParams.setEndDate(new Date());
		searchParams.addRollCallStatus(LectureRollCallStatus.open, LectureRollCallStatus.reopen);
		long numOpenAbsences = lectureService.countLectureBlocks(searchParams);
		
		openAbsencesLink.setI18nKey("dashboard.widget.open.absences", String.valueOf(numOpenAbsences));
		openAbsencesLink.setVisible(numOpenAbsences > 0);
	}

	@Override
	protected List<LectureBlock> loadLectureBlocks(Date fromDate, Date toDate) {
		// see LectureListRepositoryController
		
		LecturesBlockSearchParameters searchParams = getSearchParams();
		searchParams.setStartDate(fromDate);
		searchParams.setEndDate(toDate);
		return lectureService.getLectureBlocks(searchParams, -1, null);
	}

	@Override
	protected LectureBlockRef loadNextScheduledBlock() {
		LecturesBlockSearchParameters nextParams = getSearchParams();
		nextParams.setStartDate(new Date());
		return lectureService.getNextScheduledLectureBlock(nextParams);
	}

	@Override
	protected Date getPrevLectureBlock(Date date) {
		LecturesBlockSearchParameters searchParams = getSearchParams();
		searchParams.setStartDateBefore(date);
		List<LectureBlock> lectureBlocks = lectureService.getLectureBlocks(searchParams, 1, Boolean.FALSE);
		return !lectureBlocks.isEmpty()? lectureBlocks.get(0).getStartDate(): null;
	}

	@Override
	protected Date getNextLectureBlock(Date date) {
		LecturesBlockSearchParameters searchParams = getSearchParams();
		searchParams.setStartDate(date);
		List<LectureBlock> lectureBlocks = lectureService.getLectureBlocks(searchParams, 1, Boolean.TRUE);
		return !lectureBlocks.isEmpty()? lectureBlocks.get(0).getStartDate(): null;
	}
	
	private LecturesBlockSearchParameters getSearchParams() {
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setTeacher(getIdentity());
		searchParams.setInSomeCurriculum(false);
		searchParams.setLectureConfiguredRepositoryEntry(true);
		return searchParams;
	}
	
	@Override
	protected void setLectureBlockUrl(FormLink link, LectureBlock lectureBlock) {
		setUrl(link, getBusinessPath(lectureBlock));
	}
	
	@Override
	protected FormLink createRollCallLink(LectureBlock lectureBlock) {
		if (lectureBlock.getStartDate().after(new Date()) || !isRollCallEnabled(lectureBlock.getEntry())) {
			return null;
		}
		
		FormLink rollCallLink = addRollCallLink(lectureBlock);
		setUrl(rollCallLink, EVENTS_PATH + "[Lecture:" + lectureBlock.getKey() + "][Start:0]");
		return rollCallLink;
	}
	
	private boolean isRollCallEnabled(RepositoryEntry entry) {
		return entryKeyToRollCallEnabled.computeIfAbsent(entry.getKey(), _ -> ConfigurationHelper
				.isRollCallEnabled(lectureService.getRepositoryEntryLectureConfiguration(entry), lectureModule));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink link && link.getUserObject() instanceof String businessPath) {
			doOpen(ureq, businessPath);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private String getBusinessPath(LectureBlock lectureBlock) {
		return "[RepositoryEntry:" + lectureBlock.getEntry().getKey() + "][Lectures:0][LectureBlock:" + lectureBlock.getKey() + "]";
	}
	
	private void doOpen(UserRequest ureq, String businessPath) {
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

}
