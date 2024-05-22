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
package org.olat.course.assessment.handler;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.course.assessment.handler.AssessmentConfig.FormEvaluationScoreMode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 21 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public interface FormEvaluationHandler extends AssessmentHandler {
	
	EvaluationFormSession getSession(RepositoryEntry courseEntry, CourseNode courseNode, Identity assessedIdentity);
	
	Float getEvaluationScore(EvaluationFormSession session, FormEvaluationScoreMode scoreMode);
	
	@SuppressWarnings("unused")
	default Controller getEvaluationFormController(UserRequest ureq, WindowControl wControl,
			CourseNode courseNode, UserCourseEnvironment coachCourseEnv, UserCourseEnvironment assessedUserCourseEnvironment,
			boolean edit, boolean reopen) {
		return null;
	}
}
