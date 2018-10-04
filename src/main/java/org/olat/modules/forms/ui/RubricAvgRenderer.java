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

import org.apache.commons.lang.StringEscapeUtils;
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
			render(target, value, translator);
		}
	}

	public String render(Double avg, Translator translator) {
		StringOutput target = new StringOutput();
		render(target, avg, translator);
		return target.toString();
	}

	private void render(StringOutput target, Double value, Translator translator) {
		EvaluationFormManager evaluationFormManager = CoreSpringFactory.getImpl(EvaluationFormManager.class);
		RubricRating rating = evaluationFormManager.getRubricRating(rubric, value);
		
		target.append("<div class='o_rubric_avg o_nowrap ");
		String ratingCss = getRatingCssClass(rating);
		target.append(ratingCss, ratingCss != null);
		target.append("'>");
		if (ratingCss != null) {
			target.append("<i class='o_icon o_icon-fw ").append(getRatingIconCssClass(rating)).append("' title=\"");
			target.append(StringEscapeUtils.escapeHtml(getRatingIconExplanation(rating, rubric, translator))).append("\"> </i> ");
		}
		target.append(EvaluationFormFormatter.formatDouble(value));
		target.append("</div>");
	}

	public static String getRatingCssClass(RubricRating rating) {
		switch (rating) {
			case SUFFICIENT: return "o_rubric_sufficient";
			case NEUTRAL: return "o_rubric_neutral";
			case INSUFFICIENT: return "o_rubric_insufficient";
			default: return null;
		}
	}

	public static String getRatingIconCssClass(RubricRating rating) {
		switch (rating) {
			case SUFFICIENT: return "o_icon_rubric_sufficient";
			case NEUTRAL: return "o_icon_rubric_neutral";
			case INSUFFICIENT: return "o_icon_rubric_insufficient";
			default: return null;
		}
	}

	public static String getRatingIconExplanation(RubricRating rating, Rubric rubric, Translator translator) {
		
		switch (rating) {
			case SUFFICIENT: return translator.translate("rubric.sufficient.explanation", new String[]{rubric.getLowerBoundSufficient()+ "", rubric.getUpperBoundSufficient()+""});
			case NEUTRAL: return translator.translate("rubric.neutral.explanation", new String[]{rubric.getLowerBoundNeutral()+ "", rubric.getUpperBoundNeutral()+""});
			case INSUFFICIENT: return translator.translate("rubric.insufficient.explanation", new String[]{rubric.getLowerBoundInsufficient()+ "", rubric.getUpperBoundInsufficient()+""});
			default: return null;
		}
	}
	

	
}
