/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import java.util.Locale;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 13 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface OrganisationUnit extends Persistable, CreateInfo, ModifiedInfo {
	
	public String getName();
	
	public void setName(String name);
	
	public String getNameDe();
	
	public void setNameDe(String name);
	
	public String getNameFr();
	
	public void setNameFr(String name);
	
	public String getName(Locale locale);
	
	public void setName(String name, Locale locale);
	
	public String getMLName(Locale locale);
	
	public String getUrl();
	
	public void setUrl(String url);
	
	public String getDescription();
	
	public void setDescription(String description);
	
	/**
	 * Is this organization unit using the system configuration or must
	 * the system configuration be overriden by the ones of the unit.
	 * 
	 * @return
	 */
	public boolean isSystemConfiguration();
	
	public void setSystemConfiguration(boolean config);
	
	public String getStaffMail();
	
	public void setStaffMail(String staffMail);

	public String getStaffBcc();
	
	public void setStaffBcc(String staffBcc);
	
	public String getMailSignature();
	
	public void setMailSignature(String signature);
	

}
