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
package org.olat.course.nodes.gta.ui;

import java.io.File;

import org.olat.core.commons.modules.singlepage.SinglePageController;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
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
import org.olat.fileresource.DownloadeableMediaResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 05.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class GTAAssignedTaskController extends BasicController {

	private CloseableModalController cmc;
	private SinglePageController viewTaskCtrl;
	private VideoAudioPlayerController videoAudioPlayerController;
	private Controller docEditorCtrl;
	
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

			VFSContainer tasksContainer = gtaManager.getTasksContainer(courseEnv, gtaNode);
			VFSItem vfsItem = tasksContainer.resolve(taskFile.getName());
			// Link to download and open the file (if possible)
			if (vfsItem instanceof VFSLeaf vfsLeaf) {
				Link downloadLink = LinkFactory.createLink("download.task", "download.task", getTranslator(), mainVC, this, Link.BUTTON_XSMALL + Link.NONTRANSLATED);
				downloadLink.setCustomDisplayText(translate("download"));
				downloadLink.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
				downloadLink.setAriaRole("button");
				downloadLink.setGhost(true);
				downloadLink.setUserObject(vfsLeaf);

				DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(), ureq.getUserSession().getRoles(), vfsLeaf,
						vfsLeaf.getMetaInfo(), true, DocEditorService.MODES_VIEW);
				if(editorInfo.isEditorAvailable()) {
					Link openDocLink = LinkFactory.createLink("open.link", "preview", getTranslator(), mainVC, this, Link.BUTTON_XSMALL + Link.NONTRANSLATED);
					openDocLink.setCustomDisplayText(editorInfo.getModeButtonLabel(getTranslator()));
					openDocLink.setIconLeftCSS("o_icon o_icon-fw " + editorInfo.getModeIcon());
					openDocLink.setAriaRole("button");
					openDocLink.setGhost(true);
					openDocLink.setUserObject(vfsLeaf);

					Link documentLink = LinkFactory.createLink("doc.link", "preview", null, mainVC, this, Link.NONTRANSLATED);
					if(taskDef != null) {
						documentLink.setCustomDisplayText(StringHelper.escapeHtml(taskDef.getTitle()));
					} else {
						documentLink.setCustomDisplayText(StringHelper.escapeHtml(taskFile.getName()));
						documentLink.setIconLeftCSS("o_icon o_icon-fw o_icon_warning");
						documentLink.setEnabled(false);
					}
					if (editorInfo.isNewWindow() && !taskFile.getName().endsWith(".html")) {
						openDocLink.setNewWindow(true, true);
						documentLink.setNewWindow(true, true);
					}
					documentLink.setUserObject(vfsLeaf);
					documentLink.setTitle(taskInfos);
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
		if(source instanceof Link previewLink) {
			if ("preview".equals(((Link) source).getCommand())) {
				if (taskFile.getName().endsWith(".html")) {
					doPreview(ureq);
				} else {
					doOpenPreview(ureq, (VFSLeaf) previewLink.getUserObject());
				}
			} else if ("download.task".equals(((Link) source).getCommand())) {
				MediaResource mdr = new DownloadeableMediaResource(taskFile);
				ureq.getDispatchResult().setResultingMediaResource(mdr);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(cmc == source || source == docEditorCtrl) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(viewTaskCtrl);
		removeAsListenerAndDispose(videoAudioPlayerController);
		removeAsListenerAndDispose(docEditorCtrl);
		cmc = null;
		viewTaskCtrl = null;
		videoAudioPlayerController = null;
		docEditorCtrl = null;
	}
	
	private void doOpenPreview(UserRequest ureq, VFSLeaf vfsLeaf) {
		VFSContainer tasksContainer = gtaManager.getTasksContainer(courseEnv, gtaNode);
		DocEditorConfigs configs = GTAUIFactory.getEditorConfig(tasksContainer, vfsLeaf, vfsLeaf.getName(), Mode.VIEW, null);
		docEditorCtrl = docEditorService.openDocument(ureq, getWindowControl(), configs, DocEditorService.MODES_VIEW).getController();
		listenTo(docEditorCtrl);
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
