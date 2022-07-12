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

import static org.olat.test.JunitTestHelper.random;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceAuditLog;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyCompetenceAuditLogDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private TaxonomyCompetenceDAO taxonomyCompetenceDao;
	@Autowired
	private TaxonomyCompetenceAuditLogDAO taxonomyCompetenceAuditLogDao;
	
	@Test
	public void auditLog() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-1");
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-24", "Competence", "", null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-1", random(), "Competence level", "A very difficult competence", null, null, null, null, taxonomy);
		TaxonomyCompetence competence = taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, level, id, null);
		dbInstance.commit();
		
		String after = taxonomyCompetenceAuditLogDao.toXml(competence);
		taxonomyCompetenceAuditLogDao.auditLog(TaxonomyCompetenceAuditLog.Action.addCompetence,
				"Before", "After", "Message", taxonomy, competence, id, null);
		dbInstance.commit();
		Assert.assertNotNull(after);
	}
}
