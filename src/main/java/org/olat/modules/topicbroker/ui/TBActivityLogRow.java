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
package org.olat.modules.topicbroker.ui;

import java.util.Date;

/**
 * 
 * Initial date: Aug 5, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBActivityLogRow {
	
	private Date date;
	private TBActivityLogContext context;
	private String object;
	private String translatedActivity;
	private String valueOriginal;
	private String valueNew;
	private String doerDisplayName;
	private Long participantKey;
	private Long topicKey;
	private Long identityKey;
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public TBActivityLogContext getContext() {
		return context;
	}

	public void setContext(TBActivityLogContext context) {
		this.context = context;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public String getTranslatedActivity() {
		return translatedActivity;
	}

	public void setTranslatedActivity(String translatedActivity) {
		this.translatedActivity = translatedActivity;
	}

	public String getValueOriginal() {
		return valueOriginal;
	}

	public void setValueOriginal(String valueOriginal) {
		this.valueOriginal = valueOriginal;
	}

	public String getValueNew() {
		return valueNew;
	}

	public void setValueNew(String valueNew) {
		this.valueNew = valueNew;
	}

	public String getDoerDisplayName() {
		return doerDisplayName;
	}
	
	public void setDoerDisplayName(String doerDisplayName) {
		this.doerDisplayName = doerDisplayName;
	}

	public Long getParticipantKey() {
		return participantKey;
	}

	public void setParticipantKey(Long participantKey) {
		this.participantKey = participantKey;
	}

	public Long getTopicKey() {
		return topicKey;
	}

	public void setTopicKey(Long topicKey) {
		this.topicKey = topicKey;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}
	
}
