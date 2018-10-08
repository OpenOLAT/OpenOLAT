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
package org.olat.modules.quality.analysis.model;

import java.util.Date;

import org.olat.modules.quality.analysis.EvaluationFormView;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 03.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormViewImpl implements EvaluationFormView {

	private final RepositoryEntry formEntry;
	private final Long numberDataCollections;
	private final Date soonestDataCollectionDate;
	private final Date latestDataCollectionFinishedDate;
	private final Long numberParticipationsDone;

	public EvaluationFormViewImpl(RepositoryEntry formEntry, Long numberDataCollections, Date soonestDataCollectionDate,
			Date latestDataCollectionFinishedDate, Long numberParticipationsDone) {
		this.formEntry = formEntry;
		this.numberDataCollections = numberDataCollections;
		this.soonestDataCollectionDate = soonestDataCollectionDate;
		this.latestDataCollectionFinishedDate = latestDataCollectionFinishedDate;
		this.numberParticipationsDone = numberParticipationsDone;
	}

	@Override
	public RepositoryEntry getFormEntry() {
		return formEntry;
	}

	@Override
	public Long getNumberDataCollections() {
		return numberDataCollections;
	}

	@Override
	public Date getSoonestDataCollectionDate() {
		return soonestDataCollectionDate;
	}

	@Override
	public Date getLatestDataCollectionFinishedDate() {
		return latestDataCollectionFinishedDate;
	}

	@Override
	public Long getNumberParticipationsDone() {
		return numberParticipationsDone;
	}
	
}
