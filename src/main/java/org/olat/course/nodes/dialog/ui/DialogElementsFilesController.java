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
package org.olat.course.nodes.dialog.ui;

import java.io.File;
import java.util.UUID;

import org.olat.core.commons.modules.bc.FileUploadController;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.FolderEvent;
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
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseModule;
import org.olat.course.nodes.DialogCourseNode;
import org.olat.course.nodes.dialog.DialogElement;
import org.olat.course.nodes.dialog.DialogElementsManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DialogElementsFilesController extends BasicController {
	
	private final Link uploadButton;
	private final VelocityContainer mainVC;
	
	private FileUploadController fileUplCtr;
	private CloseableModalController cmc;
	private DialogElementListEditController elementListCtrl;
	
	private final DialogCourseNode courseNode;
	private final CourseEnvironment courseEnv;
	private final RepositoryEntry entry;

	@Autowired
	private DialogElementsManager dialogElmsMgr;
	@Autowired
	private NotificationsManager notificationsManager;
	
	public DialogElementsFilesController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv, DialogCourseNode courseNode) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.courseEnv = courseEnv;
		entry = courseEnv.getCourseGroupManager().getCourseEntry();
		
		mainVC = createVelocityContainer("edit");
		
		uploadButton = LinkFactory.createButton("dialog.upload.file", mainVC, this);
		uploadButton.setIconLeftCSS("o_icon o_icon-fw o_icon_upload");
		uploadButton.setElementCssClass("o_sel_dialog_upload");
		
		elementListCtrl = new DialogElementListEditController(ureq, getWindowControl(), entry, courseNode);
		listenTo(elementListCtrl);
		mainVC.put("dialogElementsTable", elementListCtrl.getInitialComponent());
		
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
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == uploadButton) {
			doUpload(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == fileUplCtr) {
			if (event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
				elementListCtrl.loadModel();
			} else if (event.getCommand().equals(FolderEvent.UPLOAD_EVENT)) {
				doFinalizeUploadFile(fileUplCtr.getUploadedFile());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		if(fileUplCtr != null && fileUplCtr.getUploadContainer() != null) {
			fileUplCtr.getUploadContainer().deleteSilently();
		}
		removeAsListenerAndDispose(fileUplCtr);
		removeAsListenerAndDispose(cmc);
		fileUplCtr = null;
		cmc = null;
	}
	
	private void doUpload(UserRequest ureq) {
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
		if(file == null) return;
		
		//everything when well so save the property
		DialogElement element = dialogElmsMgr.createDialogElement(entry, getIdentity(), file.getName(), file.getSize(), courseNode.getIdent());
		VFSContainer dialogContainer = dialogElmsMgr.getDialogContainer(element);
		VFSManager.copyContent(file.getParentContainer(), dialogContainer);

		// inform subscription manager about new element
		SubscriptionContext subsContext = CourseModule.createSubscriptionContext(courseEnv, courseNode);
		notificationsManager.markPublisherNews(subsContext, getIdentity(), true);

		ThreadLocalUserActivityLogger.log(CourseLoggingAction.DIALOG_ELEMENT_FILE_UPLOADED, getClass(),
				LoggingResourceable.wrapUploadFile(file.getName()));
		elementListCtrl.loadModel();
	}
}