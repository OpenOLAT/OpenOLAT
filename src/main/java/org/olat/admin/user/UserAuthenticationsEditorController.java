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

package org.olat.admin.user;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  Aug 27, 2004
 *
 * @author Mike Stock
 */
public class UserAuthenticationsEditorController extends BasicController{
	private TableController tableCtr;
	private AuthenticationsTableDataModel authTableModel;
	private DialogBoxController confirmationDialog;
	private Identity changeableIdentity;
	private CloseableModalController cmc;
	private UserAuthenticationsMoveController moveAuthenticationCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;

	/**
	 * @param ureq
	 * @param wControl
	 * @param changeableIdentity
	 */
	public UserAuthenticationsEditorController(UserRequest ureq, WindowControl wControl, Identity changeableIdentity) { 
		super(ureq, wControl);
		
		this.changeableIdentity = changeableIdentity;		
		
		// init main view container as initial component
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.auth.provider", 0, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.auth.login", 1, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.auth.credential", 2, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor("delete", "table.header.action", translate("delete")));
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor("move", "action.move", translate("action.move")));
		authTableModel = new AuthenticationsTableDataModel(securityManager.getAuthentications(changeableIdentity));
		tableCtr.setTableDataModel(authTableModel);
		listenTo(tableCtr);

		moveAuthenticationCtrl = new UserAuthenticationsMoveController(ureq, getWindowControl(), changeableIdentity);
		listenTo(moveAuthenticationCtrl);

		putInitialPanel(tableCtr.getInitialComponent());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// no events to catch
	}
	
	/**
	 * Rebuild the authentications table data model
	 */
	public void rebuildAuthenticationsTableDataModel() {
		authTableModel = new AuthenticationsTableDataModel(securityManager.getAuthentications(changeableIdentity));
		tableCtr.setTableDataModel(authTableModel);
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmationDialog) {
			if (DialogBoxUIFactory.isYesEvent(event)) { 
				Authentication auth = (Authentication)confirmationDialog.getUserObject();
				securityManager.deleteAuthentication(auth);
				getWindowControl().setInfo(getTranslator().translate("authedit.delete.success", 
						new String[] { auth.getProvider(), changeableIdentity.getName() }));
				authTableModel.setObjects(securityManager.getAuthentications(changeableIdentity));
				tableCtr.modelChanged();
			}
		} else if (source == moveAuthenticationCtrl) {
			if (event == Event.DONE_EVENT) {
				authTableModel.setObjects(securityManager.getAuthentications(changeableIdentity));
				tableCtr.modelChanged();
				cmc.deactivate();
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			}
		} else if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals("delete")) {
					int rowid = te.getRowId();
					Authentication auth = authTableModel.getObject(rowid);
					String fullname = userManager.getUserDisplayName(changeableIdentity);
					String msg = translate("authedit.delete.confirm", new String[] { auth.getProvider(), fullname });
					confirmationDialog = activateYesNoDialog(ureq, null, msg, confirmationDialog);
					confirmationDialog.setUserObject(auth);
				} else if (actionid.equals("move")) {
					int rowid = te.getRowId();
					Authentication auth = authTableModel.getObject(rowid);
					moveAuthenticationCtrl.setAuth(auth);
					cmc = new CloseableModalController(getWindowControl(), translate("close"), moveAuthenticationCtrl.getInitialComponent(), true, translate("authedit.move.title"), true);
					listenTo(cmc);
					cmc.activate();
				}
			}
		}
		
	}

	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// DialogBoxController and TableController get disposed by BasicController
	}	

	/**
	 * 
	 */
	private static class AuthenticationsTableDataModel extends DefaultTableDataModel<Authentication> {

		/**
		 * @param objects
		 */
		public AuthenticationsTableDataModel(List<Authentication> objects) {
			super(objects);
		}

		/**
		 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
		 */
		@Override
		public final Object getValueAt(int row, int col) {
			Authentication auth = getObject(row);
			switch (col) {
				case 0: return auth.getProvider();
				case 1: return auth.getAuthusername();
				case 2: return auth.getCredential();
				default: return "error";
			}
		}

		/**
		 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
		 */
		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public AuthenticationsTableDataModel createCopyWithEmptyList() {
			return new AuthenticationsTableDataModel(new ArrayList<Authentication>());
		}
	}
}
