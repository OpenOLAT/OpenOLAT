package org.olat.search.service.indexer;

import org.olat.core.util.vfs.VFSItem;

public interface FolderIndexerAccess {
	public static final FolderIndexerFullAccess FULL_ACCESS = new FolderIndexerFullAccess();
	
	public boolean allowed(VFSItem item);
}
