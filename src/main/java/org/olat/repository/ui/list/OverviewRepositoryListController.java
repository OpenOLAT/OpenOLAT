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
package org.olat.repository.ui.list;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.InfoPanel;
import org.olat.core.gui.components.panel.MainPanel;
import org.olat.core.gui.components.scope.Scope;
import org.olat.core.gui.components.scope.ScopeEvent;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.components.scope.ScopeSelection;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.coach.site.CoachSite;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.manager.InPreparationQueries;
import org.olat.repository.manager.RepositoryEntryMyImplementationsQueries;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.olat.repository.ui.list.RepositoryEntryListConfig.RepositoryEntryListPresets;
import org.olat.resource.accesscontrol.ACService;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OverviewRepositoryListController extends BasicController implements Activateable2, GenericEventListener {
	
	public static final List<CurriculumElementStatus> SCOPE_ELEMENT_STATUS = List.of(
			CurriculumElementStatus.provisional, CurriculumElementStatus.confirmed,
			CurriculumElementStatus.active, CurriculumElementStatus.cancelled,
			CurriculumElementStatus.finished);
	
	private static final String CMD_MY_COURSES = "MyCourses";
	private static final String CMD_IN_PREPARATION = "InPreparation";
	private static final String CMD_IMPLEMENTATION = "Implementation";
	private static final String CMD_IMPLEMENTATIONS_LIST = "Implementations";

	private final List<Scope> scopes;
	private final VelocityContainer mainVC;
	private ScopeSelection scopesSelection;
	private BreadcrumbedStackedPanel entriesStackPanel;
	private BreadcrumbedStackedPanel inPreparationStackPanel;
	private BreadcrumbedStackedPanel implementationsListStackPanel;
	
	private Controller currentCtrl;
	private RepositoryEntryListController entriesCtrl;
	private InPreparationListController inPreparationCtrl;
	private ImplementationsListController implementationsListCtrl;
	
	private boolean entriesDirty;
	private final boolean guestOnly;
	private final EventBus eventBus;
	private final boolean participantsOnly;
	private final List<GroupRoles> asRoles;
	
	@Autowired
	private ACService acService;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private InPreparationQueries inPreparationQueries;
	@Autowired
	private RepositoryEntryMyImplementationsQueries myImplementationsQueries;

	public OverviewRepositoryListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		
		guestOnly = ureq.getUserSession().getRoles().isGuestOnly();
		participantsOnly = repositoryModule.isMyCoursesParticipantsOnly();
		asRoles = participantsOnly
				? List.of(GroupRoles.participant)
				: List.of(GroupRoles.participant, GroupRoles.coach);

		MainPanel mainPanel = new MainPanel("myCoursesMainPanel");
		mainPanel.setDomReplaceable(false);
		mainPanel.setCssClass("o_sel_my_repository_entries");
		mainVC = createVelocityContainer("overview_scopes");
		mainPanel.setContent(mainVC);

		scopes = new ArrayList<>();
		loadScopes();
		if(showCoachingToolHint()) {
			loadCoachingToolHint(ureq);
		}

		eventBus = ureq.getUserSession().getSingleUserEventCenter();
		eventBus.registerFor(this, getIdentity(), RepositoryService.REPOSITORY_EVENT_ORES);
		
		putInitialPanel(mainPanel);
	}
	
	private void loadCoachingToolHint(UserRequest ureq) {
		InfoPanel panel = new InfoPanel("coachingToolHint");
		panel.setTitle(translate("coaching.tool.hint.title"));
		
		List<ContextEntry> ceList = BusinessControlFactory.getInstance().createCEListFromString("[CoachSite:0]");
		String coachingToolUrl = BusinessControlFactory.getInstance().getAsAuthURIString(ceList, true);
		
		StringBuilder informations = new StringBuilder();
		
		String hintI18nKey = repositoryModule.isMyCoursesParticipantsOnly()
				? "coaching.tool.hint.participants.only"
				: "coaching.tool.hint";
		
		informations.append(translate(hintI18nKey))
			.append(" <a href='").append(coachingToolUrl).append("'>")
			.append(translate("coaching.tool.hint.link")).append("</a>");
		
		panel.setInformations(informations.toString());
		panel.setPersistedStatusId(ureq, "my-courses-coaching-tool-hint-v1");
		mainVC.put("coachingToolHint", panel);
	}
		
	private boolean showCoachingToolHint() {
		if(repositoryModule.isMyCoursesCoachingToolHint()) {
			ChiefController ctrl = getWindowControl().getWindowBackOffice().getChiefController();
			return ctrl != null && ctrl.hasStaticSite(CoachSite.class)
					&& repositoryService.hasRoleExpanded(getIdentity(), GroupRoles.coach.name(), GroupRoles.owner.name());
		}
		return false;
	}
	
	private void loadScopes() {
		scopes.clear();
		scopes.add(ScopeFactory.createScope(CMD_MY_COURSES, translate("search.mycourses.student"),
				null, "o_icon o_icon-fw o_CourseModule_icon"));
		
		if(!guestOnly) {
			if(curriculumModule.isEnabled() && curriculumModule.isCurriculumInMyCourses()) {
				List<CurriculumElement> implementations = myImplementationsQueries.searchImplementations(getIdentity(), true, asRoles, SCOPE_ELEMENT_STATUS);
				for(CurriculumElement implementation:implementations) {
					String name = StringHelper.escapeHtml(implementation.getDisplayName());
					String hint = scopeDatesHint(implementation);
					scopes.add(ScopeFactory.createScope(CMD_IMPLEMENTATION + implementation.getKey().toString(),
						name, hint, "o_icon o_icon-fw o_icon_curriculum"));
				}
				if(!implementations.isEmpty() || myImplementationsQueries.hasImplementations(getIdentity(), participantsOnly)) {
					scopes.add(ScopeFactory.createScope(CMD_IMPLEMENTATIONS_LIST, translate("search.implementations.list"),
							null, "o_icon o_icon-fw o_icon_curriculum"));
				}
			}
			
			if(inPreparationQueries.hasInPreparation(getIdentity(), participantsOnly)) {
				scopes.add(ScopeFactory.createScope(CMD_IN_PREPARATION, translate("search.preparation"),
					null, "o_icon o_icon-fw o_ac_offer_pending_icon"));
			}
		}
		
		// Hold the selection
		String selectedKey = scopesSelection == null ? null : scopesSelection.getSelectedKey();
		scopesSelection = ScopeFactory.createScopeSelection("scopes", mainVC, this, scopes);
		scopesSelection.setVisible(scopes.size() > 1);
		if(selectedKey != null) {
			scopesSelection.setSelectedKey(selectedKey);
		}
	}

	private String scopeDatesHint(CurriculumElement implementation) {
		Formatter formatter = Formatter.getInstance(getLocale());
		String begin = formatter.formatDate(implementation.getBeginDate());
		String end = formatter.formatDate(implementation.getEndDate());
		
		String hint;
		if(begin != null && end != null) {
			hint = translate("search.implementations.dates", begin, end);
		} else if(begin != null) {
			hint = translate("search.implementations.begin", begin);
		} else if(end != null) {
			hint = translate("search.implementations.end", end);
		} else {
			hint = null;
		}
		return hint;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			if(currentCtrl == null) {
				activateMyEntries(ureq);
			} else {
				if(entriesCtrl != null) {
					entriesCtrl.reloadRows();
				}
				if(inPreparationCtrl != null) {
					inPreparationCtrl.reloadRows();
				}
				addToHistory(ureq, this);
			}
		} else {
			ContextEntry entry = entries.get(0);
			String scope = entry.getOLATResourceable().getResourceableTypeName();
			List<ContextEntry> subEntries = entries.subList(1, entries.size());

			if(CMD_IN_PREPARATION.equalsIgnoreCase(scope) && hasScope(CMD_IN_PREPARATION)) {
				doOpenInPreparation(ureq);
				scopesSelection.setSelectedKey(CMD_IN_PREPARATION);
			} else if("Implementation".equals(scope) || "CurriculumElement".equals(scope)) {
				activateCurriculumElement(ureq, entry.getOLATResourceable().getResourceableId());
			} else {
				RepositoryEntryListController listCtrl = doOpenEntries(ureq);
				scopesSelection.setSelectedKey(CMD_MY_COURSES);
				listCtrl.activate(ureq, subEntries, state);
			}
		}
	}
	
	private void activateCurriculumElement(UserRequest ureq, Long elementKey) {
		String scopeId = CMD_IMPLEMENTATION + elementKey;
		
		List<ContextEntry> entries;
		if(hasScope(scopeId)) {
			entries = BusinessControlFactory.getInstance()
					.createCEListFromString(OresHelper.createOLATResourceableInstance(CurriculumElement.class, elementKey));
			doOpenImplementationsList(ureq).activate(ureq, entries, null);
			scopesSelection.setSelectedKey(scopeId);
		} else {
			CurriculumElement element = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(elementKey));
			if (element == null) {
				return;
			}
			if(element.getParent() != null) {
				element = curriculumService.getImplementationOf(element);
			}
			entries = BusinessControlFactory.getInstance()
					.createCEListFromString(OresHelper.createOLATResourceableInstance(CurriculumElement.class, element.getKey()));
			if (CurriculumElementStatus.preparation == element.getElementStatus()) {
				doOpenInPreparation(ureq);
				scopesSelection.setSelectedKey(CMD_IN_PREPARATION);
			} else {
				doOpenImplementationsList(ureq).activate(ureq, entries, null);
				scopesSelection.setSelectedKey(CMD_IMPLEMENTATIONS_LIST);
			}
		}
	}
	
	private boolean hasScope(String id) {
		return scopes.stream()
				.anyMatch(scope -> id.equalsIgnoreCase(scope.getKey()));
	}
	
	private void activateMyEntries(UserRequest ureq) {
		RepositoryEntryListController listCtrl = doOpenEntries(ureq);
		scopesSelection.setSelectedKey(CMD_MY_COURSES);

		if(guestOnly) {
			listCtrl.selectFilterTab(ureq, listCtrl.getMyEntriesPreset());
		} else {
			listCtrl.selectFilterTab(ureq, listCtrl.getBookmarkPreset());
			if(listCtrl.isEmpty()) {
				listCtrl.selectFilterTab(ureq, listCtrl.getMyEntriesPreset());
			}
		}
	}

	@Override
	protected void doDispose() {
		eventBus.deregisterFor(this, RepositoryService.REPOSITORY_EVENT_ORES);
        super.doDispose();
	}
	
	@Override
	public void event(Event event) {
		if(EntryChangedEvent.CHANGE_CMD.equals(event.getCommand()) && event instanceof EntryChangedEvent) {
			EntryChangedEvent ece = (EntryChangedEvent)event;
			if(ece.getChange() == Change.addBookmark || ece.getChange() == Change.removeBookmark
					|| ece.getChange() == Change.added || ece.getChange() == Change.deleted) {
				entriesDirty = true;
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(implementationsListCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				loadScopes();
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == scopesSelection) {
			if(event instanceof ScopeEvent se) {
				if(CMD_MY_COURSES.equals(se.getSelectedKey())) {
					activateMyEntries(ureq);
				} else if(CMD_IN_PREPARATION.equals(se.getSelectedKey())) {
					doOpenInPreparation(ureq);
				} else if(CMD_IMPLEMENTATIONS_LIST.equals(se.getSelectedKey())) {
					doOpenImplementationsList(ureq);
					scopesSelection.setSelectedKey(se.getSelectedKey());// Reselect because with listen to pop
				} else if(se.getSelectedKey().startsWith(CMD_IMPLEMENTATION)) {
					Long implementationKey = Long.valueOf(se.getSelectedKey().replace(CMD_IMPLEMENTATION, ""));
					List<ContextEntry> entries = BusinessControlFactory.getInstance()
							.createCEListFromString(OresHelper.createOLATResourceableInstance(CurriculumElement.class, implementationKey));
					doOpenImplementationsList(ureq).activate(ureq, entries, null);
					scopesSelection.setSelectedKey(se.getSelectedKey());// Reselect because with listen to pop
				}
			}
		} else if(implementationsListStackPanel == source) {
			if(event instanceof PopEvent 
					&& implementationsListStackPanel.getLastController() == implementationsListCtrl) {
				scopesSelection.setSelectedKey(CMD_IMPLEMENTATIONS_LIST);
			}
		}
	}
	
	private RepositoryEntryListController doOpenEntries(UserRequest ureq) {
		if(entriesCtrl == null) {
			SearchMyRepositoryEntryViewParams searchParams
				= new SearchMyRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
			searchParams.setMembershipMandatory(true);
			searchParams.setParticipantsOnly(participantsOnly);
			searchParams.setEntryStatus(new RepositoryEntryStatusEnum[] {
					RepositoryEntryStatusEnum.review, RepositoryEntryStatusEnum.coachpublished, RepositoryEntryStatusEnum.published
				});
			searchParams.setOfferOrganisations(acService.getOfferOrganisations(searchParams.getIdentity()));
			searchParams.setOfferValidAt(new Date());
			searchParams.setRuntimeTypes(new RepositoryEntryRuntimeType[] { RepositoryEntryRuntimeType.curricular, RepositoryEntryRuntimeType.standalone });
			
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Courses", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			entriesStackPanel = new BreadcrumbedStackedPanel("mystack", getTranslator(), this);
			RepositoryEntryListConfig config = new RepositoryEntryListConfig(true, true, true,
					new RepositoryEntryListPresets(false, true, true, true, true, true));
			entriesCtrl = new RepositoryEntryListController(ureq, bwControl, searchParams, false, config, "my", entriesStackPanel);
			entriesStackPanel.pushController(translate("search.mycourses.student"), entriesCtrl);
			listenTo(entriesCtrl);
		} else if(entriesDirty) {
			entriesCtrl.reloadRows();
		}
		
		entriesDirty = false;
		currentCtrl = entriesCtrl;
		mainVC.put("component", entriesStackPanel);
		return entriesCtrl;
	}
	
	private ImplementationsListController doOpenImplementationsList(UserRequest ureq) {
		if(implementationsListCtrl == null) {
			implementationsListStackPanel = new BreadcrumbedStackedPanel("myliststack", getTranslator(), this);

			implementationsListCtrl = new ImplementationsListController(ureq, getWindowControl(), implementationsListStackPanel, asRoles);
			listenTo(implementationsListCtrl);
			implementationsListStackPanel.pushController(translate("search.implementations.list"), implementationsListCtrl);
		} else {
			implementationsListStackPanel.popUpToRootController(ureq);
		}
		
		currentCtrl = implementationsListCtrl;
		mainVC.put("component", implementationsListStackPanel);
		return implementationsListCtrl;
	}
	
	private InPreparationListController doOpenInPreparation(UserRequest ureq) {
		if(inPreparationCtrl == null) {
			SearchMyRepositoryEntryViewParams searchParams
				= new SearchMyRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
			searchParams.setMembershipMandatory(true);
			searchParams.setEntryStatus(new RepositoryEntryStatusEnum[] { RepositoryEntryStatusEnum.preparation });
			searchParams.setOfferOrganisations(acService.getOfferOrganisations(searchParams.getIdentity()));
			searchParams.setOfferValidAt(new Date());
			searchParams.setRuntimeTypes(new RepositoryEntryRuntimeType[] { RepositoryEntryRuntimeType.curricular, RepositoryEntryRuntimeType.standalone });
			
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("InPreparation", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			inPreparationStackPanel = new BreadcrumbedStackedPanel("mystack", getTranslator(), this);
			inPreparationCtrl = new InPreparationListController(ureq, bwControl, inPreparationStackPanel);
			inPreparationStackPanel.pushController(translate("search.preparation"), inPreparationCtrl);
			listenTo(inPreparationCtrl);
		} else if(entriesDirty) {
			inPreparationCtrl.reloadRows();
		}
		
		entriesDirty = false;
		currentCtrl = inPreparationCtrl;
		mainVC.put("component", inPreparationStackPanel);
		return inPreparationCtrl;
	}
}
