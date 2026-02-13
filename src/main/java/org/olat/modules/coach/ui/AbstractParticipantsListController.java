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
package org.olat.modules.coach.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.model.OrganisationWithParents;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableSingleSelectionFilter;
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
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.session.UserSessionManager;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.OpenInstantMessageEvent;
import org.olat.instantMessaging.model.Buddy;
import org.olat.instantMessaging.model.Presence;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.ParticipantStatisticsEntry;
import org.olat.modules.coach.ui.ParticipantsTableDataModel.ParticipantCols;
import org.olat.modules.coach.ui.component.CertificatesCellRenderer;
import org.olat.modules.coach.ui.component.CompletionCellRenderer;
import org.olat.modules.coach.ui.component.LastVisitCellRenderer;
import org.olat.modules.coach.ui.component.OrganisationWithParentsNameComparator;
import org.olat.modules.coach.ui.component.SearchStateEntry;
import org.olat.modules.coach.ui.component.SuccessStatusCellRenderer;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.ui.admin.IdentityChatCellRenderer;
import org.olat.user.ui.admin.IdentityOrganisationsCellRenderer;
import org.olat.user.ui.organisation.OrganisationsSmallListController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public abstract class AbstractParticipantsListController extends FormBasicController implements Activateable2 {

	protected static final String FILTER_ORGANISATIONS = "organisations";
	protected static final String FILTER_TO_BE_CONFIRMED = "tbc";
	protected static final String FILTER_NOT_VISITED = "not-visited";
	protected static final String FILTER_LAST_VISIT = "last-visit";
	protected static final String FILTER_CERTIFICATES = "certificates";
	protected static final String FILTER_WITH_COURSES = "with-courses";
	protected static final String FILTER_WITHOUT_COURSES = "without-courses";
	protected static final String FILTER_ASSESSMENT = "assessment";

	protected static final String ASSESSMENT_PASSED_NONE = "assessment-passed-none";
	protected static final String ASSESSMENT_PASSED_PARTIALLY = "assessment-passed-partially";
	protected static final String ASSESSMENT_PASSED_ALL = "assessment-passed-all";
	protected static final String ASSESSMENT_NOT_PASSED_NONE = "assessment-not-passed-none";
	protected static final String ASSESSMENT_NOT_PASSED_PARTIALLY = "assessment-not-passed-partially";
	protected static final String ASSESSMENT_NOT_PASSED_ALL = "assessment-not-passed-all";
	protected static final String CERTIFICATES_WITHOUT = "without-certificates";
	protected static final String CERTIFICATES_WITH = "with-certificates";
	protected static final String CERTIFICATES_INVALID = "invalid-certificates";
	protected static final String CONFIRMED_BY_USER = "tbc-user";
	protected static final String CONFIRMED_BY_ADMIN = "tbc-admin";
	protected static final String VISIT_LESS_1_DAY = "less-1-day";
	protected static final String VISIT_LESS_1_WEEK = "less-1-week";
	protected static final String VISIT_LESS_4_WEEKS = "less-4-weeks";
	protected static final String VISIT_LESS_12_MONTHS = "less-12-months";
	protected static final String VISIT_MORE_12_MONTS = "more-12-months";
	
	public static final String ALL_TAB_ID = "All";
	public static final String RELEVANT_TAB = "Relevant";
	private static final String CONFIRMED_BY_ADMIN_TAB = "TBCAdmin";
	private static final String WITHOUT_COURSES_TAB = "WithoutCourse";
	
    protected List<UserPropertyHandler> userPropertyHandlers;

	private FlexiFiltersTab allTab;
	private FormLink batchContactButton;
    protected FlexiTableElement tableEl;
    protected TooledStackedPanel stackPanel;
    protected ParticipantsTableDataModel tableModel;
    protected final SelectionValues organisationsKV = new SelectionValues();

	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
    private ContactFormController contactCtrl;
    private NextPreviousController userOverviewCtrl;
	private CloseableCalloutWindowController calloutCtrl;
    private OrganisationsSmallListController organisationsSmallListCtrl;

	private final boolean chatEnabled;
	protected final boolean organisationsEnabled;
	
    private boolean hasChanged;
	private final String tableId;

    @Autowired
    protected UserManager userManager;
    @Autowired
    protected BaseSecurity securityManager;
	@Autowired
	private InstantMessagingModule imModule;
	@Autowired
	private InstantMessagingService imService;
    @Autowired
    protected CoachingService coachingService;
    @Autowired
    protected BaseSecurityModule securityModule;
	@Autowired
	private UserSessionManager sessionManager;
	@Autowired
	private OrganisationModule organisationModule;
	

    public AbstractParticipantsListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
    		String tableId) {
        super(ureq, wControl, "participants");
        setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));

        this.tableId = tableId;
		chatEnabled = imModule.isEnabled() && imModule.isPrivateEnabled();
		organisationsEnabled = organisationModule.isEnabled();
		
        boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
        userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, isAdministrativeUser);

        this.stackPanel = stackPanel;
        this.stackPanel.setInvisibleCrumb(0);
        stackPanel.addListener(this);
    }

    protected void initTableForm(FormItemContainer formLayout, UserRequest ureq,
    		boolean withReservations, boolean withResources,
    		boolean withProgressAndStatus, boolean withWithoutCourses) {
        //add the table
        FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
        if(chatEnabled) {
			DefaultFlexiColumnModel chatCol = new DefaultFlexiColumnModel(ParticipantCols.status);
			chatCol.setCellRenderer(new IdentityChatCellRenderer(true, getLocale()));
			chatCol.setExportable(false);
			columnsModel.addFlexiColumnModel(chatCol);
		}
       
        int colIndex = UserListController.USER_PROPS_OFFSET;
        for (int i = 0; i < userPropertyHandlers.size(); i++) {
            UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
            boolean visible = userManager.isMandatoryUserProperty(UserListController.usageIdentifyer, userPropertyHandler);
            columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, "select",
                    true, userPropertyHandler.i18nColumnDescriptorLabelKey()));
        }

        if(organisationsEnabled) {
        	columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ParticipantCols.organisations,
        		new IdentityOrganisationsCellRenderer()));
        }
        
        if(withReservations) {
        	columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.reservationConfirmedByAdmin));
        	columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ParticipantCols.reservationConfirmedByUser));
        }

        if(withResources) {
            columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.courses));
            columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.coursesNotAttended));
        }
        
        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.lastVisit,
        		new LastVisitCellRenderer(getTranslator())));

        if (withProgressAndStatus) {
            columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.completion,
            		new CompletionCellRenderer(getTranslator())));
            columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.successStatus,
            		new SuccessStatusCellRenderer()));
        }
        
        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.certificates,
        		new CertificatesCellRenderer()));
        
        ActionsColumnModel actionsCol = new ActionsColumnModel(ParticipantCols.tools);
        actionsCol.setCellRenderer(new ActionsCellRenderer(getTranslator()));
		columnsModel.addFlexiColumnModel(actionsCol);

        tableModel = new ParticipantsTableDataModel(userManager, userPropertyHandlers, columnsModel);
        tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
        tableEl.setCustomizeColumns(true);
        tableEl.setExportEnabled(true);
        tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setSearchEnabled(true);
		tableEl.setEmptyTableSettings("default.tableEmptyMessage", null, "o_icon_user");
        
        batchContactButton = uifactory.addFormLink("contact.link", formLayout, Link.BUTTON);
        batchContactButton.setIconLeftCSS("o_icon o_icon_mail");
        tableEl.addBatchButton(batchContactButton);
        
        UserSession usess = ureq.getUserSession();
        boolean autoCompleteAllowed = securityModule.isUserAllowedAutoComplete(usess.getRoles());
        if (autoCompleteAllowed) {
           tableEl.setSearchEnabled(tableModel, usess);
        }
        
        initFilters(withReservations, withWithoutCourses);
        initFiltersPresets(ureq, withReservations, withWithoutCourses);
        tableEl.setAndLoadPersistedPreferences(ureq, "coaching-" + tableId + "-v1.2");
    }
    
    protected void initFilters(boolean withReservations, boolean withWithoutCourses) {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(2);
    	
    	if(organisationsEnabled) {
    		FlexiTableMultiSelectionFilter participantFilter = new FlexiTableMultiSelectionFilter(translate("filter.organisations"),
    				FILTER_ORGANISATIONS, organisationsKV, true);
    		filters.add(participantFilter);
    	}
    	
    	if(withReservations) {
			SelectionValues toBeConfirmedKV = new SelectionValues();
			toBeConfirmedKV.add(SelectionValues.entry(CONFIRMED_BY_USER, translate("filter.to.be.confirmed.by.user")));
			toBeConfirmedKV.add(SelectionValues.entry(CONFIRMED_BY_ADMIN, translate("filter.to.be.confirmed.by.admin")));
			FlexiTableMultiSelectionFilter participantFilter = new FlexiTableMultiSelectionFilter(translate("filter.to.be.confirmed"),
					FILTER_TO_BE_CONFIRMED, toBeConfirmedKV, true);
			filters.add(participantFilter);
    	}
		
		SelectionValues notVisitedPK = new SelectionValues();
		notVisitedPK.add(SelectionValues.entry(FILTER_NOT_VISITED, translate("filter.not.visited")));
		FlexiTableOneClickSelectionFilter notVisited = new FlexiTableOneClickSelectionFilter(translate("filter.not.visited"),
				FILTER_NOT_VISITED, notVisitedPK, true);
		filters.add(notVisited);
		
		SelectionValues lastVisitPK = new SelectionValues();
		lastVisitPK.add(SelectionValues.entry(VISIT_LESS_1_DAY, translate("filter.visit.less.1.day")));
		lastVisitPK.add(SelectionValues.entry(VISIT_LESS_1_WEEK, translate("filter.visit.less.1.week")));
		lastVisitPK.add(SelectionValues.entry(VISIT_LESS_4_WEEKS, translate("filter.visit.less.4.weeks")));
		lastVisitPK.add(SelectionValues.entry(VISIT_LESS_12_MONTHS, translate("filter.visit.less.12.months")));
		lastVisitPK.add(SelectionValues.entry(VISIT_MORE_12_MONTS, translate("filter.visit.more.12.months")));
		filters.add(new FlexiTableSingleSelectionFilter(translate("filter.last.visit"),
				FILTER_LAST_VISIT, lastVisitPK, true));
		
		SelectionValues assessmentPK = new SelectionValues();
		assessmentPK.add(SelectionValues.entry(ASSESSMENT_PASSED_NONE, translate("filter.assessment.passed.none")));
		assessmentPK.add(SelectionValues.entry(ASSESSMENT_PASSED_PARTIALLY, translate("filter.assessment.passed.partially")));
		assessmentPK.add(SelectionValues.entry(ASSESSMENT_PASSED_ALL, translate("filter.assessment.passed.all")));
		assessmentPK.add(SelectionValues.entry(ASSESSMENT_NOT_PASSED_NONE, translate("filter.assessment.not.passed.none")));
		assessmentPK.add(SelectionValues.entry(ASSESSMENT_NOT_PASSED_PARTIALLY, translate("filter.assessment.not.passed.partially")));
		assessmentPK.add(SelectionValues.entry(ASSESSMENT_NOT_PASSED_ALL, translate("filter.assessment.not.passed.all")));
		filters.add(new FlexiTableSingleSelectionFilter(translate("filter.assessment"),
				FILTER_ASSESSMENT, assessmentPK, true));

		SelectionValues certificatesPK = new SelectionValues();
		certificatesPK.add(SelectionValues.entry(CERTIFICATES_WITHOUT, translate("filter.certificate.without")));
		certificatesPK.add(SelectionValues.entry(CERTIFICATES_WITH, translate("filter.certificate.with")));
		certificatesPK.add(SelectionValues.entry(CERTIFICATES_INVALID, translate("filter.certificate.invalid")));
		filters.add(new FlexiTableSingleSelectionFilter(translate("filter.certificates"),
				FILTER_CERTIFICATES, certificatesPK, false));
		
		if(withWithoutCourses) {
			SelectionValues withoutCoursesPK = new SelectionValues();
			withoutCoursesPK.add(SelectionValues.entry(FILTER_WITHOUT_COURSES, translate("filter.without.courses")));
			FlexiTableOneClickSelectionFilter withoutCoursesFilter = new FlexiTableOneClickSelectionFilter(translate("filter.without.courses"),
					FILTER_WITHOUT_COURSES, withoutCoursesPK, false);
			filters.add(withoutCoursesFilter);
			
			SelectionValues withCoursesPK = new SelectionValues();
			withCoursesPK.add(SelectionValues.entry(FILTER_WITH_COURSES, translate("filter.with.courses")));
			FlexiTableOneClickSelectionFilter withCoursesFilter = new FlexiTableOneClickSelectionFilter(translate("filter.with.courses"),
					FILTER_WITH_COURSES, withCoursesPK, false);
			filters.add(withCoursesFilter);
		}
		
    	tableEl.setFilters(true, filters, true, false);
    }
    
    protected void setFilterOrganisations(List<Organisation> organisations) {
    	for(Organisation organisation:organisations) {
			organisationsKV.add(SelectionValues.entry(organisation.getKey().toString(),
					StringHelper.escapeHtml(organisation.getDisplayName())));
		}
    }
    
    protected void initFiltersPresets(UserRequest ureq, boolean withReservations, boolean withWithoutCourses) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.clear, List.of());
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		
		if(withWithoutCourses) {
			FlexiFiltersTab relevantTab = FlexiFiltersTabFactory.tabWithImplicitFilters(RELEVANT_TAB, translate("filter.relevant"),
					TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_WITH_COURSES,
							List.of(FILTER_WITH_COURSES))));
			tabs.add(relevantTab);
			
			FlexiFiltersTab withoutCoursesTab = FlexiFiltersTabFactory.tabWithImplicitFilters(WITHOUT_COURSES_TAB, translate("filter.without.courses"),
					TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_WITHOUT_COURSES,
							List.of(FILTER_WITHOUT_COURSES))));
			tabs.add(withoutCoursesTab);
		}

		if(withReservations) {
			FlexiFiltersTab confirmedByAdminTab = FlexiFiltersTabFactory.tabWithImplicitFilters(CONFIRMED_BY_ADMIN_TAB, translate("filter.to.confirm"),
					TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_TO_BE_CONFIRMED,
							List.of(CONFIRMED_BY_ADMIN))));
			tabs.add(confirmedByAdminTab);
		}
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
    }

    @Override
    protected void doDispose() {
        stackPanel.removeListener(this);
        super.doDispose();
    }

    @Override
    protected void formOK(UserRequest ureq) {
        //
    }

    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        if(batchContactButton == source) {
        	doContacts(ureq);
        } else if (tableEl == source) {
            if (event instanceof SelectionEvent se) {
                if ("select".equals(se.getCommand())) {
                	ParticipantStatisticsEntry selectedRow = tableModel.getObject(se.getIndex());
                    doSelect(ureq, selectedRow);
                } else if(IdentityOrganisationsCellRenderer.CMD_OTHER_ORGANISATIONS.equals(se.getCommand())) {
					String targetId = IdentityOrganisationsCellRenderer.getOtherOrganisationsId(se.getIndex());
                	ParticipantStatisticsEntry selectedRow = tableModel.getObject(se.getIndex());
					doShowOrganisations(ureq, targetId, selectedRow);
				} else if(IdentityChatCellRenderer.CMD_ONLINE_STATUS.equals(se.getCommand())) {
                	ParticipantStatisticsEntry selectedRow = tableModel.getObject(se.getIndex());
					doIm(ureq, selectedRow);
				} else if(ActionsCellRenderer.CMD_ACTIONS.equals(se.getCommand())) {
					String targetId = ActionsCellRenderer.getId(se.getIndex());
					ParticipantStatisticsEntry selectedRow = tableModel.getObject(se.getIndex());
					doOpenTools(ureq, selectedRow, targetId);
				}
            } else if (event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
            	tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
            	tableEl.reset(true, true, true);
            }
        }
        super.formInnerEvent(ureq, source, event);
    }

    protected final void loadModel() {
		List<ParticipantStatisticsEntry> statisticsList = loadStatistics();
		loadOnlineStatus(statisticsList);
		tableModel.setObjects(statisticsList);
		tableEl.reset(true, true, true);
    	tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
    }
    
    protected abstract List<ParticipantStatisticsEntry> loadStatistics();
 
    protected void loadOnlineStatus(List<ParticipantStatisticsEntry> statisticsList) {
    	if(!chatEnabled || statisticsList.isEmpty()) return;
    	
		Map<Long,ParticipantStatisticsEntry> keyToMemberMap = statisticsList.stream()
				.collect(Collectors.toMap(ParticipantStatisticsEntry::getIdentityKey, e -> e, (u, v) -> u));
		
		final Long me = getIdentity().getKey();
		List<Long> loadStatus = new ArrayList<>();
		for(ParticipantStatisticsEntry statistics:statisticsList) {
			Long identityKey = statistics.getIdentityKey();
			if(identityKey.equals(me)) {
				statistics.setOnlineStatus("me");
			} else if(sessionManager.isOnline(identityKey)) {
				loadStatus.add(identityKey);
			} else {
				statistics.setOnlineStatus(Presence.unavailable.name());
			}
		}
		
		Map<Long,String> statusMap = imService.getBuddyStatus(loadStatus);
		for(Map.Entry<Long,String> toLoad:statusMap.entrySet()) {
			String status = toLoad.getValue();
			ParticipantStatisticsEntry entry = keyToMemberMap.get(toLoad.getKey());
			if(status == null) {
				entry.setOnlineStatus(Presence.available.name());	
			} else {
				entry.setOnlineStatus(status);	
			}
		}
    }
    
    protected void loadOrganisationsFilterFromModel() {
    	Set<OrganisationWithParents> organisationsSet = new HashSet<>();
    	List<ParticipantStatisticsEntry> entries = tableModel.getObjects();
		for(ParticipantStatisticsEntry entry:entries) {
			if(entry.getOrganisations() != null) {
				organisationsSet.addAll(entry.getOrganisations());
			}
		}
		
		List<OrganisationWithParents> organisationsList = new ArrayList<>(organisationsSet);
		if(organisationsList.size() > 1) {
			Collections.sort(organisationsList, new OrganisationWithParentsNameComparator(getLocale()));
		}
    	for(OrganisationWithParents organisation:organisationsList) {
			organisationsKV.add(SelectionValues.entry(organisation.getKey().toString(),
					StringHelper.escapeHtml(organisation.getDisplayName())));
		}
    }

    private void reloadModel() {
        if (hasChanged) {
            loadModel();
            hasChanged = false;
        }
    }

    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == userOverviewCtrl) {
            if (event == Event.CHANGED_EVENT) {
                hasChanged = true;
            } else if ("next.student".equals(event.getCommand())) {
                nextStudent(ureq);
            } else if ("previous.student".equals(event.getCommand())) {
                previousStudent(ureq);
            }
        } else if(contactCtrl == source) {
        	cmc.deactivate();
        	cleanUp();
        } else if(cmc == source || calloutCtrl == source) {
        	cleanUp();
        }
        super.event(ureq, source, event);
    }

    @Override
    public void event(UserRequest ureq, Component source, Event event) {
        if (stackPanel == source) {
            if (event instanceof PopEvent pe && pe.getController() == userOverviewCtrl && hasChanged) {
            	reloadModel();
            }
        }
        super.event(ureq, source, event);
    }
    
    private void cleanUp() {
    	removeAsListenerAndDispose(calloutCtrl);
    	removeAsListenerAndDispose(contactCtrl);
    	removeAsListenerAndDispose(toolsCtrl);
    	removeAsListenerAndDispose(cmc);
    	calloutCtrl = null;
    	contactCtrl = null;
    	toolsCtrl = null;
    	cmc = null;
    }

    @Override
    public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
        if (entries == null || entries.isEmpty()) {
        	if(state instanceof SearchStateEntry search)
        	tableEl.quickSearch(ureq, search.getSearchString());
        	return;
        }

        ContextEntry ce = entries.get(0);
        OLATResourceable ores = ce.getOLATResourceable();
        if ("Identity".equals(ores.getResourceableTypeName())) {
            Long identityKey = ores.getResourceableId();
            for (ParticipantStatisticsEntry entry : tableModel.getObjects()) {
                if (identityKey.equals(entry.getIdentityKey())) {
                    removeAsListenerAndDispose(userOverviewCtrl);
                	Activateable2 overviewCtrl = doSelect(ureq, entry);
                    if (overviewCtrl != null) {
                    	overviewCtrl.activate(ureq, entries.subList(1, entries.size()), ce.getTransientState());
                    }
                    break;
                }
            }
        }
    }

    protected void previousStudent(UserRequest ureq) {
    	Object currentEntry = userOverviewCtrl.getUserObject();
        int previousIndex = tableModel.getObjects().indexOf(currentEntry) - 1;
        if (previousIndex < 0 || previousIndex >= tableModel.getRowCount()) {
            previousIndex = tableModel.getRowCount() - 1;
        }
        ParticipantStatisticsEntry previousEntry = tableModel.getObject(previousIndex);
        doSelect(ureq, previousEntry);
    }

    protected void nextStudent(UserRequest ureq) {
    	Object currentEntry = userOverviewCtrl.getUserObject();
        int nextIndex = tableModel.getObjects().indexOf(currentEntry) + 1;
        if (nextIndex < 0 || nextIndex >= tableModel.getRowCount()) {
            nextIndex = 0;
        }
        ParticipantStatisticsEntry nextEntry = tableModel.getObject(nextIndex);
        doSelect(ureq, nextEntry);
    }

    private Activateable2 doSelect(UserRequest ureq, ParticipantStatisticsEntry entry) {
    	if(userOverviewCtrl != null) {
    		removeAsListenerAndDispose(userOverviewCtrl);
    		stackPanel.popController(userOverviewCtrl);
    	}
    	
        userOverviewCtrl = createParticipantOverview(ureq, entry);
        listenTo(userOverviewCtrl);

        String displayName = userManager.getUserDisplayName(entry, userPropertyHandlers);
        stackPanel.pushController(displayName, userOverviewCtrl);
        return userOverviewCtrl;
    }
    
    protected abstract NextPreviousController createParticipantOverview(UserRequest ureq, ParticipantStatisticsEntry entry);
	
	/**
	 * Open private chat
	 * 
	 * @param ureq
	 * @param member
	 */
	private void doIm(UserRequest ureq, ParticipantStatisticsEntry entry) {
		Buddy buddy = imService.getBuddyById(entry.getIdentityKey());
		OpenInstantMessageEvent e = new OpenInstantMessageEvent(buddy);
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, InstantMessagingService.TOWER_EVENT_ORES);
	}
    
	protected void doShowOrganisations(UserRequest ureq, String elementId, ParticipantStatisticsEntry userRow) {
		List<OrganisationWithParents> organisations = userRow.getOrganisations();
		organisationsSmallListCtrl = new OrganisationsSmallListController(ureq, getWindowControl(), organisations);
		listenTo(organisationsSmallListCtrl);
		
		String title = translate("num.of.organisations", Integer.toString(organisations.size()));
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), organisationsSmallListCtrl.getInitialComponent(), elementId, title, true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doOpenTools(UserRequest ureq, ParticipantStatisticsEntry entry, String targetId) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), entry);
		listenTo(toolsCtrl);
	
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), targetId, "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doContacts(UserRequest ureq) {
		List<Long> entries = tableEl.getMultiSelectedIndex().stream()
			.map(index -> tableModel.getObject(index.intValue()))
			.filter(entry -> entry != null)
			.map(ParticipantStatisticsEntry::getIdentityKey)
			.toList();
		if(entries.isEmpty()) {
			showWarning("warning.atleastone.member");
		} else {
			List<Identity> identities = securityManager.loadIdentityByKeys(entries);
			ContactMessage cmsg = new ContactMessage(getIdentity());
			ContactList contactList = new ContactList("Contact");
			contactList.addAllIdentites(identities);
			cmsg.addEmailTo(contactList);
			doContact(ureq, cmsg);
		}
	}
	
	private void doContact(UserRequest ureq, ParticipantStatisticsEntry entry) {
		Identity identity = securityManager.loadIdentityByKey(entry.getIdentityKey());
		ContactMessage cmsg = new ContactMessage(getIdentity());
		String fullName = userManager.getUserDisplayName(identity);
		ContactList contactList = new ContactList(fullName);
		contactList.add(identity);
		cmsg.addEmailTo(contactList);
		doContact(ureq, cmsg);
	}

	private void doContact(UserRequest ureq, ContactMessage cmsg) {
		contactCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, cmsg);
		listenTo(contactCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), contactCtrl.getInitialComponent());
		cmc.activate();
		listenTo(cmc);
	}
	
	public interface NextPreviousController extends Controller, Activateable2 {
		
		public Object getUserObject();
		
	}
	
	private class ToolsController extends BasicController {
		
		private final Link contactLink;
		
		private final ParticipantStatisticsEntry entry;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, ParticipantStatisticsEntry entry) {
			super(ureq, wControl);
			this.entry = entry;
			
			VelocityContainer mainVC = createVelocityContainer("tool_participants");
			
			contactLink = LinkFactory.createLink("contact.link", "contact.link", getTranslator(), mainVC, this, Link.LINK);
			contactLink.setIconLeftCSS("o_icon o_icon-fw o_icon_mail");

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(contactLink == source) {
				doContact(ureq, entry);
			}
		}
	}
}
