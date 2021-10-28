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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.commons.modules.bc.FolderRunController.Mail;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.UserSession;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.course.CourseModule;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: Apr 22, 2004
 * Updated: Dez 10, 2015
 *
 * @author gnaegi
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 */
public class BCCourseNodeRunController extends BasicController implements Activateable2 {

	private FolderRunController frc;
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;

	public BCCourseNodeRunController(UserRequest ureq, WindowControl wContr, UserCourseEnvironment userCourseEnv, BCCourseNode courseNode, NodeEvaluation ne) {
		super(ureq, wContr);
		
		CourseEnvironment courseEnv = userCourseEnv.getCourseEnvironment();
		CourseGroupManager cgm = courseEnv.getCourseGroupManager();
		UserSession usess = ureq.getUserSession();
		boolean isGuestOnly = usess.getRoles().isGuestOnly();
		// set logger on this run controller
		addLoggingResourceable(LoggingResourceable.wrap(courseNode));

		// offer subscription, but not to guests
		SubscriptionContext nodefolderSubContext = (isGuestOnly ? null : CourseModule.createSubscriptionContext(courseEnv, courseNode));
		boolean noFolder = false;
		VFSContainer target = null;
		VFSSecurityCallback scallback;
		if(courseNode.getModuleConfiguration().getBooleanSafe(BCCourseNode.CONFIG_AUTO_FOLDER)) {
			VFSContainer directory = BCCourseNode.getNodeFolderContainer(courseNode, courseEnv);
			boolean canDownload = courseNode.canDownload(ne);
			boolean canUpload = courseNode.canUpload(userCourseEnv, ne);
			boolean isAdministrator = userCourseEnv.isAdmin();
			scallback = new FolderNodeCallback(directory.getRelPath(), canDownload, canUpload, isAdministrator, isGuestOnly, nodefolderSubContext);
			target = directory;
		} else if(courseNode.isSharedFolder()) {
			String subpath = courseNode.getModuleConfiguration().getStringValue(BCCourseNode.CONFIG_SUBPATH, "");
			VFSItem item = courseEnv.getCourseFolderContainer().resolve(subpath);
			if(item == null){
				noFolder = true;
				BCCourseNodeNoFolderForm noFolderForm = new BCCourseNodeNoFolderForm(ureq, getWindowControl());
				putInitialPanel(noFolderForm.getInitialComponent());
				return;
			} else if(item instanceof VFSContainer){
				target = new NamedContainerImpl(courseNode.getShortTitle(), (VFSContainer) item);
			}
			if(courseEnv.getCourseConfig().isSharedFolderReadOnlyMount()) {
				scallback = new FolderNodeReadOnlyCallback(nodefolderSubContext);
			} else {
				String relPath = BCCourseNode.getNodeFolderContainer(courseNode, courseEnv).getRelPath();
				
				boolean canDownload = courseNode.canDownload(ne);
				boolean canUpload = courseNode.canUpload(userCourseEnv, ne);
				
				String sfSoftkey = courseEnv.getCourseConfig().getSharedFolderSoftkey();
				RepositoryEntry sharedResource = repositoryManager.lookupRepositoryEntryBySoftkey(sfSoftkey, false);
				boolean isAdministrator = repositoryService.hasRoleExpanded(getIdentity(), sharedResource,
						OrganisationRoles.administrator.name(), OrganisationRoles.learnresourcemanager.name(), GroupRoles.owner.name());
				scallback = new FolderNodeCallback(relPath, canDownload, canUpload, isAdministrator, isGuestOnly, nodefolderSubContext);
			}
		} else{
			//create folder automatically if not found
			String subPath = courseNode.getModuleConfiguration().getStringValue(BCCourseNode.CONFIG_SUBPATH);
			VFSContainer courseContainer = courseEnv.getCourseFolderContainer();
			VFSContainer item = VFSManager.resolveOrCreateContainerFromPath(courseContainer, subPath);
			
			String relPath;
			if(item == null) {
				noFolder = true;
				BCCourseNodeNoFolderForm noFolderForm = new BCCourseNodeNoFolderForm(ureq, getWindowControl());
				putInitialPanel(noFolderForm.getInitialComponent());
				return;
			} 
			target = new NamedContainerImpl(courseNode.getShortTitle(), item);
			
			VFSContainer inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(target);
			if (inheritingContainer != null && inheritingContainer.getLocalSecurityCallback() != null
					&& inheritingContainer.getLocalSecurityCallback() .getQuota() != null) {
				relPath = inheritingContainer.getLocalSecurityCallback().getQuota().getPath();
			} else {
				relPath = VFSManager.getRelativeItemPath(target, courseContainer, null);
			}
			boolean canDownload = courseNode.canDownload(ne);
			boolean canUpload = courseNode.canUpload(userCourseEnv, ne);
			boolean isAdministrator = userCourseEnv.isAdmin();
			scallback = new FolderNodeCallback(relPath, canDownload, canUpload, isAdministrator, isGuestOnly, nodefolderSubContext);
		}
		
		//course is read only, override the security callback
		if(userCourseEnv.isCourseReadOnly()) {
			scallback = new FolderNodeReadOnlyCallback(nodefolderSubContext);
		}
		
		if(!noFolder && target != null) {
			target.setLocalSecurityCallback(scallback);

			VFSContainer courseContainer = null;
			if(scallback.canWrite() && scallback.canCopy()) {
				GroupRoles role = GroupRoles.owner;
				if (userCourseEnv.isParticipant()) {
					role = GroupRoles.participant;
				} else if (userCourseEnv.isCoach()) {
					role = GroupRoles.coach;
				}
				if (userCourseEnv.isAdmin() || cgm.hasRight(getIdentity(), CourseRights.RIGHT_COURSEEDITOR, role)) {
					// use course folder as copy source
					courseContainer = courseEnv.getCourseFolderContainer();
				}
			}
	
			VFSContainer olatNamed;
			if(!courseNode.isSharedFolder()){
				String realPath = VFSManager.getRealPath(target);
				String relPath = StringUtils.difference(FolderConfig.getCanonicalRoot(), realPath);
	
				VFSContainer olatRel = VFSManager.olatRootContainer(relPath, null);
				olatNamed = new NamedContainerImpl(target.getName(), olatRel);
				olatNamed.setLocalSecurityCallback(scallback);
			}else{
				String realPath = VFSManager.getRealPath(((NamedContainerImpl)target).getDelegate());
				String relPath = StringUtils.difference(FolderConfig.getCanonicalRoot(), realPath);
	
				VFSContainer olatRel = VFSManager.olatRootContainer(relPath, null);
				olatNamed = new NamedContainerImpl(target.getName(), olatRel);
				olatNamed.setLocalSecurityCallback(scallback);
			}
	
			boolean canMail = !userCourseEnv.isCourseReadOnly();
			frc = new FolderRunController(olatNamed, true, true, Mail.valueOf(canMail), ureq, getWindowControl(), null, null, courseContainer);
			putInitialPanel(frc.getInitialComponent());
		} else {
			BCCourseNodeNoFolderForm noFolderForm = new BCCourseNodeNoFolderForm(ureq, getWindowControl());
			putInitialPanel(noFolderForm.getInitialComponent());
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
	// no events to catch
	}

	@Override
	protected void doDispose() {
		if (frc != null) {
			frc.dispose();
			frc = null;
		}
	}
	
	public void activatePath(UserRequest ureq, String path) {
		if (frc != null) {
			frc.activatePath(ureq, path);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (frc != null) {
			frc.activate(ureq, entries, state);
		}
	}
}
