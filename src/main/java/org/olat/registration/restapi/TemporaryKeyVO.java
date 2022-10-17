/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.registration.restapi;

import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.registration.TemporaryKey;

/**
 * 
 * Initial date: 15.10.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "temporaryKeyVO")
public class TemporaryKeyVO {
	
	private Long key;
	private String emailAddress;
	private String ipAddress;
	private Date creationDate;
	private String registrationKey;
	private String regAction;
	private boolean mailSent;
	
	public TemporaryKeyVO() {
		//
	}
	
	public TemporaryKeyVO(TemporaryKey tk) {
		this.key = tk.getKey();
		this.emailAddress = tk.getEmailAddress();
		this.ipAddress = tk.getIpAddress();
		this.creationDate = tk.getCreationDate();
		this.registrationKey = tk.getRegistrationKey();
		this.regAction = tk.getRegAction();
		this.mailSent = tk.isMailSent();
	}
	
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}
	
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}
	
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public String getRegistrationKey() {
		return registrationKey;
	}
	
	public void setRegistrationKey(String registrationKey) {
		this.registrationKey = registrationKey;
	}
	
	public String getRegAction() {
		return regAction;
	}
	
	public void setRegAction(String regAction) {
		this.regAction = regAction;
	}
	
	public boolean isMailSent() {
		return mailSent;
	}
	
	public void setMailSent(boolean mailSent) {
		this.mailSent = mailSent;
	}
}
