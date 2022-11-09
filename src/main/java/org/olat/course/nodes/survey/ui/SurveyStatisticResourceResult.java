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
package org.olat.course.nodes.survey.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.SimpleMessageController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.nodes.SurveyCourseNode;
import org.olat.course.nodes.survey.SurveyRunSecurityCallback;
import org.olat.course.statistic.StatisticResourceResult;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SurveyStatisticResourceResult implements StatisticResourceResult {
	
	private final RepositoryEntry courseEntry;
	private final SurveyCourseNode courseNode;
	private final Identity identity;
	private final SurveyRunSecurityCallback secCallback;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public SurveyStatisticResourceResult(RepositoryEntry courseEntry, SurveyCourseNode courseNode, Identity identity,
			SurveyRunSecurityCallback secCallback) {
		this.courseEntry = courseEntry;
		this.courseNode = courseNode;
		this.identity = identity;
		this.secCallback = secCallback;
		CoreSpringFactory.autowireObject(this);
	}

	@Override
	public TreeModel getSubTreeModel() {
		return null;
	}

	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			TreeNode selectedNode) {
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(EvaluationFormSurveyIdentifier.of(courseEntry, courseNode.getIdent()));
		EvaluationFormParticipation participation = evaluationFormManager.loadParticipationByExecutor(survey, identity);
		if (secCallback.canViewReporting(participation)) {
			return new SurveyReportingController(ureq, wControl, courseEntry, courseNode, survey);
		}
		Translator translator = Util.createPackageTranslator(SurveyReportingController.class, ureq.getLocale());
		String noAccess = translator.translate("report.noaccess");
		return new SimpleMessageController(ureq, wControl, noAccess, "o_hint");
	}

}
