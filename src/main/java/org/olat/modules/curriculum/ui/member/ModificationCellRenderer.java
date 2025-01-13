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
package org.olat.modules.curriculum.ui.member;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 29 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ModificationCellRenderer implements FlexiCellRenderer {

	private final Translator translator;
	
	public ModificationCellRenderer(Translator translator) {
		this.translator = translator;
	}
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if(cellValue instanceof Boolean modified && modified.booleanValue()) {
			renderModification(target, "has.modification", "o_membership_modify", "o_icon_retry");
		} else if(cellValue instanceof ModificationStatus modification) {
			switch(modification) {
				case MODIFICATION: renderModification(target, "has.modification", "o_membership_modify", "o_icon_retry"); break;
				case ADD: renderModification(target, "has.add", "o_membership_add", "o_icon_plus"); break;
				case REMOVE: renderModification(target, "has.remove", "o_membership_remove", "o_icon_minus"); break;
				default: break;
			}
		} else if(cellValue instanceof ModificationStatusSummary summary) {
			if(summary.addition()) {
				renderModification(target, "has.add", "o_membership_add", "o_icon_plus");
				target.append(" ");
			}
			if(summary.modification()) {
				renderModification(target, "has.modification", "o_membership_modify", "o_icon_retry");
				target.append(" ");
			}
			if(summary.removal()) {
				renderModification(target, "has.remove", "o_membership_remove", "o_icon_minus");
				target.append(" ");
			}
		}
	}
	
	private void renderModification(StringOutput target, String i18nKey, String cssClass, String iconCssClass) {
		target.append("<span class='").append(cssClass).append("'><i class='o_icon o_icon-fw ").append(iconCssClass).append("' title='")
	      .append(translator.translate(i18nKey))
	      .append("'> </i></span>");
	}
}
