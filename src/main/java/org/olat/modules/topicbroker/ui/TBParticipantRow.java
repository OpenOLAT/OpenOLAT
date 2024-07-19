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

import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 7 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBParticipantRow  extends UserPropertiesRow {
	
	private TBBroker broker;
	private TBParticipant participant;
	private Integer boost;
	private int prioritySortOrder;
	private int requiredEnrollments;
	private int numEnrollments;
	private String enrolledString;
	private int waitingList;
	private int maxSelections;
	private List<TBSelection> selections;
	private String detailsComponentName;
	private boolean anonym = false;

	public TBParticipantRow(Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(identity, userPropertyHandlers, locale);
	}

	public TBBroker getBroker() {
		return broker;
	}

	public void setBroker(TBBroker broker) {
		this.broker = broker;
	}

	public TBParticipant getParticipant() {
		return participant;
	}

	public void setParticipant(TBParticipant participant) {
		this.participant = participant;
	}

	public Integer getBoost() {
		return boost;
	}

	public void setBoost(Integer boost) {
		this.boost = boost;
	}

	public int getPrioritySortOrder() {
		return prioritySortOrder;
	}

	public void setPrioritySortOrder(int prioritySortOrder) {
		this.prioritySortOrder = prioritySortOrder;
	}

	public int getRequiredEnrollments() {
		return requiredEnrollments;
	}

	public void setRequiredEnrollments(int requiredEnrollments) {
		this.requiredEnrollments = requiredEnrollments;
	}

	public int getNumEnrollments() {
		return numEnrollments;
	}

	public void setNumEnrollments(int numEnrollments) {
		this.numEnrollments = numEnrollments;
	}

	public String getEnrolledString() {
		return enrolledString;
	}

	public void setEnrolledString(String enrolledString) {
		this.enrolledString = enrolledString;
	}

	public int getWaitingList() {
		return waitingList;
	}

	public void setWaitingList(int waitingList) {
		this.waitingList = waitingList;
	}

	public int getMaxSelections() {
		return maxSelections;
	}

	public void setMaxSelections(int maxSelections) {
		this.maxSelections = maxSelections;
	}

	public int getNumSelections() {
		return selections.size();
	}

	public List<TBSelection> getSelections() {
		return selections;
	}

	public void setSelections(List<TBSelection> selections) {
		this.selections = selections;
	}

	public String getDetailsComponentName() {
		return detailsComponentName;
	}

	public void setDetailsComponentName(String detailsComponentName) {
		this.detailsComponentName = detailsComponentName;
	}

	public boolean isAnonym() {
		return anonym;
	}

	public void setAnonym(boolean anonym) {
		this.anonym = anonym;
	}

}
