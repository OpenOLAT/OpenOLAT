/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus;

/**
 * 
 * Initial date: 31 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum RecruitingTableContextualOption {
	
	always,
	context,
	disabled;
	
	public boolean isDisabled() {
		return this == disabled;
	}
	
	public boolean isVisible() {
		return this == context || this == always;
	}

}
