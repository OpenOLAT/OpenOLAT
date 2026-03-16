/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

/**
 * 
 * Initial date: 8 déc. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface BusinessAddress extends Address {
	
	public String getPhone();

	public void setPhone(String phone);

	public String getEmail();

	public void setEmail(String email);

}
