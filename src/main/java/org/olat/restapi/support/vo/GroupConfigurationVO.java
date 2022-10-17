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

import java.util.Map;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;


/**
 * 
 * @author srosse
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "groupVO")
public class GroupConfigurationVO {
	
	private String[] tools;
	private Map<String,Integer> toolsAccess;
	private String news;
	
	private Boolean ownersVisible;
	private Boolean participantsVisible;
	private Boolean waitingListVisible;
	
	private Boolean ownersPublic;
	private Boolean participantsPublic;
	private Boolean waitingListPublic;
	
	public GroupConfigurationVO() {
		//
	}
	
	public String[] getTools() {
		return tools;
	}
	
	public void setTools(String[] tools) {
		this.tools = tools;
	}
	
	public Map<String,Integer> getToolsAccess() {
		return toolsAccess;
	}
	
	public void setToolsAccess(Map<String,Integer> toolsAccess) {
		this.toolsAccess = toolsAccess;
	}
	
	public String getNews() {
		return news;
	}

	public void setNews(String news) {
		this.news = news;
	}

	public Boolean getOwnersVisible() {
		return ownersVisible;
	}
	
	public void setOwnersVisible(Boolean ownersVisible) {
		this.ownersVisible = ownersVisible;
	}
	
	public Boolean getParticipantsVisible() {
		return participantsVisible;
	}
	
	public void setParticipantsVisible(Boolean participantsVisible) {
		this.participantsVisible = participantsVisible;
	}

	public Boolean getWaitingListVisible() {
		return waitingListVisible;
	}

	public void setWaitingListVisible(Boolean waitingListVisible) {
		this.waitingListVisible = waitingListVisible;
	}

	public Boolean getOwnersPublic() {
		return ownersPublic;
	}

	public void setOwnersPublic(Boolean ownersPublic) {
		this.ownersPublic = ownersPublic;
	}

	public Boolean getParticipantsPublic() {
		return participantsPublic;
	}

	public void setParticipantsPublic(Boolean participantsPublic) {
		this.participantsPublic = participantsPublic;
	}

	public Boolean getWaitingListPublic() {
		return waitingListPublic;
	}

	public void setWaitingListPublic(Boolean waitingListPublic) {
		this.waitingListPublic = waitingListPublic;
	}
}
