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
package org.olat.modules.curriculum.ui.lectures;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExportableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.ui.lectures.LecturesListDataModel.StatsCols;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.AggregatedLectureBlocksStatistics;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.ParticipantLecturesOverviewController;
import org.olat.modules.lecture.ui.coach.LecturesStatisticsExport;
import org.olat.modules.lecture.ui.component.LectureStatisticsCellRenderer;
import org.olat.modules.lecture.ui.component.PercentCellRenderer;
import org.olat.repository.RepositoryEntryRef;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesListController extends FormBasicController implements ExportableFlexiTableDataModel {
	
	public static final int USER_PROPS_OFFSET = 500;
	
	private FlexiTableElement tableEl;
	private LecturesListDataModel tableModel;
	private TooledStackedPanel toolbarPanel;
	
	private final String propsIdentifier;
	private final boolean absenceNoticeEnabled;
	private final boolean authorizedAbsenceEnabled;
	private final Curriculum curriculum;
	private final CurriculumElement curriculumElement;
	private final List<RepositoryEntryRef> filterByEntries;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final List<LectureBlockIdentityStatistics> statistics;
	
	private ParticipantLecturesOverviewController participantLecturesOverviewCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private BaseSecurity securityManager;
	
	public LecturesListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			List<LectureBlockIdentityStatistics> statistics, List<RepositoryEntryRef> filterByEntries,
			Curriculum curriculum, CurriculumElement curriculumElement,
			List<UserPropertyHandler> userPropertyHandlers, String propsIdentifier) {
		super(ureq, wControl, "curriculum_lectures_table", Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.toolbarPanel = toolbarPanel;
		this.statistics = statistics;
		this.filterByEntries = filterByEntries;
		this.propsIdentifier = propsIdentifier;
		this.curriculum = curriculum;
		this.curriculumElement = curriculumElement;
		this.userPropertyHandlers = userPropertyHandlers;
		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		absenceNoticeEnabled = lectureModule.isAbsenceNoticeEnabled();
		lectureModule.isAbsenceNoticeEnabled();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("authorizedAbsenceEnabled", Boolean.valueOf(authorizedAbsenceEnabled));
			layoutCont.contextPut("absenceNoticeEnabled", Boolean.valueOf(absenceNoticeEnabled));
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, StatsCols.id));
		
		int colIndex = USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(propsIdentifier, userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, "select",
					true, userPropertyHandler.i18nColumnDescriptorLabelKey()));
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatsCols.plannedLectures));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatsCols.attendedLectures));
		if(authorizedAbsenceEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatsCols.unauthorizedAbsenceLectures));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatsCols.authorizedAbsenceLectures));
			if(absenceNoticeEnabled) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatsCols.dispensedLectures));
			}
		} else {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatsCols.absentLectures));
		}
		// add progress
		FlexiColumnModel progressCol = new DefaultFlexiColumnModel(StatsCols.progress, new LectureStatisticsCellRenderer());
		progressCol.setExportable(false);
		columnsModel.addFlexiColumnModel(progressCol);
		
		FlexiColumnModel warningCol = new DefaultFlexiColumnModel(StatsCols.rateWarning, new RateExplicitWarningCellRenderer(getTranslator()));
		warningCol.setExportable(false);
		columnsModel.addFlexiColumnModel(warningCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatsCols.currentRate, new PercentCellRenderer()));
		
		tableModel = new LecturesListDataModel(this, columnsModel, getTranslator());
		AggregatedLectureBlocksStatistics total = lectureService.aggregatedStatistics(statistics);
		tableModel.setObjects(statistics, total);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setFooter(true);
		tableEl.setFilters("activity", getFilters(), false);
	}
	
	private List<FlexiTableFilter> getFilters() {
		List<FlexiTableFilter> filters = new ArrayList<>(5);
		filters.add(new FlexiTableFilter(translate("filter.warning"), "warning"));
		filters.add(FlexiTableFilter.SPACER);
		filters.add(new FlexiTableFilter(translate("filter.showAll"), "all", true));
		return filters;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("select".equals(cmd)) {
					LectureBlockIdentityStatistics row = tableModel.getObject(se.getIndex());
					doSelectAssessedIdentity(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	public MediaResource export(FlexiTableComponent ftC) {
		return new LecturesStatisticsExport(statistics, curriculum, curriculumElement, userPropertyHandlers, getTranslator());
	}
	
	private void doSelectAssessedIdentity(UserRequest ureq, LectureBlockIdentityStatistics row) {
		removeControllerListener(participantLecturesOverviewCtrl);
		
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		participantLecturesOverviewCtrl = new ParticipantLecturesOverviewController(ureq, getWindowControl(),
				assessedIdentity, filterByEntries, true, true, true, true, false, false, false);
		listenTo(participantLecturesOverviewCtrl);
		participantLecturesOverviewCtrl.setBreadcrumbPanel(toolbarPanel);
		
		String fullName = userManager.getUserDisplayName(assessedIdentity);
		toolbarPanel.pushController(fullName, participantLecturesOverviewCtrl);
	}
}
