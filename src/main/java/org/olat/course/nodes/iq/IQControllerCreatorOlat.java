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
package org.olat.course.nodes.iq;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti.fileresource.SurveyFileResource;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.iq.IQSecurityCallback;
import org.olat.repository.RepositoryEntry;

import de.bps.onyx.plugin.run.OnyxRunController;

/**
 * Description:<br>
 * TODO: patrickb Class Description for IQControllerCreatorOlat
 * 
 * <P>
 * Initial Date:  18.06.2010 <br>
 * @author patrickb
 */
public class IQControllerCreatorOlat implements IQControllerCreator {

	/**
	 * The iq test edit screen in the course editor.
	 * @param ureq
	 * @param wControl
	 * @param course
	 * @param courseNode
	 * @param groupMgr
	 * @param euce
	 * @return
	 */
	public TabbableController createIQTestEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, IQTESTCourseNode courseNode, CourseGroupManager groupMgr, UserCourseEnvironment euce){
		return new IQEditController(ureq, wControl, stackPanel, course, courseNode, euce);
	}
	

	/**
	 * The iq test edit screen in the course editor.
	 * @param ureq
	 * @param wControl
	 * @param course
	 * @param courseNode
	 * @param groupMgr
	 * @param euce
	 * @return
	 */
	public TabbableController createIQSelftestEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, IQSELFCourseNode courseNode, CourseGroupManager groupMgr, UserCourseEnvironment euce){
		return new IQEditController(ureq, wControl, stackPanel, course, courseNode, euce);
	}
	

	/**
	 * The iq test edit screen in the course editor.
	 * @param ureq
	 * @param wControl
	 * @param course
	 * @param courseNode
	 * @param groupMgr
	 * @param euce
	 * @return
	 */
	public TabbableController createIQSurveyEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, IQSURVCourseNode courseNode, CourseGroupManager groupMgr, UserCourseEnvironment euce){
		return new IQEditController(ureq, wControl, stackPanel, course, courseNode, euce);
	}
	
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param userCourseEnv
	 * @param ne
	 * @param courseNode
	 * @return
	 */
	@Override
	public Controller createIQTestRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, IQTESTCourseNode courseNode) {
		Controller controller;
		// Do not allow guests to start tests
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			Translator trans = Util.createPackageTranslator(IQTESTCourseNode.class, ureq.getLocale());
			String title = trans.translate("guestnoaccess.title");
			String message = trans.translate("guestnoaccess.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			ModuleConfiguration config = courseNode.getModuleConfiguration();
			boolean onyx = IQEditController.CONFIG_VALUE_QTI2.equals(config.get(IQEditController.CONFIG_KEY_TYPE_QTI));
			if (onyx) {
				controller = new OnyxRunController(userCourseEnv, config, ureq, wControl, courseNode);
			} else {
				AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
				IQSecurityCallback sec = new CourseIQSecurityCallback(courseNode, am, ureq.getIdentity());
				RepositoryEntry repositoryEntry = courseNode.getReferencedRepositoryEntry();
				if(repositoryEntry == null) {
					Translator trans = Util.createPackageTranslator(IQControllerCreatorOlat.class, ureq.getLocale());
					String title = trans.translate("error.test.undefined.short", new String[]{ courseNode.getShortTitle() });
					String message = trans.translate("error.test.undefined.long", new String[]{ courseNode.getShortTitle() });
					controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
				} else {
					OLATResourceable ores = repositoryEntry.getOlatResource();
					Long resId = ores.getResourceableId();
					TestFileResource fr = new TestFileResource();
					fr.overrideResourceableId(resId);
					if(!CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(fr, null)) {
						//QTI1
						controller = new IQRunController(userCourseEnv, courseNode.getModuleConfiguration(), sec, ureq, wControl, courseNode);
					} else {
						Translator trans = Util.createPackageTranslator(IQTESTCourseNode.class, ureq.getLocale());
						String title = trans.translate("editor.lock.title");
						String message = trans.translate("editor.lock.message");
						controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
					}
				}
			}
		}
		return controller;
	}
	
	@Override
	public Controller createIQTestPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, IQTESTCourseNode courseNode) {
		Controller controller;
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		boolean onyx = IQEditController.CONFIG_VALUE_QTI2.equals(config.get(IQEditController.CONFIG_KEY_TYPE_QTI));
		if (onyx) {
			controller = new OnyxRunController(ureq, wControl, courseNode);
		} else {
			controller = new IQPreviewController(ureq, wControl, userCourseEnv, courseNode);
		}
		return controller;
	}

	@Override
	public Controller createIQSelftestRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, IQSELFCourseNode courseNode) {
		Controller controller;
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		boolean onyx = IQEditController.CONFIG_VALUE_QTI2.equals(config.get(IQEditController.CONFIG_KEY_TYPE_QTI));
		if (onyx) {
			controller = new OnyxRunController(userCourseEnv, config, ureq, wControl, courseNode);
		} else {
			IQSecurityCallback sec = new CourseIQSecurityCallback(courseNode, am, ureq.getIdentity());
			controller = new IQRunController(userCourseEnv, courseNode.getModuleConfiguration(), sec, ureq, wControl, courseNode);
		}
		return controller;
	}

	@Override
	public Controller createIQSurveyRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, IQSURVCourseNode courseNode) {
		Controller controller;
		
		// Do not allow guests to start questionnaires
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			Translator trans = Util.createPackageTranslator(IQSURVCourseNode.class, ureq.getLocale());
			String title = trans.translate("guestnoaccess.title");
			String message = trans.translate("guestnoaccess.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			ModuleConfiguration config = courseNode.getModuleConfiguration();
			boolean onyx = IQEditController.CONFIG_VALUE_QTI2.equals(config.get(IQEditController.CONFIG_KEY_TYPE_QTI));
			if (onyx) {
				controller = new OnyxRunController(userCourseEnv, config, ureq, wControl, courseNode);
			} else {
				RepositoryEntry repositoryEntry = courseNode.getReferencedRepositoryEntry();
				OLATResourceable ores = repositoryEntry.getOlatResource();
				Long resId = ores.getResourceableId();
				SurveyFileResource fr = new SurveyFileResource();
				fr.overrideResourceableId(resId);
				if(!CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(fr, null)) {
					AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
					IQSecurityCallback sec = new CourseIQSecurityCallback(courseNode, am, ureq.getIdentity());
					controller = new IQRunController(userCourseEnv, courseNode.getModuleConfiguration(), sec, ureq, wControl, courseNode);
				} else {
					Translator trans = Util.createPackageTranslator(IQSURVCourseNode.class, ureq.getLocale());
					String title = trans.translate("editor.lock.title");
					String message = trans.translate("editor.lock.message");
					controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
				}
			}
		}
		return controller;
	}
}