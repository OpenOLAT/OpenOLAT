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

import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * Renders a translate icon (with action) followed by the plain text of the cell value.
 * Only the icon is a link, the text itself is not clickable. In the export (renderer == null),
 * only the plain text is written.
 *
 * Initial date: 12 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TranslateCellRenderer extends StaticFlexiCellRenderer {

	public static final String CMD_TRANSLATE = "translate";

	public TranslateCellRenderer() {
		this(CMD_TRANSLATE);
	}

	public TranslateCellRenderer(String action) {
		super(null, action, null, "o_icon_language", null);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		String name = cellValue == null ? null : cellValue.toString();
		if (renderer == null) {
			if (StringHelper.containsNonWhitespace(name)) {
				target.append(name);
			}
			return;
		}
		setLinkTitle(translator.translate("translate.name", name));
		super.render(renderer, target, cellValue, row, source, ubu, translator);
		if (StringHelper.containsNonWhitespace(name)) {
			target.append(" ").appendHtmlEscaped(name);
		}
	}
}
