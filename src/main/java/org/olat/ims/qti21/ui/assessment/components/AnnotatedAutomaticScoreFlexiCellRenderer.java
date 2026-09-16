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
package org.olat.ims.qti21.ui.assessment.components;

import java.math.BigDecimal;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.ims.qti21.ui.assessment.model.CorrectionIdentityAssessmentItemRow;

/**
 * 
 * Initial date: 14 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AnnotatedAutomaticScoreFlexiCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		Object obj = source.getFormItem().getTableDataModel().getObject(row);
		if(obj instanceof CorrectionIdentityAssessmentItemRow itemRow) {
			BigDecimal score = itemRow.getScore();
			if(score == null) {
				score = BigDecimal.ZERO;
			}
			
			target.append(AssessmentHelper.getRoundedScore(score));
			if(!itemRow.isManualCorrection() && itemRow.getManualScore() != null) {
				BigDecimal diff = itemRow.getManualScore().subtract(score);
				int comparison = diff.compareTo(BigDecimal.ZERO);
				if(comparison != 0) {
					target.append(" (");
					if(comparison > 0) {
						target.append("+");
					}
					target.append(AssessmentHelper.getRoundedScore(diff)).append(")");	
				}
			}
		}
	}
}
