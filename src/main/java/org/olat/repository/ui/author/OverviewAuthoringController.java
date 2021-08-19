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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
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
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;

/**
 * 
 * Initial date: 28.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OverviewAuthoringController extends BasicController implements Activateable2, GenericEventListener {

	private final AuthorListController authorListCtrl;

	private boolean entriesDirty;
	private final boolean isGuestOnly;
	private final EventBus eventBus;
	
	public OverviewAuthoringController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		
		Roles roles = ureq.getUserSession().getRoles();
		isGuestOnly = roles.isGuestOnly();

		eventBus = ureq.getUserSession().getSingleUserEventCenter();
		eventBus.registerFor(this, getIdentity(), RepositoryService.REPOSITORY_EVENT_ORES);
		
		authorListCtrl = new AuthorListController(ureq, wControl);
		listenTo(authorListCtrl);
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), RepositoryService.REPOSITORY_EVENT_ORES);
		putInitialPanel(authorListCtrl.getStackPanel());
	}
	
	@Override
	protected void doDispose() {
		eventBus.deregisterFor(this, RepositoryService.REPOSITORY_EVENT_ORES);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, RepositoryService.REPOSITORY_EVENT_ORES);
	}
	
	@Override
	public void event(Event event) {
		if(EntryChangedEvent.CHANGE_CMD.equals(event.getCommand()) && event instanceof EntryChangedEvent) {
			EntryChangedEvent ece = (EntryChangedEvent)event;
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
			if(isGuestOnly) {
				authorListCtrl.selectFilterTab(authorListCtrl.getMyTab());
			} else {
				authorListCtrl.selectFilterTab(authorListCtrl.getFavoritTab());
				if(authorListCtrl.isEmpty()) {
					authorListCtrl.selectFilterTab(authorListCtrl.getMyTab());
				}
			}
			
			if(entriesDirty) {
				authorListCtrl.reloadRows();
			} else {
				authorListCtrl.reloadDirtyRows();
			}
		} else {
			ContextEntry entry = entries.get(0);
			String segment = entry.getOLATResourceable().getResourceableTypeName();
			if("Favorits".equals(segment) && authorListCtrl.getFavoritTab() != null) {
				if(isGuestOnly) {
					authorListCtrl.selectFilterTab(authorListCtrl.getMyTab());
				} else {
					authorListCtrl.selectFilterTab(authorListCtrl.getFavoritTab());
				}
			} else if("My".equals(segment)) {
				authorListCtrl.selectFilterTab(authorListCtrl.getMyTab());
			} else if("Search".equals(segment) && authorListCtrl.getSearchTab() != null) {
				authorListCtrl.selectFilterTab(authorListCtrl.getSearchTab());
			} else if("Deleted".equals(segment) && authorListCtrl.getDeletedTab() != null) {
				authorListCtrl.selectFilterTab(authorListCtrl.getDeletedTab());
			} else {
				authorListCtrl.selectFilterTab(authorListCtrl.getMyTab());
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}