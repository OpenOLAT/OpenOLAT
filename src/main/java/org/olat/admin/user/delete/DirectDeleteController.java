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
import java.util.Collections;
import java.util.List;

import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller for 'Direct User Deletion' tab.
 * @author guretzki
 */
public class DirectDeleteController extends BasicController {

	private VelocityContainer myContent;

	private UserListForm userListForm;
	private BulkDeleteController bdc;
	private CloseableModalController cmc;
	private DeletableUserSearchController usc;
	private ConfirmDeleteUserController deleteConfirmCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserDeletionManager userDeletionManager;
	
	public DirectDeleteController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		myContent = createVelocityContainer("directdelete");

		initializeUserSearchController(ureq);
		initializeUserListForm(ureq);
		
		putInitialPanel(myContent);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	/**
	 * This dispatches controller events...
	 * 
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == usc) {
			if (event == Event.CANCELLED_EVENT) {
				removeAsListenerAndDispose(usc);	
			} else if (event instanceof MultiIdentityChosenEvent) {
				MultiIdentityChosenEvent multiEvent = (MultiIdentityChosenEvent) event;
				List<Identity> toDelete = multiEvent.getChosenIdentities();
				if (toDelete.isEmpty()) {
					showError("msg.selectionempty");
				} else {
					doConfirmDelete(ureq, toDelete);
				}
			} else if (event instanceof SingleIdentityChosenEvent) {
				// single choose event may come from autocompleter user search
				SingleIdentityChosenEvent uce = (SingleIdentityChosenEvent) event;
				doConfirmDelete(ureq, Collections.singletonList(uce.getChosenIdentity()));
			}
		} else if (source == bdc) {
			List<Identity>  toDelete = bdc.getToDelete();
			cmc.deactivate();
			cleanUp();
			doConfirmDelete(ureq, toDelete);
		} else if (source == deleteConfirmCtrl) {
			if (event == Event.DONE_EVENT) {
				boolean success = doDeleteIdentities(deleteConfirmCtrl.getToDelete());
				if (bdc != null) {
					bdc.sendMail();
				}	
				initializeUserSearchController(ureq);
				initializeUserListForm(ureq);
				if (success) {
					showInfo("deleted.users.msg");					
				}
			}
			cmc.deactivate();
			cleanUp();
		}  else if(cmc == source) {
			cleanUp();
		} else if (source == userListForm) {
			doBulkDelete(ureq);
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(deleteConfirmCtrl);
		removeAsListenerAndDispose(bdc);
		removeAsListenerAndDispose(cmc);
		deleteConfirmCtrl = null;
		bdc = null;
		cmc = null;
	}
	
	private void doConfirmDelete(UserRequest ureq, List<Identity> toDelete) {
		deleteConfirmCtrl = new ConfirmDeleteUserController(ureq, getWindowControl(), toDelete);
		listenTo(deleteConfirmCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteConfirmCtrl.getInitialComponent(), true, "");
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doBulkDelete(UserRequest ureq) {
		bdc = new BulkDeleteController(ureq, getWindowControl(), userListForm.getLogins(), userListForm.getReason());
		listenTo(bdc);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), bdc.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}
	
	private boolean doDeleteIdentities(List<Identity> toDeleteIdentities) {
		boolean totalSuccess = true;
		for (int i = 0; i < toDeleteIdentities.size(); i++) {
			Identity identity = toDeleteIdentities.get(i);
			boolean success = userDeletionManager.deleteIdentity(identity);
			if (success) {
				dbInstance.intermediateCommit();								
			} else {
				totalSuccess = false;
				showError("error.delete", userManager.getUserDisplayName(identity));
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
}
