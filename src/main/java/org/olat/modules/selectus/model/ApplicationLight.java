/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;

import org.olat.modules.selectus.model.attributes.PositionAttributeDefinitionConfiguration;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface ApplicationLight extends ApplicationShort, OLATResourceable, ModifiedInfo {
	
	public Long getPositionKey();

	public void setDecision(Integer decision);
	
	public String[] getAdditionalValues();
	
	public String getAdditionalValue(int index);
	
	public PositionAttributeDefinitionConfiguration[] getAdditionalTypes();
	
	public PositionAttributeDefinitionConfiguration getAdditionalType(int index);

}
