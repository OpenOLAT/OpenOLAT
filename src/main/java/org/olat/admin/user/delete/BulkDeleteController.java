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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package org.olat.admin.user.delete;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.admin.securitygroup.gui.UserControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;

public class BulkDeleteController extends BasicController {
	
	private VelocityContainer vc;
	private BaseSecurity securityManager;
	
	private String userlist, reason;
	private List<Identity> toDelete;
	private List<String> lstLoginsFound;
	private List<String> lstLoginsNotfound;
	
	private TableController tblCtrFound, tblCtrNotfound;
	private Link btnNext;
	
	private final MailManager mailService;

	public BulkDeleteController(UserRequest ureq, WindowControl wControl, String userlist, String reason) {
		super(ureq, wControl);
		
		this.userlist = userlist;
		this.reason = reason;
		mailService = CoreSpringFactory.getImpl(MailManager.class);
		
		vc = createVelocityContainer("bulkdelete");
		processUserList(this.userlist);
		
		if(toDelete != null && toDelete.size() > 0) {
			tblCtrFound = UserControllerFactory.createTableControllerFor(new TableGuiConfiguration(), toDelete, ureq, getWindowControl(), null);
			listenTo(tblCtrFound);
			btnNext = LinkFactory.createButton("next", vc, this);
			vc.put("table.users.found", tblCtrFound.getInitialComponent());
		}
		
		if(lstLoginsNotfound.size() > 0) {
			tblCtrNotfound = new TableController(null, ureq, wControl, getTranslator());
			listenTo(tblCtrNotfound);
			tblCtrNotfound.addColumnDescriptor(new DefaultColumnDescriptor("table.col.login", 0, null, ureq.getLocale()));
			TableDataModel<String> tblData = new LoginTableDataModel(lstLoginsNotfound);
			tblCtrNotfound.setTableDataModel(tblData);
			
			vc.put("table.users.notfound", tblCtrNotfound.getInitialComponent());
		}
		
		vc.contextPut("reason", this.reason);
		
		putInitialPanel(vc);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == btnNext) {
			fireEvent(ureq, event);
		}
	}
	
	/**
	 * Separate logins that are found in system and not found
	 * @param loginsString
	 */
	private void processUserList(String loginsString) {
		securityManager = BaseSecurityManager.getInstance();
		toDelete = new ArrayList<Identity>();
		lstLoginsFound = new ArrayList<String>();
		lstLoginsNotfound = new ArrayList<String>();
		String[] logins = loginsString.split("\r?\n");
		for( String login : logins) {
			if(login.equals(""))
				continue;
			Identity ident = securityManager.findIdentityByName(login);
			if(ident == null) {
				lstLoginsNotfound.add(login);
			} else if(ident.getStatus().intValue() == Identity.STATUS_DELETED.intValue()) {
				lstLoginsNotfound.add(login);
			} else {
				// prevent double entries
				if(!lstLoginsFound.contains(login)) {
					lstLoginsFound.add(login);
					toDelete.add(ident);
				}
			}
		}
	}
	
	/**
	 * Send the mail with informations: who deletes, when, list of deleted users
	 * list of not deleted users, reason for deletion
	 */
	public void sendMail() {
		String recipient = WebappHelper.getMailConfig("mailDeleteUser");
		if (recipient.equals("disabled")) {
			return;
		}
		
		StringBuilder loginsFound = new StringBuilder();
		for(String login : lstLoginsFound) {
			loginsFound.append(login + "\n");
		}
		StringBuilder loginsNotfound = new StringBuilder();
		for(String login : lstLoginsNotfound) {
			loginsNotfound.append(login + "\n");
		}
		DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, getLocale());
		
		String[] bodyArgs = new String[] {
				getIdentity().getName(),
				loginsFound.toString(),
				loginsNotfound.toString(),
				reason,
				df.format(new Date())
		};

		ContactList cl = new ContactList(recipient);
		cl.add(recipient);
		cl.add(getIdentity());

		try {
			MailBundle bundle = new MailBundle();
			bundle.setFrom(WebappHelper.getMailConfig("mailReplyTo"));
			bundle.setContent(translate("mail.subject"), translate("mail.body", bodyArgs));
			bundle.setContactList(cl);
			mailService.sendMessage(bundle);
		} catch (Exception e) {
			logError("Notificatoin mail for bulk deletion could not be sent", null);
		} 
	}
	
	public List<Identity> getToDelete() {
		return toDelete;
	}

	@Override
	protected void doDispose() {
		//nothing
	}
}

class LoginTableDataModel extends DefaultTableDataModel<String> {

	public LoginTableDataModel(List<String> logins) {
		super(logins);
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Object getValueAt(int row, int col) {
		String login = getObject(row);
		switch (col) {
			case 0: return login;
			default: return "error";
		}
	}
}
