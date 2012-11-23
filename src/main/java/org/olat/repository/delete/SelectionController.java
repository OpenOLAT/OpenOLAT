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

import org.apache.velocity.VelocityContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;

import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
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
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.PackageTranslator;

import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailNotificationEditController;
import org.olat.core.util.mail.MailTemplate;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryTypeColumnDescriptor;
import org.olat.repository.RepositoryManager;
import org.olat.repository.delete.service.RepositoryDeletionManager;

/**
 * Controller for tab 'Learning-resource selection' 
 * 
 * @author Christian Guretzki
 */
public class SelectionController extends BasicController {
	private static final String PACKAGE_REPOSITORY_MANAGER = Util.getPackageName(RepositoryManager.class);
	private static final String MY_PACKAGE = Util.getPackageName(SelectionController.class);
	
	private static final String ACTION_SINGLESELECT_CHOOSE = "ssc";
	private static final String ACTION_MULTISELECT_CHOOSE = "msc";
	private static final String KEY_EMAIL_SUBJECT = "delete.announcement.email.subject";
	private static final String KEY_EMAIL_BODY = "delete.announcement.email.body";

	private VelocityContainer myContent;
	private Panel deleteSelectionPanel;
	private SelectionForm selectionForm;
	private TableController tableCtr;
	private RepositoryEntryDeleteTableModel redtm;
	//private PackageTranslator pT;
	private VelocityContainer selectionListContent;
	private Link editParameterLink;
	private MailNotificationEditController deleteRepositoryMailCtr;
	private List selectedRepositoryEntries;
	private CloseableModalController cmc;


	/**
	 * @param ureq
	 * @param wControl
	 * @param cancelbutton
	 */
	public SelectionController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		PackageTranslator fallbackTrans = new PackageTranslator(PACKAGE_REPOSITORY_MANAGER, ureq.getLocale());
		this.setTranslator( new PackageTranslator( MY_PACKAGE, ureq.getLocale(), fallbackTrans) );
		myContent = createVelocityContainer("panel");
		deleteSelectionPanel = new Panel("deleteSelectionPanel");
		deleteSelectionPanel.addListener(this);
		myContent.put("panel", deleteSelectionPanel);
		initializeTableController(ureq);
		initializeContent();

		putInitialPanel(myContent);
	}

	private void initializeContent() {
		updateRepositoryEntryList();
		selectionListContent = createVelocityContainer("selectionlist");
		selectionListContent.put("repositorylist", tableCtr.getInitialComponent() );
		selectionListContent.contextPut("header", translate("selection.delete.header",
				new String[] { Integer.toString(RepositoryDeletionManager.getInstance().getLastUsageDuration()) }));
		editParameterLink = LinkFactory.createButtonXSmall("button.editParameter", selectionListContent, this);
		deleteSelectionPanel.setContent(selectionListContent);
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
			listenTo (selectionForm);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(
					getWindowControl(), translate("close"), selectionForm.getInitialComponent()
			);
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
					RepositoryManager.getInstance().setLastUsageNowFor( (RepositoryEntry) redtm.getObject(rowid) );
					updateRepositoryEntryList();
				}
			} else if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
				if (tmse.getAction().equals(ACTION_MULTISELECT_CHOOSE)) {
					handleEmailButtonEvent(ureq, tmse);
				}
			} 
			initializeContent();
		} else if (source == deleteRepositoryMailCtr) {
			if (event == Event.DONE_EVENT) {
				String result = RepositoryDeletionManager.getInstance().sendDeleteEmailTo(selectedRepositoryEntries, deleteRepositoryMailCtr.getMailTemplate(),
						deleteRepositoryMailCtr.isTemplateChanged(),	KEY_EMAIL_SUBJECT, KEY_EMAIL_BODY, ureq.getIdentity(), getTranslator());					
				cmc.deactivate();
				if(deleteRepositoryMailCtr.getMailTemplate() != null) {
					// when mailtemplate is null, user decides to send no email => no status message
					if (result.length() > 0 ) {
						showWarning("delete.email.announcement.warning.header", result);
					} else {
						showInfo("selection.feedback.msg");
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
				RepositoryDeletionManager.getInstance().setLastUsageDuration(selectionForm.getLastUsageDuration());
				RepositoryDeletionManager.getInstance().setDeleteEmailDuration(selectionForm.getDeleteEmailDuration());
				initializeContent();
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, event);
			}
			cmc.deactivate();
		} 
	}

	private void handleEmailButtonEvent(UserRequest ureq, TableMultiSelectEvent tmse) {
		if (redtm.getObjects(tmse.getSelection()).size() != 0) {
			selectedRepositoryEntries = redtm.getObjects(tmse.getSelection());
			
			MailTemplate deleteMailTemplate = createMailTemplate(translate(KEY_EMAIL_SUBJECT), translate(KEY_EMAIL_BODY));
			deleteMailTemplate.addToContext("lastloginduration",   Integer.toString(RepositoryDeletionManager.getInstance().getLastUsageDuration() ));
			deleteMailTemplate.addToContext("durationdeleteemail", Integer.toString(RepositoryDeletionManager.getInstance().getDeleteEmailDuration() ));
	
			removeAsListenerAndDispose(deleteRepositoryMailCtr);
			deleteRepositoryMailCtr = new MailNotificationEditController(getWindowControl(), ureq, deleteMailTemplate, true, false);
			listenTo(deleteRepositoryMailCtr);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(
					getWindowControl(), translate("close"),
					deleteRepositoryMailCtr.getInitialComponent()
			);
			listenTo(cmc);
			
			cmc.activate();
		} else {
			showWarning("nothing.selected.msg");
		}
	}

	private void initializeTableController(UserRequest ureq) {
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("error.no.repository.found"));
		
		removeAsListenerAndDispose(tableCtr);
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo (tableCtr);
		
		tableCtr.addColumnDescriptor(new RepositoryEntryTypeColumnDescriptor("table.header.typeimg", 0,null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.displayname", 1, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.author", 2, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.lastusage", 3, null, ureq.getLocale()));
		tableCtr.addMultiSelectAction("action.delete.selection", ACTION_MULTISELECT_CHOOSE);
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor(ACTION_SINGLESELECT_CHOOSE, "table.header.action", myContent
					.getTranslator().translate("action.activate")));
		tableCtr.setMultiSelect(true);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}

	public void updateRepositoryEntryList() {
		List l = RepositoryDeletionManager.getInstance().getDeletableReprositoryEntries(RepositoryDeletionManager.getInstance().getLastUsageDuration());
		redtm = new RepositoryEntryDeleteTableModel(l);
		tableCtr.setTableDataModel(redtm);
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

}


class SelectionForm extends FormBasicController {

	private IntegerElement lastUsageDuration;
	private IntegerElement emailDuration;

	/**
	 * @param name
	 * @param cancelbutton
	 * @param isAdmin if true, no field must be filled in at all, otherwise
	 *          validation takes place
	 */
	public SelectionForm(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm (ureq);
	}

	public int getDeleteEmailDuration() {
		return emailDuration.getIntValue();
	}

	public int getLastUsageDuration() {
		return lastUsageDuration.getIntValue();
	}

	

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
		
	}
	
	protected void formCancelled (UserRequest ureq) {
		fireEvent (ureq, Event.CANCELLED_EVENT);	
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		lastUsageDuration = uifactory.addIntegerElement("lastUsageDuration", "edit.parameter.form.lastusage.duration", RepositoryDeletionManager.getInstance().getLastUsageDuration(), formLayout);
		emailDuration = uifactory.addIntegerElement("emailDuration", "edit.parameter.form.email.duration", RepositoryDeletionManager.getInstance().getDeleteEmailDuration(), formLayout);
		
		lastUsageDuration.setMinValueCheck(1, null);
		emailDuration.setMinValueCheck(1, null);
		
		lastUsageDuration.setDisplaySize(3);
		emailDuration.setDisplaySize(3);
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		
		uifactory.addFormSubmitButton("submit", "edit.parameter.form.submit", buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
	}

	@Override
	protected void doDispose() {
		//
	}
}