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

import java.util.Date;

import org.olat.modules.quality.QualityDataCollectionLight;
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
	public boolean canUpdateStart(QualityDataCollectionLight dataCollection) {
		return !isStarted(dataCollection);
	}

	@Override
	public boolean canUpdateDeadline(QualityDataCollectionLight dataCollection) {
		Date deadline = dataCollection.getDeadline();
		return deadline == null || deadline.after(new Date());
	}

	@Override
	public boolean canDeleteDataCollections() {
		return true;
	}

	@Override
	public boolean canDeleteDataCollection(QualityDataCollectionLight dataCollection) {
		return !isStarted(dataCollection);
	}

	@Override
	public boolean canRevomeParticipation(QualityDataCollectionLight dataCollection) {
		return !isStarted(dataCollection);
	}
	
	private boolean isStarted(QualityDataCollectionLight dataCollection) {
		Date now = new Date();
		return dataCollection.getStart() != null && dataCollection.getStart().before(now);
	}

}
