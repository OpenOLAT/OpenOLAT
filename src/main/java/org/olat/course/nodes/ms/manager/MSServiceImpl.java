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
package org.olat.course.nodes.ms.manager;

import static org.olat.modules.forms.EvaluationFormSurveyIdentifier.of;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.course.nodes.ms.MSService;
import org.olat.course.nodes.ms.MinMax;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationIdentifier;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.forms.RubricStatistic;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.modules.forms.SliderStatistic;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 11 Jun 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MSServiceImpl implements MSService {
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	@Override
	public EvaluationFormSession getOrCreateSession(RepositoryEntry formEntry, RepositoryEntry ores, String nodeIdent,
			Identity assessedIdentity) {
		EvaluationFormSurveyIdentifier surveyIdent = of(ores, nodeIdent, assessedIdentity.getKey().toString());
		EvaluationFormSurvey survey = loadOrCreateSurvey(formEntry, surveyIdent);
		EvaluationFormParticipation participation = loadOrCreateParticipation(survey);
		return loadOrCreateSesssion(participation);
	}
	
	private EvaluationFormSurvey loadOrCreateSurvey(RepositoryEntry formEntry, EvaluationFormSurveyIdentifier surveyIdent) {
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(surveyIdent);
		if (survey == null) {
			survey = evaluationFormManager.createSurvey(surveyIdent, formEntry);
		}
		return survey;
	}

	private EvaluationFormParticipation loadOrCreateParticipation(EvaluationFormSurvey survey) {
		// All coaches have to edit the same participation. So use the same identifier for all.
		EvaluationFormParticipationIdentifier identifier = new EvaluationFormParticipationIdentifier("ms-course-node", "1");
		EvaluationFormParticipation loadedParticipation = evaluationFormManager.loadParticipationByIdentifier(survey, identifier);
		if (loadedParticipation == null) {
			loadedParticipation = evaluationFormManager.createParticipation(survey, identifier);
		}
		return loadedParticipation;
	}

	private EvaluationFormSession loadOrCreateSesssion(EvaluationFormParticipation participation) {
		EvaluationFormSession session = evaluationFormManager.loadSessionByParticipation(participation);
		if (session == null) {
			session = evaluationFormManager.createSession(participation);
		}
		return session;
	}

	@Override
	public EvaluationFormSession getSession(EvaluationFormSessionRef sessionRef) {
		return evaluationFormManager.loadSessionByKey(sessionRef);
	}

	@Override
	public EvaluationFormSession closeSession(EvaluationFormSession session) {
		return evaluationFormManager.finishSession(session);
	}

	@Override
	public EvaluationFormSession reopenSession(EvaluationFormSession session) {
		return evaluationFormManager.reopenSession(session);
	}

	@Override
	public boolean hasSessions(OLATResourceable ores, String nodeIdent) {
		return !evaluationFormManager.loadSurveys(of(ores, nodeIdent)).isEmpty();
	}

	@Override
	public void deleteSessions(RepositoryEntry ores, String nodeIdent) {
		List<EvaluationFormSurvey> surveys = evaluationFormManager.loadSurveys(of(ores, nodeIdent));
		for (EvaluationFormSurvey survey : surveys) {
			evaluationFormManager.deleteSurvey(survey);
		}
	}

	@Override
	public List<RubricStatistic> getRubricStatistics(EvaluationFormSession session) {
		List<RubricStatistic> statistics = new ArrayList<>();
		Form form = evaluationFormManager.loadForm(session.getSurvey().getFormEntry());
		SessionFilter sessionFilter = SessionFilterFactory.create(session);
		for (AbstractElement element : form.getElements()) {
			if (Rubric.TYPE.equals(element.getType())) {
				Rubric rubric = (Rubric) element;
				RubricStatistic statistic = evaluationFormManager.getRubricStatistic(rubric, sessionFilter);
				statistics.add(statistic);
			}
		}
		return statistics;
	}

	@Override
	public MinMax calculateMinMaxSum(RepositoryEntry formEntry, float scalingFactor) {
		double sumMin = 0.0;
		double sumMax = 0.0;
		Form form = evaluationFormManager.loadForm(formEntry);
		for (AbstractElement element : form.getElements()) {
			if (Rubric.TYPE.equals(element.getType())) {
				Rubric rubric = (Rubric) element;
				int numberOfSliders = rubric.getSliders().size();
				int steps = rubric.getSteps();
				double min = rubric.getScaleType().getStepValue(steps, 1);
				double max = rubric.getScaleType().getStepValue(steps, steps);
				sumMin += numberOfSliders * min;
				sumMax += numberOfSliders * max;
			}
		}
		sumMin = scalingFactor * sumMin;
		sumMax = scalingFactor * sumMax;
		return MinMax.of(Float.valueOf((float)sumMin), Float.valueOf((float)sumMax));
	}

	@Override
	public MinMax calculateMinMaxAvg(RepositoryEntry formEntry, float scalingFactor) {
		double sumMin = 0.0;
		double sumMax = 0.0;
		int numberAvgs = 0;
		Form form = evaluationFormManager.loadForm(formEntry);
		for (AbstractElement element : form.getElements()) {
			if (Rubric.TYPE.equals(element.getType())) {
				Rubric rubric = (Rubric) element;
				int numberOfSliders = rubric.getSliders().size();
				int steps = rubric.getSteps();
				double min = rubric.getScaleType().getStepValue(steps, 1);
				double max = rubric.getScaleType().getStepValue(steps, steps);
				sumMin += numberOfSliders * min;
				sumMax += numberOfSliders * max;
				numberAvgs += numberOfSliders;
			}
		}
		if (numberAvgs > 0) {
			double avgMin = sumMin / numberAvgs;
			avgMin = scalingFactor * avgMin;
			double avgMax = sumMax / numberAvgs;
			avgMax = scalingFactor * avgMax;
			return MinMax.of(Float.valueOf((float)avgMin), Float.valueOf((float)avgMax));
		}
		return MinMax.of(0.0f, 0.0f);
	}

	@Override
	public Float calculateScoreBySum(EvaluationFormSession session) {
		double sum = 0.0;
		Form form = evaluationFormManager.loadForm(session.getSurvey().getFormEntry());
		for (AbstractElement element : form.getElements()) {
			if (Rubric.TYPE.equals(element.getType())) {
				Rubric rubric = (Rubric) element;
				RubricStatistic rubricStatistic = evaluationFormManager.getRubricStatistic(rubric, SessionFilterFactory.create(session));
				Double rubricSum = rubricStatistic.getTotalStatistic().getSum();
				if (rubricSum != null) {
					sum += (float)rubricSum.doubleValue();
				}
			}
		}
		return Float.valueOf((float)sum);
	}

	@Override
	public Float calculateScoreByAvg(EvaluationFormSession session) {
		double sumAvgs = 0.0;
		int numberAvgs = 0;
		Form form = evaluationFormManager.loadForm(session.getSurvey().getFormEntry());
		for (AbstractElement element : form.getElements()) {
			if (Rubric.TYPE.equals(element.getType())) {
				Rubric rubric = (Rubric) element;
				RubricStatistic rubricStatistic = evaluationFormManager.getRubricStatistic(rubric, SessionFilterFactory.create(session));
				for (Slider slider : rubric.getSliders()) {
					SliderStatistic sliderStatistic = rubricStatistic.getSliderStatistic(slider);
					Double sliderAvg = sliderStatistic.getAvg();
					if (sliderAvg != null) {
						numberAvgs++;
						sumAvgs += sliderAvg.doubleValue();
					}
				}
			}
		}
		if (numberAvgs > 0) {
			double avg = sumAvgs / numberAvgs;
			return Float.valueOf((float)avg);
		}
		return null;
	}

	@Override
	public Float scaleScore(Float score, float scale) {
		if (score == null) return null;
		
		return scale * score.floatValue();
	}

}
