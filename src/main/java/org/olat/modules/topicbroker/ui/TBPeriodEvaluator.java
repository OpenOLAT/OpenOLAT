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

import org.olat.modules.topicbroker.TBBroker;

/**
 * 
 * Initial date: 14 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBPeriodEvaluator {
	
	private TBBroker broker;
	private final boolean canWithdraw;
	private boolean beforeSelectionPeriod;
	private final boolean selectionPeriodInit;
	private boolean selectionPeriod;
	private final boolean withdrawPeriodInit;
	private boolean withdrawPeriod;
	
	
	public TBPeriodEvaluator(TBBroker broker) {
		this.broker = broker;
		this.canWithdraw = broker.isParticipantCanWithdraw();
		beforeSelectionPeriod = evaluateBeforeSelectionPeriod();
		selectionPeriodInit = evaluateSelectionPeriod();
		selectionPeriod = selectionPeriodInit;
		withdrawPeriodInit = evaluateWithdrawPeriod();
		withdrawPeriod = withdrawPeriodInit;
	}

	public void setBroker(TBBroker broker) {
		this.broker = broker;
	}
	
	public void refresh() {
		beforeSelectionPeriod = evaluateBeforeSelectionPeriod();
		selectionPeriod = evaluateSelectionPeriod();
		withdrawPeriod = evaluateWithdrawPeriod();
	}

	public boolean isPeriodChanged() {
		return selectionPeriodInit != selectionPeriod
				|| withdrawPeriodInit != withdrawPeriod;
	}
	
	public boolean isBeforeSelectionPeriod() {
		return beforeSelectionPeriod;
	}
	
	private boolean evaluateBeforeSelectionPeriod() {
		if (broker.getEnrollmentStartDate() != null || broker.getEnrollmentDoneDate() != null) {
			return false;
		}
		
		if (broker.getSelectionStartDate() == null || broker.getSelectionEndDate() == null) {
			return false;
		}
		
		Date now = new Date();
		if (now.before(broker.getSelectionStartDate())) {
			return true;
		}
		
		return false;
	}

	public boolean isSelectionPeriod() {
		return selectionPeriod;
	}
	
	private boolean evaluateSelectionPeriod() {
		if (broker.getEnrollmentStartDate() != null || broker.getEnrollmentDoneDate() != null) {
			return false;
		}
		
		if (broker.getSelectionStartDate() == null || broker.getSelectionEndDate() == null) {
			return false;
		}
		
		Date now = new Date();
		if (now.after(broker.getSelectionStartDate()) && now.before(broker.getSelectionEndDate())) {
			return true;
		}
		
		return false;
	}
	
	public boolean isWithdrawPeriod() {
		return withdrawPeriod;
	}
	
	private boolean evaluateWithdrawPeriod() {
		if (selectionPeriod) {
			return true;
		}
		if (!canWithdraw) {
			return false;
		}
		if (broker.getWithdrawEndDate() == null) {
			return true;
		}
		
		Date now = new Date();
		if (broker.getWithdrawEndDate().after(now)) {
			return true;
		}
		
		return false;
	}

}
