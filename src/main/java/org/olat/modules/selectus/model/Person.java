/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */

package org.olat.modules.selectus.model;

import java.util.Date;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  22 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface Person extends PersonName {
	
	public void setTitle(String title);
	
	public void setFirstName(String firstName);
	
	public void setLastName(String lastName);

	public String getGender();
	
	public void setGender(String gender);
	
	public String getMaritalStatus();

	public void setMaritalStatus(String maritalStatus);
	
	public Boolean getDisability();
	
	public void setDisability(Boolean disability);
	
	public Date getBirthday();

	public void setBirthday(Date birthday);

	public String getNationality();

	public void setNationality(String nationality);
	
	public String getAdditionalNationalities();
	
	public void setAdditionalNationalities(String nationalities);

	public String getMail();

	public void setMail(String mail);

	public String getPhone();
	
	public void setPhone(String phone);
	
	public String getMobilePhone();

	public void setMobilePhone(String mobilePhone);
	
	public String getAcademicTitle();

	public void setAcademicTitle(String academicTitle);

}
