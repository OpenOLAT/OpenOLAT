/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import org.olat.core.id.Identity;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface Notes {
	
	public Identity getAuthor();
	
	public Long getApplicationKey();
	
	public String getContent();
	
	public void setContent(String notes);

}
