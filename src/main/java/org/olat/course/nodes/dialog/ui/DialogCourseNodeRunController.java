/**
* OLAT - Online Learning and Training<br>
* https://www.olat.org
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
* <a href="https://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.dialog.ui;

import java.util.List;

import org.olat.core.commons.controllers.linkchooser.LinkChooserController;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.nodes.DialogCourseNode;
import org.olat.course.nodes.dialog.DialogElement;
import org.olat.course.nodes.dialog.DialogElementsManager;
import org.olat.course.nodes.dialog.DialogSecurityCallback;
import org.olat.course.nodes.dialog.security.SecurityCallbackFactory;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: 03.11.2005 <br>
 * 
 * @author guido
 */
public class DialogCourseNodeRunController extends BasicController implements Activateable2 {

	private static final String FORUM = "forum";
	private static final String DELETE = "delete";

	private final Link backButton;
	private Link deleteLink;
	private final VelocityContainer mainVC;

	private final DialogCourseNode courseNode;
	private final RepositoryEntry entry;
	private final UserCourseEnvironment userCourseEnv;
	private final DialogSecurityCallback secCallback;

	private CloseableModalController cmc;
	private LinkChooserController fileCopyCtrl;
	private DialogElementController dialogElementCtrl;
	private final DialogElementListController dialogElementListCtrl;
	private ContextualSubscriptionController csCtrl;

	private DialogElement element;

	@Autowired
	private ForumManager forumManager;
	@Autowired
	private DialogElementsManager dialogElmsMgr;

	public DialogCourseNodeRunController(UserRequest ureq, WindowControl wControl, DialogCourseNode courseNode,
			UserCourseEnvironment userCourseEnv, DialogSecurityCallback secCallback) {
		super(ureq, wControl);
		this.userCourseEnv = userCourseEnv;
		this.courseNode = courseNode;
		this.secCallback = secCallback;
		this.entry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();

		addLoggingResourceable(LoggingResourceable.wrap(courseNode));

		mainVC = createVelocityContainer("dialog");
		
		if (!userCourseEnv.getIdentityEnvironment().getRoles().isGuestOnly()) {
			SubscriptionContext subsContext = CourseModule.createSubscriptionContext(userCourseEnv.getCourseEnvironment(), courseNode);
			secCallback = SecurityCallbackFactory.create(secCallback, subsContext);
		}
		if (secCallback.getSubscriptionContext() != null) {
			String businessPath = "[RepositoryEntry:" +entry.getKey() + "][CourseNode:" + courseNode.getIdent() + "]";
			PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(DialogElement.class), "", businessPath);
			csCtrl = new ContextualSubscriptionController(ureq, getWindowControl(), secCallback.getSubscriptionContext(), pdata);
			listenTo(csCtrl);
			mainVC.put("subscription", csCtrl.getInitialComponent());
		}
		
		backButton = LinkFactory.createLinkBack(mainVC, this);

		dialogElementListCtrl = new DialogElementListController(ureq, getWindowControl(), userCourseEnv, courseNode, secCallback, true);
		listenTo(dialogElementListCtrl);
		mainVC.put("files", dialogElementListCtrl.getInitialComponent());
		putInitialPanel(mainVC);
	}

	/**
	 * Toolbar for deleting/archiving current/selected dialog file
	 */
	private void initToolbar() {
		boolean isCurrentUserCreator = element.getAuthor().equals(getIdentity());
		boolean canEditDialog = isCurrentUserCreator || secCallback.mayEditMessageAsModerator();

		if(canEditDialog) {
			Dropdown actionDropdown = new Dropdown("actionTools", null, false, getTranslator());
			actionDropdown.setButton(true);
			actionDropdown.setEmbbeded(true);
			actionDropdown.setCarretIconCSS("o_icon o_icon_actions o_icon-lg");
			actionDropdown.setElementCssClass("dropdown-menu-right");

			deleteLink = LinkFactory.createLink(DELETE, DELETE, getTranslator(), mainVC, this, Link.LINK);
			deleteLink.setDomReplacementWrapperRequired(false);
			deleteLink.setIconLeftCSS("o_icon o_icon_delete_item");
			actionDropdown.addComponent(deleteLink);
			mainVC.put("actionDropdown", actionDropdown);
		} else {
			mainVC.remove("actionDropdown");
		}
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		String name = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("CourseNode".equals(name)) {
			back(ureq);
		} else if("Message".equals(name)) {
			back(ureq);
			activateByMessage(ureq, entries);
		} else if("Element".equals(name)) {
			back(ureq);
			activateByDialogElement(ureq, entries.get(0).getOLATResourceable().getResourceableId());
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		// backLink got triggered
		if (event == Event.BACK_EVENT) {
			back(ureq);
		} else if (event instanceof DialogRunEvent dre) {
			// in case of file got uploaded or copied successfully
			if (dre.getElement() != null) {
				element = dre.getElement();
				doDialog(ureq);
			}
		} else if(dialogElementListCtrl == source) {
			// case when specific dialog file entry gets selected
			if(event instanceof SelectRowEvent sre) {
				element = dialogElmsMgr.getDialogElementByKey(sre.getRow().getDialogElementKey());
				doDialog(ureq);
			}
		} else if (source == cmc) {
			cleanUp();
		} 
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(fileCopyCtrl);
		removeAsListenerAndDispose(cmc);
		fileCopyCtrl = null;
		cmc = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(backButton == source) {
			back(ureq);
		} else if (deleteLink == source && (source instanceof Link link)) {
			String cmd = link.getCommand();
			if (DELETE.equals(cmd)) {
				dialogElementListCtrl.doConfirmDelete(ureq, element);
			}
		}
	}
	
	private void back(UserRequest ureq) {
		mainVC.remove(FORUM);
		if(dialogElementCtrl != null) {
			dialogElementListCtrl.loadModel();
			removeAsListenerAndDispose(dialogElementCtrl);
			dialogElementCtrl = null;
		}
		addToHistory(ureq);
	}
	
	private void activateByMessage(UserRequest ureq, List<ContextEntry> entries) {
		if(entries == null || entries.isEmpty()) return;
		
		Long messageKey = entries.get(0).getOLATResourceable().getResourceableId();
		Message message = forumManager.getMessageById(messageKey);
		if(message == null) return;
			
		element = dialogElmsMgr.getDialogElementByForum(message.getForum().getKey());
		if(!checkAccess(element)) {
			return;
		}
		initToolbar();
		dialogElementCtrl = new DialogElementController(ureq, getWindowControl(), element, userCourseEnv, courseNode, secCallback);
		listenTo(dialogElementCtrl);
		mainVC.put(FORUM, dialogElementCtrl.getInitialComponent());
		//activate message
		dialogElementCtrl.activate(ureq, entries, null);
	}
	
	private void activateByDialogElement(UserRequest ureq, Long elementKey) {
		element = dialogElmsMgr.getDialogElementByKey(elementKey);
		if(!checkAccess(element)) {
			return;
		}
		initToolbar();
		dialogElementCtrl = new DialogElementController(ureq, getWindowControl(), element, userCourseEnv, courseNode, secCallback);
		listenTo(dialogElementCtrl);
		mainVC.put(FORUM, dialogElementCtrl.getInitialComponent());
	}
	
	private boolean checkAccess(DialogElement element) {
		return element != null && courseNode.getIdent().equals(element.getSubIdent()) && entry.equals(element.getEntry());
	}
	
	private void doDialog(UserRequest ureq) {
		removeAsListenerAndDispose(dialogElementCtrl);

		if(element == null) {
			showInfo("element.already.deleted");
			dialogElementListCtrl.loadModel();
		} else {
			initToolbar();

			dialogElementCtrl = new DialogElementController(ureq, getWindowControl(), element, userCourseEnv, courseNode, secCallback);
			listenTo(dialogElementCtrl);
			mainVC.put(FORUM, dialogElementCtrl.getInitialComponent());
		}
	}
}
