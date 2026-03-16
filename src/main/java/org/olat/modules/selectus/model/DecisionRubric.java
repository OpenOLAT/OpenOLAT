/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 16 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface DecisionRubric extends CreateInfo, ModifiedInfo {
	
	public Long getKey();
	
	public ApplicationLight getApplication();
	
	public DecisionRubricDefinition getDefinition();
	
	public String getStringValue();

	public void setStringValue(String stringValue);

	public Integer getIntegerValue();

	public void setIntegerValue(Integer integerValue);

}
