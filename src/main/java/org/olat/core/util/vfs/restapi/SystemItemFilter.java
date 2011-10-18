package org.olat.core.util.vfs.restapi;

import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.VFSItemFilter;

public class SystemItemFilter implements VFSItemFilter {
	
	@Override
	public boolean accept(VFSItem vfsItem) {
		String name = vfsItem.getName();
		return !name.startsWith(".");
	}
}
