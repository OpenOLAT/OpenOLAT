/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.review;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 3 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface ReviewElementDefinition extends CreateInfo, ModifiedInfo {
	
	public static final double MIN_SLIDER_VALUE = 1.0d;
	
	public Long getKey();
	
	public ReviewElementType getType();
	
	public String getLabel();
	
	public void setLabel(String label);

}
