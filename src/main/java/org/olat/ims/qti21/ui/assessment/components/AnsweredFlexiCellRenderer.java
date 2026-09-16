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
package org.olat.ims.qti21.ui.assessment.components;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.ims.qti21.ui.assessment.model.CorrectionAssessmentItemRow;
import org.olat.ims.qti21.ui.assessment.model.CorrectionIdentityRow;

/**
 * 
 * Initial date: 14 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AnsweredFlexiCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		Object obj = source.getFormItem().getTableDataModel().getObject(row);
		if(obj instanceof CorrectionAssessmentItemRow itemRow) {
			render(target, itemRow.getNumOfSessions(), itemRow.getNumAnswered());
		} else if(obj instanceof CorrectionIdentityRow itemRow) {
			render(target, itemRow.getNumOfSessions(), itemRow.getNumAnswered());
		}
	}
	
	private void render(StringOutput target, int numOfSessions, int numOfAnswered) {
		target.append(numOfAnswered).append("/").append(numOfSessions);
	}
}
