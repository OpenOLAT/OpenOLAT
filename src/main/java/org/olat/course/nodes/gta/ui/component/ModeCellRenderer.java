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
package org.olat.course.nodes.gta.ui.component;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

public class ModeCellRenderer extends StaticFlexiCellRenderer {

	public ModeCellRenderer(String action) {
		super("", action, true, true);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if (cellValue instanceof Mode) {
			Mode mode = (Mode) cellValue;
			switch (mode) {
			case EDIT:
				setIconLeftCSS("o_icon_edit o_icon-lg");
				break;
			case VIEW:
				setIconLeftCSS("o_icon_preview o_icon-lg");
				break;
			default:
				setIconLeftCSS(null);
				break;
			}
		} else {
			setIconLeftCSS(null);
		}
		super.render(renderer, target, cellValue, row, source, ubu, translator);
	}

}