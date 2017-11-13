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

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.model.TaxonomyImpl;
import org.olat.modules.taxonomy.model.TaxonomyInfos;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	
	@Test
	public void createTaxonomy() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID21", "My first taxonomy", "A very little taxonomy", "EXT_REF");
		dbInstance.commit();
		
		Assert.assertNotNull(taxonomy);
		Assert.assertNotNull(taxonomy.getKey());
		Assert.assertNotNull(((TaxonomyImpl)taxonomy).getGroup());
		Assert.assertEquals("ID21", taxonomy.getIdentifier());
		Assert.assertEquals("My first taxonomy", taxonomy.getDisplayName());
		Assert.assertEquals("A very little taxonomy", taxonomy.getDescription());
		Assert.assertEquals("EXT_REF", taxonomy.getExternalId());
	}
	
	@Test
	public void createAndLoadTaxonomy() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID22", "An other taxonomy", "A little taxonomy", "REF-22");
		dbInstance.commitAndCloseSession();
		
		Taxonomy reloaded = taxonomyDao.loadByKey(taxonomy.getKey());
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(reloaded);
		Assert.assertEquals(taxonomy, reloaded);
		Assert.assertNotNull(((TaxonomyImpl)reloaded).getGroup());
		Assert.assertNotNull(((TaxonomyImpl)reloaded).getGroup().getKey());
		
		Assert.assertEquals("ID22", reloaded.getIdentifier());
		Assert.assertEquals("An other taxonomy", reloaded.getDisplayName());
		Assert.assertEquals("A little taxonomy", reloaded.getDescription());
		Assert.assertEquals("REF-22", reloaded.getExternalId());
	}
	
	@Test
	public void getTaxonomyList() {
		Taxonomy taxonomy0 = taxonomyDao.createTaxonomy("ID40", "An other taxonomy", "A little taxonomy", "REF-40");
		Taxonomy taxonomy1 = taxonomyDao.createTaxonomy("ID41", "An other taxonomy", "A little taxonomy", "REF-41");
		dbInstance.commitAndCloseSession();
		
		List<Taxonomy> taxonomyList = taxonomyDao.getTaxonomyList();
		Assert.assertTrue(taxonomyList.contains(taxonomy0));
		Assert.assertTrue(taxonomyList.contains(taxonomy1));
	}
	
	@Test
	public void getTaxonomyInfosList() {
		Taxonomy taxonomy0 = taxonomyDao.createTaxonomy("ID40", "An other taxonomy", "A little taxonomy", "REF-40");
		Taxonomy taxonomy1 = taxonomyDao.createTaxonomy("ID41", "An other taxonomy", "A little taxonomy", "REF-41");
		dbInstance.commitAndCloseSession();
		
		List<TaxonomyInfos> infosList = taxonomyDao.getTaxonomyInfosList();
		Assert.assertNotNull(infosList);
		int found = 0;
		for(TaxonomyInfos info:infosList) {
			if(info.getKey().equals(taxonomy0.getKey()) || info.getKey().equals(taxonomy1.getKey())) {
				found++;
			}
		}
		Assert.assertEquals(2, found);
	}
}