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

package org.olat.course.nodes.dialog.ui;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.olat.core.commons.controllers.linkchooser.LinkChooserController;
import org.olat.core.commons.controllers.linkchooser.URLChoosenEvent;
import org.olat.core.commons.modules.bc.FileUploadController;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
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
import org.olat.core.util.WebappHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
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

	private Link copyButton;
	private Link backButton;
	private Link uploadButton;
	private final VelocityContainer mainVC;

	private final DialogCourseNode courseNode;
	private final RepositoryEntry entry;
	private final UserCourseEnvironment userCourseEnv;
	private final DialogSecurityCallback secCallback;

	private CloseableModalController cmc;
	private FileUploadController fileUplCtr;
	private LinkChooserController fileCopyCtr;
	private DialogElementController dialogCtr;
	private DialogElementListController filesCtrl;
	private ContextualSubscriptionController csCtr;

	@Autowired
	private ForumManager forumManager;
	@Autowired
	private DialogElementsManager dialogElmsMgr;
	@Autowired
	private NotificationsManager notificationsManager;

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
			csCtr = new ContextualSubscriptionController(ureq, getWindowControl(), secCallback.getSubscriptionContext(), pdata);
			listenTo(csCtr);
			mainVC.put("subscription", csCtr.getInitialComponent());
		}
		
		backButton = LinkFactory.createLinkBack(mainVC, this);
		
		if (secCallback.canCopyFile()) {
			copyButton = LinkFactory.createButton("dialog.copy.file", mainVC, this);
		}
		
		if(secCallback.mayOpenNewThread()) {
			uploadButton = LinkFactory.createButton("dialog.upload.file", mainVC, this);
			uploadButton.setIconLeftCSS("o_icon o_icon-fw o_icon_upload");
			uploadButton.setElementCssClass("o_sel_dialog_upload");
		}

		filesCtrl = new DialogElementListController(ureq, getWindowControl(), userCourseEnv, courseNode, secCallback, true);
		listenTo(filesCtrl);
		mainVC.put("files", filesCtrl.getInitialComponent());
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		if(fileUplCtr != null && fileUplCtr.getUploadContainer() != null) {
			fileUplCtr.getUploadContainer().deleteSilently();
		}
        super.doDispose();
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
		if(filesCtrl == source) {
			if(event instanceof SelectRowEvent) {
				SelectRowEvent sde = (SelectRowEvent)event;
				doDialog(ureq, sde.getRow());
			}
		} else if (source == fileUplCtr) {
			if(event instanceof FolderEvent && FolderEvent.UPLOAD_EVENT.equals(event.getCommand())) {
				doFinalizeUploadFile(fileUplCtr.getUploadedFile());
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == fileCopyCtr) {
			if (event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
				filesCtrl.loadModel();
			} else if (event instanceof URLChoosenEvent) {
				URLChoosenEvent choosenEvent = (URLChoosenEvent)event;
				String fileUrl = choosenEvent.getURL();
				if(fileUrl.indexOf("://") < 0) {
					doCopySelectedFile(fileUrl);
					filesCtrl.loadModel();
				}
			}
			cmc.deactivate();
			cleanUp();
		}  else if (source == cmc) {
			cleanUp();
		} 
	}
	
	private void cleanUp() {
		if(fileUplCtr != null && fileUplCtr.getUploadContainer() != null) {
			fileUplCtr.getUploadContainer().deleteSilently();
		}
		removeAsListenerAndDispose(fileCopyCtr);
		removeAsListenerAndDispose(fileUplCtr);
		removeAsListenerAndDispose(cmc);
		fileCopyCtr = null;
		fileUplCtr = null;
		cmc = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == uploadButton){
			doUploadFile(ureq);
		} else if (source == copyButton) {
			doCopy(ureq);
		} else if(backButton == source) {
			back(ureq);
		}
	}
	
	private void back(UserRequest ureq) {
		mainVC.remove("forum");
		if(dialogCtr != null) {
			filesCtrl.load(dialogCtr.getElement());
			removeAsListenerAndDispose(dialogCtr);
			dialogCtr = null;
		}
		addToHistory(ureq);
	}
	
	private void activateByMessage(UserRequest ureq, List<ContextEntry> entries) {
		if(entries == null || entries.isEmpty()) return;
		
		Long messageKey = entries.get(0).getOLATResourceable().getResourceableId();
		Message message = forumManager.getMessageById(messageKey);
		if(message == null) return;
			
		DialogElement element = dialogElmsMgr.getDialogElementByForum(message.getForum().getKey());
		if(!checkAccess(element)) {
			return;
		}
		
		dialogCtr = new DialogElementController(ureq, getWindowControl(), element, userCourseEnv, courseNode, secCallback);
		listenTo(dialogCtr);
		mainVC.put("forum", dialogCtr.getInitialComponent());
		//activate message
		dialogCtr.activate(ureq, entries, null);
	}
	
	private void activateByDialogElement(UserRequest ureq, Long elementKey) {
		DialogElement element = dialogElmsMgr.getDialogElementByKey(elementKey);
		if(!checkAccess(element)) {
			return;
		}
		
		dialogCtr = new DialogElementController(ureq, getWindowControl(), element, userCourseEnv, courseNode, secCallback);
		listenTo(dialogCtr);
		mainVC.put("forum", dialogCtr.getInitialComponent());
	}
	
	private boolean checkAccess(DialogElement element) {
		return element != null && courseNode.getIdent().equals(element.getSubIdent()) && entry.equals(element.getEntry());
	}
	
	private void doDialog(UserRequest ureq, DialogElementRow row) {
		removeAsListenerAndDispose(dialogCtr);
		
		DialogElement element = dialogElmsMgr.getDialogElementByKey(row.getDialogElementKey());
		if(element == null) {
			showInfo("element.already.deleted");
			filesCtrl.loadModel();
		} else {
			dialogCtr = new DialogElementController(ureq, getWindowControl(), element, userCourseEnv, courseNode, secCallback);
			listenTo(dialogCtr);
			mainVC.put("forum", dialogCtr.getInitialComponent());
		}
	}
	
	private void doUploadFile(UserRequest ureq) {
		removeAsListenerAndDispose(fileUplCtr);
		
		VFSContainer tmpContainer = new LocalFolderImpl(new File(WebappHelper.getTmpDir(), "poster_" + UUID.randomUUID()));
		fileUplCtr = new FileUploadController(getWindowControl(), tmpContainer, ureq,
				FolderConfig.getLimitULKB(), Quota.UNLIMITED, null, false, false, false, false, true, false);
		listenTo(fileUplCtr);
		
		cmc = new CloseableModalController(getWindowControl(), "close", fileUplCtr.getInitialComponent(),
				true, translate("dialog.upload.file"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doFinalizeUploadFile(VFSLeaf file) {
		//everything when well so save the property
		DialogElement element = dialogElmsMgr.createDialogElement(entry, getIdentity(), file.getName(), file.getSize(), courseNode.getIdent());
		VFSContainer dialogContainer = dialogElmsMgr.getDialogContainer(element);
		VFSManager.copyContent(file.getParentContainer(), dialogContainer);

		markPublisherNews();
		filesCtrl.loadModel();
	}
	
	private void doCopy(UserRequest ureq) {
		VFSContainer courseContainer = userCourseEnv.getCourseEnvironment().getCourseFolderContainer();
		fileCopyCtr = new LinkChooserController(ureq, getWindowControl(), courseContainer, null, null, null, false, "", null, null, true);
		listenTo(fileCopyCtr);
		
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), "close", fileCopyCtr.getInitialComponent(),
				true, translate("dialog.copy.file"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCopySelectedFile(String fileUrl) {
		VFSContainer courseContainer = userCourseEnv.getCourseEnvironment().getCourseFolderContainer();
		VFSLeaf vl = (VFSLeaf) courseContainer.resolve(fileUrl);
		DialogElement newElement = dialogElmsMgr.createDialogElement(entry, getIdentity(),
				vl.getName(), vl.getSize(), courseNode.getIdent());
		
		//copy file
		VFSContainer dialogContainer = dialogElmsMgr.getDialogContainer(newElement);
		VFSLeaf copyVl = dialogContainer.createChildLeaf(vl.getName());
		if(copyVl == null) {
			copyVl = (VFSLeaf)dialogContainer.resolve(vl.getName());
		}
		VFSManager.copyContent(vl, copyVl, true, userCourseEnv.getIdentityEnvironment().getIdentity());
		
		markPublisherNews();
		filesCtrl.loadModel();
	}

	/**
	 * Inform subscription manager about new element.
	 */
	private void markPublisherNews() {
		if (secCallback.getSubscriptionContext() != null) {
			notificationsManager.markPublisherNews(secCallback.getSubscriptionContext(), getIdentity(), true);
		}
	}
}
