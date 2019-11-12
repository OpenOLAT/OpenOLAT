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
package org.olat.course.learningpath.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.run.scoring.StatusCompletionEvaluator;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 12 Nov 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathProgressRenderer implements FlexiCellRenderer {
	
	private static final StatusCompletionEvaluator STATUS_COMPLETION_EVALUATOR = new StatusCompletionEvaluator();

	private CourseAssessmentService courseAssessmentService;
	
	public LearningPathProgressRenderer() {
		courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue instanceof LearningPathTreeNode) {
			LearningPathTreeNode treeNode = (LearningPathTreeNode)cellValue;
			if (Boolean.TRUE.equals(treeNode.getFullyAssessed())) {
				target.append("<i class=\"o_icon o_icon-fw o_lp_done\"></i>");
			} else if (AssessmentEntryStatus.notReady.equals(treeNode.getAssessmentStatus())) {
				// render nothing
			} else {
				renderProgressBar(renderer, target, ubu, translator, treeNode);
			}
		}
	}

	private void renderProgressBar(Renderer renderer, StringOutput target, URLBuilder ubu, Translator translator,
			LearningPathTreeNode treeNode) {
		ProgressBar progressBar = new ProgressBar("progress-" + treeNode.getIdent());
		progressBar.setMax(1.0f);
		progressBar.setWidthInPercent(true);
		progressBar.setPercentagesEnabled(true);
		progressBar.setLabelAlignment(LabelAlignment.none);
		float actual = getActual(treeNode);
		progressBar.setActual(actual);
		progressBar.getHTMLRendererSingleton().render(renderer, target, progressBar, ubu, translator, null, null);
	}

	private float getActual(LearningPathTreeNode treeNode) {
		float actual = 0.5f;
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(treeNode.getCourseNode());
		boolean hasCompletion = !Mode.none.equals(assessmentConfig.getCompletionMode());
		if (hasCompletion) {
			actual = treeNode.getCompletion() != null ? treeNode.getCompletion().floatValue() : 0.0f;
		} else {
			Double statusCompletion = STATUS_COMPLETION_EVALUATOR.getCompletion(treeNode.getFullyAssessed(),
					treeNode.getAssessmentStatus());
			actual = statusCompletion != null ? statusCompletion.floatValue() : 0;
		}
		return actual;
	}

}
