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
package org.olat.modules.quality;

import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 08.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface QualityManager {

	public QualityDataCollection createDataCollection(RepositoryEntry formEntry);
	
	public QualityDataCollection updateDataCollection(QualityDataCollection dataCollection);

	public QualityDataCollection loadDataCollectionByKey(QualityDataCollectionRef dataCollectionRef);

	public int getDataCollectionCount();

	public List<QualityDataCollectionView> loadDataCollections(Translator translator, int firstResult, int maxResults,
			SortKey... orderBy);

	/**
	 * Deletes a data collection and the whole survey.
	 *
	 * @param dataCollection
	 */
	public void deleteDataCollection(QualityDataCollectionLight dataCollection);
	
	public RepositoryEntry loadFormEntry(QualityDataCollectionLight dataCollection);

	public boolean isFormEntryUpdateable(QualityDataCollection dataCollection);

	public void updateFormEntry(QualityDataCollection dataCollection, RepositoryEntry formEntry);

	public void addParticipants(QualityDataCollectionLight dataCollection, List<Identity> executors);
	
	public int getParticipationCount(QualityDataCollectionLight dataCollection);

	public List<QualityParticipation> loadParticipations(QualityDataCollectionLight dataCollection,
			int firstResult, int maxResults, SortKey... orderBy);

	public int getExecutorParticipationCount(IdentityRef executor);

	public List<QualityExecutorParticipation> loadExecutorParticipations(IdentityRef executor, int firstResult,
			int maxResults, SortKey[] orderBy);

}
