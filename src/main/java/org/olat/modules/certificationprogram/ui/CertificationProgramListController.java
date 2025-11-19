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
package org.olat.modules.certificationprogram.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.CertificationProgramStatusEnum;
import org.olat.modules.certificationprogram.model.CertificationProgramWithStatistics;
import org.olat.modules.certificationprogram.ui.CertificationProgramListTableModel.ProgramCols;
import org.olat.modules.certificationprogram.ui.component.CertificationProgramStatusCellRenderer;
import org.olat.modules.certificationprogram.ui.component.DurationCellRenderer;
import org.olat.modules.certificationprogram.ui.component.RecertificationModeCellRenderer;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramListController extends FormBasicController implements Activateable2 {
	
	private static final String ALL_TAB_ID = "All";
	private static final String ACTIVE_TAB_ID = "Active";
	private static final String INACTIVE_TAB_ID = "Inactive";
	
	public static final String CONTEXT_DETAILS = "Details";
	public static final String CONTEXT_OVERVIEW = "Overview";
	public static final String CONTEXT_MEMBERS = "Members";
	public static final String CONTEXT_MESSAGES = "Messages";
	public static final String CONTEXT_OWNERS = "Owners";
	public static final String CONTEXT_ELEMENTS = "Elements";
	public static final String CONTEXT_SETTINGS = "Settings";
	
	private static final String CMD_SELECT = "select";
	
	protected static final String FILTER_PROGRAM_STATUS = "ProgramStatus";
	protected static final String FILTER_CREDITPOINT_SYSTEM = "CreditPointSystem";

	private FlexiFiltersTab allTab;
	private FlexiFiltersTab activeTab;
	private FlexiFiltersTab inactiveTab;
	
	private FlexiTableElement tableEl;
	private FormLink addCertificationProgramButton;
	private CertificationProgramListTableModel tableModel;
	private final TooledStackedPanel toolbarPanel;

	
	private final List<CreditPointSystem> creditPointSystems;
	private final CertificationProgramSecurityCallback secCallback;

	private CloseableModalController cmc;
	private AddCertificationProgramController addCertificationProgramCtrl;
	private CertificationProgramDetailsController certificationProgramDetailsCtrl;
	
	@Autowired
	private CreditPointService creditPointService;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	public CertificationProgramListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			CertificationProgramSecurityCallback secCallback) {
		super(ureq, wControl, "program_list");
		this.secCallback = secCallback;
		this.toolbarPanel = toolbarPanel;
		
		creditPointSystems = creditPointService.getCreditPointSystems();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initButtonForm(formLayout);
		initTableForm(formLayout, ureq);
	}
	
	private void initButtonForm(FormItemContainer formLayout) {
		addCertificationProgramButton = uifactory.addFormLink("add.certification.program", formLayout, Link.BUTTON);
		addCertificationProgramButton.setIconLeftCSS("o_icon o_icon_add");
	}
	
	private void initTableForm(FormItemContainer formLayout, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ProgramCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProgramCols.displayName, CMD_SELECT));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ProgramCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProgramCols.validityPeriod,
				new DurationCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProgramCols.recertificationMode,
				new RecertificationModeCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProgramCols.requiredCreditPoint));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProgramCols.usersCertified));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProgramCols.removed));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ProgramCols.programStatus,
				new CertificationProgramStatusCellRenderer(getTranslator())));
		
		tableModel = new CertificationProgramListTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, true, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		
		initFilters();
		initFiltersPresets(ureq);
		tableEl.setAndLoadPersistedPreferences(ureq, "certification-programs-list-v1");
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry("", translate("filter.program.status.all")));
		statusValues.add(SelectionValues.entry(CertificationProgramStatusEnum.active.name(), translate("filter.program.status.active")));
		statusValues.add(SelectionValues.entry(CertificationProgramStatusEnum.inactive.name(), translate("filter.program.status.inactive")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.program.status"),
				FILTER_PROGRAM_STATUS, statusValues, true));

		SelectionValues creditPointSystemsValues = new SelectionValues();
		for(CreditPointSystem system:creditPointSystems) {
			String val = translate("filter.creditpoint.system.label",
					StringHelper.escapeHtml(system.getLabel()), StringHelper.escapeHtml(system.getName()));
			creditPointSystemsValues.add(SelectionValues.entry(system.getKey().toString(), val));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.creditpoint.system"),
				FILTER_CREDITPOINT_SYSTEM, creditPointSystemsValues, true));
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	private void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		tabs.add(allTab);
		
		activeTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ACTIVE_TAB_ID, translate("filter.program.status.active"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_PROGRAM_STATUS, CertificationProgramStatusEnum.active.name())));
		tabs.add(activeTab);
		
		inactiveTab = FlexiFiltersTabFactory.tabWithImplicitFilters(INACTIVE_TAB_ID, translate("filter.program.status.inactive"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_PROGRAM_STATUS, CertificationProgramStatusEnum.inactive.name())));
		tabs.add(inactiveTab);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, activeTab);
	}
	
	private void loadModel(UserRequest ureq) {
		Date now = DateUtils.getEndOfDay(ureq.getRequestTimestamp());
		List<CertificationProgramWithStatistics> programs = certificationProgramService.getCertificationProgramsWithStatistics(getIdentity(), now);
		List<CertificationProgramRow> rows = new ArrayList<>();
		for(CertificationProgramWithStatistics program:programs) {
			CertificationProgram certificationProgram = program.certificationProgram();
			String creditPoints = CertificationHelper.creditPointsToString(certificationProgram);
			long certified = program.validCertificates() + program.expiredCertificates();
			long removed = program.notRenewableCertificates() + program.revokedCertificates();
			CertificationProgramRow row = new CertificationProgramRow(program.certificationProgram(), certified, removed, creditPoints);
			rows.add(row);
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void filterModel() {
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			tableEl.setSelectedFilterTab(ureq, activeTab);
			loadModel(ureq);
			filterModel();
		} else {
			String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if(ACTIVE_TAB_ID.equals(resName)) {
				tableEl.setSelectedFilterTab(ureq, activeTab);
				loadModel(ureq);
				filterModel();
			} else if(INACTIVE_TAB_ID.equals(resName)) {
				tableEl.setSelectedFilterTab(ureq, inactiveTab);
				loadModel(ureq);
				filterModel();
			} else if("CertificationProgram".equalsIgnoreCase(resName)) {
				tableEl.setSelectedFilterTab(ureq, allTab);
				loadModel(ureq);
				CertificationProgramRow row = tableModel.getRow(entries.get(0).getOLATResourceable().getResourceableId());
				if(row != null) {
					List<ContextEntry> subEntries = entries.subList(1, entries.size());
					doOpenCertificationProgram(ureq, row).activate(ureq, subEntries, entries.get(0).getTransientState());
				}
			} else {
				tableEl.setSelectedFilterTab(ureq, allTab);
				loadModel(ureq);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(certificationProgramDetailsCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				loadModel(ureq);
				filterModel();
			}
		} else if(addCertificationProgramCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel(ureq);
				doOpenCertificationProgramSettings(ureq, addCertificationProgramCtrl.getCertificationProgram());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(addCertificationProgramCtrl);
		removeAsListenerAndDispose(cmc);
		addCertificationProgramCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addCertificationProgramButton == source) {
			doAddCertificationProgram(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				if(CMD_SELECT.equals(se.getCommand())) {
					CertificationProgramRow row = tableModel.getObject(se.getIndex());
					List<ContextEntry> entries = BusinessControlFactory.getInstance()
							.createCEListFromResourceType(CertificationProgramListController.CONTEXT_ELEMENTS);
					doOpenCertificationProgram(ureq, row).activate(ureq, entries, null);
				}
			} else if(event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				filterModel();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doAddCertificationProgram(UserRequest ureq) {
		addCertificationProgramCtrl = new AddCertificationProgramController(ureq, getWindowControl());
		listenTo(addCertificationProgramCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), addCertificationProgramCtrl.getInitialComponent(),
				true, translate("add.certification.program"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doOpenCertificationProgramSettings(UserRequest ureq, CertificationProgram program) {
		if(program == null) {
			return;
		}
		
		CertificationProgramRow row = this.tableModel.getRow(program);
		if(row == null) {
			return;
		}
		
		List<ContextEntry> entries = BusinessControlFactory.getInstance()
				.createCEListFromResourceType(CertificationProgramListController.CONTEXT_SETTINGS);
		doOpenCertificationProgram(ureq, row).activate(ureq, entries, null);
	}
	
	private CertificationProgramDetailsController doOpenCertificationProgram(UserRequest ureq, CertificationProgramRow row) {
		CertificationProgram program = certificationProgramService.getCertificationProgram(row);
		WindowControl swControl	= addToHistory(ureq, OresHelper.createOLATResourceableInstance(CertificationProgram.class, program.getKey()), null);
		certificationProgramDetailsCtrl = new CertificationProgramDetailsController(ureq, swControl, toolbarPanel, program, secCallback);
		listenTo(certificationProgramDetailsCtrl);
		
		toolbarPanel.pushController(program.getDisplayName(), certificationProgramDetailsCtrl);
		return certificationProgramDetailsCtrl;
	}
}
