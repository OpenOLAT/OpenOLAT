/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager.comparator;

import java.util.Comparator;
import java.util.Date;

import org.olat.modules.selectus.model.ApplicationComment;

/**
 * Older first
 * 
 * Initial date: 15 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationCommentCreationDateComparator implements Comparator<ApplicationComment> {

	@Override
	public int compare(ApplicationComment o1, ApplicationComment o2) {
		Date creationDate1 = o1.getCreationDate();
		Date creationDate2 = o2.getCreationDate();
		return creationDate2.compareTo(creationDate1);
	}
}
