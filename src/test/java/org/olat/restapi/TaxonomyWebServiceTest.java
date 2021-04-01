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
package org.olat.restapi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyLevelTypeToType;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyLevelRefImpl;
import org.olat.modules.taxonomy.restapi.TaxonomyCompetenceVO;
import org.olat.modules.taxonomy.restapi.TaxonomyLevelTypeVO;
import org.olat.modules.taxonomy.restapi.TaxonomyLevelVO;
import org.olat.modules.taxonomy.restapi.TaxonomyVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 5 Oct 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyWebServiceTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(TaxonomyWebServiceTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyService taxonomyService;

	@Test
	public void getTaxonomy()
	throws IOException, URISyntaxException {
		Taxonomy taxonomy = taxonomyService.createTaxonomy("REST-Tax-1", "Taxonomy on rest", "Rest is cool", "Ext-tax-1");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(taxonomy);

		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("taxonomy").path(taxonomy.getKey().toString()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		TaxonomyVO taxonomyVO = conn.parse(response, TaxonomyVO.class);
		Assert.assertNotNull(taxonomyVO);
		Assert.assertEquals(taxonomy.getKey(), taxonomyVO.getKey());
		Assert.assertEquals("REST-Tax-1", taxonomyVO.getIdentifier());
		Assert.assertEquals("Taxonomy on rest", taxonomyVO.getDisplayName());
		Assert.assertEquals("Rest is cool", taxonomyVO.getDescription());
		Assert.assertEquals("Ext-tax-1", taxonomyVO.getExternalId());
	}
	
	@Test
	public void getFlatTaxonomyLevels()
	throws IOException, URISyntaxException {
		Taxonomy taxonomy = taxonomyService.createTaxonomy("REST-Tax-2", "Taxonomy on rest", "Rest is cool", "Ext-tax-1");
		TaxonomyLevel level1 = taxonomyService.createTaxonomyLevel("REST-Tax-l-1", "Level 1 on rest", "Level", "Ext-3", null, null, taxonomy);
		TaxonomyLevel level2 = taxonomyService.createTaxonomyLevel("REST-Tax-l-2", "Level 2 on rest", "Level", "Ext-4", null, null, taxonomy);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(taxonomy);

		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("taxonomy").path(taxonomy.getKey().toString()).path("levels").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<TaxonomyLevelVO> taxonomyLevelVOList = parseTaxonomyLevelsArray(response.getEntity().getContent());
		Assert.assertNotNull(taxonomyLevelVOList);
		Assert.assertEquals(2, taxonomyLevelVOList.size());
		
		boolean found1 = false;
		boolean found2 = false;
		for(TaxonomyLevelVO levelVo:taxonomyLevelVOList) {
			if(level1.getKey().equals(levelVo.getKey())) {
				found1 = true;
			} else if(level2.getKey().equals(levelVo.getKey())) {
				found2 = true;
			}
		}
		Assert.assertTrue(found1);
		Assert.assertTrue(found2);
	}
	
	@Test
	public void putTaxonomyLevel_rootLevel()
	throws IOException, URISyntaxException {
		Taxonomy taxonomy = taxonomyService.createTaxonomy("REST-Tax-2", "Taxonomy on rest", "PUT is cool", "PUT-tax-1");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(taxonomy);
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		String uid = UUID.randomUUID().toString();
		TaxonomyLevelVO levelVo = new TaxonomyLevelVO();
		levelVo.setIdentifier(uid);
		levelVo.setDisplayName("PUT root level");
		levelVo.setDescription("Try to PUT a root level");
		levelVo.setExternalId("EXT-190");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("taxonomy").path(taxonomy.getKey().toString()).path("levels").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, levelVo);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		//check the returned value
		TaxonomyLevelVO newTaxonomyLevelVo = conn.parse(response, TaxonomyLevelVO.class);
		Assert.assertNotNull(newTaxonomyLevelVo);
		Assert.assertNotNull(newTaxonomyLevelVo.getKey());
		Assert.assertEquals(uid, newTaxonomyLevelVo.getIdentifier());
		Assert.assertEquals("PUT root level", newTaxonomyLevelVo.getDisplayName());
		Assert.assertEquals("Try to PUT a root level", newTaxonomyLevelVo.getDescription());
		Assert.assertEquals("EXT-190", newTaxonomyLevelVo.getExternalId());
		
		//check the database
		List<TaxonomyLevel> levels = taxonomyService.getTaxonomyLevels(taxonomy);
		Assert.assertNotNull(levels);
		Assert.assertEquals(1, levels.size());
		TaxonomyLevel savedLevel = levels.get(0);
		Assert.assertEquals(newTaxonomyLevelVo.getKey(), savedLevel.getKey());
		Assert.assertEquals(uid, savedLevel.getIdentifier());
		Assert.assertEquals("PUT root level", savedLevel.getDisplayName());
		Assert.assertEquals("Try to PUT a root level", savedLevel.getDescription());
		Assert.assertEquals("EXT-190", savedLevel.getExternalId());
	}
	
	@Test
	public void putTaxonomyLevel_subLevel()
	throws IOException, URISyntaxException {
		Taxonomy taxonomy = taxonomyService.createTaxonomy("REST-Tax-3", "Taxonomy on rest", "PUT is cool, yes!", "PUT-tax-2");
		TaxonomyLevel rootLevel = taxonomyService.createTaxonomyLevel("REST-Tax-r-1", "Root level on rest", "Level", "Ext-23", null, null, taxonomy);
		TaxonomyLevelType type = taxonomyService.createTaxonomyLevelType("Sub-type", "Type for a sub level", "All is in the title", "TYP-23", true, taxonomy);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(taxonomy);
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		String uid = UUID.randomUUID().toString();
		TaxonomyLevelVO levelVo = new TaxonomyLevelVO();
		levelVo.setIdentifier(uid);
		levelVo.setDisplayName("PUT a sub level");
		levelVo.setDescription("Try to PUT a level above the root");
		levelVo.setExternalId("EXT-191");
		levelVo.setParentKey(rootLevel.getKey());
		levelVo.setTypeKey(type.getKey());
		
		URI request = UriBuilder.fromUri(getContextURI()).path("taxonomy").path(taxonomy.getKey().toString()).path("levels").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, levelVo);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		//check the returned value
		TaxonomyLevelVO newTaxonomyLevelVo = conn.parse(response, TaxonomyLevelVO.class);
		Assert.assertNotNull(newTaxonomyLevelVo);
		Assert.assertNotNull(newTaxonomyLevelVo.getKey());
		Assert.assertEquals(uid, newTaxonomyLevelVo.getIdentifier());
		Assert.assertEquals("PUT a sub level", newTaxonomyLevelVo.getDisplayName());
		Assert.assertEquals("EXT-191", newTaxonomyLevelVo.getExternalId());
		Assert.assertEquals(rootLevel.getKey(), newTaxonomyLevelVo.getParentKey());
		Assert.assertEquals(type.getKey(), newTaxonomyLevelVo.getTypeKey());
		
		//check the database
		TaxonomyLevel savedLevel = taxonomyService.getTaxonomyLevel(new TaxonomyLevelRefImpl(newTaxonomyLevelVo.getKey()));
		Assert.assertNotNull(savedLevel);
		Assert.assertEquals(newTaxonomyLevelVo.getKey(), savedLevel.getKey());
		Assert.assertEquals(newTaxonomyLevelVo.getParentKey(), savedLevel.getParent().getKey());
		
		//check parent line
		List<TaxonomyLevel> parentLine = taxonomyService.getTaxonomyLevelParentLine(savedLevel, taxonomy);
		Assert.assertNotNull(parentLine);
		Assert.assertEquals(2, parentLine.size());
		Assert.assertEquals(rootLevel, parentLine.get(0));
		Assert.assertEquals(savedLevel, parentLine.get(1));
	}
	
	/**
	 * Update level. 
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void updateTaxonomyLevel()
	throws IOException, URISyntaxException {
		Taxonomy taxonomy = taxonomyService.createTaxonomy("REST-Tax-4", "Taxonomy on rest", "PUT is cool, yes!", "PUT-tax-2");
		TaxonomyLevel rootLevel = taxonomyService.createTaxonomyLevel("REST-Tax-u-1", "Root level on rest", "Level", "Ext-25", null, null, taxonomy);
		TaxonomyLevel levelToUpdate = taxonomyService.createTaxonomyLevel("REST-Tax-u-1", "Sub level on rest", "Level", "Ext-26", null, rootLevel, taxonomy);
		TaxonomyLevelType type = taxonomyService.createTaxonomyLevelType("Sub-type", "Type for a sub level", "All is in the title", "TYP-27", true, taxonomy);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(taxonomy);
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		TaxonomyLevelVO levelVo = new TaxonomyLevelVO();
		levelVo.setKey(levelToUpdate.getKey());
		levelVo.setIdentifier("Updated id");
		levelVo.setDisplayName("Updated name");
		levelVo.setDescription("Updated description");
		levelVo.setExternalId("Updated ext.");
		levelVo.setTypeKey(type.getKey());
		levelVo.setParentKey(rootLevel.getKey());
		
		URI request = UriBuilder.fromUri(getContextURI()).path("taxonomy").path(taxonomy.getKey().toString()).path("levels").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, levelVo);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		//check the updated value
		TaxonomyLevelVO updatedTaxonomyLevelVo = conn.parse(response, TaxonomyLevelVO.class);
		Assert.assertNotNull(updatedTaxonomyLevelVo);
		Assert.assertEquals("Updated id", updatedTaxonomyLevelVo.getIdentifier());
		Assert.assertEquals("Updated name", updatedTaxonomyLevelVo.getDisplayName());
		Assert.assertEquals("Updated description", updatedTaxonomyLevelVo.getDescription());
		Assert.assertEquals("Updated ext.", updatedTaxonomyLevelVo.getExternalId());
		Assert.assertEquals(rootLevel.getKey(), updatedTaxonomyLevelVo.getParentKey());
		Assert.assertEquals(type.getKey(), updatedTaxonomyLevelVo.getTypeKey());
		
		//check the database
		TaxonomyLevel savedLevel = taxonomyService.getTaxonomyLevel(new TaxonomyLevelRefImpl(updatedTaxonomyLevelVo.getKey()));
		Assert.assertNotNull(savedLevel);
		Assert.assertEquals("Updated id", savedLevel.getIdentifier());
		Assert.assertEquals("Updated name", savedLevel.getDisplayName());
		Assert.assertEquals("Updated description", savedLevel.getDescription());
		Assert.assertEquals("Updated ext.", savedLevel.getExternalId());
		Assert.assertEquals(rootLevel.getKey(), savedLevel.getParent().getKey());
		Assert.assertEquals(type.getKey(), savedLevel.getType().getKey());
	}
	
	@Test
	public void deleteTaxonomyLevel()
	throws IOException, URISyntaxException {
		Taxonomy taxonomy = taxonomyService.createTaxonomy("REST-Del-1", "Taxonomy on rest", "Delete is sad!", "DELETE-tax-1");
		TaxonomyLevel rootLevel = taxonomyService.createTaxonomyLevel("REST-Del-root", "Root level on rest", "Level", "Ext-55", null, null, taxonomy);
		TaxonomyLevel levelToDelete = taxonomyService.createTaxonomyLevel("REST-Del-u-1", "Sub level on rest", "Level", "Ext-56", null, rootLevel, taxonomy);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(taxonomy);
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI request = UriBuilder.fromUri(getContextURI()).path("taxonomy").path(taxonomy.getKey().toString())
				.path("levels").path(levelToDelete.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		//check the deleted value
		TaxonomyLevel deletedLevel =taxonomyService.getTaxonomyLevel(new TaxonomyLevelRefImpl(levelToDelete.getKey()));
		Assert.assertNull(deletedLevel);
		TaxonomyLevel survivingRootLevel = taxonomyService.getTaxonomyLevel(new TaxonomyLevelRefImpl(rootLevel.getKey()));
		Assert.assertNotNull(survivingRootLevel);
	}
	
	/**
	 * The REST method only delete something if possible. If the level
	 * has some children, competences... the call will not delete it.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void deleteTaxonomyLevel_notPossible()
	throws IOException, URISyntaxException {
		Taxonomy taxonomy = taxonomyService.createTaxonomy("REST-Del-2", "Taxonomy on rest", "Delete is sad! But there is some hope.", "DELETE-tax-2");
		TaxonomyLevel rootLevel = taxonomyService.createTaxonomyLevel("REST-Del-root", "Root level on rest", "Level", "Ext-57", null, null, taxonomy);
		TaxonomyLevel levelToDelete = taxonomyService.createTaxonomyLevel("REST-Del-u-2", "Sub level on rest", "Level", "Ext-58", null, rootLevel, taxonomy);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(taxonomy);
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI request = UriBuilder.fromUri(getContextURI()).path("taxonomy").path(taxonomy.getKey().toString())
				.path("levels").path(rootLevel.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(304, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		//check the updated value
		TaxonomyLevel survivingLevel = taxonomyService.getTaxonomyLevel(new TaxonomyLevelRefImpl(levelToDelete.getKey()));
		Assert.assertNotNull(survivingLevel);
		TaxonomyLevel survivingRootLevel = taxonomyService.getTaxonomyLevel(new TaxonomyLevelRefImpl(rootLevel.getKey()));
		Assert.assertNotNull(survivingRootLevel);
	}
	
	@Test
	public void getTaxonomyLevelTypes()
	throws IOException, URISyntaxException {
		Taxonomy taxonomy = taxonomyService.createTaxonomy("REST-Tax-2", "Taxonomy on rest", "Rest is cool", "Ext-tax-1");
		TaxonomyLevelType type1 = taxonomyService.createTaxonomyLevelType("RESR-Type-1", "Type 1 on rest", "Type", "EXT-Type-1", true, taxonomy);
		TaxonomyLevelType type2 = taxonomyService.createTaxonomyLevelType("RESR-Type-2", "Type 2 on rest", "Type", "EXT-Type-2", true, taxonomy);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(taxonomy);
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("taxonomy").path(taxonomy.getKey().toString()).path("types").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<TaxonomyLevelTypeVO> typeVoList = parseTaxonomyLevelTypesArray(response.getEntity().getContent());
		Assert.assertNotNull(typeVoList);
		Assert.assertEquals(2, typeVoList.size());
		
		boolean found1 = false;
		boolean found2 = false;
		for(TaxonomyLevelTypeVO typeVo:typeVoList) {
			if(type1.getKey().equals(typeVo.getKey())) {
				found1 = true;
			} else if(type2.getKey().equals(typeVo.getKey())) {
				found2 = true;
			}
		}
		Assert.assertTrue(found1);
		Assert.assertTrue(found2);
	}
	
	@Test
	public void getTaxonomyLevelType()
	throws IOException, URISyntaxException {
		Taxonomy taxonomy = taxonomyService.createTaxonomy("REST-Tax-2", "Taxonomy on rest", "Rest is cool", "Ext-tax-1");
		TaxonomyLevelType type = taxonomyService.createTaxonomyLevelType("REST-Type-3", "Type 3 on rest", "Type", "EXT-Type-3", true, taxonomy);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(taxonomy);
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("taxonomy").path(taxonomy.getKey().toString())
				.path("types").path(type.getKey().toString()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		TaxonomyLevelTypeVO typeVo = conn.parse(response, TaxonomyLevelTypeVO.class);
		Assert.assertNotNull(typeVo);
	}
	
	@Test
	public void putTaxonomyLevelType()
	throws IOException, URISyntaxException {
		Taxonomy taxonomy = taxonomyService.createTaxonomy("REST-Tax-2", "Taxonomy on rest", "Rest is cool", "Ext-tax-1");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(taxonomy);
		
		TaxonomyLevelTypeVO newTypeVo = new TaxonomyLevelTypeVO();
		String identifier = UUID.randomUUID().toString();
		newTypeVo.setIdentifier(identifier);
		newTypeVo.setDisplayName("REST-Type-5");
		newTypeVo.setDescription("Unused description");
		newTypeVo.setExternalId("EXT-type-5");
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("taxonomy").path(taxonomy.getKey().toString())
				.path("types").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, newTypeVo);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		TaxonomyLevelTypeVO typeVo = conn.parse(response, TaxonomyLevelTypeVO.class);
		Assert.assertNotNull(typeVo);
		Assert.assertEquals(identifier, typeVo.getIdentifier());
		Assert.assertEquals("REST-Type-5", typeVo.getDisplayName());
		Assert.assertEquals("Unused description", typeVo.getDescription());
		Assert.assertEquals("EXT-type-5", typeVo.getExternalId());
	}
	
	@Test
	public void getTaxonomyLevelTypeAllowedSubTypes()
	throws IOException, URISyntaxException {
		Taxonomy taxonomy = taxonomyService.createTaxonomy("REST-Tax-4", "Taxonomy on rest", "Rest is cool", "Ext-tax-1");
		TaxonomyLevelType type = taxonomyService.createTaxonomyLevelType("REST-Type-4", "Type 4 on rest", "Type", "EXT-Type-4", true, taxonomy);
		TaxonomyLevelType subType1 = taxonomyService.createTaxonomyLevelType("REST-Type-4-1", "Type 4.1 on rest", "Type", "EXT-Type-4-1", true, taxonomy);
		TaxonomyLevelType subType2 = taxonomyService.createTaxonomyLevelType("REST-Type-4-2", "Type 4.2 on rest", "Type", "EXT-Type-4-2", true, taxonomy);
		dbInstance.commit();
		List<TaxonomyLevelType> subTypes = new ArrayList<>(2);
		subTypes.add(subType1);
		subTypes.add(subType2);
		type = taxonomyService.updateTaxonomyLevelType(type, subTypes);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(taxonomy);
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("taxonomy").path(taxonomy.getKey().toString())
				.path("types").path(type.getKey().toString()).path("allowedSubTypes").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<TaxonomyLevelTypeVO> typeVoList = parseTaxonomyLevelTypesArray(response.getEntity().getContent());
		Assert.assertNotNull(typeVoList);
		Assert.assertEquals(2, typeVoList.size());
		
		boolean found1 = false;
		boolean found2 = false;
		for(TaxonomyLevelTypeVO typeVo:typeVoList) {
			if(subType1.getKey().equals(typeVo.getKey())) {
				found1 = true;
			} else if(subType2.getKey().equals(typeVo.getKey())) {
				found2 = true;
			}
		}
		Assert.assertTrue(found1);
		Assert.assertTrue(found2);
	}
	
	
	@Test
	public void allowTaxonomyLevelTypeAllowedSubType()
	throws IOException, URISyntaxException {
		Taxonomy taxonomy = taxonomyService.createTaxonomy("REST-Tax-4", "Taxonomy on rest", "Rest is cool", "Ext-tax-1");
		TaxonomyLevelType type = taxonomyService.createTaxonomyLevelType("REST-Type-4", "Type 4 on rest", "Type", "EXT-Type-4", true, taxonomy);
		TaxonomyLevelType subType1 = taxonomyService.createTaxonomyLevelType("REST-Type-4-1", "Type 4.1 on rest", "Type", "EXT-Type-4-1", true, taxonomy);
		TaxonomyLevelType subType2 = taxonomyService.createTaxonomyLevelType("REST-Type-4-2", "Type 4.2 on rest", "Type", "EXT-Type-4-2", true, taxonomy);
		dbInstance.commit();
		type = taxonomyService.updateTaxonomyLevelType(type, Collections.singletonList(subType1));
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("taxonomy").path(taxonomy.getKey().toString())
				.path("types").path(type.getKey().toString()).path("allowedSubTypes").path(subType2.getKey().toString()).build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		TaxonomyLevelType reloadedType = taxonomyService.getTaxonomyLevelType(type);
		Set<TaxonomyLevelTypeToType> typeToTypes = reloadedType.getAllowedTaxonomyLevelSubTypes();
		Assert.assertEquals(2, typeToTypes.size());
		boolean found1 = false;
		boolean found2 = false;
		for(TaxonomyLevelTypeToType typeToType:typeToTypes) {
			TaxonomyLevelType subType = typeToType.getAllowedSubTaxonomyLevelType();
			if(subType1.getKey().equals(subType.getKey())) {
				found1 = true;
			} else if(subType2.getKey().equals(subType.getKey())) {
				found2 = true;
			}
		}
		Assert.assertTrue(found1);
		Assert.assertTrue(found2);
	}
	
	@Test
	public void disallowTaxonomyLevelTypeAllowedSubType()
	throws IOException, URISyntaxException {
		Taxonomy taxonomy = taxonomyService.createTaxonomy("REST-Tax-6", "Taxonomy on rest", "Rest is cool", "Ext-tax-6");
		TaxonomyLevelType type = taxonomyService.createTaxonomyLevelType("REST-Type-6", "Type 6 on rest", "Type", "EXT-Type-6", true, taxonomy);
		TaxonomyLevelType subType1 = taxonomyService.createTaxonomyLevelType("REST-Type-6-1", "Type 6.1 on rest", "Type", "EXT-Type-6-1", true, taxonomy);
		TaxonomyLevelType subType2 = taxonomyService.createTaxonomyLevelType("REST-Type-6-2", "Type 6.2 on rest", "Type", "EXT-Type-6-2", true, taxonomy);
		TaxonomyLevelType subType3 = taxonomyService.createTaxonomyLevelType("REST-Type-6-3", "Type 6.3 on rest", "Type", "EXT-Type-6-3", true, taxonomy);
		dbInstance.commit();
		List<TaxonomyLevelType> allowedSubTypes = new ArrayList<>();
		allowedSubTypes.add(subType1);
		allowedSubTypes.add(subType2);
		allowedSubTypes.add(subType3);
		type = taxonomyService.updateTaxonomyLevelType(type, allowedSubTypes);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("taxonomy").path(taxonomy.getKey().toString())
				.path("types").path(type.getKey().toString()).path("allowedSubTypes").path(subType2.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		TaxonomyLevelType reloadedType = taxonomyService.getTaxonomyLevelType(type);
		Set<TaxonomyLevelTypeToType> typeToTypes = reloadedType.getAllowedTaxonomyLevelSubTypes();
		Assert.assertEquals(2, typeToTypes.size());
		boolean found1 = false;
		boolean found2 = false;
		boolean found3 = false;
		for(TaxonomyLevelTypeToType typeToType:typeToTypes) {
			TaxonomyLevelType subType = typeToType.getAllowedSubTaxonomyLevelType();
			if(subType1.getKey().equals(subType.getKey())) {
				found1 = true;
			} else if(subType2.getKey().equals(subType.getKey())) {
				found2 = true;
			} else if(subType3.getKey().equals(subType.getKey())) {
				found3 = true;
			}
		}
		Assert.assertTrue(found1);
		Assert.assertFalse(found2);
		Assert.assertTrue(found3);
	}
	
	@Test
	public void getTaxonomyLevelComptences()
	throws IOException, URISyntaxException {
		// prepare a level, 2 users and 2 competences
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("competence-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("competence-2");
		Taxonomy taxonomy = taxonomyService.createTaxonomy("REST-Tax-6", "Taxonomy on rest", "Rest is cool", "Ext-tax-6");
		TaxonomyLevel level = taxonomyService.createTaxonomyLevel("REST-Tax-l-1", "Level 1 on rest", "Level", "Ext-3", null, null, taxonomy);
		taxonomyService.addTaxonomyLevelCompetences(level, id1, TaxonomyCompetenceTypes.have, null);
		taxonomyService.addTaxonomyLevelCompetences(level, id2, TaxonomyCompetenceTypes.manage, null);
		dbInstance.commitAndCloseSession();
		
		// get the competences
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("taxonomy").path(taxonomy.getKey().toString())
				.path("levels").path(level.getKey().toString()).path("competences").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<TaxonomyCompetenceVO> competenceList = parseTaxonomyComptencesArray(response.getEntity().getContent());
		Assert.assertNotNull(competenceList);
		Assert.assertEquals(2, competenceList.size());
		
		boolean foundComptenceId1 = false;
		boolean foundComptenceId2 = false;
		for(TaxonomyCompetenceVO competence:competenceList) {
			if(competence.getTaxonomyLevelKey().equals(level.getKey())) {
				if(competence.getIdentityKey().equals(id1.getKey())
					&& TaxonomyCompetenceTypes.have.name().equals(competence.getTaxonomyCompetenceType())) {
					foundComptenceId1 = true;
				} else if(competence.getIdentityKey().equals(id2.getKey())
					&& TaxonomyCompetenceTypes.manage.name().equals(competence.getTaxonomyCompetenceType())) {
					foundComptenceId2 = true;
				}
			}
		}
		Assert.assertTrue(foundComptenceId1);
		Assert.assertTrue(foundComptenceId2);
	}
	
	@Test
	public void getTaxonomyComptencesByIdentity()
	throws IOException, URISyntaxException {
		// prepare a level, 2 users and 2 competences
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competence-4");
		Taxonomy taxonomy = taxonomyService.createTaxonomy("REST-Tax-20", "Taxonomy on rest", "Rest is cool", "Ext-tax-7");
		TaxonomyLevel level1 = taxonomyService.createTaxonomyLevel("REST-Tax-l-21", "Level 1 on rest", "Level", "Ext-7", null, null, taxonomy);
		TaxonomyLevel level2 = taxonomyService.createTaxonomyLevel("REST-Tax-l-22", "Level 1 on rest", "Level", "Ext-7", null, null, taxonomy);
		taxonomyService.addTaxonomyLevelCompetences(level1, id, TaxonomyCompetenceTypes.teach, null);
		taxonomyService.addTaxonomyLevelCompetences(level2, id, TaxonomyCompetenceTypes.have, null);
		dbInstance.commitAndCloseSession();
		
		// get the competences
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("taxonomy").path(taxonomy.getKey().toString())
				.path("competences").path(id.getKey().toString()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<TaxonomyCompetenceVO> competenceList = parseTaxonomyComptencesArray(response.getEntity().getContent());
		Assert.assertNotNull(competenceList);
		Assert.assertEquals(2, competenceList.size());
		
		boolean foundComptenceId1 = false;
		boolean foundComptenceId2 = false;
		for(TaxonomyCompetenceVO competence:competenceList) {
			if(competence.getTaxonomyLevelKey().equals(level1.getKey()) && competence.getIdentityKey().equals(id.getKey())
					&& TaxonomyCompetenceTypes.teach.name().equals(competence.getTaxonomyCompetenceType())) {
				foundComptenceId1 = true;
			} else if(competence.getTaxonomyLevelKey().equals(level2.getKey()) && competence.getIdentityKey().equals(id.getKey())
					&& TaxonomyCompetenceTypes.have.name().equals(competence.getTaxonomyCompetenceType())) {
				foundComptenceId2 = true;
			}
		}
		Assert.assertTrue(foundComptenceId1);
		Assert.assertTrue(foundComptenceId2);
	}
	
	@Test
	public void getTaxonomyLevelComptences_byIdentity()
	throws IOException, URISyntaxException {
		// prepare a level, 1 user and 1 competence
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competence-4");
		Taxonomy taxonomy = taxonomyService.createTaxonomy("REST-Tax-7", "Taxonomy on rest", "Rest is cool", "Ext-tax-7");
		TaxonomyLevel level = taxonomyService.createTaxonomyLevel("REST-Tax-l-7", "Level 1 on rest", "Level", "Ext-7", null, null, taxonomy);
		taxonomyService.addTaxonomyLevelCompetences(level, id, TaxonomyCompetenceTypes.teach, null);
		dbInstance.commitAndCloseSession();
		
		// get the competences
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("taxonomy").path(taxonomy.getKey().toString())
				.path("levels").path(level.getKey().toString()).path("competences")
				.path(id.getKey().toString()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<TaxonomyCompetenceVO> competenceList = parseTaxonomyComptencesArray(response.getEntity().getContent());
		Assert.assertNotNull(competenceList);
		Assert.assertEquals(1, competenceList.size());
		TaxonomyCompetenceVO competence = competenceList.get(0);
		Assert.assertEquals(id.getKey(), competence.getIdentityKey());
		Assert.assertEquals(level.getKey(), competence.getTaxonomyLevelKey());
		Assert.assertEquals(TaxonomyCompetenceTypes.teach.name(), competence.getTaxonomyCompetenceType());
	}
	
	@Test
	public void putTaxonomyLevelComptence()
	throws IOException, URISyntaxException {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competence-4");
		Taxonomy taxonomy = taxonomyService.createTaxonomy("REST-Tax-8", "Taxonomy on rest", "PUT is cool, yes!", "PUT-tax-2");
		TaxonomyLevel level = taxonomyService.createTaxonomyLevel("REST-Tax-r-8", "Root level on rest", "Level", "Ext-23", null, null, taxonomy);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		TaxonomyCompetenceVO competenceVo = new TaxonomyCompetenceVO();
		competenceVo.setIdentityKey(id.getKey());
		competenceVo.setTaxonomyCompetenceType(TaxonomyCompetenceTypes.target.name());
		competenceVo.setTaxonomyLevelKey(level.getKey());
		
		URI request = UriBuilder.fromUri(getContextURI()).path("taxonomy").path(taxonomy.getKey().toString())
				.path("levels").path(level.getKey().toString()).path("competences").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, competenceVo);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		//check the returned value
		TaxonomyCompetenceVO newTaxonomyCompetenceVo = conn.parse(response, TaxonomyCompetenceVO.class);
		Assert.assertNotNull(newTaxonomyCompetenceVo);
		Assert.assertNotNull(newTaxonomyCompetenceVo.getKey());
		Assert.assertEquals(id.getKey(), newTaxonomyCompetenceVo.getIdentityKey());
		Assert.assertEquals(TaxonomyCompetenceTypes.target.name(), newTaxonomyCompetenceVo.getTaxonomyCompetenceType());
		Assert.assertEquals(level.getKey(), newTaxonomyCompetenceVo.getTaxonomyLevelKey());
		
		//check the database
		List<TaxonomyCompetence> competences = taxonomyService.getTaxonomyCompetences(id, TaxonomyCompetenceTypes.target);
		Assert.assertNotNull(competences);
		Assert.assertEquals(1, competences.size());
		TaxonomyCompetence competence = competences.get(0);
		Assert.assertEquals(id, competence.getIdentity());
		Assert.assertEquals(level, competence.getTaxonomyLevel());
		Assert.assertEquals(TaxonomyCompetenceTypes.target, competence.getCompetenceType());
	}
	
	@Test
	public void removeTaxonomyLevelCompetence()
	throws IOException, URISyntaxException {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competence-4");
		Taxonomy taxonomy = taxonomyService.createTaxonomy("REST-Tax-8", "Taxonomy on rest", "PUT is cool, yes!", "PUT-tax-2");
		TaxonomyLevel level = taxonomyService.createTaxonomyLevel("REST-Tax-r-8", "Root level on rest", "Level", "Ext-23", null, null, taxonomy);
		TaxonomyCompetence competence = taxonomyService.addTaxonomyLevelCompetences(level, id, TaxonomyCompetenceTypes.target, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(competence);
		//make sure we have something to delete
		TaxonomyCompetence reloadedCompetence =  taxonomyService.getTaxonomyCompetence(competence);
		Assert.assertNotNull(reloadedCompetence);
		dbInstance.commitAndCloseSession();
		
		// remove the competence
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("taxonomy").path(taxonomy.getKey().toString())
				.path("levels").path(level.getKey().toString()).path("competences")
				.path(competence.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		//check the database
		List<TaxonomyCompetence> competences = taxonomyService.getTaxonomyCompetences(id, TaxonomyCompetenceTypes.target);
		Assert.assertNotNull(competences);
		Assert.assertEquals(0, competences.size());
	}
	
	protected List<TaxonomyLevelVO> parseTaxonomyLevelsArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<TaxonomyLevelVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	protected List<TaxonomyLevelTypeVO> parseTaxonomyLevelTypesArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<TaxonomyLevelTypeVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	protected List<TaxonomyCompetenceVO> parseTaxonomyComptencesArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<TaxonomyCompetenceVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}
