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
package org.olat.modules.portfolio.ui.renderer;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.modules.assessment.AssessmentEntryLight;

/**
 * 
 * Initial date: 23.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentEntryCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public AssessmentEntryCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator trans) {

		String val;
		if(cellValue instanceof AssessmentEntryLight) {
			AssessmentEntryLight entry = (AssessmentEntryLight)cellValue;
			if(entry.getScore() != null) {
				if(entry.getPassed() != null) {
					if(entry.getPassed().booleanValue()) {
						val = translator.translate("table.grading.passed.points",
								new String[] { AssessmentHelper.getRoundedScore(entry.getScore()) });
					} else {
						val = translator.translate("table.grading.failed.points",
								new String[] { AssessmentHelper.getRoundedScore(entry.getScore()) });
					}
				} else {
					val = translator.translate("table.grading.points",
							new String[] { AssessmentHelper.getRoundedScore(entry.getScore()) });
				}
			} else if(entry.getPassed() != null) {
				if(entry.getPassed().booleanValue()) {
					val = translator.translate("passed.true");
				} else {
					val = translator.translate("passed.false");
				}
			} else {
				val = translator.translate("table.grading.no");
			}
		} else {
			val = translator.translate("table.grading.no");
		}
		target.append(val);
	}
}
