/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 */
package org.olat.admin.user.delete;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.olat.admin.securitygroup.gui.UserControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
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
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.Emailer;

public class BulkDeleteController extends BasicController {
	
	private VelocityContainer vc;
	private BaseSecurity securityManager;
	
	private String userlist, reason;
	private List<Identity> toDelete;
	private List<String> lstLoginsFound;
	private List<String> lstLoginsNotfound;
	
	private TableController tblCtrFound, tblCtrNotfound;
	private Link btnNext;

	public BulkDeleteController(UserRequest ureq, WindowControl wControl, String userlist, String reason) {
		super(ureq, wControl);
		
		this.userlist = userlist;
		this.reason = reason;
		
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
			TableDataModel tblData = new LoginTableDataModel(lstLoginsNotfound);
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
	public void sendMail(UserRequest ureq) {
		StringBuffer loginsFound = new StringBuffer();
		for(String login : lstLoginsFound) loginsFound.append(login + "\n");
		StringBuffer loginsNotfound = new StringBuffer();
		for(String login : lstLoginsNotfound) loginsNotfound.append(login + "\n");
		DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, ureq.getLocale());
		
		String[] bodyArgs = new String[] {
				ureq.getIdentity().getName(),
				loginsFound.toString(),
				loginsNotfound.toString(),
				reason,
				df.format(new Date())
		};
		
		String subject = translate("mail.subject");
		String body = getTranslator().translate("mail.body", bodyArgs);
		
		ContactList cl = new ContactList(WebappHelper.getMailConfig("mailSupport"));
		cl.add(WebappHelper.getMailConfig("mailSupport"));
		cl.add(ureq.getIdentity());
		List<ContactList> lstAddrTO = new ArrayList<ContactList>();
		lstAddrTO.add(cl);
		
		Emailer mailer = new Emailer(ureq.getLocale());
		try {
			//fxdiff VCRP-16: intern mail system
			mailer.sendEmail(null, lstAddrTO, subject, body);
		} catch (AddressException e) {
			Tracing.createLoggerFor(BulkDeleteController.class).error("Notificatoin mail for bulk deletion could not be sent");
		} catch (MessagingException e) {
			Tracing.createLoggerFor(BulkDeleteController.class).error("Notificatoin mail for bulk deletion could not be sent");
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

class LoginTableDataModel extends DefaultTableDataModel {

	public LoginTableDataModel(List logins) {
		super(logins);
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Object getValueAt(int row, int col) {
		String login = (String)getObject(row);
		
		switch (col) {
			case 0: return login;
			default: return "error";
		}
	}
	
}
