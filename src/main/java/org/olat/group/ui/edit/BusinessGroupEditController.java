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

package org.olat.group.ui.edit;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.admin.securitygroup.gui.GroupController;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.admin.securitygroup.gui.IdentitiesMoveEvent;
import org.olat.admin.securitygroup.gui.IdentitiesRemoveEvent;
import org.olat.admin.securitygroup.gui.WaitingGroupController;
import org.olat.basesecurity.SecurityGroup;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.collaboration.CollaborationToolsSettingsController;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.choice.Choice;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupAddResponse;
import org.olat.group.BusinessGroupService;
import org.olat.group.GroupLoggingAction;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.model.DisplayMembers;
import org.olat.group.right.BGRightManager;
import org.olat.group.right.BGRights;
import org.olat.group.ui.BGControllerFactory;
import org.olat.group.ui.BGMailHelper;
import org.olat.group.ui.BusinessGroupFormController;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description: <BR>
 * This controller displays a tabbed pane that lets the user configure and
 * modify a business group. The functionality must be configured using the
 * BGConfigFlags.
 * <P>
 * Fires BusinessGroupModifiedEvent via the OLATResourceableEventCenter
 * <P>
 * Initial Date: Aug 17, 2004
 * 
 * @author patrick
 */

public class BusinessGroupEditController extends BasicController implements ControllerEventListener, GenericEventListener, Activateable2 {
	private final BGRightManager rightManager;
	private final BGAreaManager areaManager;
	private final BusinessGroupService businessGroupService;
	
	private BusinessGroupFormController modifyBusinessGroupController;
	private BusinessGroup currBusinessGroup;
	private CollaborationToolsSettingsController ctc;
	private GroupController ownerGrpCntrllr;
	private GroupController partipGrpCntrllr;
	private WaitingGroupController waitingGruppeController;

	private AreasToGroupDataModel areaDataModel;

	private RightsToGroupDataModel rightDataModel;
	private Choice areasChoice, rightsChoice;
	private List<BGArea> selectedAreas;
	private List<String> selectedRights;
	private BGRights bgRights;
	private TabbedPane tabbedPane;
	private VelocityContainer vc_edit;
	private VelocityContainer vc_tab_bgDetails;
	private VelocityContainer vc_tab_grpmanagement;
	private VelocityContainer vc_tab_bgCTools;
	private VelocityContainer vc_tab_bgAreas;
	private VelocityContainer vc_tab_bgRights;
	//fxdiff VCRP-1,2: access control of resources
	private int tabAccessIndex;
	private BusinessGroupEditAccessController tabAccessCtrl;
	private DisplayMemberSwitchForm dmsForm;

	private LockResult lockEntry;

	private DialogBoxController alreadyLockedDialogController;
	
	private BusinessGroupEditResourceController resourceController;

	/**
	 * Never call this constructor directly, use the BGControllerFactory instead!!
	 * 
	 * @param ureq
	 * @param wControl
	 * @param currBusinessGroup
	 * @param configurationFlags Flags to configure the controllers features. The
	 *          controller does no type specific stuff implicit just by looking at
	 *          the group type. Type specifig features must be flagged.
	 */
	public BusinessGroupEditController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		super(ureq, wControl);
		
		// OLAT-4955: setting the stickyActionType here passes it on to any controller defined in the scope of the editor,
		//            basically forcing any logging action called within the bg editor to be of type 'admin'
		getUserActivityLogger().setStickyActionType(ActionType.admin);
		addLoggingResourceable(LoggingResourceable.wrap(businessGroup));
		
		// Initialize managers
		areaManager = CoreSpringFactory.getImpl(BGAreaManager.class);
		rightManager = CoreSpringFactory.getImpl(BGRightManager.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		// Initialize other members

		// Initialize translator:
		// package translator with default group fallback translators and type
		// translator
		setTranslator(Util.createPackageTranslator(BGControllerFactory.class, getLocale(), getTranslator()));
		// Initialize available rights
		bgRights = new CourseRights(ureq.getLocale());
		// try to acquire edit lock on business group
		String locksubkey = "groupEdit";
		lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(businessGroup, ureq.getIdentity(), locksubkey);
		if (lockEntry.isSuccess()) {
			// reload group to minimize stale object exception and update last usage
			// timestamp
			currBusinessGroup = businessGroupService.setLastUsageFor(businessGroup);
			if(currBusinessGroup == null) {
				VelocityContainer vc = createVelocityContainer("deleted");
				vc.contextPut("name", businessGroup.getName());
				putInitialPanel(vc);
				return;
			}
			
			// add as listener to BusinessGroup so we are being notified about
			// changes.
			CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), currBusinessGroup);

			/*
			 * add Tabbed Panes for configuration
			 */
			//fxdiff VCRP-1,2: access control of resources
			tabbedPane = new TabbedPane("bgTabbs", ureq.getLocale());
			tabbedPane.addListener(this);
			setAllTabs(ureq);
			vc_edit = createVelocityContainer("edit");
			vc_edit.put("tabbedpane", tabbedPane);
			String[] title = new String[] { StringEscapeUtils.escapeHtml(currBusinessGroup.getName()) };
			vc_edit.contextPut("title", getTranslator().translate("group.edit.title", title));
			putInitialPanel(vc_edit);
		}else{
			//lock was not successful !
			alreadyLockedDialogController = DialogBoxUIFactory.createResourceLockedMessage(ureq, wControl, lockEntry, "error.message.locked", getTranslator());
			listenTo(alreadyLockedDialogController);
			alreadyLockedDialogController.activate();
		}
		//fxdiff BAKS-7 Resume function
		ContextEntry ce = wControl.getBusinessControl().popLauncherContextEntry();
		if(ce != null) {
			wControl.getBusinessControl().setCurrentContextEntry(BusinessControlFactory.getInstance().createContextEntry(currBusinessGroup));//tab are not in the regular path
			tabbedPane.activate(ureq, Collections.singletonList(ce), null);
			tabbedPane.addToHistory(ureq, wControl);
		}
	}
	
	private void setAllTabs(UserRequest ureq) {
		vc_tab_bgDetails = createTabDetails(ureq, currBusinessGroup);// modifies vc_tab_bgDetails
		tabbedPane.addTab(translate("group.edit.tab.details"), vc_tab_bgDetails);

		vc_tab_bgCTools = createTabCollabTools(ureq);
		tabbedPane.addTab(translate("group.edit.tab.collabtools"), vc_tab_bgCTools);

		vc_tab_bgAreas = createTabAreas();
		tabbedPane.addTab(translate("group.edit.tab.areas"), vc_tab_bgAreas);

		vc_tab_bgRights = createTabRights();
		tabbedPane.addTab(translate("group.edit.tab.rights"), vc_tab_bgRights);

		vc_tab_grpmanagement = createTabGroupManagement(ureq);
		tabbedPane.addTab(translate("group.edit.tab.members"), vc_tab_grpmanagement);

		resourceController = new BusinessGroupEditResourceController(ureq, getWindowControl(), currBusinessGroup);
		listenTo(resourceController);
  	tabbedPane.addTab(translate("group.edit.tab.resources"), resourceController.getInitialComponent());
		
		tabAccessCtrl = new BusinessGroupEditAccessController(ureq, getWindowControl(), currBusinessGroup);
	  listenTo(tabAccessCtrl);
	  tabAccessIndex = tabbedPane.addTab(translate("group.edit.tab.accesscontrol"), tabAccessCtrl.getInitialComponent());
	}
	
	/**
	 * Refresh Member-tab when waiting-list configuration change.
	 * @param ureq
	 */
	private void refreshAllTabs(UserRequest ureq) {
	  tabbedPane.removeAll();
		tabbedPane.addTab(translate("group.edit.tab.details"), vc_tab_bgDetails);
		tabbedPane.addTab(translate("group.edit.tab.collabtools"), vc_tab_bgCTools);
		tabbedPane.addTab(translate("group.edit.tab.areas"), vc_tab_bgAreas);
		tabbedPane.addTab(translate("group.edit.tab.rights"), vc_tab_bgRights);
		tabbedPane.addTab(translate("group.edit.tab.members"), createTabGroupManagement(ureq));
		tabbedPane.addTab(translate("group.edit.tab.resources"), resourceController.getInitialComponent());
		tabbedPane.addTab(translate("group.edit.tab.accesscontrol"), tabAccessCtrl.getInitialComponent());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == areasChoice) {
			if (event == Choice.EVNT_VALIDATION_OK) {
				updateGroupAreaRelations();
				// do loggin
				for (Iterator<BGArea> iter = selectedAreas.iterator(); iter.hasNext();) {
					BGArea area = iter.next();
					ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_AREA_UPDATED, getClass(),
							LoggingResourceable.wrap(area));
				}
				if (selectedAreas.size()==0) {
					ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_AREA_UPDATED_EMPTY, getClass());
				}
			}
		} else if (source == rightsChoice) {
			if (event == Choice.EVNT_VALIDATION_OK) {
				updateGroupRightsRelations();
				// do loggin
				for (Iterator<String> iter = selectedRights.iterator(); iter.hasNext();) {
					ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_RIGHT_UPDATED, getClass(),
							LoggingResourceable.wrapBGRight(iter.next()));
				}
				if (selectedRights.size()==0) {
					ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_RIGHT_UPDATED_EMPTY, getClass());
				}
				// notify current active users of this business group
				BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.GROUPRIGHTS_MODIFIED_EVENT, currBusinessGroup, null);
			}
		//fxdiff BAKS-7 Resume function
		} else if (source == tabbedPane && event instanceof TabbedPaneChangedEvent) {
			tabbedPane.addToHistory(ureq, getWindowControl());
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == dmsForm && event == Event.CHANGED_EVENT) {
			businessGroupService.updateDisplayMembers(currBusinessGroup,
					new DisplayMembers(dmsForm.getShowOwners(), dmsForm.getShowPartipiciants(), dmsForm.getShowWaitingList()));
			// notify current active users of this business group
			BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.CONFIGURATION_MODIFIED_EVENT, currBusinessGroup, null);
			// do loggin
			ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_CONFIGURATION_CHANGED, getClass());

		} else if (source == ctc) {
			if (event == Event.CHANGED_EVENT) {
				// notify current active users of this business group
				BusinessGroupModifiedEvent
						.fireModifiedGroupEvents(BusinessGroupModifiedEvent.CONFIGURATION_MODIFIED_EVENT, currBusinessGroup, null);
			}
		} else if (source == alreadyLockedDialogController) {
			//closed dialog box either by clicking ok, or closing the box
			if (event == Event.CANCELLED_EVENT || DialogBoxUIFactory.isOkEvent(event)) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		} else if(event instanceof IdentitiesAddEvent ) { 
			IdentitiesAddEvent identitiesAddedEvent = (IdentitiesAddEvent) event;
			BusinessGroupAddResponse response = null;
			addLoggingResourceable(LoggingResourceable.wrap(currBusinessGroup));
			if (source == ownerGrpCntrllr) {
			  response = businessGroupService.addOwners(ureq.getIdentity(), identitiesAddedEvent.getAddIdentities(), currBusinessGroup);
			} else if (source == partipGrpCntrllr) {
				response = businessGroupService.addParticipants(ureq.getIdentity(), identitiesAddedEvent.getAddIdentities(), currBusinessGroup);					
			} else if (source == waitingGruppeController) {
				response = businessGroupService.addToWaitingList(ureq.getIdentity(), identitiesAddedEvent.getAddIdentities(), currBusinessGroup);									
			}
			identitiesAddedEvent.setIdentitiesAddedEvent(response.getAddedIdentities());
			identitiesAddedEvent.setIdentitiesWithoutPermission(response.getIdentitiesWithoutPermission());
			identitiesAddedEvent.setIdentitiesAlreadyInGroup(response.getIdentitiesAlreadyInGroup());			
			fireEvent(ureq, Event.CHANGED_EVENT );
	  }	else if (event instanceof IdentitiesRemoveEvent) {
	  	List<Identity> identities = ((IdentitiesRemoveEvent) event).getRemovedIdentities();
			if (source == ownerGrpCntrllr) {
			  businessGroupService.removeOwners(ureq.getIdentity(), identities, currBusinessGroup);
			} else if (source == partipGrpCntrllr) {
			  businessGroupService.removeParticipants(ureq.getIdentity(), identities, currBusinessGroup);
			  if (currBusinessGroup.getWaitingListEnabled().booleanValue()) {
          // It is possible that a user is transfered from waiting-list to participants => reload data to see transfered user in right group.
			  	partipGrpCntrllr.reloadData();
			    waitingGruppeController.reloadData();
			  }
			} else if (source == waitingGruppeController) {
			  businessGroupService.removeFromWaitingList(ureq.getIdentity(), identities, currBusinessGroup);
			}
	  	fireEvent(ureq, Event.CHANGED_EVENT );
		} else if (source == waitingGruppeController) {
			if (event instanceof IdentitiesMoveEvent) {
				IdentitiesMoveEvent identitiesMoveEvent = (IdentitiesMoveEvent) event;
				BusinessGroupAddResponse response = businessGroupService.moveIdentityFromWaitingListToParticipant(identitiesMoveEvent.getChosenIdentities(), ureq.getIdentity(), currBusinessGroup);
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
					MailContext context = new MailContextImpl(currBusinessGroup, null, getWindowControl().getBusinessControl().getAsString());
					MailerResult mailerResult = mailer.sendMailAsSeparateMails(context, identitiesMoveEvent.getMovedIdentities(), null, null, mailTemplate, null);
					MailHelper.printErrorsAndWarnings(mailerResult, getWindowControl(), ureq.getLocale());
				}
				fireEvent(ureq, Event.CHANGED_EVENT );		
			} 
		//fxdiff VCRP-1,2: access control of resources
		} else if (source == this.modifyBusinessGroupController || source == tabAccessCtrl) {
			if (event == Event.DONE_EVENT) {
				// update business group with the specified values
				// values are taken from the modifyBusinessGroupForm
				currBusinessGroup = updateBusinessGroup();
				// inform index about change
				refreshAllTabs(ureq);
				// notify current active users of this business group
				BusinessGroupModifiedEvent
						.fireModifiedGroupEvents(BusinessGroupModifiedEvent.CONFIGURATION_MODIFIED_EVENT, this.currBusinessGroup, null);
				// rename the group also in the IM servers group list

				if (InstantMessagingModule.isEnabled()) {
					String groupID = InstantMessagingModule.getAdapter().createChatRoomString(this.currBusinessGroup);
					InstantMessagingModule.getAdapter().renameRosterGroup(groupID, this.modifyBusinessGroupController.getGroupName());
				}

				// do logging
				ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_CONFIGURATION_CHANGED, getClass());
				//fxdiff VCRP-1,2: access control of resources
				if(source == tabAccessCtrl) {
					tabbedPane.setSelectedPane(tabAccessIndex);
				}
				
			} else if (event == Event.CANCELLED_EVENT) {
				// reinit details form
				// TODO:fj:b introduce reset() for a form
				
				if (this.modifyBusinessGroupController != null) {
					removeAsListenerAndDispose(this.modifyBusinessGroupController);
				}
				modifyBusinessGroupController = new BusinessGroupFormController(ureq, getWindowControl(), currBusinessGroup);
				listenTo(modifyBusinessGroupController);
				vc_tab_bgDetails.put("businessGroupForm", this.modifyBusinessGroupController.getInitialComponent());

			}
		}
	}

	/**
	 * persist the updates
	 */
	private BusinessGroup updateBusinessGroup() {
		String bgName = modifyBusinessGroupController.getGroupName();
		String bgDesc = modifyBusinessGroupController.getGroupDescription();
		Integer bgMax = modifyBusinessGroupController.getGroupMax();
		Integer bgMin = modifyBusinessGroupController.getGroupMin();
		boolean waitingListEnabled = modifyBusinessGroupController.isWaitingListEnabled();
		boolean autoCloseRanksEnabled = modifyBusinessGroupController.isAutoCloseRanksEnabled();
		vc_tab_grpmanagement.contextPut("hasWaitingGrp", waitingListEnabled);
		return businessGroupService.updateBusinessGroup(currBusinessGroup, bgName, bgDesc,
				bgMin, bgMax, waitingListEnabled, autoCloseRanksEnabled);
	}

	/**
	 * Update the areas associated to this group: remove and add areas
	 */
	private void updateGroupAreaRelations() {
		// refresh group to prevent stale object exception and context proxy issues
		currBusinessGroup = businessGroupService.loadBusinessGroup(currBusinessGroup);
		// 1) add areas to group
		List<Integer> addedAreas = areasChoice.getAddedRows();
		for (Integer position:addedAreas) {
			BGArea area = areaDataModel.getObject(position.intValue());
			areaManager.addBGToBGArea(currBusinessGroup, area);
			selectedAreas.add(area);
		}
		// 2) remove areas from group
		List<Integer> removedAreas = areasChoice.getRemovedRows();
		for (Integer position: removedAreas) {
			BGArea area = areaDataModel.getObject(position.intValue());
			areaManager.removeBGFromArea(currBusinessGroup, area);
			selectedAreas.remove(area);
		}
	}

	/**
	 * Update the rights associated to this group: remove and add rights
	 */
	private void updateGroupRightsRelations() {
		// refresh group to prevent stale object exception and context proxy issues
		currBusinessGroup = businessGroupService.loadBusinessGroup(currBusinessGroup);
		// 1) add rights to group
		List<Integer> addedRights = rightsChoice.getAddedRows();
		for (Integer position: addedRights) {
			String right = rightDataModel.getObject(position.intValue());
			rightManager.addBGRight(right, currBusinessGroup);
			selectedRights.add(right);
		}
		// 2) remove rights from group
		List<Integer> removedRights = rightsChoice.getRemovedRows();
		for (Integer position:removedRights) {
			String right = rightDataModel.getObject(position.intValue());
			rightManager.removeBGRight(right, currBusinessGroup);
			selectedRights.remove(right);
		}
	}

	/**
	 * get a CollaborationToolController via CollaborationToolsFactory, the CTC is
	 * initialised with the UserRequest. The CTC provides a List of Collaboration
	 * Tools which can be enabled and disabled (so far) through checkboxes.
	 * 
	 * @param ureq
	 */
	private VelocityContainer createTabCollabTools(UserRequest ureq) {
		VelocityContainer tmp = createVelocityContainer("tab_bgCollabTools");
		CollaborationTools ctsm = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(currBusinessGroup);
		ctc = ctsm.createCollaborationToolsSettingsController(ureq, getWindowControl());
		// we are listening on CollaborationToolsSettingsController events
		// which are just propagated to our attached controllerlistener...
		// e.g. the BusinessGroupMainRunController, updating the MenuTree
		// if a CollaborationToolsSetting has changed... so far this means
		// enabling/disabling a Tool within the tree.
		listenTo(ctc);
		tmp.put("collabTools", ctc.getInitialComponent());
		tmp.contextPut("type", this.currBusinessGroup.getType());
		return tmp;
	}

	/**
	 * adds a area-to-group tab to the tabbed pane
	 */
	private VelocityContainer createTabAreas() {
		VelocityContainer tmp = createVelocityContainer("tab_bgAreas");
		List<BGArea> allAreas = areaManager.findBGAreasOfBusinessGroup(currBusinessGroup);
		selectedAreas = areaManager.findBGAreasOfBusinessGroup(currBusinessGroup);
		areaDataModel = new AreasToGroupDataModel(allAreas, selectedAreas);

		areasChoice = new Choice("areasChoice", getTranslator());
		areasChoice.setSubmitKey("submit");
		areasChoice.setCancelKey("cancel");
		areasChoice.setTableDataModel(areaDataModel);
		areasChoice.addListener(this);
		tmp.put("areasChoice", areasChoice);
		tmp.contextPut("noAreasFound", (allAreas.size() > 0 ? Boolean.FALSE : Boolean.TRUE));
		tmp.contextPut("isGmAdmin", Boolean.TRUE);
		tmp.contextPut("type", currBusinessGroup.getType());
		return tmp;
	}

	/**
	 * adds a right-to-group tab to the tabbed pane
	 */
	private VelocityContainer createTabRights() {
		VelocityContainer tmp = createVelocityContainer("tab_bgRights");
		selectedRights = rightManager.findBGRights(currBusinessGroup);
		rightDataModel = new RightsToGroupDataModel(bgRights, selectedRights);

		rightsChoice = new Choice("rightsChoice", getTranslator());
		rightsChoice.setSubmitKey("submit");
		rightsChoice.setCancelKey("cancel");
		rightsChoice.setTableDataModel(this.rightDataModel);
		rightsChoice.addListener(this);
		tmp.put("rightsChoice", rightsChoice);
		tmp.contextPut("type", this.currBusinessGroup.getType());
		return tmp;
	}

	/**
	 * @param businessGroup
	 */
	private VelocityContainer createTabDetails(UserRequest ureq, BusinessGroup businessGroup) {
		VelocityContainer tmp = createVelocityContainer("tab_bgDetail");
		
		removeAsListenerAndDispose(modifyBusinessGroupController);
		modifyBusinessGroupController = new BusinessGroupFormController(ureq, getWindowControl(), businessGroup);
		listenTo(this.modifyBusinessGroupController);
		
		tmp.put("businessGroupForm", modifyBusinessGroupController.getInitialComponent());
		tmp.contextPut("BuddyGroup", businessGroup);
		tmp.contextPut("type", currBusinessGroup.getType());
		tmp.contextPut("groupid", currBusinessGroup.getKey());
		return tmp;
	}

	/**
	 * @param ureq
	 */
	private VelocityContainer createTabGroupManagement(UserRequest ureq) {

		boolean hasWaitingList = currBusinessGroup.getWaitingListEnabled().booleanValue();
		//
		VelocityContainer tmp = createVelocityContainer("tab_bgGrpMngmnt");
		// Member Display Form, allows to enable/disable that others partips see
		// partips and/or owners
		//
		DisplayMembers displayMembers = businessGroupService.getDisplayMembers(currBusinessGroup);
		// configure the form with checkboxes for owners and/or partips according
		// the booleans
		removeAsListenerAndDispose(dmsForm);
		dmsForm = new DisplayMemberSwitchForm(ureq, getWindowControl(), true, true, hasWaitingList);
		listenTo(dmsForm);
		// set if the checkboxes are checked or not.
		dmsForm.setShowOwnersChecked(displayMembers.isShowOwners());
		dmsForm.setShowPartipsChecked(displayMembers.isShowParticipants());
		if (hasWaitingList) dmsForm.setShowWaitingListChecked(displayMembers.isShowWaitingList());
		
		tmp.put("displayMembers", dmsForm.getInitialComponent());
		Roles roles = ureq.getUserSession().getRoles();
		boolean enableTablePreferences = roles.isOLATAdmin() || roles.isGroupManager();
		boolean requiresOwner = true;
		// groupcontroller which allows to remove all members depending on
		// configuration.
		removeAsListenerAndDispose(ownerGrpCntrllr);
		ownerGrpCntrllr = new GroupController(ureq, getWindowControl(), true, requiresOwner, enableTablePreferences, currBusinessGroup.getOwnerGroup());
		listenTo(ownerGrpCntrllr);
		// add mail templates used when adding and removing users
		MailTemplate ownerAddUserMailTempl = BGMailHelper.createAddParticipantMailTemplate(currBusinessGroup, ureq.getIdentity());
		ownerGrpCntrllr.setAddUserMailTempl(ownerAddUserMailTempl,true);
		MailTemplate ownerAremoveUserMailTempl = BGMailHelper.createRemoveParticipantMailTemplate(currBusinessGroup, ureq.getIdentity());
		ownerGrpCntrllr.setRemoveUserMailTempl(ownerAremoveUserMailTempl,true);
		// expose to velocity
		tmp.put("ownerGrpMngmnt", ownerGrpCntrllr.getInitialComponent());
		tmp.contextPut("hasOwnerGrp", Boolean.TRUE);

		// groupcontroller which allows to remove all members
		removeAsListenerAndDispose(partipGrpCntrllr);
		partipGrpCntrllr = new GroupController(ureq, getWindowControl(), true, false, enableTablePreferences, currBusinessGroup.getPartipiciantGroup());
		listenTo(partipGrpCntrllr);
		
		// add mail templates used when adding and removing users
		MailTemplate partAddUserMailTempl = BGMailHelper.createAddParticipantMailTemplate(currBusinessGroup, ureq.getIdentity());
		partipGrpCntrllr.setAddUserMailTempl(partAddUserMailTempl,true);
		MailTemplate partAremoveUserMailTempl = BGMailHelper.createRemoveParticipantMailTemplate(currBusinessGroup, ureq.getIdentity());
		partipGrpCntrllr.setRemoveUserMailTempl(partAremoveUserMailTempl,true);
		// expose to velocity
		tmp.put("partipGrpMngmnt", partipGrpCntrllr.getInitialComponent());
		tmp.contextPut("type", this.currBusinessGroup.getType());  
		
		// Show waiting list only if enabled 
		if (hasWaitingList) {
	    // waitinglist-groupcontroller which allows to remove all members
			SecurityGroup waitingList = currBusinessGroup.getWaitingGroup();
			removeAsListenerAndDispose(waitingGruppeController);
			waitingGruppeController = new WaitingGroupController(ureq, getWindowControl(), true, false, enableTablePreferences, waitingList );
			listenTo(waitingGruppeController);

			// add mail templates used when adding and removing users
			MailTemplate waitAddUserMailTempl = BGMailHelper.createAddWaitinglistMailTemplate(currBusinessGroup, ureq.getIdentity());
			waitingGruppeController.setAddUserMailTempl(waitAddUserMailTempl,true);
			MailTemplate waitRemoveUserMailTempl = BGMailHelper.createRemoveWaitinglistMailTemplate(currBusinessGroup, ureq.getIdentity());
			waitingGruppeController.setRemoveUserMailTempl(waitRemoveUserMailTempl,true);
			MailTemplate waitTransferUserMailTempl = BGMailHelper.createWaitinglistTransferMailTemplate(currBusinessGroup, ureq.getIdentity());
			waitingGruppeController.setTransferUserMailTempl(waitTransferUserMailTempl);
			// expose to velocity
			tmp.put("waitingGrpMngmnt", waitingGruppeController.getInitialComponent());
			tmp.contextPut("hasWaitingGrp", Boolean.TRUE);
		} else {
			tmp.contextPut("hasWaitingGrp", Boolean.FALSE);
		}
		return tmp;
	}

	/**
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {
		if (event instanceof OLATResourceableJustBeforeDeletedEvent) {
			OLATResourceableJustBeforeDeletedEvent delEvent = (OLATResourceableJustBeforeDeletedEvent) event;
			if (!delEvent.targetEquals(currBusinessGroup)) throw new AssertException(
					"receiving a delete event for a olatres we never registered for!!!:" + delEvent.getDerivedOres());
			dispose();
		} 
	}
	
	@Override
	//fxdiff BAKS-7 Resume function
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty() || tabbedPane == null) return;
		tabbedPane.activate(ureq, entries, state);
	}

	/**
	 * @return true if lock on group has been acquired, flase otherwhise
	 */
	public boolean isLockAcquired() {
		return lockEntry.isSuccess();
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean asynchronous)
	 */
	@Override
	protected void doDispose() {
		if(currBusinessGroup != null) {
			CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, currBusinessGroup);
			//release lock on dispose
			releaseBusinessGroupEditLock();
		}
	}

	private void releaseBusinessGroupEditLock() {
		if(lockEntry.isSuccess()){
			// release lock
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
		}else if(alreadyLockedDialogController != null){
			//dispose if dialog still visible
			alreadyLockedDialogController.dispose();
		}
	}
}