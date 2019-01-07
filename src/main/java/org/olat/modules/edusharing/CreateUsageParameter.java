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
package org.olat.modules.edusharing;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Initial date: 9 Dec 2018<br>
 * @author  uhensler, urs.hensler@frentix.com, http://www.frentix.com
 */
public class CreateUsageParameter {

	private final String resourceId;
	private final String eduRef;
	private final String user;
	private final String courseId;
	private String userMail;
	private XMLGregorianCalendar fromUsed;
	private XMLGregorianCalendar toUsed;
	private int distinctPersons;
	private String version;
	private String xmlParams;
	
	public CreateUsageParameter(String resourceId, String eduRef, String user, String courseId) {
		this.resourceId = resourceId;
		this.eduRef = eduRef;
		this.user = user;
		this.courseId = courseId;
	}

	public String getResourceId() {
		return resourceId;
	}

	public String getEduRef() {
		return eduRef;
	}

	public String getUser() {
		return user;
	}

	public String getCourseId() {
		return courseId;
	}

	public String getUserMail() {
		return userMail;
	}

	public void setUserMail(String userMail) {
		this.userMail = userMail;
	}

	public XMLGregorianCalendar getFromUsed() {
		return fromUsed;
	}

	public void setFromUsed(XMLGregorianCalendar fromUsed) {
		this.fromUsed = fromUsed;
	}

	public XMLGregorianCalendar getToUsed() {
		return toUsed;
	}

	public void setToUsed(XMLGregorianCalendar toUsed) {
		this.toUsed = toUsed;
	}

	public int getDistinctPersons() {
		return distinctPersons;
	}

	public void setDistinctPersons(int distinctPersons) {
		this.distinctPersons = distinctPersons;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getXmlParams() {
		return xmlParams;
	}

	public void setXmlParams(String xmlParams) {
		this.xmlParams = xmlParams;
	}
}