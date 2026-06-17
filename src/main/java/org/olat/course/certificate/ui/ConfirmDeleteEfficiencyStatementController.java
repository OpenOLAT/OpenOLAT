/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.certificate.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.course.assessment.model.AssessmentNodesLastModified;
import org.olat.course.assessment.model.UserEfficiencyStatementLight;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 juin 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ConfirmDeleteEfficiencyStatementController extends ConfirmationController {
	
	private final Long statementKey;
	private final Long certificateKey;
	private final RepositoryEntry entry;
	private final Identity assessedIdentity;

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private EfficiencyStatementManager efficiencyStatementMgr;
	
	public ConfirmDeleteEfficiencyStatementController(UserRequest ureq, WindowControl wControl,
			String message, String confirmation, String confirmButton, Identity assessedIdentity,
			RepositoryEntry entry, Long certificateKey, Long statementKey) {
		super(ureq, wControl, message, confirmation, confirmButton, ButtonType.danger, null, false);
		setTranslator(Util.createPackageTranslator(ConfirmationController.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.statementKey = statementKey;
		this.certificateKey = certificateKey;
		this.assessedIdentity = assessedIdentity;
		initForm(ureq);
	}

	@Override
	protected void doAction(UserRequest ureq) {
		if (statementKey == null) {
			return;
		}
		
		UserEfficiencyStatementLight efficiencyStatement = efficiencyStatementMgr.getUserEfficiencyStatementLightByKey(statementKey);
		if(efficiencyStatement != null) {
			efficiencyStatementMgr.deleteEfficiencyStatement(efficiencyStatement);
		} else if(certificateKey != null) {
			// Delete standalone certificate
			Certificate certificate = certificatesManager.getCertificateById(certificateKey);
			if(certificate.getOlatResource() == null) {
				certificatesManager.deleteStandalonCertificate(certificate);
			}
		}
		
		// Still in a course as participant. Rebuild the efficiency statement
		if(entry != null && repositoryService.hasRoleExpanded(assessedIdentity, entry, GroupRoles.participant.name())) {	
			IdentityEnvironment ienv = new IdentityEnvironment();
			ienv.setIdentity(assessedIdentity);
			ICourse course = CourseFactory.loadCourse(entry);
			CourseEnvironment courseEnv = course.getCourseEnvironment();
			UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, courseEnv);
			Identity assessedIdentity = userCourseEnv.getIdentityEnvironment().getIdentity();
			ScoreAccounting scoreAccounting = userCourseEnv.getScoreAccounting();
			scoreAccounting.evaluateAll();
			
			List<AssessmentNodeData> data = new ArrayList<>(50);
			CourseNode rootNode = courseEnv.getRunStructure().getRootNode();
			AssessmentNodesLastModified lastModifications = new AssessmentNodesLastModified();
			AssessmentHelper.getAssessmentNodeDataList(0, rootNode, scoreAccounting, userCourseEnv, true, true,
					true, data, lastModifications);			
			efficiencyStatementMgr.updateUserEfficiencyStatement(assessedIdentity, courseEnv, data, lastModifications, entry);
		}
	
		showInfo("info.efficiencyStatement.deleted");
		super.doAction(ureq);
	}
}
