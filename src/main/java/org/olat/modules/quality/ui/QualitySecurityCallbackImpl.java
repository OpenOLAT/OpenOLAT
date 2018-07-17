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
package org.olat.modules.quality.ui;

import static org.olat.modules.quality.QualityDataCollectionStatus.FINISHED;
import static org.olat.modules.quality.QualityDataCollectionStatus.PREPARATION;
import static org.olat.modules.quality.QualityDataCollectionStatus.READY;

import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityExecutorParticipation;
import org.olat.modules.quality.QualityExecutorParticipationStatus;
import org.olat.modules.quality.QualityReminder;
import org.olat.modules.quality.QualitySecurityCallback;

/**
 * 
 * Initial date: 08.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualitySecurityCallbackImpl implements QualitySecurityCallback {

	@Override
	public boolean canViewDataCollections() {
		return true;
	}

	@Override
	public boolean canEditDataCollections() {
		return true;
	}

	@Override
	public boolean canUpdateBaseConfiguration(QualityDataCollectionLight dataCollection) {
		return PREPARATION.equals(dataCollection.getStatus());
	}

	@Override
	public boolean canSetPreparation(QualityDataCollectionLight dataCollection) {
		return READY.equals(dataCollection.getStatus());
	}

	@Override
	public boolean canSetReady(QualityDataCollectionLight dataCollection) {
		return PREPARATION.equals(dataCollection.getStatus());
	}

	@Override
	public boolean canSetRunning(QualityDataCollectionLight dataCollection) {
		return isNotStarted(dataCollection);
	}

	@Override
	public boolean canSetFinished(QualityDataCollectionLight dataCollection) {
		return isNotFinished(dataCollection);
	}

	@Override
	public boolean canAddParticipants(QualityDataCollectionLight dataCollection) {
		return isNotFinished(dataCollection);
	}

	@Override
	public boolean canEditReminders() {
		return true;
	}

	@Override
	public boolean canEditReminder(QualityDataCollectionLight dataCollection, QualityReminder reminder) {
		return canEditReminders() && isNotSent(reminder) && isNotFinished(dataCollection);
	}

	private boolean isNotSent(QualityReminder reminder) {
		return reminder == null || !reminder.isSent();
	}

	@Override
	public boolean canDeleteDataCollections() {
		return true;
	}

	@Override
	public boolean canDeleteDataCollection(QualityDataCollectionLight dataCollection) {
		return isNotStarted(dataCollection);
	}

	@Override
	public boolean canRevomeParticipation(QualityDataCollectionLight dataCollection) {
		return isNotStarted(dataCollection);
	}

	@Override
	public boolean canExecute(QualityExecutorParticipation participation) {
		QualityExecutorParticipationStatus status = participation.getExecutionStatus();
		return QualityExecutorParticipationStatus.READY.equals(status)
				|| QualityExecutorParticipationStatus.PARTICIPATING.equals(status);
	}

	private boolean isNotStarted(QualityDataCollectionLight dataCollection) {
		QualityDataCollectionStatus status = dataCollection.getStatus();
		return PREPARATION.equals(status) || READY.equals(status);
	}

	private boolean isNotFinished(QualityDataCollectionLight dataCollection) {
		return !FINISHED.equals(dataCollection.getStatus());
	}
	
}
