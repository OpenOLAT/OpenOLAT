package org.olat.core.util.vfs.filters;

import org.olat.core.util.vfs.VFSItem;

/**
 * 
 * Initial date: 24 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSAllItemsFilter implements VFSItemFilter {
	
	public static final VFSItemFilter ACCEPT_ALL = new VFSAllItemsFilter();
	
	private VFSAllItemsFilter() {
		//
	}

	@Override
	public boolean accept(VFSItem vfsItem) {
		return true;
	}
}
