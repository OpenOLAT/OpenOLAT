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

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.qpool.StudyField;
import org.olat.modules.qpool.model.StudyFieldImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("studyFieldDao")
public class StudyFieldDAO {

	@Autowired
	private DB dbInstance;
	
	public StudyField createAndPersist(StudyField parentField, String field) {
		StudyFieldImpl newStudyField = new StudyFieldImpl();
		newStudyField.setCreationDate(new Date());
		newStudyField.setLastModified(new Date());
		newStudyField.setField(field);
		newStudyField.setParentField(parentField);
		dbInstance.getCurrentEntityManager().persist(newStudyField);
		return newStudyField;
	}
	
	public StudyField update(StudyField studyField) {
		return dbInstance.getCurrentEntityManager().merge(studyField);
	}
	
	public StudyField loadStudyFieldById(Long key) {
		List<StudyField> fields = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadStudyFieldByKey", StudyField.class)
				.setParameter("key", key)
				.getResultList();
		
		if(fields.isEmpty()) {
			return null;
		}
		return fields.get(0);
	}

}
