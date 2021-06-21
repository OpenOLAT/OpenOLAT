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

import org.olat.core.commons.persistence.DB;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemAuditLog;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QLicense;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.taxonomy.model.TaxonomyLevelImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Initial date: 21.01.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QuestionItemAuditLogDAO {
	
	private static final Logger log = Tracing.createLoggerFor(QuestionItemAuditLogDAO.class);
	
	private static final XStream qitemXStream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		XStreamHelper.allowDefaultPackage(qitemXStream);
		qitemXStream.alias("questionItem", QuestionItemImpl.class);
		qitemXStream.alias("taxonomyLevel", TaxonomyLevelImpl.class);
		qitemXStream.alias("educationalContext", QEducationalContext.class);
		qitemXStream.alias("type", QItemType.class);
		qitemXStream.alias("license", QLicense.class);
		qitemXStream.ignoreUnknownElements();
		qitemXStream.omitField(QuestionItemImpl.class, "creationDate");
		qitemXStream.omitField(QuestionItemImpl.class, "lastModified");
		qitemXStream.omitField(QuestionItemImpl.class, "ownerGroup");
		qitemXStream.omitField(TaxonomyLevelImpl.class, "creationDate");
		qitemXStream.omitField(TaxonomyLevelImpl.class, "lastModified");
		qitemXStream.omitField(TaxonomyLevelImpl.class, "taxonomy");
		qitemXStream.omitField(TaxonomyLevelImpl.class, "parent");
		qitemXStream.omitField(TaxonomyLevelImpl.class, "type");
		qitemXStream.omitField(QEducationalContext.class, "creationDate");
		qitemXStream.omitField(QEducationalContext.class, "lastModified");
		qitemXStream.omitField(QItemType.class, "creationDate");
		qitemXStream.omitField(QItemType.class, "lastModified");
		qitemXStream.omitField(QLicense.class, "creationDate");
		qitemXStream.omitField(QLicense.class, "lastModified");
	}
	
	@Autowired
	private DB dbInstance;
	
	public void persist(QuestionItemAuditLog auditLog) {
		dbInstance.getCurrentEntityManager().persist(auditLog);
	}

	public List<QuestionItemAuditLog> getAuditLogByQuestionItem(QuestionItemShort item) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select log from qitemauditlog log where log.questionItemKey=:questionItemKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QuestionItemAuditLog.class)
				.setParameter("questionItemKey", item.getKey())
				.getResultList();
	}
	
	public String toXml(QuestionItem item) {
		if(item == null) return null;
		return qitemXStream.toXML(item);
	}
	
	public QuestionItem questionItemFromXml(String xml) {
		QuestionItem item = null;
		if(StringHelper.containsNonWhitespace(xml)) {
			try {
				Object obj = qitemXStream.fromXML(xml);
				if(obj instanceof QuestionItem) {
					item = (QuestionItem) obj;
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return item;
	}

}
