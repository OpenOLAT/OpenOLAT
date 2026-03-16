/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 24 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum AssignmentMethods {
	
	manual,
	automatic;
	
	public static final AssignmentMethods[] valueOfArray(String assigments) {
		AssignmentMethods[] assignmentsEnum;
		if(StringHelper.containsNonWhitespace(assigments)) {
			String[] roleArr = assigments.split(",");
			assignmentsEnum = new AssignmentMethods[roleArr.length];
			for(int i=roleArr.length; i-->0; ) {
				assignmentsEnum[i] = AssignmentMethods.valueOf(roleArr[i]);
			}
		} else {
			assignmentsEnum = new AssignmentMethods[0];
		}
		return assignmentsEnum;
	}
}
