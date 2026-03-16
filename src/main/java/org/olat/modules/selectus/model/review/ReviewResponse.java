/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.review;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 3 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface ReviewResponse extends CreateInfo, ModifiedInfo {

	public Long getKey();
	
	public String getStringValue();

	public void setStringValue(String stringValue);

	public Integer getIntegerValue();

	public void setIntegerValue(Integer integerValue);
	
	public Identity getReviewer();
	
	public ReviewElementDefinition getElement();
}
