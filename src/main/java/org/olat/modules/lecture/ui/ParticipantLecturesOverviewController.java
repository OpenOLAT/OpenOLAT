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
package org.olat.modules.lecture.ui;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.AggregatedLectureBlocksStatistics;
import org.olat.modules.lecture.model.LectureBlockStatistics;
import org.olat.modules.lecture.ui.ParticipantLecturesDataModel.LecturesCols;
import org.olat.modules.lecture.ui.component.LectureStatisticsCellRenderer;
import org.olat.modules.lecture.ui.component.PercentCellRenderer;
import org.olat.modules.lecture.ui.component.RateWarningCellRenderer;
import org.olat.modules.lecture.ui.export.IdentityAuditLogExport;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantLecturesOverviewController extends FormBasicController implements BreadcrumbPanelAware, Activateable2 {
	
	private FormLink logButton;
	private FlexiTableElement tableEl;
	private BreadcrumbPanel stackPanel;
	private ParticipantLecturesDataModel tableModel;
	
	private final boolean withLog;
	private final boolean withPrint;
	private final boolean withTitle;
	private final boolean withSelect;
	private final Identity assessedIdentity;
	private final boolean authorizedAbsenceEnabled;
	private final List<RepositoryEntryRef> filterByEntries;
	
	private ParticipantLectureBlocksController lectureBlocksCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryService repositoryService;
	
	public ParticipantLecturesOverviewController(UserRequest ureq, WindowControl wControl, boolean withTitle) {
		this(ureq, wControl, ureq.getIdentity(), null, true, true, false, withTitle);
	}
	
	public ParticipantLecturesOverviewController(UserRequest ureq, WindowControl wControl,
			Identity assessedIdentity, List<RepositoryEntryRef> filterByEntries,
			boolean withPrint, boolean withSelect, boolean withLog, boolean withTitle) {
		super(ureq, wControl, "participant_overview");
		this.withLog = withLog;
		this.withPrint = withPrint;
		this.withTitle = withTitle;
		this.withSelect = withSelect;
		this.assessedIdentity = assessedIdentity;
		this.filterByEntries = filterByEntries;
		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		initForm(ureq);
		loadModel();
	}
	
	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = stackPanel;
	}
	
	public int getRowCount() {
		return tableModel.getRowCount();
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(withPrint && formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("winid", "w" + layoutCont.getFormItemComponent().getDispatchID());
			layoutCont.getFormItemComponent().addListener(this);
			layoutCont.getFormItemComponent().contextPut("withPrint", Boolean.TRUE);
			if(withTitle) {
				setFormTitle("menu.my.lectures.alt");
			}
		} else if(withTitle) {
			setFormTitle("lectures.print.title", new String[]{
					StringHelper.escapeHtml(userManager.getUserDisplayName(assessedIdentity))
			});
		}
		
		if(withLog) {
			logButton = uifactory.addFormLink("log", formLayout, Link.BUTTON);
			logButton.setIconLeftCSS("o_icon o_icon_log");
		}
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("authorizedAbsenceEnabled", authorizedAbsenceEnabled);
		}
	
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		String select = withPrint ? "details" : null;
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LecturesCols.externalRef, select));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LecturesCols.entry, select));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LecturesCols.plannedLectures));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LecturesCols.attendedLectures));
		if(authorizedAbsenceEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LecturesCols.unauthorizedAbsentLectures));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LecturesCols.authorizedAbsentLectures));
		} else {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LecturesCols.absentLectures));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LecturesCols.progress, new LectureStatisticsCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LecturesCols.rateWarning, new RateWarningCellRenderer(getTranslator())));
		DefaultFlexiColumnModel rateColumn = new DefaultFlexiColumnModel(LecturesCols.rate, new PercentCellRenderer());
		rateColumn.setFooterCellRenderer(new PercentCellRenderer());
		columnsModel.addFlexiColumnModel(rateColumn);
		if(withSelect) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("details", translate("details"), "details"));
		}
		
		tableModel = new ParticipantLecturesDataModel(columnsModel, getTranslator(), getLocale()); 
		int paging = withPrint ? 20 : -1;
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, paging, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "participant-lectures-overview");
		tableEl.setCustomizeColumns(false);
		tableEl.setEmtpyTableMessageKey("empty.lectures.list");
		tableEl.setFooter(true);
	}
	
	private void loadModel() {
		List<LectureBlockStatistics> statistics = lectureService.getParticipantLecturesStatistics(assessedIdentity);
		if(filterByEntries != null && !filterByEntries.isEmpty()) {
			Set<Long> acceptedEntries = filterByEntries.stream()
					.map(RepositoryEntryRef::getKey).collect(Collectors.toSet());
			
			List<LectureBlockStatistics> filteredStatistics = statistics.stream()
					.filter(s -> acceptedEntries.contains(s.getRepoKey()))
					.collect(Collectors.toList());
			statistics = filteredStatistics;
		}
		AggregatedLectureBlocksStatistics total = lectureService.aggregatedStatistics(statistics);
		tableModel.setObjects(statistics, total);
		tableEl.reset(true, true, true);
	}
	
	public boolean hasRows() {
		return tableModel.getRowCount() > 0;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("RepositoryEntry".equalsIgnoreCase(type)) {
			Long repoEntryKey = entries.get(0).getOLATResourceable().getResourceableId();
			for(LectureBlockStatistics row: tableModel.getObjects()) {
				if(row.getRepoKey().equals(repoEntryKey)) {
					doSelect(ureq, row);
					break;
				}
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(flc.getFormItemComponent() == source && "print".equals(event.getCommand())) {
			doPrint(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				LectureBlockStatistics row = tableModel.getObject(se.getIndex());
				if("details".equals(cmd)) {
					doSelect(ureq, row);
				} else if("open.course".equals(cmd)) {
					doOpenCourse(ureq, row);
				}
			}
		} else if(logButton == source) {
			doExportLog(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSelect(UserRequest ureq, LectureBlockStatistics statistics) {
		removeAsListenerAndDispose(lectureBlocksCtrl);
		
		RepositoryEntry entry = repositoryService.loadByKey(statistics.getRepoKey());
		if(entry == null) {
			showWarning("warning.repositoryentry.deleted");
			loadModel();
		} else {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("RepositoryEntry", entry.getKey()), null);
			lectureBlocksCtrl = new ParticipantLectureBlocksController(ureq, swControl, entry, assessedIdentity);
			listenTo(lectureBlocksCtrl);
			stackPanel.pushController(entry.getDisplayname(), lectureBlocksCtrl);
		}
	}
	
	private void doPrint(UserRequest ureq) {
		ControllerCreator printControllerCreator = (lureq, lwControl) -> {
			lwControl.getWindowBackOffice().getChiefController().addBodyCssClass("o_lectures_print");
			Controller printCtrl = new ParticipantLecturesOverviewController(lureq, lwControl, assessedIdentity, filterByEntries, false, false, false, true);
			listenTo(printCtrl);
			return printCtrl;				
		};
		ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createPrintPopupLayout(printControllerCreator);
		openInNewBrowserWindow(ureq, layoutCtrlr);
	}
	
	private void doOpenCourse(UserRequest ureq, LectureBlockStatistics row) {
		String businessPath = "[RepositoryEntry:" + row.getRepoKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doExportLog(UserRequest ureq) {
		List<LectureBlockAuditLog> auditLog = lectureService.getAuditLog(assessedIdentity);
		IdentityAuditLogExport export = new IdentityAuditLogExport(assessedIdentity, auditLog, authorizedAbsenceEnabled, getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(export);
	}
}
