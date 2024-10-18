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
package org.olat.modules.curriculum.ui;

import static org.olat.modules.curriculum.ui.CurriculumListManagerController.CONTEXT_ELEMENT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
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
import org.olat.core.gui.control.generic.confirmation.BulkDeleteConfirmationController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.member.wizard.ImportMemberByUsernamesController;
import org.olat.course.member.wizard.ImportMember_1_MemberStep;
import org.olat.course.member.wizard.MembersByNameContext;
import org.olat.course.member.wizard.MembersContext;
import org.olat.group.ui.main.MemberPermissionChangeEvent;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumManagedFlag;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.model.CurriculumElementInfosSearchParams;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.modules.curriculum.site.CurriculumElementTreeRowComparator;
import org.olat.modules.curriculum.ui.CurriculumComposerTableModel.ElementCols;
import org.olat.modules.curriculum.ui.component.CurriculumStatusCellRenderer;
import org.olat.modules.curriculum.ui.copy.CopySettingsController;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.modules.curriculum.ui.event.SelectCurriculumElementEvent;
import org.olat.modules.curriculum.ui.event.SelectReferenceEvent;
import org.olat.modules.quality.QualityModule;
import org.olat.modules.quality.generator.ui.CurriculumElementPreviewListController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumComposerController extends FormBasicController implements Activateable2 {
	
	private static final String NEW_ELEMENT  = "new-element";
	
	private static final String ALL_TAB_ID = "All";
	private static final String RELEVANT_TAB_ID = "Relevant";

	protected static final String FILTER_TYPE = "Type";
	protected static final String FILTER_STATUS = "Status";
	protected static final String FILTER_CURRICULUM = "Curriculum";
	
	private static final String CMD_ADD_ELEMENT = "add-element";

	private Map<String,FlexiFiltersTab> statusTabMap;
	
	private FlexiTableElement tableEl;
	private CurriculumComposerTableModel tableModel;
	private TooledStackedPanel toolbarPanel;
	
	private FormLink overrideLink;
	private FormLink unOverrideLink;
	private FormLink bulkDeleteButton;
	private DropdownItem newElementMenu;
	private FormLink newGenericElementButton;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private CopySettingsController copyCtrl;
	private ReferencesController referencesCtrl;
	private StepsMainRunController importMembersWizard;
	private EditCurriculumElementController newElementCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private EditCurriculumElementController newSubElementCtrl;
	private MoveCurriculumElementController moveElementCtrl;
	private ConfirmCurriculumElementDeleteController confirmDeleteCtrl;
	private CurriculumElementCalendarController calendarsCtrl;
	private CurriculumElementPreviewListController qualityPreviewCtrl;
	private BulkDeleteConfirmationController bulkDeleteConfirmationCtrl;
	private CurriculumElementLearningPathController learningPathController;
	private CurriculumStructureCalloutController curriculumStructureCalloutCtrl;
	
	private int counter;
	private final boolean managed;
	private boolean overrideManaged;
	private final Curriculum curriculum;
	private final CurriculumElement rootElement;
	private final CurriculumComposerConfig config;
	private final CurriculumSecurityCallback secCallback;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private QualityModule qualityModule;
	
	/**
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param toolbarPanel The toolbar 
	 * @param curriculum The root curriculum (optional), if null all elements are shown
	 * 		and add element is disabled
	 * @param config The configuration of the controller
	 * @param secCallback Security callback
	 */
	public CurriculumComposerController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			Curriculum curriculum, CurriculumElement rootElement, CurriculumComposerConfig config, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, "manage_curriculum_structure");
		this.toolbarPanel = toolbarPanel;
		this.config = config;
		this.secCallback = secCallback;
		this.curriculum = curriculum;
		this.rootElement = rootElement;
		
		if(curriculum != null) {
			managed = CurriculumManagedFlag.isManaged(curriculum, CurriculumManagedFlag.members);
		} else {
			managed = false;
		}
		
		initForm(ureq);
		toolbarPanel.addListener(this);
	}
	
	public Curriculum getCurriculum() {
		return curriculum;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			if(StringHelper.containsNonWhitespace(config.getTitle())) {
				layoutCont.contextPut("title", config.getTitle());
				layoutCont.contextPut("titleSize", config.getTitleSize());
				layoutCont.contextPut("titleIconCssClass", config.getTitleIconCssClass());
			}
		}
		
		initButtons(formLayout, ureq);
		initFormTable(formLayout, ureq);
		initFilters();
		initFiltersPresets();
	}
	
	private void initButtons(FormItemContainer formLayout, UserRequest ureq) {
		if(secCallback.canManagerCurriculumElementsUsers()) {
			if(managed && isAllowedToOverrideManaged(ureq)) {
				overrideLink = uifactory.addFormLink("override.member", formLayout, Link.BUTTON);
				overrideLink.setIconLeftCSS("o_icon o_icon-fw o_icon_refresh");
				
				unOverrideLink = uifactory.addFormLink("unoverride.member", formLayout, Link.BUTTON);
				unOverrideLink.setIconLeftCSS("o_icon o_icon-fw o_icon_refresh");
				unOverrideLink.setVisible(false);
			}
		}
		
		if(secCallback.canNewCurriculumElement()) {
			newElementMenu = uifactory.addDropdownMenu("add.curriculum.element.menu", "add.curriculum.element.menu", null, formLayout, getTranslator());
			newElementMenu.setIconCSS("o_icon o_icon-fw o_icon_add");
			newElementMenu.setOrientation(DropdownOrientation.right);
			
			List<CurriculumElementType> allowedTypes;
			if(rootElement == null) {
				allowedTypes = curriculumService.getAllowedCurriculumElementType(rootElement, rootElement);
			} else {
				allowedTypes = curriculumService.getAllowedCurriculumElementType(rootElement.getParent(), rootElement);
			}
			
			for(CurriculumElementType allowedType:allowedTypes) {
				String link = translate("add.curriculum.element.typed", StringHelper.escapeHtml(allowedType.getDisplayName()));
				newGenericElementButton = uifactory.addFormLink("add.curriculum.element." + allowedType.getKey(),
						CMD_ADD_ELEMENT, link, null, formLayout, Link.LINK | Link.NONTRANSLATED);
				newGenericElementButton.setUserObject(allowedType);
				newElementMenu.addElement(newGenericElementButton);
			}
			
			newGenericElementButton = uifactory.addFormLink("add.curriculum.element", formLayout, Link.LINK);
			newGenericElementButton.setElementCssClass("o_sel_add_curriculum_element");
			newElementMenu.addElement(newGenericElementButton);
		}
	}
	
	private void initFormTable(FormItemContainer formLayout, UserRequest ureq) {	
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementCols.key));
		if(config.isFlat()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.displayName, "select"));
		} else {
			TreeNodeFlexiCellRenderer treeNodeRenderer = new TreeNodeFlexiCellRenderer("select");
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.displayName, "select", treeNodeRenderer));
		}
		
		DefaultFlexiColumnModel structureCol = new DefaultFlexiColumnModel(ElementCols.structure);
		structureCol.setIconHeader("o_icon o_icon-lg o_icon_curriculum_structure");
		structureCol.setExportable(false);
		columnsModel.addFlexiColumnModel(structureCol);
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementCols.externalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(curriculum == null, ElementCols.curriculum));
		DateFlexiCellRenderer dateRenderer = new DateFlexiCellRenderer(getLocale());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.beginDate, dateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.endDate, dateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.resources));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false,
				ElementCols.numOfMembers, "members"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(config.isDefaultNumOfParticipants(),
				ElementCols.numOfParticipants, "participants"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false,
				ElementCols.numOfCoaches, "coachs"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false,
				ElementCols.numOfOwners, "owners"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.status,
				new CurriculumStatusCellRenderer(getTranslator())));
		
		boolean withOptions = curriculum != null;
		DefaultFlexiColumnModel calendarsCol = new DefaultFlexiColumnModel(withOptions, ElementCols.calendars);
		calendarsCol.setIconHeader("o_icon o_icon-lg o_icon_calendar");
		columnsModel.addFlexiColumnModel(calendarsCol);
		
		DefaultFlexiColumnModel lecturesCol = new DefaultFlexiColumnModel(withOptions, ElementCols.lectures);
		lecturesCol.setIconHeader("o_icon o_icon-lg o_icon_lecture");
		columnsModel.addFlexiColumnModel(lecturesCol);
		if (qualityModule.isEnabled()) {
			DefaultFlexiColumnModel qualityCol = new DefaultFlexiColumnModel(withOptions, ElementCols.qualityPreview);
			qualityCol.setIconHeader("o_icon o_icon-lg o_icon_qual_preview");
			columnsModel.addFlexiColumnModel(qualityCol);
		}
		
		DefaultFlexiColumnModel progressCol = new DefaultFlexiColumnModel(withOptions, ElementCols.learningProgress);
		progressCol.setIconHeader("o_icon o_icon-lg o_CourseModule_icon");
		columnsModel.addFlexiColumnModel(progressCol);

		if(secCallback.canEditCurriculumElements() || (!managed && secCallback.canManagerCurriculumElementsUsers())) {
			StickyActionColumnModel toolsColumn = new StickyActionColumnModel(ElementCols.tools);
			toolsColumn.setIconHeader("o_icon o_icon-lg o_icon_actions");
			toolsColumn.setExportable(false);
			toolsColumn.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(toolsColumn);
		}
		
		tableModel = new CurriculumComposerTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setElementCssClass("o_curriculum_el_listing");		
		if(secCallback.canNewCurriculumElement()) {
			tableEl.setEmptyTableSettings("table.curriculum.element.empty", "table.curriculum.element.empty.hint", "o_icon_curriculum_element", "add.curriculum.element", "o_icon_add", true);
		} else {			
			tableEl.setEmptyTableSettings("table.curriculum.element.empty", null, "o_icon_curriculum_element");
		}
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setExportEnabled(true);
		tableEl.setPageSize(40);
		tableEl.setSearchEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "curriculum-composer");
		
		if(secCallback.canNewCurriculumElement()) {
			bulkDeleteButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
			tableEl.addBatchButton(bulkDeleteButton);
			tableEl.setMultiSelect(true);
		}
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		if(curriculum == null) {
			CurriculumSearchParameters searchParams = new CurriculumSearchParameters();
			List<Curriculum> curriculums = curriculumService.getCurriculums(searchParams);
			if(!curriculums.isEmpty()) {
				SelectionValues curriculumValues = new SelectionValues();
				for(Curriculum curriculum:curriculums) {
					curriculumValues.add(SelectionValues.entry(curriculum.getKey().toString(),
							StringHelper.escapeHtml(curriculum.getDisplayName())));
				}
				
				FlexiTableMultiSelectionFilter curriculumFilter = new FlexiTableMultiSelectionFilter(translate("filter.curriculum"),
						FILTER_CURRICULUM, curriculumValues, true);
				filters.add(curriculumFilter);
				
			}
		}
		
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.preparation.name(), translate("filter.preparation")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.provisional.name(), translate("filter.provisional")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.confirmed.name(), translate("filter.confirmed")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.active.name(), translate("filter.active")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.cancelled.name(), translate("filter.cancelled")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.finished.name(), translate("filter.finished")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.deleted.name(), translate("filter.deleted")));
		FlexiTableMultiSelectionFilter statusFilter = new FlexiTableMultiSelectionFilter(translate("filter.status"),
				FILTER_STATUS, statusValues, true);
		filters.add(statusFilter);
		
		List<CurriculumElementType> types = curriculumService.getCurriculumElementTypes();
		SelectionValues typesValues = new SelectionValues();
		for(CurriculumElementType type:types) {
			typesValues.add(SelectionValues.entry(type.getKey().toString(), type.getDisplayName()));
		}
		FlexiTableMultiSelectionFilter typeFilter = new FlexiTableMultiSelectionFilter(translate("filter.types"),
				FILTER_TYPE, typesValues, true);
		filters.add(typeFilter);
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	private void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		Map<String,FlexiFiltersTab> map = new HashMap<>();
		
		FlexiFiltersTab allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		map.put(ALL_TAB_ID.toLowerCase(), allTab);
		
		FlexiFiltersTab relevantTab = FlexiFiltersTabFactory.tabWithImplicitFilters(RELEVANT_TAB_ID, translate("filter.relevant"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS,
						List.of(CurriculumElementStatus.preparation.name(), CurriculumElementStatus.preparation.name(),
								CurriculumElementStatus.confirmed.name(), CurriculumElementStatus.active.name() ))));
		relevantTab.setFiltersExpanded(true);
		tabs.add(relevantTab);
		map.put(RELEVANT_TAB_ID.toLowerCase(), relevantTab);
		
		for(CurriculumElementStatus status:CurriculumElementStatus.visibleAdmin()) {
			FlexiFiltersTab statusTab = FlexiFiltersTabFactory.tabWithImplicitFilters(status.name(), translate("filter." + status.name()),
					TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS,
							List.of(status.name()))));
			statusTab.setFiltersExpanded(true);
			tabs.add(statusTab);
			map.put(status.name(), statusTab);
		}
		statusTabMap = Map.copyOf(map);
		
		tableEl.setFilterTabs(true, tabs);
	}
	
	protected boolean isAllowedToOverrideManaged(UserRequest ureq) {
		if(curriculum != null) {
			Roles roles = ureq.getUserSession().getRoles();
			return roles.isAdministrator() && curriculumService.hasRoleExpanded(curriculum, getIdentity(),
					OrganisationRoles.administrator.name());
		}
		return false;
	}

	@Override
	protected void doDispose() {
		toolbarPanel.removeListener(this);
		if(!toolbarPanel.isToolbarEnabled()) {
			toolbarPanel.setToolbarEnabled(true);
		}
        super.doDispose();
	}
	
	private void loadModel() {
		CurriculumElementInfosSearchParams searchParams = getSearchParams();
		
		List<CurriculumElementInfos> elements = curriculumService.getCurriculumElementsWithInfos(searchParams);
		List<CurriculumElementRow> rows = new ArrayList<>(elements.size());
		Map<Long, CurriculumElementRow> keyToRows = new HashMap<>();
		for(CurriculumElementInfos element:elements) {
			CurriculumElementRow row = forgeRow(element);
			rows.add(row);
			keyToRows.put(element.getKey(), row);
		}
		//parent line
		for(CurriculumElementRow row:rows) {
			if(row.getParentKey() != null) {
				row.setParent(keyToRows.get(row.getParentKey()));
			}
		}
		Collections.sort(rows, new CurriculumElementTreeRowComparator(getLocale()));
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private CurriculumElementInfosSearchParams getSearchParams() {
		CurriculumElementInfosSearchParams searchParams = new CurriculumElementInfosSearchParams();
		if(curriculum != null) {
			searchParams.setCurriculum(curriculum);
		}
		if(rootElement != null) {
			searchParams.setParentElement(rootElement);
		}
		if(config.isRootElementsOnly()) {
			searchParams.setRootElementsOnly(true);
		}
		return searchParams;
	}
	
	private void reloadElement(CurriculumElement element) {
		CurriculumElementRow row = tableModel.getCurriculumElementRowByKey(element.getKey());
		if(row != null) {
			row.setCurriculumElement(element);
			row.setCurriculumElementType(element.getType());
		}
	}
	
	private CurriculumElementRow forgeRow(CurriculumElementInfos element) {
		String id = element.getKey().toString();
		
		FormLink toolsLink = uifactory.addFormLink("tools_".concat(id), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-lg");
		toolsLink.setTitle(translate("action.more"));
		
		FormLink structureLink = uifactory.addFormLink("structure_".concat(id), "structure", "", null, null, Link.NONTRANSLATED);
		structureLink.setIconLeftCSS("o_icon o_icon-lg o_icon_curriculum_structure");
		structureLink.setTitle(translate("action.more"));
		
		FormLink resourcesLink = null;
		if(element.getNumOfResources() > 0) {
			resourcesLink = uifactory.addFormLink("resources_" + (++counter), "resources", String.valueOf(element.getNumOfResources()), null, null, Link.NONTRANSLATED);
		}
		CurriculumElementRow row = new CurriculumElementRow(element.getCurriculumElement(), element.getNumOfResources(),
				element.getNumOfParticipants(), element.getNumOfCoaches(), element.getNumOfOwners(),
				toolsLink, resourcesLink, structureLink);
		toolsLink.setUserObject(row);
		structureLink.setUserObject(row);
		if(resourcesLink != null) {
			resourcesLink.setUserObject(row);
		}

		String businessPath = CurriculumHelper.getBusinessPath(row);
		row.setBaseUrl(BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(businessPath));
		
		if(row.isCalendarsEnabled()) {
			FormLink calendarsLink = uifactory.addFormLink("cals_" + (++counter), "calendars", "", null, null, Link.LINK | Link.NONTRANSLATED);
			calendarsLink.setIconLeftCSS("o_icon o_icon-lg o_icon_timetable");
			calendarsLink.setTitle(translate("calendars"));
			row.setCalendarsLink(calendarsLink);
			calendarsLink.setUserObject(row);
		}
		if(row.isLecturesEnabled()) {
			FormLink lecturesLink = uifactory.addFormLink("lecs_" + (++counter), "lectures", "", null, null, Link.LINK | Link.NONTRANSLATED);
			lecturesLink.setIconLeftCSS("o_icon o_icon-lg o_icon_lecture");
			lecturesLink.setTitle(translate("lectures"));
			row.setLecturesLink(lecturesLink);
			lecturesLink.setUserObject(row);
		}
		if(qualityModule.isEnabled() && qualityModule.isPreviewEnabled()) {
			FormLink qualityPreviewLink = uifactory.addFormLink("qp_" + (++counter), "quality.preview", "", null, null, Link.LINK | Link.NONTRANSLATED);
			qualityPreviewLink.setIconLeftCSS("o_icon o_icon-lg o_icon_qual_preview");
			qualityPreviewLink.setTitle(translate("quality.preview"));
			row.setQualityPreviewLink(qualityPreviewLink);
			qualityPreviewLink.setUserObject(row);
		}
		if(row.isLearningProgressEnabled()) {
			FormLink learningProgressLink = uifactory.addFormLink("lp_" + (++counter), "learning.progress", "", null, null, Link.LINK | Link.NONTRANSLATED);
			learningProgressLink.setIconLeftCSS("o_icon o_icon-lg o_CourseModule_icon");
			learningProgressLink.setTitle(translate("learning.progress"));
			row.setLearningProgressLink(learningProgressLink);
			learningProgressLink.setUserObject(row);
		}
		
		return row;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("CurriculumElement".equalsIgnoreCase(type) || CONTEXT_ELEMENT.equalsIgnoreCase(type)) {
			activateElement(ureq, entries);
		} else if("Search".equalsIgnoreCase(type)) {
			Long elementKey = entries.get(0).getOLATResourceable().getResourceableId();
			tableEl.quickSearch(ureq, elementKey.toString());
		} else if("Zoom".equalsIgnoreCase(type)) {
			Long elementKey = entries.get(0).getOLATResourceable().getResourceableId();
			CurriculumElementRow row = tableModel.getCurriculumElementRowByKey(elementKey);
			if(row != null) {
				tableEl.focus(row);
			}
		} else if(statusTabMap != null && statusTabMap.containsKey(type.toLowerCase())) {
			FlexiFiltersTab statusTab = statusTabMap.get(type.toLowerCase());
			tableEl.setSelectedFilterTab(ureq, statusTab);
			loadModel();
		}
	}
	
	private void activateElement(UserRequest ureq, List<ContextEntry> entries) {
		Long elementKey = entries.get(0).getOLATResourceable().getResourceableId();
		CurriculumElementRow row = tableModel.getCurriculumElementRowByKey(elementKey);
		if(row != null) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenCurriculumElementDetails(ureq, row, subEntries);
		} else if(rootElement == null) {
			CurriculumElement element = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(elementKey));
			if(element != null && element.getCurriculum().equals(curriculum) && element.getParent() != null) {
				List<CurriculumElement> parentLine = curriculumService.getCurriculumElementParentLine(element);
				CurriculumElementRow implementationRow = tableModel.getCurriculumElementRowByKey(parentLine.get(0).getKey());
				if(implementationRow != null) {
					doOpenCurriculumElementDetails(ureq, implementationRow, entries);
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == importMembersWizard) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(importMembersWizard);
				importMembersWizard = null;
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					loadModel();
				}
			}
		} else if(newElementCtrl == source || newSubElementCtrl == source
				|| moveElementCtrl == source || confirmDeleteCtrl == source
				|| copyCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(bulkDeleteConfirmationCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doDeleteCurriculumElements((ToDelete)bulkDeleteConfirmationCtrl.getUserObject());
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (referencesCtrl == source || curriculumStructureCalloutCtrl == source) {
			toolsCalloutCtrl.deactivate();
			cleanUp();
			if(event instanceof SelectReferenceEvent sre) {
				launch(ureq, sre.getEntry());
			} else if(event instanceof SelectCurriculumElementEvent scee) {
				List<ContextEntry> subEntries = BusinessControlFactory.getInstance().createCEListFromString("[Structure:0]");
				doOpenCurriculumElementDetails(ureq, scee.getEntry(), subEntries);
			}
		} else if(source instanceof CurriculumElementDetailsController) {
			if(event == Event.CHANGED_EVENT) {
				loadModel();
			}
		} else if(cmc == source || toolsCalloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(moveElementCtrl);
		removeAsListenerAndDispose(newElementCtrl);
		removeAsListenerAndDispose(referencesCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(copyCtrl);
		removeAsListenerAndDispose(cmc);
		toolsCalloutCtrl = null;
		confirmDeleteCtrl = null;
		moveElementCtrl = null;
		newElementCtrl = null;
		referencesCtrl = null;
		copyCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(toolbarPanel == source) {
			if(event instanceof PopEvent pe
					&& pe.getController() instanceof CurriculumElementDetailsController elementCtrl) {
				reloadElement(elementCtrl.getCurriculumElement());
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == overrideLink) {
			doOverrideManagedResource();
		} else if (source == unOverrideLink) {
			doUnOverrideManagedResource();
		} else if(newGenericElementButton == source) {
			doNewCurriculumElement(ureq, null);
		} else if(this.bulkDeleteButton == source) {
			doConfirmBulkDelete(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				if("select".equals(cmd)) {
					CurriculumElementRow row = tableModel.getObject(se.getIndex());
					doOpenCurriculumElementOverview(ureq, row);
				} else if("members".equals(cmd)) {
					CurriculumElementRow row = tableModel.getObject(se.getIndex());
					doOpenCurriculumElementUserManagement(ureq, row, null);
				} else if("participants".equals(cmd)) {
					CurriculumElementRow row = tableModel.getObject(se.getIndex());
					doOpenCurriculumElementUserManagement(ureq, row, "Participants");
				} else if("coachs".equals(cmd)) {
					CurriculumElementRow row = tableModel.getObject(se.getIndex());
					doOpenCurriculumElementUserManagement(ureq, row, "Coachs");
				} else if("owners".equals(cmd)) {
					CurriculumElementRow row = tableModel.getObject(se.getIndex());
					doOpenCurriculumElementUserManagement(ureq, row, "Owners");
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
				tableEl.reset(false, true, true);// only reload
			} else if(event instanceof FlexiTableFilterTabEvent) {
				loadModel();
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doNewCurriculumElement(ureq, null);
			}
		} else if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if("tools".equals(cmd)) {
				doOpenTools(ureq, (CurriculumElementRow)link.getUserObject(), link);
			} else if(CMD_ADD_ELEMENT.equals(cmd) && link.getUserObject() instanceof CurriculumElementType type) {
				doNewCurriculumElement(ureq, type);
			} else if("resources".equals(cmd)) {
				doOpenReferences(ureq, (CurriculumElementRow)link.getUserObject(), link);
			}  else if("structure".equals(cmd)) {
				doOpenStructure(ureq, (CurriculumElementRow)link.getUserObject(), link);
			} else if("calendars".equals(cmd)) {
				doOpenCalendars(ureq, (CurriculumElementRow)link.getUserObject());
			} else if("lectures".equals(cmd) && link.getUserObject() instanceof CurriculumElementRow row) {
				doOpenCurriculumElementLectures(ureq, row);
			} else if("quality.preview".equals(cmd)) {
				doOpenQualityPreview(ureq, (CurriculumElementRow)link.getUserObject());
			} else if("learning.progress".equals(cmd)) {
				doOpenLearningProgress(ureq, (CurriculumElementRow)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOverrideManagedResource() {
		overrideManagedResource(true);
	}
	
	private void doUnOverrideManagedResource() {
		overrideManagedResource(false);
	}
	
	private void overrideManagedResource(boolean override) {
		overrideManaged = override;

		overrideLink.setVisible(!overrideManaged);
		unOverrideLink.setVisible(overrideManaged);
	}
	
	private void doNewCurriculumElement(UserRequest ureq, CurriculumElementType type) {
		if(guardModalController(newElementCtrl)) return;

		CurriculumElement parentElement = rootElement == null ? null : rootElement;
		newElementCtrl = new EditCurriculumElementController(ureq, getWindowControl(), parentElement,
				type, curriculum, secCallback);
		listenTo(newElementCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				newElementCtrl.getInitialComponent(), true, translate("add.curriculum.element"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doNewSubCurriculumElement(UserRequest ureq, CurriculumElementRow row, CurriculumElementType type) {
		CurriculumElement parentElement = curriculumService.getCurriculumElement(row);
		if(parentElement == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			Curriculum elementsCurriculum = parentElement.getCurriculum();
			newSubElementCtrl = new EditCurriculumElementController(ureq, getWindowControl(), parentElement,
					type, elementsCurriculum, secCallback);
			newSubElementCtrl.setParentElement(parentElement);
			listenTo(newSubElementCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					newSubElementCtrl.getInitialComponent(), true, translate("add.curriculum.element"));
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doCopyCurriculumElement(UserRequest ureq, CurriculumElementRow row) {
		CurriculumElement element = curriculumService.getCurriculumElement(row);
		if(element == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			copyCtrl = new CopySettingsController(ureq, getWindowControl(), element);
			listenTo(copyCtrl);
			
			String title = translate("copy.element.title", StringHelper.escapeHtml(element.getDisplayName()));
			cmc = new CloseableModalController(getWindowControl(), translate("close"), copyCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doMoveCurriculumElement(UserRequest ureq, CurriculumElementRow row) {
		CurriculumElement element = curriculumService.getCurriculumElement(row);
		if(element == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			Curriculum elementsCurriculum = element.getCurriculum();
			List<CurriculumElement> elementsToMove = List.of(element);
			moveElementCtrl = new MoveCurriculumElementController(ureq, getWindowControl(), elementsToMove, elementsCurriculum, secCallback);
			listenTo(moveElementCtrl);
			
			String title = translate("move.element.title", StringHelper.escapeHtml(row.getDisplayName()));
			cmc = new CloseableModalController(getWindowControl(), translate("close"), moveElementCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doImportMembers(UserRequest ureq, CurriculumElementRow focusedRow) {
		removeAsListenerAndDispose(importMembersWizard);
		
		CurriculumElement focusedElement = focusedRow == null ? null : focusedRow.getCurriculumElement();
		Curriculum focusedCurriculum = focusedElement == null ? curriculum : focusedElement.getCurriculum();
		MembersContext membersContext= MembersContext.valueOf(focusedCurriculum, focusedElement, overrideManaged, true);
		Step start = new ImportMember_1_MemberStep(ureq, membersContext);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			addMembers(uureq, runContext);
			MembersByNameContext membersByNameContext = (MembersByNameContext)runContext.get(ImportMemberByUsernamesController.RUN_CONTEXT_KEY);
			if(!membersByNameContext.getNotFoundNames().isEmpty()) {
				String notFoundNames = membersByNameContext.getNotFoundNames().stream()
						.collect(Collectors.joining(", "));
				showWarning("user.notfound", notFoundNames);
			}
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		importMembersWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("add.member"), "o_sel_group_import_logins_wizard");
		listenTo(importMembersWizard);
		getWindowControl().pushAsModalDialog(importMembersWizard.getInitialComponent());
	}
	
	private void addMembers(UserRequest ureq, StepsRunContext runContext) {
		Roles roles = ureq.getUserSession().getRoles();

		Set<Identity> members = ((MembersByNameContext)runContext.get(ImportMemberByUsernamesController.RUN_CONTEXT_KEY)).getIdentities();
		MailTemplate template = (MailTemplate)runContext.get("mailTemplate");
		MailPackage mailing = new MailPackage(template, getWindowControl().getBusinessControl().getAsString(), template != null);
		MemberPermissionChangeEvent changes = (MemberPermissionChangeEvent)runContext.get("permissions");
		List<CurriculumElementMembershipChange> curriculumChanges = changes.generateCurriculumElementMembershipChange(members);
		curriculumService.updateCurriculumElementMemberships(getIdentity(), roles, curriculumChanges, mailing);
		MailHelper.printErrorsAndWarnings(mailing.getResult(), getWindowControl(), false, getLocale());
	}
	
	private void doOpenTools(UserRequest ureq, CurriculumElementRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		CurriculumElement element = curriculumService.getCurriculumElement(row);
		if(element == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			toolsCtrl = new ToolsController(ureq, getWindowControl(), row, element);
			listenTo(toolsCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	
	private void doOpenCurriculumElementOverview(UserRequest ureq, CurriculumElementRow row) {
		List<ContextEntry> overview = BusinessControlFactory.getInstance().createCEListFromString("[Overview:0]");
		doOpenCurriculumElementDetails(ureq, row, overview);
	}
	
	private void doOpenCurriculumElementMetadata(UserRequest ureq, CurriculumElementRow row) {
		List<ContextEntry> overview = BusinessControlFactory.getInstance().createCEListFromString("[Metadata:0]");
		doOpenCurriculumElementDetails(ureq, row, overview);
	}
	
	private void doOpenCurriculumElementLectures(UserRequest ureq, CurriculumElementRow row) {
		List<ContextEntry> overview = BusinessControlFactory.getInstance().createCEListFromString("[Lectures:0]");
		doOpenCurriculumElementDetails(ureq, row, overview);
	}
	
	private void doOpenCurriculumElementUserManagement(UserRequest ureq, CurriculumElementRow row, String memberType) {
		String path = "[Members:0]";
		if(memberType != null) {
			path += "[" + memberType + ":0]";
		}
		List<ContextEntry> overview = BusinessControlFactory.getInstance().createCEListFromString(path);
		doOpenCurriculumElementDetails(ureq, row, overview);
	}
	
	private void doOpenCurriculumElementDetails(UserRequest ureq, CurriculumElementRow row, List<ContextEntry> entries) {
		CurriculumElement element = curriculumService.getCurriculumElement(row);
		if(element == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else if(rootElement != null && rootElement.equals(element)) {
			fireEvent(ureq, new ActivateEvent(entries));
		} else {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance(CurriculumElement.class, row.getKey()), null);
			CurriculumElementDetailsController editCtrl = new CurriculumElementDetailsController(ureq, swControl, toolbarPanel,
					element.getCurriculum(), element, secCallback);
			listenTo(editCtrl);
			toolbarPanel.pushController(row.getDisplayName(), editCtrl);
			editCtrl.activate(ureq, entries, null);
		}
	}
	
	private void doOpenCurriculumElementInNewWindow(CurriculumElementRow row) {
		String businessPath = CurriculumHelper.getBusinessPath(row) + "[Overview:0]";
		String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(businessPath);
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
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
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					referencesCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	
	private void doOpenStructure(UserRequest ureq, CurriculumElementRow row, FormLink link) {
		CurriculumElement curriculumElement = row.getCurriculumElement();
		
		curriculumStructureCalloutCtrl = new CurriculumStructureCalloutController(ureq, getWindowControl(), curriculumElement);
		listenTo(curriculumStructureCalloutCtrl);
		
		CalloutSettings settings = new CalloutSettings(true, CalloutOrientation.bottom, true,  null);
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				curriculumStructureCalloutCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "", settings);
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doOpenCalendars(UserRequest ureq, CurriculumElementRow row) {
		removeAsListenerAndDispose(calendarsCtrl);
		toolbar(false);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Calendars", row.getKey());
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		List<RepositoryEntry> entries = curriculumService.getRepositoryEntriesWithDescendants(row.getCurriculumElement());
		calendarsCtrl = new CurriculumElementCalendarController(ureq, bwControl, row, entries, secCallback);
		listenTo(calendarsCtrl);
		toolbarPanel.pushController(translate("calendars"), calendarsCtrl);
	}
	
	private void doOpenQualityPreview(UserRequest ureq, CurriculumElementRow row) {
		removeAsListenerAndDispose(qualityPreviewCtrl);
		toolbar(false);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("QualityPreview", row.getKey());
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(row);
		qualityPreviewCtrl = new CurriculumElementPreviewListController(ureq, bwControl, toolbarPanel, curriculumElement);
		listenTo(qualityPreviewCtrl);
		toolbarPanel.pushController(row.getDisplayName(), null, row);
		toolbarPanel.pushController(translate("quality.preview"), qualityPreviewCtrl);
	}
	
	private void doOpenLearningProgress(UserRequest ureq, CurriculumElementRow row) {
		removeAsListenerAndDispose(learningPathController);
		toolbar(false);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("LearningProgress", row.getKey());
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(row);
		learningPathController = new CurriculumElementLearningPathController(ureq, bwControl, toolbarPanel, curriculumElement);
		listenTo(learningPathController);
		
		if(curriculumElement == null || !curriculumElement.equals(row.getCurriculumElement())) {
			toolbarPanel.pushController(row.getDisplayName(), null, row);
		}
		toolbarPanel.pushController(translate("learning.progress"), learningPathController);
	}
	
	private void doConfirmDelete(UserRequest ureq, CurriculumElementRow row) {
		if(guardModalController(confirmDeleteCtrl)) return;
		
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(row);
		if(curriculumElement == null) {
			showWarning("warning.curriculum.deleted");
			loadModel();
		} else {
			confirmDeleteCtrl = new ConfirmCurriculumElementDeleteController(ureq, getWindowControl(), curriculumElement);
			listenTo(confirmDeleteCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeleteCtrl.getInitialComponent(), true,
					translate("confirmation.delete.element.title", row.getDisplayName()));
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doConfirmBulkDelete(UserRequest ureq) {
		if(guardModalController(bulkDeleteConfirmationCtrl)) return;

		List<CurriculumElementRow> curriculumElements =  tableEl.getMultiSelectedIndex().stream()
				.map(index  -> tableModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.filter(element -> secCallback.canEditCurriculumElement(element.getCurriculumElement())
						&& !CurriculumElementManagedFlag.isManaged(element.getCurriculumElement(), CurriculumElementManagedFlag.delete))
				.toList();
		
		if(curriculumElements.isEmpty()) {
			showWarning("curriculums.elements.bulk.delete.empty.selection");
		} else {
			List<String> curriculumsNames = curriculumElements.stream()
					.map(CurriculumElementRow::getDisplayName)
					.toList();

			bulkDeleteConfirmationCtrl = new BulkDeleteConfirmationController(ureq, getWindowControl(), 
					translate("curriculums.elements.bulk.delete.text", String.valueOf(curriculumElements.size())),
					translate("curriculums.elements.bulk.delete.confirm", String.valueOf(curriculumElements.size())),
					translate("curriculums.elements.bulk.delete.button"),
					translate("curriculums.elements.bulk.delete.topics"),
					curriculumsNames,
					null);
			
			bulkDeleteConfirmationCtrl.setUserObject(new ToDelete(curriculumElements));
			listenTo(bulkDeleteConfirmationCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), bulkDeleteConfirmationCtrl.getInitialComponent(),
					true, translate("curriculums.elements.bulk.delete.title"), true);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doDeleteCurriculumElements(ToDelete toDelete) {
		for(CurriculumElementRow element:toDelete.elements()) {
			CurriculumElement elementToDelete = curriculumService.getCurriculumElement(element);
			if(elementToDelete != null) {
				curriculumService.deleteCurriculumElement(element);
				dbInstance.commitAndCloseSession();
			}
		}
	}
	
	private void launch(UserRequest ureq, RepositoryEntryRef ref) {
		String businessPath = "[RepositoryEntry:" + ref.getKey() + "]";
		if(!NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl())) {
			tableEl.reloadData();
		}
	}
	
	private void toolbar(boolean enable) {
		toolbarPanel.setToolbarEnabled(enable);
	}
	
	private record ToDelete(List<CurriculumElementRow> elements) {
		//
	}
	
	private class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		private Link newLink;
		private Link openLink;
		private Link editLink;
		private Link moveLink;
		private Link copyLink;
		private Link deleteLink;
		private Link addMemberLink;
		private Link manageMembersLink;
		
		private CurriculumElementRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl,
				CurriculumElementRow row, CurriculumElement element) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");

			List<String> links = new ArrayList<>(4);
			
			openLink = addLink("open.new.tab", "o_icon_arrow_up_right_from_square", links);
			openLink.setNewWindow(true, true);
			links.add("-");
			
			if(secCallback.canEditCurriculumElement(element)) {
				editLink = addLink("edit", "o_icon_edit", links);
				if(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.move)) {
					moveLink = addLink("move.element", "o_icon_move", links);
				}
				if(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.addChildren)) {
					addNewElementLinks(element, links);
				}
				copyLink = addLink("copy.element", "o_icon_copy", links);
			}

			if(!managed && secCallback.canManagerCurriculumElementUsers(element)) {
				if(!links.isEmpty()) {
					links.add("-");
				}
				
				manageMembersLink = addLink("manage.members", "o_icon_group", links);
				addMemberLink = addLink("add.member", "o_icon_add_member", links);
			}
			
			if(secCallback.canEditCurriculumElement(element) && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.delete)) {
				links.add("-");
				deleteLink = addLink("delete", "o_icon_delete_item", links);
			}

			mainVC.contextPut("links", links);
			
			putInitialPanel(mainVC);
		}
		
		private void addNewElementLinks(CurriculumElement parentElement, List<String> links) {
			CurriculumElementType parentType = parentElement.getType();
			if(parentType != null && parentType.isSingleElement()) return;
			
			List<CurriculumElementType> types = curriculumService.getAllowedCurriculumElementType(parentElement, null);
			if(types.isEmpty()) {
				newLink = addLink("add.element.under", "o_icon_levels", links);
			} else {
				for(CurriculumElementType type:types) {
					String name = "new_el_" + type.getKey();
					String label = translate("add.element.with.type.under", StringHelper.escapeHtml(type.getDisplayName()));
					Link link = LinkFactory.createLink(name, name, NEW_ELEMENT, label, getTranslator(), mainVC, this, Link.LINK | Link.NONTRANSLATED);
					link.setIconLeftCSS("o_icon o_icon-fw o_icon_levels");
					link.setUserObject(type);
					mainVC.put(name, link);
					links.add(name);
				}
			}
		}
		
		private Link addLink(String name, String iconCss, List<String> links) {
			Link link = LinkFactory.createLink(name, name, getTranslator(), mainVC, this, Link.LINK);
			mainVC.put(name, link);
			links.add(name);
			link.setIconLeftCSS("o_icon o_icon-fw " + iconCss);
			return link;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(openLink == source) {
				close();
				doOpenCurriculumElementInNewWindow(row);
			} else if(editLink == source) {
				close();
				doOpenCurriculumElementMetadata(ureq, row);
			} else if(moveLink == source) {
				close();
				doMoveCurriculumElement(ureq, row);
			} else if(deleteLink == source) {
				close();
				doConfirmDelete(ureq, row);
			} else if(newLink == source) {
				close();
				doNewSubCurriculumElement(ureq, row, null);
			} else if(copyLink == source) {
				close();
				doCopyCurriculumElement(ureq, row);
			} else if(addMemberLink == source) {
				close();
				doImportMembers(ureq, row);
			} else if(manageMembersLink == source) {
				close();
				doOpenCurriculumElementUserManagement(ureq, row, null);
			} else if(source instanceof Link link
					&& NEW_ELEMENT.equals(link.getCommand())
					&& link.getUserObject() instanceof CurriculumElementType type) {
				close();
				doNewSubCurriculumElement(ureq, row, type);
			}
		}
		
		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
}
