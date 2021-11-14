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

package org.olat.admin.quota;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
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
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.UserSession;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * This controller shows the list of all quotas in the system and offers an
 * editor to edit the quotas or to create a new one.
 * 
 * @author Felix Jost
 */
public class QuotaController extends BasicController {
	
	private Link addQuotaButton;
	private final VelocityContainer myContent;
	private QuotaTableModel quotaTableModel;

	private TableController tableCtr;
	private CloseableModalController cmc;
	private GenericQuotaEditController quotaEditCtr;

	@Autowired
	private QuotaManager quotaManager;

	/**
	 * @param ureq
	 * @param wControl
	 */
	public QuotaController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		myContent = createVelocityContainer("index");

		UserSession usess = ureq.getUserSession();
		boolean isAdministrator = usess.getRoles().isAdministrator() || usess.getRoles().isSystemAdmin();
		
		addQuotaButton = LinkFactory.createButton("qf.new", myContent, this);
		addQuotaButton.setVisible(isAdministrator);

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo (tableCtr);

		quotaTableModel = new QuotaTableModel();
		quotaTableModel.setObjects(quotaManager.listCustomQuotasKB());
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.path", 0, null, getLocale()));
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.quota", 1, null, getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT, new QuotaByteRenderer()));
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.limit", 2, null, getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT, new QuotaByteRenderer()));
		if(isAdministrator) {
			tableCtr.addColumnDescriptor(new StaticColumnDescriptor("qf.edit", "table.action", translate("edit")));
			tableCtr.addColumnDescriptor(new StaticColumnDescriptor("qf.del", "table.action", translate("delete")));
		}
		tableCtr.setTableDataModel(quotaTableModel);
		
		myContent.put("quotatable", tableCtr.getInitialComponent());
		putInitialPanel(myContent);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == addQuotaButton){
			doAddQuota(ureq);
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == quotaEditCtr) {
			if (event == Event.CHANGED_EVENT) {
				quotaTableModel.setObjects(quotaManager.listCustomQuotasKB());
				tableCtr.setTableDataModel(quotaTableModel);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		} else if (source == tableCtr && event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
			TableEvent te = (TableEvent)event;
			Quota q = quotaTableModel.getRowData(te.getRowId());
			if (te.getActionId().equals("qf.edit")) {
				doEditQuota(ureq, q);
			} else if (te.getActionId().equals("qf.del")) {
				// try to delete quota
				doDeleteQuota(q);
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(quotaEditCtr);
		removeAsListenerAndDispose(cmc);
		quotaEditCtr = null;
		cmc = null;
	}
	
	private void doDeleteQuota(Quota q) {
		boolean deleted = quotaManager.deleteCustomQuota(q);
		if (deleted) {
			quotaTableModel.setObjects(quotaManager.listCustomQuotasKB());
			tableCtr.setTableDataModel(quotaTableModel);
			showInfo("qf.deleted", q.getPath());
		} else {
			// default quotas can not be deleted
			showError("qf.cannot.del.default");
		}
	}
	
	private void doEditQuota(UserRequest ureq, Quota q) {
		if(guardModalController(quotaEditCtr)) return;
		
		quotaEditCtr = new GenericQuotaEditController(ureq, getWindowControl(), q, false);
		listenTo(quotaEditCtr);
		
		cmc = new CloseableModalController(getWindowControl(), "close", quotaEditCtr.getInitialComponent(), true, translate("qf.edit"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doAddQuota(UserRequest ureq) {
		if(guardModalController(quotaEditCtr)) return;
		
		// start edit workflow in dedicated quota edit controller
		removeAsListenerAndDispose(quotaEditCtr);
		quotaEditCtr = new GenericQuotaEditController(ureq, getWindowControl());
		listenTo(quotaEditCtr);
		
		cmc = new CloseableModalController(getWindowControl(), "close", quotaEditCtr.getInitialComponent(), true, translate("qf.new"));
		cmc.activate();
		listenTo(cmc);
	}
}