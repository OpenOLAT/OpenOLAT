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
package org.olat.course.assessment.ui.inspection;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateTimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.session.UserSessionManager;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentInspection;
import org.olat.course.assessment.AssessmentInspectionService;
import org.olat.course.assessment.AssessmentInspectionStatusEnum;
import org.olat.course.assessment.model.AssessmentEntryInspection;
import org.olat.course.assessment.ui.inspection.AssessmentInspectionOverviewListModel.OverviewCols;
import org.olat.course.assessment.ui.inspection.elements.CourseNodeCellRenderer;
import org.olat.course.assessment.ui.inspection.elements.MinuteCellRenderer;
import org.olat.course.assessment.ui.tool.event.AssessmentInspectionSelectionEvent;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionSmallOverviewController extends FormBasicController {
	
	private FormLink scheduledLink;
	private FormLink inProgressLink;
	private FlexiTableElement scheduledTable;
	private FlexiTableElement inProgressTable;
	private AssessmentInspectionOverviewListModel scheduledModel;
	private AssessmentInspectionOverviewListModel inProgressModel;

	private int counter = 0;
	private final RepositoryEntry courseEntry;
	
	private CloseableModalController cmc;
	private ConfirmCancelInspectionController confirmCancelCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserSessionManager sessionManager;
	@Autowired
	private AssessmentInspectionService inspectionService;
	
	public AssessmentInspectionSmallOverviewController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry) {
		super(ureq, wControl, "small_overviews");
		this.courseEntry = courseEntry;
		initForm(ureq);
		loadModel();
	}
	
	public int getNumOfInspections() {
		return scheduledModel.getRowCount() + inProgressModel.getRowCount();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		scheduledLink = uifactory.addFormLink("link.in.progress", "overview.small.link.in.progress.plural", null, formLayout,
				Link.LINK | Link.NONTRANSLATED);
		scheduledLink.setIconLeftCSS("o_icon o_icon-fw o_icon_time");
		inProgressLink = uifactory.addFormLink("link.scheduled", "overview.small.link.scheduled.plural", null, formLayout,
				Link.LINK | Link.NONTRANSLATED);
		inProgressLink.setIconLeftCSS("o_icon o_icon-fw o_icon_status_in_progress");

		initInProgressForm(formLayout);
		initScheduledForm(formLayout);
	}
	
	private void initInProgressForm(FormItemContainer formLayout) {	
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.courseNode,
					new CourseNodeCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.participant));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.inspectionStart,
				new DateTimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.inspectionDurationShort,
				new MinuteCellRenderer(getTranslator())));

		StickyActionColumnModel cancelCol = new StickyActionColumnModel(OverviewCols.cancel);
		cancelCol.setExportable(false);
		cancelCol.setIconHeader("o_icon o_icon_cancel o_icon-fw o_icon-lg");
		columnsModel.addFlexiColumnModel(cancelCol);

		inProgressModel = new AssessmentInspectionOverviewListModel(columnsModel, getIdentity(), sessionManager, getLocale());
		inProgressTable = uifactory.addTableElement(getWindowControl(), "inProgressTable", inProgressModel, 25, false, getTranslator(), formLayout);
		inProgressTable.setCustomizeColumns(false);
		inProgressTable.setNumOfRowsEnabled(false);
		inProgressTable.setCssDelegate(new AssessmentInspectionTableCSSDelegate(inProgressModel));
	}
	
	private void initScheduledForm(FormItemContainer formLayout) {	
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.courseNode,
					new CourseNodeCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.participant));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.inspectionStart,
				new DateTimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.inspectionDuration,
				new MinuteCellRenderer(getTranslator())));

		scheduledModel = new AssessmentInspectionOverviewListModel(columnsModel, getIdentity(), sessionManager, getLocale());
		scheduledTable = uifactory.addTableElement(getWindowControl(), "scheduledTable", scheduledModel, 25, false, getTranslator(), formLayout);
		scheduledTable.setCustomizeColumns(false);
		scheduledTable.setNumOfRowsEnabled(false);
		scheduledTable.setCssDelegate(new AssessmentInspectionTableCSSDelegate(scheduledModel));
	}
	
	private void loadModel() {
		SearchAssessmentInspectionParameters params = new SearchAssessmentInspectionParameters();
		params.setInspectionStatus(List.of(AssessmentInspectionStatusEnum.scheduled, AssessmentInspectionStatusEnum.inProgress));
		params.setEntry(courseEntry);
		
		List<AssessmentEntryInspection> inspectionEntryList = inspectionService.searchInspection(params);
		List<AssessmentInspectionRow> scheduledRows = new ArrayList<>(inspectionEntryList.size());
		List<AssessmentInspectionRow> inProgressRows = new ArrayList<>(inspectionEntryList.size());
		
		if(!inspectionEntryList.isEmpty()) {
			ICourse course = CourseFactory.loadCourse(courseEntry);
			for(AssessmentEntryInspection inspectionEntry:inspectionEntryList) {
				AssessmentInspection inspection = inspectionEntry.inspection();
				if(inspection.getInspectionStatus() == AssessmentInspectionStatusEnum.scheduled) {
					scheduledRows.add(forgeRow(inspectionEntry, course));
				} else if(inspection.getInspectionStatus() == AssessmentInspectionStatusEnum.inProgress) {
					inProgressRows.add(forgeRow(inspectionEntry, course));
				}
			}
		}
		
		scheduledModel.setObjects(scheduledRows);
		inProgressModel.setObjects(inProgressRows);
		scheduledTable.reset(true, true, true);
		scheduledTable.setVisible(!scheduledRows.isEmpty());
		inProgressTable.reset(true, true, true);
		inProgressTable.setVisible(!inProgressRows.isEmpty());
		
		String i18n = inProgressRows.size() == 1 ? "overview.small.link.in.progress.singular" : "overview.small.link.in.progress.plural";
		inProgressLink.setI18nKey(translate(i18n, Integer.toString(inProgressRows.size())));
		inProgressLink.setVisible(!inProgressRows.isEmpty());
		
		String scheduledI18n = scheduledRows.size() == 1 ? "overview.small.link.scheduled.singular" : "overview.small.link.scheduled.plural";
		scheduledLink.setI18nKey(translate(scheduledI18n, Integer.toString(scheduledRows.size())));
		scheduledLink.setVisible(!scheduledRows.isEmpty());
		
		flc.setDirty(true);
	}
	
	private AssessmentInspectionRow forgeRow(AssessmentEntryInspection inspectionEntry, ICourse course) {
		AssessmentInspection inspection = inspectionEntry.inspection();
		String courseNodeIdent = inspection.getSubIdent();
		CourseNode cNode = course.getRunStructure().getNode(courseNodeIdent);
		CourseNodeConfiguration courseNodeConfig = CourseNodeFactory.getInstance().getCourseNodeConfiguration(cNode.getType());
		
		String fullName = userManager.getUserDisplayName(inspectionEntry.identity());
		AssessmentInspectionRow row = new AssessmentInspectionRow(fullName, inspectionEntry.inspection(),
				inspectionEntry.assessmentStatus(), cNode.getShortTitle(), courseNodeConfig.getIconCSSClass());
		
		FormLink cancelLink = uifactory.addFormLink("cancel_" + (++counter), "cancel", "", null, null, Link.NONTRANSLATED);
		cancelLink.setIconLeftCSS("o_icon o_icon_cancel o_icon-fws o_icon-lg");
		cancelLink.setVisible(inspection.getInspectionStatus() == AssessmentInspectionStatusEnum.inProgress);
		row.setCancelButton(cancelLink);
		cancelLink.setUserObject(row);
		
		return row;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmCancelCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmCancelCtrl);
		removeAsListenerAndDispose(cmc);
		confirmCancelCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(inProgressLink == source) {
			fireEvent(ureq, new AssessmentInspectionSelectionEvent(AssessmentInspectionStatusEnum.inProgress));
		} else if(scheduledLink == source) {
			fireEvent(ureq, new AssessmentInspectionSelectionEvent(AssessmentInspectionStatusEnum.scheduled));
		} else if(source instanceof FormLink link) {
			if("cancel".equals(link.getCmd()) && link.getUserObject() instanceof AssessmentInspectionRow row) {
				doConfirmCancelInspection(ureq, row.getInspection());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doConfirmCancelInspection(UserRequest ureq, AssessmentInspection inspection) {
		List<AssessmentInspection> inspectionList = List.of(inspection);
		confirmCancelCtrl = new ConfirmCancelInspectionController(ureq, getWindowControl(), inspectionList);
		listenTo(confirmCancelCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmCancelCtrl.getInitialComponent(), true, translate("bulk.cancel"));
		cmc.activate();
		listenTo(cmc);
	}
}
