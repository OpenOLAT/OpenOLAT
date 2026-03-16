/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus;

/**
 * 
 * Initial date: 7 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum ParallelApplicationScope {
	
	all,
	organisation;
	
	public static ParallelApplicationScope secureValue(String val) {
		for(ParallelApplicationScope scope:values()) {
			if(scope.name().equals(val)) {
				return scope;
			}
		}
		return all;
	}
}
