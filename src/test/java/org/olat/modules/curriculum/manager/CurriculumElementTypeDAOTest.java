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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
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

}
