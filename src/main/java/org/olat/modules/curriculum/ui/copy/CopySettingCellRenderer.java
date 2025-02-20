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
package org.olat.modules.curriculum.ui.copy;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyResources;

/**
 * 
 * Initial date: 18 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CopySettingCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public CopySettingCellRenderer(Translator translator) {
		this.translator = translator;
	}
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if(cellValue instanceof CopyResources copySetting) {
			render(target, copySetting);
		}
	}
	
	private void render(StringOutput target, CopyResources copySetting) {
		if(copySetting == CopyResources.relation) {
			renderActivity(target, "reuse", "o_activity_modify", "o_icon_recycle");
		} else if(copySetting == CopyResources.resource) {
			renderActivity(target, "copy", "o_activity_add", "o_icon_copy");
		}
	}
	
	private void renderActivity(StringOutput target, String i18nKey, String cssClass, String iconCssClass) {
		target.append("<span class='").append(cssClass).append("'><i class='o_icon o_icon-fw ").append(iconCssClass).append("' title='")
	      .append(translator.translate(i18nKey))
	      .append("'> </i></span>");
	}
}
