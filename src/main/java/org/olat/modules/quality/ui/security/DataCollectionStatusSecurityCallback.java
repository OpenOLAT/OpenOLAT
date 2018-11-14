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
package org.olat.modules.quality.ui.security;

import static org.olat.modules.quality.QualityDataCollectionStatus.FINISHED;
import static org.olat.modules.quality.QualityDataCollectionStatus.PREPARATION;
import static org.olat.modules.quality.QualityDataCollectionStatus.READY;

import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityReminder;

/**
 * 
 * Initial date: 14 Nov 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class DataCollectionStatusSecurityCallback implements DataCollectionSecurityCallback {

	private final QualityDataCollectionStatus status;

	public DataCollectionStatusSecurityCallback(QualityDataCollectionStatus status) {
		this.status = status;
	}

	@Override
	public boolean canViewDataCollectionConfigurations() {
		return true;
	}
	
	@Override
	public boolean canUpdateBaseConfiguration() {
		return PREPARATION.equals(status);
	}

	@Override
	public boolean canSetPreparation() {
		return READY.equals(status);
	}

	@Override
	public boolean canSetReady() {
		return PREPARATION.equals(status);
	}

	@Override
	public boolean canSetRunning() {
		return isNotRunning();
	}

	@Override
	public boolean canSetFinished() {
		return isNotFinished();
	}

	@Override
	public boolean canDeleteDataCollection() {
		return isNotRunning();
	}

	@Override
	public boolean canAddParticipants() {
		return isNotFinished();
	}

	@Override
	public boolean canRevomeParticipation() {
		return isNotRunning();
	}

	@Override
	public boolean canEditReminder(QualityReminder reminder) {
		return isNotFinished() && isNotSent(reminder);
	}

	private boolean isNotSent(QualityReminder reminder) {
		return reminder == null || !reminder.isSent();
	}

	@Override
	public boolean canEditReportAccessOnline() {
		return true;
	}
	
	@Override
	public boolean canEditReportAccessEmail() {
		return isNotFinished();
	}

	@Override
	public boolean canEditReportAccessMembers() {
		return true;
	}

	@Override
	public boolean canViewReport() {
		return isRunning();
	}

	private boolean isRunning() {
		return !isNotRunning();
	}
	
	private boolean isNotRunning() {
		return PREPARATION.equals(status) || READY.equals(status);
	}

	private boolean isNotFinished() {
		return !FINISHED.equals(status);
	}

}
