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

package org.olat.admin.user.delete;

import java.util.List;

import org.apache.velocity.VelocityContext;
import org.olat.admin.user.UserSearchController;
import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
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
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailNotificationEditController;
import org.olat.core.util.mail.MailTemplate;
import org.olat.user.UserManager;

/**
 * Controller for tab 'User Selection' 
 * 
 * @author Christian Guretzki
 */
public class SelectionController extends BasicController {
	
	private static final String ACTION_SINGLESELECT_CHOOSE = "ssc";
	private static final String ACTION_MULTISELECT_CHOOSE = "msc";
	private static final String KEY_EMAIL_SUBJECT = "delete.announcement.email.subject";
	private static final String KEY_EMAIL_BODY = "delete.announcement.email.body";

	private VelocityContainer myContent;
	private Panel userSelectionPanel;
	private SelectionForm selectionForm;
	private TableController tableCtr;	
	private UserDeleteTableModel tdm;
	
	private VelocityContainer selectionListContent;
	private Link editParameterLink;
	private MailNotificationEditController deleteUserMailCtr;
	private List<Identity> selectedIdentities;
	private boolean isAdministrativeUser;
	private Translator propertyHandlerTranslator;
  
	private CloseableModalController cmc;

  /**
	 * @param ureq
	 * @param wControl
	 * @param cancelbutton
	 */
	public SelectionController(UserRequest ureq, WindowControl wControl) {
		super (ureq, wControl);
		
		Translator fallbackTrans = Util.createPackageTranslator(UserSearchController.class, getLocale());
		setTranslator(Util.createPackageTranslator(SelectionController.class, getLocale(), fallbackTrans));
		//use the PropertyHandlerTranslator	as tableCtr translator
		propertyHandlerTranslator = UserManager.getInstance().getPropertyHandlerTranslator(getTranslator());
				
		myContent = this.createVelocityContainer("panel");

		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = CoreSpringFactory.getImpl(BaseSecurityModule.class).isUserAllowedAdminProps(roles);
		//(roles.isAuthor() || roles.isGroupManager() || roles.isUserManager() || roles.isOLATAdmin());		
		
		userSelectionPanel = new Panel("userSelectionPanel");
		userSelectionPanel.addListener(this);
		initializeTableController(ureq);
		initializeContent();
		myContent.put("panel", userSelectionPanel);
		
		putInitialPanel(myContent);
	}

	private void initializeContent() {
		updateUserList();
		
		selectionListContent = this.createVelocityContainer("selectionuserlist");
		selectionListContent.put("userlist", tableCtr.getInitialComponent() );
		selectionListContent.contextPut("header", getTranslator().translate("user.selection.delete.header",
				new String[] { Integer.toString(UserDeletionManager.getInstance().getLastLoginDuration()) }));
		editParameterLink = LinkFactory.createButtonXSmall("button.editParameter", selectionListContent, this);
		userSelectionPanel.setContent(selectionListContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == editParameterLink) {
			removeAsListenerAndDispose(selectionForm);
			selectionForm = new SelectionForm(ureq, getWindowControl());
			listenTo(selectionForm);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), selectionForm.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
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
					UserDeletionManager.getInstance().setIdentityAsActiv( tdm.getObject(rowid) );
				}
			} else if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
				if (tmse.getAction().equals(ACTION_MULTISELECT_CHOOSE)) {
					handleEmailButtonEvent(ureq, tmse);
				}
			}
			initializeContent();
		} else if (source == deleteUserMailCtr) {
			if (event == Event.DONE_EVENT) {
				String warningMessage = UserDeletionManager.getInstance().sendUserDeleteEmailTo(selectedIdentities,	deleteUserMailCtr.getMailTemplate(), 
						deleteUserMailCtr.isTemplateChanged(),	KEY_EMAIL_SUBJECT, KEY_EMAIL_BODY, ureq.getIdentity(), getTranslator());
				cmc.deactivate();		
				if(deleteUserMailCtr.getMailTemplate() != null) {
					// when mailtemplate is null, user decides to send no email => no status message
					if (warningMessage.length() > 0 ) {						
						this.showWarning("delete.email.announcement.warning.header",warningMessage);
					} else {						
						this.showInfo("selection.feedback.msg");
					}
				}
				initializeContent();
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			} else {
				throw new RuntimeException("unknown event ::" + event.getCommand());
			}
		} else if (source == selectionForm) {
			if (event == Event.DONE_EVENT) {
				UserDeletionManager.getInstance().setLastLoginDuration(selectionForm.getLastLoginDuration());
				UserDeletionManager.getInstance().setDeleteEmailDuration(selectionForm.getDeleteEmailDuration());
				initializeContent();
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
			cmc.deactivate();
			removeAsListenerAndDispose(selectionForm);
			removeAsListenerAndDispose(cmc);
		} 
	}

	private void handleEmailButtonEvent(UserRequest ureq, TableMultiSelectEvent tmse) {
		List<Identity> identities = tdm.getObjects(tmse.getSelection());
		if (identities.size() > 0) {
			selectedIdentities = identities;
			MailTemplate deleteMailTemplate = createMailTemplate(translate(KEY_EMAIL_SUBJECT), translate(KEY_EMAIL_BODY));
			deleteMailTemplate.setCpfrom(Boolean.FALSE);
			deleteMailTemplate.addToContext("lastloginduration",   Integer.toString(UserDeletionManager.getInstance().getLastLoginDuration() ));
			deleteMailTemplate.addToContext("durationdeleteemail", Integer.toString(UserDeletionManager.getInstance().getDeleteEmailDuration() ));

			removeAsListenerAndDispose(deleteUserMailCtr);
			deleteUserMailCtr = new MailNotificationEditController(getWindowControl(), ureq, deleteMailTemplate, true, false, false);
			listenTo(deleteUserMailCtr);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteUserMailCtr.getInitialComponent());
			listenTo(cmc);
			
			cmc.activate();
		} else {			
			showWarning("nothing.selected.msg");
		}
	}

	private void initializeTableController(UserRequest ureq) {
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("error.no.user.found"));
		
		removeAsListenerAndDispose(tableCtr);
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), propertyHandlerTranslator);
		listenTo(tableCtr);
		
		List<Identity> l = UserDeletionManager.getInstance().getDeletableIdentities(UserDeletionManager.getInstance().getLastLoginDuration());		
		tdm = new UserDeleteTableModel(l, ureq.getLocale(), isAdministrativeUser);				
		tdm.addColumnDescriptors(tableCtr, null);		
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor(ACTION_SINGLESELECT_CHOOSE, "table.header.action", translate("action.activate")));
		tableCtr.addMultiSelectAction("action.delete.selection", ACTION_MULTISELECT_CHOOSE);		
		tableCtr.setMultiSelect(true);
		tableCtr.setTableDataModel(tdm);		
	}

	public void updateUserList() {
		List<Identity> l = UserDeletionManager.getInstance().getDeletableIdentities(UserDeletionManager.getInstance().getLastLoginDuration());		
		tdm.setObjects(l);	
		tableCtr.setTableDataModel(tdm);			
	}

	/**
	 * Create default template which fill in context 'firstname' , 'lastname' and 'username'.
	 * @param subject
	 * @param body
	 * @return
	 */
	private MailTemplate createMailTemplate(String subject, String body) {		
		return new MailTemplate(subject, body, null) {
			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity identity) {
				context.put("firstname", identity.getUser().getProperty(UserConstants.FIRSTNAME, null));
				context.put("lastname", identity.getUser().getProperty(UserConstants.LASTNAME, null));
				context.put("username", identity.getName());
			}
		};
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}
		
}


class SelectionForm extends FormBasicController {

	private IntegerElement lastLoginDuration;
	private IntegerElement emailDuration;

	
	public SelectionForm(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	public int getDeleteEmailDuration() {
		return emailDuration.getIntValue();
	}

	public int getLastLoginDuration() {
		return lastLoginDuration.getIntValue();
	}

	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
		
	}
	
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		lastLoginDuration = uifactory.addIntegerElement("lastLoginDuration", "edit.parameter.form.lastlogin.duration", UserDeletionManager.getInstance().getLastLoginDuration(), formLayout);
		lastLoginDuration.setDisplaySize(3);
		lastLoginDuration.setMinValueCheck(1, null);
		
		emailDuration = uifactory.addIntegerElement ("emailDuration", "edit.parameter.form.email.duration", UserDeletionManager.getInstance().getDeleteEmailDuration(), formLayout);
		emailDuration.setDisplaySize(3);
		emailDuration.setMinValueCheck(1, null);
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		
		uifactory.addFormSubmitButton("submit", "submit", buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
	}

	@Override
	protected void doDispose() {
		// 
	}
}