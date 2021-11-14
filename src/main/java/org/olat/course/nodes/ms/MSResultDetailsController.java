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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.RubricStatistic;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.NameDisplay;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 Jun 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MSResultDetailsController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private EvaluationFormExecutionController formCtrl;

	private EvaluationFormSession session;
	
	@Autowired
	private MSService msService;

	public MSResultDetailsController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment assessedUserCourseEnv, CourseNode courseNode) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("result_details");
		
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		RepositoryEntry formEntry = MSCourseNode.getEvaluationForm(config);
		RepositoryEntry ores = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		String nodeIdent = courseNode.getIdent();
		Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		UserNodeAuditManager auditManager = assessedUserCourseEnv.getCourseEnvironment().getAuditManager();
		AuditEnv auditEnv = AuditEnv.of(auditManager , courseNode, assessedIdentity, getIdentity(), Role.coach);
		session =  msService.getOrCreateSession(formEntry, ores, nodeIdent, assessedIdentity, auditEnv);
		
		String scoreConfig = config.getStringValue(MSCourseNode.CONFIG_KEY_SCORE);
		
		boolean pointsFromEvaluationForm = MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM.equals(scoreConfig)
				|| MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG.equals(scoreConfig);
		if (pointsFromEvaluationForm) {
			String scaleConfig = config.getStringValue(MSCourseNode.CONFIG_KEY_EVAL_FORM_SCALE,
					MSCourseNode.CONFIG_DEFAULT_EVAL_FORM_SCALE);
			float scale = Float.parseFloat(scaleConfig);

			List<RubricStatistic> statistics = msService.getRubricStatistics(session);

			List<RubricWrapper> rubricWrappers = new ArrayList<>(statistics.size());
			for (int i = 0; i < statistics.size(); i++) {
				RubricStatistic statistic = statistics.get(i);
				String name = getName(statistic.getRubric(), i+1);
				String value = getValue(statistic, scoreConfig, scale);
				RubricWrapper rubricWrapper = new RubricWrapper(name, value);
				rubricWrappers.add(rubricWrapper);
			}
			mainVC.contextPut("rubrics", rubricWrappers);
		}
		
		formCtrl = new EvaluationFormExecutionController(ureq, getWindowControl(), session, true, false, false, null);
		listenTo(formCtrl);
		mainVC.put("evaluationForm", formCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	private String getName(Rubric rubric, int index) {
		boolean showName = rubric.getNameDisplays().contains(NameDisplay.report)
				&& StringHelper.containsNonWhitespace(rubric.getName());
		String[] args = showName
				? new String[] { "\"" + rubric.getName() + "\"" }
				: new String[] { String.valueOf(index) };
		return translate("result.details.score", args);
	}

	private String getValue(RubricStatistic statistic, String scoreConfig, float scalingFactor) {
		Double score = null;
		switch (scoreConfig) {
		case MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM:
			score = statistic.getTotalStatistic().getSum();
			break;
		case MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG:
			score = statistic.getTotalStatistic().getAvg();
			break;
		default:
			//
		}
		
		if (score != null) {
			return AssessmentHelper.getRoundedScore(msService.scaleScore(score.floatValue(), scalingFactor));
		}
		return AssessmentHelper.getRoundedScore(0f);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public static class RubricWrapper {
		
		private final String name;
		private final String value;
		
		RubricWrapper(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}
	}

}
