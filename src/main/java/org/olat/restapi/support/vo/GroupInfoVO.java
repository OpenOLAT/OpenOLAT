/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.restapi.support.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Description:<br>
 * GroupInfoVO
 * 
 * <P>
 * Initial Date:  26 aug. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "groupInfoVO")
public class GroupInfoVO extends GroupVO {
	
	private String news;
	private Long forumKey;
	private Boolean hasWiki = Boolean.FALSE;
	private Boolean hasFolder = Boolean.FALSE;
	
	public GroupInfoVO() {
		//make JAXB happy
	}

	public String getNews() {
		return news;
	}

	public void setNews(String news) {
		this.news = news;
	}

	public Long getForumKey() {
		return forumKey;
	}

	public void setForumKey(Long forumKey) {
		this.forumKey = forumKey;
	}

	public Boolean getHasWiki() {
		return hasWiki;
	}

	public void setHasWiki(Boolean hasWiki) {
		this.hasWiki = hasWiki;
	}

	public Boolean getHasFolder() {
		return hasFolder;
	}

	public void setHasFolder(Boolean hasFolder) {
		this.hasFolder = hasFolder;
	}
}
