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
package org.olat.restapi.system.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * <h3>Description:</h3>
 * 
 * Initial Date:  21 juin 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "sessionsVO")
public class SessionsVO {

	@XmlAttribute(name="count", required=true)
	private int count;
	@XmlAttribute(name="authenticatedCount", required=true)
	private int authenticatedCount;
	@XmlAttribute(name="secureAuthenticatedCount", required=true)
	private int secureAuthenticatedCount;
	@XmlAttribute(name="webdavCount", required=true)
	private int webdavCount;
	@XmlAttribute(name="secureWebdavCount", required=true)
	private int secureWebdavCount;
	@XmlAttribute(name="restCount", required=true)
	private int restCount;
	@XmlAttribute(name="secureRestCount", required=true)
	private int secureRestCount;
	@XmlAttribute(name="instantMessagingCount", required=true)
	private int instantMessagingCount;
	
	public SessionsVO() {
		//make JAXB happy
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getAuthenticatedCount() {
		return authenticatedCount;
	}

	public void setAuthenticatedCount(int authenticatedCount) {
		this.authenticatedCount = authenticatedCount;
	}
	
	public int getSecureAuthenticatedCount() {
		return secureAuthenticatedCount;
	}

	public void setSecureAuthenticatedCount(int secureAuthenticatedCount) {
		this.secureAuthenticatedCount = secureAuthenticatedCount;
	}

	public int getWebdavCount() {
		return webdavCount;
	}

	public void setWebdavCount(int webdavCount) {
		this.webdavCount = webdavCount;
	}

	public int getSecureWebdavCount() {
		return secureWebdavCount;
	}

	public void setSecureWebdavCount(int secureWebdavCount) {
		this.secureWebdavCount = secureWebdavCount;
	}

	public int getRestCount() {
		return restCount;
	}

	public void setRestCount(int restCount) {
		this.restCount = restCount;
	}

	public int getSecureRestCount() {
		return secureRestCount;
	}

	public void setSecureRestCount(int secureRestCount) {
		this.secureRestCount = secureRestCount;
	}

	public int getInstantMessagingCount() {
		return instantMessagingCount;
	}

	public void setInstantMessagingCount(int instantMessagingCount) {
		this.instantMessagingCount = instantMessagingCount;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Sessions[count=").append(getCount()).append(':')
			.append("authenticatedCount=").append(getAuthenticatedCount()).append(']');
		return sb.toString();
	}
}
