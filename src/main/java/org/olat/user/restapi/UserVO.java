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
package org.olat.user.restapi;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * 
 * 
 * <P>
 * Initial Date:  7 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "userVO")
public class UserVO {

	private Long key;
	private String login;
	private String externalId;
	private String password;
	private String firstName;
	private String lastName;
	private String email;
	private String portrait;

	@XmlElementWrapper(name="properties")
	@XmlElement(name="property")
	private List<UserPropertyVO> properties = new ArrayList<>();

	public UserVO() {
		//make JAXB happy
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPortrait() {
		return portrait;
	}

	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}

	public List<UserPropertyVO> getProperties() {
		return properties;
	}

	public void setProperties(List<UserPropertyVO> properties) {
		this.properties = properties;
	}
	
	public void putProperty(String name, String value) {
		properties.add(new UserPropertyVO(name,value));
	}
	
	public String getProperty(String name) {
		for(UserPropertyVO entry:properties) {
			if(entry.getName().equals(name)) {
				return entry.getValue();
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "UserVO[key=" + key + ":name=" + login + "]";
	}
	
	@Override
	public int hashCode() {
		return key == null ? 7287 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof UserVO) {
			UserVO vo = (UserVO)obj;
			return key != null && key.equals(vo.key);
		}
		return false;
	}
}