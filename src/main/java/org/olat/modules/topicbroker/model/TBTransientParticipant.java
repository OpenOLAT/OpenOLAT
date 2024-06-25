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
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBParticipant;

/**
 * 
 * Initial date: 4 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBTransientParticipant implements TBParticipant {
	
	private Long key;
	private Date creationDate;
	private Date lastModified;
	private Integer boost;
	private Integer maxEnrollments;
	private Identity identity;
	private TBBroker broker;
	
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
	public Integer getBoost() {
		return boost;
	}

	@Override
	public void setBoost(Integer boost) {
		this.boost = boost;
	}

	@Override
	public Integer getRequiredEnrollments() {
		return maxEnrollments;
	}

	@Override
	public void setRequiredEnrollments(Integer maxEnrollments) {
		this.maxEnrollments = maxEnrollments;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public TBBroker getBroker() {
		return broker;
	}

	public void setBroker(TBBroker broker) {
		this.broker = broker;
	}
	
	public static final TBTransientParticipant copyKeyOf(TBParticipant source) {
		TBTransientParticipant copy = new TBTransientParticipant();
		copy.setKey(source.getKey());
		return copy;
	}

}
