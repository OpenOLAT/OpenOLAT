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

/**
 * 
 * Initial date: 03.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormViewImpl implements EvaluationFormView {

	private final Long formEntryKey;
	private final Date formCreatedDate;
	private final String formTitle;
	private final Long numberDataCollections;
	private final Date soonestDataCollectionDate;
	private final Date latestDataCollectionDate;
	private final Long numberParticipationsDone;
	
	public EvaluationFormViewImpl(Long formEntryKey, Date formCreatedDate, String formTitle, Long numberDataCollections,
			Date soonestDataCollectionDate, Date latestDataCollectionDate, Long numberParticipationsDone) {
		this.formEntryKey = formEntryKey;
		this.formCreatedDate = formCreatedDate;
		this.formTitle = formTitle;
		this.numberDataCollections = numberDataCollections;
		this.soonestDataCollectionDate = soonestDataCollectionDate;
		this.latestDataCollectionDate = latestDataCollectionDate;
		this.numberParticipationsDone = numberParticipationsDone;
	}

	@Override
	public String getResourceableTypeName() {
		return EvaluationFormView.RESOURCEABLE_TYPE;
	}

	@Override
	public Long getResourceableId() {
		return formEntryKey;
	}

	@Override
	public Long getFormEntryKey() {
		return formEntryKey;
	}

	@Override
	public Date getFormCreatedDate() {
		return formCreatedDate;
	}

	@Override
	public String getFormTitle() {
		return formTitle;
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
	public Date getLatestDataCollectionDate() {
		return latestDataCollectionDate;
	}

	@Override
	public Long getNumberParticipationsDone() {
		return numberParticipationsDone;
	}
	
}
