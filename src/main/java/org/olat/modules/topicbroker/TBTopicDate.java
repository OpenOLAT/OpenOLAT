/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.topicbroker;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 4 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public interface TBTopicDate extends TBTopicDateRef, ModifiedInfo, CreateInfo {
	
	public static final int LOCATION_MAX_LENGTH = 255;
	public static final int DESCRIPTION_MAX_LENGTH = 1000;
	
	Date getStartDate();
	
	void setStartDate(Date startDate);
	
	Date getEndDate();
	
	void setEndDate(Date endDate);
	
	String getLocation();
	
	void setLocation(String location);
	
	String getDescription();
	
	void setDescription(String description);
	
	TBTopic getTopic();
	
}
