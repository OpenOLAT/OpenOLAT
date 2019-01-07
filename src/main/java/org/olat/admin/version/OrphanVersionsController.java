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
package org.olat.admin.version;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.version.OrphanVersion;
import org.olat.core.util.vfs.version.VFSRevision;
import org.olat.core.util.vfs.version.VersionsManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * List all orphans
 * 
 * <P>
 * Initial Date:  5 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OrphanVersionsController extends BasicController {
	
	private static final String CMD_DELETE = "delete";
	private static final DecimalFormat sizeFormat = new DecimalFormat("#0.#", new DecimalFormatSymbols(Locale.ENGLISH));
	
	private TableController tableCtr;
	private final List<OrphanVersion> orphans;
	
	@Autowired
	private VersionsManager versionsManager;

	public OrphanVersionsController(UserRequest ureq, WindowControl wControl, List<OrphanVersion> orphans) {
		super(ureq, wControl);
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("table.empty"));
		tableConfig.setDownloadOffered(true);
		tableConfig.setMultiSelect(true);
		
		this.orphans = orphans;

		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.file", 0, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.versions", 1, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.size", 2, null, getLocale()));
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_DELETE, "delete", translate("delete")));
		tableCtr.setTableDataModel(new OrphanTableModel(orphans));
		
		tableCtr.addMultiSelectAction("delete", CMD_DELETE);
		
		listenTo(tableCtr);

		putInitialPanel(tableCtr.getInitialComponent());
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				OrphanVersion orphan = (OrphanVersion) tableCtr.getTableDataModel().getObject(rowid);
				if (actionid.equals(CMD_DELETE)) {
					versionsManager.delete(orphan);
					orphans.remove(orphan);
					tableCtr.modelChanged();
				}
			} else if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
				// Multiselect events
				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
				BitSet selectedOrphans = tmse.getSelection();
				String actionid = tmse.getAction();
				if (CMD_DELETE.equals(actionid)) {
					List<OrphanVersion> toRemove = new ArrayList<>();
					for (int i=selectedOrphans.nextSetBit(0); i >= 0; i=selectedOrphans.nextSetBit(i+1)) {
						int rowCount = tableCtr.getTableDataModel().getRowCount();
						if(i >= 0 && i < rowCount) {
							OrphanVersion orphan = (OrphanVersion)tableCtr.getTableDataModel().getObject(i);
							versionsManager.delete(orphan);
							toRemove.add(orphan);
						}
					}
					for(OrphanVersion orphan:toRemove) {
						orphans.remove(orphan);
					}
					tableCtr.setTableDataModel(new OrphanTableModel(orphans));
					tableCtr.modelChanged();
				}
			}
		}
	}

	private class OrphanTableModel implements TableDataModel<OrphanVersion> {
		
		private List<OrphanVersion> orphanList;
		
		public OrphanTableModel(List<OrphanVersion> orphans) {
			this.orphanList = orphans;
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			return orphanList.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			OrphanVersion orphan = getObject(row);
			switch(col) {
				case 0: return orphan.getOriginalFilePath();
				case 1: {
					return orphan.getVersions().getRevisions().size();
				}
				case 2: {
					List<VFSRevision> versions = orphan.getVersions().getRevisions();
					long size = 0l;
					for(VFSRevision revision:versions) {
						size += revision.getSize();
					}
					
					String unit = "KB";
					double humanSize = size / 1024.0d;
					if(humanSize > 1024) {
						humanSize /= 1024;
						unit = "MB";
					}
					return sizeFormat.format(humanSize) + " " + unit;
				}
				default: return "ERROR";
			}
		}

		@Override
		public OrphanVersion getObject(int row) {
			return orphanList.get(row);
		}

		@Override
		public void setObjects(List<OrphanVersion> objects) {
			orphanList = objects;
		}

		@Override
		public Object createCopyWithEmptyList() {
			return new OrphanTableModel(Collections.<OrphanVersion>emptyList());
		}
	}
}