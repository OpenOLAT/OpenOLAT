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
package org.olat.core.commons.modules.bc.version;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.modules.bc.commands.FolderCommand;
import org.olat.core.commons.modules.bc.commands.FolderCommandStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.BaseTableDataModelWithoutFilter;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSRevisionMediaResource;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.version.VFSRevision;
import org.olat.core.util.vfs.version.Versions;
import org.olat.core.util.vfs.version.VersionsManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * This controller shows the list of deleted files in a container.<br>
 * Events:
 * <ul>
 * <li>FOLDERCOMMAND_FINISHED</li>
 * </ul>
 * <P>
 * Initial Date: 15 sept. 2009 <br>
 * 
 * @author srosse
 */
public class DeletedFileListController extends BasicController {

	private static final String CMD_DOWNLOAD = "download";
	private static final String CMD_RESTORE = "restore";
	private static final String CMD_DELETE = "delete";
	private static final String CMD_CANCEL = "cancel";

	private int status = FolderCommandStatus.STATUS_SUCCESS;

	private final VFSContainer container;
	private final List<Versions> deletedFiles;

	private VelocityContainer mainVC;
	private TableController deletedFilesListTableCtr;
	private DialogBoxController dialogCtr;

	private boolean isAdmin;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private VersionsManager versionsManager;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public DeletedFileListController(UserRequest ureq, WindowControl wControl, VFSContainer container) {
		super(ureq, wControl);
		this.container = container;
		deletedFiles = versionsManager.getDeletedFiles(container);
		
		isAdmin = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());

		TableGuiConfiguration summaryTableConfig = new TableGuiConfiguration();
		summaryTableConfig.setDownloadOffered(false);
		summaryTableConfig.setTableEmptyMessage(getTranslator().translate("version.noDeletedFiles"));

		deletedFilesListTableCtr = new TableController(summaryTableConfig, ureq, getWindowControl(), getTranslator());

		deletedFilesListTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("version.name", 0, null, ureq.getLocale()));
		deletedFilesListTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("version.author", 1, null, ureq.getLocale()));
		deletedFilesListTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("version.deletedBy", 2, null, ureq.getLocale()));
		deletedFilesListTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("version.date", 3, null, ureq.getLocale()));
		deletedFilesListTableCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_DOWNLOAD, "version.download", getTranslator().translate(
				"version.download")));

		VFSSecurityCallback secCallback = VFSManager.findInheritedSecurityCallback(container);
		if (secCallback != null) {
			if (secCallback.canDeleteRevisionsPermanently()) {
				deletedFilesListTableCtr.addMultiSelectAction("delete", CMD_DELETE);
			}
			if (secCallback.canWrite()) {
				deletedFilesListTableCtr.addMultiSelectAction("version.restore", CMD_RESTORE);
			}
		}

		deletedFilesListTableCtr.addMultiSelectAction("cancel", CMD_CANCEL);
		deletedFilesListTableCtr.setMultiSelect(true);
		loadModel(ureq);
		listenTo(deletedFilesListTableCtr);

		mainVC = createVelocityContainer("deleted_files");
		mainVC.put("deletedFileList", deletedFilesListTableCtr.getInitialComponent());
		putInitialPanel(mainVC);
	}
	
	private void loadModel(UserRequest ureq) {
		Collection<String> names = new HashSet<>();
		for(Versions deletedFile:deletedFiles) {
			if(deletedFile.getCreator() != null && !"-".equals(deletedFile.getCreator())) {
				names.add(deletedFile.getCreator());
			}
			VFSRevision lastRevision = getLastRevision(deletedFile);
			if(lastRevision != null && lastRevision.getAuthor() != null && !"-".equals(lastRevision.getAuthor())) {
				names.add(lastRevision.getAuthor());
			}
		}
		
		Map<String, String> mappedFullnames = userManager.getUserDisplayNamesByUserName(names);
		DeletedFileListDataModel model = new DeletedFileListDataModel(deletedFiles, mappedFullnames, ureq.getLocale());
		deletedFilesListTableCtr.setTableDataModel(model);
	}

	@Override
	protected void doDispose() {
	//
	}

	public int getStatus() {
		return status;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	// nothing to track
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == deletedFilesListTableCtr) {
			if (event instanceof TableEvent) {
				TableEvent tEvent = (TableEvent) event;
				int row = tEvent.getRowId();
				if (CMD_DOWNLOAD.equals(tEvent.getActionId())) {
					VFSRevision version = getLastRevision(deletedFiles.get(row));
					MediaResource resource = new VFSRevisionMediaResource(version, true);
					ureq.getDispatchResult().setResultingMediaResource(resource);
				} else if (CMD_RESTORE.equals(tEvent.getActionId())) {
					VFSRevision version = getLastRevision(deletedFiles.get(row));
					if (versionsManager.restore(container, version)) {
						status = FolderCommandStatus.STATUS_SUCCESS;
						fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
					} else {
						status = FolderCommandStatus.STATUS_FAILED;
						// ERROR
					}
				}
			} else if (event instanceof TableMultiSelectEvent) {
				TableMultiSelectEvent tEvent = (TableMultiSelectEvent) event;
				if (CMD_CANCEL.equals(tEvent.getAction())) {
					status = FolderCommandStatus.STATUS_CANCELED;
					fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
				} else if (CMD_RESTORE.equals(tEvent.getAction())) {
					List<VFSRevision> selectedRevisions = getSelectedRevisions(tEvent.getSelection());
					boolean allOk = true;
					for (VFSRevision revision : selectedRevisions) {
						allOk &= versionsManager.restore(container, revision);
					}
					if (allOk) {
						status = FolderCommandStatus.STATUS_SUCCESS;
						fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
					} else {
						status = FolderCommandStatus.STATUS_FAILED;
					}
				} else if (CMD_DELETE.equals(tEvent.getAction())) {
					List<Versions> versionsToDelete = getSelectedVersions(tEvent.getSelection());
					if (!versionsToDelete.isEmpty()) {
						
						String msg = translate("version.del.confirm") + "<p>" + renderVersionsAsHtml(versionsToDelete) + "</p>";
						// create dialog controller
						dialogCtr = activateYesNoDialog(ureq, translate("version.del.header"), msg, dialogCtr);
						dialogCtr.setUserObject(versionsToDelete);
					}
				}
			}
		} else if (source == dialogCtr) {
			if (DialogBoxUIFactory.isYesEvent(event)) {	
				@SuppressWarnings("unchecked")
				List<Versions> versionsToDelete =  (List<Versions>)dialogCtr.getUserObject();
				versionsManager.deleteVersions(container, versionsToDelete);
				status = FolderCommandStatus.STATUS_SUCCESS;
				fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
			}
		}
	}
	
	private String renderVersionsAsHtml(List<Versions> versions) {
		StringBuilder sb = new StringBuilder();
		sb.append("<ul>");
		for (Versions version:versions) {
			VFSRevision lastRevision = getLastRevision(version);
			if (lastRevision != null) {
				sb.append("<li>")
					.append(lastRevision.getName())
					.append("</li>");
			}
		}
		sb.append("</ul>");
		return sb.toString();
	}
	
	protected List<Versions> getSelectedVersions(BitSet objectMarkers) {
		List<Versions> results = new ArrayList<>();
		for (int i = objectMarkers.nextSetBit(0); i >= 0; i = objectMarkers.nextSetBit(i + 1)) {
			if (i >= 0 && i < deletedFiles.size()) {
				results.add(deletedFiles.get(i));
			}
		}
		return results;
	}

	protected List<VFSRevision> getSelectedRevisions(BitSet objectMarkers) {
		List<VFSRevision> results = new ArrayList<>();
		for (int i = objectMarkers.nextSetBit(0); i >= 0; i = objectMarkers.nextSetBit(i + 1)) {
			if (i >= 0 && i < deletedFiles.size()) {
				VFSRevision elem = getLastRevision(deletedFiles.get(i));
				if (elem != null) {
					results.add(elem);
				}
			}
		}
		return results;
	}

	protected VFSRevision getLastRevision(Versions versions) {
		VFSRevision lastRevision = null;
		if (!versions.getRevisions().isEmpty()) {
			lastRevision = versions.getRevisions().get(versions.getRevisions().size() - 1);
		}
		return lastRevision;
	}

	public class DeletedFileListDataModel extends BaseTableDataModelWithoutFilter<Versions> implements TableDataModel<Versions> {
		private final DateFormat format;
		private final List<Versions> versionList;
		private final Calendar cal = Calendar.getInstance();
		private final Map<String, String> mappedFullnames;

		public DeletedFileListDataModel(List<Versions> versionList, Map<String, String> mappedFullnames, Locale locale) {
			this.versionList = versionList;
			this.mappedFullnames = mappedFullnames;
			format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
		}

		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			return versionList.size();
		}

		public Object getValueAt(int row, int col) {
			Versions versioned = versionList.get(row);
			switch (col) {
				case 0: {
					VFSRevision lastRevision = getLastRevision(versioned);
					return lastRevision == null ? null : lastRevision.getName();
				}
				case 1:
					return getFullName(versioned.getCreator());
				case 2: {
					VFSRevision lastRevision = getLastRevision(versioned);
					return lastRevision == null ? null : getFullName(lastRevision.getAuthor());
				}
				case 3: {
					VFSRevision lastRevision = getLastRevision(versioned);
					if (lastRevision == null) return null;
					cal.setTimeInMillis(lastRevision.getLastModified());
					return format.format(cal.getTime());
				}
				default:
					return "";
			}
		}
		
		private String getFullName(String name) {
			if(!StringHelper.containsNonWhitespace(name)) {
				return null;
			}
			String fullName = mappedFullnames.get(name);
			if(fullName == null) {
				return null;
			}
			
			StringBuilder sb = new StringBuilder();
			sb.append(fullName);
			if(isAdmin) {
				sb.append(" (").append(name).append(")");
			}
			return sb.toString();
		}
	}
}
