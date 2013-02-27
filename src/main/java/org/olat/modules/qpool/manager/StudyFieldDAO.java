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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.qpool.StudyField;
import org.olat.modules.qpool.model.StudyFieldImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.emory.mathcs.backport.java.util.Collections;

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
	
	public StudyField update(String name, StudyField field) {
		StudyField reloadedField = loadStudyFieldById(field.getKey());
		((StudyFieldImpl)reloadedField).setField(name);
		return dbInstance.getCurrentEntityManager().merge(reloadedField);
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
	
	public List<StudyField> loadAllFields() {
		StringBuilder sb = new StringBuilder();
		sb.append("select f from qstudyfield f ");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), StudyField.class)
				.getResultList();
	}
	
	public List<StudyField> loadFields(StudyField parent) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadStudyFieldsByparent", StudyField.class)
				.setParameter("parentKey", parent.getKey())
				.getResultList();
	}

	public String getMaterializedPath(StudyField field) {
		if(field == null) {
			return "";
		}
		
		List<StudyField> parentLine = new ArrayList<StudyField>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select f from qstudyfield f ")
		  .append(" left join fetch f.parentField pf ")
		  .append(" left join fetch pf.parentField ppf ")
		  .append(" left join fetch ppf.parentField pppf ")
		  .append(" where f.key=:key");

		StudyFieldImpl fetchedField = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), StudyFieldImpl.class)
				.setParameter("key", field.getKey())
				.getSingleResult();
		
		parentLine.add(fetchedField);
		if(fetchedField.getParentField() != null) {
			StudyFieldImpl pf = (StudyFieldImpl)fetchedField.getParentField();
			parentLine.add(pf);
			if(pf.getParentField() != null) {
				StudyFieldImpl ppf = (StudyFieldImpl)pf.getParentField();
				parentLine.add(ppf);
				if(ppf.getParentField() != null) {
					StudyFieldImpl pppf = (StudyFieldImpl)ppf.getParentField();
					parentLine.add(pppf);
				}
			}
		}
		
		Collections.reverse(parentLine);
		StringBuilder path = new StringBuilder();
		for(StudyField f:parentLine) {
			path.append("/").append(f.getField());
		}
		return path.toString();
	}
}
