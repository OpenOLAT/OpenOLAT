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
package org.olat.ims.qti21.ui.editor.overview;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.course.assessment.AssessmentHelper;

/**
 * 
 * Initial date: 14 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MaxScoreCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public MaxScoreCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if(cellValue instanceof Double) {
			ControlObjectRow objectRow = (ControlObjectRow)source.getFormItem().getTableDataModel().getObject(row);
			Double estimatedMaxScore = objectRow.getEstimatedMaxScore();
			Double totalMaxScore = objectRow.getMaxScore();
			String estimated = AssessmentHelper.getRoundedScore(estimatedMaxScore);
			String total = AssessmentHelper.getRoundedScore(totalMaxScore);
			if(estimated != null && total != null) {
				target.append(estimated);
				if(!estimated.equals(total)) {
					String explanation = translator.translate("max.score.configuration.explain", estimated, total);
					target.append(" <i class='o_icon o_icon_warn' title='").append(explanation).append("'> </i>");
				}	
			} else if(estimated != null) {
				target.append(estimated);
			} else if(total != null) {
				target.append(total);
			}
		}
	}
}
