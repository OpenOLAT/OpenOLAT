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
package org.olat.repository.ui.author;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.archiver.ArchivesOverviewController;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OverviewAuthoringController extends BasicController implements Activateable2, GenericEventListener {

	private final BreadcrumbedStackedPanel stackPanel;
	
	private final AuthorListController authorListCtrl;
	private ArchivesOverviewController archivesOverviewCtrl;

	private boolean entriesDirty;
	private final boolean isGuestOnly;
	private final EventBus eventBus;
	
	@Autowired
	private CurriculumModule curriculumModule;

	public OverviewAuthoringController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		
		Roles roles = ureq.getUserSession().getRoles();
		isGuestOnly = roles.isGuestOnly();

		eventBus = ureq.getUserSession().getSingleUserEventCenter();
		eventBus.registerFor(this, getIdentity(), RepositoryService.REPOSITORY_EVENT_ORES);
		
		AuthorListConfiguration config = AuthorListConfiguration.allEnabled();
		config.setAllowedRuntimeTypes(allowedRuntimeTypes());
		SearchAuthorRepositoryEntryViewParams searchParams = new SearchAuthorRepositoryEntryViewParams(getIdentity(), roles);
		searchParams.setCanCopy(true);
		searchParams.setCanDownload(true);
		searchParams.setCanReference(true);
		authorListCtrl = new AuthorListController(ureq, wControl, searchParams, config);
		listenTo(authorListCtrl);
		
		stackPanel = new BreadcrumbedStackedPanel("authoring", getTranslator(), this);
		stackPanel.pushController(translate("author.title"), authorListCtrl);
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), RepositoryService.REPOSITORY_EVENT_ORES);
		putInitialPanel(stackPanel);
	}

	private List<RepositoryEntryRuntimeType> allowedRuntimeTypes() {
		List<RepositoryEntryRuntimeType> runtimeTypes = new ArrayList<>(List.of(RepositoryEntryRuntimeType.values()));
		if (!curriculumModule.isEnabled()) {
			runtimeTypes.remove(RepositoryEntryRuntimeType.curricular);
		}
		return runtimeTypes;
	}

	@Override
	protected void doDispose() {
		eventBus.deregisterFor(this, RepositoryService.REPOSITORY_EVENT_ORES);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, RepositoryService.REPOSITORY_EVENT_ORES);
        super.doDispose();
	}
	
	@Override
	public void event(Event event) {
		if(EntryChangedEvent.CHANGE_CMD.equals(event.getCommand()) && event instanceof EntryChangedEvent ece) {
			if(ece.getChange() == Change.modifiedAccess
					|| ece.getChange() == Change.modifiedAtPublish
					|| ece.getChange() == Change.modifiedDescription) {
				authorListCtrl.addDirtyRows(ece.getRepositoryEntryKey());
			} else if(ece.getChange() == Change.restored) {
				entriesDirty = true;
			}
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			if(!authorListCtrl.hasTab()) {
				if(isGuestOnly) {
					authorListCtrl.selectFilterTab(ureq, authorListCtrl.getMyTab());
				} else {
					authorListCtrl.selectFilterTab(ureq, authorListCtrl.getFavoritTab());
					if(authorListCtrl.isEmpty()) {
						authorListCtrl.selectFilterTab(ureq, authorListCtrl.getMyTab());
					}
				}
			}
			
			if(entriesDirty) {
				authorListCtrl.reloadRows();
			} else {
				authorListCtrl.reloadDirtyRows();
			}
		} else {
			authorListCtrl.activate(ureq, entries, state);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == authorListCtrl) {
			if(event instanceof AuthoringEvent ae && AuthoringEvent.COURSE_ARCHIVE_LIST.equals(ae.getCommand())) {
				doOpenCourseArchive(ureq);
			}
		}
	}
	
	private void doOpenCourseArchive(UserRequest ureq) {
		removeAsListenerAndDispose(archivesOverviewCtrl);
		archivesOverviewCtrl = null;
		
		archivesOverviewCtrl = new ArchivesOverviewController(ureq, getWindowControl());
		listenTo(archivesOverviewCtrl);
		stackPanel.pushController(translate("archives.title.crumb"), archivesOverviewCtrl);
	}

}