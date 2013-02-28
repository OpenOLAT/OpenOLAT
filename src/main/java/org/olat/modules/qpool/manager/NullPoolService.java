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
package org.olat.modules.qpool.manager;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.DB;
import org.olat.ims.qti.QTIConstants;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.StudyField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

/**
 * Create a pool with some questions 
 * Initial date: 21.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("nullPoolDao")
public class NullPoolService implements ApplicationListener<ContextRefreshedEvent> {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PoolDAO poolDao;
	@Autowired
	private StudyFieldDAO studyFieldDao;
	@Autowired
	private QuestionItemDAO questionItemDao;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		int numOfPools = poolDao.getNumOfPools();
		if(numOfPools == 0) {
			//create a pool if there isn't any
			createPools();
			createStudyFields();
			createQuestions();
		}
	}
	
	private void createPools() {
		poolDao.createPool("Catalog");
		dbInstance.commit();
	}
	
	private void createStudyFields() {
		StudyField science = studyFieldDao.createAndPersist(null, "Science");
		StudyField mathematics = studyFieldDao.createAndPersist(science, "Mathematics");
		StudyField topology = studyFieldDao.createAndPersist(mathematics, "Topology");
		studyFieldDao.createAndPersist(topology, "Graph theory");

		StudyField physics = studyFieldDao.createAndPersist(science, "Physics");
		studyFieldDao.createAndPersist(physics, "General relativity");
		StudyField quantum = studyFieldDao.createAndPersist(physics, "Quantum mechanics");
		studyFieldDao.createAndPersist(quantum, "Quantum mechanics");
		studyFieldDao.createAndPersist(quantum, "Quantum chromodynamics");
		studyFieldDao.createAndPersist(quantum, "Standard Model");
		studyFieldDao.createAndPersist(quantum, "Higgs field");
		
		studyFieldDao.createAndPersist(science, "Chemistry");
		dbInstance.commit();
	}
	
	private void createQuestions() {
		List<StudyField> fields = studyFieldDao.loadAllFields();

		int numOfQuestions = questionItemDao.getNumOfQuestions();
		if(numOfQuestions < 3) {
			List<Pool> pools = poolDao.getPools(0, -1);
			for(int i=0; i<200; i++) {
				long randomIndex = Math.round(Math.random() * (fields.size() - 1));
				StudyField field = fields.get((int)randomIndex);
				QuestionItem item = questionItemDao.create(null, "NGC " + i, QTIConstants.QTI_12_FORMAT, Locale.ENGLISH.getLanguage(), field, null, randomType());
				poolDao.addItemToPool(item, pools.get(0));
			}
		}
		dbInstance.commit();
	}
	
	private QuestionType randomType() {
		long t = Math.round(Math.random() * (QuestionType.values().length - 1));
		return QuestionType.values()[(int)t];
	}


}
