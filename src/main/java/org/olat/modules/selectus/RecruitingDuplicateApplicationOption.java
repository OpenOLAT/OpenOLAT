/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus;

/**
 * 
 * Initial date: 19 mars 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public enum RecruitingDuplicateApplicationOption {
	
	ALLOWED,
	NOT_ALLOWED,
	AT_POSITION;

	public static final RecruitingDuplicateApplicationOption propertyOf(String prop) {
		RecruitingDuplicateApplicationOption p;
		if("true".equals(prop)) {
			p = RecruitingDuplicateApplicationOption.ALLOWED;
		} else if("position".equals(prop)) {
			p = RecruitingDuplicateApplicationOption.AT_POSITION;
		} else {
			p = RecruitingDuplicateApplicationOption.NOT_ALLOWED;
		}
		return p;
	}
}
