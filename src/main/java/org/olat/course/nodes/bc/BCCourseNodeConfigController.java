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
package org.olat.course.nodes.bc;

import java.io.File;

import org.olat.admin.quota.QuotaConstants;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.FullAccessWithQuotaCallback;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.BCCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: Apr 28, 2004
 *
 * @author gnaegi
 */
public class BCCourseNodeConfigController extends FormBasicController {
	
	private SingleSelection folderTargetChoose;
	private FormLink chooseFolder;
	private StaticTextElement subPath;
	private FormLink createFolder;
	private FormLink folderViewLink;
	private FormItem sharedFolderWarning, sharedFolderInfo;
	private FormItem linkedFolderWarning;
	
	private BreadcrumbPanel stackPanel;
	private CloseableModalController cmc;
	private BCCourseNodeEditCreateFolderForm createFolderForm;
	private BCCourseNodeEditChooseFolderForm chooseForm;
	private FolderRunController folderCtrl;

	private final BCCourseNode node;
	private final ModuleConfiguration moduleConfig;
	private final ICourse course;
	
	@Autowired
	private QuotaManager quotaManager;
	
	public BCCourseNodeConfigController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			BCCourseNode bcNode, ICourse course) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.node = bcNode;
		this.moduleConfig = node.getModuleConfiguration();
		this.course = course;
		initForm(ureq);
		validate();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer pathCont = FormLayoutContainer.createDefaultFormLayout("path", getTranslator());
		formLayout.add(pathCont);
		pathCont.setFormTitle(translate("info.select"));
		pathCont.setFormContextHelp("Course Element: Folder#_folder_configuration");
		
		String[] keys = {"autoPath", "pathChoose"};
		String[] values= {translate("pathChoose.auto"), translate("pathChoose.custom")};
		folderTargetChoose = uifactory.addRadiosVertical("pathChoose", pathCont, keys, values);
		folderTargetChoose.addActionListener(FormEvent.ONCLICK);
		subPath = uifactory.addStaticTextElement("subPathLab.label", translate("subPathLab.dummy"), pathCont);

		sharedFolderInfo = uifactory.addStaticExampleText("warning","", "<div class=\"o_important\">"+translate("info.sharedfolder")+"</div>",pathCont);
		sharedFolderWarning = uifactory.createSimpleErrorText("warning", translate("warning.no.sharedfolder"));
		formLayout.add(sharedFolderWarning);
		
		linkedFolderWarning = uifactory.createSimpleErrorText("warning2", translate("warning.no.linkedfolder"));
		formLayout.add(linkedFolderWarning);

		boolean isAuto = moduleConfig.getBooleanSafe(BCCourseNode.CONFIG_AUTO_FOLDER);

		if(isAuto) {
			folderTargetChoose.select("autoPath", true);
			subPath.setVisible(false);
		} else {
			folderTargetChoose.select("pathChoose", true);
			String subpath = moduleConfig.getStringValue(BCCourseNode.CONFIG_SUBPATH);

			if(subpath != "") {
				subPath.setValue(subpath);
			}
			subPath.setVisible(true);
		}
		
		if(node.isSharedFolder()) {
			sharedFolderInfo.setVisible(course.getCourseConfig().isSharedFolderReadOnlyMount());
		} else {
			sharedFolderInfo.setVisible(false);
		}

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		pathCont.add(buttonsCont);

		chooseFolder = uifactory.addFormLink("chooseFolder", buttonsCont, Link.BUTTON);
		chooseFolder.setVisible(folderTargetChoose.isSelected(1));

		createFolder = uifactory.addFormLink("createFolder", buttonsCont, Link.BUTTON);
		createFolder.setVisible(folderTargetChoose.isSelected(1));
		
		
		FormLayoutContainer transferCont = FormLayoutContainer.createDefaultFormLayout("transfer", getTranslator());
		formLayout.add(transferCont);
		transferCont.setRootForm(mainForm);
		transferCont.setFormTitle(translate("info.folder"));
		
		folderViewLink = uifactory.addFormLink("folder.view", transferCont, Link.BUTTON);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		validate();
		if(source == folderTargetChoose){
			subPath.setVisible(folderTargetChoose.isSelected(1));
			chooseFolder.setVisible(folderTargetChoose.isSelected(1));
			createFolder.setVisible(folderTargetChoose.isSelected(1));
			if(folderTargetChoose.isSelected(1)){
				moduleConfig.setBooleanEntry(BCCourseNode.CONFIG_AUTO_FOLDER, false);
				String path = moduleConfig.getStringValue(BCCourseNode.CONFIG_SUBPATH);
				if(StringHelper.containsNonWhitespace(path)){
					subPath.setValue(path);
				}else{
					subPath.setValue(translate("subPathLab.dummy"));
				}
			}else{
				moduleConfig.setBooleanEntry(BCCourseNode.CONFIG_AUTO_FOLDER, true);
				moduleConfig.setStringValue(BCCourseNode.CONFIG_SUBPATH, "");
			}
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			createFolder.setVisible(folderTargetChoose.isSelected(1));
		} else if(source == createFolder){
			createFolderForm = new BCCourseNodeEditCreateFolderForm(ureq, getWindowControl(), course, node);
			listenTo(createFolderForm);

			String title = translate("chooseFolder");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), createFolderForm.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		} else if (source == chooseFolder){
			VFSContainer namedContainer = course.getCourseFolderContainer();

			chooseForm = new BCCourseNodeEditChooseFolderForm(ureq, getWindowControl(), namedContainer);
			listenTo(chooseForm);

			String title = translate("createFolder");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), chooseForm.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		} else if (source == folderViewLink) {
			doOpenFolder(ureq);
		} 
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == stackPanel) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				if(pe.getController() == folderCtrl) {
					cleanUp();
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == createFolderForm){
			if(Event.CANCELLED_EVENT == event) {
				cmc.deactivate();
			} else if(event instanceof SelectFolderEvent) {
				cmc.deactivate();
				SelectFolderEvent sfe = (SelectFolderEvent)event;
				String subpath = sfe.getSubpath();
				VFSContainer selectedContainer = (VFSContainer) course.getCourseFolderContainer().resolve(subpath);
				updatePublisher(selectedContainer);
				moduleConfig.setStringValue(BCCourseNode.CONFIG_SUBPATH, subpath);
				subPath.setValue(subpath);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
			validate();
			cleanUp();
		} else if(source == chooseForm) {
			if(Event.CANCELLED_EVENT == event){
				cmc.deactivate();
			} else if(event instanceof SelectFolderEvent) {
				cmc.deactivate();
				
				SelectFolderEvent sfe = (SelectFolderEvent)event;
				String subpath = sfe.getSubpath();
				subPath.setValue(subpath);

				VFSContainer selectedContainer = (VFSContainer) course.getCourseFolderContainer().resolve(subpath);
				updatePublisher(selectedContainer);
				moduleConfig.setStringValue(BCCourseNode.CONFIG_SUBPATH, subpath);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
			validate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void validate() {
		if(folderTargetChoose.isSelected(0)){
			sharedFolderWarning.setVisible(false);
			linkedFolderWarning.setVisible(false);
		} else {
			if(isSharedfolderNotPresent()){
				sharedFolderWarning.setVisible(true);
			} else {
				sharedFolderWarning.setVisible(false);
			}
			if(node.isSharedFolder()) {
				sharedFolderInfo.setVisible(course.getCourseConfig().isSharedFolderReadOnlyMount());
			} else {
				sharedFolderInfo.setVisible(false);
			}
			if(isLinkedFolderNotPresent()) {
				linkedFolderWarning.setVisible(true);
			} else {
				linkedFolderWarning.setVisible(false);
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(createFolderForm);
		removeAsListenerAndDispose(chooseForm);
		removeAsListenerAndDispose(folderCtrl);
		removeAsListenerAndDispose(cmc);
		createFolderForm = null;
		chooseForm = null;
		folderCtrl = null;
		cmc = null;
	}

	private void updatePublisher(VFSContainer container){
		File realFile = VFSManager.getRealFile(container);
		String relPath = new File(FolderConfig.getCanonicalRoot()).toPath().relativize(realFile.toPath()).toString();

		NotificationsManager notifManager = CoreSpringFactory.getImpl(NotificationsManager.class);
		SubscriptionContext nodefolderSubContext = CourseModule.createSubscriptionContext(course.getCourseEnvironment(), node);

		Publisher publisher = notifManager.getPublisher(nodefolderSubContext);
		if (publisher != null) {
			String businessPath = getWindowControl().getBusinessControl().getAsString();
			String data = "/"+relPath;
			PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(BCCourseNode.class), data, businessPath);
			notifManager.updatePublisherData(nodefolderSubContext, pdata);
		}
	}
	
	private void doOpenFolder(UserRequest ureq) {
		VFSContainer namedContainer = null;
		if(node.getModuleConfiguration().getBooleanSafe(BCCourseNode.CONFIG_AUTO_FOLDER)){
			VFSContainer directory = BCCourseNode.getNodeFolderContainer(node, course.getCourseEnvironment());
			directory.setLocalSecurityCallback(getSecurityCallbackWithQuota(directory.getRelPath()));
			namedContainer = directory;
		} else {
			VFSContainer courseContainer = course.getCourseFolderContainer();
			String path = node.getModuleConfiguration().getStringValue(BCCourseNode.CONFIG_SUBPATH, "");
			VFSItem pathItem = courseContainer.resolve(path);
			if(pathItem instanceof VFSContainer){
				namedContainer = (VFSContainer) pathItem;
				if(node.isSharedFolder()) {
					if(course.getCourseConfig().isSharedFolderReadOnlyMount()) {
						namedContainer.setLocalSecurityCallback(new ReadOnlyCallback());
					} else {
						String relPath = BCCourseNode.getNodeFolderContainer(node, course.getCourseEnvironment()).getRelPath();
						namedContainer.setLocalSecurityCallback(getSecurityCallbackWithQuota(relPath));
					}
				} else {
					VFSContainer inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(namedContainer);
					if (inheritingContainer != null && inheritingContainer.getLocalSecurityCallback() != null
							&& inheritingContainer.getLocalSecurityCallback() .getQuota() != null) {
						Quota quota = inheritingContainer.getLocalSecurityCallback().getQuota();
						namedContainer.setLocalSecurityCallback(new FullAccessWithQuotaCallback(quota));
					} else {
						namedContainer.setLocalSecurityCallback(new ReadOnlyCallback());
					}
				}
			}
		}
		
		if(namedContainer == null) {
			showWarning("warning.no.linkedfolder");
		} else {
			removeAsListenerAndDispose(folderCtrl);
			folderCtrl = new FolderRunController(namedContainer, false, ureq, getWindowControl());
			listenTo(folderCtrl);
			stackPanel.pushController("Preview", folderCtrl);
		}
	}
	
	private VFSSecurityCallback getSecurityCallbackWithQuota(String relPath) {
		Quota quota = quotaManager.getCustomQuota(relPath);
		if (quota == null) {
			Quota defQuota = quotaManager.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_NODES);
			quota = quotaManager.createQuota(relPath, defQuota.getQuotaKB(), defQuota.getUlLimitKB());
		}
		return new FullAccessWithQuotaCallback(quota);
	}
	
	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
	}

	private boolean isSharedfolderNotPresent() {
		if(moduleConfig.getStringValue(BCCourseNode.CONFIG_SUBPATH, "").startsWith("/_sharedfolder")){
			if(course.getCourseEnvironment().getCourseFolderContainer().resolve("/_sharedfolder/") == null){
				return true;
			}
		}
		return false;
	}

	private boolean isLinkedFolderNotPresent() {
		VFSContainer courseBase = course.getCourseBaseContainer();
		String subpath = moduleConfig.getStringValue(BCCourseNode.CONFIG_SUBPATH);
		
		VFSItem folder;
		if(subpath != null && subpath.startsWith("/_sharedfolder/")) {
			folder = course.getCourseEnvironment().getCourseFolderContainer().resolve(subpath);
		} else {
			folder = courseBase.resolve("/coursefolder" + subpath);
		}
		return folder == null;
	}
}
