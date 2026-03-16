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
 * Initial date: 15 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface Category extends CreateInfo, ModifiedInfo {
	
	public Long getKey();
	
	public String getName();
	
	public void setName(String name);
	
	public String getColor();
	
	public void setColor(String color);
	
	public Position getPosition();

}
