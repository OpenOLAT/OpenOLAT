/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 17 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum FilterPermissions {
	
	author("role.author"),
	secretary("role.secretary"),
	head("role.head"),
	exofficio("role.exofficio"),
	member("role.member");
	
	private final String i18nKey;

	private FilterPermissions(String i18nKey) {
		this.i18nKey = i18nKey;
	}
	
	public String i18nKey() {
		return i18nKey;
	}
	
	public static FilterPermissions[] valueOfArray(String sharingPermissions) {
		FilterPermissions[] enumArray;
		if(StringHelper.containsNonWhitespace(sharingPermissions)) {
			String[] permissionsArr = sharingPermissions.split(",");
			enumArray = new FilterPermissions[permissionsArr.length];
			for(int i=permissionsArr.length; i-->0; ) {
				if(permissionsArr[i].equals("staff")) {
					permissionsArr[i] = "author";
				}
				enumArray[i] = FilterPermissions.valueOf(permissionsArr[i]);
			}
		} else {
			enumArray = new FilterPermissions[0];
		}
		return enumArray;
	}
	
	public static List<String> toStringList() {
		List<String> list = new ArrayList<>(6);
		for(FilterPermissions permission:values()) {
			list.add(permission.name());
		}
		return list;
	}
	
	public static boolean isInArray(FilterPermissions[] array, FilterPermissions value) {
		for(FilterPermissions element:array) {
			if(element == value) {
				return true;
			}
		}
		return false;
	}
}
