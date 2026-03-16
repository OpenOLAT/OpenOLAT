/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 23 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface RecruitingAuditLogUserSettings extends CreateInfo {
	
	public Long getKey();
	
	public boolean isEnabled();

	public void setEnabled(boolean enabled);

	public String getInterval();

	public void setInterval(String interval);

	public Identity getIdentity();

}
