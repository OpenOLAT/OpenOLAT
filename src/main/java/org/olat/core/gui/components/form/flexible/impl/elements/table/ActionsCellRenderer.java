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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import java.util.List;

import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 13 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ActionsCellRenderer extends StaticFlexiCellRenderer {
	
	public static final String CMD_ACTIONS = "oActions";
	private static final List<String> actions = List.of(CMD_ACTIONS);
	
	public ActionsCellRenderer(Translator translator) {
		super("", CMD_ACTIONS);
		setIconLeftCSS("o_icon-lg o_icon_actions");
		setLinkTitle(translator.translate("action.more"));
	}
	
	@Override
	protected String getId(Object cellValue, int row, FlexiTableComponent source) {
		return getId(row);
	}

	@Override
	public List<String> getActions() {
		return actions;
	}
	
	public static String getId(int row) {
		return "o_c" + CMD_ACTIONS + "_" + row;
	}

	@Override
	protected String getLabel(Renderer renderer, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu,
			Translator translator) {
		return "";
	}
}
