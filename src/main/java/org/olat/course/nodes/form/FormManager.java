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
package org.olat.course.nodes.form;

import java.io.File;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.ui.EvaluationFormExcelExport;
import org.olat.modules.forms.ui.EvaluationFormExcelExport.UserColumns;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 21.04.2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface FormManager {
	
	public EvaluationFormSurveyIdentifier getSurveyIdentifier(CourseNode courseNode, ICourse course);

	public EvaluationFormSurveyIdentifier getSurveyIdentifier(CourseNode courseNode, RepositoryEntry courseEntry);

	public EvaluationFormSurvey loadSurvey(EvaluationFormSurveyIdentifier surveyIdent);

	public void deleteSurvey(EvaluationFormSurvey survey);

	public boolean isFormUpdateable(EvaluationFormSurvey survey);

	public EvaluationFormSurvey updateSurveyForm(EvaluationFormSurvey survey, RepositoryEntry formEntry);

	public EvaluationFormSurvey createSurvey(EvaluationFormSurveyIdentifier surveyIdent, RepositoryEntry formEntry);

	public Form loadForm(EvaluationFormSurvey survey);

	public File getFormFile(EvaluationFormSurvey survey);

	public DataStorage loadStorage(EvaluationFormSurvey survey);

	public EvaluationFormParticipation loadParticipation(EvaluationFormSurvey survey, Identity identity);

	public EvaluationFormParticipation loadOrCreateParticipation(EvaluationFormSurvey survey, Identity identity);

	public List<EvaluationFormParticipation> getParticipations(EvaluationFormSurvey survey,
			EvaluationFormParticipationStatus status, boolean fetchExecutor);

	public void reopenParticipation(EvaluationFormParticipationRef participationRef, CourseNode courseNode, CourseEnvironment courseEnv);

	public void deleteParticipation(EvaluationFormParticipationRef participationRef, CourseNode courseNode, CourseEnvironment courseEnv);

	public EvaluationFormSession loadOrCreateSession(EvaluationFormParticipation participation);

	public EvaluationFormSession getDoneSession(EvaluationFormSurvey survey, Identity identity);
	
	public void onQuickSave(CourseNode courseNode, UserCourseEnvironment userCourseEnv, Double completion);

	public void onExecutionFinished(CourseNode courseNode, UserCourseEnvironment userCourseEnv);
	
	public List<FormParticipation> getFormParticipations(EvaluationFormSurvey survey,
			UserCourseEnvironment userCourseEnv, FormParticipationSearchParams searchParams);

	public Long getSessionsCount(SessionFilter filter);
	
	public void deleteAllData(EvaluationFormSurvey survey, CourseNode courseNode, UserCourseEnvironment userCourseEnv);

	public EvaluationFormExcelExport getExcelExport(FormCourseNode courseNode, EvaluationFormSurveyIdentifier identifier, UserColumns userColumns);

}
