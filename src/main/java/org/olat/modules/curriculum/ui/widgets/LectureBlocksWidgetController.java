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
package org.olat.modules.curriculum.ui.widgets;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.ui.CurriculumDashboardController;
import org.olat.modules.curriculum.ui.CurriculumElementLectureBlocksTableModel.BlockCols;
import org.olat.modules.curriculum.ui.CurriculumListManagerController;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.addwizard.AddLectureBlock1ResourcesStep;
import org.olat.modules.lecture.ui.addwizard.AddLectureBlockStepCallback;
import org.olat.modules.lecture.ui.addwizard.AddLectureContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlocksWidgetController extends FormBasicController {

	private FormLink lecturesLink;
	private FormLink minimizeButton;
	private FormLink addLecturesLink;
	private FlexiTableElement todayTableEl;
	private FlexiTableElement nextDaysTableEl;
	private LectureBlocksWidgetTableModel todayTableModel;
	private LectureBlocksWidgetTableModel nextDaysTableModel;
	private StaticTextElement eventsTodayEl;
	private StaticTextElement eventsNextDaysEl;

	private AtomicBoolean minimized;
	private Curriculum curriculum;
	private final String preferencesId;
	private CurriculumElement curriculumElement;
	private final LecturesSecurityCallback secCallback;

	private StepsMainRunController addLectureCtrl;
	
	@Autowired
	private LectureService lectureService;
	
	public LectureBlocksWidgetController(UserRequest ureq, WindowControl wControl,
			LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "events_widget", Util
				.createPackageTranslator(CurriculumDashboardController.class, ureq.getLocale()));
		this.secCallback = secCallback;
		preferencesId = "widget-cur-mgmt";
		initForm(ureq);
		loadModel(ureq.getRequestTimestamp());
	}
	
	public LectureBlocksWidgetController(UserRequest ureq, WindowControl wControl,
			Curriculum curriculum, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "events_widget", Util
				.createPackageTranslator(CurriculumDashboardController.class, ureq.getLocale()));
		this.curriculum = curriculum;
		this.secCallback = secCallback;
		preferencesId = "widget-lectures-cur-" + curriculum.getKey();
		initForm(ureq);
		loadModel(ureq.getRequestTimestamp());
	}
	
	public LectureBlocksWidgetController(UserRequest ureq, WindowControl wControl,
			CurriculumElement curriculumElement, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "events_widget", Util
				.createPackageTranslator(CurriculumDashboardController.class, ureq.getLocale()));
		this.curriculumElement = curriculumElement;
		this.secCallback = secCallback;
		preferencesId = "widget-lectures-cur-el-" + curriculumElement.getKey();
		initForm(ureq);
		loadModel(ureq.getRequestTimestamp());
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		lecturesLink = uifactory.addFormLink("curriculum.lectures", formLayout);
		lecturesLink.setIconRightCSS("o_icon o_icon_course_next");
		lecturesLink.setIconLeftCSS("o_icon o_icon-fw o_icon_calendar_day");
		
		Boolean minimizedObj = (Boolean)ureq.getUserSession()
				.getGuiPreferences()
				.get(LectureBlocksWidgetController.class, preferencesId, Boolean.FALSE);
		minimized = new AtomicBoolean(minimizedObj != null && minimizedObj.booleanValue());
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("minimized", minimized);
		}
		
		minimizeButton = uifactory.addFormLink("curriculum.minimize", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		minimizeButton.setTitle(translate("curriculum.minimize"));
		minimizeButton.setElementCssClass("o_button_details");
		updateMinimizeButton();
		
		if(secCallback.canNewLectureBlock() && (curriculum != null || curriculumElement != null)) {
			addLecturesLink = uifactory.addFormLink("curriculum.add.lectures", "", null, formLayout, Link.LINK | Link.NONTRANSLATED);
			addLecturesLink.setIconLeftCSS("o_icon o_icon_add");
			addLecturesLink.setTitle(translate("curriculum.add.lectures"));
		}
		
		eventsTodayEl = uifactory.addStaticTextElement("num.of.events.today", "", formLayout);
		eventsNextDaysEl = uifactory.addStaticTextElement("num.of.events.next.days", "", formLayout);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.title, "select",
				new LectureBlockTitleCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.location,
				new LectureBlockLocationCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.title,
				new LectureBlockStartDateCellRenderer(getLocale())));
		
		todayTableModel = new LectureBlocksWidgetTableModel(columnsModel);
		todayTableEl = initTable(formLayout, "today", todayTableModel);
		todayTableEl.setEmptyTableMessageKey("empty.today.lectures");

		nextDaysTableModel = new LectureBlocksWidgetTableModel(columnsModel);
		nextDaysTableEl = initTable(formLayout, "nextDays", nextDaysTableModel);
		nextDaysTableEl.setEmptyTableMessageKey("empty.next.days.lectures");
	}
	
	private void updateMinimizeButton() {
		if(minimized.get()) {
			minimizeButton.setIconLeftCSS("o_icon o_icon_details_expand");
		} else {
			minimizeButton.setIconLeftCSS("o_icon o_icon_details_collaps");
		}
	}

	private FlexiTableElement initTable(FormItemContainer formLayout, String name, LectureBlocksWidgetTableModel tableModel) {
		FlexiTableElement tableEl = nextDaysTableEl = uifactory.addTableElement(getWindowControl(), name, tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCssDelegate(tableModel);
		return tableEl;
	}
	
	private void loadModel(Date now) {
		loadTodayModel(now);
		loadNextDaysModel(now);
	}
	
	private void loadTodayModel(Date now) {
		LecturesBlockSearchParameters searchParams = getSearchParameters();
		searchParams.setStartDate(DateUtils.getStartOfDay(now));
		searchParams.setEndDate(DateUtils.getEndOfDay(now));
		
		List<LectureBlock> lectureBlocks = lectureService.getLectureBlocks(searchParams, -1, Boolean.TRUE);
		long numOfBlocks = lectureBlocks.size();
		trimBeforeNow(lectureBlocks, now);
		if(lectureBlocks.size() > 5) {
			lectureBlocks = new ArrayList<>(lectureBlocks.subList(0, 5));
		}
		List<LectureBlockWidgetRow> rows = lectureBlocks.stream()
				.map(LectureBlockWidgetRow::new)
				.toList();
		todayTableModel.setObjects(rows);
		todayTableEl.reset(true, true, true);
		eventsTodayEl.setValue(Long.toString(numOfBlocks));
	}
	
	private void trimBeforeNow(List<LectureBlock> lectureBlocks, Date now) {
		for(Iterator<LectureBlock> it=lectureBlocks.iterator(); it.hasNext() && lectureBlocks.size() > 5; ) {
			LectureBlock lectureBlock = it.next();
			if(lectureBlock.getEndDate() != null && lectureBlock.getEndDate().before(now)) {
				it.remove();
			}
		}
	}
	
	private void loadNextDaysModel(Date now) {
		LecturesBlockSearchParameters searchParams = getSearchParameters();
		searchParams.setStartDate(DateUtils.getEndOfDay(now));
		
		List<LectureBlock> lectureBlocks = lectureService.getLectureBlocks(searchParams, 5, Boolean.TRUE);
		long numOfBlocks = lectureBlocks.size();
		if(numOfBlocks > 5) {
			numOfBlocks = lectureService.countLectureBlocks(searchParams);
		}
		List<LectureBlockWidgetRow> rows = lectureBlocks.stream()
				.map(LectureBlockWidgetRow::new)
				.toList();
		nextDaysTableModel.setObjects(rows);
		nextDaysTableEl.reset(true, true, true);	
		eventsNextDaysEl.setValue(Long.toString(numOfBlocks));
	}
	
	private LecturesBlockSearchParameters getSearchParameters() {
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setLectureConfiguredRepositoryEntry(false);
		if(curriculum != null) {
			searchParams.setCurriculum(curriculum);
		} else if(curriculumElement != null) {
			searchParams.setCurriculum(curriculumElement.getCurriculum());
			searchParams.setCurriculumElementPath(curriculumElement.getMaterializedPathKeys());
		}
		return searchParams;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addLectureCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					loadModel(ureq.getRequestTimestamp());
				}
				cleanUp();
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(addLectureCtrl);
		addLectureCtrl = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(lecturesLink == source) {
			List<ContextEntry> entries = BusinessControlFactory.getInstance()
					.createCEListFromResourceType(CurriculumListManagerController.CONTEXT_LECTURES);
			fireEvent(ureq, new ActivateEvent(entries));
		} else if(addLecturesLink == source) {
			doAddLectureBlock(ureq);
		} else if(minimizeButton == source) {
			toogle(ureq);
		} else if(todayTableEl == source ) {
			if(event instanceof SelectionEvent se) {
				LectureBlockWidgetRow row = todayTableModel.getObject(se.getIndex());
				doOpen(ureq, row);
			}
		} else if(nextDaysTableEl == source) {
			if(event instanceof SelectionEvent se) {
				LectureBlockWidgetRow row = nextDaysTableModel.getObject(se.getIndex());
				doOpen(ureq, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpen(UserRequest ureq, LectureBlockWidgetRow row) {
		List<ContextEntry> entries = BusinessControlFactory.getInstance()
				.createCEListFromString("[Lectures:0][Lecture:" + row.getLectureBlock().getKey() + "]");
		fireEvent(ureq, new ActivateEvent(entries));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void toogle(UserRequest ureq) {
		minimized.set(!minimized.get());
		updateMinimizeButton();
		ureq.getUserSession().getGuiPreferences()
			.putAndSave(LectureBlocksWidgetController.class, preferencesId, Boolean.valueOf(minimized.get()));
	}
	
	private void doAddLectureBlock(UserRequest ureq) {
		if(guardModalController(addLectureCtrl) || !secCallback.canNewLectureBlock()) return;
		
		if(curriculumElement == null && curriculum == null) {
			showWarning("error.no.entry.curriculum");
			return;
		}
		
		AddLectureContext addLecture = new AddLectureContext(curriculum, curriculumElement, List.of());
		addLecture.setCurriculumElement(curriculumElement);
		
		AddLectureBlock1ResourcesStep step = new AddLectureBlock1ResourcesStep(ureq, addLecture);
		AddLectureBlockStepCallback stop = new AddLectureBlockStepCallback(addLecture);
		String title = translate("add.lecture");
		
		removeAsListenerAndDispose(addLectureCtrl);
		addLectureCtrl = new StepsMainRunController(ureq, getWindowControl(), step, stop, null, title, "");
		listenTo(addLectureCtrl);
		getWindowControl().pushAsModalDialog(addLectureCtrl.getInitialComponent());
	}
}
