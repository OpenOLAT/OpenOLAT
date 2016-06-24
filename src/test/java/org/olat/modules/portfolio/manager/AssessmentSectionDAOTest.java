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
package org.olat.modules.portfolio.manager;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.portfolio.AssessmentSection;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentSectionDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private AssessmentSectionDAO assessmentSectionDao;
	
	@Test
	public void createAssessmentSection() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("aowner-1");
		//create a binder with a section
		Binder binder = portfolioService.createNewBinder("ABinder", "Assessment on binder", null, owner);
		dbInstance.commit();
		portfolioService.appendNewSection("Section", "Coached section", null, null, binder);
		dbInstance.commit();
		List<Section> sections = portfolioService.getSections(binder);
		
		// create the assessment point
		Boolean passed = Boolean.TRUE;
		Section section = sections.get(0);
		BigDecimal score = new BigDecimal("3.5");
		AssessmentSection assessmentSection = assessmentSectionDao.createAssessmentSection(score, passed, section, owner);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(assessmentSection);
		Assert.assertNotNull(assessmentSection.getKey());
		Assert.assertNotNull(assessmentSection.getCreationDate());
		Assert.assertNotNull(assessmentSection.getLastModified());
		Assert.assertEquals(section, assessmentSection.getSection());
		Assert.assertEquals(passed, assessmentSection.getPassed());
		Assert.assertEquals(score, assessmentSection.getScore());

		// reload the assessment point
		AssessmentSection reloadedAssessmentSection = assessmentSectionDao.loadByKey(assessmentSection.getKey());
		Assert.assertNotNull(reloadedAssessmentSection);
		Assert.assertEquals(assessmentSection, reloadedAssessmentSection);
	}

}
