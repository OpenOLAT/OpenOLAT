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
package org.olat.modules.curriculum.ui.wizard;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 20 avr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class NoteCellRenderer extends StaticFlexiCellRenderer {
	
	public static final String CMD_NOTE = "note";
	
	public NoteCellRenderer() {
		super("", CMD_NOTE);
	}
	
	protected static final String getId(int row) {
		return "o_c" + CMD_NOTE + "_" + row;
	}
	
	@Override
	protected String getId(Object cellValue, int row, FlexiTableComponent source) {
		return getId(row);
	}
	
	@Override
	protected String getLabel(Renderer renderer, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu,
			Translator translator) {
		if(Boolean.TRUE.equals(cellValue)) {
			return "<span><i class='o_icon o_icon-fw o_icon_notes'> </i></span>";
		}
		return null;
	}
}
