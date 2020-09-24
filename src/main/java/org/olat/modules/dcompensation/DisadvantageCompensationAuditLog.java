package org.olat.modules.dcompensation;

import org.olat.core.id.CreateInfo;

/**
 * 
 * Initial date: 24 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface DisadvantageCompensationAuditLog extends CreateInfo {

	public Long getKey();
	
	public String getAction();
	
	public String getBefore();
	
	public String getAfter();
	
	public String getSubIdent();

	public Long getEntryKey();

	public Long getIdentityKey();

	public Long getCompensationKey();

	public Long getAuthorKey();
	
	public enum Action {
		create,
		update,
		delete
	}

}
