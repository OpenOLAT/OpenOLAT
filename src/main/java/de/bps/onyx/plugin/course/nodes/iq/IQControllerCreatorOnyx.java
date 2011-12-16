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
* <p>
* Initial code contributed and copyrighted by<br>
* BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
* <p>
*/
package de.bps.onyx.plugin.course.nodes.iq;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.iq.CourseIQSecurityCallback;
import org.olat.course.nodes.iq.IQControllerCreatorOlat;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import de.bps.ims.qti.QTIResultDetailsController;

import org.olat.ims.qti.QTIResultManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.iq.IQSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

import de.bps.onyx.plugin.OnyxExportManager;
import de.bps.onyx.plugin.OnyxModule;
import de.bps.onyx.plugin.run.OnyxRunController;

/**
 * Description:<br>
 * TODO: thomasw Class Description for IQControllerCreatorOnyx
 *
 * <P>
 * Initial Date:  28.06.2010 <br>
 * @author thomasw
 */
public class IQControllerCreatorOnyx extends IQControllerCreatorOlat {

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
	public TabbableController createIQTestEditController(UserRequest ureq, WindowControl wControl, ICourse course, IQTESTCourseNode courseNode, CourseGroupManager groupMgr, UserCourseEnvironment euce){
		return new IQEditController(ureq, wControl, course, courseNode, groupMgr, euce);
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
	public TabbableController createIQSelftestEditController(UserRequest ureq, WindowControl wControl, ICourse course, IQSELFCourseNode courseNode, CourseGroupManager groupMgr, UserCourseEnvironment euce){
		return new IQEditController(ureq, wControl, course, courseNode, groupMgr, euce);
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
	public TabbableController createIQSurveyEditController(UserRequest ureq, WindowControl wControl, ICourse course, IQSURVCourseNode courseNode, CourseGroupManager groupMgr, UserCourseEnvironment euce){
		return new IQEditController(ureq, wControl, course, courseNode, groupMgr, euce);
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
	public Controller createIQTestRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne, IQTESTCourseNode courseNode){

		Controller controller = null;
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		boolean qti2 = config.get(IQEditController.CONFIG_KEY_TYPE_QTI)!=null && config.get(IQEditController.CONFIG_KEY_TYPE_QTI).equals(IQEditController.CONFIG_VALUE_QTI2);

		if (qti2) {
			AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
			IQSecurityCallback sec = new CourseIQSecurityCallback(courseNode, am, ureq.getIdentity());
			controller = new OnyxRunController(userCourseEnv, config, sec, ureq, wControl, courseNode);
		} else {
			controller = super.createIQTestRunController(ureq, wControl, userCourseEnv, ne, courseNode);
		}

		return controller;
	}

	public Controller createIQTestPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne, IQTESTCourseNode courseNode){
		Controller controller = null;
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		boolean qti2 = config.get(IQEditController.CONFIG_KEY_TYPE_QTI)!=null && config.get(IQEditController.CONFIG_KEY_TYPE_QTI).equals(IQEditController.CONFIG_VALUE_QTI2);

		if (qti2) {
			// <OLATCE-1054>
			controller = new OnyxRunController(ureq, wControl, courseNode.getReferencedRepositoryEntry().getOlatResource(), true);
			// </OLATCE-1054>
		} else {
			controller = super.createIQTestPreviewController(ureq, wControl, userCourseEnv, ne, courseNode);
		}
		return controller;
	}

	public Controller createIQSelftestRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne, IQSELFCourseNode courseNode){
		Controller controller = null;
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		boolean qti2 = config.get(IQEditController.CONFIG_KEY_TYPE_QTI)!=null && config.get(IQEditController.CONFIG_KEY_TYPE_QTI).equals(IQEditController.CONFIG_VALUE_QTI2);

		if (qti2) {
			AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
			IQSecurityCallback sec = new CourseIQSecurityCallback(courseNode, am, ureq.getIdentity());
			controller = new OnyxRunController(userCourseEnv, config, sec, ureq, wControl, courseNode);
		} else {
			controller = super.createIQSelftestRunController(ureq, wControl, userCourseEnv, ne, courseNode);
		}
		return controller;
	}

	public Controller createIQSurveyRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne, IQSURVCourseNode courseNode) {
		Controller controller = null;

		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			Translator trans = Util.createPackageTranslator(IQSURVCourseNode.class, ureq.getLocale());
			String title = trans.translate("guestnoaccess.title");
			String message = trans.translate("guestnoaccess.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			ModuleConfiguration config = courseNode.getModuleConfiguration();
			boolean qti2 = config.get(IQEditController.CONFIG_KEY_TYPE_QTI)!=null && config.get(IQEditController.CONFIG_KEY_TYPE_QTI).equals(IQEditController.CONFIG_VALUE_QTI2);

			if (qti2) {
				AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
				IQSecurityCallback sec = new CourseIQSecurityCallback(courseNode, am, ureq.getIdentity());
				controller = new OnyxRunController(userCourseEnv, config, sec, ureq, wControl, courseNode);
			} else {
				controller = super.createIQSurveyRunController(ureq, wControl, userCourseEnv, ne, courseNode);
			}
		}
		return controller;
	}
	
	public Controller createIQTestDetailsEditController(Long courseResourceableId, String ident, Identity identity,
			RepositoryEntry referencedRepositoryEntry, String qmdEntryTypeAssess, UserRequest ureq, WindowControl wControl) {
		return new QTIResultDetailsController(courseResourceableId, ident, identity, referencedRepositoryEntry, qmdEntryTypeAssess, ureq, wControl);
	}


	@Override
	public boolean archiveIQTestCourseNode(Locale locale, String repositorySoftkey, Long courseResourceableId, String shortTitle,  String ident, File exportDirectory, String charset) {
		boolean qti2 = OnyxModule.isOnyxTest(RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftkey, true).getOlatResource());
		if (qti2) {
			ICourse course = CourseFactory.loadCourse(courseResourceableId);
			CourseNode currentCourseNode = course.getRunStructure().getNode(ident);
			Long repKey = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftkey, true).getKey();
			QTIResultManager qrm = QTIResultManager.getInstance();
			List results = qrm.selectResults(courseResourceableId, ident, repKey, 1);
			if(results.size() > 0){
				OnyxExportManager.getInstance().exportResults(results, exportDirectory, currentCourseNode);
			}
			return true;
		} else {
			return super.archiveIQTestCourseNode(locale, repositorySoftkey, courseResourceableId, shortTitle, ident, exportDirectory, charset);
		}	
	}
}

