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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.modules.fo;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;

/**
 * @author schneider
 */
public interface Message extends MessageLight, CreateInfo, ModifiedInfo,  Persistable, Comparable<Message> {
	
	public String getTitle();
	
	public void setTitle(String string);
	
	public String getBody();
	
	public void setBody(String string);
	
	public Identity getCreator();
	
	public boolean isGuest();
	
	public String getPseudonym();
	
	public void setPseudonym(String pseudonym);
	
	public Forum getForum();
	
	public Identity getModifier();
	
	public void setModifier(Identity identity);
	
	/**
	 * @return The date the modifier makes a change
	 */
	public Date getModificationDate();
	
	public void setModificationDate(Date date);
	
	public Message getParent();
	
	public void setParent(Message message);
	
	public Message getThreadtop();
	
	public void setThreadtop(Message message);

	public Integer getNumOfWords();
	
	public void setNumOfWords(Integer numOfWords);
	
	public Integer getNumOfCharacters();

	public void setNumOfCharacters(Integer numOfCharacters);
	
	public int getStatusCode();
	
	public void setStatusCode(int statusCode);
	
	
	public enum OrderBy {
		title,
		creationDate
	}
}