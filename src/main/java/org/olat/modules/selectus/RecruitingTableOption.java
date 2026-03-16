/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus;

/**
 * 
 * Initial date: 11.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum RecruitingTableOption {	
	enabled,
	optional,
	disabled;

	public boolean isDisabled() {
		return name().equals("disabled");
	}

	public boolean isVisible() {
		return name().equals("enabled");
	}
}