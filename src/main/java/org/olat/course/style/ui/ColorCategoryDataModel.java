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
package org.olat.course.style.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.course.style.ColorCategory;

/**
 * 
 * Initial date: 23 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.comm
 *
 */
public class ColorCategoryDataModel extends DefaultFlexiTableDataModel<ColorCategoryRow> {
	
	private static final ColorCategoryCols[] COLS = ColorCategoryCols.values();
	
	public ColorCategoryDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		ColorCategoryRow reason = getObject(row);
		return getValueAt(reason, col);
	}

	private Object getValueAt(ColorCategoryRow row, int col) {
		switch(COLS[col]) {
			case upDown: return row.getUpDown();
			case identifier: return row.getColorCategory().getIdentifier();
			case translaton: return row.getTranslation();
			case enabled: return Boolean.valueOf(row.getColorCategory().isEnabled());
			case color: return "o_colcat_bg " + row.getColorCategory().getCssClass();
			case cssClass: return row.getColorCategory().getCssClass();
			case edit: return Boolean.TRUE;
			case delete: return Boolean.valueOf(ColorCategory.Type.custom == row.getColorCategory().getType());
			default: return null;
		}
	}
	
	public enum ColorCategoryCols implements FlexiColumnDef {
		upDown("color.category.updown"),
		identifier("color.category.identifier"),
		translaton("color.category.translation"),
		enabled("color.category.enabled.label"),
		color("color.category.color"),
		cssClass("color.category.css.class"),
		edit("color.category.edit"),
		delete("color.category.delete");
		
		
		private final String i18nKey;
		
		private ColorCategoryCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
