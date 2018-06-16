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
package org.olat.modules.quality.manager;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityDataCollectionParticipation;
import org.olat.modules.quality.QualityDataCollectionRef;
import org.olat.modules.quality.QualityDataCollectionView;
import org.olat.modules.quality.QualityManager;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 08.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QualityManagerImpl implements QualityManager {

	@Autowired
	private QualityDataCollectionDAO dataCollectionDao;
	@Autowired
	private QualityParticipationDAO participationDao;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	@Override
	public QualityDataCollection createDataCollection(RepositoryEntry formEntry) {
		QualityDataCollection dataCollection = dataCollectionDao.createDataCollection();
		evaluationFormManager.createSurvey(dataCollection, null, formEntry);
		return dataCollection;
	}

	@Override
	public QualityDataCollection updateDataCollection(QualityDataCollection dataCollection) {
		return dataCollectionDao.updateDataCollection(dataCollection);
	}

	@Override
	public QualityDataCollection loadDataCollectionByKey(QualityDataCollectionRef dataCollectionRef) {
		return dataCollectionDao.loadDataCollectionByKey(dataCollectionRef);
	}

	@Override
	public int getDataCollectionCount() {
		return dataCollectionDao.getDataCollectionCount();
	}

	@Override
	public List<QualityDataCollectionView> loadDataCollections(Translator translator, int firstResult, int maxResults,
			SortKey... orderBy) {
		return dataCollectionDao.loadDataCollections(translator, firstResult, maxResults, orderBy);
	}

	@Override
	public void deleteDataCollection(QualityDataCollectionLight dataCollection) {
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(dataCollection, null);
		evaluationFormManager.deleteSurvey(survey);
		dataCollectionDao.deleteDataCollection(dataCollection);
	}

	@Override
	public RepositoryEntry loadFormEntry(QualityDataCollectionLight dataCollection) {
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(dataCollection, null);
		return survey.getFormEntry() != null? survey.getFormEntry(): null;
	}

	@Override
	public boolean isFormEntryUpdateable(QualityDataCollection dataCollection) {
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(dataCollection, null);
		return evaluationFormManager.isFormUpdateable(survey);
	}

	@Override
	public void updateFormEntry(QualityDataCollection dataCollection, RepositoryEntry formEntry) {
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(dataCollection, null);
		evaluationFormManager.updateSurveyForm(survey, formEntry);
	}

	@Override
	public int getParticipationCount(QualityDataCollectionLight dataCollection) {
		return participationDao.getParticipationCount(dataCollection);
	}

	@Override
	public List<QualityDataCollectionParticipation> loadParticipations(QualityDataCollectionLight dataCollection,
			int firstResult, int maxResults, SortKey... orderBy) {
		return participationDao.loadParticipations(dataCollection, firstResult, maxResults, orderBy);
	}

	@Override
	public void addParticipants(QualityDataCollectionLight dataCollection, List<Identity> executors) {
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(dataCollection, null);
		for (Identity executor: executors) {
			EvaluationFormParticipationRef participation = evaluationFormManager.loadParticipationByExecutor(survey, executor);
			if (participation == null) {
				evaluationFormManager.createParticipation(survey, executor, true);
			}
		}
	}
}
