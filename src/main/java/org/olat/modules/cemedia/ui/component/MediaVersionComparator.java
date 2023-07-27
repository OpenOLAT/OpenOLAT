package org.olat.modules.cemedia.ui.component;

import java.util.Comparator;
import java.util.Date;

import org.olat.modules.cemedia.MediaVersion;

/**
 * 
 * Initial date: 27 juil. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaVersionComparator implements Comparator<MediaVersion> {

	@Override
	public int compare(MediaVersion o1, MediaVersion o2) {
		Date c1 = o1.getCollectionDate() == null ? o1.getCreationDate() : o1.getCollectionDate();
		Date c2 = o2.getCollectionDate() == null ? o2.getCreationDate() : o2.getCollectionDate();
		return c1.compareTo(c2);
	}
}
