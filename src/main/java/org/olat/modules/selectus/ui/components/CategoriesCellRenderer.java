/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
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
