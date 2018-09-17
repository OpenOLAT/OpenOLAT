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
package org.olat.modules.forms.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.RubricRating;
import org.olat.modules.forms.model.xml.Rubric;

/**
 * 
 * Initial date: 19.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RubricAvgRenderer implements FlexiCellRenderer {

	private final Rubric rubric;

	public RubricAvgRenderer(Rubric rubric) {
		this.rubric = rubric;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue instanceof Double) {
			Double value = (Double) cellValue;
			render(target, value);
		}
	}

	public String render(Double avg) {
		StringOutput target = new StringOutput();
		render(target, avg);
		return target.toString();
	}

	private void render(StringOutput target, Double value) {
		target.append("<div class='o_rubric_avg ");
		target.append(getColorCss(rubric, value));
		target.append("'>");
		target.append(EvaluationFormFormatter.formatDouble(value));
		target.append("</div>");
	}

	public static String getColorCss(Rubric rubric, Double value) {
		EvaluationFormManager evaluationFormManager = CoreSpringFactory.getImpl(EvaluationFormManager.class);
		RubricRating rating = evaluationFormManager.getRubricRating(rubric, value);
		switch (rating) {
			case SUFFICIENT: return "o_rubric_sufficient";
			case NEUTRAL: return "o_rubric_neutral";
			case INSUFFICIENT: return "o_rubric_insufficient";
			default: return null;
		}
	}
	
}
