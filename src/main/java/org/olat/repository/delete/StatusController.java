/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.repository.delete;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryTypeColumnDescriptor;
import org.olat.repository.RepositoryManager;
import org.olat.repository.delete.service.RepositoryDeletionManager;

/**
 * Controller for tab 'Learning-resource selection' 
 * 
 * @author Christian Guretzki
 */
public class StatusController extends BasicController {
	private static final String PACKAGE_REPOSITORY_MANAGER = Util.getPackageName(RepositoryManager.class);
	private static final String MY_PACKAGE = Util.getPackageName(StatusController.class);
	
	private static final String ACTION_SINGLESELECT_CHOOSE = "ssc";

	private VelocityContainer myContent;
	private Panel repositoryDeleteStatusPanel;
	private TableController tableCtr;
	private RepositoryEntryDeleteTableModel redtm;

	/**
	 * @param ureq
	 * @param wControl
	 * @param cancelbutton
	 */
	public StatusController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		PackageTranslator fallbackTrans = new PackageTranslator(PACKAGE_REPOSITORY_MANAGER, ureq.getLocale());
		this.setTranslator( new PackageTranslator( MY_PACKAGE, ureq.getLocale(), fallbackTrans) );
		myContent = createVelocityContainer("deletestatus");

		repositoryDeleteStatusPanel = new Panel("repositoryDeleteStatusPanel");
		repositoryDeleteStatusPanel.addListener(this);
		myContent.put("repositoryDeleteStatusPanel", repositoryDeleteStatusPanel);
		myContent.contextPut("header", translate("status.delete.email.header", 
				new String [] { Integer.toString(RepositoryDeletionManager.getInstance().getDeleteEmailDuration()) }));
		initializeTableController(ureq);

		putInitialPanel (myContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				if (te.getActionId().equals(ACTION_SINGLESELECT_CHOOSE)) {
					int rowid = te.getRowId();
					RepositoryManager.setLastUsageNowFor( (RepositoryEntry) redtm.getObject(rowid) );
					updateRepositoryEntryList();				
				}
			} 
		}
	}
	
	private void initializeTableController(UserRequest ureq) {
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("error.no.repository.found"));
		
		removeAsListenerAndDispose(tableCtr);
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(tableCtr);
		tableCtr.addColumnDescriptor(new RepositoryEntryTypeColumnDescriptor("table.header.typeimg", 0,null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.displayname", 1, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.author", 2, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.lastusage", 3, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.deleteEmail", 4, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor(ACTION_SINGLESELECT_CHOOSE, "table.header.action", myContent
					.getTranslator().translate("action.activate")));
		
		updateRepositoryEntryList();
		tableCtr.setMultiSelect(false);
		repositoryDeleteStatusPanel.setContent(tableCtr.getInitialComponent());
	}

	protected void updateRepositoryEntryList() {
		List l = RepositoryDeletionManager.getInstance().getReprositoryEntriesInDeletionProcess(RepositoryDeletionManager.getInstance().getDeleteEmailDuration());
		redtm = new RepositoryEntryDeleteTableModel(l);
		tableCtr.setTableDataModel(redtm);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}
		
}


