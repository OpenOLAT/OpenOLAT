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

import java.io.File;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.StackedController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
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
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti.QTIResultDetailsController;
import org.olat.ims.qti.export.QTIExportFormatter;
import org.olat.ims.qti.export.QTIExportFormatterCSVType1;
import org.olat.ims.qti.export.QTIExportManager;
import org.olat.ims.qti.fileresource.SurveyFileResource;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.modules.iq.IQSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

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
	public TabbableController createIQTestEditController(UserRequest ureq, WindowControl wControl, StackedController stackPanel, ICourse course, IQTESTCourseNode courseNode, CourseGroupManager groupMgr, UserCourseEnvironment euce){
		return new IQEditController(ureq, wControl, stackPanel, course, courseNode, groupMgr, euce);
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
	public TabbableController createIQSelftestEditController(UserRequest ureq, WindowControl wControl, StackedController stackPanel, ICourse course, IQSELFCourseNode courseNode, CourseGroupManager groupMgr, UserCourseEnvironment euce){
		return new IQEditController(ureq, wControl, stackPanel, course, courseNode, groupMgr, euce);
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
	public TabbableController createIQSurveyEditController(UserRequest ureq, WindowControl wControl, StackedController stackPanel, ICourse course, IQSURVCourseNode courseNode, CourseGroupManager groupMgr, UserCourseEnvironment euce){
		return new IQEditController(ureq, wControl, stackPanel, course, courseNode, groupMgr, euce);
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
		
		// Do not allow guests to start tests
		Roles roles = ureq.getUserSession().getRoles();
		Translator trans = Util.createPackageTranslator(IQTESTCourseNode.class, ureq.getLocale());
		if (roles.isGuestOnly()) {
			String title = trans.translate("guestnoaccess.title");
			String message = trans.translate("guestnoaccess.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
			IQSecurityCallback sec = new CourseIQSecurityCallback(courseNode, am, ureq.getIdentity());
			RepositoryEntry repositoryEntry = ne.getCourseNode().getReferencedRepositoryEntry();
			OLATResourceable ores = repositoryEntry.getOlatResource();
			Long resId = ores.getResourceableId();
			TestFileResource fr = new TestFileResource();
			fr.overrideResourceableId(resId);
			if(!CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(fr, null)) {
				//QTI1
				controller = new IQRunController(userCourseEnv, courseNode.getModuleConfiguration(), sec, ureq, wControl, courseNode);
			} else {
				String title = trans.translate("editor.lock.title");
				String message = trans.translate("editor.lock.message");
				controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
			}
		}
		
		return controller;
	}
	
	public Controller createIQTestPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne, IQTESTCourseNode courseNode){
		return new IQPreviewController(ureq, wControl, userCourseEnv, courseNode, ne);
	}
	
	public Controller createIQSelftestRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne, IQSELFCourseNode courseNode){
		AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		IQSecurityCallback sec = new CourseIQSecurityCallback(courseNode, am, ureq.getIdentity());
		return  new IQRunController(userCourseEnv, courseNode.getModuleConfiguration(), sec, ureq, wControl, courseNode);		
	}
	
	public Controller createIQSurveyRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne, IQSURVCourseNode courseNode){
		Controller controller = null;
		
		// Do not allow guests to start questionnaires
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			Translator trans = Util.createPackageTranslator(IQSURVCourseNode.class, ureq.getLocale());
			String title = trans.translate("guestnoaccess.title");
			String message = trans.translate("guestnoaccess.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			RepositoryEntry repositoryEntry = ne.getCourseNode().getReferencedRepositoryEntry();
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
		return controller;
		
	}


	public Controller createIQTestDetailsEditController(Long courseResourceableId, String ident, Identity identity,
			RepositoryEntry referencedRepositoryEntry, String qmdEntryTypeAssess, UserRequest ureq, WindowControl wControl) {
		return new QTIResultDetailsController(ureq, wControl, courseResourceableId, ident, identity, referencedRepositoryEntry, qmdEntryTypeAssess);
	}


	@Override
	public boolean archiveIQTestCourseNode(Locale locale, String repositorySoftkey, Long courseResourceableId, String shortTitle,  String ident, File exportDirectory, String charset) {
		QTIExportManager qem = QTIExportManager.getInstance();
		Long repKey = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftkey, true).getKey();
		QTIExportFormatter qef = new QTIExportFormatterCSVType1(locale,"\t", "\"", "\\", "\r\n", false);
		boolean retVal = qem.selectAndExportResults(qef, courseResourceableId, shortTitle, ident, repKey, exportDirectory,charset, ".xls");
		return retVal;
	}

}
