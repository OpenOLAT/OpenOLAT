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
package org.olat.modules.adobeconnect.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 18 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectContentTableModel extends DefaultFlexiTableDataModel<AdobeConnectContentRow>
implements SortableFlexiTableDataModel<AdobeConnectContentRow> {
	
	private final Locale locale;
	
	public AdobeConnectContentTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<AdobeConnectContentRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	public Integer indexOf(String scoId) {
		for(int i=getRowCount(); i-->0; ) {
			AdobeConnectContentRow content = getObject(i);
			if(scoId.equals(content.getSco().getScoId())) {
				return Integer.valueOf(i);
			}
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		AdobeConnectContentRow content = getObject(row);
		return getValueAt(content, col) ;
	}
	
	@Override
	public Object getValueAt(AdobeConnectContentRow row, int col) {
		switch(ACContentsCols.values()[col]) {
			case icon: return row.getSco();
			case dateBegin: return row.getSco().getDateBegin();
			case type: return row.getSco().getType();
			case name: return row.getSco().getName();
			case resource: return row.getOpenLink();
			default: return "ERROR";
		}
	}
	
	public enum ACContentsCols implements FlexiSortableColumnDef {
		
		icon("content.icon"),
		dateBegin("content.begin"),
		type("content.type"),
		name("content.name"),
		resource("content.resource");
		
		private final String i18nHeaderKey;
		
		private ACContentsCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
}
