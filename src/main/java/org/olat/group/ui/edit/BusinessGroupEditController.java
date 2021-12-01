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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupLifecycleManager;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupStatusEnum;
import org.olat.group.GroupLoggingAction;
import org.olat.group.ui.BGControllerFactory;
import org.olat.group.ui.lifecycle.BusinessGroupStatusController;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: <BR>
 * This controller displays a tabbed pane that lets the user configure and
 * modify a business group.
 * <P>
 * Fires BusinessGroupModifiedEvent via the OLATResourceableEventCenter
 * <P>
 * Initial Date: Aug 17, 2004
 * 
 * @author patrick, srosse
 */
public class BusinessGroupEditController extends BasicController implements GenericEventListener, Activateable2 {

	private BusinessGroup currBusinessGroup;

	private TabbedPane tabbedPane;
	private VelocityContainer mainVC;
	private final TooledStackedPanel toolbarPanel;

	private LockResult lockEntry;
	private DialogBoxController alreadyLockedDialogController;

	//controllers in tabs
	private BusinessGroupEditDetailsController editDetailsController;
	private BusinessGroupToolsController collaborationToolsController;
	private BusinessGroupMembersController membersController;
	private BusinessGroupEditResourceController resourceController;
	private BusinessGroupEditAccessController tabAccessCtrl;
	private BusinessGroupStatusController lifecycleCtrl;
	
	private int membersTab;
	private final String type;

	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupLifecycleManager businessGroupLifecycleManager;

	/**
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param currBusinessGroup The business group to edit
	 * @param configurationFlags Flags to configure the controllers features. The
	 *          controller does no type specific stuff implicit just by looking at
	 *          the group type. Type specific features must be flagged.
	 */
	public BusinessGroupEditController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel, BusinessGroup businessGroup) {
		super(ureq, wControl);
		this.toolbarPanel = toolbarPanel;
		type = businessGroup.getTechnicalType();
		
		// OLAT-4955: setting the stickyActionType here passes it on to any controller defined in the scope of the editor,
		//            basically forcing any logging action called within the bg editor to be of type 'admin'
		getUserActivityLogger().setStickyActionType(ActionType.admin);
		addLoggingResourceable(LoggingResourceable.wrap(businessGroup));

		// Initialize translator:
		setTranslator(Util.createPackageTranslator(BGControllerFactory.class, getLocale(), getTranslator()));

		// try to acquire edit lock on business group
		String locksubkey = "groupEdit";
		lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(businessGroup, ureq.getIdentity(), locksubkey, getWindow());
		if (lockEntry.isSuccess()) {
			// reload group to minimize stale object exception and update last usage timestamp
			boolean groupOwner = businessGroupService.isIdentityInBusinessGroup(getIdentity(), businessGroup.getKey(), true, false, null);
			if(groupOwner) {
				currBusinessGroup = businessGroupService.setLastUsageFor(getIdentity(), businessGroup);
			} else {
				currBusinessGroup = businessGroupService.loadBusinessGroup(businessGroup);
			}
			if(currBusinessGroup == null) {
				VelocityContainer vc = createVelocityContainer("deleted");
				vc.contextPut("name", businessGroup.getName());
				putInitialPanel(vc);
			} else {
				// add as listener to BusinessGroup so we are being notified about changes.
				CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), currBusinessGroup);
	
				//create some controllers
				editDetailsController = new BusinessGroupEditDetailsController(ureq, getWindowControl(), businessGroup);
				listenTo(editDetailsController);

				tabbedPane = new TabbedPane("bgTabbs", ureq.getLocale());
				tabbedPane.addListener(this);
				setAllTabs(ureq);
				mainVC = createVelocityContainer("edit");
				mainVC.put("tabbedpane", tabbedPane);
				updateContainer();
				putInitialPanel(mainVC);
			}
		} else {
			//lock was not successful
			alreadyLockedDialogController = DialogBoxUIFactory.createResourceLockedMessage(ureq, wControl, lockEntry, "error.message.locked", getTranslator());
			listenTo(alreadyLockedDialogController);
			alreadyLockedDialogController.activate();
			putInitialPanel(new Panel("empty"));
		}
	}
	
	public BusinessGroup getBusinessGroup() {
		return currBusinessGroup;
	}
	
	private void updateContainer() {
		if(mainVC == null) return;
		
		String[] title = new String[] { StringHelper.escapeHtml(currBusinessGroup.getName()) };
		mainVC.contextPut("title", translate("group.edit.title", title));
		
		BusinessGroupStatusEnum status = currBusinessGroup.getGroupStatus();
		
		String value = translate("status." + status.name());
		boolean mailSent = false;
		if(status == BusinessGroupStatusEnum.active) {
			mailSent = businessGroupLifecycleManager.getInactivationEmailDate(currBusinessGroup) != null;	
		} else if(status == BusinessGroupStatusEnum.inactive) {
			mailSent = businessGroupLifecycleManager.getSoftDeleteEmailDate(currBusinessGroup) != null;
		}
		
		if(mailSent) {
			value += " - " + translate("status.within.reactiontime");
		}
		mainVC.contextPut("status", value);
		mainVC.contextPut("statusCssClass", "o_businessgroup_status_" + status);
	}
	
	/**
	 * Learning areas and and course rights should only appear when at least one course is associated.</br>
	 * <ul><li>
	 * a) No courses associated and user is not author</br>
	 * Description, Tools, Members, Publishing and booking
	 * </li><li>
	 * b) No course associated and user is author:</br>
	 * Description, Tools, Members, Courses, Publishing and booking
	 * </li><li>
	 * c) With courses associated:</br>
	 * Description, Tools, Members, Courses, Learning areas, Course rights, Publishing and booking 
	 * 
	 * @param ureq
	 */
	private void setAllTabs(UserRequest ureq) {
		tabAccessCtrl = getAccessController(ureq);
		int currentSelectedPane = tabbedPane.getSelectedPane();

		tabbedPane.removeAll();
		
		editDetailsController.setAllowWaitingList(tabAccessCtrl == null || !tabAccessCtrl.isPaymentMethodInUse());
		tabbedPane.addTab(translate("group.edit.tab.details"), editDetailsController.getInitialComponent());
		tabbedPane.addTab(ureq, translate("group.edit.tab.collabtools"), uureq -> {
				collaborationToolsController = new BusinessGroupToolsController(uureq, getWindowControl(), currBusinessGroup);
				listenTo(collaborationToolsController);
				return collaborationToolsController.getInitialComponent();
			});
		
		membersTab = tabbedPane.addTab(ureq, translate("group.edit.tab.members"), uureq -> {
				if(membersController == null) {
					membersController = new BusinessGroupMembersController(uureq, getWindowControl(), toolbarPanel, currBusinessGroup);
					listenTo(membersController);
				} else {
					membersController.updateBusinessGroup(currBusinessGroup);
				}
				return membersController.getInitialComponent();
			});
		
		//resources (optional)
		Roles roles = ureq.getUserSession().getRoles();
		boolean resourceEnabled = roles.isAdministrator() || roles.isGroupManager() || roles.isAuthor()
				|| businessGroupService.hasResources(currBusinessGroup);
		if(resourceEnabled && BusinessGroup.BUSINESS_TYPE.equals(type)) {
			tabbedPane.addTab(ureq, translate("group.edit.tab.resources"), uureq -> {
				if(resourceController == null) {
					resourceController = new BusinessGroupEditResourceController(uureq, getWindowControl(), currBusinessGroup);
					listenTo(resourceController);
				}
				return resourceController.getInitialComponent();
			});
		} else {
			removeAsListenerAndDispose(resourceController);
			resourceController = null;
		}

		if(tabAccessCtrl != null) {
			tabbedPane.addTab(ureq, translate("group.edit.tab.accesscontrol"), uureq -> tabAccessCtrl.getInitialComponent());
		}
		
		tabbedPane.addTab(ureq, translate("group.edit.tab.lifecycle"), uureq -> {
			removeControllerListener(lifecycleCtrl);
			// always a new up-to-date one
			lifecycleCtrl = new BusinessGroupStatusController(uureq, getWindowControl(), currBusinessGroup);
			listenTo(lifecycleCtrl);
			return lifecycleCtrl.getInitialComponent();
		});

		if(currentSelectedPane > 0) {
			if(currentSelectedPane < tabbedPane.getTabCount()) {
				tabbedPane.setSelectedPane(ureq, currentSelectedPane);
			} else {
				// the last tab can modify the list of tabs
				tabbedPane.setSelectedPane(ureq, tabbedPane.getTabCount() - 1);
			}
		}
	}

	private BusinessGroupEditAccessController getAccessController(UserRequest ureq) {
		if(tabAccessCtrl == null && acModule.isEnabled() && BusinessGroup.BUSINESS_TYPE.equals(type)) { 
			tabAccessCtrl = new BusinessGroupEditAccessController(ureq, getWindowControl(), currBusinessGroup);
			if(BusinessGroupManagedFlag.isManaged(currBusinessGroup, BusinessGroupManagedFlag.bookings)
					&& tabAccessCtrl.getNumOfBookingConfigurations() == 0) {
				//booking is managed, no booking, don't show it
				tabAccessCtrl = null;
			} else {
				listenTo(tabAccessCtrl);
			}
		}
		if(tabAccessCtrl != null) {
			tabAccessCtrl.updateBusinessGroup(currBusinessGroup);
		}
		return tabAccessCtrl;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		 if (source == tabbedPane && event instanceof TabbedPaneChangedEvent) {
			tabbedPane.addToHistory(ureq, getWindowControl());
			if(tabbedPane.getSelectedPane() == membersTab) {
				membersController.updateBusinessGroup(currBusinessGroup);
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == collaborationToolsController) {
			if (event == Event.CHANGED_EVENT) {
				fireEvent(ureq, event);
				// notify current active users of this business group
				BusinessGroupModifiedEvent
						.fireModifiedGroupEvents(BusinessGroupModifiedEvent.CONFIGURATION_MODIFIED_EVENT, currBusinessGroup, null, getIdentity());
			}
		} else if (source == alreadyLockedDialogController) {
			//closed dialog box either by clicking ok, or closing the box
			if (event == Event.CANCELLED_EVENT || DialogBoxUIFactory.isOkEvent(event)) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		} else if (source == editDetailsController) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				//reload the business group
				currBusinessGroup = editDetailsController.getGroup();
				fireEvent(ureq, event);
				
				// inform index about change
				setAllTabs(ureq);
				// notify current active users of this business group
				BusinessGroupModifiedEvent
						.fireModifiedGroupEvents(BusinessGroupModifiedEvent.CONFIGURATION_MODIFIED_EVENT, currBusinessGroup, null, getIdentity());
				// do logging
				ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_CONFIGURATION_CHANGED, getClass());
			}
		} else if (source == membersController) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				//reload the business group
				currBusinessGroup = membersController.getGroup();
				fireEvent(ureq, event);
			}
		} else if (source == tabAccessCtrl || source == resourceController) {
			setAllTabs(ureq);
			fireEvent(ureq, event);
		} else if (source == lifecycleCtrl) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				currBusinessGroup = lifecycleCtrl.getBusinessGroup();
				setAllTabs(ureq);
				updateContainer();
				fireEvent(ureq, event);
			} else if(event == Event.CLOSE_EVENT) {
				fireEvent(ureq, event);
			}
		}
	}

	@Override
	public void event(Event event) {
		if (event instanceof OLATResourceableJustBeforeDeletedEvent) {
			OLATResourceableJustBeforeDeletedEvent delEvent = (OLATResourceableJustBeforeDeletedEvent) event;
			if (!delEvent.targetEquals(currBusinessGroup)) throw new AssertException(
					"receiving a delete event for a olatres we never registered for!!!:" + delEvent.getDerivedOres());
			dispose();
		} 
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty() || tabbedPane == null) return;
		tabbedPane.activate(ureq, entries, state);
	}

	/**
	 * @return true if lock on group has been acquired, false otherwhise
	 */
	public boolean isLockAcquired() {
		return lockEntry.isSuccess();
	}

	@Override
	protected void doDispose() {
		if(currBusinessGroup != null) {
			CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, currBusinessGroup);
			//release lock on dispose
			releaseBusinessGroupEditLock();
		}
        super.doDispose();
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