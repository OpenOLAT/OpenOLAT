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
package org.olat.modules.scorm.assessment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 19 oct. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ScoCmiDetailsTableModel extends DefaultFlexiTableDataModel<CmiData>
implements SortableFlexiTableDataModel<CmiData>, FilterableFlexiTableModel {
	
	private static final CmiDataCols[] COLS = CmiDataCols.values();
	
	private List<CmiData> backupRows;
	private final Translator translator;
	private final Pattern pattern = Pattern.compile("[0-9]");
	
	public ScoCmiDetailsTableModel(FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
		this.translator = translator;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CmiData> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, translator.getLocale()).sort();
			super.setObjects(rows);
		}
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if (StringHelper.containsNonWhitespace(searchString)) {
			List<CmiData> filteredRows = new ArrayList<>();
			searchString = searchString.toLowerCase();
			for (CmiData row : backupRows) {
				if (accept(searchString, row)) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupRows);
		}
	}
	
	private boolean accept(String searchString, CmiData entry) {
		return accept(searchString, entry.getItemId()) || accept(searchString, entry.getKey())
				|| accept(searchString, entry.getValue()) || accept(searchString, translate(entry.getKey()));
	}

	private boolean accept(String searchString, String value) {
		return StringHelper.containsNonWhitespace(value) && value.toLowerCase().contains(searchString);
	}

	@Override
	public Object getValueAt(int row, int col) {
		CmiData data = getObject(row);
		return getValueAt(data, col);
	}

	@Override
	public Object getValueAt(CmiData data, int col) {
		return switch(COLS[col]) {
			case itemId -> data.getItemId();
			case translatedKey -> translate(data.getKey());
			case key -> data.getKey();
			case value -> data.getValue();
			default -> "ERROR";
		};
	}
	
	@Override
	public void setObjects(List<CmiData> objects) {
		backupRows = objects;
		super.setObjects(objects);
	}
	
	private String translate(String key) {
		String translation;
		
		try {
			Matcher matcher = pattern.matcher(key);
			if(matcher.find()) {
				String pos = key.substring(matcher.start(), matcher.end());
				if(matcher.find()) {
					String pos2 = key.substring(matcher.start(), matcher.end());
					key = key.replace(pos + ".", "").replace(pos2 + ".", "");
					translation = translator.translate(key, pos, pos2);
				}
				else {
					key = key.replace(pos + ".", "");
					translation = translator.translate(key, pos);
				}
			}
			else {
				translation = translator.translate(key);
			}
			if(translation == null || translation.length() > 150) {
				return key;
			}
			return translation;
		}
		catch(Exception ex) {
			return key;
		}
	}
	
	public enum CmiDataCols implements FlexiSortableColumnDef {
		itemId("cmis.column.header.itemId"),
		translatedKey("cmis.column.header.translatedKey"),
		key("cmis.column.header.key"),
		value("cmis.column.header.value");
		
		private final String i18nKey;
		
		private CmiDataCols(String i18nKey) {
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
