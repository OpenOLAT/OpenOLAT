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
package org.olat.modules.taxonomy.ui.importwizard;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.manager.importexport.FileMessage;
import org.olat.modules.taxonomy.manager.importexport.FileRow.Status;

/**
 * Renders the status of a row or the list of its validation messages.
 * 
 * Initial date: 30 Sept 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class ImportReviewCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if(cellValue instanceof Status status) {
			String icon = switch(status) {
				case created -> "o_icon_add";
				case changed -> "o_icon_edit";
				case unchanged -> "o_icon_check";
				case error -> "o_icon_error";
			};
			target.append("<span class='o_nowrap o_taxonomy_import_status o_taxonomy_import_").append(status.name()).append("'>")
				.append("<i class='o_icon o_icon-fw ").append(icon).append("'> </i> ")
				.append(translator.translate("importexport.status." + status.name()))
				.append("</span>");
		} else if(cellValue instanceof List<?> messages) {
			for(Object object:messages) {
				if(object instanceof FileMessage message) {
					target.append("<div class='").append(message.isError() ? "o_error" : "o_warning").append("'>")
						.append("<i class='o_icon o_icon-fw ").append(message.isError() ? "o_icon_error" : "o_icon_warn").append("'> </i> ");
					if(message.column() != null) {
						target.append(StringHelper.escapeHtml(translator.translate(message.column()))).append(": ");
					}
					target.append(StringHelper.escapeHtml(translator.translate(message.i18nKey(), message.args())))
						.append("</div>");
				}
			}
		} else if(cellValue != null) {
			target.append(StringHelper.escapeHtml(cellValue.toString()));
		}
	}
}
