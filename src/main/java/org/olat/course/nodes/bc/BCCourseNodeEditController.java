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

package org.olat.course.nodes.bc;

import org.olat.admin.quota.QuotaConstants;
import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.commons.modules.bc.vfs.OlatNamedContainerImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.FullAccessWithQuotaCallback;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.config.CourseConfig;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: Apr 28, 2004
 * Updated: 22 Dez, 2015
 *
 * @author gnaegi
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 */
public class BCCourseNodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	public static final String PANE_TAB_FOLDER = "pane.tab.folder";
	public static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	static final String[] paneKeys = { PANE_TAB_FOLDER, PANE_TAB_ACCESSIBILITY };

	public static final String CONFIG_AUTO_FOLDER = "config.autofolder";
	public static final String CONFIG_SUBPATH = "config.subpath";

	private ICourse course;
	private BCCourseNode bcNode;
	
	private Link vfButton;
	private TabbedPane myTabbedPane;
	private VelocityContainer accessabiliryContent, folderContent;

	private CloseableModalController cmc;
	private FolderRunController folderCtrl;
	private BCCourseNodeEditForm folderPathChoose;
	private ConditionEditController uploaderCondContr, downloaderCondContr;
	
	
	@Autowired
	private QuotaManager quotaManager;

	/**
	 * Constructor for a folder course building block editor controller
	 * 
	 * @param bcNode
	 * @param course
	 * @param ureq
	 * @param wControl
	 */
	public BCCourseNodeEditController(BCCourseNode bcNode, ICourse course, UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment euce) {
		super(ureq,wControl);
		//o_clusterOK by guido: inside course editor its save to have a reference to the course
		this.course = course;
		this.bcNode = bcNode;
		myTabbedPane = null;
		
		accessabiliryContent = createVelocityContainer("edit");

		// Uploader precondition
		Condition uploadCondition = bcNode.getPreConditionUploaders();
		uploaderCondContr = new ConditionEditController(ureq, getWindowControl(), euce,
				uploadCondition, AssessmentHelper
						.getAssessableNodes(course.getEditorTreeModel(), bcNode));		
		listenTo(uploaderCondContr);

		CourseConfig courseConfig = course.getCourseConfig();
		if(bcNode.getModuleConfiguration().getStringValue(CONFIG_SUBPATH, "").startsWith("/_sharedfolder")
				&& courseConfig.isSharedFolderReadOnlyMount()) {
			accessabiliryContent.contextPut("uploadable", false);
		} else {
			accessabiliryContent.contextPut("uploadable", true);
		}
		accessabiliryContent.put("uploadCondition", uploaderCondContr.getInitialComponent());

		// Uploader precondition
		Condition downloadCondition = bcNode.getPreConditionDownloaders();
		downloaderCondContr = new ConditionEditController(ureq, getWindowControl(), euce,
				downloadCondition, AssessmentHelper
						.getAssessableNodes(course.getEditorTreeModel(), bcNode));
		listenTo(downloaderCondContr);
		accessabiliryContent.put("downloadCondition", downloaderCondContr.getInitialComponent());

		folderContent = createVelocityContainer("folder");
		folderPathChoose = new BCCourseNodeEditForm(ureq, wControl, bcNode, course);
		listenTo(folderPathChoose);
		folderContent.put("pathChooser", folderPathChoose.getInitialComponent());

		vfButton = LinkFactory.createButton("folder.view", folderContent, this);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == vfButton){
			doOpenFolder(ureq);
		}
	}
	
	private void doOpenFolder(UserRequest ureq) {
		VFSContainer namedContainer = null;
		if(bcNode.getModuleConfiguration().getBooleanSafe(CONFIG_AUTO_FOLDER)){
			OlatNamedContainerImpl directory = BCCourseNode.getNodeFolderContainer(bcNode, course.getCourseEnvironment());
			directory.setLocalSecurityCallback(getSecurityCallbackWithQuota(directory.getRelPath()));
			namedContainer = directory;
		} else {
			VFSContainer courseContainer = course.getCourseFolderContainer();
			String path = bcNode.getModuleConfiguration().getStringValue(CONFIG_SUBPATH, "");
			VFSItem pathItem = courseContainer.resolve(path);
			if(pathItem instanceof VFSContainer){
				namedContainer = (VFSContainer) pathItem;
				if(bcNode.isSharedFolder()) {
					if(course.getCourseConfig().isSharedFolderReadOnlyMount()) {
						namedContainer.setLocalSecurityCallback(new ReadOnlyCallback());
					} else {
						String relPath = BCCourseNode.getNodeFolderContainer(bcNode, course.getCourseEnvironment()).getRelPath();
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
			folderCtrl = new FolderRunController(namedContainer, false, ureq, getWindowControl());
			listenTo(folderCtrl);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), folderCtrl.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private VFSSecurityCallback getSecurityCallbackWithQuota(String relPath) {
		Quota quota = quotaManager.getCustomQuota(relPath);
		if (quota == null) {
			Quota defQuota = QuotaManager.getInstance().getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_NODES);
			quota = QuotaManager.getInstance().createQuota(relPath, defQuota.getQuotaKB(), defQuota.getUlLimitKB());
		}
		return new FullAccessWithQuotaCallback(quota);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == uploaderCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = uploaderCondContr.getCondition();
				bcNode.setPreConditionUploaders(cond);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == downloaderCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = downloaderCondContr.getCondition();
				bcNode.setPreConditionDownloaders(cond);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == folderPathChoose){
			if(bcNode.getModuleConfiguration().getStringValue(CONFIG_SUBPATH, "").startsWith("/_sharedfolder")){
				accessabiliryContent.contextPut("uploadable", false);
			} else {
				accessabiliryContent.contextPut("uploadable", true);
			}
			fireEvent(urequest, event);
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(folderCtrl);
		removeAsListenerAndDispose(cmc);
		folderCtrl = null;
		cmc = null;
	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.TabbableDefaultController#addTabs(org.olat.core.gui.components.TabbedPane)
	 */
	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessabiliryContent);
		tabbedPane.addTab(translate(PANE_TAB_FOLDER), folderContent);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}
}