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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.nodes.ms.AuditEnv;
import org.olat.course.nodes.ms.MSService;
import org.olat.course.nodes.ms.MinMax;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationIdentifier;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.forms.Limit;
import org.olat.modules.forms.RubricStatistic;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.modules.forms.SliderStatistic;
import org.olat.modules.forms.SlidersStatistic;
import org.olat.modules.forms.SlidersStepCounts;
import org.olat.modules.forms.StepCounts;
import org.olat.modules.forms.model.SliderStatisticImpl;
import org.olat.modules.forms.model.SlidersStatisticImpl;
import org.olat.modules.forms.model.SlidersStepCountsImpl;
import org.olat.modules.forms.model.StepCountsBuilder;
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
			Identity assessedIdentity, AuditEnv auditEnv) {
		EvaluationFormSurveyIdentifier surveyIdent = getSurveyIdentitfier(ores, nodeIdent, assessedIdentity);
		EvaluationFormSurvey survey = loadOrCreateSurvey(formEntry, surveyIdent);
		EvaluationFormParticipation participation = loadOrCreateParticipation(survey);
		return loadOrCreateSesssion(participation, auditEnv);
	}

	private EvaluationFormSurveyIdentifier getSurveyIdentitfier(RepositoryEntry ores, String nodeIdent,
			Identity assessedIdentity) {
		OLATResourceable msOres = getMsOlatResourceable(ores);
		return of(msOres, nodeIdent, assessedIdentity.getKey().toString());
	}

	private EvaluationFormSurveyIdentifier getSurveysIdentifier(RepositoryEntry ores, String nodeIdent) {
		return of(getMsOlatResourceable(ores), nodeIdent);
	}

	private OLATResourceable getMsOlatResourceable(RepositoryEntry ores) {
		return OresHelper.createOLATResourceableInstance(SURVEY_ORES_TYPE_NAME, ores.getKey());
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

	private EvaluationFormSession loadOrCreateSesssion(EvaluationFormParticipation participation, AuditEnv auditEnv) {
		EvaluationFormSession session = evaluationFormManager.loadSessionByParticipation(participation);
		if (session == null) {
			session = evaluationFormManager.createSession(participation);
			logAudit(auditEnv, "Evaluation started");
		}
		return session;
	}

	@Override
	public EvaluationFormSession getSession(RepositoryEntry ores, String nodeIdent, Identity assessedIdentity,
			EvaluationFormSessionStatus status) {
		EvaluationFormSurveyIdentifier surveyIdent = getSurveyIdentitfier(ores, nodeIdent, assessedIdentity);
		SessionFilter filter = SessionFilterFactory.create(surveyIdent, status);
		List<EvaluationFormSession> sessions = evaluationFormManager.loadSessionsFiltered(filter, 0, -1);
		return !sessions.isEmpty()? sessions.get(0): null;
	}

	@Override
	public EvaluationFormSession getSession(EvaluationFormSessionRef sessionRef) {
		return evaluationFormManager.loadSessionByKey(sessionRef);
	}

	@Override
	public EvaluationFormSession closeSession(EvaluationFormSession session, AuditEnv auditEnv) {
		EvaluationFormSession finishSession = evaluationFormManager.finishSession(session);
		logAudit(auditEnv, "Evaluation finshed");
		return finishSession;
	}

	@Override
	public EvaluationFormSession reopenSession(EvaluationFormSession session, AuditEnv auditEnv) {
		EvaluationFormSession reopenSession = evaluationFormManager.reopenSession(session);
		logAudit(auditEnv, "Evaluation reopened");
		return reopenSession;
	}

	@Override
	public boolean hasSessions(RepositoryEntry ores, String nodeIdent) {
		return !evaluationFormManager.loadSurveys(getSurveysIdentifier(ores, nodeIdent)).isEmpty();
	}

	@Override
	public List<EvaluationFormSession> getSessions(RepositoryEntry ores, String nodeIdent) {
		SessionFilter filter = SessionFilterFactory.create(getSurveysIdentifier(ores, nodeIdent));
		return evaluationFormManager.loadSessionsFiltered(filter, 0, -1);
	}

	@Override
	public void deleteSession(RepositoryEntry ores, String nodeIdent, Identity assessedIdentity, AuditEnv auditEnv) {
		EvaluationFormSurveyIdentifier surveyIdent = getSurveyIdentitfier(ores, nodeIdent, assessedIdentity);	
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(surveyIdent);
		evaluationFormManager.deleteSurvey(survey);
		logAudit(auditEnv, "Evaluation deleted");
	}

	@Override
	public void deleteSessions(RepositoryEntry ores, String nodeIdent) {
		List<EvaluationFormSurvey> surveys = evaluationFormManager.loadSurveys(getSurveysIdentifier(ores, nodeIdent));
		for (EvaluationFormSurvey survey : surveys) {
			evaluationFormManager.deleteSurvey(survey);
		}
	}

	@Override
	public List<RubricStatistic> getRubricStatistics(EvaluationFormSession session) {
		Form form = evaluationFormManager.loadForm(session.getSurvey().getFormEntry());
		List<RubricStatistic> statistics = new ArrayList<>();
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
	public Map<String, Map<Rubric, RubricStatistic>> getRubricStatistics(RepositoryEntry ores, String nodeIdent, Form form) {
		List<EvaluationFormSession> sessions = getSessions(ores, nodeIdent);
		Map<String, EvaluationFormSession> identToSesssion = sessions.stream()
				.collect(Collectors.toMap(
						s -> s.getSurvey().getIdentifier().getSubident2(),
						Function.identity()));
		
		if (!sessions.isEmpty()) {
			List<Rubric> rubrics = form.getElements().stream()
					.filter(e -> Rubric.TYPE.equals(e.getType()))
					.map(e -> (Rubric)e)
					.collect(Collectors.toList());
			List<String> responseIdentifiers = rubrics.stream()
					.flatMap(r -> r.getSliders().stream())
					.map(Slider::getId)
					.collect(Collectors.toList());
			SessionFilter filter = SessionFilterFactory.create(getSurveysIdentifier(ores, nodeIdent));
			List<EvaluationFormResponse> responses = evaluationFormManager.getResponses(responseIdentifiers, filter , Limit.all());
			Map<EvaluationFormSession, List<EvaluationFormResponse>> sessionToResponses = responses.stream()
					.collect(Collectors.groupingBy(EvaluationFormResponse::getSession));
			
			Map<String, Map<Rubric, RubricStatistic>> identToStatistics = new HashMap<>();
			for (Entry<String, EvaluationFormSession> entry : identToSesssion.entrySet()) {
				EvaluationFormSession session = entry.getValue();
				List<EvaluationFormResponse> list = sessionToResponses.get(session);
				if (list != null && !list.isEmpty()) {
					Map<Rubric, RubricStatistic> rubricToStatistic = new HashMap<>(); 
					for (Rubric rubric: rubrics) {
						SlidersStepCounts slidersStepCounts = getSlidersStepCounts(rubric, sessionToResponses.get(session));
						SlidersStatistic slidersStatistic = evaluationFormManager.calculateSlidersStatistic(rubric, slidersStepCounts);
						RubricStatistic rubricStatistic = evaluationFormManager.getRubricStatistic(rubric, slidersStatistic);
						rubricToStatistic.put(rubric, rubricStatistic);
					}
					identToStatistics.put(entry.getKey(), rubricToStatistic);
				}
			}
			return identToStatistics;
		}
		
		return Collections.emptyMap();
	}

	private SlidersStepCounts getSlidersStepCounts(Rubric rubric, List<EvaluationFormResponse> responses) {
		SlidersStepCountsImpl slidersStepCount = new SlidersStepCountsImpl();
		for (Slider slider: rubric.getSliders()) {
			EvaluationFormResponse response = getSliderResponse(slider, responses);
			if (response != null) {
				StepCounts stepCount = getStepCounts(rubric, response);
				slidersStepCount.put(slider, stepCount);
			}
		}
		return slidersStepCount;
	}

	private EvaluationFormResponse getSliderResponse(Slider slider, List<EvaluationFormResponse> responses) {
		for (EvaluationFormResponse response: responses) {
			if (slider.getId().equals(response.getResponseIdentifier())) {
				return response;
			}
		}
		return null;
	}

	private StepCounts getStepCounts(Rubric rubric, EvaluationFormResponse response) {
		return StepCountsBuilder.builder(rubric.getSteps())
				.withCount(response.getNumericalResponse().intValue(), Long.valueOf(1))
				.build();
	}

	@Override
	public MinMax calculateMinMaxSum(RepositoryEntry formEntry, float scalingFactor) {
		Form form = evaluationFormManager.loadForm(formEntry);
		MinMax minMax = calculateMinMaxSum(form);
		return MinMax.of(scalingFactor * minMax.getMin(), scalingFactor * minMax.getMax());
	}
	
	MinMax calculateMinMaxSum(Form form) {
		float sumMin = 0.0f;
		float sumMax = 0.0f;
		for (AbstractElement element : form.getElements()) {
			if (Rubric.TYPE.equals(element.getType())) {
				Rubric rubric = (Rubric) element;
				float rubricSumMin = evaluationFormManager.getRubricStatistic(rubric, getSlidersStatistic(rubric, true))
						.getTotalStatistic()
						.getSum().floatValue();
				float rubricSumMax = evaluationFormManager.getRubricStatistic(rubric, getSlidersStatistic(rubric, false))
						.getTotalStatistic()
						.getSum().floatValue();
				if (rubricSumMin > rubricSumMax) {
					float temp = rubricSumMin;
					rubricSumMin = rubricSumMax;
					rubricSumMax = temp;
				}
				// If no responses the min is 0
				rubricSumMin = rubricSumMin > 0? 0: rubricSumMin;
				
				sumMin += rubricSumMin;
				sumMax += rubricSumMax;
			}
		}
		return MinMax.of(sumMin, sumMax);
	}

	@Override
	public MinMax calculateMinMaxAvg(RepositoryEntry formEntry, float scalingFactor) {
		Form form = evaluationFormManager.loadForm(formEntry);
		MinMax minMax = calculateMinMaxAvg(form);
		return MinMax.of(scalingFactor * minMax.getMin(), scalingFactor * minMax.getMax());
	}
	
	MinMax calculateMinMaxAvg(Form form) {
		float sumMin = 0.0f;
		float sumMax = 0.0f;
		int numberAvgs = 0;
		for (AbstractElement element : form.getElements()) {
			if (Rubric.TYPE.equals(element.getType())) {
				Rubric rubric = (Rubric) element;
				float rubricAvgMin = evaluationFormManager.getRubricStatistic(rubric, getSlidersStatistic(rubric, true))
						.getTotalStatistic()
						.getAvg().floatValue();
				float rubricAvgMax = evaluationFormManager.getRubricStatistic(rubric, getSlidersStatistic(rubric, false))
						.getTotalStatistic()
						.getAvg().floatValue();
				// switch if descending scale
				if (rubricAvgMin > rubricAvgMax) {
					float temp = rubricAvgMin;
					rubricAvgMin = rubricAvgMax;
					rubricAvgMax = temp;
				}
				// If no responses the min is 0
				rubricAvgMin = rubricAvgMin > 0? 0: rubricAvgMin;
				int numberOfSliders = rubric.getSliders().size();
				sumMin += numberOfSliders * rubricAvgMin;
				sumMax += numberOfSliders * rubricAvgMax;
				numberAvgs += numberOfSliders;
			}
		}
		if (numberAvgs > 0) {
			float avgMin = sumMin / numberAvgs;
			float avgMax = sumMax / numberAvgs;
			return MinMax.of(avgMin, avgMax);
		}
		return MinMax.of(0.0f, 0.0f);
	}
	
	private SlidersStatistic getSlidersStatistic(Rubric rubric, boolean min) {
		SlidersStatisticImpl slidersStatisticImpl = new SlidersStatisticImpl();
		int step = min? 1: rubric.getSteps();
		for (Slider slider : rubric.getSliders()) {
			StepCounts stepCounts = StepCountsBuilder.builder(rubric.getSteps())
					.withCount(step, Long.valueOf(1))
					.build();
			SliderStatistic sliderStatistic = new SliderStatisticImpl(null, null, null, null, null, null, stepCounts, null);
			slidersStatisticImpl.put(slider, sliderStatistic);
		}
		return slidersStatisticImpl;
	}

	@Override
	public Float calculateScoreBySum(EvaluationFormSession session) {
		if (session == null) return null;
		
		Form form = evaluationFormManager.loadForm(session.getSurvey().getFormEntry());
		Function<Rubric, RubricStatistic> rubricFunction = rubric -> {
			return evaluationFormManager.getRubricStatistic(rubric, SessionFilterFactory.create(session));
		};
		return calculateScoreBySum(form, rubricFunction);
	}

	@Override
	public Float calculateScoreBySum(Form form, Function<Rubric, RubricStatistic> rubricFunction) {
		double sum = 0.0;
		for (AbstractElement element : form.getElements()) {
			if (Rubric.TYPE.equals(element.getType())) {
				Rubric rubric = (Rubric) element;
				RubricStatistic rubricStatistic = rubricFunction.apply(rubric);
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
		if (session == null) return null;

		Form form = evaluationFormManager.loadForm(session.getSurvey().getFormEntry());
		Function<Rubric, RubricStatistic> rubricFunction = rubric -> {
			return evaluationFormManager.getRubricStatistic(rubric, SessionFilterFactory.create(session));
		};
		return calculateScoreByAvg(form, rubricFunction);
	}
	
	@Override
	public Float calculateScoreByAvg(Form form, Function<Rubric, RubricStatistic> rubricFunction) {
		double sumAvgs = 0.0;
		int numberAvgs = 0;
		for (AbstractElement element : form.getElements()) {
			if (Rubric.TYPE.equals(element.getType())) {
				Rubric rubric = (Rubric) element;
				RubricStatistic rubricStatistic = rubricFunction.apply(rubric);
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

	private void logAudit(AuditEnv auditEnv, String message) {
		UserNodeAuditManager am = auditEnv.getAuditManager();
		am.appendToUserNodeLog(
				auditEnv.getCourseNode(),
				auditEnv.getIdentity(),
				auditEnv.getAssessedIdentity(),
				message,
				auditEnv.getBy());
	}

}
