/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 13 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface ApplicationComment extends ModifiedInfo, CreateInfo {
	
	public Long getKey();
	
	public boolean isDeleted();
	
	public String getComment();
	
	public void setComment(String comment);
	
	public Identity getAuthor();
	
	public Application getApplication();
	
	public Identity getReviewer();
	
	public ApplicationComment getParentComment();

}
