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

package org.olat.core.commons.modules.glossary;

import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;

/**
 *
 * 
 * <P>
 * Initial Date:  15 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class Author {
	
	private String firstname;
	private String surname;
	private String link;
	
	public Author() {
		//
	}
	
	public Author(Identity identity) {
		firstname = identity.getUser().getProperty(UserConstants.FIRSTNAME, null);
		surname = identity.getUser().getProperty(UserConstants.LASTNAME, null);
		link = "[Identity:" + identity.getKey() + "]";
	}
	
	public Long extractKey() {
		if(StringHelper.containsNonWhitespace(link)) {
			int indexId = link.indexOf("[Identity:");
			int indexUsername = link.indexOf(']');
			if(indexId >= 0 && indexUsername > indexId) {
				String keyString = link.substring(indexId + 10, indexUsername);
				return Long.parseLong(keyString);
			}
		}
		return null;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
}
