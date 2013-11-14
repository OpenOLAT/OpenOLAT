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

import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.IdentityShort;
import org.olat.core.CoreSpringFactory;
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
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.VFSRevisionMediaResource;
import org.olat.core.util.vfs.version.VFSRevision;
import org.olat.core.util.vfs.version.Versionable;
import org.olat.core.util.vfs.version.Versions;
import org.olat.user.UserManager;

/**
 * 
 * Description:<br>
 * This controller shows the list of revisions from a versioned file.<br>
 * Events:
 * <ul>
 * <li>FOLDERCOMMAND_FINISHED</li>
 * </ul>
 * <P>
 * Initial Date: 15 sept. 2009 <br>
 * 
 * @author srosse
 */
public class RevisionListController extends BasicController {

	private static final String CMD_DOWNLOAD = "download";
	private static final String CMD_RESTORE = "restore";
	private static final String CMD_DELETE = "delete";
	private static final String CMD_CANCEL = "cancel";

	private int status = FolderCommandStatus.STATUS_SUCCESS;

	private final Versionable versionedFile;
	private TableController revisionListTableCtr;
	private DialogBoxController confirmDeleteBoxCtr;
	private final VelocityContainer mainVC;
	private final boolean isAdmin;
	private final UserManager userManager;

	public RevisionListController(UserRequest ureq, WindowControl wControl, Versionable versionedFile, boolean readOnly) {
		this(ureq, wControl, versionedFile, null, null, readOnly);
	}

	public RevisionListController(UserRequest ureq, WindowControl wControl, Versionable versionedFile,
			String title, String description, boolean readOnly) {
		super(ureq, wControl);
		isAdmin = CoreSpringFactory.getImpl(BaseSecurityModule.class)
				.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		
		//reload the file with all possible precautions
		VFSLeaf versionedLeaf = null;
		if(versionedFile instanceof VFSLeaf) {
			versionedLeaf = (VFSLeaf)versionedFile;
		}
		if(versionedLeaf != null && versionedLeaf.getParentContainer() != null) {
			//reload the file
			versionedLeaf = (VFSLeaf)versionedLeaf.getParentContainer().resolve(((VFSLeaf) versionedFile).getName());
			if(versionedLeaf instanceof Versionable) {
				versionedFile = (Versionable)versionedLeaf;
			}
		}
		this.versionedFile = versionedFile;

		TableGuiConfiguration summaryTableConfig = new TableGuiConfiguration();
		summaryTableConfig.setDownloadOffered(true);
		summaryTableConfig.setTableEmptyMessage(getTranslator().translate("version.noRevisions"));

		revisionListTableCtr = new TableController(summaryTableConfig, ureq, getWindowControl(), getTranslator());
		revisionListTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("version.nr", 0, null, ureq.getLocale()) {
			@Override
			public int compareTo(int rowa, int rowb) {
				Object a = table.getTableDataModel().getValueAt(rowa, dataColumn);
				Object b = table.getTableDataModel().getValueAt(rowb, dataColumn);
				if (a == null || b == null) {
					boolean bb = (b == null);
					return (a == null) ? (bb ? 0: -1) : (bb ? 1: 0);
				}
				try {
					Long la = new Long((String)a);
					Long lb = new Long((String)b);
					return la.compareTo(lb);
				} catch (NumberFormatException e) {
					return super.compareTo(rowa, rowb);
				}
			}
		});
		revisionListTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("version.author", 1, null, ureq.getLocale()));
		revisionListTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("version.comment", 2, null, ureq.getLocale()));
		revisionListTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("version.date", 3, null, ureq.getLocale()));
		revisionListTableCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_DOWNLOAD, "version.download", getTranslator().translate(
				"version.download")));
		//read only cannot restore / delete
		if(!readOnly) {
			revisionListTableCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_RESTORE, "version.restore", getTranslator().translate(
					"version.restore")));
			revisionListTableCtr.addMultiSelectAction("delete", CMD_DELETE);
		}
		
		revisionListTableCtr.addMultiSelectAction("cancel", CMD_CANCEL);
		revisionListTableCtr.setMultiSelect(true);
		loadModel(versionedLeaf);
		listenTo(revisionListTableCtr);

		mainVC = createVelocityContainer("revisions");
		mainVC.put("revisionList", revisionListTableCtr.getInitialComponent());

		if (StringHelper.containsNonWhitespace(title)) {
			mainVC.contextPut("title", title);
		}
		if (StringHelper.containsNonWhitespace(description)) {
			mainVC.contextPut("description", description);
		}

		putInitialPanel(mainVC);
	}
	
	private void loadModel(VFSLeaf versionedLeaf) {
		Versions versions = versionedFile.getVersions();
		List<VFSRevision> revisions = new ArrayList<VFSRevision>(versions.getRevisions());
		revisions.add(new CurrentRevision(versionedLeaf, versions));
		
		Collection<String> names = new HashSet<String>();
		for(VFSRevision revision:revisions) {
			if(revision.getAuthor() != null) {
				names.add(revision.getAuthor());
			}
		}
		
		Map<String, IdentityShort> mappedIdentities = new HashMap<String, IdentityShort>();
		for(IdentityShort identity :BaseSecurityManager.getInstance().findShortIdentitiesByName(names)) {
			mappedIdentities.put(identity.getName(), identity);
		}

		revisionListTableCtr.setTableDataModel(new RevisionListDataModel(revisions, mappedIdentities, getLocale()));
	}

	@Override
	protected void doDispose() {
	// disposed by BasicController
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
		if (source == revisionListTableCtr) {
			if (event instanceof TableEvent) {
				TableEvent tEvent = (TableEvent) event;
				int row = tEvent.getRowId();
				if (CMD_DOWNLOAD.equals(tEvent.getActionId())) {
					
					MediaResource resource;
					if(row < versionedFile.getVersions().getRevisions().size()) {
						//restore current, do nothing
						VFSRevision version = versionedFile.getVersions().getRevisions().get(row);
						resource = new VFSRevisionMediaResource(version, true);
					} else {
						resource = new VFSMediaResource((VFSLeaf)versionedFile);
						((VFSMediaResource)resource).setDownloadable(true);
					}
					ureq.getDispatchResult().setResultingMediaResource(resource);
				} else if (CMD_RESTORE.equals(tEvent.getActionId())) {
					if(row >= versionedFile.getVersions().getRevisions().size()) {
						//restore current, do nothing
						status = FolderCommandStatus.STATUS_SUCCESS;
						fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
					} else {
						VFSRevision version = versionedFile.getVersions().getRevisions().get(row);
						String comment = translate("version.restore.comment", new String[]{version.getRevisionNr()});
						if (versionedFile.getVersions().restore(ureq.getIdentity(), version, comment)) {
							status = FolderCommandStatus.STATUS_SUCCESS;
							fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
						} else {
							status = FolderCommandStatus.STATUS_FAILED;
							showError("version.restore.failed");
							fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
						}
					}
				}
			} else if (event instanceof TableMultiSelectEvent) {
				TableMultiSelectEvent tEvent = (TableMultiSelectEvent) event;
				if (CMD_CANCEL.equals(tEvent.getAction())) {
					status = FolderCommandStatus.STATUS_CANCELED;
					fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
				} else {
					List<VFSRevision> selectedVersions = getSelectedRevisions(tEvent.getSelection());
					if (!selectedVersions.isEmpty()) {
						if (CMD_DELETE.equals(tEvent.getAction())) {
							String numOfVersionToDelete = Integer.toString(selectedVersions.size());
							confirmDeleteBoxCtr = activateYesNoDialog(ureq, null, translate("version.confirmDelete",
									new String[] { numOfVersionToDelete }), confirmDeleteBoxCtr);
							confirmDeleteBoxCtr.setUserObject(selectedVersions);
						}
					}
				}
			}
		} else if (source == confirmDeleteBoxCtr) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				@SuppressWarnings("unchecked")
				List<VFSRevision> selectedVersions = (List<VFSRevision>) confirmDeleteBoxCtr.getUserObject();
				versionedFile.getVersions().delete(ureq.getIdentity(), selectedVersions);
				status = FolderCommandStatus.STATUS_SUCCESS;
			} else {
				status = FolderCommandStatus.STATUS_CANCELED;
			}
			fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
		}
	}

	private List<VFSRevision> getSelectedRevisions(BitSet objectMarkers) {
		List<VFSRevision> allVersions = versionedFile.getVersions().getRevisions();

		List<VFSRevision> results = new ArrayList<VFSRevision>();
		for (int i = objectMarkers.nextSetBit(0); i >= 0; i = objectMarkers.nextSetBit(i + 1)) {
			if (i >= 0 && i < allVersions.size()) {
				VFSRevision elem = allVersions.get(i);
				results.add(elem);
			}
		}

		return results;
	}

	public class RevisionListDataModel extends BaseTableDataModelWithoutFilter<VFSRevision> implements TableDataModel<VFSRevision> {
		private final DateFormat format;
		private final List<VFSRevision> versionList;
		private final Calendar cal = Calendar.getInstance();
		private final Map<String, IdentityShort> mappedIdentities;

		public RevisionListDataModel(List<VFSRevision> versionList, Map<String, IdentityShort> mappedIdentities, Locale locale) {
			this.versionList = versionList;
			this.mappedIdentities = mappedIdentities;
			format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
		}

		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			return versionList.size();
		}

		public Object getValueAt(int row, int col) {
			VFSRevision version = versionList.get(row);
			switch (col) {
				case 0:
					return version.getRevisionNr();
				case 1:
					return getFullName(version.getAuthor());
				case 2: {
					String comment =  version.getComment();
					if (StringHelper.containsNonWhitespace(comment)) {
						return comment;
					} else if ("1".equals(version.getRevisionNr())) {
						return translate("version.initialRevision");
					}
					return "";
				}
				case 3:
					cal.setTimeInMillis(version.getLastModified());
					return format.format(cal.getTime());
				default:
					return "";
			}
		}
		
		private String getFullName(String name) {
			if(!StringHelper.containsNonWhitespace(name)) {
				return null;
			}
			IdentityShort id = mappedIdentities.get(name);
			if(id == null) {
				return null;
			}
			
			StringBuilder sb = new StringBuilder();
			sb.append(userManager.getUserDisplayName(id));
			if(isAdmin) {
				sb.append(" (").append(name).append(")");
			}
			return sb.toString();
		}
	}
	
	public class CurrentRevision implements VFSRevision {
		private final VFSLeaf versionFile;
		private final Versions versions;
		
		public CurrentRevision(VFSLeaf versionFile, Versions versions) {
			this.versionFile = versionFile;
			this.versions = versions;
		}

		public String getAuthor() {
			return versions.getAuthor();
		}

		public String getComment() {
			String comment = versions.getComment();
			if (StringHelper.containsNonWhitespace(comment)) {
				return comment;
			} else if ("1".equals(versions.getRevisionNr())) {
				return translate("version.initialRevision");
			}
			return "";
		}

		public InputStream getInputStream() {
			return versionFile.getInputStream();
		}

		public long getLastModified() {
			return versionFile.getLastModified();
		}

		public String getName() {
			return versionFile.getName();
		}

		public String getRevisionNr() {
			return versions.getRevisionNr();
		}

		public long getSize() {
			return versionFile.getSize();
		}
	}
}