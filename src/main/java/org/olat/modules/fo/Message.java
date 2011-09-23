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
* <p>
*/ 

package org.olat.modules.fo;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;

/**
 * @author schneider
 */
public interface Message extends CreateInfo, ModifiedInfo,  Persistable, Comparable<Message> {
	public abstract String getBody();
	public abstract Identity getCreator();
	public abstract Forum getForum();
	public abstract Identity getModifier();
	public abstract Message getParent();
	public abstract Message getThreadtop();
	public abstract String getTitle();
	public abstract Integer getNumOfWords();
	public abstract Integer getNumOfCharacters();
	public abstract void setBody(String string);
	public abstract void setCreator(Identity identity);
	public abstract void setForum(Forum forum);
	public abstract void setModifier(Identity identity);
	public abstract void setParent(Message message);
	public abstract void setThreadtop(Message message);
	public abstract void setTitle(String string);
	public abstract void setNumOfWords(Integer numOfWords);
	public abstract void setNumOfCharacters(Integer numOfCharacters);
	public int getStatusCode();
	public void setStatusCode(int statusCode);
}