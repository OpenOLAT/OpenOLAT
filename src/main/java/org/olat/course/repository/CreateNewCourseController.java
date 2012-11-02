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

package org.olat.course.repository;

import java.io.File;
import java.util.UUID;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.PersistingCourseGroupManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.IAddController;
import org.olat.repository.controllers.RepositoryAddCallback;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;

/**
 * Description:<BR/>
 * Implementation of the repository add controller for OLAT courses
 * <P/>
 * Initial Date:  Oct 12, 2004
 *
 * @author gnaegi
 */
public class CreateNewCourseController extends BasicController implements IAddController {

	//private static final String PACKAGE_REPOSITORY = Util.getPackageName(RepositoryManager.class);
	private OLATResource newCourseResource;
	private ICourse course;//o_clusterOK: creation process


	/**
	 * Constructor for the add course controller
	 * @param addCallback
	 * @param ureq
	 */
	public CreateNewCourseController(RepositoryAddCallback addCallback, UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		setBasePackage(RepositoryManager.class);
		
		// do prepare course now
		newCourseResource = OLATResourceManager.getInstance().createOLATResourceInstance(CourseModule.class);
		if (addCallback != null) {
			addCallback.setResourceable(newCourseResource);
			addCallback.setDisplayName(translate(newCourseResource.getResourceableTypeName()));
			addCallback.setResourceName("-");
			addCallback.finished(ureq);
		}
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#getTransactionComponent()
	 */
	public Component getTransactionComponent() {
		return getInitialComponent();
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#transactionFinishBeforeCreate()
	 */
	public boolean transactionFinishBeforeCreate() {
		// Create course and persist course resourceable.
		course = CourseFactory.createEmptyCourse(newCourseResource, "New Course", "New Course", "");
		return true;
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#transactionAborted()
	 */
	public void transactionAborted() {
		// Nothing to do here... no course has been created yet.
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to listen to
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		// nothing to listen to
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#repositoryEntryCreated(org.olat.repository.RepositoryEntry)
	 */
	public void repositoryEntryCreated(RepositoryEntry re) {
		// Create course admin policy for owner group of repository entry
		// -> All owners of repository entries are course admins
		BaseSecurity secMgr = BaseSecurityManager.getInstance();
		secMgr.createAndPersistPolicy(re.getOwnerGroup(), Constants.PERMISSION_ADMIN, re.getOlatResource());
		//fxdiff VCRP-1,2: access control of resources
		secMgr.createAndPersistPolicy(re.getParticipantGroup(), Constants.PERMISSION_PARTI, re.getOlatResource());
		secMgr.createAndPersistPolicy(re.getTutorGroup(), Constants.PERMISSION_COACH, re.getOlatResource());
		// set root node title
				
		course = CourseFactory.openCourseEditSession(re.getOlatResource().getResourceableId());
		String displayName = re.getDisplayname();
		course.getRunStructure().getRootNode().setShortTitle(Formatter.truncateOnly(displayName, 25)); //do not use truncate!
		course.getRunStructure().getRootNode().setLongTitle(displayName);
		
		CourseNode rootNode = ((CourseEditorTreeNode)course.getEditorTreeModel().getRootNode()).getCourseNode();
		rootNode.setShortTitle(Formatter.truncateOnly(displayName, 25)); //do not use truncate!
		rootNode.setLongTitle(displayName);
		
		CourseFactory.saveCourse(course.getResourceableId());
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
	}

	@Override
	public void repositoryEntryCopied(RepositoryEntry sourceEntry, RepositoryEntry newEntry) {
		ICourse sourceCourse = CourseFactory.loadCourse(sourceEntry.getOlatResource().getResourceableId());
		CourseGroupManager sourceCgm = sourceCourse.getCourseEnvironment().getCourseGroupManager();
		CourseEnvironmentMapper env = PersistingCourseGroupManager.getInstance(sourceCourse).getBusinessGroupEnvironment();
		
		File fExportDir = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
		fExportDir.mkdirs();
		sourceCgm.exportCourseBusinessGroups(fExportDir, env, false);

		course = CourseFactory.loadCourse(newEntry.getOlatResource().getResourceableId());
		CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
		// import groups
		CourseEnvironmentMapper envMapper = cgm.importCourseBusinessGroups(fExportDir);
		//upgrade to the current version of the course
		course = CourseFactory.loadCourse(cgm.getCourseResource());
		course.postImport(envMapper);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//nothing to do here
	}
}
