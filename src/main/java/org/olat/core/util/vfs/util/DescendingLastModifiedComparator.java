package org.olat.core.util.vfs.util;

import java.util.Comparator;

import org.olat.core.util.vfs.VFSItem;

/**
 * <p>
 * Orders VFSItems by their last modified date descendingly
 * <p>
 * Initial Date: Sep 16, 2009 <br>
 * 
 * @author gwassmann, gwassmann@frentix.com, www.frentix.com
 */
public class DescendingLastModifiedComparator implements Comparator<VFSItem> {
	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(VFSItem o1, VFSItem o2) {
		long o1mod = o1.getLastModified();
		long o2mod = o2.getLastModified();
		return o1mod > o2mod ? -1 : 1;
	}
}