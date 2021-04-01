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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyLevelTypeToType;
import org.olat.modules.taxonomy.model.TaxonomyLevelTypeImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLevelTypeDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyLevelTypeDAO taxonomyLevelTypeDao;
	@Autowired
	private TaxonomyLevelTypeToTypeDAO taxonomyLevelTypeToTypeDao;
	
	@Test
	public void createTaxonomyLevelType() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("Tax-type", "Typed taxonomy", "A typed taxonomy", "");
		TaxonomyLevelType type = taxonomyLevelTypeDao.createTaxonomyLevelType("Type-0", "A first type", "Typed", "TYP-0", true, taxonomy);
		dbInstance.commit();
		
		Assert.assertNotNull(type);
		Assert.assertNotNull(type.getKey());
		Assert.assertNotNull(type.getCreationDate());
		Assert.assertEquals("Type-0", type.getIdentifier());
		Assert.assertEquals("A first type", type.getDisplayName());
		Assert.assertEquals("Typed", type.getDescription());
		Assert.assertEquals("TYP-0", type.getExternalId());
	}

	@Test
	public void createAndLoadTaxonomyLevelType() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("Tax-type", "Typed taxonomy", "A typed taxonomy", "");
		TaxonomyLevelType type = taxonomyLevelTypeDao.createTaxonomyLevelType("Type-reload", "A first reloaded type", "Typed", "TYP-1", true, taxonomy);
		dbInstance.commitAndCloseSession();

		TaxonomyLevelType reloadedType = taxonomyLevelTypeDao.loadTaxonomyLevelTypeByKey(type.getKey());
		
		Assert.assertNotNull(reloadedType);
		Assert.assertEquals(type, reloadedType);
		Assert.assertEquals("Type-reload", reloadedType.getIdentifier());
		Assert.assertEquals("A first reloaded type", reloadedType.getDisplayName());
		Assert.assertEquals("Typed", reloadedType.getDescription());
		Assert.assertEquals("TYP-1", reloadedType.getExternalId());	
	}
	
	@Test
	public void loadTaxonomyLevelTypeByTaxonomy() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("Tax-type", "Typed taxonomy", "A typed taxonomy", "");
		Taxonomy taxonomyMarker = taxonomyDao.createTaxonomy("Tax-marker", "Marker taxonomy", "An unused taxonomy", "");
		TaxonomyLevelType type = taxonomyLevelTypeDao.createTaxonomyLevelType("Type-reload", "A first reloaded type", "Typed", "TYP-1", true, taxonomy);
		dbInstance.commitAndCloseSession();
		
		//check the taxonomy with types
		List<TaxonomyLevelType> taxonomyTypes = taxonomyLevelTypeDao.loadTaxonomyLevelTypeByTaxonomy(taxonomy);
		Assert.assertNotNull(taxonomyTypes);
		Assert.assertEquals(1, taxonomyTypes.size());
		Assert.assertEquals(type, taxonomyTypes.get(0));
		
		//check the marker without
		List<TaxonomyLevelType> taxonomyWithoutTypes = taxonomyLevelTypeDao.loadTaxonomyLevelTypeByTaxonomy(taxonomyMarker);
		Assert.assertNotNull(taxonomyWithoutTypes);
		Assert.assertEquals(0, taxonomyWithoutTypes.size());
	}
	
	@Test
	public void allowSubTypes() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("Tax-type", "Typed taxonomy", "A typed taxonomy", "");
		TaxonomyLevelType type = taxonomyLevelTypeDao.createTaxonomyLevelType("Type-parent", "A type", null, null, true, taxonomy);
		TaxonomyLevelType subType1 = taxonomyLevelTypeDao.createTaxonomyLevelType("Type-sub-1", "A type", null, null, true, taxonomy);
		TaxonomyLevelType subType2 = taxonomyLevelTypeDao.createTaxonomyLevelType("Type-sub-2", "A type", null, null, true, taxonomy);
		dbInstance.commitAndCloseSession();
		
		taxonomyLevelTypeToTypeDao.addAllowedSubType(type, subType1);
		taxonomyLevelTypeToTypeDao.addAllowedSubType(type, subType2);
		dbInstance.commitAndCloseSession();

		TaxonomyLevelTypeImpl reloadedType = (TaxonomyLevelTypeImpl)taxonomyLevelTypeDao.loadTaxonomyLevelTypeByKey(type.getKey());
		Assert.assertNotNull(reloadedType.getAllowedTaxonomyLevelSubTypes());
		Assert.assertEquals(2, reloadedType.getAllowedTaxonomyLevelSubTypes().size());
		Set<TaxonomyLevelTypeToType> allowedTypeSet = reloadedType.getAllowedTaxonomyLevelSubTypes();
		List<TaxonomyLevelType> allowedSubTypes = allowedTypeSet.stream()
				.map(t -> t.getAllowedSubTaxonomyLevelType())
				.collect(Collectors.toList());
		Assert.assertTrue(allowedSubTypes.contains(subType1));
		Assert.assertTrue(allowedSubTypes.contains(subType2));
	}
}
