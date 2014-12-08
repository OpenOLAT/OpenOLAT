package org.olat.repository.ui.catalog;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 04.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogEntryRowModel extends DefaultFlexiTableDataModel<CatalogEntryRow> {
	
	public CatalogEntryRowModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public DefaultFlexiTableDataModel<CatalogEntryRow> createCopyWithEmptyList() {
		return new CatalogEntryRowModel(getTableColumnModel());
	}

	@Override
	public Object getValueAt(int row, int col) {
		CatalogEntryRow item = getObject(row);
		switch(Cols.values()[col]) {
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
		}
		return null;
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
		delete("tools.delete.catalog.entry");
		
		private final String i18nKey;
		
		private Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
}
