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
package org.olat.course.nodes.livestream.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.course.nodes.livestream.model.UrlTemplate;

/**
 * 
 * Initial date: 4 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UrlTemplateDataModel extends DefaultFlexiTableDataModel<UrlTemplate>
implements SortableFlexiTableDataModel<UrlTemplate> {
	
	private final Locale locale;
	
	public UrlTemplateDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<UrlTemplate> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		UrlTemplate reason = getObject(row);
		return getValueAt(reason, col);
	}

	@Override
	public Object getValueAt(UrlTemplate row, int col) {
		switch(UrlTemplateCols.values()[col]) {
			case id: return row.getKey();
			case name: return row.getName();
			case url1: return row.getUrl1();
			case url2: return row.getUrl2();
			default: return null;
		}
	}

	@Override
	public DefaultFlexiTableDataModel<UrlTemplate> createCopyWithEmptyList() {
		return new UrlTemplateDataModel(getTableColumnModel(), locale);
	}
	
	public enum UrlTemplateCols implements FlexiSortableColumnDef {
		id("url.template.id"),
		name("url.template.name"),
		url1("url.template.url1"),
		url2("url.template.url2");
		
		private final String i18nKey;
		
		private UrlTemplateCols(String i18nKey) {
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