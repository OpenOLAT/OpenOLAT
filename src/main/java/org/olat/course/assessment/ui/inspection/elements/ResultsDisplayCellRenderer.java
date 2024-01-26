/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.inspection.elements;

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.course.assessment.ui.inspection.AssessmentInspectionConfigurationRow;
import org.olat.ims.qti21.QTI21AssessmentResultsOptions;

/**
 * 
 * Initial date: 19 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ResultsDisplayCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public ResultsDisplayCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		if(cellValue instanceof AssessmentInspectionConfigurationRow configuration) {
			List<String> options = configuration.getConfiguration().getOverviewOptionsAsList();
			if(options.size() >= 5) {
				target.append(translator.translate("qti.form.summary.all"));
			} else {
				target.append(translator.translate("qti.form.summary.size", Integer.toString(options.size()), "5"));
				if(configuration.getInfosButton() != null) {
					target.append(" ");
					Component cmp = configuration.getInfosButton().getComponent();
					if(cmp.isVisible()) {
						cmp.getHTMLRendererSingleton().render(renderer, target, cmp, ubu, translator, new RenderResult(), null);
					}
					cmp.setDirty(false);
				}
			}
		}
	}
	
	public String translatedOption(String option) {
		return switch(option) {
			case QTI21AssessmentResultsOptions.METADATA -> translator.translate("qti.form.summary.metadata");
			case QTI21AssessmentResultsOptions.SECTION_SUMMARY -> translator.translate("qti.form.summary.sections");
			case QTI21AssessmentResultsOptions.QUESTION_SUMMARY -> translator.translate("qti.form.summary.questions.metadata");
			case QTI21AssessmentResultsOptions.USER_SOLUTIONS -> translator.translate("qti.form.summary.responses");
			case QTI21AssessmentResultsOptions.CORRECT_SOLUTIONS -> translator.translate("qti.form.summary.solutions");
			default -> "";
		};
	}

}
