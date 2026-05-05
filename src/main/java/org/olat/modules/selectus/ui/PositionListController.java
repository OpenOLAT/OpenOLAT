/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui;

import static org.olat.modules.selectus.ui.events.SelectPositionLightEvent.SELECT_POSITION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.PositionAttributeDefinitionTypeEnum;
import org.olat.modules.selectus.model.PositionLight;
import org.olat.modules.selectus.model.PositionLightWithStatistics;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.attributes.PositionAttributeDefinitionComparator;
import org.olat.modules.selectus.model.attributes.SelectConfiguration;
import org.olat.modules.selectus.model.attributes.TextConfiguration;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.components.LongTextRenderer;
import org.olat.modules.selectus.ui.components.PercentageCellRenderer;
import org.olat.modules.selectus.ui.components.SelectAdditionalAttributeCellRenderer;
import org.olat.modules.selectus.ui.components.StatusCellRenderer;
import org.olat.modules.selectus.ui.events.DeletePositionAnonymousEvent;
import org.olat.modules.selectus.ui.events.DeletePositionPermanentlyEvent;
import org.olat.modules.selectus.ui.events.NewPositionEvent;
import org.olat.modules.selectus.ui.events.SelectApplicationEvent;
import org.olat.modules.selectus.ui.events.SelectPositionEvent;
import org.olat.modules.selectus.ui.events.SelectPositionLightEvent;
import org.olat.modules.selectus.ui.position.CopyPositionListController;
import org.olat.modules.selectus.ui.position.PositionConfirmDeleteAnonymousController;
import org.olat.modules.selectus.ui.position.PositionConfirmDeleteController;
import org.olat.modules.selectus.ui.position.PositionConfirmDeletePermanentlyController;
import org.olat.modules.selectus.ui.position.PositionEditController;
import org.olat.modules.selectus.ui.position.PositionsDataModel;
import org.olat.modules.selectus.ui.position.PositionsDataModel.Fields;
import org.olat.modules.selectus.ui.report.ReportGenerator;
import org.olat.modules.selectus.ui.report.ReportGeneratorResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  30 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PositionListController extends FormBasicController implements TooledController/*, AvailableKeyValuesFactory */ {
	
	private static final String PREFS_ID = "recruitingPositionFlexiList-v3.1";
	public static final int CUSTOM_ATTRIBUTES_COLS_OFFSET = 50000;

	public static final String FILTER_STATUS_KEY = "status";
	public static final String FILTER_ORGANISATION_KEY = "organisation";
	
	private Link addPosition;
	private Link searchButton;
	private Link copyPositionButton;
	private FormLink downloadReportButton;
	private final TooledStackedPanel stackPanel;

	private FlexiTableElement tableEl;
	private PositionsDataModel positionsDataModel;
	
	private CloseableModalController cmc;
	private SearchApplicationsController searchAppsCtrl;
	private PositionEditController addPositionController;
	private CopyPositionListController copyPositionController; 
	private PositionConfirmDeleteController confirmDeleteController;
	private PositionConfirmDeleteAnonymousController confirmDeleteAnonymousController;
	private PositionConfirmDeletePermanentlyController confirmDeletePermanentlyController;
	
	private final Roles roles;
	private final RecruitingSecurityCallback secCallback;
	private final List<Organisation> organisations;
	private final List<PositionAttributeDefinition> globalAttributes;

	@Autowired @Qualifier("reportGenerator")
	private ReportGenerator reportGenerator;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	
	public PositionListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RecruitingSecurityCallback secCallback) {
		super(ureq, wControl, "position_list");
		
		roles = ureq.getUserSession().getRoles();
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		
		globalAttributes = recruitingService.getGlobalAttributeDefinition();
		if(globalAttributes.size() > 1) {
			Collections.sort(globalAttributes, new PositionAttributeDefinitionComparator());
		}
		
		if(organisationModule.isEnabled()) {
			organisations = organisationService.getOrganisations(getIdentity(), roles,
					OrganisationRoles.administrator, OrganisationRoles.principal, OrganisationRoles.selectusmanager);
		} else {
			organisations = List.of(organisationService.getDefaultOrganisation());
		}

		initForm(ureq);
		loadModel();
	}
	
	private List<PositionStatus> getFilterStatus() {
		List<PositionStatus> keyValues = new ArrayList<>();
		if(secCallback.canAddPosition()) {
			keyValues.add(PositionStatus.preparation);
			keyValues.add(PositionStatus.published);
		}
		keyValues.add(PositionStatus.publishedAndInScreening);
		keyValues.add(PositionStatus.closedAndInScreening);
		keyValues.add(PositionStatus.closedAndNoRating);
		if(secCallback.canAddPosition()) {
			keyValues.add(PositionStatus.closed);
		}
		if(secCallback.canReportingPosition()) {
			keyValues.add(PositionStatus.reporting);
		}
		return keyValues;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.positionTitle, SELECT_POSITION));
		if(organisationModule.isEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.organisation, SELECT_POSITION));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.status, new StatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.deadline, new DateCellRenderer()));
		
		if(recruitingModule.isPositionPlannigIdEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.planingsNumber, SELECT_POSITION));
		}
		if(recruitingModule.isPositionDepartmentEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.department, SELECT_POSITION));
		}

		if(secCallback.canReportingPosition()) {
			initColumnsModel(columnsModel, null, getLocale());
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.numOfApplications, "select_apps"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.numOfMaleApplications, "select_apps"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Fields.numOfFemaleApplications, "select_apps"));

		if(secCallback.canDeletePosition()) {
			DefaultFlexiColumnModel deleteColumn = new DefaultFlexiColumnModel("delete", -1, "delete",
							new StaticFlexiCellRenderer("", "delete", null, "o_icon o_icon_delete_item", translate("delete")));
			deleteColumn.setIconHeader("o_icon o_icon_delete_item");
			deleteColumn.setHeaderLabel(translate("delete"));
			deleteColumn.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(deleteColumn);
		}
		
		IdentityEnvironment identityEnv = ureq.getUserSession().getIdentityEnvironment();
		positionsDataModel = new PositionsDataModel(columnsModel, identityEnv, globalAttributes, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "positions", positionsDataModel, 20, false, getTranslator(), formLayout);
		
		tableEl.setAndLoadPersistedPreferences(ureq, PREFS_ID);
		tableEl.setExportEnabled(false);
		tableEl.setElementCssClass("o_sel_position_list");
		tableEl.setPageSize(250);// Large default
		
		tableEl.setSearchEnabled(true);
		initFilters();
		if(secCallback.canReportingPosition()) {
			tableEl.setMultiSelect(true);
			tableEl.setSelectAllEnable(true);
			downloadReportButton = uifactory.addFormLink("download.report", "download.report", null, formLayout, Link.BUTTON);
		}
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("position.list.empty")
				.build());
	}
	
	public void initColumnsModel(FlexiTableColumnModel columnsModel, String action, Locale locale) {
		for(int i=0; i<globalAttributes.size(); i++) {
			PositionAttributeDefinition definition = globalAttributes.get(i);
			PositionAttributeDefinitionTypeEnum type = definition.getTypeEnum();
			if(definition.getTabEnum() == PositionApplicationAttributeTabEnum.global
					&& (type == PositionAttributeDefinitionTypeEnum.question || type == PositionAttributeDefinitionTypeEnum.select
							|| type == PositionAttributeDefinitionTypeEnum.number || type == PositionAttributeDefinitionTypeEnum.percentage
							|| type == PositionAttributeDefinitionTypeEnum.date)) {
				String label = definition.getLabel(locale, true);
				DefaultFlexiColumnModel column = new DefaultFlexiColumnModel(false, "custom.attribute." + i, CUSTOM_ATTRIBUTES_COLS_OFFSET+ i, action, true, "custom-attr-" + i);
				
				if(type == PositionAttributeDefinitionTypeEnum.question) {
					TextConfiguration configuration = definition.getConfiguration(TextConfiguration.class);
					if(configuration == null || configuration.getMaxLength() > 64) {
						column.setCellRenderer(new LongTextRenderer());
					}
				} else if(type == PositionAttributeDefinitionTypeEnum.select) {
					SelectConfiguration configuration = definition.getConfiguration(SelectConfiguration.class);
					column.setCellRenderer(new SelectAdditionalAttributeCellRenderer(configuration, locale));
				} else if(type == PositionAttributeDefinitionTypeEnum.percentage) {
					column.setCellRenderer(new PercentageCellRenderer());
				} else if(type == PositionAttributeDefinitionTypeEnum.date) {
					column.setCellRenderer(new DateCellRenderer());
				}
				
				column.setHeaderLabel(label);
				columnsModel.addFlexiColumnModel(column);
			}
		}
	}
	
	private void initFilters() {
		tableEl.setSearchEnabled(true);

		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		if(organisationModule.isEnabled()) {
			SelectionValues organisationsKV = new SelectionValues();
			for(Organisation organisation: organisations) {
				organisationsKV.add(SelectionValues.entry(organisation.getKey().toString(), StringHelper.escapeHtml(organisation.getDisplayName())));
			}
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.organisations"),
					FILTER_ORGANISATION_KEY, organisationsKV, true));
		}
		
		// Position status
		SelectionValues statusKV = new SelectionValues();
		for(PositionStatus status: getFilterStatus()) {
			statusKV.add(SelectionValues.entry(status.name(), translate("status." + status.name())));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.position.status"),
				FILTER_STATUS_KEY, statusKV, true));
		
		for(PositionAttributeDefinition globalAttribute:globalAttributes) {
			//TODO custom attributes
		}
		
		tableEl.setFilters(true, filters, true, true);
	}
	
	public int getNumOfPositions() {
		return positionsDataModel.getRowCount();
	}
	
	public Long getPositionKeyAt(int index) {
		return positionsDataModel.getObject(index).getKey();
	}
	
	public boolean hasPositionWith(Long key) {
		List<PositionLightWithStatistics> positions = positionsDataModel.getObjects();
		for(PositionLightWithStatistics position:positions) {
			if(position.getKey().equals(key)) {
				return true;
			}
		}
		return false;
	}

	public void loadModel() {
		List<PositionLightWithStatistics> positions = recruitingService
				.getPositionsLightWithStatistics(getIdentity(), roles, globalAttributes, getLocale());
		positionsDataModel.setObjects(positions);
		positionsDataModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
	}
	
	@Override
	public void initTools() {
		if(secCallback.canAddPosition()) {
			addPosition = LinkFactory.createToolLink("add_position", translate("add_position"), this);
			addPosition.setElementCssClass("o_sel_add_position");
			addPosition.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
			stackPanel.addTool(addPosition, Align.left);
			
			copyPositionButton = LinkFactory.createToolLink("copy.position", translate("copy.position"), this);
			copyPositionButton.setElementCssClass("o_sel_copy_position");
			copyPositionButton.setIconLeftCSS("o_icon o_icon-lg o_icon_copy");
			stackPanel.addTool(copyPositionButton, Align.left);
		}
		
		if(secCallback.canSearchApplications()) {
			searchButton = LinkFactory.createToolLink("search.application", translate("search.application"), this);
			searchButton.setElementCssClass("o_sel_search_application");
			searchButton.setIconLeftCSS("o_icon o_icon-lg o_icon_search");
			stackPanel.addTool(searchButton, Align.left);
		}
	}
	
	@Override
	public void removeModalControllers() {
		super.removeModalControllers();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(downloadReportButton == source) {
			doDownloadReport(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				PositionLight position = positionsDataModel.getObject(se.getIndex());
				if(SELECT_POSITION.equals(cmd)) {
					fireEvent(ureq, new SelectPositionLightEvent(position));
				} else if("select_apps".equals(cmd)) {
					List<ContextEntry> activation = BusinessControlFactory.getInstance()
							.createCEListFromString(OresHelper.createOLATResourceableType("Applications"));
					fireEvent(ureq, new SelectPositionLightEvent(position, activation));
				} else if("delete".equals(cmd)) {
					confirmDelete(ureq, position);
				}
			} else if(event instanceof FlexiTableSearchEvent ftse) {
				doSearch(ftse.getSearch());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSearch(String searchString) {
		positionsDataModel.filter(searchString, tableEl.getFilters());
		tableEl.reset(true, true, false);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == addPosition) {
			addPosition(ureq);
		} else if(source == copyPositionButton) {
			doCopyPosition(ureq);
		} else if(source == searchButton) {
			searchApplications(ureq);
		} else if(source == stackPanel) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				if(addPositionController != null && pe.getController() == addPositionController) {
					Position position = addPositionController.getPosition();
					fireEvent(ureq, Event.CHANGED_EVENT);
					fireEvent(ureq, new SelectPositionEvent(position));
					cleanUp();
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == confirmDeleteController) {
			Position position = confirmDeleteController.getPosition();
			cmc.deactivate();
			cleanUp();
			if (event instanceof DeletePositionPermanentlyEvent) {
				confirmDeletePermanentely(ureq, position);
			} else if(event instanceof DeletePositionAnonymousEvent) {
				confirmDeleteAnonymous(ureq, position);
			} else if(event == Event.DONE_EVENT) {
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(source == confirmDeletePermanentlyController || source == confirmDeleteAnonymousController) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(addPositionController == source) {
			if(event == Event.CANCELLED_EVENT) {
				Position position = addPositionController.getPosition();
				fireEvent(ureq, Event.CHANGED_EVENT);
				fireEvent(ureq, new SelectPositionEvent(position));
				cleanUp();
			}
		} else if(copyPositionController == source) {
			if(event instanceof NewPositionEvent ep) {
				fireEvent(ureq, Event.CHANGED_EVENT);
				fireEvent(ureq, new SelectPositionEvent(ep.getNewPosition(), true));
				cleanUp();
			}
		} else if(source == searchAppsCtrl) {
			if(event instanceof SelectApplicationEvent) {
				fireEvent(ureq, event);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
		} 
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeletePermanentlyController);
		removeAsListenerAndDispose(confirmDeleteController);
		removeAsListenerAndDispose(addPositionController);
		removeAsListenerAndDispose(searchAppsCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeletePermanentlyController = null;
		confirmDeleteController = null;
		addPositionController = null;
		searchAppsCtrl = null;
		cmc = null;
	}
	
	private void searchApplications(UserRequest ureq) {
		removeAsListenerAndDispose(searchAppsCtrl);
		removeControllerListener(cmc);
		
		searchAppsCtrl = new SearchApplicationsController(ureq, getWindowControl(), secCallback);
		listenTo(searchAppsCtrl);

		String title = translate("search.application");
		cmc = new CloseableModalController(getWindowControl(), "c", searchAppsCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}

	private void confirmDelete(UserRequest ureq, PositionLight position) {
		Position fullPosition = recruitingService.getPosition(position.getKey());
		if(PositionStatus.reporting.name().equals(fullPosition.getStatus())) {
			confirmDeletePermanentely(ureq, fullPosition);
		} else {
			PositionRole positionRole = recruitingService.getRole(fullPosition, getIdentity());
			RecruitingPositionSecurityCallback positionSecCallback
				= new RecruitingPositionSecurityCallbackImpl(secCallback, fullPosition, getIdentity(), ureq.getUserSession().getRoles(), positionRole);
			confirmDeleteController = new PositionConfirmDeleteController(ureq, getWindowControl(), fullPosition, positionSecCallback);
			listenTo(confirmDeleteController);

			String title = translate("confirm.delete.title");
			cmc = new CloseableModalController(getWindowControl(), "c", confirmDeleteController.getInitialComponent(), title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void confirmDeletePermanentely(UserRequest ureq, Position position) {
		confirmDeletePermanentlyController = new PositionConfirmDeletePermanentlyController(ureq, getWindowControl(), position);
		listenTo(confirmDeletePermanentlyController);
		
		String title = translate("confirm.delete.title");
		cmc = new CloseableModalController(getWindowControl(), "c", confirmDeletePermanentlyController.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void confirmDeleteAnonymous(UserRequest ureq, Position position) {
		confirmDeleteAnonymousController = new PositionConfirmDeleteAnonymousController(ureq, getWindowControl(), position);
		listenTo(confirmDeleteAnonymousController);

		String title = translate("confirm.delete.anonymous.title");
		cmc = new CloseableModalController(getWindowControl(), "c", confirmDeleteAnonymousController.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void addPosition(UserRequest ureq) {
		//TODO selectus organisation
		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		Position newPosition = recruitingService.createPosition(defaultOrganisation);
		PositionRole positionRole = recruitingService.getRole(newPosition, getIdentity());
		RecruitingPositionSecurityCallback positionSecCallback
			= new RecruitingPositionSecurityCallbackImpl(secCallback, newPosition, getIdentity(), ureq.getUserSession().getRoles(), positionRole);
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Edit"), null);
		addPositionController = new PositionEditController(ureq, swControl, stackPanel, newPosition, true, positionSecCallback);
		listenTo(addPositionController);
		
		stackPanel.pushController(translate("new_position"), addPositionController);
	}
	
	private void doCopyPosition(UserRequest ureq) {
		SortKey[] orderBys = tableEl.getOrderBy();
		copyPositionController = new CopyPositionListController(ureq, getWindowControl(), orderBys);
		listenTo(copyPositionController);
		
		stackPanel.pushController(translate("copy.position.configuration"), copyPositionController);
	}
	
	private void doDownloadReport(UserRequest ureq) {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		if(selectedIndexes == null || selectedIndexes.isEmpty()) {
			showWarning("warning.atleast.reporting.position");
			return;
		}
		
		List<Position> selectedPositions = new ArrayList<>(selectedIndexes.size());
		for(Integer selectedIndex:selectedIndexes) {
			PositionLightWithStatistics row = positionsDataModel.getObject(selectedIndex.intValue());
			if(PositionStatus.reporting.name().equals(row.getStatus())
					|| PositionStatus.closed.name().equals(row.getStatus())
					|| PositionStatus.closedAndInScreening.name().equals(row.getStatus())
					|| PositionStatus.closedAndNoRating.name().equals(row.getStatus())
					|| PositionStatus.publishedAndInScreening.name().equals(row.getStatus())
					|| PositionStatus.published.name().equals(row.getStatus())) {
				Position position = recruitingService.getPosition(row.getKey());
				selectedPositions.add(position);
			}
		}
		
		if(selectedPositions == null || selectedPositions.isEmpty()) {
			showWarning("warning.atleast.reporting.position");
			return;
		}
		
		ReportGeneratorResource reportResource = new ReportGeneratorResource(reportGenerator,
				selectedPositions, getIdentity(), getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(reportResource);
	}
}
