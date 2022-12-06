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
package org.olat.modules.grade.ui.component;

import static org.olat.modules.grade.ui.GradeUIFactory.THREE_DIGITS;

import java.util.Iterator;
import java.util.NavigableSet;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.ui.GradeUIFactory;

/**
 * 
 * Initial date: 24 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeScoreRangeTableRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		GradeScoreRangeTable gadeScoreRangeTable = (GradeScoreRangeTable)source;
		NavigableSet<GradeScoreRange> gradeScoreRanges = gadeScoreRangeTable.getGradeScoreRanges();
		Iterator<GradeScoreRange> rangesIterator = gradeScoreRanges.iterator();
		int numColumns = gadeScoreRangeTable.getNumColumns();
		
		sb.append("<div class='o_gr_grade_scores'>");
		int numRowsRendered = 0;
		for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {
			int numRows = (int)Math.ceil((float)(gradeScoreRanges.size() - numRowsRendered) / (numColumns - columnIndex));
			int fromIndex = columnIndex * numRows;
			int toIndex = (columnIndex + 1) * numRows;

			sb.append("<table class='table table-striped o_gr_grade_score_table'>");
			sb.append("<tbody>");
			sb.append("<tr>");
			sb.append("<th>").append(translator.translate("grade.score.score")).append("</th>");
			sb.append("<th>").append(GradeUIFactory.translateGradeSystemLabel(translator, gradeScoreRanges.first().getGradeSystemIdent())).append("</th>");
			sb.append("</tr>");
			
			int rangeIndex = fromIndex;
			while (rangeIndex < toIndex && rangesIterator.hasNext()) {
				GradeScoreRange row = rangesIterator.next();
				sb.append("<tr").append(" class='o_gr_passed'", row.getPassed() != null && row.getPassed().booleanValue()).append(">");
				sb.append("<td>").append(THREE_DIGITS.format(row.getLowerBound())).append("-").append(THREE_DIGITS.format(row.getUpperBound())).append("</td>");
				String grade = GradeUIFactory.translatePerformanceClass(translator, row.getPerformanceClassIdent(), row.getGrade(), row.getGradeSystemIdent());
				sb.append("<td>").append(grade).append("</td>");
				sb.append("</tr>");
				rangeIndex++;
			}
			sb.append("</tbody>");
			sb.append("</table>");
			
			numRowsRendered += numRows;
		}
		sb.append("</div>");
	}
	
}
