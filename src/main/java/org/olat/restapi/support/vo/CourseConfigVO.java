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
package org.olat.restapi.support.vo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Description:<br>
 * The course configuration
 * 
 * <P>
 * Initial Date:  27 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "courseVO")
public class CourseConfigVO {
	
	private Boolean calendar;
	private Boolean chat;
	private String cssLayoutRef;
	private Boolean efficencyStatement;
	private String glossarySoftkey;
	private String sharedFolderSoftKey;
	
	public CourseConfigVO() {
		//make JAXB happy
	}

	public Boolean getCalendar() {
		return calendar;
	}

	public void setCalendar(Boolean calendar) {
		this.calendar = calendar;
	}

	public Boolean getChat() {
		return chat;
	}

	public void setChat(Boolean chat) {
		this.chat = chat;
	}

	public String getCssLayoutRef() {
		return cssLayoutRef;
	}

	public void setCssLayoutRef(String cssLayoutRef) {
		this.cssLayoutRef = cssLayoutRef;
	}
	
	public Boolean getEfficencyStatement() {
		return efficencyStatement;
	}

	public void setEfficencyStatement(Boolean efficencyStatement) {
		this.efficencyStatement = efficencyStatement;
	}

	public String getGlossarySoftkey() {
		return glossarySoftkey;
	}

	public void setGlossarySoftkey(String glossarySoftkey) {
		this.glossarySoftkey = glossarySoftkey;
	}

	public String getSharedFolderSoftKey() {
		return sharedFolderSoftKey;
	}

	public void setSharedFolderSoftKey(String sharedFolderSoftKey) {
		this.sharedFolderSoftKey = sharedFolderSoftKey;
	}
}
