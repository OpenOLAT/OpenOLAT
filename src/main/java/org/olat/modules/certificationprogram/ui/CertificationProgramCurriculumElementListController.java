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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateWithDayFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings.CalloutOrientation;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.model.CertificationCurriculumElementWithInfos;
import org.olat.modules.certificationprogram.ui.CurriculumElementTableModel.CurriculumElementCols;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.curriculum.ui.ReferencesController;
import org.olat.modules.curriculum.ui.component.CurriculumStatusCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramCurriculumElementListController extends FormBasicController
implements Activateable2, FlexiTableComponentDelegate {

	private static final String ALL_TAB_ID = "All";
	private static final String RELEVANT_TAB_ID = "Relevant";
	private static final String FINISHED_TAB_ID = "Finished";
	private static final String CANCELLED_TAB_ID = "Cancelled";
	
	private static final String TOGGLE_DETAILS_CMD = "toggle-details";
	private static final String CMD_RESOURCES = "resources";

	protected static final String FILTER_ELEMENT_STATUS = "ElementStatus";
	protected static final String FILTER_WITH_CONTENT = "WithContent";

	private FlexiFiltersTab allTab;
	private FlexiFiltersTab relevantTab;
	private FlexiFiltersTab finishedTab;
	private FlexiFiltersTab cancelledTab;

	private final VelocityContainer detailsVC;
	private FormLink addCurriculumElementButton;
	private FlexiTableElement tableEl;
	private CurriculumElementTableModel tableModel;
	
	private int counter = 0;
	private CertificationProgram certificationProgram;
	private final CertificationProgramSecurityCallback secCallback;

	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private ReferencesController referencesCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private CurriculumElementListController curriculumElementListController;
	
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	public CertificationProgramCurriculumElementListController(UserRequest ureq, WindowControl wControl,
			CertificationProgram certificationProgram, CertificationProgramSecurityCallback secCallback) {
		super(ureq, wControl, "program_implementations_list",
				Util.createPackageTranslator(CurriculumComposerController.class, ureq.getLocale()));
		this.secCallback = secCallback;
		this.certificationProgram = certificationProgram;
		detailsVC = createVelocityContainer("program_implementation_details");
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initButtonsForm(formLayout);
		initTableForm(formLayout, ureq);
	}
	
	private void initButtonsForm(FormItemContainer formLayout) {
		if(secCallback.canEditCertificationProgram()) {
			addCurriculumElementButton = uifactory.addFormLink("add.curriculum.element", formLayout, Link.BUTTON);
			addCurriculumElementButton.setIconLeftCSS("o_icon o_icon_add");
		}
	}
	
	private void initTableForm(FormItemContainer formLayout, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CurriculumElementCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumElementCols.displayName, TOGGLE_DETAILS_CMD));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumElementCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CurriculumElementCols.externalId));
		
		DateWithDayFlexiCellRenderer dateRenderer = new DateWithDayFlexiCellRenderer(getLocale());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumElementCols.beginDate,
				dateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumElementCols.endDate,
				dateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumElementCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumElementCols.resources));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumElementCols.numOfParticipants));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumElementCols.numOfPassedParticipants));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumElementCols.status,
				new CurriculumStatusCellRenderer(getTranslator())));
		
        ActionsColumnModel actionsCol = new ActionsColumnModel(CurriculumElementCols.tools);
        actionsCol.setCellRenderer(new ActionsCellRenderer(getTranslator()));
		columnsModel.addFlexiColumnModel(actionsCol);
		
		tableModel = new CurriculumElementTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, true, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_certification_curriculum_elements");
		tableEl.setSearchEnabled(true);

		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);

		initFilters();
		initFiltersPresets(ureq);
		tableEl.setAndLoadPersistedPreferences(ureq, "certification-programs-select-element-v1.1");
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.preparation.name(), translate("filter.preparation")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.provisional.name(), translate("filter.provisional")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.confirmed.name(), translate("filter.confirmed")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.active.name(), translate("filter.active")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.cancelled.name(), translate("filter.cancelled")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.finished.name(), translate("filter.finished")));
		FlexiTableMultiSelectionFilter statusFilter = new FlexiTableMultiSelectionFilter(translate("filter.status"),
				FILTER_ELEMENT_STATUS, statusValues, true);
		filters.add(statusFilter);
		
		SelectionValues includeValues = new SelectionValues();
		includeValues.add(SelectionValues.entry(FILTER_WITH_CONTENT, translate("filter.with.content")));
		FlexiTableOneClickSelectionFilter withContentFilter = new FlexiTableOneClickSelectionFilter(translate("filter.with.content"),
				FILTER_WITH_CONTENT, includeValues, true);
		filters.add(withContentFilter);

		tableEl.setFilters(true, filters, false, false);
	}
	
	private void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		
		relevantTab = FlexiFiltersTabFactory.tabWithImplicitFilters(RELEVANT_TAB_ID, translate("filter.relevant"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ELEMENT_STATUS,
						List.of(CurriculumElementStatus.preparation.name(), CurriculumElementStatus.provisional.name(),
								CurriculumElementStatus.confirmed.name(), CurriculumElementStatus.active.name() ))));
		relevantTab.setFiltersExpanded(false);
		tabs.add(relevantTab);
		
		cancelledTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CANCELLED_TAB_ID, translate("filter.cancelled"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ELEMENT_STATUS,
						List.of(CurriculumElementStatus.cancelled.name()))));
		cancelledTab.setFiltersExpanded(false);
		tabs.add(cancelledTab);
		
		finishedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(FINISHED_TAB_ID, translate("filter.finished"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_ELEMENT_STATUS,
						List.of(CurriculumElementStatus.finished.name()))));
		finishedTab.setFiltersExpanded(false);
		tabs.add(finishedTab);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, relevantTab);
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>(1);
		if(rowObject instanceof CurriculumElementRow elementRow
				&& elementRow.getDetailsController() != null) {
			components.add(elementRow.getDetailsController().getInitialFormItem().getComponent());
		}
		return components;
	}

	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}
	
	private void loadModel() {
		List<CertificationCurriculumElementWithInfos> elementsInfosList = certificationProgramService
				.getCurriculumElementsFor(certificationProgram);
		List<CurriculumElementRow> rows = new ArrayList<>(elementsInfosList.size());
		for(CertificationCurriculumElementWithInfos elementsInfos:elementsInfosList) {
			CurriculumElementRow row = forgeRow(elementsInfos);
			rows.add(row);
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private CurriculumElementRow forgeRow(CertificationCurriculumElementWithInfos elementsInfos) {
		CurriculumElementRow row = new CurriculumElementRow(elementsInfos.curriculumElement(), elementsInfos.curriculum(),
				elementsInfos.numOfParticipants(), elementsInfos.numOfPassedParticipants(), elementsInfos.numOfResources());
		long refs = elementsInfos.numOfResources();
		if(refs > 0) {
			FormLink resourcesLink = uifactory.addFormLink("resources_" + (++counter), CMD_RESOURCES, String.valueOf(refs), null, null, Link.NONTRANSLATED);
			resourcesLink.setUserObject(row);
			row.setResources(resourcesLink);
		}
		return row;
	}
	
	private void filterModel() {
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(ALL_TAB_ID.equalsIgnoreCase(resName)) {
			tableEl.setSelectedFilterTab(ureq, allTab);
			filterModel();
		} else if(RELEVANT_TAB_ID.equalsIgnoreCase(resName)) {
			tableEl.setSelectedFilterTab(ureq, relevantTab);
			filterModel();
		} else if(FINISHED_TAB_ID.equalsIgnoreCase(resName)) {
			tableEl.setSelectedFilterTab(ureq, finishedTab);
			filterModel();
		} else if(CANCELLED_TAB_ID.equalsIgnoreCase(resName)) {
			tableEl.setSelectedFilterTab(ureq, cancelledTab);
			filterModel();
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(curriculumElementListController == source) {
			if(event == FormEvent.DONE_EVENT) {
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
		removeAsListenerAndDispose(curriculumElementListController);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		curriculumElementListController = null;
		calloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == addCurriculumElementButton) {
			doAddCurriculumElement(ureq);
		} else if(tableEl == source) {
			 if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				if(TOGGLE_DETAILS_CMD.equals(cmd)) {
					CurriculumElementRow row = tableModel.getObject(se.getIndex());
					if(row.getDetailsController() != null) {
						doCloseCurriculumElementDetails(row);
						tableEl.collapseDetails(se.getIndex());
					} else {
						doOpenCurriculumElementDetails(ureq, row);
						tableEl.expandDetails(se.getIndex());
					}
				} else if(ActionsCellRenderer.CMD_ACTIONS.equals(se.getCommand())) {
					String targetId = ActionsCellRenderer.getId(se.getIndex());
					CurriculumElementRow selectedRow = tableModel.getObject(se.getIndex());
					doOpenTools(ureq, selectedRow, targetId);
				}
			} else if(event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				filterModel();
			} else if(event instanceof DetailsToggleEvent toggleEvent) {
				CurriculumElementRow row = tableModel.getObject(toggleEvent.getRowIndex());
				if(toggleEvent.isVisible()) {
					doOpenCurriculumElementDetails(ureq, row);
				} else {
					doCloseCurriculumElementDetails(row);
				}
			}
		} else if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if(CMD_RESOURCES.equals(cmd) && link.getUserObject() instanceof CurriculumElementRow row) {
				doOpenReferences(ureq, row, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doAddCurriculumElement(UserRequest ureq) {
		curriculumElementListController = new CurriculumElementListController(ureq, getWindowControl(), certificationProgram);
		listenTo(curriculumElementListController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), curriculumElementListController.getInitialComponent(),
				true, translate("select.certification.program"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenCurriculumElementDetails(UserRequest ureq, CurriculumElementRow row) {
		if(row == null) return;
		
		if(row.getDetailsController() != null) {
			removeAsListenerAndDispose(row.getDetailsController());
			flc.remove(row.getDetailsController().getInitialFormItem());
		}
		
		CertificationProgramCurriculumElementDetailsController detailsCtrl = new CertificationProgramCurriculumElementDetailsController(ureq, getWindowControl(),
				mainForm, row);
		listenTo(detailsCtrl);
		row.setDetailsController(detailsCtrl);
		flc.add(detailsCtrl.getInitialFormItem());
	}
	
	private void doCloseCurriculumElementDetails(CurriculumElementRow row) {
		if(row.getDetailsController() == null) return;
		removeAsListenerAndDispose(row.getDetailsController());
		flc.remove(row.getDetailsController().getInitialFormItem());
		row.setDetailsController(null);
	}
	
	private void doOpenReferences(UserRequest ureq, CurriculumElementRow row, FormLink link) {
		removeAsListenerAndDispose(referencesCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		CurriculumElement element = curriculumService.getCurriculumElement(row);
		if(element == null ) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			referencesCtrl = new ReferencesController(ureq, getWindowControl(), getTranslator(), element);
			listenTo(referencesCtrl);
	
			CalloutSettings settings = new CalloutSettings(true, CalloutOrientation.bottom, true, null);
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					referencesCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "", settings);
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	
	private void doOpenTools(UserRequest ureq, CurriculumElementRow elementRow, String targetId) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), elementRow);
		listenTo(toolsCtrl);
	
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), targetId, "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CurriculumElementRow elementRow) {
			super(ureq, wControl);
			
			VelocityContainer mainVC = createVelocityContainer("tool_elements");
			
			String url = BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathString("[CurriculumAdmin:0][Curriculums:0][Curriculum:"
							+ elementRow.getCurriculumKey() + "][Implementations:0][CurriculumElement:" + elementRow.getKey() + "][Overview:0]");
			ExternalLink openCourseLink = LinkFactory.createExternalLink("open.course", translate("open.course"), url);
			openCourseLink.setIconLeftCSS("o_icon o_icon-fw o_icon_content_popup");
			openCourseLink.setName(translate("open.new.tab"));
			mainVC.put("open.element", openCourseLink);

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
}
