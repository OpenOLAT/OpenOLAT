/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 15.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum ReferenceStatus {
	
	notSent,
	sentAwaiting,
	submitted,
	late,
	deactivated;
	
	
	public static ReferenceStatus[] valueOfArray(String status) {
		ReferenceStatus[] statusEnum;
		if(StringHelper.containsNonWhitespace(status)) {
			String[] statusArr = status.split("[,]");
			statusEnum = new ReferenceStatus[statusArr.length];
			for(int i=statusArr.length; i-->0; ) {
				statusEnum[i] = ReferenceStatus.valueOf(statusArr[i]);
			}
		} else {
			statusEnum = new ReferenceStatus[0];
		}
		return statusEnum;
	}
	
	public static final List<String> toNameList(ReferenceStatus... referenceStatus) {
		List<String> statusList = new ArrayList<>();
		if(referenceStatus != null && referenceStatus.length > 0) {
			for(ReferenceStatus status:referenceStatus) {
				statusList.add(status.name());
			}
		}
		return statusList;
	}
	
	public static boolean isActive(ReferenceStatus status, ReferenceRequestStatus requestStatus) {
		return status != ReferenceStatus.deactivated && requestStatus != ReferenceRequestStatus.declined;
	}
}
