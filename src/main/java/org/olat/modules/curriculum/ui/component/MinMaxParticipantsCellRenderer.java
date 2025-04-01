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

/**
 * 
 * Initial date: 13 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MinMaxParticipantsCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		if(cellValue instanceof MinMaxParticipants minMax) {
			if (renderer != null) {
				if (minMax.min() != null && minMax.count() != null && minMax.min() > minMax.count()) {
					target.append("<span title=\"" + trans.translate("warning.min.number.not.reached") + "\"><i class=\"o_icon o_icon_warn\"></i> ");
				} else if (minMax.max() != null && minMax.count() != null && minMax.max() < minMax.count()) {
					target.append("<span title=\"" + trans.translate("warning.max.number.exceeded") + "\"><i class=\"o_icon o_icon_warn\"></i> ");
				}
			}
			renderValue(target, minMax.min());
			target.append("/");
			renderValue(target, minMax.max());
		}
	}
	
	private void renderValue(StringOutput target, Long val) {
		if(val == null) {
			target.append("-");
		} else {
			target.append(val.longValue());
		}
	}
}
