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
package org.olat.course.nodes.gta.ui;

import java.io.File;

import org.olat.core.commons.modules.singlepage.SinglePageController;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.ui.DocEditorController;
import org.olat.core.commons.services.video.ui.VideoAudioPlayerController;
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
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskHelper;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.run.environment.CourseEnvironment;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 05.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAAssignedTaskController extends BasicController {

	private Link downloadButton;
	private Link downloadLink;

	private CloseableModalController cmc;
	private SinglePageController viewTaskCtrl;
	private VideoAudioPlayerController videoAudioPlayerController;
	
	private File taskFile;
	private final GTACourseNode gtaNode;
	private final CourseEnvironment courseEnv;
	
	@Autowired
	private GTAManager gtaManager;

	@Autowired
	private DocEditorService docEditorService;
	
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param task The task
	 * @param taskDef The task definition if any
	 * @param courseEnv The course environment
	 * @param gtaNode The course node
	 * @param i18nDescription The description of the step
	 * @param i18nWarning The warning message if the assignee wasn't able to choose a task
	 * @param message
	 */
	public GTAAssignedTaskController(UserRequest ureq, WindowControl wControl, Task task,
			TaskDefinition taskDef, CourseEnvironment courseEnv, GTACourseNode gtaNode,
			String i18nDescription, String i18nWarning, String message) {
		super(ureq, wControl, Util.createPackageTranslator(DocEditorController.class, ureq.getLocale()));
		
		this.gtaNode = gtaNode;
		this.courseEnv = courseEnv;
		
		VelocityContainer mainVC = createVelocityContainer("assigned_task");
		mainVC.contextPut("description", translate(i18nDescription));
		if(StringHelper.containsNonWhitespace(message)) {
			mainVC.contextPut("message", message);
		}

		File taskDir = gtaManager.getTasksDirectory(courseEnv, gtaNode);
		
		if(task == null || !StringHelper.containsNonWhitespace(task.getTaskName())) {
			mainVC.contextPut("warningMsg", translate(i18nWarning));
		} else {
			taskFile = new File(taskDir, task.getTaskName());

			double fileSizeInMB = taskFile.length() / (1024.0d * 1024.0d);
			String[] infos = new String[] { taskFile.getName(), TaskHelper.format(fileSizeInMB) };
			String taskInfos = translate("download.task.infos", infos);
			String cssIcon = CSSHelper.createFiletypeIconCssClassFor(taskFile.getName());
			mainVC.contextPut("cssIcon", cssIcon);
			if(taskDef != null) {
				mainVC.contextPut("taskDescription", taskDef.getDescription());
			}
			// two links to same file: explicit button and task name
			downloadButton = LinkFactory.createCustomLink("download.task", "download.task", null, Link.BUTTON + Link.NONTRANSLATED, mainVC, this);
			downloadButton.setCustomDisplayText(translate("download.task"));
			downloadButton.setTitle(taskInfos);
			downloadButton.setIconLeftCSS("o_icon o_icon_download");
			downloadButton.setTarget("_blank");
			downloadButton.setVisible(taskFile.exists());
	
			downloadLink = LinkFactory.createCustomLink("download.link", "download.link", null, Link.NONTRANSLATED, mainVC, this);
			if(taskDef != null) {
				downloadLink.setCustomDisplayText(StringHelper.escapeHtml(taskDef.getTitle()));
			} else {
				downloadLink.setCustomDisplayText(StringHelper.escapeHtml(taskFile.getName()));
				downloadLink.setIconLeftCSS("o_icon o_icon-fw o_icon_warning");
				downloadLink.setEnabled(false);
			}
			downloadLink.setTitle(taskInfos);
			if(!taskFile.getName().endsWith(".html")) {
				downloadLink.setTarget("_blank");
			}
			
			// Link to preview the file (if possible)
			VFSContainer tasksContainer = gtaManager.getTasksContainer(courseEnv, gtaNode);
			VFSItem vfsItem = tasksContainer.resolve(taskFile.getName());
			if (vfsItem instanceof VFSLeaf) {
				VFSLeaf vfsLeaf = (VFSLeaf)vfsItem;
				if(docEditorService.hasEditor(getIdentity(), ureq.getUserSession().getRoles(), vfsLeaf, Mode.VIEW, true)) {
					Link previewLink = LinkFactory.createLink("preview", "preview", getTranslator(), mainVC, this, Link.NONTRANSLATED);
					previewLink.setCustomDisplayText(StringHelper.escapeHtml(docEditorService.getModeButtonLabel(Mode.VIEW, taskFile.getName(), getTranslator())));
					previewLink.setIconLeftCSS("o_icon o_icon-fw " + docEditorService.getModeIcon(Mode.VIEW, taskFile.getName()));
					previewLink.setElementCssClass("btn btn-default btn-xs o_button_ghost");
					previewLink.setAriaRole("button");
					previewLink.setUserObject(vfsLeaf);
					if (!docEditorService.isAudioVideo(Mode.VIEW, vfsLeaf.getName())) {
						previewLink.setNewWindow(true, true);
					}
				}
				
				mainVC.contextPut("size", vfsLeaf.getSize());
			}
			
			// Meta data
			mainVC.contextPut("type", taskFile.getName().substring(taskFile.getName().lastIndexOf(".") + 1).toUpperCase());
		}

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(downloadLink == source) {
			if(taskFile.getName().endsWith(".html")) {
				doPreview(ureq);
			} else {
				MediaResource mdr = new FileMediaResource(taskFile, true);
				ureq.getDispatchResult().setResultingMediaResource(mdr);
			}
		} else if(downloadButton == source) {
			MediaResource mdr;
			if(taskFile.getName().endsWith(".html")) {
				File taskDir = gtaManager.getTasksDirectory(courseEnv, gtaNode);
				mdr = new HTMLZippedMediaResource(taskFile.getName(), taskDir);
			} else {
				mdr = new FileMediaResource(taskFile, true);
			}
			ureq.getDispatchResult().setResultingMediaResource(mdr);
		} else if(source instanceof Link && "preview".equals(((Link)source).getCommand())) {
			Link previewLink = (Link)source;
			doOpenPreview(ureq, (VFSLeaf)previewLink.getUserObject());
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(viewTaskCtrl);
		removeAsListenerAndDispose(videoAudioPlayerController);
		cmc = null;
		viewTaskCtrl = null;
		videoAudioPlayerController = null;
	}
	
	private void doOpenPreview(UserRequest ureq, VFSLeaf vfsLeaf) {
		VFSContainer tasksContainer = gtaManager.getTasksContainer(courseEnv, gtaNode);
		DocEditorConfigs configs = GTAUIFactory.getEditorConfig(tasksContainer, vfsLeaf, vfsLeaf.getName(), Mode.VIEW, null);
		if (docEditorService.isAudioVideo(Mode.VIEW, vfsLeaf.getName())) {
			videoAudioPlayerController = new VideoAudioPlayerController(ureq, getWindowControl(), configs, null);
			String title = translate("av.play");
			cmc = new CloseableModalController(getWindowControl(), "close",
				videoAudioPlayerController.getInitialComponent(), true, title, true);
			listenTo(cmc);
			cmc.activate();
		} else {
			String url = docEditorService.prepareDocumentUrl(ureq.getUserSession(), configs);
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
		}
	}
	
	private void doPreview(UserRequest ureq) {
		if(guardModalController(viewTaskCtrl)) return;
		
		VFSContainer tasksContainer = gtaManager.getTasksContainer(courseEnv, gtaNode);
		viewTaskCtrl = new SinglePageController(ureq, getWindowControl(), tasksContainer, taskFile.getName(),
				false, null, null, TaskHelper.getStandardDeliveryOptions(), false);
		listenTo(viewTaskCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), viewTaskCtrl.getInitialComponent(), true, taskFile.getName());
		listenTo(cmc);
		cmc.activate();
	}
}
