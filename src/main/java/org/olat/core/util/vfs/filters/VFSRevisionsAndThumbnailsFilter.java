package org.olat.core.util.vfs.filters;

import org.olat.core.util.vfs.VFSItem;

/**
 * Accept all files but thumbnails and revisions.
 * 
 * Initial date: 24 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSRevisionsAndThumbnailsFilter implements VFSItemFilter {

	@Override
	public boolean accept(VFSItem vfsItem) {
		String name = vfsItem.getName();
		return !name.startsWith("._oo_th_") && !name.startsWith("._oo_vr_");
	}
}
