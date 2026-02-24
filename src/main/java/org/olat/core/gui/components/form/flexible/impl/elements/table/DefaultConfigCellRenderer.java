/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 *
 * Initial date: 20 Feb 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class DefaultConfigCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Object cellValue,
					   int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if (cellValue instanceof Boolean isDefault && Boolean.TRUE.equals(isDefault)) {
			Translator t = Util.createPackageTranslator(DefaultConfigCellRenderer.class, translator.getLocale());
			String title = t.translate("default");
			sb.append("<span class='o_labeled_light o_default_config'>")
					.append("<i class='o_icon o_icon-fw o_icon_default_config'> </i> ")
					.append("<span>").append(title).append("</span>")
					.append("</span>");
		}
	}
}
