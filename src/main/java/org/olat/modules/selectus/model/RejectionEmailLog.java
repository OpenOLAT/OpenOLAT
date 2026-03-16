/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import org.olat.core.id.CreateInfo;

/**
 * 
 * Initial date: 20 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface RejectionEmailLog extends CreateInfo  {
	
	public Long getKey();
	
	public int getStatus();
	
	public boolean isRejected();
	
	public String getMailTemplate();
	
	public ApplicationLight getApplication();
	
	

}
