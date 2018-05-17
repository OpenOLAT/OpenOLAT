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

import java.util.ArrayList;
import java.util.List;

import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.user.UserManager;

/**
 * Controller for 'Direct User Deletion' tab.
 * @author guretzki
 */
public class DirectDeleteController extends BasicController {

	private VelocityContainer myContent;

	private DeletableUserSearchController usc;
	private DialogBoxController deleteConfirmController;
	private List<Identity> toDelete;
	private UserListForm userListForm;
	private BulkDeleteController bdc;
	private CloseableModalController cmc;
	
	private final UserManager userManager;
	
	public DirectDeleteController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		userManager = UserManager.getInstance();
		
		myContent = createVelocityContainer("directdelete");

		initializeUserSearchController(ureq);
		initializeUserListForm(ureq);
		
		putInitialPanel(myContent);
	}

	/**
	 * This dispatches component events...
	 * 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	/**
	 * This dispatches controller events...
	 * 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller sourceController, Event event) {
		if (sourceController == usc) {
			if (event == Event.CANCELLED_EVENT) {
				removeAsListenerAndDispose(usc);
				
			} else if (event instanceof MultiIdentityChosenEvent) {
				MultiIdentityChosenEvent multiEvent = (MultiIdentityChosenEvent) event;
				toDelete = multiEvent.getChosenIdentities();
				if (toDelete.size() == 0) {
					showError("msg.selectionempty");
					return;
				}
				String names = buildUserNameList(toDelete);
				deleteConfirmController = activateOkCancelDialog(ureq, null, translate("readyToDelete.delete.confirm", names), deleteConfirmController);
				return;
			} else if (event instanceof SingleIdentityChosenEvent) {
				// single choose event may come from autocompleter user search
				SingleIdentityChosenEvent uce = (SingleIdentityChosenEvent) event;
				toDelete = new ArrayList<Identity>();
				toDelete.add(uce.getChosenIdentity());
				
				String fullname = userManager.getUserDisplayName(uce.getChosenIdentity());
				deleteConfirmController = activateOkCancelDialog(ureq, null, translate("readyToDelete.delete.confirm", fullname), deleteConfirmController);
				return;
			} else {
				throw new AssertException("unknown event ::" + event.getCommand());
			}
		} else if (sourceController == deleteConfirmController) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				boolean success = deleteIdentities(toDelete);
				if (bdc != null) {
					bdc.sendMail();
				}
				
				initializeUserSearchController(ureq);
				initializeUserListForm(ureq);
				
				if (success) {
					showInfo("deleted.users.msg");					
				}
				// else error already shown
			}
		} else if (sourceController == bdc) {
			toDelete = bdc.getToDelete();
			cmc.deactivate();
			
			String names = buildUserNameList(toDelete);
			deleteConfirmController = activateOkCancelDialog(ureq, null, translate("readyToDelete.delete.confirm", names), deleteConfirmController);
		} else if (sourceController == cmc) {
			if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();		
			}
		} else if (sourceController == userListForm) {
			
			removeAsListenerAndDispose(bdc);
			bdc = new BulkDeleteController(ureq, getWindowControl(), userListForm.getLogins(), userListForm.getReason());
			listenTo(bdc);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), bdc.getInitialComponent());
			listenTo(cmc);
			
			cmc.activate();
		}
	}

	/**
	 * Build comma separated list of usernames.
	 * @param toDelete
	 * @return
	 */
	private String buildUserNameList(List<Identity> toDeleteIdentities) {
		StringBuilder buf = new StringBuilder();
		for (Identity identity : toDeleteIdentities) {
			if (buf.length() > 0) {
				buf.append(", ");
			}
			buf.append(userManager.getUserDisplayName(identity));
		}
		return buf.toString();
	}

	private boolean deleteIdentities(List<Identity> toDeleteIdentities) {
		boolean totalSuccess = true;
		for (int i = 0; i < toDeleteIdentities.size(); i++) {
			Identity identity = toDeleteIdentities.get(i);
			boolean success = UserDeletionManager.getInstance().deleteIdentity(identity);
			if (success) {
				DBFactory.getInstance().intermediateCommit();								
			} else {
				totalSuccess = false;
				showError("error.delete", identity.getName());
			}
		}
		return totalSuccess;
	}

	private void initializeUserSearchController(UserRequest ureq) {
		removeAsListenerAndDispose(usc);
		usc = new DeletableUserSearchController(ureq, getWindowControl());
		listenTo(usc);
		myContent.put("usersearch", usc.getInitialComponent());
		myContent.contextPut("deletedusers", new ArrayList<Identity>());		
	}
	
	private void initializeUserListForm(UserRequest ureq) {
		myContent.contextRemove("userlist");
		removeAsListenerAndDispose(userListForm);
		userListForm = new UserListForm(ureq, getWindowControl());
		listenTo(userListForm);
		myContent.put("userlist", userListForm.getInitialComponent());
	}

	protected void doDispose() {
		//
	}

}
