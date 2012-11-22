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
package org.olat.catalog.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.catalog.CatalogEntry;
import org.olat.catalog.CatalogManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.AbstractRepositoryEntryListController;
import org.olat.repository.ui.RepositoryEntryDetails;

/**
 * 
 * Initial date: 21.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogEntryListController extends AbstractRepositoryEntryListController {


	private CloseableModalController cmc;
	private DialogBoxController dialogDeleteCtr;
	private CatalogMoveController catEntryMoveController;
	
	private LockResult catModificationLock;
	private List<CatalogEntry> catalogEntries;

	private final CatalogManager cm;
	
	public CatalogEntryListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, false);

		cm = CoreSpringFactory.getImpl(CatalogManager.class);
	}
	
	public void setCatalogEntries(List<CatalogEntry> entries) {
		this.catalogEntries = entries;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link) {
			Link link = (Link)source;
			if("del_cat_repo".equals(link.getCommand())) {
				doDelete(ureq, (Long)link.getUserObject());
			} else if("move_cat_repo".equals(link.getCommand())) {
				doMove(ureq, (Long)link.getUserObject());
			} else if("edit_cat_repo".equals(link.getCommand())) {
				doEdit(ureq, (Long)link.getUserObject());
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == dialogDeleteCtr) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				Long key = (Long)dialogDeleteCtr.getUserObject();
				deleteCatalogEntry(key);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cleanUp();
		} else if(source == catEntryMoveController) {
			if(event.equals(Event.DONE_EVENT)){
				CatalogEntry entry = catEntryMoveController.getMovedCatalogEntry();
				String name = entry.getRepositoryEntry() == null ? entry.getName() : entry.getRepositoryEntry().getDisplayname();
				showInfo("tools.move.catalog.entry.success", name);
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if(event.equals(Event.FAILED_EVENT)){
				showError("tools.move.catalog.entry.failed");
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		// in any case, remove the lock
		if (catModificationLock != null && catModificationLock.isSuccess()) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(catModificationLock);
			catModificationLock = null;
		}
		
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(dialogDeleteCtr);
		removeAsListenerAndDispose(catEntryMoveController);
		
		cmc = null;
		dialogDeleteCtr = null;
		catEntryMoveController = null;
	}

	@Override
	protected void addAdditionalLinks(RepositoryEntryDetails entry, VelocityContainer vc) {
		
		CatalogEntry currentEntry = null;
		for(CatalogEntry catalogEntry:catalogEntries) {
			if(catalogEntry.getRepositoryEntry() != null
					&& catalogEntry.getRepositoryEntry().getKey().equals(entry.getKey())) {
				currentEntry = catalogEntry;
				break;
			}
		}
		
		if(currentEntry != null) {
			Link delete = LinkFactory.createCustomLink("del_" + entry.getKey(), "del_cat_repo", "delete", Link.BUTTON, vc, this);
			delete.setUserObject(currentEntry.getKey());
			entry.addButtonName(delete.getComponentName());
			Link move = LinkFactory.createCustomLink("move_" + entry.getKey(), "move_cat_repo", "move", Link.BUTTON, vc, this);
			move.setUserObject(currentEntry.getKey());
			entry.addButtonName(move.getComponentName());
			Link edit = LinkFactory.createCustomLink("edit_" + entry.getKey(), "edit_cat_repo", "edit", Link.BUTTON, vc, this);
			edit.setUserObject(currentEntry.getKey());
			entry.addButtonName(edit.getComponentName());
		}
		
		super.addAdditionalLinks(entry, vc);

	}

	@Override
	public void loadModel() {
		if(catalogEntries == null || catalogEntries.isEmpty()) {
			processModel(Collections.<RepositoryEntry>emptyList());
		} else {
			List<RepositoryEntry> repoEntries = new ArrayList<RepositoryEntry>(catalogEntries.size());
			for(CatalogEntry catalogEntry:catalogEntries) {
				repoEntries.add(catalogEntry.getRepositoryEntry());
			}
			processModel(repoEntries);
		}
	}
	
	private void doEdit(UserRequest ureq, Long key) {
		
	}
	
	private void doDelete(UserRequest ureq, Long key) {
		CatalogEntry entryToDelete = cm.getCatalogEntryByKey(key);
		String[] trnslP = { entryToDelete.getName() };
		dialogDeleteCtr = activateYesNoDialog(ureq, null, translate("dialog.modal.leaf.delete.text", trnslP), dialogDeleteCtr);
		dialogDeleteCtr.setUserObject(key);
	}
	
	private void deleteCatalogEntry(Long key) {
		CatalogEntry entryToDelete = cm.getCatalogEntryByKey(key);
		cm.deleteCatalogEntry(entryToDelete);
	}
	
	private void doMove(UserRequest ureq, Long key) {
		CatalogEntry entryToMove = cm.getCatalogEntryByKey(key);

		boolean ajax = getWindowControl().getWindowBackOffice().getWindowManager().isAjaxEnabled();
		if (ajax) {
			catEntryMoveController= new CatalogAjaxMoveController(ureq, getWindowControl(), entryToMove);
		} else {
			catEntryMoveController= new CatalogEntryMoveController(getWindowControl(), ureq, entryToMove, getTranslator());
		}
		listenTo(catEntryMoveController);
		cmc = new CloseableModalController(getWindowControl(), "close", catEntryMoveController.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}
}
