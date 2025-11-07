/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.creditpoint.manager;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.creditpoint.CreditPointExpirationType;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CurriculumElementCreditPointConfiguration;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.manager.CurriculumDAO;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementCreditPointConfigurationDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumDAO curriculumDao;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private CreditPointSystemDAO creditPointSystemDao;
	@Autowired
	private CurriculumElementCreditPointConfigurationDAO curriculumElementCreditPointConfigurationDao;
	
	@Test
	public void createConfiguration() {
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem("config-coin-el-1", "CCEL1", Integer.valueOf(180), CreditPointExpirationType.DAY, false, false);

		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-1", "Curriculum for points", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-points-1", "1. Element with points",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Assert.assertNotNull(element);
		dbInstance.commitAndCloseSession();
		
		dbInstance.commitAndCloseSession();
		
		CurriculumElementCreditPointConfiguration config = curriculumElementCreditPointConfigurationDao.createConfiguration(element, cpSystem);
		dbInstance.commit();
		
		Assert.assertNotNull(config);
		Assert.assertEquals(cpSystem, config.getCreditPointSystem());
		Assert.assertEquals(element, config.getCurriculumElement());
	}
}
