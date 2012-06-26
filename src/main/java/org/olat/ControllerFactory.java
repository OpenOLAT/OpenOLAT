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

package org.olat;

import java.util.Locale;

import org.olat.admin.SystemAdminMainController;
import org.olat.admin.UserAdminMainController;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.chiefcontrollers.BaseChiefController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.nodes.ta.DropboxController;
import org.olat.course.nodes.ta.ReturnboxController;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.ui.BGControllerFactory;
import org.olat.group.ui.main.BGMainController;
import org.olat.home.HomeMainController;
import org.olat.home.InviteeHomeMainController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoyUIFactory;
import org.olat.repository.controllers.RepositoryMainController;
import org.olat.test.GUIDemoMainController;


/**
 *  Initial Date:  May 6, 2004
 *  @author gnaegi
 */
public class ControllerFactory {
	/**
	 * Create a controller for a specific OLAT resource
	 * 
	 * @param olatResourceable The OLAT resource
	 * @param initialViewIdentifier if null the default view will be started,
	 *          otherwise a controllerfactory type dependant view will be
	 *          activated (subscription subtype)
	 * @param ureq
	 * @param wControl
	 * @param exceptIfNoneFound if true, then an exception will be thrown if no
	 *          controller could be created for whatever reason. if false, the
	 *          call will just return null.
	 * @return the created controller
	 */
	public static MainLayoutController createLaunchController(OLATResourceable olatResourceable, String initialViewIdentifier, UserRequest ureq,
			WindowControl wControl, boolean exceptIfNoneFound) {
		Roles roles = ureq.getUserSession().getRoles();
		if (olatResourceable == null) {
			// special case after login: guest will get guesthome, other user the
			// normal home
			if (ureq.getUserSession().getRoles().isGuestOnly()) return new HomeMainController(ureq, wControl);

		} else if (OresHelper.isOfType(olatResourceable, BGMainController.class)) {
			if (roles.isGuestOnly()) throw new OLATSecurityException("Tried to launch a BuddyGroupMainController, but is in guest group " + roles);
			return BGControllerFactory.getInstance().createBuddyGroupMainController(ureq, wControl);

		} else if (OresHelper.isOfType(olatResourceable, BusinessGroup.class)) {
			if (roles.isGuestOnly()) throw new OLATSecurityException("Tried to launch a BusinessGroup, but is in guest group " + roles);
			BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
			BusinessGroup bg = bgs.loadBusinessGroup(olatResourceable.getResourceableId());
			boolean isOlatAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
			// check if allowed to start (must be member or admin)
			if (isOlatAdmin || bgs.isIdentityInBusinessGroup(ureq.getIdentity(), bg)) {	
				// only olatadmins or admins of this group can administer this group
				return BGControllerFactory.getInstance().createRunControllerFor(ureq, wControl, bg, isOlatAdmin,
						initialViewIdentifier);
			}
			// else skip

		} else if (OresHelper.isOfType(olatResourceable, RepositoryEntry.class)) {
			// it this is a respository type, the resourceableID is the repository
			// entry key
			RepositoryManager rm = RepositoryManager.getInstance();
			RepositoryEntry re = rm.lookupRepositoryEntry(olatResourceable.getResourceableId());
			MainLayoutController ctrl = RepositoyUIFactory.createLaunchController(re, initialViewIdentifier, ureq, wControl);
			if (ctrl != null) return ctrl;

		} else if (OresHelper.isOfType(olatResourceable, CourseModule.class)) {
			// gets called by subscription launcher
			//FIXME:fj:make it clearer here: subscriptioncontext is always given by
			// the surrounding resource, but for launching we always need the
			// repoentry
			// if it is a course, we also take the repository entry, since a course
			// can only be called by a repoentry
			RepositoryManager rm = RepositoryManager.getInstance();
			RepositoryEntry re = rm.lookupRepositoryEntry(olatResourceable, false);
			MainLayoutController ctrl = RepositoyUIFactory.createLaunchController(re, initialViewIdentifier, ureq, wControl);
			if (ctrl != null) return ctrl;
		} else if (OresHelper.isOfType(olatResourceable, AssessmentManager.class)) {
			// gets called by subscription launcher
			//FIXME:fj:make it clearer here: subscriptioncontext is always given by
			// the surrounding resource, but for launching we always need the
			// repoentry
			// if it is a course, we also take the repository entry, since a course
			// can only be called by a repoentry
			RepositoryManager rm = RepositoryManager.getInstance();
			// launch course with view identifyer assessment tool. however notifications 
			// publisher data provides not existing assessmentManager resource
			OLATResourceable fakedCourseResource = OresHelper.createOLATResourceableInstance(CourseModule.class, olatResourceable.getResourceableId());
			RepositoryEntry re = rm.lookupRepositoryEntry(fakedCourseResource, false);
			MainLayoutController ctrl = RepositoyUIFactory.createLaunchController(re, "assessmentTool", ureq, wControl);
			
			if (ctrl != null) return ctrl;
		} else if (OresHelper.isOfType(olatResourceable, DropboxController.class)) {
			// JumpIn-handling for task-dropbox notification 
			RepositoryManager rm = RepositoryManager.getInstance();
			OLATResourceable fakedCourseResource = OresHelper.createOLATResourceableInstance(CourseModule.class, olatResourceable.getResourceableId());
			RepositoryEntry re = rm.lookupRepositoryEntry(fakedCourseResource,false);
			if (re == null) {
				return null;// found no repositoryEntry => return null
			}
			MainLayoutController ctrl = RepositoyUIFactory.createLaunchController(re, "assessmentTool:nodeChoose", ureq, wControl);	
			if (ctrl != null) return ctrl;
		} else if (OresHelper.isOfType(olatResourceable, ReturnboxController.class)) {
			// JumpIn-handling for task-returnbox notification 
			RepositoryManager rm = RepositoryManager.getInstance();
			OLATResourceable fakedCourseResource = OresHelper.createOLATResourceableInstance(CourseModule.class, olatResourceable.getResourceableId());
			RepositoryEntry re = rm.lookupRepositoryEntry(fakedCourseResource, false);
			MainLayoutController ctrl = RepositoyUIFactory.createLaunchController(re, initialViewIdentifier, ureq, wControl);	
			if (ctrl != null) return ctrl;
		}

		// --- repository ---
		else if (OresHelper.isOfType(olatResourceable, RepositoryMainController.class)) {
			return new RepositoryMainController(ureq, wControl);
		}
		// --- home ---
		else if (OresHelper.isOfType(olatResourceable, HomeMainController.class)) {
			return new HomeMainController(ureq, wControl);
		} else if (OresHelper.isOfType(olatResourceable, SystemAdminMainController.class)) {
			if (!roles.isOLATAdmin()) throw new OLATSecurityException("Tried to launch a SystemAdminMainController, but is not in admin group " + roles);
			return new SystemAdminMainController(ureq, wControl);
		} else if (OresHelper.isOfType(olatResourceable, UserAdminMainController.class)) {
			if (!roles.isUserManager()) throw new OLATSecurityException("Tried to launch a UserAdminMainController, but is not in admin group " + roles);
			return new UserAdminMainController(ureq, wControl);
		} else if (OresHelper.isOfType(olatResourceable, InviteeHomeMainController.class)) {
			if (!roles.isInvitee()) throw new OLATSecurityException("Tried to launch a InviteeMainController, but is not an invitee " + roles);
			return new InviteeHomeMainController(ureq, wControl);
		} else if (OresHelper.isOfType(olatResourceable, GUIDemoMainController.class)) {
			if (!roles.isOLATAdmin()) throw new OLATSecurityException("Tried to launch a GUIDemoMainController, but is not in admin group "
					+ roles);
			return new GUIDemoMainController(ureq, wControl);
		} else { // ask the handlerfactory of the repository if it can handle it
			// a repository entry ?
			RepositoryManager rm = RepositoryManager.getInstance();
			//OLAT-1842
			//if the LaunchController is created from a link click in a 
			//notification list/email then we cannot be strict in the repoentry lookup.
			//Because the notification can live longer then the resource it is pointing to.
			RepositoryEntry re = rm.lookupRepositoryEntry(olatResourceable, false);
			//but now we have to handle the NULL case.
			//from the method signature it follows that one can choose if the method
			//should throw the "Unable to creat..." exception or to return null.
			//Hence it follows to set the ctrl == null if no repoentry found.
			MainLayoutController ctrl;
			if(re == null){
				ctrl = null;
			}else{
				ctrl = RepositoyUIFactory.createLaunchController(re, initialViewIdentifier, ureq, wControl);
			}
			if (ctrl != null) return ctrl;
		}
		
		if (exceptIfNoneFound) {
			throw new AssertException("Unable to create launch controller for resourceable: " + olatResourceable.getResourceableTypeName() + ", "
					+ olatResourceable.getResourceableId());
		} else {
			return null;
		}
	}

	/**
	 * Translate a resourceableTypeName.
	 * 
	 * @param resourceableTypeName
	 * @param locale
	 * @return the String representing the type in the given locale
	 */
	public static String translateResourceableTypeName(String resourceableTypeName, Locale locale) {
		//REVIEW:12-2007:CodeCleanup
		//TODO: cache per locale?? -> Task for i18n manager
		Translator trans = Util.createPackageTranslator(BaseChiefController.class, locale);
		String tr = trans.translate(resourceableTypeName);
		return tr;
		//REVIEW:12-2007:CodeCleanup
		//Localization.getInstance().getString("org.olat", resourceableTypeName, null, locale);
	}

	
	

}