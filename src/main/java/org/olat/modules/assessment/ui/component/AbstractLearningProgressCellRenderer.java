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
package org.olat.modules.assessment.ui.component;

import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.course.run.scoring.AssessmentEvaluation;

/**
 * 
 * Initial date: 13 Nov 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractLearningProgressCellRenderer implements FlexiCellRenderer {
	
	private final Locale locale;
	private final boolean chartVisible;
	private final boolean labelVisible;
	
	public AbstractLearningProgressCellRenderer(Locale locale) {
		this(locale, true, true);
	}
	
	public AbstractLearningProgressCellRenderer(Locale locale, boolean chartVisible, boolean labelVisible) {
		this.locale = locale;
		this.chartVisible = chartVisible;
		this.labelVisible = labelVisible;
	}
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		AssessmentEvaluation assessmentEvaluation = getAssessmentEvaluation(cellValue);
		if (assessmentEvaluation != null) {
			LearningProgressComponent learningProgressComponent = new LearningProgressComponent("progress-" + CodeHelper.getRAMUniqueID(), locale, null);
			learningProgressComponent.setChartVisible(chartVisible);
			learningProgressComponent.setLabelVisible(labelVisible);
			learningProgressComponent.setFullyAssessed(assessmentEvaluation.getFullyAssessed());
			learningProgressComponent.setStatus(assessmentEvaluation.getAssessmentStatus());
			learningProgressComponent.setCompletion(getActual(cellValue));
			learningProgressComponent.getHTMLRendererSingleton().render(renderer, target, learningProgressComponent, ubu, translator, null, null);
		}
	}

	protected abstract AssessmentEvaluation getAssessmentEvaluation(Object cellValue);
	
	protected abstract float getActual(Object cellValue);

}