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

import org.olat.modules.quality.QualityReminder;

/**
 * 
 * Initial date: 14 Nov 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class DataCollectionReadOnlySecurityCallback implements DataCollectionSecurityCallback {

	@Override
	public boolean canViewDataCollectionConfigurations() {
		return true;
	}

	@Override
	public boolean canUpdateBaseConfiguration() {
		return false;
	}

	@Override
	public boolean canSetPreparation() {
		return false;
	}

	@Override
	public boolean canSetReady() {
		return false;
	}

	@Override
	public boolean canSetRunning() {
		return false;
	}

	@Override
	public boolean canSetFinished() {
		return false;
	}

	@Override
	public boolean canDeleteDataCollection() {
		return false;
	}

	@Override
	public boolean canAddParticipants() {
		return false;
	}

	@Override
	public boolean canRevomeParticipation() {
		return false;
	}

	@Override
	public boolean canEditReminder(QualityReminder reminder) {
		return false;
	}

	@Override
	public boolean canEditReportAccessOnline() {
		return false;
	}

	@Override
	public boolean canEditReportAccessEmail() {
		return false;
	}

	@Override
	public boolean canEditReportAccessMembers() {
		return false;
	}

	@Override
	public boolean canViewReport() {
		return true;
	}

}
