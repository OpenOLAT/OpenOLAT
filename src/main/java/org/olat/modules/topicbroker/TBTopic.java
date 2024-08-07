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
package org.olat.modules.topicbroker;

import java.util.Date;
import java.util.Set;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 29 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public interface TBTopic extends TBTopicRef, ModifiedInfo, CreateInfo {
	
	public static final int IDENTIFIER_MAX_LENGTH = 64;
	public static final int TITLE_MAX_LENGTH = 200;
	
	String getIdentifier();
	
	void setIdentifier(String identifier);
	
	String getTitle();

	void setTitle(String title);

	String getDescription();

	void setDescription(String description);

	Integer getMinParticipants();
	
	void setMinParticipants(Integer minParticipants);
	
	Integer getMaxParticipants();
	
	void setMaxParticipants(Integer maxParticipants);
	
	Set<Long> getGroupRestrictionKeys();
	
	void setGroupRestrictionKeys(Set<Long> groupRestrictionKeys);
	
	int getSortOrder();
	
	Date getDeletedDate();
	
	Identity getDeletedBy();
	
	Identity getCreator();
	
	TBBroker getBroker();

}
