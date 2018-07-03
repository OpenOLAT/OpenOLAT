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

import java.util.Collection;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.forms.EvaluationFormParticipation;
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
	 * Deletes a data collection, the whole survey and all contexts.
	 *
	 * @param dataCollection
	 */
	public void deleteDataCollection(QualityDataCollectionLight dataCollection);
	
	public RepositoryEntry loadFormEntry(QualityDataCollectionLight dataCollection);

	public boolean isFormEntryUpdateable(QualityDataCollection dataCollection);

	public void updateFormEntry(QualityDataCollection dataCollection, RepositoryEntry formEntry);

	/**
	 * Add the executors to the data collection and returns the participations of
	 * the executors. If already a participation for an executor exists, no further
	 * participation is created, but the existing participation is used and
	 * returned.
	 *
	 * @param dataCollection
	 * @param executors
	 * @return
	 */
	public List<EvaluationFormParticipation> addParticipations(QualityDataCollectionLight dataCollection,
			Collection<Identity> executors);

	public int getParticipationCount(QualityDataCollectionLight dataCollection);

	public List<QualityParticipation> loadParticipations(QualityDataCollectionLight dataCollection,
			int firstResult, int maxResults, SortKey... orderBy);

	public int getExecutorParticipationCount(IdentityRef executor);

	public List<QualityExecutorParticipation> loadExecutorParticipations(IdentityRef executor, int firstResult,
			int maxResults, SortKey[] orderBy);

	public QualityContextBuilder createContextBuilder(QualityDataCollection dataCollection,
			EvaluationFormParticipation participation);
	
	/**
	 * Create a QualityContextBuilder and populate it with the data according to the
	 * participation and the repository entry.
	 *
	 * @param dataCollection
	 * @param participation
	 * @param entry
	 * @param role 
	 * @return
	 */
	public QualityContextBuilder createContextBuilder(QualityDataCollection dataCollection,
			EvaluationFormParticipation participation, RepositoryEntry entry, GroupRoles role);
	
	/**
	 * Create a QualityContextBuilder and populate it with the data according to the
	 * participation and the curriculum element.
	 *
	 * @param dataCollection
	 * @param participation
	 * @param curriculumElement
	 * @param role
	 * @return
	 */
	public QualityContextBuilder createContextBuilder(QualityDataCollection dataCollection,
			EvaluationFormParticipation participation, CurriculumElement curriculumElement, CurriculumRoles role);

	public void deleteContext(QualityContextRef contextRef);

	/**
	 * Deletes the contexts of the specified references. If a deleted context was
	 * the last one of a participation, the participation is deleted as well.
	 *
	 * @param contetxtRefs
	 */
	public void deleteContextsAndParticipations(Collection<QualityContextRef> contextRefs);

}
