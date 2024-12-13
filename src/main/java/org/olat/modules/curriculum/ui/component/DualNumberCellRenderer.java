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
package org.olat.modules.curriculum.ui.component;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.curriculum.ui.member.DualNumber;
import org.olat.modules.curriculum.ui.wizard.SingleNumber;

/**
 * 
 * Initial date: 13 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DualNumberCellRenderer implements FlexiCellRenderer {

	private final Translator translator;
	
	public DualNumberCellRenderer(Translator translator) {
		this.translator = translator;
	}
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		if(cellValue instanceof DualNumber num) {
			target.append("<span>");
			if(num.warning()) {
				renderWarning(target);
			}
			target.append(num.found()).append("/").append(num.expected()).append("</span>");
		} else if(cellValue instanceof SingleNumber num) {
			target.append("<span>");
			if(num.warning()) {
				renderWarning(target);
			}
			if(num.plus()) {
				target.append("+");
			}
			target.append(num.found()).append("</span>");
		}
	}
	
	private void renderWarning(StringOutput target) {
		target.append("<i class='o_icon o_icon-fw o_icon_warn' title='").append(translator.translate("warning")).append("'> </i> ");
	}
}
