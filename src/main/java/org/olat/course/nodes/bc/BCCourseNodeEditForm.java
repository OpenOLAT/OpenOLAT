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

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.BCCourseNode;

/**
 * Initial Date: Apr 28, 2004
 *
 * @author gnaegi
 */
public class BCCourseNodeEditForm extends FormBasicController implements ControllerEventListener{

	private SingleSelection folderTargetChoose;
	private FormLink chooseFolder;
	private StaticTextElement subPath;
	private BCCourseNode node;
	private ICourse course;
	private CloseableModalController cmc;
	private FormLink createFolder;
	private BCCourseNodeEditCreateFolderForm createFolderForm;
	private FormItem sharedFolderWarning, sharedFolderInfo;
	private FormItem linkedFolderWarning;
	private BCCourseNodeEditChooseFolderForm chooseForm;

	public BCCourseNodeEditForm(UserRequest ureq, WindowControl wControl, BCCourseNode bcNode, ICourse course) {
		super(ureq, wControl);
		node = bcNode;
		this.course = course;
		initForm(ureq);
		validate();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		String[] keys = {"autoPath", "pathChoose"};
		String[] values= {translate("pathChoose.auto"), translate("pathChoose.custom")};
		folderTargetChoose = uifactory.addRadiosVertical("pathChoose", formLayout, keys, values);

		folderTargetChoose.addActionListener(FormEvent.ONCLICK);
		subPath = uifactory.addStaticTextElement("subPathLab.label", translate("subPathLab.dummy"), formLayout);

		sharedFolderInfo = uifactory.addStaticExampleText("warning","", "<div class=\"o_important\">"+translate("info.sharedfolder")+"</div>",formLayout);
		sharedFolderWarning = uifactory.createSimpleErrorText("warning", translate("warning.no.sharedfolder"));
		formLayout.add(sharedFolderWarning);

		linkedFolderWarning = uifactory.createSimpleErrorText("warning2", translate("warning.no.linkedfolder"));
		formLayout.add(linkedFolderWarning);

		boolean isAuto = node.getModuleConfiguration().getBooleanSafe(BCCourseNode.CONFIG_AUTO_FOLDER);

		if(isAuto) {
			folderTargetChoose.select("autoPath", true);
			subPath.setVisible(false);
		} else {
			folderTargetChoose.select("pathChoose", false);
			String subpath = node.getModuleConfiguration().getStringValue(BCCourseNode.CONFIG_SUBPATH);

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

		FormLayoutContainer buttons2Cont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttons2Cont);

		chooseFolder = uifactory.addFormLink("chooseFolder", buttons2Cont, Link.BUTTON);
		chooseFolder.setVisible(folderTargetChoose.isSelected(1));

		createFolder = uifactory.addFormLink("createFolder", buttons2Cont, Link.BUTTON);
		createFolder.setVisible(folderTargetChoose.isSelected(1));
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
				node.getModuleConfiguration().setBooleanEntry(BCCourseNode.CONFIG_AUTO_FOLDER, false);
				String path = node.getModuleConfiguration().getStringValue(BCCourseNode.CONFIG_SUBPATH);
				if(StringHelper.containsNonWhitespace(path)){
					subPath.setValue(path);
				}else{
					subPath.setValue(translate("subPathLab.dummy"));
				}
			}else{
				node.getModuleConfiguration().setBooleanEntry(BCCourseNode.CONFIG_AUTO_FOLDER, true);
				node.getModuleConfiguration().setStringValue(BCCourseNode.CONFIG_SUBPATH, "");
			}
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			createFolder.setVisible(folderTargetChoose.isSelected(1));
		} else if(source == createFolder){
			createFolderForm = new BCCourseNodeEditCreateFolderForm(ureq, getWindowControl(), course, node);
			listenTo(createFolderForm);

			String title = translate("createFolder");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), createFolderForm.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		} else if (source == chooseFolder){
			VFSContainer namedContainer = course.getCourseFolderContainer();

			chooseForm = new BCCourseNodeEditChooseFolderForm(ureq, getWindowControl(), namedContainer);
			listenTo(chooseForm);

			String title = translate("chooseFolder");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), chooseForm.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
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
				node.getModuleConfiguration().setStringValue(BCCourseNode.CONFIG_SUBPATH, subpath);
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
				node.getModuleConfiguration().setStringValue(BCCourseNode.CONFIG_SUBPATH, subpath);
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
		removeAsListenerAndDispose(cmc);
		createFolderForm = null;
		chooseForm = null;
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

	private boolean isSharedfolderNotPresent() {
		if(node.getModuleConfiguration().getStringValue(BCCourseNode.CONFIG_SUBPATH, "").startsWith("/_sharedfolder")){
			if(course.getCourseEnvironment().getCourseFolderContainer().resolve("/_sharedfolder/") == null){
				return true;
			}
		}
		return false;
	}

	private boolean isLinkedFolderNotPresent() {
		VFSContainer courseBase = course.getCourseBaseContainer();
		String subpath = node.getModuleConfiguration().getStringValue(BCCourseNode.CONFIG_SUBPATH);
		
		VFSItem folder;
		if(subpath != null && subpath.startsWith("/_sharedfolder/")) {
			folder = course.getCourseEnvironment().getCourseFolderContainer().resolve(subpath);
		} else {
			folder = courseBase.resolve("/coursefolder" + subpath);
		}
		return folder == null;
	}
}
