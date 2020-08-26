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

package org.olat.admin.securitygroup.gui;

import java.util.List;

import org.olat.basesecurity.SecurityGroup;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.MailNotificationEditController;
import org.olat.core.util.mail.MailTemplate;
import org.olat.group.ui.main.OnlineIconRenderer;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Description:<BR>
 * Waiting-list group management controller. Displays the list of users that are in the 
 * given waiting group and features an 'move as participant' button to add users to the group.
 * The following events are fired:<br>
 * SingleIdentityChosenEvent
 * Event.CANCELLED_EVENT
 * <P>
 * Initial Date:  16.05.2006
 *
 * @author Christian Guretzki
 */

public class WaitingGroupController extends GroupController {

	protected static final String COMMAND_MOVE_USER_WAITINGLIST = "move.user.waitinglist";
	private MailTemplate transferUserMailTempl;
	private MailNotificationEditController transferMailCtr;
	private CloseableModalController cmc;
	private List<Identity> toTransfer;

	/**
	 * @param ureq
	 * @param wControl
	 * @param mayModifyMembers
	 * @param keepAtLeastOne
	 * @param enableTablePreferences
	 * @param aSecurityGroup
	 */
	public WaitingGroupController(UserRequest ureq, WindowControl wControl, boolean mayModifyMembers, boolean keepAtLeastOne, boolean enableTablePreferences,
			boolean allowDownload, boolean mandatoryEmail, SecurityGroup waitingListGroup) {
		super(ureq, wControl, mayModifyMembers, keepAtLeastOne, enableTablePreferences, false, allowDownload, mandatoryEmail, waitingListGroup);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller sourceController, Event event) {
		if (sourceController == tableCtr) {
			if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
				// Multiselect events
				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
				if (tmse.getAction().equals(COMMAND_MOVE_USER_WAITINGLIST)) {
					if (tmse.getSelection().isEmpty()) {
						getWindowControl().setWarning(translate("msg.selectionempty"));
					}	else {				
						List<Identity> objects = identitiesTableModel.getIdentities(tmse.getSelection());
						toTransfer = objects;
						
						removeAsListenerAndDispose(transferMailCtr);
						transferMailCtr = new MailNotificationEditController(getWindowControl(), ureq, transferUserMailTempl, true, mandatoryEmail, true);
						listenTo(transferMailCtr);
						
						removeAsListenerAndDispose(cmc);
						cmc = new CloseableModalController(getWindowControl(), translate("close"), transferMailCtr.getInitialComponent());
						listenTo(cmc);
						
						cmc.activate();
						return; // don't execute super method
					}
				}
			}
		} else if (sourceController == transferMailCtr) {
			if (event == Event.DONE_EVENT) {
				// fetch configured mail template and finish this controller workflow
				MailTemplate customTransferTemplate = transferMailCtr.getMailTemplate();
				cmc.deactivate();
				IdentitiesMoveEvent identitiesMoveEvent = new IdentitiesMoveEvent(toTransfer);
				identitiesMoveEvent.setMailTemplate(customTransferTemplate);
				fireEvent(ureq, identitiesMoveEvent);
				StringBuilder infoMessage = new StringBuilder();
				for (Identity identity : identitiesMoveEvent.getNotMovedIdentities()) {
					String fullName = userManager.getUserDisplayName(identity);
					infoMessage.append(translate("msg.alreadyinwaiitinggroup", fullName)).append("<br />");
				}
				// report any errors on screen
				if (infoMessage.length() > 0) getWindowControl().setInfo(infoMessage.toString());
				return;  // don't execute super method
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
				return;  // don't execute super method
			} else {
				throw new RuntimeException("unknown event ::" + event.getCommand());
			}
			
		} 
		// it is no WaitingGroupController event, forward it to super class GroupController 
		super.event(ureq,sourceController,event);
	}
		
	/**
	 * Init WaitingList-table-controller for waitinglist with addional column action=move user to participant-list.
	 * Show added-date attribute and sort waiting list per default by added date.
	 */
	@Override
	protected void initGroupTable(TableController tableCtr, UserRequest ureq, boolean enableTablePreferences, boolean enableUserSelection) {
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		// first the login name
		if(chatEnabled) {
			tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.online", 1, COMMAND_IM, getLocale(),
					ColumnDescriptor.ALIGNMENT_LEFT, new OnlineIconRenderer()));
		}
		
		int visibleColId = 0;
		// followed by the users fields
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			ColumnDescriptor cd = userPropertyHandler.getColumnDescriptor(i + 3, COMMAND_VCARD, ureq.getLocale());
			// make all user attributes clickable to open visiting card
			if (cd instanceof DefaultColumnDescriptor) {
				DefaultColumnDescriptor dcd = (DefaultColumnDescriptor) cd;
				dcd.setIsPopUpWindowAction(true, "height=700, width=900, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no");
				
			}
			tableCtr.addColumnDescriptor(visible, cd);
			if (visible) {
				visibleColId++;
			}
		}
		
		// in the end
		if (enableTablePreferences) {
			tableCtr.addColumnDescriptor(true, new DefaultColumnDescriptor("table.subject.addeddate", 2, COMMAND_VCARD, ureq.getLocale()));
			tableCtr.setSortColumn(++visibleColId,true);	
		}
		
		if (mayModifyMembers) {
			tableCtr.addMultiSelectAction("action.waitinglist.move", COMMAND_MOVE_USER_WAITINGLIST);
			tableCtr.addMultiSelectAction("action.remove", COMMAND_REMOVEUSER);
			tableCtr.setMultiSelect(true);
		}   	
	}
	
	/**
	 * @param addUserMailDefaultTempl Set a template to send mail when adding
	 *          users to group
	 */
	public void setTransferUserMailTempl(MailTemplate transferUserMailTempl) {
		this.transferUserMailTempl = transferUserMailTempl;
	}
	
}
