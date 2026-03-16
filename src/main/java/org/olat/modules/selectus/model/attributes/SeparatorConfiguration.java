/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.attributes;

/**
 * 
 * Initial date: 17 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SeparatorConfiguration implements AttributeConfiguration {
	
	private boolean withLine;

	public boolean isWithLine() {
		return withLine;
	}

	public void setWithLine(boolean withLine) {
		this.withLine = withLine;
	}
}
