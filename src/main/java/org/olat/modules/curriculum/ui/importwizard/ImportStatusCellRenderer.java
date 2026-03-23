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
package org.olat.modules.curriculum.ui.importwizard;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 10 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportStatusCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public ImportStatusCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if(cellValue instanceof ImportCurriculumsStatus status) {
			Object rowObject = source.getFormItem().getTableDataModel().getObject(row);
			if(rowObject instanceof AbstractImportRow importedRow) {
				target.append("<span>");
				CurriculumImportedStatistics stats = importedRow.getValidationStatistics();
				if(stats != null) {
					if(stats.errors() > 0) {
						renderIcon(target, "o_icon_validation_error");
					} else if(stats.warnings() > 0) {
						renderIcon(target, "o_icon_validation_warning");
					}
				}
				
				String i18nKey = "status.no.changes";
				if(status == ImportCurriculumsStatus.NEW) {
					i18nKey = "status.new";
				} else if(status == ImportCurriculumsStatus.MODIFIED) {
					i18nKey = "status.changed";
				} 
				target.append(translator.translate(i18nKey)).append("</span>");
			}
		}
	}
	
	private void renderIcon(StringOutput target, String iconCssClass) {
		target.append("<i class='o_icon o_icon-fw ").append(iconCssClass).append("'> </i> ");
	}
}
