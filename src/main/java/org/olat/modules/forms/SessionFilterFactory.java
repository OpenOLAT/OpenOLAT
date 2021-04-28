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
package org.olat.modules.forms;

import static java.util.Collections.singletonList;

import java.util.Collection;
import java.util.Collections;

import org.olat.modules.forms.model.jpa.SessionRefFilter;
import org.olat.modules.forms.model.jpa.SurveysFilter;

/**
 * 
 * Initial date: 10.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SessionFilterFactory {
	
	public static SessionFilter create(EvaluationFormSessionRef sessionRef) {
		return new SessionRefFilter(singletonList(sessionRef));
	}
	
	public static SessionFilter create(Collection<? extends EvaluationFormSessionRef> sessionRefs) {
		return new SessionRefFilter(sessionRefs);
	}

	public static SessionFilter create(EvaluationFormSurveyIdentifier surveyIdentitfier) {
		return new SurveysFilter(surveyIdentitfier);
	}

	public static SessionFilter create(EvaluationFormSurveyIdentifier surveyIdentitfier,
			EvaluationFormSessionStatus status) {
		return new SurveysFilter(surveyIdentitfier, status);
	}
	
	public static SessionFilter create(EvaluationFormSurveyRef survey) {
		return new SurveysFilter(Collections.singletonList(survey));
	}

	public static SessionFilter createSelectDone(EvaluationFormSurveyRef survey) {
		return createSelectDone(Collections.singletonList(survey));
	}

	public static SessionFilter createSelectDone(Collection<? extends EvaluationFormSurveyRef> surveys) {
		return new SurveysFilter(surveys, EvaluationFormSessionStatus.done, false);
	}
	
	public static SessionFilter createSelectDone(EvaluationFormSurveyRef survey, boolean fetchExecutor) {
		return new SurveysFilter(Collections.singletonList(survey), EvaluationFormSessionStatus.done, fetchExecutor);
	}
}
