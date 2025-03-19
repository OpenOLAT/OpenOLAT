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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.MainPanel;
import org.olat.core.gui.components.scope.Scope;
import org.olat.core.gui.components.scope.ScopeEvent;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.components.scope.ScopeSelection;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
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
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumSecurityCallbackFactory;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.ui.CurriculumElementListController;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
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

	private static final String CMD_MY_COURSES = "MyCourses";
	private static final String CMD_IN_PREPARATION = "InPreparation";
	private static final String CMD_IMPLEMENTATION = "Implementation";

	private final List<Scope> scopes;
	private final VelocityContainer mainVC;
	private final ScopeSelection scopesSelection;
	private BreadcrumbedStackedPanel entriesStackPanel;
	private BreadcrumbedStackedPanel inPreparationStackPanel;
	private BreadcrumbedStackedPanel implementationStackPanel;
	
	private Controller currentCtrl;
	private RepositoryEntryListController entriesCtrl;
	private InPreparationListController inPreparationCtrl;
	private CurriculumElementListController elementListCtrl;
	
	private boolean entriesDirty;
	private final boolean guestOnly;
	
	private final EventBus eventBus;
	
	@Autowired
	private ACService acService;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private InPreparationQueries inPreparationQueries;
	@Autowired
	private RepositoryEntryMyImplementationsQueries myImplementationsQueries;

	public OverviewRepositoryListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		
		guestOnly = ureq.getUserSession().getRoles().isGuestOnly();

		MainPanel mainPanel = new MainPanel("myCoursesMainPanel");
		mainPanel.setDomReplaceable(false);
		mainPanel.setCssClass("o_sel_my_repository_entries");
		mainVC = createVelocityContainer("overview_scopes");
		mainPanel.setContent(mainVC);

		scopes = new ArrayList<>();
		scopes.add(ScopeFactory.createScope(CMD_MY_COURSES, translate("search.mycourses.student"),
				null, "o_icon o_icon-fw o_CourseModule_icon"));
		
		if(!guestOnly && curriculumModule.isEnabled() && curriculumModule.isCurriculumInMyCourses()) {
			List<CurriculumElement> implementations = myImplementationsQueries.searchImplementations(getIdentity());
			for(CurriculumElement implementation:implementations) {
				String name = StringHelper.escapeHtml(implementation.getDisplayName());
				scopes.add(ScopeFactory.createScope(CMD_IMPLEMENTATION + implementation.getKey().toString(),
					name, null, "o_icon o_icon-fw o_icon_curriculum"));
			}
		}
		
		if(!guestOnly && inPreparationQueries.hasInPreparation(getIdentity())) {
			scopes.add(ScopeFactory.createScope(CMD_IN_PREPARATION, translate("search.preparation"),
				null, "o_icon o_icon-fw o_ac_offer_pending_icon"));
		}
		
		scopesSelection = ScopeFactory.createScopeSelection("scopes", mainVC, this, scopes);
		scopesSelection.setVisible(scopes.size() > 1);

		eventBus = ureq.getUserSession().getSingleUserEventCenter();
		eventBus.registerFor(this, getIdentity(), RepositoryService.REPOSITORY_EVENT_ORES);
		
		putInitialPanel(mainPanel);
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
			Long key = entry.getOLATResourceable().getResourceableId();
			List<ContextEntry> subEntries = entries.subList(1, entries.size());

			if(CMD_IN_PREPARATION.equalsIgnoreCase(scope) && hasScope(CMD_IN_PREPARATION)) {
				doOpenInPreparation(ureq);
				scopesSelection.setSelectedKey(CMD_IN_PREPARATION);
			} else if(CMD_IMPLEMENTATION.equalsIgnoreCase(scope) && hasImplementationScope(key)) {
				doOpenImplementation(ureq, entry.getOLATResourceable().getResourceableId());
				scopesSelection.setSelectedKey(CMD_IMPLEMENTATION + key.toString());
			} else {
				RepositoryEntryListController listCtrl = doOpenEntries(ureq);
				scopesSelection.setSelectedKey(CMD_MY_COURSES);
				listCtrl.activate(ureq, subEntries, state);
			}
		}
	}
	
	private boolean hasImplementationScope(Long key) {
		final String id = CMD_IMPLEMENTATION + key.toString();
		return scopes.stream()
				.anyMatch(scope -> id.equalsIgnoreCase(scope.getKey()));
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
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == scopesSelection) {
			if(event instanceof ScopeEvent se) {
				if(CMD_MY_COURSES.equals(se.getSelectedKey())) {
					activateMyEntries(ureq);
				} else if(CMD_IN_PREPARATION.equals(se.getSelectedKey())) {
					doOpenInPreparation(ureq);
				} else if(se.getSelectedKey().startsWith(CMD_IMPLEMENTATION)) {
					Long implementationKey = Long.valueOf(se.getSelectedKey().replace(CMD_IMPLEMENTATION, ""));
					doOpenImplementation(ureq, implementationKey);
				}
				
			}
		}
	}
	
	private RepositoryEntryListController doOpenEntries(UserRequest ureq) {
		if(entriesCtrl == null) {
			SearchMyRepositoryEntryViewParams searchParams
				= new SearchMyRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
			searchParams.setMembershipMandatory(true);
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
	
	private CurriculumElementListController doOpenImplementation(UserRequest ureq, Long implementationKey) {
		implementationStackPanel = new BreadcrumbedStackedPanel("mystack", getTranslator(), this);
		
		CurriculumElement element = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(implementationKey));
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Implementation", element.getKey());
		WindowControl swControl = addToHistory(ureq, ores, null);
		CurriculumSecurityCallback secCallback = CurriculumSecurityCallbackFactory.createDefaultCallback();
		elementListCtrl = new CurriculumElementListController(ureq, swControl, implementationStackPanel,
				getIdentity(), element.getCurriculum(), element, secCallback);
		listenTo(elementListCtrl);
		implementationStackPanel.pushController(element.getDisplayName(), elementListCtrl);
		
		entriesDirty = false;
		currentCtrl = elementListCtrl;
		mainVC.put("component", implementationStackPanel);
		return elementListCtrl;
	}
}
