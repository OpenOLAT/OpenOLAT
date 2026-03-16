/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

/**
 * 
 * Initial date: 17.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface BusinessInformations {
	
	public String getOrganization();
	
	public void setOrganization(String organization);
	
	public String getAffiliation();
	
	public String getUnit();

	public void setUnit(String unit);

	public String getCurrentPosition();

	public void setCurrentPosition(String currentPosition);

}
