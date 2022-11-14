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
import org.olat.ims.qti21.ui.assessment.model.CorrectionIdentityAssessmentItemRow;
import org.olat.ims.qti21.ui.assessment.model.CorrectionIdentityRow;
import org.olat.ims.qti21.ui.assessment.model.CorrectionRow;

/**
 * 
 * Initial date: 26 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CorrectedFlexiCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		Object obj = source.getFormItem().getTableDataModel().getObject(row);
		if(obj instanceof CorrectionIdentityRow) {
			render(target, (CorrectionIdentityRow)obj);
		} else if(obj instanceof CorrectionRow) {
			render(target, (CorrectionRow)obj);
		} else if(obj instanceof CorrectionIdentityAssessmentItemRow) {
			render(target, (CorrectionIdentityAssessmentItemRow)obj);
		} 
	}
	
	public void render(StringOutput target, CorrectionRow itemRow) {
		target.append(itemRow.getNumCorrected());
		if(!itemRow.isManualCorrection()) {
			if(itemRow.getNumCorrected() + itemRow.getNumAutoCorrected() >= itemRow.getNumOfSessions()) {
				target.append(" <i class='o_icon o_icon_fw o_icon_ok'> </i>");
			} 
		}
	}
	
	private void render(StringOutput target, CorrectionIdentityRow identityRow) {
		target.append(identityRow.getNumCorrected());
		if(identityRow.getNumNotCorrected() == 0) {
			target.append(" <i class='o_icon o_icon_fw o_icon_ok'> </i>");
		}
	}
	
	private void render(StringOutput target, CorrectionIdentityAssessmentItemRow itemRow) {
		if(itemRow.getManualScore() != null) {
			target.append(" <i class='o_icon o_icon_fw o_icon_ok'> </i>");
		}
	}
}
