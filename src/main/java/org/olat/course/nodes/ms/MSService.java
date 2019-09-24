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
package org.olat.course.nodes.ms;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.olat.core.id.Identity;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.RubricStatistic;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 11 Jun 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface MSService {
	
	String SURVEY_ORES_TYPE_NAME = "course-ms";

	EvaluationFormSession getOrCreateSession(RepositoryEntry formEntry, RepositoryEntry ores, String nodeIdent,
			Identity assessedIdentity, AuditEnv auditEnv);

	EvaluationFormSession getSession(RepositoryEntry ores, String nodeIdent, Identity assessedIdentity,
			EvaluationFormSessionStatus status);

	EvaluationFormSession getSession(EvaluationFormSessionRef sessionRef);

	EvaluationFormSession closeSession(EvaluationFormSession session, AuditEnv auditEnv);

	EvaluationFormSession reopenSession(EvaluationFormSession session, AuditEnv auditEnv);

	boolean hasSessions(RepositoryEntry ores, String nodeIdent);
	
	List<EvaluationFormSession> getSessions(RepositoryEntry ores, String nodeIdent);
	
	void deleteSession(RepositoryEntry ores, String nodeIdent, Identity assessedIdentity, AuditEnv auditEnv);

	void deleteSessions(RepositoryEntry ores, String nodeIdent);
	
	List<RubricStatistic> getRubricStatistics(EvaluationFormSession session);
	
	Map<String, Map<Rubric, RubricStatistic>> getRubricStatistics(RepositoryEntry ores, String nodeIdent, Form form);
	
	/**
	 * Calculates the possible minimum and maximum sum of all rubrics in the
	 * evaluation form of the formEntry.
	 *
	 * @param formEntry
	 * @return
	 */
	MinMax calculateMinMaxSum(RepositoryEntry formEntry, float scalingFactor);

	/**
	 * Calculates the possible minimum and maximum average of all rubrics in the
	 * evaluation form of the formEntry.
	 *
	 * @param formEntry
	 * @return
	 */
	MinMax calculateMinMaxAvg(RepositoryEntry formEntry, float scalingFactor);

	Float calculateScoreBySum(EvaluationFormSession session);
	
	Float calculateScoreBySum(Form form, Function<Rubric, RubricStatistic> rubricFunction);

	Float calculateScoreByAvg(EvaluationFormSession session);
	
	Float calculateScoreByAvg(Form form, Function<Rubric, RubricStatistic> rubricFunction);

	Float scaleScore(Float score, float scalingFactor);

}
