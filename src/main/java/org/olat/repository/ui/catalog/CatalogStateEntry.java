package org.olat.repository.ui.catalog;

import org.olat.core.id.context.StateEntry;
import org.olat.repository.CatalogEntry;

/**
 * 
 * Initial date: 04.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogStateEntry implements StateEntry {
	
	private static final long serialVersionUID = -5592837683379007704L;
	
	private CatalogEntry entry;
	
	public CatalogStateEntry(CatalogEntry entry) {
		this.entry = entry;
	}
	
	public CatalogEntry getEntry() {
		return entry;
	}

	@Override
	public CatalogStateEntry clone() {
		CatalogStateEntry clone = new CatalogStateEntry(entry);
		return clone;
	}

}
