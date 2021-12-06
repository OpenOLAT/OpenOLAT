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
package org.olat.modules.quality.generator.ui;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 07.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class GeneratorDataModel extends DefaultFlexiTableDataModel<GeneratorRow>
		implements SortableFlexiTableDataModel<GeneratorRow> {
	
	private final Translator translator;
	
	GeneratorDataModel(FlexiTableColumnModel columnsModel, Translator translator) {
		super(columnsModel);
		this.translator = translator;
	}
	
	public GeneratorRow getObjectByKey(Long key) {
		List<GeneratorRow> rows = getObjects();
		for (GeneratorRow row: rows) {
			if (row != null && row.getGeneratorRef().getKey().equals(key)) {
				return row;
			}
		}
		return null;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<GeneratorRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, translator.getLocale()).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		GeneratorRow generator = getObject(row);
		return getValueAt(generator, col);
	}

	@Override
	public Object getValueAt(GeneratorRow row, int col) {
		switch(GeneratorCols.values()[col]) {
			case enabled: return row.isEnabled();
			case title: {
				String title = row.getTitle();
			return StringHelper.containsNonWhitespace(title)
					? title
					: translator.translate("generator.title.empty");
			}
			case providerName: return row.getProviderName();
			case numberDataCollections: return row.getNumberDataCollections();
			default: return null;
		}
	}
	
	enum GeneratorCols implements FlexiSortableColumnDef {
		enabled("generator.enabled"),
		title("generator.title"),
		providerName("generator.provider.name"),
		numberDataCollections("generator.number.data.collections");
		
		private final String i18nKey;
		
		private GeneratorCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}

}
