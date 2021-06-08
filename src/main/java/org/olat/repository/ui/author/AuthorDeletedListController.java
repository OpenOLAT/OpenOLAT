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
import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.course.CourseModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams.OrderBy;
import org.olat.repository.ui.author.AuthoringEntryDataModel.Cols;

/**
 * 
 * Initial date: 1 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthorDeletedListController extends AuthorListController {

	private DToolsController dToolsCtrl;
	
	private FormLink restoreButton;
	private FormLink deletePermanentlyButton;
	
	private ConfirmRestoreController confirmRestoreCtrl;
	private ConfirmDeletePermanentlyController confirmDeletePermanentlyCtrl;
	
	public AuthorDeletedListController(UserRequest ureq, WindowControl wControl, String i18nName,
			SearchAuthorRepositoryEntryViewParams searchParams, boolean withSearch) {
		super(ureq, wControl, i18nName, searchParams, withSearch, false);
	}

	@Override
	protected void initTools(UserRequest ureq) {
		//
	}

	@Override
	protected void initActionsColumns(FlexiTableColumnModel columnsModel) {

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.deletedBy.i18nKey(), Cols.deletedBy.ordinal(),
				true, OrderBy.deletedBy.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.deletionDate.i18nKey(), Cols.deletionDate.ordinal(),
				true, OrderBy.deletionDate.name(), new DateFlexiCellRenderer(getLocale())));
		
		DefaultFlexiColumnModel detailsColumn = new DefaultFlexiColumnModel(Cols.detailsSupported.i18nKey(), Cols.detailsSupported.ordinal(), "details",
				new StaticFlexiCellRenderer("", "details", "o_icon o_icon-lg o_icon_details", translate("details")));
		detailsColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(detailsColumn);
		if(hasAuthorRight) {
			DefaultFlexiColumnModel toolsColumn = new DefaultFlexiColumnModel(Cols.tools.i18nKey(), Cols.tools.ordinal());
			toolsColumn.setExportable(false);
			columnsModel.addFlexiColumnModel(toolsColumn);
		}
	}
	

	@Override
	protected void initBatchButtons(FormItemContainer formLayout) {
		restoreButton = uifactory.addFormLink("tools.restore", formLayout, Link.BUTTON);
		tableEl.addBatchButton(restoreButton);

		if(hasAdministratorRight) {
			deletePermanentlyButton = uifactory.addFormLink("tools.delete.permanently", formLayout, Link.BUTTON);
			tableEl.addBatchButton(deletePermanentlyButton);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDeletePermanentlyCtrl == source) {
			cmc.deactivate();
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				reloadRows();
			}
			cleanUp();
		} else if(confirmRestoreCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				if(cmc != null) {
					cmc.deactivate();
				}
				reloadRows();
				cleanUp();
			} else if(event == Event.CANCELLED_EVENT) {
				if(cmc != null) {
					cmc.deactivate();
				}
				cleanUp();
			}
		} else if(dToolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				toolsCalloutCtrl.deactivate();
				cleanUp();
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void cleanUp() {
		removeAsListenerAndDispose(confirmDeletePermanentlyCtrl);
		removeAsListenerAndDispose(confirmRestoreCtrl);
		confirmDeletePermanentlyCtrl = null;
		confirmRestoreCtrl = null;
		super.cleanUp();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(restoreButton == source) {
			List<AuthoringEntryRow> rows = getMultiSelectedRows();
			if(!rows.isEmpty()) {
				doRestore(ureq, rows);
			} else {
				showWarning("bulk.update.nothing.selected");
			}
		} else if(deletePermanentlyButton == source) {
			List<AuthoringEntryRow> rows = getMultiSelectedRows();
			if(!rows.isEmpty()) {
				doDeletePermanently(ureq, rows);
			} else {
				showWarning("bulk.update.nothing.selected");
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("tools".equals(cmd)) {
				AuthoringEntryRow row = (AuthoringEntryRow)link.getUserObject();
				doOpenTools(ureq, row, link);
				return;// override the event
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpenTools(UserRequest ureq, AuthoringEntryRow row, FormLink link) {
		removeAsListenerAndDispose(dToolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		RepositoryEntry entry = repositoryService.loadByKey(row.getKey());
		if(entry == null) {
			tableEl.reloadData();
			showWarning("repositoryentry.not.existing");
		} else {
			dToolsCtrl = new DToolsController(ureq, getWindowControl(), row, entry);
			listenTo(dToolsCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					dToolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}

	private void doRestore(UserRequest ureq, List<AuthoringEntryRow> rows) {
		List<Long> deleteableRowKeys = new ArrayList<>(rows.size());
		for(AuthoringEntryRow row:rows) {
			boolean managed = RepositoryEntryManagedFlag.isManaged(row.getManagedFlags(), RepositoryEntryManagedFlag.delete);
			if(!managed && canManage(row)) {
				deleteableRowKeys.add(row.getKey());
			}
		}
		
		List<RepositoryEntry> entriesToRestore = repositoryManager.lookupRepositoryEntries(deleteableRowKeys);
		if(entriesToRestore.isEmpty()) {
			showWarning("bulk.update.nothing.applicable.selected");
		} else {
			removeAsListenerAndDispose(confirmRestoreCtrl);
			removeAsListenerAndDispose(cmc);
			
			confirmRestoreCtrl = new ConfirmRestoreController(ureq, getWindowControl(), entriesToRestore);
			listenTo(confirmRestoreCtrl);
			
			String title = translate("tools.restore");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmRestoreCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doDeletePermanently(UserRequest ureq, List<AuthoringEntryRow> rows) {
		List<Long> deleteableRowKeys = new ArrayList<>(rows.size());
		for(AuthoringEntryRow row:rows) {
			boolean managed = RepositoryEntryManagedFlag.isManaged(row.getManagedFlags(), RepositoryEntryManagedFlag.delete);
			if(!managed && repositoryService.hasRoleExpanded(getIdentity(), row, OrganisationRoles.learnresourcemanager.name(),
				OrganisationRoles.administrator.name())) {
				deleteableRowKeys.add(row.getKey());
			}
		}
		
		List<RepositoryEntry> entriesToDelete = repositoryManager.lookupRepositoryEntries(deleteableRowKeys);
		if(entriesToDelete.isEmpty()) {
			showWarning("bulk.update.nothing.applicable.selected");
		} else {
			removeAsListenerAndDispose(confirmDeletePermanentlyCtrl);
			removeAsListenerAndDispose(cmc);
			
			confirmDeletePermanentlyCtrl = new ConfirmDeletePermanentlyController(ureq, getWindowControl(), entriesToDelete, rows.size() != entriesToDelete.size());
			listenTo(confirmDeletePermanentlyCtrl);
			
			String title = translate("details.delete");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeletePermanentlyCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}

	private class DToolsController extends BasicController {

		private final VelocityContainer mainVC;
		
		private final boolean isOwner;
		private final boolean isAuthor;
		private final AuthoringEntryRow row;
		
		public DToolsController(UserRequest ureq, WindowControl wControl, AuthoringEntryRow row, RepositoryEntry entry) {
			super(ureq, wControl);
			setTranslator(AuthorDeletedListController.this.getTranslator());
			this.row = row;
			
			boolean isManager = repositoryService.hasRoleExpanded(getIdentity(), entry,
					OrganisationRoles.administrator.name(), OrganisationRoles.learnresourcemanager.name());
			isOwner = isManager || repositoryService.hasRole(getIdentity(), entry, GroupRoles.owner.name());
			isAuthor = isManager || repositoryService.hasRoleExpanded(getIdentity(), entry, OrganisationRoles.author.name());

			RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(entry);

			mainVC = createVelocityContainer("tools");
			List<String> links = new ArrayList<>();

			boolean copyManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.copy);
			boolean canCopy = (isAuthor || isOwner) && (entry.getCanCopy() || isOwner) && !copyManaged;
			
			boolean canDownload = entry.getCanDownload() && handler.supportsDownload();
			// disable download for courses if not author or owner
			if (entry.getOlatResource().getResourceableTypeName().equals(CourseModule.getCourseTypeName()) && !(isOwner || isAuthor)) {
				canDownload = false;
			}
			// always enable download for owners
			if (isOwner && handler.supportsDownload()) {
				canDownload = true;
			}
			
			if(canCopy || canDownload) {
				if (canCopy) {
					addLink("details.copy", "copy", "o_icon o_icon-fw o_icon_copy", links);
				}
				if(canDownload) {
					addLink("details.download", "download", "o_icon o_icon-fw o_icon_download", links);
				}
			}
			
			if(isOwner) {
				addLink("tools.restore", "restore", "o_icon o_icon-fw o_icon_restore", links);
			}
			
			if(isManager && !RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.delete)) {
				addLink("details.delete.permanently", "delete", "o_icon o_icon-fw o_icon_delete_item", links);
			}

			mainVC.contextPut("links", links);
			putInitialPanel(mainVC);
		}
		
		private void addLink(String name, String cmd, String iconCSS, List<String> links) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
			links.add(name);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(source instanceof Link) {
				Link link = (Link)source;
				String cmd = link.getCommand();
				if("copy".equals(cmd)) {
					doCopy(ureq, row);
				} else if("download".equals(cmd)) {
					doDownload(ureq, row);
				} else if("restore".equals(cmd)) {
					doRestore(ureq, Collections.singletonList(row));
				} else if("delete".equals(cmd)) {
					doDeletePermanently(ureq, Collections.singletonList(row));
				}
			}
		}

		@Override
		protected void doDispose() {
			//
		}
	}
}
