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
package org.olat.user.ui.organisation.component;

import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 8 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InheritanceModeFlexiCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public InheritanceModeFlexiCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator t) {
		if(cellValue == GroupMembershipInheritance.root) {
			render(target, "o_icon_inheritance_root", translator.translate("table.tooltip.inheritance.root"));
		} else if(cellValue == GroupMembershipInheritance.inherited) {
			render(target, "o_icon_inheritance_inherited", translator.translate("table.tooltip.inheritance.inherited"));
		} else if(cellValue == GroupMembershipInheritance.none) {
			render(target, "o_icon_inheritance_none", translator.translate("table.tooltip.inheritance.none"));
		}
	}
	
	private void render(StringOutput target, String icon, String tooltip) {
		target.append("<i title='").append(tooltip).append("' class='o_icon ").append(icon).append("'> </i>");
	}
}
