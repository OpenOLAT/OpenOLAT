/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import org.olat.core.id.CreateInfo;

/**
 * 
 * Initial date: 27 oct. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface ReferenceComment extends CreateInfo {
	
	public Long getKey();
	
	public String getComment();
	
	public Reference getReference();

}
