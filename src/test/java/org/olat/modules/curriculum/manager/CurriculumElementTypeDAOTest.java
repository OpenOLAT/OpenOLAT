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
package org.olat.modules.curriculum.manager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.components.date.OffsetDirection;
import org.olat.modules.curriculum.AutomationContext;
import org.olat.modules.curriculum.AutomationDependingOn;
import org.olat.modules.curriculum.AutomationType;
import org.olat.modules.curriculum.AutomationUnit;
import org.olat.modules.curriculum.CurriculumAutomationConfig;
import org.olat.modules.curriculum.CurriculumAutomationRule;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeToType;
import org.olat.modules.curriculum.model.CurriculumElementTypeImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementTypeDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumElementTypeDAO curriculumElementTypeDao;
	@Autowired
	private CurriculumElementTypeToTypeDAO curriculumElementTypeToTypeDao;
	
	@Test
	public void createCurriculumElementType() {
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType("cur-el-1", "1. Element", "First element", "AC-234");
		Assert.assertNotNull(type);
		dbInstance.commitAndCloseSession();
		
		//check fields
		Assert.assertNotNull(type.getKey());
		Assert.assertNotNull(type.getCreationDate());
		Assert.assertNotNull(type.getLastModified());
		Assert.assertEquals("cur-el-1", type.getIdentifier());
		Assert.assertEquals("1. Element", type.getDisplayName());
		Assert.assertEquals("First element", type.getDescription());
		Assert.assertEquals("AC-234", type.getExternalId());
		Assert.assertFalse(type.isImplOnly());
	}
	
	@Test
	public void hasType() {
		boolean hasCurriculumElementTypes = curriculumElementTypeDao.hasType();
		Assert.assertTrue(hasCurriculumElementTypes);
	}
	
	@Test
	public void loadByKey() {
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType("cur-el-2", "2. Element", "Second element", "AC-235");
		Assert.assertNotNull(type);
		dbInstance.commitAndCloseSession();
		
		// load the element type
		CurriculumElementType reloadedType = curriculumElementTypeDao.loadByKey(type.getKey());
		dbInstance.commitAndCloseSession();
		//check
		Assert.assertNotNull(reloadedType);
		Assert.assertEquals(type, reloadedType);
		Assert.assertNotNull(reloadedType.getCreationDate());
		Assert.assertNotNull(reloadedType.getLastModified());
		Assert.assertEquals("cur-el-2", reloadedType.getIdentifier());
		Assert.assertEquals("2. Element", reloadedType.getDisplayName());
		Assert.assertEquals("Second element", reloadedType.getDescription());
		Assert.assertEquals("AC-235", reloadedType.getExternalId());
	}
	
	@Test
	public void loadByExternalId() {
		String externalId = UUID.randomUUID().toString();
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType("cur-el-2", "2. Element", "Second element", externalId);
		Assert.assertNotNull(type);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumElementType> types = curriculumElementTypeDao.loadByExternalId(externalId);
		Assertions.assertThat(types)
			.isNotNull()
			.hasSize(1)
			.containsExactly(type);
	}
	
	@Test
	public void loadAlltypes() {
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType("cur-el-3", "3. Element", "Third element", "AC-236");
		Assert.assertNotNull(type);
		dbInstance.commitAndCloseSession();
		
		// load the element type
		List<CurriculumElementType> allTypes = curriculumElementTypeDao.load();
		dbInstance.commitAndCloseSession();
		//check
		Assert.assertNotNull(allTypes);
		Assert.assertTrue(allTypes.contains(type));
	}
	
	@Test
	public void createCurriculumElementType_implOnly() {
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType("cur-el-impl", "Impl type", "An implementation type", null);
		type.setImplOnly(true);
		type = curriculumElementTypeDao.update(type);
		dbInstance.commitAndCloseSession();

		CurriculumElementType reloadedType = curriculumElementTypeDao.loadByKey(type.getKey());
		Assert.assertNotNull(reloadedType);
		Assert.assertTrue(reloadedType.isAllowedAsRootElement());
		Assert.assertFalse(reloadedType.isSingleElement());
		Assert.assertEquals(-1, reloadedType.getMaxRepositoryEntryRelations());
		Assert.assertTrue(reloadedType.isImplOnly());
	}

	@Test
	public void allowSubTypes() {
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType("Type-parent", "A type", null, null);
		CurriculumElementType subType1 = curriculumElementTypeDao.createCurriculumElementType("Type-sub-1", "A type", null, null);
		CurriculumElementType subType2 = curriculumElementTypeDao.createCurriculumElementType("Type-sub-2", "A type", null, null);
		dbInstance.commitAndCloseSession();
		
		curriculumElementTypeToTypeDao.addAllowedSubType(type, subType1);
		curriculumElementTypeToTypeDao.addAllowedSubType(type, subType2);
		dbInstance.commitAndCloseSession();

		CurriculumElementTypeImpl reloadedType = (CurriculumElementTypeImpl)curriculumElementTypeDao.loadByKey(type.getKey());
		Assert.assertNotNull(reloadedType.getAllowedSubTypes());
		Assert.assertEquals(2, reloadedType.getAllowedSubTypes().size());
		Set<CurriculumElementTypeToType> allowedTypeSet = reloadedType.getAllowedSubTypes();
		List<CurriculumElementType> allowedSubTypes = allowedTypeSet.stream()
				.map(t -> t.getAllowedSubType())
				.collect(Collectors.toList());
		Assert.assertTrue(allowedSubTypes.contains(subType1));
		Assert.assertTrue(allowedSubTypes.contains(subType2));
	}

	@Test
	public void testAutomationConfigPersistence() {
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType("cur-el-automation-1", "Automation Type", null, null);
		dbInstance.commitAndCloseSession();

		CurriculumAutomationRule rule = new CurriculumAutomationRule();
		rule.setContext(AutomationContext.IMPLEMENTATION);
		rule.setAutomationType(AutomationType.STATUS_CHANGE);
		rule.setTargetStatus("confirmed");
		rule.setEnabled(true);
		rule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		rule.setValue(14);
		rule.setUnit(AutomationUnit.DAYS);
		rule.setDirection(OffsetDirection.BEFORE);
		rule.setOnlyWhenStatus(new HashSet<>(Set.of("preparation")));

		CurriculumAutomationConfig config = new CurriculumAutomationConfig();
		config.addRule(rule);

		type.setAutomationConfig(config);
		type = curriculumElementTypeDao.update(type);
		dbInstance.commitAndCloseSession();

		CurriculumElementType reloadedType = curriculumElementTypeDao.loadByKey(type.getKey());

		Assertions.assertThat(reloadedType.getAutomationConfig()).isNotNull();
		Assertions.assertThat(reloadedType.getAutomationConfig().getRules()).hasSize(1);
		Assertions.assertThat(reloadedType.getAutomationConfig().getRules().get(0).getTargetStatus()).isEqualTo("confirmed");
		Assertions.assertThat(reloadedType.getAutomationConfig().getRules().get(0).getValue()).isEqualTo(14);
	}

	@Test
	public void testAutomationConfigNoSeeding() {
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType("cur-el-automation-2", "Automation Type Seed", null, null);
		dbInstance.commitAndCloseSession();

		CurriculumElementType reloadedType = curriculumElementTypeDao.loadByKey(type.getKey());

		Assertions.assertThat(reloadedType.getAutomationConfig()).isNull();
	}

}
