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
package org.olat.repository.ui.catalog;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiBusinessPathModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.id.context.BusinessControlFactory;

/**
 * 
 * Initial date: 04.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogEntryRowModel extends DefaultFlexiTableDataModel<CatalogEntryRow>
	implements SortableFlexiTableDataModel<CatalogEntryRow>, FlexiBusinessPathModel {
	
	private static final Cols[] COLS = Cols.values();
	
	public CatalogEntryRowModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}
	
	@Override
	public String getUrl(Component source, Object object, String action) {
		String url = null;
		if(action != null && object instanceof CatalogEntryRow) {
			if(action.endsWith("select")) {
				CatalogEntryRow row = (CatalogEntryRow)object;
				url = BusinessControlFactory.getInstance()
						.getAuthenticatedURLFromBusinessPathString("[RepositoryEntry:" + row.getKey() + "]");
			} else if(action.endsWith("details")) {
				CatalogEntryRow row = (CatalogEntryRow)object;
				url = BusinessControlFactory.getInstance()
						.getAuthenticatedURLFromBusinessPathString("[RepositoryEntry:" + row.getKey() + "][Infos:0]");
			}
		}
		return url;
	}

	@Override
	public Object getValueAt(int row, int col) {
		CatalogEntryRow item = getObject(row);
		switch (COLS[col]) {
		case up:
			return row == 0 ? Boolean.FALSE : Boolean.TRUE;
		case down:
			return row >= (getRowCount() - 1) ? Boolean.FALSE : Boolean.TRUE;
		default:
		}
		return getValueAt(item, col);
	}
	
	@Override
	public Object getValueAt(CatalogEntryRow item, int col) {
		switch(COLS[col]) {
			case key: return item.getKey();
			case ac: return item;
			case type: return item;
			case displayName: return item.getDisplayname();
			case lifecycleLabel: return item.getLifecycleLabel();
			case lifecycleSoftkey: return item.getLifecycleSoftKey();
			case lifecycleStart: return item.getLifecycleStart();
			case lifecycleEnd: return item.getLifecycleEnd();
			case externalId: return item.getExternalId();
			case externalRef: return item.getExternalRef();
			case authors: return item.getAuthors();
			case access: return item;
			case creationDate: return item.getCreationDate();
			case detailsSupported: return item;
			case move: return item;
			case delete: return item;
			case position: return item.getPositionLink();
			default: return "";
		}
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CatalogEntryRow> views = new CatalogEntryRowSortDelegate(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}
	
	public enum Cols {
		key("table.header.key"),
		ac("table.header.ac"),
		type("table.header.typeimg"),
		lifecycleLabel("table.header.lifecycle.label"),
		lifecycleSoftkey("table.header.lifecycle.softkey"),
		lifecycleStart("table.header.lifecycle.start"),
		lifecycleEnd("table.header.lifecycle.end"),
		externalId("table.header.externalid"),
		externalRef("table.header.externalref"),
		displayName("cif.displayname"),
		authors("table.header.authors"),
		access("table.header.access"),
		creationDate("table.header.date"),
		detailsSupported("table.header.details"),
		move("tools.move.catalog.entry"),
		delete("tools.delete.catalog.entry"),
		up("table.header.up"),
		down("table.header.down"),
		position("table.header.position");
		
		private final String i18nKey;
		
		private Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
}
