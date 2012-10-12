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
package org.olat.group.ui.edit;

import java.util.Collections;
import java.util.List;

import org.olat.admin.securitygroup.gui.GroupController;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.admin.securitygroup.gui.IdentitiesMoveEvent;
import org.olat.admin.securitygroup.gui.IdentitiesRemoveEvent;
import org.olat.admin.securitygroup.gui.WaitingGroupController;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupAddResponse;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.GroupLoggingAction;
import org.olat.group.model.DisplayMembers;
import org.olat.group.ui.BGMailHelper;
import org.olat.repository.RepositoryEntryShort;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupMembersController extends BasicController {
	
	private final VelocityContainer mainVC;

	private DisplayMemberSwitchForm dmsForm;
	private GroupController ownerGrpCntrllr;
	private GroupController partipGrpCntrllr;
	private WaitingGroupController waitingGruppeController;
	
	private BusinessGroup businessGroup;
	private final BusinessGroupService businessGroupService;
	
	public BusinessGroupMembersController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		super(ureq, wControl);
		
		this.businessGroup = businessGroup;
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		
		mainVC = createVelocityContainer("tab_bgGrpMngmnt");
		putInitialPanel(mainVC);
		
		boolean hasWaitingList = businessGroup.getWaitingListEnabled().booleanValue();

		// Member Display Form, allows to enable/disable that others partips see
		// partips and/or owners
		//
		DisplayMembers displayMembers = businessGroupService.getDisplayMembers(businessGroup);
		// configure the form with checkboxes for owners and/or partips according
		// the booleans
		dmsForm = new DisplayMemberSwitchForm(ureq, getWindowControl(), true, true, hasWaitingList);
		listenTo(dmsForm);
		// set if the checkboxes are checked or not.
		dmsForm.setDisplayMembers(displayMembers);
		
		mainVC.put("displayMembers", dmsForm.getInitialComponent());
		Roles roles = ureq.getUserSession().getRoles();
		boolean enableTablePreferences = roles.isOLATAdmin() || roles.isGroupManager();

		List<RepositoryEntryShort> courses = businessGroupService.findShortRepositoryEntries(Collections.<BusinessGroupShort>singletonList(businessGroup), 0, 1);
		boolean requiresOwner = courses.isEmpty();
		// groupcontroller which allows to remove all members depending on
		// configuration.
		ownerGrpCntrllr = new GroupController(ureq, getWindowControl(), true, requiresOwner, enableTablePreferences, businessGroup.getOwnerGroup());
		listenTo(ownerGrpCntrllr);
		// add mail templates used when adding and removing users
		MailTemplate ownerAddUserMailTempl = BGMailHelper.createAddParticipantMailTemplate(businessGroup, ureq.getIdentity());
		ownerGrpCntrllr.setAddUserMailTempl(ownerAddUserMailTempl,true);
		MailTemplate ownerAremoveUserMailTempl = BGMailHelper.createRemoveParticipantMailTemplate(businessGroup, ureq.getIdentity());
		ownerGrpCntrllr.setRemoveUserMailTempl(ownerAremoveUserMailTempl,true);
		// expose to velocity
		mainVC.put("ownerGrpMngmnt", ownerGrpCntrllr.getInitialComponent());
		mainVC.contextPut("hasOwnerGrp", Boolean.TRUE);

		// groupcontroller which allows to remove all members
		removeAsListenerAndDispose(partipGrpCntrllr);
		partipGrpCntrllr = new GroupController(ureq, getWindowControl(), true, false, enableTablePreferences, businessGroup.getPartipiciantGroup());
		listenTo(partipGrpCntrllr);
		
		// add mail templates used when adding and removing users
		MailTemplate partAddUserMailTempl = BGMailHelper.createAddParticipantMailTemplate(businessGroup, ureq.getIdentity());
		partipGrpCntrllr.setAddUserMailTempl(partAddUserMailTempl,true);
		MailTemplate partAremoveUserMailTempl = BGMailHelper.createRemoveParticipantMailTemplate(businessGroup, ureq.getIdentity());
		partipGrpCntrllr.setRemoveUserMailTempl(partAremoveUserMailTempl,true);
		// expose to velocity
		mainVC.put("partipGrpMngmnt", partipGrpCntrllr.getInitialComponent());
		
		// Show waiting list only if enabled 
	   // waitinglist-groupcontroller which allows to remove all members
		SecurityGroup waitingList = businessGroup.getWaitingGroup();
		waitingGruppeController = new WaitingGroupController(ureq, getWindowControl(), true, false, enableTablePreferences, waitingList );
		listenTo(waitingGruppeController);

		// add mail templates used when adding and removing users
		MailTemplate waitAddUserMailTempl = BGMailHelper.createAddWaitinglistMailTemplate(businessGroup, ureq.getIdentity());
		waitingGruppeController.setAddUserMailTempl(waitAddUserMailTempl,true);
		MailTemplate waitRemoveUserMailTempl = BGMailHelper.createRemoveWaitinglistMailTemplate(businessGroup, ureq.getIdentity());
		waitingGruppeController.setRemoveUserMailTempl(waitRemoveUserMailTempl,true);
		MailTemplate waitTransferUserMailTempl = BGMailHelper.createWaitinglistTransferMailTemplate(businessGroup, ureq.getIdentity());
		waitingGruppeController.setTransferUserMailTempl(waitTransferUserMailTempl);
		// expose to velocity
		mainVC.put("waitingGrpMngmnt", waitingGruppeController.getInitialComponent());

		mainVC.contextPut("hasWaitingGrp", new Boolean(hasWaitingList));
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	protected void updateBusinessGroup(BusinessGroup businessGroup) {
		this.businessGroup = businessGroup;
		
		boolean hasWaitingList = businessGroup.getWaitingListEnabled().booleanValue();	
		Boolean waitingFlag = (Boolean)mainVC.getContext().get("hasWaitingGrp");
		if(waitingFlag == null || waitingFlag.booleanValue() != hasWaitingList) {
			mainVC.contextPut("hasWaitingGrp", new Boolean(hasWaitingList));
			dmsForm.setWaitingListVisible(hasWaitingList);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == dmsForm) {
			if(event == Event.CHANGED_EVENT) {
				businessGroupService.updateDisplayMembers(businessGroup, dmsForm.getDisplayMembers());
				// notify current active users of this business group
				BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.CONFIGURATION_MODIFIED_EVENT, businessGroup, null);
				// do loggin
				ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_CONFIGURATION_CHANGED, getClass());
			}
		} else if(event instanceof IdentitiesAddEvent ) { 
			IdentitiesAddEvent identitiesAddedEvent = (IdentitiesAddEvent) event;
			BusinessGroupAddResponse response = null;
			addLoggingResourceable(LoggingResourceable.wrap(businessGroup));
			if (source == ownerGrpCntrllr) {
			  response = businessGroupService.addOwners(ureq.getIdentity(), identitiesAddedEvent.getAddIdentities(), businessGroup);
			} else if (source == partipGrpCntrllr) {
				response = businessGroupService.addParticipants(ureq.getIdentity(), identitiesAddedEvent.getAddIdentities(), businessGroup);					
			} else if (source == waitingGruppeController) {
				response = businessGroupService.addToWaitingList(ureq.getIdentity(), identitiesAddedEvent.getAddIdentities(), businessGroup);									
			}
			identitiesAddedEvent.setIdentitiesAddedEvent(response.getAddedIdentities());
			identitiesAddedEvent.setIdentitiesWithoutPermission(response.getIdentitiesWithoutPermission());
			identitiesAddedEvent.setIdentitiesAlreadyInGroup(response.getIdentitiesAlreadyInGroup());			
			fireEvent(ureq, Event.CHANGED_EVENT );
	  }	else if (event instanceof IdentitiesRemoveEvent) {
	  	List<Identity> identities = ((IdentitiesRemoveEvent) event).getRemovedIdentities();
			if (source == ownerGrpCntrllr) {
			  businessGroupService.removeOwners(ureq.getIdentity(), identities, businessGroup);
			} else if (source == partipGrpCntrllr) {
			  businessGroupService.removeParticipants(ureq.getIdentity(), identities, businessGroup);
			  if (businessGroup.getWaitingListEnabled().booleanValue()) {
	        // It is possible that a user is transfered from waiting-list to participants => reload data to see transfered user in right group.
			  	partipGrpCntrllr.reloadData();
			    waitingGruppeController.reloadData();
			  }
			} else if (source == waitingGruppeController) {
			  businessGroupService.removeFromWaitingList(ureq.getIdentity(), identities, businessGroup);
			}
	  	fireEvent(ureq, Event.CHANGED_EVENT );
		} else if (source == waitingGruppeController) {
			if (event instanceof IdentitiesMoveEvent) {
				IdentitiesMoveEvent identitiesMoveEvent = (IdentitiesMoveEvent) event;
				BusinessGroupAddResponse response = businessGroupService.moveIdentityFromWaitingListToParticipant(identitiesMoveEvent.getChosenIdentities(), ureq.getIdentity(), businessGroup);
				identitiesMoveEvent.setNotMovedIdentities(response.getIdentitiesAlreadyInGroup());
				identitiesMoveEvent.setMovedIdentities(response.getAddedIdentities());
				// Participant and waiting-list were changed => reload both
		  	partipGrpCntrllr.reloadData();
		    waitingGruppeController.reloadData();
				// send mail for all of them
				MailerWithTemplate mailer = MailerWithTemplate.getInstance();
				MailTemplate mailTemplate = identitiesMoveEvent.getMailTemplate();
				if (mailTemplate != null) {
					//fxdiff VCRP-16: intern mail system
					MailContext context = new MailContextImpl(businessGroup, null, getWindowControl().getBusinessControl().getAsString());
					MailerResult mailerResult = mailer.sendMailAsSeparateMails(context, identitiesMoveEvent.getMovedIdentities(), null, null, mailTemplate, null);
					MailHelper.printErrorsAndWarnings(mailerResult, getWindowControl(), ureq.getLocale());
				}
				fireEvent(ureq, Event.CHANGED_EVENT );		
			}
		}
		super.event(ureq, source, event);
	}
}
