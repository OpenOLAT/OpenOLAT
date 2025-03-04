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
package org.olat.repository;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Initial date: Mär 03, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class DefaultCycleBadgeRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Object cellValue,
					   int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if (cellValue instanceof Boolean isDefault && Boolean.TRUE.equals(isDefault)) {
			String title = translator.translate("lifecycle.default");
			sb.append("<span class='o_labeled_light o_default_cycle_badge' title='").append(title).append("'>")
					.append("<i class='o_icon o_icon-fw o_icon_star'> </i> ")
					.append("<span>").append(title).append("</span>")
					.append("</span>");
		}
	}
}
