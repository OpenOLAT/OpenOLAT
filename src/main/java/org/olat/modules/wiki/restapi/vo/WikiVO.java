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
package org.olat.modules.wiki.restapi.vo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Represents a wiki-Resource<br />
 * There are two "types":
 * <ul>
 * <li>wikis as repositoryEntries (wikis that are used in courses)</li>
 * <li>Wikis that are used in learningGroups</li>
 * </ul>
 * 
 * @author strentini, sergio.trentini@frentix.com, http://www.frentix.com
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "wikiVO")
public class WikiVO {

	/**
	 * the resourceable-id (of a repositoryEntry if isGroupWiki=false) the
	 * resourceable-id (of a learningGroup if isGroupWiki=true) <br />
	 * <p>
	 * in both cases, you can retrieve a wiki by using this key:
	 * http://www.domain.ch/olat/restapi/wikis/key/
	 * </p>
	 **/
	private Long key;
	private String softkey;
	private String title;

	public WikiVO() {

	}

	public Long getKey() {
		return key;
	}

	public String getSoftkey() {
		return softkey;
	}

	public String getTitle() {
		return title;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public void setSoftkey(String softkey) {
		this.softkey = softkey;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("WikiVO[key=").append(key).append(":title=").append(title);
		sb.append("]");
		return sb.toString();
	}

}
