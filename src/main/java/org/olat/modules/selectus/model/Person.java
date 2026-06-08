/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
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
