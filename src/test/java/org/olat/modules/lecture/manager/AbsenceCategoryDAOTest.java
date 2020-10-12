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
package org.olat.modules.lecture.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.lecture.AbsenceCategory;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsenceCategoryDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AbsenceCategoryDAO absenceCategoryDao;
	
	@Test
	public void createAbsenceCategory() {
		String title = UUID.randomUUID().toString();
		String description = "Long absence";
		AbsenceCategory absenceCategory = absenceCategoryDao.createAbsenceCategory(title, description, true);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(absenceCategory);
		Assert.assertNotNull(absenceCategory.getKey());
		Assert.assertNotNull(absenceCategory.getCreationDate());
		Assert.assertNotNull(absenceCategory.getLastModified());
		Assert.assertTrue(absenceCategory.isEnabled());
		Assert.assertEquals(title, absenceCategory.getTitle());
		Assert.assertEquals(description, absenceCategory.getDescription());
	}
	
	@Test
	public void updateAbsenceCategory() {
		String title = UUID.randomUUID().toString();
		String description = "Long absence";
		AbsenceCategory absenceCategory = absenceCategoryDao.createAbsenceCategory(title, description, true);
		dbInstance.commitAndCloseSession();
		
		String updatedTitle = UUID.randomUUID().toString();
		String updatedDescription = "A very long absence";
		absenceCategory.setEnabled(false);
		absenceCategory.setTitle(updatedTitle);
		absenceCategory.setDescription(updatedDescription);
		AbsenceCategory updatedAbsenceCategory = absenceCategoryDao.updateAbsenceCategory(absenceCategory);
		dbInstance.commitAndCloseSession();
		
		AbsenceCategory reloadedAbsenceCategory = absenceCategoryDao.getAbsenceCategory(updatedAbsenceCategory.getKey());
		Assert.assertNotNull(reloadedAbsenceCategory);
		Assert.assertNotNull(reloadedAbsenceCategory.getKey());
		Assert.assertNotNull(reloadedAbsenceCategory.getCreationDate());
		Assert.assertNotNull(reloadedAbsenceCategory.getLastModified());
		Assert.assertFalse(reloadedAbsenceCategory.isEnabled());
		Assert.assertEquals(updatedAbsenceCategory, reloadedAbsenceCategory);
		Assert.assertEquals(absenceCategory, reloadedAbsenceCategory);
		Assert.assertEquals(updatedTitle, reloadedAbsenceCategory.getTitle());
		Assert.assertEquals(updatedDescription, reloadedAbsenceCategory.getDescription());
	}
	
	@Test
	public void getAllAbsencesCategories() {
		AbsenceCategory absenceCategory = absenceCategoryDao
				.createAbsenceCategory(UUID.randomUUID().toString(), "Random category", true);
		dbInstance.commitAndCloseSession();
		
		List<AbsenceCategory> categories = absenceCategoryDao.getAbsencesCategories(null);
		
		Assert.assertNotNull(categories);
		Assert.assertFalse(categories.isEmpty());
		Assert.assertTrue(categories.contains(absenceCategory));
	}
	
	@Test
	public void getDisabledAbsencesCategories() {
		AbsenceCategory disabledAbsenceCategory = absenceCategoryDao
				.createAbsenceCategory(UUID.randomUUID().toString(), "Random disabled category", false);
		AbsenceCategory enabledAbsenceCategory = absenceCategoryDao
				.createAbsenceCategory(UUID.randomUUID().toString(), "Random enabled category", true);
		dbInstance.commitAndCloseSession();
		
		List<AbsenceCategory> categories = absenceCategoryDao.getAbsencesCategories(Boolean.FALSE);
		assertThat(categories)
			.isNotNull()
			.contains(disabledAbsenceCategory)
			.doesNotContain(enabledAbsenceCategory);
	}
	
	@Test
	public void getEnabledAbsencesCategories() {
		AbsenceCategory disabledAbsenceCategory = absenceCategoryDao
				.createAbsenceCategory(UUID.randomUUID().toString(), "Random disabled category", false);
		AbsenceCategory enabledAbsenceCategory = absenceCategoryDao
				.createAbsenceCategory(UUID.randomUUID().toString(), "Random enabled category", true);
		dbInstance.commitAndCloseSession();
		
		List<AbsenceCategory> categories = absenceCategoryDao.getAbsencesCategories(Boolean.TRUE);
		assertThat(categories)
			.isNotNull()
			.contains(enabledAbsenceCategory )
			.doesNotContain(disabledAbsenceCategory);
	}
	
	@Test
	public void isAbsenceCategoryInUse() {
		AbsenceCategory absenceCategory = absenceCategoryDao
				.createAbsenceCategory(UUID.randomUUID().toString(), "Random category", true);
		dbInstance.commitAndCloseSession();
		
		boolean inUse = absenceCategoryDao.isAbsenceCategoryInUse(absenceCategory);
		Assert.assertFalse(inUse);
	}

}
