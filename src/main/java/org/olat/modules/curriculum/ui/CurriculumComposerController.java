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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateWithDayFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
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
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumManagedFlag;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.model.CurriculumElementInfosSearchParams;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.modules.curriculum.site.CurriculumElementTreeRowComparator;
import org.olat.modules.curriculum.ui.CurriculumComposerTableModel.ElementCols;
import org.olat.modules.curriculum.ui.component.CurriculumStatusCellRenderer;
import org.olat.modules.curriculum.ui.component.MinMaxParticipantsCellRenderer;
import org.olat.modules.curriculum.ui.component.ParticipantsAvailabilityNumRenderer;
import org.olat.modules.curriculum.ui.copy.CopyElement1SettingsStep;
import org.olat.modules.curriculum.ui.copy.CopyElementCallback;
import org.olat.modules.curriculum.ui.copy.CopyElementContext;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.modules.curriculum.ui.event.CurriculumElementEvent;
import org.olat.modules.curriculum.ui.event.SelectCurriculumElementRowEvent;
import org.olat.modules.curriculum.ui.event.SelectLectureBlockEvent;
import org.olat.modules.curriculum.ui.event.SelectReferenceEvent;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.quality.QualityModule;
import org.olat.modules.quality.generator.ui.CurriculumElementPreviewListController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.ParticipantsAvailability.ParticipantsAvailabilityNum;
import org.olat.resource.accesscontrol.model.OfferAndAccessInfos;
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

	static final String FILTER_TYPE = "Type";
	static final String FILTER_OFFER = "Offer";
	static final String FILTER_STATUS = "Status";
	static final String FILTER_PERIOD = "Period";
	static final String FILTER_CURRICULUM = "Curriculum";
	static final String FILTER_OCCUPANCY_STATUS = "Occupancy";
	static final String FILTER_OCCUPANCY_STATUS_NOT_SPECIFIED = "NotSpecified";
	static final String FILTER_OCCUPANCY_STATUS_NOT_REACHED = "NotReached";
	static final String FILTER_OCCUPANCY_STATUS_MIN_REACHED = "MinReached";
	static final String FILTER_OCCUPANCY_STATUS_FREE_SEATS = "FreeSeats";
	static final String FILTER_OCCUPANCY_STATUS_FULLY_BOOKED = "Full";
	static final String FILTER_OCCUPANCY_STATUS_OVERBOOKED = "Overbooked";
	
	protected static final String CMD_MEMBERS = "members";
	protected static final String CMD_PENDING = "pending";
	protected static final String CMD_ADD_ELEMENT = "add-element";
	protected static final String CMD_SELECT_CURRICULUM = "select-cur";

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
	private ReferencesController referencesCtrl;
	private StepsMainRunController copyElementCtrl;
	private EditCurriculumElementMetadataController newElementCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private EditCurriculumElementMetadataController newSubElementCtrl;
	private MoveCurriculumElementController moveElementCtrl;
	private ConfirmDeleteCurriculumElementController confirmDeleteCtrl;
	private CurriculumElementCalendarController calendarsCtrl;
	private CurriculumElementPreviewListController qualityPreviewCtrl;
	private CurriculumElementLearningPathController learningPathController;
	private CurriculumStructureCalloutController curriculumStructureCalloutCtrl;
	private ConfirmDeleteCurriculumElementListController bulkDeleteConfirmationCtrl;
	
	private int counter;
	private final boolean managed;
	private boolean overrideManaged;
	private final String businessPath;
	private final Curriculum curriculum;
	private final CurriculumElement rootElement;
	private final CurriculumComposerConfig config;
	private final CurriculumSecurityCallback secCallback;
	private final LecturesSecurityCallback lecturesSecCallback;
	
	@Autowired
	private ACService acService;
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
	CurriculumComposerController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			Curriculum curriculum, CurriculumElement rootElement, CurriculumComposerConfig config,
			CurriculumSecurityCallback secCallback, LecturesSecurityCallback lecturesSecCallback) {
		super(ureq, wControl, "manage_curriculum_structure");
		businessPath =  BusinessControlFactory.getInstance().getAsString(getWindowControl().getBusinessControl());
		this.toolbarPanel = toolbarPanel;
		this.config = config;
		this.secCallback = secCallback;
		this.curriculum = curriculum;
		this.rootElement = rootElement;
		this.lecturesSecCallback = lecturesSecCallback;
		
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
			layoutCont.contextPut("helpUrl", config.getHelpUrl());
		}
		
		initButtons(formLayout, ureq);
		initFormTable(formLayout, ureq);
		initFilters(ureq);
		initFiltersPresets();
	}
	
	private void initButtons(FormItemContainer formLayout, UserRequest ureq) {
		if(secCallback.canManageCurriculumElementsUsers(curriculum)) {
			if(managed && isAllowedToOverrideManaged(ureq)) {
				overrideLink = uifactory.addFormLink("override.member", formLayout, Link.BUTTON);
				overrideLink.setIconLeftCSS("o_icon o_icon-fw o_icon_refresh");
				
				unOverrideLink = uifactory.addFormLink("unoverride.member", formLayout, Link.BUTTON);
				unOverrideLink.setIconLeftCSS("o_icon o_icon-fw o_icon_refresh");
				unOverrideLink.setVisible(false);
			}
		}
		
		if(secCallback.canNewCurriculumElement(curriculum)) {
			newElementMenu = uifactory.addDropdownMenu("add.curriculum.element.menu", "add.curriculum.element.menu", null, formLayout, getTranslator());
			newElementMenu.setElementCssClass("o_sel_curriculum_new_elements");
			newElementMenu.setIconCSS("o_icon o_icon-fw o_icon_add");
			newElementMenu.setOrientation(DropdownOrientation.right);
			
			List<CurriculumElementType> allowedTypes;
			if(rootElement == null) {
				allowedTypes = curriculumService.getAllowedCurriculumElementType(null, null);
			} else {
				allowedTypes = curriculumService.getAllowedCurriculumElementType(rootElement, null);
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
		if(rootElement != null) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.numbering));
		}
		DefaultFlexiColumnModel nameCol;
		if(config.isFlat()) {
			nameCol = new DefaultFlexiColumnModel(ElementCols.displayName, "select");
		} else {
			TreeNodeFlexiCellRenderer treeNodeRenderer = new ElementTreeNodeFlexiCellRenderer("select");
			treeNodeRenderer.setPush(true);
			nameCol = new DefaultFlexiColumnModel(ElementCols.displayName, treeNodeRenderer);
		}
		nameCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(nameCol);
		
		DefaultFlexiColumnModel structureCol = new DefaultFlexiColumnModel(ElementCols.structure);
		structureCol.setIconHeader("o_icon o_icon-lg o_icon_structure");
		structureCol.setExportable(false);
		columnsModel.addFlexiColumnModel(structureCol);
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementCols.externalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(curriculum == null, ElementCols.curriculum,
				CMD_SELECT_CURRICULUM));
		DateWithDayFlexiCellRenderer dateRenderer = new DateWithDayFlexiCellRenderer(getLocale());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.beginDate, dateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.endDate, dateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.resources));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false,
				ElementCols.numOfMembers, CMD_MEMBERS));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(config.isFlat(),
				ElementCols.numOfPending, CMD_PENDING));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(config.isDefaultNumOfParticipants(),
				ElementCols.numOfParticipants, CurriculumRoles.participant.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false,
				ElementCols.numOfCoaches, CurriculumRoles.coach.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false,
				ElementCols.numOfMasterCoaches, CurriculumRoles.mastercoach.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false,
				ElementCols.numOfOwners, CurriculumRoles.owner.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false,
				ElementCols.numOfCurriculumElementOwners, CurriculumRoles.curriculumelementowner.name()));
		if(config.isWithMixMaxColumn()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementCols.minMaxParticipants,
				new MinMaxParticipantsCellRenderer()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementCols.availability,
					new ParticipantsAvailabilityNumRenderer(getLocale())));
		}
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
		progressCol.setIconHeader("o_icon o_icon-lg o_icon_progress");
		columnsModel.addFlexiColumnModel(progressCol);

		if(secCallback.canEditCurriculumElements(curriculum) || (!managed && secCallback.canManageCurriculumElementsUsers(curriculum))) {
			StickyActionColumnModel toolsColumn = new StickyActionColumnModel(ElementCols.tools);
			toolsColumn.setIconHeader("o_icon o_icon-lg o_icon_actions");
			toolsColumn.setExportable(false);
			toolsColumn.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(toolsColumn);
		}
		
		if(!config.isFlat()) {
			for(int i=columnsModel.getColumnCount(); i-->0; ) {
				columnsModel.getColumnModel(i).setSortable(false);
			}
		}
		
		tableModel = new CurriculumComposerTableModel(columnsModel, config.isFlat(), getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 50, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setElementCssClass("o_curriculum_el_listing");		
		if(secCallback.canNewCurriculumElement(curriculum)) {
			tableEl.setEmptyTableSettings("table.curriculum.element.empty", "table.curriculum.element.empty.hint", "o_icon_curriculum_element", "add.curriculum.element", "o_icon_add", true);
		} else {			
			tableEl.setEmptyTableSettings("table.curriculum.element.empty", null, "o_icon_curriculum_element");
		}
		tableEl.setExportEnabled(true);
		tableEl.setSearchEnabled(true);
		
		String tablePrefsId = getTablePrefsId();
		tableEl.setAndLoadPersistedPreferences(ureq, tablePrefsId);

		if(secCallback.canNewCurriculumElement(curriculum) && config.isFlat()) {
			bulkDeleteButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
			tableEl.addBatchButton(bulkDeleteButton);
			tableEl.setMultiSelect(true);
		}
	}
	
	private String getTablePrefsId() {
		if(rootElement != null) {
			return "curriculum-composer-v7";
		}
		if(curriculum != null) {
			return "cur-implementations-v7";
		}
		return "cur-otherlist-v7";
	}
	
	private void initFilters(UserRequest ureq) {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		if(curriculum == null) {
			List<Curriculum> curriculums = loadCurriculumsForFilter(ureq);
			if(!curriculums.isEmpty()) {
				SelectionValues curriculumValues = new SelectionValues();
				for(Curriculum cur:curriculums) {
					String key = cur.getKey().toString();
					String value = StringHelper.escapeHtml(cur.getDisplayName());
					if(StringHelper.containsNonWhitespace(cur.getIdentifier())) {
						value += " <small class=\"mute\"> \u00B7 " + StringHelper.escapeHtml(cur.getIdentifier()) + "</small>";
					}
					curriculumValues.add(SelectionValues.entry(key, value));
				}
				
				FlexiTableMultiSelectionFilter curriculumFilter = new FlexiTableMultiSelectionFilter(translate("filter.curriculum"),
						FILTER_CURRICULUM, curriculumValues, true);
				filters.add(curriculumFilter);
			}
		}
		
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.preparation.name(), translate("filter.preparation")));
		if(rootElement == null) {
			statusValues.add(SelectionValues.entry(CurriculumElementStatus.provisional.name(), translate("filter.provisional")));
			statusValues.add(SelectionValues.entry(CurriculumElementStatus.confirmed.name(), translate("filter.confirmed")));
		}
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.active.name(), translate("filter.active")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.cancelled.name(), translate("filter.cancelled")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.finished.name(), translate("filter.finished")));
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
		
		FlexiTableDateRangeFilter periodFilter = new FlexiTableDateRangeFilter(translate("filter.date.range"),
				FILTER_PERIOD, true, false, getLocale());
		filters.add(periodFilter);
		
		if(config.isWithMixMaxColumn()) {
			SelectionValues occupancyValues = new SelectionValues();
			occupancyValues.add(SelectionValues.entry(FILTER_OCCUPANCY_STATUS_NOT_SPECIFIED, translate("filter.occupancy.status.not.specified")));
			occupancyValues.add(SelectionValues.entry(FILTER_OCCUPANCY_STATUS_NOT_REACHED, translate("filter.occupancy.status.not.reached")));
			occupancyValues.add(SelectionValues.entry(FILTER_OCCUPANCY_STATUS_MIN_REACHED, translate("filter.occupancy.status.min.reached")));
			occupancyValues.add(SelectionValues.entry(FILTER_OCCUPANCY_STATUS_FREE_SEATS, translate("filter.occupancy.status.free.seats")));
			occupancyValues.add(SelectionValues.entry(FILTER_OCCUPANCY_STATUS_FULLY_BOOKED, translate("filter.occupancy.status.fully.booked")));
			occupancyValues.add(SelectionValues.entry(FILTER_OCCUPANCY_STATUS_OVERBOOKED, translate("filter.occupancy.status.overbooked")));
			FlexiTableMultiSelectionFilter occupanyFilter = new FlexiTableMultiSelectionFilter(translate("filter.occupancy.status"),
					FILTER_OCCUPANCY_STATUS, occupancyValues, true);
			filters.add(occupanyFilter);
			
		}
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	private List<Curriculum> loadCurriculumsForFilter(UserRequest ureq) {
		CurriculumSearchParameters managerParams = new CurriculumSearchParameters();
		managerParams.setCurriculumAdmin(getIdentity());
		Set<Curriculum> curriculums = new HashSet<>(curriculumService.getCurriculums(managerParams));
		
		Roles roles = ureq.getUserSession().getRoles();
		if(roles.isPrincipal()) {
			CurriculumSearchParameters principalParams = new CurriculumSearchParameters();
			principalParams.setCurriculumPrincipal(getIdentity());
			curriculums.addAll(curriculumService.getCurriculums(principalParams));
		}
		
		CurriculumSearchParameters elementOwnerParams = new CurriculumSearchParameters();
		elementOwnerParams.setElementOwner(getIdentity());
		curriculums.addAll(curriculumService.getCurriculums(elementOwnerParams));
		return List.copyOf(curriculums);
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
						List.of(CurriculumElementStatus.preparation.name(), CurriculumElementStatus.provisional.name(),
								CurriculumElementStatus.confirmed.name(), CurriculumElementStatus.active.name() ))));
		relevantTab.setFiltersExpanded(true);
		tabs.add(relevantTab);
		map.put(RELEVANT_TAB_ID.toLowerCase(), relevantTab);
		
		for(CurriculumElementStatus status:CurriculumElementStatus.visibleAdmin()) {
			if(status == CurriculumElementStatus.deleted
					|| (rootElement != null && (status == CurriculumElementStatus.provisional || status == CurriculumElementStatus.confirmed))) {
				continue;
			}
			
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
        super.doDispose();
	}
	
	void loadModel() {
		CurriculumElementInfosSearchParams searchParams = getSearchParams();
		
		List<CurriculumElementInfos> elements = curriculumService.getCurriculumElementsWithInfos(searchParams);
		if(rootElement == null) {
			loadViewOnlyImplementations(elements);
		} else {
			loadGapsInElements(elements);
		}
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
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
	}
	
	private void loadViewOnlyImplementations(List<CurriculumElementInfos> elements) {
		if(rootElement != null) return;

		Set<Long> elementsMap = elements.stream()
				.map(CurriculumElementInfos::getKey)
				.collect(Collectors.toSet());
		
		List<CurriculumElement> ownedElements = new ArrayList<>();//TODO curriculum secCallback.getOwnedCurriculumElements();
		
		Set<Long> implementationsKeys = new HashSet<>();
		for(CurriculumElement ownedElement:ownedElements) {

			if(!elementsMap.contains(ownedElement.getKey())) {
				if(ownedElement.getParent() == null) {
					implementationsKeys.add(ownedElement.getKey());
				} else {
					List<Long> ancestors = ownedElement.getMaterializedPathKeysList();
					if(!ancestors.isEmpty()) {
						Long implementationKey = ancestors.get(0);
						if(!elementsMap.contains(implementationKey)) {
							implementationsKeys.add(implementationKey);
							
						}
						
					}
				}
			}
		}
		
		if(!implementationsKeys.isEmpty()) {
			List<CurriculumElementRef> implementsRefs = implementationsKeys.stream()
					.map(CurriculumElementRefImpl::new)
					.map(CurriculumElementRef.class::cast).toList();
			
			CurriculumElementInfosSearchParams searchParams = new CurriculumElementInfosSearchParams(null);
			if(curriculum != null) {
				searchParams.setCurriculum(curriculum);
			}
			searchParams.setCurriculumElements(implementsRefs);
			searchParams.setImplementationsOnly(true);
			
			List<CurriculumElementInfos> implementations = curriculumService.getCurriculumElementsWithInfos(searchParams);
			elements.addAll(implementations);
		}
	}
	
	/**
	 * Load the gaps in the tree if an implementation is loaded (rootElement is not null).
	 * 
	 * @param elements The elements loaded by permissions
	 */
	private void loadGapsInElements(List<CurriculumElementInfos> elements) {
		if(rootElement == null) return;
		
		Set<Long> elementsMap = elements.stream()
				.map(CurriculumElementInfos::getKey)
				.collect(Collectors.toSet());
		
		Set<Long> gaps = new HashSet<>();
		for(CurriculumElementInfos element:elements) {
			CurriculumElement parent = element.curriculumElement().getParent();
			if(parent != null && !elementsMap.contains(parent.getKey())) {
				List<Long> ancestors = parent.getMaterializedPathKeysList();
				gaps.add(parent.getKey());
				if(!ancestors.isEmpty()) {
					for(Long ancestor:ancestors) {
						if(!elementsMap.contains(ancestor)) {
							gaps.add(ancestor);
						}
					}
				}
			}
		}
		// Remove the root element, because it's not shown
		gaps.remove(rootElement.getKey());
		gaps.removeAll(rootElement.getMaterializedPathKeysList());
		
		if(!gaps.isEmpty()) {
			CurriculumElementInfosSearchParams searchParams = new CurriculumElementInfosSearchParams(null);
			List<CurriculumElementRef> gapEls = gaps.stream()
					.map(CurriculumElementRefImpl::new)
					.map(CurriculumElementRef.class::cast)
					.toList();
			searchParams.setCurriculumElements(gapEls);
			List<CurriculumElementInfos> gapElements = curriculumService.getCurriculumElementsWithInfos(searchParams);
			elements.addAll(gapElements);
		}
	}

	private void filterModel() {
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		// Don't reload the data, only reset paging and number of rows
		tableEl.reset(true, true, false);
	}
	
	private CurriculumElementInfosSearchParams getSearchParams() {
		CurriculumElementInfosSearchParams searchParams = new CurriculumElementInfosSearchParams(getIdentity());
		if(curriculum != null) {
			searchParams.setCurriculum(curriculum);
		}
		if(rootElement != null) {
			searchParams.setParentElement(rootElement, false);
		}
		if(config.isImplementationsOnly()) {
			searchParams.setImplementationsOnly(true);
		}
		return searchParams;
	}
	
	private void reloadElement(CurriculumElement element) {
		CurriculumElementRow row = tableModel.getCurriculumElementRowByKey(element.getKey());
		if(row != null) {
			row.setCurriculumElement(element);
			row.setCurriculumElementType(element.getType());
			row.setParticipantsAvailabilityNum(acService.getParticipantsAvailability(
					row.getMinMaxParticipants().max(),
					row.getNumOfParticipants() + row.getNumOfPending(), true));
		}
	}
	
	private CurriculumElementRow forgeRow(CurriculumElementInfos element) {
		String id = element.getKey().toString();
		ParticipantsAvailabilityNum participantsAvailability = acService.getParticipantsAvailability(
				element.curriculumElement().getMaxParticipants(),
				element.numOfParticipants() + element.numOfPending(), true);
		
		
		FormLink toolsLink = uifactory.addFormLink("tools_".concat(id), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-lg");
		toolsLink.setTitle(translate("action.more"));
		
		FormLink structureLink = null;
		CurriculumElementType type = element.curriculumElement().getType();
		if(type == null || !type.isSingleElement()) {
			structureLink = uifactory.addFormLink("structure_".concat(id), "structure", "", null, null, Link.NONTRANSLATED);
			structureLink.setIconLeftCSS("o_icon o_icon-lg o_icon_structure");
			structureLink.setTitle(translate("action.structure"));
		}
		
		FormLink resourcesLink = null;
		long refs = element.numOfResources() + element.numOfLectureBlocks();
		if(refs > 0) {
			resourcesLink = uifactory.addFormLink("resources_" + (++counter), "resources", String.valueOf(refs), null, null, Link.NONTRANSLATED);
		}
		CurriculumElementRow row = new CurriculumElementRow(element.curriculumElement(), refs,
				element.numOfParticipants(), element.numOfCoaches(), element.numOfOwners(),
				element.numOfCurriculumElementOwners(), element.numOfMasterChoaches(), element.numOfPending(),
				participantsAvailability, toolsLink, resourcesLink, structureLink);
		
		boolean editable = secCallback.canEditCurriculumElement(element.curriculumElement());
		row.setSelectable(editable);
		toolsLink.setUserObject(row);
		if(structureLink != null) {
			structureLink.setUserObject(row);
		}
		if(resourcesLink != null) {
			resourcesLink.setUserObject(row);
		}

		String path = businessPath + "[CurriculumElement:" + id + "]";
		row.setBaseUrl(BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(path));
		String curriculumPath = CurriculumHelper.getCurriculumBusinessPath(row.getCurriculumKey());
		row.setCurriculumUrl(BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(curriculumPath));
		
		if(row.isCalendarsEnabled() && editable) {
			FormLink calendarsLink = uifactory.addFormLink("cals_" + (++counter), "calendars", "", null, null, Link.LINK | Link.NONTRANSLATED);
			calendarsLink.setIconLeftCSS("o_icon o_icon-lg o_icon_calendar");
			calendarsLink.setTitle(translate("calendars"));
			row.setCalendarsLink(calendarsLink);
			calendarsLink.setUserObject(row);
		}
		if(row.isLecturesEnabled() && editable) {
			FormLink lecturesLink = uifactory.addFormLink("lecs_" + (++counter), "lectures", "", null, null, Link.LINK | Link.NONTRANSLATED);
			lecturesLink.setIconLeftCSS("o_icon o_icon-lg o_icon_lecture");
			lecturesLink.setTitle(translate("lectures"));
			row.setLecturesLink(lecturesLink);
			lecturesLink.setUserObject(row);
		}
		if(qualityModule.isEnabled() && qualityModule.isPreviewEnabled() && editable) {
			FormLink qualityPreviewLink = uifactory.addFormLink("qp_" + (++counter), "quality.preview", "", null, null, Link.LINK | Link.NONTRANSLATED);
			qualityPreviewLink.setIconLeftCSS("o_icon o_icon-lg o_icon_qual_preview");
			qualityPreviewLink.setTitle(translate("quality.preview"));
			row.setQualityPreviewLink(qualityPreviewLink);
			qualityPreviewLink.setUserObject(row);
		}
		if(row.isLearningProgressEnabled() && editable) {
			FormLink learningProgressLink = uifactory.addFormLink("lp_" + (++counter), "learning.progress", "", null, null, Link.LINK | Link.NONTRANSLATED);
			learningProgressLink.setIconLeftCSS("o_icon o_icon-lg o_icon_progress");
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
			if(tableEl.getSelectedFilterTab() == null) {
				if(config.isFlat()) {
					activateFilterTab(ureq, statusTabMap.get(RELEVANT_TAB_ID.toLowerCase()));
				} else {
					activateFilterTab(ureq, statusTabMap.get(ALL_TAB_ID.toLowerCase()));
				}
			}
			activateElement(ureq, entries);
		} else if("Search".equalsIgnoreCase(type)) {
			Long elementKey = entries.get(0).getOLATResourceable().getResourceableId();
			tableEl.quickSearch(ureq, elementKey.toString());
		} else if(statusTabMap != null && statusTabMap.containsKey(type.toLowerCase())) {
			FlexiFiltersTab statusTab = statusTabMap.get(type.toLowerCase());
			activateFilterTab(ureq, statusTab);
		}
	}
	
	private void activateFilterTab(UserRequest ureq, FlexiFiltersTab statusTab) {
		tableEl.setSelectedFilterTab(ureq, statusTab);
		if(RELEVANT_TAB_ID.equalsIgnoreCase(statusTab.getId()) && config.isFlat()) {
			FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
			sortOptions.setDefaultOrderBy(new SortKey(ElementCols.beginDate.name(), true));
			sortOptions.setFromColumnModel(true);
			tableEl.setSortSettings(sortOptions);
		}
		loadModel();
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
		if(newElementCtrl == source || newSubElementCtrl == source
				|| moveElementCtrl == source || confirmDeleteCtrl == source
				|| bulkDeleteConfirmationCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == copyElementCtrl) {
			if (event == Event.CANCELLED_EVENT) {
				getWindowControl().pop();
				cleanUp();
			} else if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				getWindowControl().pop();
				loadModel();
				cleanUp();
			}
		} else if (referencesCtrl == source || curriculumStructureCalloutCtrl == source) {
			toolsCalloutCtrl.deactivate();
			cleanUp();
			if(event instanceof SelectReferenceEvent sre) {
				launch(ureq, sre.getEntry());
			} else if(event instanceof SelectCurriculumElementRowEvent scee) {
				List<ContextEntry> subEntries = BusinessControlFactory.getInstance().createCEListFromString("[Structure:0]");
				doOpenCurriculumElementDetails(ureq, scee.getCurriculumElement(), subEntries);
			} else if(event instanceof SelectLectureBlockEvent slbe) {
				List<ContextEntry> subEntries = BusinessControlFactory.getInstance().createCEListFromString("[Lectures:0]");
				doOpenCurriculumElementDetails(ureq, slbe.getCurriculumElement(), subEntries);
			}
		} else if(source instanceof CurriculumElementDetailsController) {
			if(event == Event.CHANGED_EVENT) {
				loadModel();
			} else if(event instanceof CurriculumElementEvent cee) {
				if(rootElement != null) {
					fireEvent(ureq, event);
				} else {
					toolbarPanel.popController(source);
					doOpenCurriculumElementDetails(ureq, cee.getCurriculumElement(), cee.getContext());
				}
			}
		} else if(cmc == source || toolsCalloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(curriculumStructureCalloutCtrl);
		removeAsListenerAndDispose(bulkDeleteConfirmationCtrl);
		removeAsListenerAndDispose(copyElementCtrl);
		removeAsListenerAndDispose(newSubElementCtrl);
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(moveElementCtrl);
		removeAsListenerAndDispose(newElementCtrl);
		removeAsListenerAndDispose(referencesCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(cmc);
		curriculumStructureCalloutCtrl = null;
		bulkDeleteConfirmationCtrl = null;
		copyElementCtrl = null;
		newSubElementCtrl = null;
		toolsCalloutCtrl = null;
		confirmDeleteCtrl = null;
		moveElementCtrl = null;
		newElementCtrl = null;
		referencesCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(toolbarPanel == source) {
			if(event instanceof PopEvent pe) {
				doProcessPopEvent(ureq, pe);
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doProcessPopEvent(UserRequest ureq, PopEvent pe) {
		if(rootElement == null && curriculum == null) {
			if(pe.getUserObject() instanceof CurriculumElement || pe.getController() instanceof CurriculumElementDetailsController) {
				final Object uobject = toolbarPanel.getLastUserObject();
				final Controller uctrl = toolbarPanel.getLastController();

				toolbarPanel.popUpToController(this);
				if(uctrl == this) {
					if(pe.getController() instanceof CurriculumElementDetailsController detailsCtrl) {
						reloadElement(detailsCtrl.getCurriculumElement());
					}
				} else if(uobject instanceof CurriculumElement curriculumElement) {
					doOpenCurriculumElementDetails(ureq, curriculumElement, List.of());
				}
			} else if(pe.getController() instanceof CurriculumElementDetailsController detailsCtrl) {
				reloadElement(detailsCtrl.getCurriculumElement());
			}
		} else if(pe.getController() instanceof CurriculumElementDetailsController detailsCtrl) {
			reloadElement(detailsCtrl.getCurriculumElement());
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == overrideLink) {
			doOverrideManagedResource();
		} else if (source == unOverrideLink) {
			doUnOverrideManagedResource();
		} else if(newGenericElementButton == source) {
			doNewCurriculumElement(ureq, null);
		} else if(bulkDeleteButton == source) {
			doConfirmBulkDelete(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				if("select".equals(cmd)) {
					CurriculumElementRow row = tableModel.getObject(se.getIndex());
					doOpenCurriculumElementOverview(ureq, row);
				} else if(CMD_MEMBERS.equals(cmd)) {
					CurriculumElementRow row = tableModel.getObject(se.getIndex());
					doOpenCurriculumElementUserManagement(ureq, row, null);
				} else if(CMD_PENDING.equals(cmd)) {
					CurriculumElementRow row = tableModel.getObject(se.getIndex());
					doOpenCurriculumElementUserManagement(ureq, row, CMD_PENDING);
				} else if(CurriculumRoles.isValueOf(cmd)) {
					CurriculumElementRow row = tableModel.getObject(se.getIndex());
					doOpenCurriculumElementUserManagement(ureq, row, CurriculumRoles.valueOf(cmd).name());
				} else if(CMD_SELECT_CURRICULUM.equals(cmd)) {
					CurriculumElementRow row = tableModel.getObject(se.getIndex());
					doOpenCurriculum(ureq, row);
				}
			} else if(event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				filterModel();
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
			} else if("structure".equals(cmd)) {
				doOpenStructure(ureq, (CurriculumElementRow)link.getUserObject(), link);
			} else if("calendars".equals(cmd)) {
				doOpenCalendars(ureq, (CurriculumElementRow)link.getUserObject());
			} else if("lectures".equals(cmd) && link.getUserObject() instanceof CurriculumElementRow row) {
				doOpenCurriculumElementAbsences(ureq, row);
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
		newElementCtrl = new EditCurriculumElementMetadataController(ureq, getWindowControl(), parentElement,
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
			newSubElementCtrl = new EditCurriculumElementMetadataController(ureq, getWindowControl(), parentElement,
					type, elementsCurriculum, secCallback);
			newSubElementCtrl.setParentElement(parentElement);
			listenTo(newSubElementCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					newSubElementCtrl.getInitialComponent(), true, translate("add.curriculum.element"));
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
			moveElementCtrl = new MoveCurriculumElementController(ureq, getWindowControl(), element, curriculum, secCallback);
			listenTo(moveElementCtrl);
			
			String title = translate("move.element.title", StringHelper.escapeHtml(row.getDisplayName()));
			cmc = new CloseableModalController(getWindowControl(), translate("close"), moveElementCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
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
	
	private void doOpenCurriculum(UserRequest ureq, CurriculumElementRow row) {
		String curriculumPath = CurriculumHelper.getCurriculumBusinessPath(row.getCurriculumKey());
		NewControllerFactory.getInstance().launch(curriculumPath, ureq, getWindowControl());
	}
	
	private void doOpenCurriculumElementOverview(UserRequest ureq, CurriculumElementRow row) {
		List<ContextEntry> overview = BusinessControlFactory.getInstance().createCEListFromString("[Overview:0]");
		doOpenCurriculumElementDetails(ureq, row, overview);
	}
	
	private void doOpenCurriculumElementAbsences(UserRequest ureq, CurriculumElementRow row) {
		List<ContextEntry> overview = BusinessControlFactory.getInstance().createCEListFromString("[Absences:0]");
		doOpenCurriculumElementDetails(ureq, row, overview);
	}
	
	private void doOpenCurriculumElementUserManagement(UserRequest ureq, CurriculumElementRow row, String memberType) {
		String path = "[Members:0]";
		if(CMD_PENDING.equalsIgnoreCase(memberType)) {
			path += "[Pending:0][All:0]";
		} else if(memberType != null) {
			path += "[" + memberType + ":0]";
		}
		List<ContextEntry> overview = BusinessControlFactory.getInstance().createCEListFromString(path);
		doOpenCurriculumElementDetails(ureq, row, overview);
	}
	
	private void doOpenCurriculumElementSettings(UserRequest ureq, CurriculumElementRow row) {
		String path = "[Metadata:0]";
		List<ContextEntry> overview = BusinessControlFactory.getInstance().createCEListFromString(path);
		doOpenCurriculumElementDetails(ureq, row, overview);
	}
	
	private void doOpenCurriculumElementDetails(UserRequest ureq, CurriculumElementRef row, List<ContextEntry> entries) {
		CurriculumElement element = curriculumService.getCurriculumElement(row);
		doOpenCurriculumElementDetails(ureq, element, entries);
	}
	
	private void doOpenCurriculumElementDetails(UserRequest ureq, CurriculumElement element, List<ContextEntry> entries) {
		if(element == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else if(rootElement != null && rootElement.equals(element)) {
			fireEvent(ureq, new ActivateEvent(entries));
		} else if(rootElement != null) {
			fireEvent(ureq, new CurriculumElementEvent(element, entries));
		} else {
			WindowControl swControl	= addToHistory(ureq, OresHelper.createOLATResourceableInstance(CurriculumElement.class, element.getKey()), null);
			CurriculumElementDetailsController editCtrl = new CurriculumElementDetailsController(ureq, swControl, toolbarPanel,
					element.getCurriculum(), element, secCallback, lecturesSecCallback);
			listenTo(editCtrl);
			addIntermediatePath(element);
			toolbarPanel.pushController(element.getDisplayName(), editCtrl);
			editCtrl.activate(ureq, entries, null);
		}
	}
	
	private void addIntermediatePath(CurriculumElement element) {
		List<CurriculumElement> parentLine = curriculumService.getCurriculumElementParentLine(element);
		for(CurriculumElement parent:parentLine) {
			toolbarPanel.popUserObject(parent);
		}
		
		parentLine.remove(element);
		for(CurriculumElement parent:parentLine) {
			toolbarPanel.pushController(parent.getDisplayName(), null, parent);
		}
	}
	
	private void doOpenCurriculumElementInNewWindow(CurriculumElementRow row) {
		String path = "[CurriculumAdmin:0][Curriculums:0][Curriculum:" + row.getCurriculumKey() + "][Implementations:0][CurriculumElement:" + row.getKey() + "][Overview:0]";
		String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(path);
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
			referencesCtrl = new ReferencesController(ureq, getWindowControl(), getTranslator(), row);
			listenTo(referencesCtrl);
	
			CalloutSettings settings = new CalloutSettings(true, CalloutOrientation.bottom, true, null);
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					referencesCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "", settings);
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	
	private void doOpenStructure(UserRequest ureq, CurriculumElementRow row, FormLink link) {
		CurriculumElement curriculumElement = row.getCurriculumElement();
		curriculumStructureCalloutCtrl = new CurriculumStructureCalloutController(ureq, getWindowControl(),
				curriculumElement, null, false, secCallback);
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
			List<CurriculumElement> descendants = curriculumService.getCurriculumElementsDescendants(curriculumElement);
			ConfirmDelete confirmDelete = ConfirmDelete.valueOf(curriculumElement, descendants, getTranslator());
			
			confirmDeleteCtrl = new ConfirmDeleteCurriculumElementController(ureq, getWindowControl(),
					confirmDelete.message(), confirmDelete.confirmation(), confirmDelete.confirmationButton(),
					curriculumElement, descendants);
			listenTo(confirmDeleteCtrl);

			cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeleteCtrl.getInitialComponent(),
					true, confirmDelete.title());
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
			List<CurriculumElement> elements = curriculumElements.stream()
					.map(CurriculumElementRow::getCurriculumElement)
					.toList();

			bulkDeleteConfirmationCtrl = new ConfirmDeleteCurriculumElementListController(ureq, getWindowControl(), 
					translate("curriculums.implementations.bulk.delete.text", String.valueOf(curriculumElements.size())),
					translate("curriculums.implementations.bulk.delete.confirm", String.valueOf(curriculumElements.size())),
					translate("curriculums.element.bulk.delete.button"),
					/* translate("curriculums.elements.bulk.delete.topics"), */
					elements);
			
			bulkDeleteConfirmationCtrl.setUserObject(new ToDelete(curriculumElements));
			listenTo(bulkDeleteConfirmationCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), bulkDeleteConfirmationCtrl.getInitialComponent(),
					true, translate("curriculums.elements.bulk.delete.title"), true);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doCopyElement(UserRequest ureq, CurriculumElementRow row) {
		CurriculumElement element = curriculumService.getCurriculumElement(row);
		List<CurriculumElement> descendants = curriculumService.getCurriculumElementsDescendants(element);
		List<OfferAndAccessInfos> offersAndAccessList = acService.findOfferAndAccessByResource(element.getResource(), true);
		CopyElementContext context = new CopyElementContext(element, descendants, offersAndAccessList);
		CopyElement1SettingsStep step = new CopyElement1SettingsStep(ureq, context);
		CopyElementCallback finish = new CopyElementCallback(context);
		
		removeAsListenerAndDispose(copyElementCtrl);
		String title = translate("wizard.duplicate.element");
		copyElementCtrl = new StepsMainRunController(ureq, getWindowControl(), step, finish, null, title, "");
		listenTo(copyElementCtrl);
		getWindowControl().pushAsModalDialog(copyElementCtrl.getInitialComponent());
		
	}
	
	private void launch(UserRequest ureq, RepositoryEntryRef ref) {
		String coursePath = "[RepositoryEntry:" + ref.getKey() + "]";
		if(!NewControllerFactory.getInstance().launch(coursePath, ureq, getWindowControl())) {
			tableEl.reloadData();
		}
	}
	
	private void toolbar(boolean enable) {
		toolbarPanel.setToolbarEnabled(enable);
	}
	
	private record ToDelete(List<CurriculumElementRow> elements) {
		//
	}
	
	private final class ElementTreeNodeFlexiCellRenderer extends TreeNodeFlexiCellRenderer {
		
		public ElementTreeNodeFlexiCellRenderer(String action) {
			super(action);
		}
		
		@Override
		public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
				FlexiTableComponent source, URLBuilder ubu, Translator translator) {
			CurriculumElementRow elementRow = tableModel.getObject(row);
			renderIndented(renderer, target, cellValue, row, source, ubu, translator, false, elementRow.isSelectable());
		}
	}
	
	private final class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		private Link newLink;
		private Link openLink;
		private Link moveLink;
		private Link deleteLink;
		private Link duplicateLink;
		private Link manageMembersLink;
		private Link openSettingsLink;
		
		private CurriculumElementRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl,
				CurriculumElementRow row, CurriculumElement element) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");

			List<String> links = new ArrayList<>(4);
			
			openLink = addLink("open.new.tab", "o_icon_arrow_up_right_from_square", links);
			openLink.setNewWindow(true, true);
			
			if(curriculum != null && secCallback.canEditCurriculumElementSettings(element)) {
				openSettingsLink = addLink("edit", "o_icon_edit", links);
			}
				
			if(curriculum != null && secCallback.canMoveCurriculumElement(element)
						&& !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.move) && element.getParent() != null) {
				moveLink = addLink("move.element", "o_icon_move", links);
			}
			
			if(curriculum != null && secCallback.canNewCurriculumElement(curriculum)
					&& secCallback.canEditCurriculumElement(element) && element.getParent() != null
					&& !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.addChildren)) {
				addNewElementLinks(element, links);
			}
			
			if(secCallback.canNewCurriculumElement(curriculum)
					&& !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.addChildren)) {
				duplicateLink = addLink("duplicate.element", "o_icon_copy", links);
			}

			if(secCallback.canManageCurriculumElementUsers(element)) {
				links.add("-");
				manageMembersLink = addLink("manage.members", "o_icon_group", links);
			}
			
			if(secCallback.canDeleteCurriculumElement(element)
					&& !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.delete)) {
				links.add("-");
				deleteLink = addLink("delete", "o_icon_delete_item", links);
			}

			mainVC.contextPut("links", links);
			putInitialPanel(mainVC);
		}
		
		private void addNewElementLinks(CurriculumElement parentElement, List<String> links) {
			CurriculumElementType parentType = parentElement.getType();
			if(parentType != null && parentType.isSingleElement()) return;

			links.add("-");
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
			} else if(deleteLink == source) {
				close();
				doConfirmDelete(ureq, row);
			} else if(moveLink == source) {
				close();
				doMoveCurriculumElement(ureq, row);
			} else if(newLink == source) {
				close();
				doNewSubCurriculumElement(ureq, row, null);
			} else if(openSettingsLink == source) {
				close();
				doOpenCurriculumElementSettings(ureq, row);
			} else if(manageMembersLink == source) {
				close();
				doOpenCurriculumElementUserManagement(ureq, row, null);
			} else if(duplicateLink == source) {
				close();
				doCopyElement(ureq, row);
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
