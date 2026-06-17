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
package org.olat.modules.selectus.ui.components;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

import org.olat.modules.selectus.model.Category;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.model.AppToCategory;

/**
 * 
 * Initial date: 16 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CategoriesCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof List) {
			renderCategories(target, (List<?>) cellValue); 
		}
	}
	
	private void renderCategories(StringOutput target, List<?> categories) {
		for(Object category:categories) {
			if(category instanceof Category) {
				target.append(RecruitingHelper.getLabel((Category)category));
			} else if(category instanceof AppToCategory) {
				AppToCategory cat = (AppToCategory)category;
				target.append(RecruitingHelper.getLabel(cat.getCategoryName(), cat.getCategoryColor(), cat.administrative()));
			} else if(category instanceof ApplicationCategoryInfos) {
				ApplicationCategoryInfos cat = (ApplicationCategoryInfos)category;
				target.append(RecruitingHelper.getLabel(cat.getCategory().getName(), cat.getCategory().getColor(), cat.isAdministrative()));
			} else {
				target.append(RecruitingHelper.getLabel(category.toString(), null, false));
			}
		}
	}
}
