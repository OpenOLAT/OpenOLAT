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
package org.olat.modules.scorm.assessment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.scorm.ScormSessionController;
import org.olat.modules.scorm.assessment.ScoSummaryTableModel.SummaryCols;
import org.olat.modules.scorm.server.servermodels.ScoUtils;

/**
 * 
 * Description:<br>
 * Show the assessment's details of SCORM course for a selected user. A popup shows all the
 * CMI datas of the SCOs visited by the user
 * 
 * <P>
 * Initial Date:  17 août 2009 <br>
 * @author srosse
 */
public class ScormResultDetailsController extends FormBasicController {
	
	private static final String CMD_SELECT = "select";
	protected static final String FILTER_LAST_RUN = "LastRun";
	
	public static final String ALL_TAB_ID = "All";
	public static final String LAST_RUN_TAB_ID = "LastRun";

	private FormLink resetButton;
	private FlexiTableElement tableEl;
	private ScoSummaryTableModel tableModel;
	
	private CloseableModalController cmc;
	private ScoCmiDetailsController cmiDataCtrl;
	private DialogBoxController resetConfirmationBox;
	
	private final CourseNode node;
	private final UserCourseEnvironment coachCourseEnv;
	private final UserCourseEnvironment assessedUserCourseEnv;

	public ScormResultDetailsController(UserRequest ureq, WindowControl wControl, CourseNode node,
			UserCourseEnvironment coachCourseEnv, UserCourseEnvironment assessedUserCourseEnv) {
		super(ureq, wControl, "scores");
		
		this.node = node;
		this.coachCourseEnv = coachCourseEnv;
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {		
		if(!coachCourseEnv.isCourseReadOnly()) {
			resetButton = uifactory.addFormLink("reset", formLayout, Link.BUTTON);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SummaryCols.lastRun,
				new ScoLastRunCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SummaryCols.scoId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SummaryCols.date));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SummaryCols.duration));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SummaryCols.score));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), CMD_SELECT));
		
		tableModel = new ScoSummaryTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "summary", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSearchEnabled(true);
		
		initFilters();
		initFiltersPresets(ureq);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();

		SelectionValues lastRunValues = new SelectionValues();
		lastRunValues.add(SelectionValues.entry(FILTER_LAST_RUN, translate("filter.last.run")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("filter.last.run"),
				FILTER_LAST_RUN, lastRunValues, true));

		tableEl.setFilters(true, filters, false, false);
	}
	
	private void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(2);
		
		FlexiFiltersTab allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		allTab.setElementCssClass("o_sel_assessment_all");
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);

		FlexiFiltersTab markedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(LAST_RUN_TAB_ID, translate("filter.last.run"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FILTER_LAST_RUN, FILTER_LAST_RUN)));
		markedTab.setFiltersExpanded(true);
		tabs.add(markedTab);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}
	
	private void loadModel() {
		CourseEnvironment courseEnv = assessedUserCourseEnv.getCourseEnvironment();
		String username = assessedUserCourseEnv.getIdentityEnvironment().getIdentity().getName();

		Map<Date, List<CmiData>> rawDatas = ScormAssessmentManager.getInstance().visitScoDatasMultiResults(username, courseEnv, node);
		if(rawDatas == null) {
			tableModel.setObjects(new ArrayList<>());
		} else {
			List<ScoSummaryRow> rows = new ArrayList<>(rawDatas.size());
			for(Map.Entry<Date, List<CmiData>> entry:rawDatas.entrySet()) {
				Date date = entry.getKey();
				List<CmiData> cmiData = entry.getValue();
				ScoId scoId = getScoId(date, cmiData);
				ScoreAndTotalTime timeScore =  totalTimeAndScore(cmiData);
				rows.add(new ScoSummaryRow(scoId.id(), scoId.last(), date, timeScore.score(), timeScore.totalTime(), cmiData));
			}
			tableModel.setObjects(rows);
		}
		tableEl.reset(true, true, true);
	}

	private ScoId getScoId(Date date, List<CmiData> cmiObject) {
		if(cmiObject != null && !cmiObject.isEmpty()) {
			CmiData data = cmiObject.get(0);
			long time = date.getTime();
			String itemId = data.getItemId();
			int index = itemId.lastIndexOf('_');
			if(index >= 0) {
				String marker = itemId.substring(index + 1, itemId.length());
				if(StringHelper.isLong(marker)) {
					long longMarker = Long.parseLong(marker);
					long diff = Math.abs(time - longMarker);
					if(diff < 1000) {// less than a second
						return new ScoId(itemId.substring(0, index), false);
					}
				}
			}
			return new ScoId(data.getItemId(), true);
		}
		return null;
	}
	
	private ScoreAndTotalTime totalTimeAndScore(List<CmiData> cmiObject) {
		Double score = null;
		String totalTime = null;
		for(CmiData data:cmiObject) {
			String key = data.getKey();
			if(ScormSessionController.CMI_RAW_SCORE.equals(key)) {
				String value = data.getValue();
				if(StringHelper.containsNonWhitespace(value)) {
					try {
						if(score == null) {
							score = Double.valueOf(value);
						} else {
							score = Double.valueOf(score.doubleValue() + Double.parseDouble(value));
						}
					} catch (NumberFormatException e) {
						getLogger().debug("", e);
					}
				}
			} else if(ScormSessionController.CMI_TOTAL_TIME.equals(key)) {
				String value = data.getValue();
				if(StringHelper.containsNonWhitespace(value)) {
					if(totalTime == null) {
						totalTime = value;
					} else {
						totalTime = ScoUtils.addTimes(totalTime, value);
					}
				}
			}
		}
		return new ScoreAndTotalTime(score, totalTime);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se && CMD_SELECT.equals(se.getCommand())) {
				ScoSummaryRow row = tableModel.getObject(se.getIndex());
				doSelectSco(ureq, row);
			} else if(event instanceof FlexiTableSearchEvent
					|| event instanceof FlexiTableSearchEvent
					|| event instanceof FlexiTableFilterTabEvent) {
				tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
				tableEl.reset(true, true, true);
			}
		} else if(resetButton == source) {
			doConfirmReset(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (resetConfirmationBox == source) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				doReset(ureq);
			}
		} else if(cmiDataCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeControllerListener(cmiDataCtrl);
		removeControllerListener(cmc);
		cmiDataCtrl = null;
		cmc = null;
	}
	
	private void doConfirmReset(UserRequest ureq) {
		String title = translate("reset.title");
		User user = assessedUserCourseEnv.getIdentityEnvironment().getIdentity().getUser();
		String name = user.getProperty(UserConstants.FIRSTNAME, null) + " " + user.getProperty(UserConstants.LASTNAME, null);
		String text = translate("reset.text", name);
		resetConfirmationBox = activateOkCancelDialog(ureq, title, text, resetConfirmationBox);
	}
	
	/**
	 * Delete the SCO files
	 * 
	 * @param ureq The user request
	 */
	private void doReset(UserRequest ureq) {
		Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		CourseEnvironment courseEnv = assessedUserCourseEnv.getCourseEnvironment();
		ScormAssessmentManager.getInstance().deleteResults(assessedIdentity, courseEnv, node);
		fireEvent(ureq, Event.DONE_EVENT);
		loadModel();
	}
	
	private void doSelectSco(UserRequest ureq, ScoSummaryRow row) {
		removeAsListenerAndDispose(cmiDataCtrl);
		removeAsListenerAndDispose(cmc);
		
		cmiDataCtrl = new ScoCmiDetailsController(ureq, getWindowControl(), row.getCmiData());
		listenTo(cmiDataCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), cmiDataCtrl.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}
	
	private record ScoId(String id, boolean last) {
		//
	}
	
	private record ScoreAndTotalTime(Double score, String totalTime) {
		//
	}
}