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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
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
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRepositoryEntryViews;
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
import org.olat.repository.RepositoryEntryMyView;
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
	private BreadcrumbPanel stackPanel;
	private AggregatedTable genericTable;
	
	private int count = 0;
	private final boolean openAll;
	private final boolean withLog;
	private final boolean withPrint;
	private final boolean withTitle;
	private final boolean withSelect;
	private final boolean printCommand;
	private final Identity assessedIdentity;
	private final boolean absenceNoticeEnabled;
	private final boolean authorizedAbsenceEnabled;
	private final boolean withCurriculumAggregation;
	private final List<RepositoryEntryRef> filterByEntries;
	private final List<AggregatedElement> aggregatedElements;
	
	private ParticipantLectureBlocksController lectureBlocksCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	
	public ParticipantLecturesOverviewController(UserRequest ureq, WindowControl wControl, boolean withTitle, boolean withCurriculumAggregation) {
		this(ureq, wControl, ureq.getIdentity(), null, true, true, false, withTitle, withCurriculumAggregation, false, false);
	}
	
	public ParticipantLecturesOverviewController(UserRequest ureq, WindowControl wControl,
			Identity assessedIdentity, List<RepositoryEntryRef> filterByEntries,
			boolean withPrint, boolean withSelect, boolean withLog, boolean withTitle,
			boolean withCurriculumAggregation, boolean openAll, boolean printCommand) {
		super(ureq, wControl, "participant_overview");
		this.openAll = openAll;
		this.withLog = withLog;
		this.withPrint = withPrint;
		this.withTitle = withTitle;
		this.withSelect = withSelect;
		this.printCommand = printCommand;
		this.assessedIdentity = assessedIdentity;
		this.filterByEntries = filterByEntries;
		this.withCurriculumAggregation = withCurriculumAggregation;
		absenceNoticeEnabled = lectureModule.isAbsenceNoticeEnabled();
		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		if(curriculumModule.isEnabled() && withCurriculumAggregation) {
			aggregatedElements = loadAggregation();
		} else {
			aggregatedElements = Collections.emptyList();
		}
		initForm(ureq);
		loadModel();
	}
	
	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = stackPanel;
	}
	
	public boolean hasRows() {
		return getRowCount() > 0;
	}
	
	public int getRowCount() {
		int rows = genericTable.getTableModel().getRowCount();
		for(AggregatedElement element:aggregatedElements) {
			rows += element.getTable().getTableModel().getRowCount();
		}
		return rows;
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
		
		if( formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.getFormItemComponent().contextPut("printCommand", Boolean.valueOf(printCommand));
		}
		
		if(withLog) {
			logButton = uifactory.addFormLink("log", formLayout, Link.BUTTON);
			logButton.setIconLeftCSS("o_icon o_icon_log");
		}

		List<String> containerIds = new ArrayList<>();
		String page = velocity_root + "/participant_overview_table.html";
		if(!aggregatedElements.isEmpty()) {
			Collections.sort(aggregatedElements, new AggregatedTableComparator(getLocale()));
			for(AggregatedElement aggregatedElement:aggregatedElements) {
				String containerId = "cont_" + (++count);
				containerIds.add(containerId);
				FormLayoutContainer layoutCont = FormLayoutContainer.createCustomFormLayout(containerId, getTranslator(), page);
				formLayout.add(layoutCont);
				layoutCont.setRootForm(mainForm);
				layoutCont.contextPut("elementDisplayName", aggregatedElement.getCurriculumElementView().getCurriculumElement().getDisplayName());
				layoutCont.contextPut("elementIdentifier", aggregatedElement.getCurriculumElementView().getCurriculumElement().getIdentifier());
				layoutCont.contextPut("opened", openAll || aggregatedElement.isNow());
				layoutCont.contextPut("titleSize", "4");
				aggregatedElement.setTable(initTable(ureq,  layoutCont));
			}
		}
		
		String containerId = "cont_" + (++count);
		containerIds.add(containerId);
		FormLayoutContainer genericLayoutCont = FormLayoutContainer.createCustomFormLayout(containerId, getTranslator(), page);
		formLayout.add(genericLayoutCont);
		genericLayoutCont.setRootForm(mainForm);
		if(!aggregatedElements.isEmpty()) {
			genericLayoutCont.contextPut("elementDisplayName", translate("lectures.without.curriculum"));
		}
		genericLayoutCont.contextPut("opened", Boolean.TRUE);
		genericLayoutCont.contextPut("titleSize", "3");
		genericTable = initTable(ureq, genericLayoutCont);
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("authorizedAbsenceEnabled", authorizedAbsenceEnabled);
			layoutCont.contextPut("absenceNoticeEnabled", absenceNoticeEnabled);
			layoutCont.contextPut("aggregatedElements", containerIds);
			if(!aggregatedElements.isEmpty()) {
				layoutCont.contextPut("aggregatedElementsTitle", translate("lectures.with.curriculum"));
			}
		}
	}
	
	private AggregatedTable initTable(UserRequest ureq, FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		String select = withPrint ? "details" : null;
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LecturesCols.externalRef, select));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LecturesCols.entry, select));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LecturesCols.plannedLectures));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LecturesCols.attendedLectures));
		if(authorizedAbsenceEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LecturesCols.unauthorizedAbsentLectures));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LecturesCols.authorizedAbsentLectures));
			if(absenceNoticeEnabled) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LecturesCols.dispensedLectures));
			}
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
		
		ParticipantLecturesDataModel model = new ParticipantLecturesDataModel(columnsModel, getTranslator(), getLocale()); 
		int paging = withPrint ? 20 : -1;
		FlexiTableElement table = uifactory.addTableElement(getWindowControl(), "table", model, paging, false, getTranslator(), formLayout);
		table.setAndLoadPersistedPreferences(ureq, "participant-lectures-overview-v2");
		table.setCustomizeColumns(false);
		table.setEmptyTableMessageKey("empty.lectures.list");
		table.setFooter(true);
		return new AggregatedTable(table, model);
	}
	
	private List<AggregatedElement> loadAggregation() {
		Roles roles = securityManager.getRoles(assessedIdentity);
		List<Curriculum> curriculums = curriculumService.getMyCurriculums(assessedIdentity);
		List<CurriculumElementRepositoryEntryViews> elementsWithViews = curriculumService
				.getCurriculumElements(assessedIdentity, roles, new ArrayList<>(curriculums));

		List<CurriculumElementRepositoryEntryViews> elementsToAggregate = new ArrayList<>();
		for(CurriculumElementRepositoryEntryViews view:elementsWithViews) {
			boolean aggregate = CurriculumLectures.isEnabled(view.getCurriculumElement(), view.getCurriculumElementType());
			if(aggregate) {
				elementsToAggregate.add(view);
			}
		}

		List<AggregatedElement> finalElements;
		if(elementsToAggregate.isEmpty()) {
			finalElements = Collections.emptyList();
		} else {
			finalElements = descendants(elementsToAggregate, elementsWithViews);
		}
		return finalElements;
	}
	
	private List<AggregatedElement> descendants(List<CurriculumElementRepositoryEntryViews> references,
			List<CurriculumElementRepositoryEntryViews> elementsWithViews) {
		Map<CurriculumElementRepositoryEntryViews, AggregatedElement> results = new HashMap<>();
		for(CurriculumElementRepositoryEntryViews reference:references) {
			results.put(reference, new AggregatedElement(reference));
		}

		for(CurriculumElementRepositoryEntryViews view:elementsWithViews) {
			for(CurriculumElementRepositoryEntryViews parent=view; parent != null; parent=parent.getParent()) {
				if(references.contains(parent)) {
					results.get(parent).addDescendant(view);
				}
			}
		}
		
		return new ArrayList<>(results.values());
	}
	
	public void loadModel() {
		List<LectureBlockStatistics> statistics = lectureService.getParticipantLecturesStatistics(assessedIdentity, getIdentity());
		if(filterByEntries != null && !filterByEntries.isEmpty()) {
			Set<Long> acceptedEntries = filterByEntries.stream()
					.map(RepositoryEntryRef::getKey).collect(Collectors.toSet());
			
			List<LectureBlockStatistics> filteredStatistics = statistics.stream()
					.filter(s -> acceptedEntries.contains(s.getRepoKey()))
					.collect(Collectors.toList());
			statistics = filteredStatistics;
		}

		Set<Long> excludedKeys = new HashSet<>();
		if(!aggregatedElements.isEmpty()) {
			for(AggregatedElement aggregatedElement:aggregatedElements) {
				Set<Long> includedKeys = aggregatedElement.getRepositoryEntryKeys();
				excludedKeys.addAll(includedKeys);
				List<LectureBlockStatistics> subStatistics = statistics.stream()
						.filter(s -> includedKeys.contains(s.getRepoKey()))
						.collect(Collectors.toList());
				AggregatedLectureBlocksStatistics subTotal = lectureService.aggregatedStatistics(subStatistics);
				aggregatedElement.getTable().setStatistics(subStatistics, subTotal);
			}
		}
		
		if(!excludedKeys.isEmpty()) {
			List<LectureBlockStatistics> filteredStatistics = statistics.stream()
					.filter(s -> !excludedKeys.contains(s.getRepoKey()))
					.collect(Collectors.toList());
			statistics = filteredStatistics;
		}

		AggregatedLectureBlocksStatistics total = lectureService.aggregatedStatistics(statistics);
		genericTable.setStatistics(statistics, total);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("RepositoryEntry".equalsIgnoreCase(type)) {
			Long repoEntryKey = entries.get(0).getOLATResourceable().getResourceableId();
			if(!activate(ureq, repoEntryKey, genericTable)) {
				for(AggregatedElement aggregatedElement:aggregatedElements) {
					if(activate(ureq, repoEntryKey, aggregatedElement.getTable())) {
						break;
					}
				}
			}
		}
	}
	
	private boolean activate(UserRequest ureq, Long repoEntryKey, AggregatedTable table) {
		for(LectureBlockStatistics row: table.getTableModel().getObjects()) {
			if(row.getRepoKey().equals(repoEntryKey)) {
				doSelect(ureq, row);
				return true;
			}
		}
		return false;
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
		if(source == genericTable.getTableEl()) {
			processTableEvent(ureq, event, genericTable);
		} else if(source instanceof FlexiTableElement) {
			for(AggregatedElement aggregatedElement:aggregatedElements) {
				if(source == aggregatedElement.getTable().getTableEl()) {
					processTableEvent(ureq, event, aggregatedElement.getTable());
				}
			}
		} else if(logButton == source) {
			doExportLog(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void processTableEvent(UserRequest ureq, FormEvent event, AggregatedTable table) {
		if(event instanceof SelectionEvent) {
			SelectionEvent se = (SelectionEvent)event;
			String cmd = se.getCommand();
			LectureBlockStatistics row = table.getTableModel().getObject(se.getIndex());
			if("details".equals(cmd)) {
				doSelect(ureq, row);
			} else if("open.course".equals(cmd)) {
				doOpenCourse(ureq, row);
			}
		}
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
			Controller printCtrl = new ParticipantLecturesOverviewController(lureq, lwControl, assessedIdentity, filterByEntries,
					false, false, false, true, withCurriculumAggregation, true, true);
			listenTo(printCtrl);
			return printCtrl;				
		};
		ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createPrintPopupLayout(printControllerCreator);
		openInNewBrowserWindow(ureq, layoutCtrlr, true);
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
	
	protected static class AggregatedElement {
		
		private AggregatedTable table;
		private final Set<Long> repositoryEntryKeys = new HashSet<>();
		private final CurriculumElementRepositoryEntryViews curriculumElementView;
		private final List<CurriculumElementRepositoryEntryViews> descendantsAndItself = new ArrayList<>();
		
		public AggregatedElement(CurriculumElementRepositoryEntryViews curriculumElementView) {
			this.curriculumElementView = curriculumElementView;
		}
		
		public CurriculumElementRepositoryEntryViews getCurriculumElementView() {
			return curriculumElementView;
		}
		
		public void addDescendant(CurriculumElementRepositoryEntryViews view) {
			descendantsAndItself.add(view);
			for(RepositoryEntryMyView entry:view.getEntries()) {
				repositoryEntryKeys.add(entry.getKey());
			}
		}
		
		public Set<Long> getRepositoryEntryKeys() {
			return repositoryEntryKeys;
		}

		public AggregatedTable getTable() {
			return table;
		}

		public void setTable(AggregatedTable table) {
			this.table = table;
		}
		
		public boolean isNow() {
			Date begin = curriculumElementView.getCurriculumElement().getBeginDate();
			Date end = curriculumElementView.getCurriculumElement().getEndDate();
			
			Date now = new Date();
			if(begin != null && end != null) {
				return now.after(begin) && now.before(end);
			} else if(begin != null) {
				return now.after(begin);
			} else if(end != null) {
				return now.before(end);
			}
			return false;
		}
	}
	
	protected static class AggregatedTable {
		
		private final FlexiTableElement tableEl;
		private final ParticipantLecturesDataModel tableModel;
		
		public AggregatedTable(FlexiTableElement tableEl, ParticipantLecturesDataModel tableModel) {
			this.tableEl = tableEl;
			this.tableModel = tableModel;
		}

		public FlexiTableElement getTableEl() {
			return tableEl;
		}

		public ParticipantLecturesDataModel getTableModel() {
			return tableModel;
		}
		
		public void setStatistics(List<LectureBlockStatistics> statistics, AggregatedLectureBlocksStatistics total) {
			tableModel.setObjects(statistics, total);
			tableEl.reset(true, true, true);
			tableEl.setVisible(!statistics.isEmpty());
		}
	}
	
	protected static class AggregatedTableComparator implements Comparator<AggregatedElement> {
		
		private Collator collator;
		
		public AggregatedTableComparator(Locale locale) {
			collator = Collator.getInstance(locale);
		}

		@Override
		public int compare(AggregatedElement t1, AggregatedElement t2) {
			boolean now1 = t1.isNow();
			boolean now2 = t2.isNow();
			
			int c = 0;
			if(now1 && !now2) {
				c = -1;
			} else if(!now1 && now2) {
				c = 1;
			}
			
			if(c == 0) {
				CurriculumElement c1 = t1.getCurriculumElementView().getCurriculumElement();
				CurriculumElement c2 = t2.getCurriculumElementView().getCurriculumElement();
				c = compareDate(c1.getEndDate(), c2.getEndDate());
				if(c == 0) {
					c = compareDate(c1.getBeginDate(), c2.getBeginDate());
				}
				if(c == 0) {
					c = compareStrings(c1.getDisplayName(), c2.getDisplayName());
				}
				if(c == 0) {
					c = compareStrings(c1.getIdentifier(), c2.getIdentifier());
				}
				if(c == 0) {
					c = c1.getKey().compareTo(c2.getKey());
				}
			}
			
			return c;
		}
		
		private int compareDate(Date d1, Date d2) {
			if(d1 == null || d2 == null) {
				return compareNulls(d1, d2);
			}
			return d1.compareTo(d2);
		}
		
		private int compareStrings(String s1, String s2) {
			if(s1 == null || s2 == null) {
				return compareNulls(s1, s2);
			}
			return collator.compare(s1, s2);
		}
		
		private int compareNulls(Object d1, Object d2) {
			if(d1 == null && d2 == null) {
				return 0;
			} else if(d1 == null) {
				return 1;
			} else if(d2 == null) {
				return -1;
			}
			return 0;
		}
	}
}
