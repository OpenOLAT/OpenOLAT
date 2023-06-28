/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.dialog.ui;

import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
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
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseModule;
import org.olat.course.nodes.DialogCourseNode;
import org.olat.course.nodes.dialog.DialogElement;
import org.olat.course.nodes.dialog.DialogElementsManager;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class DialogElementsFilesController extends BasicController {

	private final Link uploadButton;
	private final VelocityContainer mainVC;

	private DialogFileUploadController dialogFileUploadCtrl;
	private CloseableModalController cmc;
	private final DialogElementListEditController elementListCtrl;
	
	private final DialogCourseNode courseNode;
	private final UserCourseEnvironment userCourseEnv;
	private final RepositoryEntry entry;

	@Autowired
	private DialogElementsManager dialogElmsMgr;
	@Autowired
	private NotificationsManager notificationsManager;

	public DialogElementsFilesController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, DialogCourseNode courseNode) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		entry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();

		mainVC = createVelocityContainer("edit");
		
		uploadButton = LinkFactory.createButton("dialog.upload.file", mainVC, this);
		uploadButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		uploadButton.setElementCssClass("o_sel_dialog_upload");
		
		elementListCtrl = new DialogElementListEditController(ureq, getWindowControl(), entry, userCourseEnv, courseNode);
		listenTo(elementListCtrl);
		mainVC.put("dialogElementsTable", elementListCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == uploadButton) {
			doUploadFile(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == dialogFileUploadCtrl) {
			if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			} else if (event == Event.DONE_EVENT) {
					if (dialogFileUploadCtrl.getActionSelectedKey().equals(DialogFileUploadController.DIALOG_ACTION_UPLOAD)) {
						// upload new file
						VFSLeaf newFile = dialogElmsMgr.doUpload(
								dialogFileUploadCtrl.getFileUploadEl().getUploadFile(),
								dialogFileUploadCtrl.getFileNameElValue(),
								getIdentity());

						if (newFile != null) {
							doFinalizeUploadFile(newFile);
							fireEvent(ureq, Event.DONE_EVENT);
						} else {
							showError("failed");
							fireEvent(ureq, Event.FAILED_EVENT);
						}
					} else {
						// copy file
						VFSContainer courseContainer = userCourseEnv.getCourseEnvironment().getCourseFolderContainer();
						String filename = dialogFileUploadCtrl.getFileNameElValue();
						String fileToCopy = dialogFileUploadCtrl.getFileChooserElValue();
						if(!fileToCopy.contains("://")) {
							DialogElement element = dialogElmsMgr.doCopySelectedFile(fileToCopy, filename, courseContainer,
									getIdentity(), entry, courseNode.getIdent(),
									dialogFileUploadCtrl.getAuthoredByElValue());
							if (element != null) {
								markPublisherNews();
							}
						}
					}
				elementListCtrl.loadModel();
				}
				cmc.deactivate();
				cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(dialogFileUploadCtrl);
		removeAsListenerAndDispose(cmc);
		dialogFileUploadCtrl = null;
		cmc = null;
	}

	private void doUploadFile(UserRequest ureq) {
		removeAsListenerAndDispose(dialogFileUploadCtrl);

		// canCopyFile true, because this env/scope is in edit for admins
		dialogFileUploadCtrl = new DialogFileUploadController(ureq, getWindowControl(), userCourseEnv, true);
		listenTo(dialogFileUploadCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), dialogFileUploadCtrl.getInitialComponent(),
				true, translate("dialog.upload.file"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doFinalizeUploadFile(VFSLeaf file) {
		if(file == null) return;
		
		//everything when well so save the property
		DialogElement element = dialogElmsMgr.createDialogElement(entry, getIdentity(), file.getName(), file.getSize(), courseNode.getIdent(), dialogFileUploadCtrl.getAuthoredByElValue());
		VFSContainer dialogContainer = dialogElmsMgr.getDialogContainer(element);
		VFSManager.copyContent(file.getParentContainer(), dialogContainer);

		// inform subscription manager about new element
		markPublisherNews();

		ThreadLocalUserActivityLogger.log(CourseLoggingAction.DIALOG_ELEMENT_FILE_UPLOADED, getClass(),
				LoggingResourceable.wrapUploadFile(file.getName()));
	}

	/**
	 * Inform subscription manager about new element.
	 */
	private void markPublisherNews() {
		SubscriptionContext subsContext = CourseModule.createSubscriptionContext(userCourseEnv.getCourseEnvironment(), courseNode);
		notificationsManager.markPublisherNews(subsContext, getIdentity(), true);
	}
}