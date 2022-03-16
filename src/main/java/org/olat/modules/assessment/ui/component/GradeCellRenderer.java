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
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.modules.assessment.ui.AssessedIdentityElementRow;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.grade.ui.GradeUIFactory;

/**
 * 
 * Initial date: 4 Mar 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeCellRenderer implements FlexiCellRenderer {

	private final Translator gradeTanslator;

	public GradeCellRenderer(Locale locale) {
		gradeTanslator = Util.createPackageTranslator(GradeUIFactory.class, locale);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue instanceof AssessedIdentityElementRow) {
			AssessedIdentityElementRow assessmentRow = (AssessedIdentityElementRow)cellValue;
			appendGrade(target, assessmentRow.getPerformanceClassIdent(), assessmentRow.getGrade(), assessmentRow.getPassed());
		} else if (cellValue instanceof AssessmentNodeData) {
			AssessmentNodeData assessmentRow = (AssessmentNodeData)cellValue;
			appendGrade(target, assessmentRow.getPerformanceClassIdent(), assessmentRow.getGrade(), assessmentRow.getPassed());
		} else if (cellValue instanceof EfficiencyStatementEntry) {
			EfficiencyStatementEntry assessmentRow = (EfficiencyStatementEntry)cellValue;
			appendGrade(target, assessmentRow.getPerformanceClassIdent(), assessmentRow.getGrade(), assessmentRow.getPassed());
		}
	}

	private void appendGrade(StringOutput target, String performanceClassident, String grade, Boolean passed) {
		String gradeStr = GradeUIFactory.translatePerformanceClass(gradeTanslator, performanceClassident, grade);
		if (StringHelper.containsNonWhitespace(grade)) {
			target.append("<span class='o_gr_cell ");
			if (passed != null) {
				if (passed.booleanValue()) {
					target.append("o_gr_passed");
				} else {
					target.append("o_gr_failed");
				}
			}
			target.append("'>");
			target.append(gradeStr);
			target.append("</span>");
		}
	}

}
