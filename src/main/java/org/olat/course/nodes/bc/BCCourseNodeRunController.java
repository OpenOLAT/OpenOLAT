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
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.commons.modules.bc.vfs.OlatNamedContainerImpl;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.UserSession;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Initial Date: Apr 22, 2004
 * Updated: Dez 10, 2015
 *
 * @author gnaegi
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 */
public class BCCourseNodeRunController extends DefaultController implements Activateable2 {

	private FolderRunController frc;

	/**
	 * Constructor for a briefcase course building block runtime controller
	 * 
	 * @param ureq
	 * @param userCourseEnv
	 * @param wContr
	 * @param bcCourseNode
	 * @param scallback
	 */
	public BCCourseNodeRunController(UserRequest ureq, WindowControl wContr, UserCourseEnvironment userCourseEnv, BCCourseNode courseNode, NodeEvaluation ne) {
		super(wContr);
		
		CourseEnvironment courseEnv = userCourseEnv.getCourseEnvironment();
		UserSession usess = ureq.getUserSession();
		boolean isOlatAdmin = usess.getRoles().isOLATAdmin();
		boolean isGuestOnly = usess.getRoles().isGuestOnly();
		// set logger on this run controller
		addLoggingResourceable(LoggingResourceable.wrap(courseNode));

		// offer subscription, but not to guests
		SubscriptionContext nodefolderSubContext = (isGuestOnly ? null : CourseModule.createSubscriptionContext(courseEnv, courseNode));
		boolean noFolder = false;
		VFSContainer target = null;
		VFSSecurityCallback scallback;
		if(courseNode.getModuleConfiguration().getBooleanSafe(BCCourseNodeEditController.CONFIG_AUTO_FOLDER)) {
			OlatNamedContainerImpl directory = BCCourseNode.getNodeFolderContainer(courseNode, courseEnv);
			scallback = new FolderNodeCallback(directory.getRelPath(), ne, isOlatAdmin, isGuestOnly, nodefolderSubContext);
			target = directory;
		} else if(courseNode.isSharedFolder()) {
			String subpath = courseNode.getModuleConfiguration().getStringValue(BCCourseNodeEditController.CONFIG_SUBPATH, "");
			VFSItem item = courseEnv.getCourseFolderContainer().resolve(subpath);
			if(item == null){
				noFolder = true;
				BCCourseNodeNoFolderForm noFolderForm = new BCCourseNodeNoFolderForm(ureq, getWindowControl());
				setInitialComponent(noFolderForm.getInitialComponent());
			} else if(item instanceof VFSContainer){
				target = new NamedContainerImpl(courseNode.getShortTitle(), (VFSContainer) item);
			}
			if(courseEnv.getCourseConfig().isSharedFolderReadOnlyMount()) {
				scallback = new FolderNodeReadOnlyCallback(nodefolderSubContext);
			} else {
				String relPath = BCCourseNode.getNodeFolderContainer(courseNode, courseEnv).getRelPath();
				scallback = new FolderNodeCallback(relPath, ne, isOlatAdmin, isGuestOnly, nodefolderSubContext);
			}
		} else{
			//create folder automatically if not found
			String subPath = courseNode.getModuleConfiguration().getStringValue(BCCourseNodeEditController.CONFIG_SUBPATH);
			VFSContainer courseContainer = courseEnv.getCourseFolderContainer();
			VFSContainer item = VFSManager.resolveOrCreateContainerFromPath(courseContainer, subPath);
			
			String relPath;
			if(item == null) {
				noFolder = true;
				BCCourseNodeNoFolderForm noFolderForm = new BCCourseNodeNoFolderForm(ureq, getWindowControl());
				setInitialComponent(noFolderForm.getInitialComponent());
				scallback = new ReadOnlyCallback();
			} else {
				target = new NamedContainerImpl(courseNode.getShortTitle(), item);
				
				VFSContainer inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(target);
				if (inheritingContainer != null && inheritingContainer.getLocalSecurityCallback() != null
						&& inheritingContainer.getLocalSecurityCallback() .getQuota() != null) {
					relPath = inheritingContainer.getLocalSecurityCallback().getQuota().getPath();
				} else {
					relPath = VFSManager.getRelativeItemPath(target, courseContainer, null);
				}
				scallback = new FolderNodeCallback(relPath, ne, isOlatAdmin, isGuestOnly, nodefolderSubContext);
			}
		}
		
		//course is read only, override the security callback
		if(userCourseEnv.isCourseReadOnly()) {
			scallback = new FolderNodeReadOnlyCallback(nodefolderSubContext);
		}
		
		if(!noFolder) {
			target.setLocalSecurityCallback(scallback);

			VFSContainer courseContainer = null;
			if(scallback.canWrite() && scallback.canCopy()) {
				Identity identity = ureq.getIdentity();
				ICourse course = CourseFactory.loadCourse(courseEnv.getCourseResourceableId());
				RepositoryManager rm = RepositoryManager.getInstance();
				RepositoryEntry entry = rm.lookupRepositoryEntry(course, true);
				if (isOlatAdmin || rm.isOwnerOfRepositoryEntry(identity, entry)
						|| courseEnv.getCourseGroupManager().hasRight(identity, CourseRights.RIGHT_COURSEEDITOR)) {
					// use course folder as copy source
					courseContainer = courseEnv.getCourseFolderContainer();
				}
			}
	
			OlatNamedContainerImpl olatNamed;
			if(!courseNode.isSharedFolder()){
				String realPath = VFSManager.getRealPath(target);
				String relPath = StringUtils.difference(FolderConfig.getCanonicalRoot(), realPath);
	
				OlatRootFolderImpl olatRel = new OlatRootFolderImpl(relPath, null);
				olatNamed = new OlatNamedContainerImpl(target.getName(), olatRel);
				olatNamed.setLocalSecurityCallback(scallback);
			}else{
				String realPath = VFSManager.getRealPath(((NamedContainerImpl)target).getDelegate());
				String relPath = StringUtils.difference(FolderConfig.getCanonicalRoot(), realPath);
	
				OlatRootFolderImpl olatRel = new OlatRootFolderImpl(relPath, null);
				olatNamed = new OlatNamedContainerImpl(target.getName(), olatRel);
				olatNamed.setLocalSecurityCallback(scallback);
			}
	
			boolean canMail = !userCourseEnv.isCourseReadOnly();
			frc = new FolderRunController(olatNamed, true, true, canMail, ureq, getWindowControl(), null, null, courseContainer);
			setInitialComponent(frc.getInitialComponent());
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
	// no events to catch
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
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
