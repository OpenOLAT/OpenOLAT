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
package org.olat.course.assessment.manager;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.TimerTask;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.i18n.I18nManager;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.DigitalSignatureOptions;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;

/**
 * 
 * Initial date: 16 oct. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PullTestSessionsTask extends TimerTask implements Serializable {
	
	private static final Logger log = Tracing.createLoggerFor(PullTestSessionsTask.class);

	private static final long serialVersionUID = 3863367666724686544L;
	
	private Long coachKey;
	private Long courseEntryKey;
	private List<Long> testSessionKeys;
	
	public PullTestSessionsTask(Long courseEntryKey, List<Long> testSessionKeys, Long coachKey) {
		this.courseEntryKey = courseEntryKey;
		this.testSessionKeys = testSessionKeys;
		this.coachKey = coachKey;
	}

	@Override
	public void run() {
		RepositoryEntry courseEntry = CoreSpringFactory.getImpl(RepositoryService.class)
				.loadByKey(courseEntryKey);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		
		DB dbInstance = CoreSpringFactory.getImpl(DB.class);
		for(Long testSessionKey:testSessionKeys) {
			try {
				pullSession(course, testSessionKey);
			} catch (Exception e) {
				log.error("", e);
			} finally {
				dbInstance.commitAndCloseSession();
			}
		}
	}
	
	private void pullSession(ICourse course, Long testSessionKey) {
		QTI21Service qtiService = CoreSpringFactory.getImpl(QTI21Service.class);
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		InstantMessagingService imService = CoreSpringFactory.getImpl(InstantMessagingService.class);
		AssessmentTestSession session = qtiService.getAssessmentTestSession(testSessionKey);
		if(session == null || session.isCancelled() || session.isExploded()
				|| session.getFinishTime() != null || session.getTerminationTime() != null) {
			return;
		}
		
		Identity identity = session.getIdentity();
		CourseNode node = course.getRunStructure().getNode(session.getSubIdent());
		if(node instanceof IQTESTCourseNode) {
			Object identifier = identity == null ? session.getAnonymousIdentifier() : identity.getKey();
			log.info(Tracing.M_AUDIT, "Retrieve test session async: {} (assessed identity={}) retrieved by coach {}",
					session.getKey(), identifier, coachKey);

			IQTESTCourseNode courseNode = (IQTESTCourseNode)node;
			String language = null;
			if(identity != null) {
				language = identity.getUser().getPreferences().getLanguage();
			}
			
			Identity actor = null;
			if(coachKey != null) {
				actor = securityManager.loadIdentityByKey(coachKey);
			}
			
			Locale locale = CoreSpringFactory.getImpl(I18nManager.class).getLocaleOrDefault(language);
			DigitalSignatureOptions signatureOptions = courseNode.getSignatureOptions(session, locale);
			session = qtiService.pullSession(session, signatureOptions, actor);

			RepositoryEntry courseEntry = session.getRepositoryEntry();
			CourseEnvironment courseEnv = CourseFactory.loadCourse(courseEntry).getCourseEnvironment();
			UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
					.createAndInitUserCourseEnvironment(session.getIdentity(), courseEnv);
			
			if(actor != null) {
				courseNode.pullAssessmentTestSession(session, assessedUserCourseEnv, actor, Role.coach, locale);
			} else {
				courseNode.pullAssessmentTestSession(session, assessedUserCourseEnv, null, Role.auto, locale);
			}
			
			Identity imActor = actor == null ? identity : actor;
			imService.endChannel(imActor, courseEntry.getOlatResource(), node.getIdent(), identifier.toString());
		}
	}
}
