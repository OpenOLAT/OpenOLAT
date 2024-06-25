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
package org.olat.modules.topicbroker.model;

import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBTopic;

/**
 * 
 * Initial date: 4 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBTransientSelection implements TBSelection {
	
	private Long key;
	private Date creationDate;
	private Date lastModified;
	private boolean enrolled;
	private int sortOrder;
	private Identity creator;
	private TBParticipant participant;
	private TBTopic topic;
	
	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public boolean isEnrolled() {
		return enrolled;
	}

	public void setEnrolled(boolean enrolled) {
		this.enrolled = enrolled;
	}

	@Override
	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	@Override
	public Identity getCreator() {
		return creator;
	}

	public void setCreator(Identity creator) {
		this.creator = creator;
	}

	@Override
	public TBParticipant getParticipant() {
		return participant;
	}

	public void setParticipant(TBParticipant participant) {
		this.participant = participant;
	}

	@Override
	public TBTopic getTopic() {
		return topic;
	}

	public void setTopic(TBTopic topic) {
		this.topic = topic;
	}
	
	public static final TBTransientSelection copyValuesOf(TBSelection source) {
		TBTransientSelection copy = new TBTransientSelection();
		copy.setKey(source.getKey());
		copy.setCreationDate(source.getCreationDate());
		copy.setLastModified(source.getLastModified());
		copy.setEnrolled(source.isEnrolled());
		copy.setSortOrder(source.getSortOrder());
		return copy;
	}

}
