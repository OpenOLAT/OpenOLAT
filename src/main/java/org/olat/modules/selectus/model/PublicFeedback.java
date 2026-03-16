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
 * Initial date: 30 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface PublicFeedback extends CreateInfo, ModifiedInfo {

	public Long getKey();
	
	public String getFirstName();
	
	public String getLastName();
	
	public String getEmail();

	public String getExternalId();

	public String getExternalRef();
	
	public String getComment();
	
	public void setComment(String comment);
}
