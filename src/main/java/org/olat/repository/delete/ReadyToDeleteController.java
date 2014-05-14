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

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
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
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.delete.service.RepositoryDeletionManager;
import org.olat.repository.ui.RepositoryEntryTypeColumnDescriptor;

/**
 * Controller for 'Ready-to-delete' tab.
 * 
 * @author Christian Guretzki
 */
public class ReadyToDeleteController extends BasicController {

	private static final String ACTION_SINGLESELECT_CHOOSE = "ssc";
	private static final String ACTION_MULTISELECT_CHOOSE = "msc";

	private VelocityContainer myContent;
	private Panel readyToDeletePanel;
	private TableController tableCtr;
	private RepositoryEntryDeleteTableModel redtm;
	private Link feedbackBackLink;
	private List<RepositoryEntry> readyToDeleteRepositoryEntries;
	private DialogBoxController deleteConfirmController;

	private final RepositoryService repositoryService;

	/**
	 * @param ureq
	 * @param wControl
	 * @param cancelbutton
	 */
	public ReadyToDeleteController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(RepositoryManager.class, ureq.getLocale()));

		repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		
		setTranslator( Util.createPackageTranslator(ReadyToDeleteController.class, getLocale(), getTranslator()) );
		myContent = createVelocityContainer("panel");

		readyToDeletePanel = new Panel("readyToDeletePanel");
		readyToDeletePanel.addListener(this);
		myContent.put("panel", readyToDeletePanel);

		initializeTableController(ureq);
		
		putInitialPanel(myContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == feedbackBackLink) {
			initializeTableController(ureq);
		}
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
					repositoryService.setLastUsageNowFor(redtm.getObject(rowid) );
					updateRepositoryEntryList();
				}
			} else if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
				if (tmse.getAction().equals(ACTION_MULTISELECT_CHOOSE)) {
					handleDeleteRepositoryEntriesEvent(ureq, tmse);
				}
			} 
		} else if (source == deleteConfirmController) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				RepositoryDeletionManager.getInstance().deleteRepositoryEntries(ureq, getWindowControl(), readyToDeleteRepositoryEntries);
				showInfo("readyToDelete.deleted.msg");
			}
			updateRepositoryEntryList();
		}
	}

	private void handleDeleteRepositoryEntriesEvent(UserRequest ureq, TableMultiSelectEvent tmse) {
		if (redtm.getObjects(tmse.getSelection()).size() != 0) {
			readyToDeleteRepositoryEntries = redtm.getObjects(tmse.getSelection());
			deleteConfirmController = activateOkCancelDialog(ureq, null, translate("readyToDelete.delete.confirm"), deleteConfirmController);
		} else {
			showWarning("nothing.selected.msg");
		}
	}

	private void initializeTableController(UserRequest ureq) {
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("error.no.repository.found"));
		tableConfig.setShowAllLinkEnabled(false);
		
		removeAsListenerAndDispose(tableCtr);
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(tableCtr);

		tableCtr.addColumnDescriptor(new RepositoryEntryTypeColumnDescriptor("table.header.typeimg", 0,null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.displayname", 1, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.author", 2, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.lastusage", 3, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.deleteEmail", 4, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor(ACTION_SINGLESELECT_CHOOSE, "table.header.action", myContent
					.getTranslator().translate("action.activate")));
		
		tableCtr.addMultiSelectAction("action.ready.to.delete", ACTION_MULTISELECT_CHOOSE);

		updateRepositoryEntryList();
		tableCtr.setMultiSelect(true);
		
		VelocityContainer readyToDeleteContent = createVelocityContainer("readyToDelete");
		readyToDeleteContent.put("readyToDelete", tableCtr.getInitialComponent());
		readyToDeleteContent.contextPut("header", translate("ready.to.delete.header", 
				Integer.toString(RepositoryDeletionManager.getInstance().getDeleteEmailDuration()) ));
		readyToDeletePanel.setContent(readyToDeleteContent);
		
	}

	protected void updateRepositoryEntryList() {
		List<RepositoryEntry> l = RepositoryDeletionManager.getInstance().getReprositoryEntriesReadyToDelete(RepositoryDeletionManager.getInstance().getDeleteEmailDuration());
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
