/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager.comparator;

import java.util.Comparator;
import java.util.Date;

import org.olat.modules.selectus.model.RecruitingAuditLog;

/**
 * 
 * Initial date: 23 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RecruitingAuditLogCreationDateComparator implements Comparator<RecruitingAuditLog> {

	@Override
	public int compare(RecruitingAuditLog o1, RecruitingAuditLog o2) {
		Date creationDate1 = o1.getCreationDate();
		Date creationDate2 = o2.getCreationDate();
		return creationDate2.compareTo(creationDate1);
	}
	
}
