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
package org.olat.modules.selectus.ui.position.component;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

import org.olat.modules.selectus.ui.position.model.EditStepRow;

/**
 * 
 * Initial date: 12 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditStepStatusRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public EditStepStatusRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if(cellValue instanceof EditStepRow) {
			EditStepRow stepRow = (EditStepRow)cellValue;
			if(stepRow.isStaffOnly()) {
				renderStatus(target, "step.status.staff.only", "o_step_staff_only");
			} else if(stepRow.isEnabled()) {
				renderStatus(target, "step.status.enabled", "o_step_enabled");
			} else {
				renderStatus(target, "step.status.disabled", "o_step_disabled");
			}
		}
	}
	
	private void renderStatus(StringOutput target, String i18nKey, String cssClass) {
		target.append("<span class='o_labeled ").append(cssClass).append("'>")
		      .append(translator.translate(i18nKey)).append("</span>");
	}
}
