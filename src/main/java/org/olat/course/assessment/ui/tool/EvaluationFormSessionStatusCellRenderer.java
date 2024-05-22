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
package org.olat.course.assessment.ui.tool;

import java.util.Locale;

import org.olat.core.gui.components.table.LabelCellRenderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.forms.EvaluationFormSessionStatus;

/**
 * 
 * Initial date: 8 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormSessionStatusCellRenderer extends LabelCellRenderer {

	private final boolean light;
	private final Translator trans;
	
	public EvaluationFormSessionStatusCellRenderer(Locale locale, boolean light) {
		trans = Util.createPackageTranslator(AssessmentStatusCellRenderer.class, locale);
		this.light = light;
	}

	@Override
	protected boolean isLabelLight() {
		return light;
	}

	@Override
	protected boolean isNullRendered() {
		return true;
	}
	
	public String render(EvaluationFormSessionStatus status) {
		StringOutput stringOutput = new StringOutput();
		render(stringOutput, trans, status);
		return stringOutput.toString();
	}

	protected EvaluationFormSessionStatus getStatus(Object val) {
		if(val instanceof EvaluationFormSessionStatus status) {
			return status;
		}
		return null;
	}

	@Override
	protected String getIconCssClass(Object val) {
		return null;
	}

	@Override
	protected String getCellValue(Object val, Translator translator) {
		EvaluationFormSessionStatus status = getStatus(val);
		if(status == EvaluationFormSessionStatus.inProgress) {
			return trans.translate("assessment.evaluation.status.inProgress");
		} else if(status == EvaluationFormSessionStatus.done) {
			return trans.translate("assessment.evaluation.status.done");	
		}
		return trans.translate("assessment.evaluation.status.open");
	}
	
	@Override
	protected String getElementCssClass(Object val) {
		EvaluationFormSessionStatus status = getStatus(val);
		if(status == EvaluationFormSessionStatus.inProgress) {
			return "o_evaluation_in_progress";
		} else if(status == EvaluationFormSessionStatus.done) {
			return "o_evaluation_done";
		}
		return "o_evaluation_open";
	}
}
