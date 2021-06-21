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
package org.olat.modules.taxonomy.manager;

import java.util.Date;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceAuditLog;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.model.TaxonomyCompetenceAuditLogImpl;
import org.olat.modules.taxonomy.model.TaxonomyCompetenceImpl;
import org.olat.modules.taxonomy.model.TaxonomyLevelImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Initial date: 30 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TaxonomyCompetenceAuditLogDAO {
	
	private static final XStream competenceXStream = XStreamHelper.createXStreamInstanceForDBObjects();
	static {
		XStreamHelper.allowDefaultPackage(competenceXStream);
		competenceXStream.alias("competence", TaxonomyCompetenceImpl.class);
		competenceXStream.alias("taxonomyLevel", TaxonomyLevelImpl.class);
		competenceXStream.ignoreUnknownElements();
		competenceXStream.omitField(TaxonomyCompetenceImpl.class, "identity");
		competenceXStream.omitField(TaxonomyCompetenceImpl.class, "lastModified");
		competenceXStream.omitField(TaxonomyCompetenceImpl.class, "taxonomy");
		competenceXStream.omitField(TaxonomyLevelImpl.class, "taxonomy");
	}
	
	@Autowired
	private DB dbInstance;
	
	public void auditLog(TaxonomyCompetenceAuditLog.Action action, String before, String after, String message,
			TaxonomyRef taxonomy, TaxonomyCompetence competence,
			IdentityRef assessedIdentity, IdentityRef author) {
		TaxonomyCompetenceAuditLogImpl auditLog = new TaxonomyCompetenceAuditLogImpl();
		auditLog.setCreationDate(new Date());
		auditLog.setAction(action.name());
		auditLog.setBefore(before);
		auditLog.setAfter(after);
		auditLog.setMessage(message);
		if(taxonomy != null) {
			auditLog.setTaxonomyKey(taxonomy.getKey());
		}
		if(competence != null) {
			auditLog.setTaxonomyCompetenceKey(competence.getKey());
		}
		if(assessedIdentity != null) {
			auditLog.setIdentityKey(assessedIdentity.getKey());
		}
		if(author != null) {
			auditLog.setAuthorKey(author.getKey());
		}
		dbInstance.getCurrentEntityManager().persist(auditLog);
	}
	
	public String toXml(TaxonomyCompetence competence) {
		if(competence == null) return null;
		return competenceXStream.toXML(competence);
	}
}
