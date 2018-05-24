package org.olat.user;

import java.util.Set;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 23 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface UserDataExport extends ModifiedInfo, CreateInfo {
	
	public Long getKey();
	
	public String getDirectory();
	
	public ExportStatus getStatus();
	
	public void setStatus(ExportStatus status);
	
	public Identity getIdentity();
	
	public Set<String> getExportIds();
	
	
	public enum ExportStatus {
		
		none,
		requested,
		processing,
		ready
		
	}

}
