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

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.modules.assessment.ui.AssessedIdentityElementRow;
import org.olat.modules.coach.model.EfficiencyStatementEntry;

/**
 * 
 * Initial date: 28 Mar 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ColorizedScoreCellRenderer extends PassedDependentRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue instanceof AssessedIdentityElementRow) {
			AssessedIdentityElementRow assessmentRow = (AssessedIdentityElementRow)cellValue;
			append(target, assessmentRow.getPassed(), AssessmentHelper.getRoundedScore(assessmentRow.getScore()));
		} else if (cellValue instanceof AssessmentNodeData) {
			AssessmentNodeData assessmentRow = (AssessmentNodeData)cellValue;
			append(target, assessmentRow.getPassed(), AssessmentHelper.getRoundedScore(assessmentRow.getScore()));
		} else if (cellValue instanceof EfficiencyStatementEntry) {
			EfficiencyStatementEntry assessmentRow = (EfficiencyStatementEntry)cellValue;
			append(target, assessmentRow.getPassed(), AssessmentHelper.getRoundedScore(assessmentRow.getScore()));
		}
	}

}
