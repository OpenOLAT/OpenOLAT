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
package org.olat.portfolio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.modules.fo.portfolio.ForumArtefact;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.manager.EPMapPolicy;
import org.olat.portfolio.manager.EPMapPolicy.Type;
import org.olat.portfolio.model.EPFilterSettings;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.ElementType;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * do queries to check for performance bottlenecks: 
 * - Create a lot of artefacts, do load and searches 
 * - create a lot of huge maps, publish them for all users, do searches
 * 
 * <P>
 * Initial Date: 26.01.2011 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPPerformanceTest extends OlatTestCase {

	private static final String LOREM_STRING_512 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean id sapien ac justo congue mollis. " +
			"Sed pulvinar magna nec nulla gravida eu ullamcorper dolor mattis. Phasellus quis neque dolor. Aliquam non odio ligula. Integer purus nisi, " +
			"cursus accumsan ultricies eget, gravida sed eros. Maecenas malesuada commodo nisl, sit amet aliquam elit dapibus ut. Duis ultricies nibh at " +
			"felis commodo a rutrum ipsum tristique. Nulla facilisi. Vivamus convallis faucibus augue quis ultrices. Sed quam orci, dignissim metus. ";
	private static final List<String> tagList1 = new ArrayList<String>(Arrays.asList("Haus", "baum", "Wald"));
	private static final List<String> tagList2 = new ArrayList<String>(Arrays.asList("Schule", "Lehrer"));
	

	@Autowired
	private DB dbInstance;

	@Autowired
	private EPFrontendManager epFrontendManager;
	
	private static Identity ident1, ident2;
	private static boolean isInitialized = false;

	@Before
	public void setUp() {
		if (!isInitialized) {
			ident1 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString());
			ident2 = JunitTestHelper.createAndPersistIdentityAsUser(UUID.randomUUID().toString());
		}
	}

	@After
	public void tearDown() {
		deleteMaps();
		dbInstance.commitAndCloseSession();
	}

	@Test
	public void testManagers() {
		assertNotNull(dbInstance);
		assertNotNull(epFrontendManager);
	}
	
	@Test
	public void testRealisticArtefactAmount(){
		internalTestManyArtefactCreation(200);
	}
		
	private void internalTestManyArtefactCreation(int artefactAmount){
		long start = System.currentTimeMillis();
		Runtime r = Runtime.getRuntime();
		for (int j = 0; j < artefactAmount; j++) {
			AbstractArtefact artefact = createAndFillArtefact(j);
//			 tag the artefacts
			if (j %2 == 0) {
				epFrontendManager.setArtefactTags(ident1, artefact, tagList1);
			} else {
				epFrontendManager.setArtefactTags(ident1, artefact, tagList2);
			}
			
			if (j % 10 == 0) {
				DBFactory.getInstance().closeSession();
			}
			if (j % 100 == 0){
				logger.info("created another 100 artefacts! -> " + j);
				logger.info("  free memory: " + r.freeMemory());
			}
		} // for
		
		
		// load the whole artefact list
		long now = System.currentTimeMillis();
		logger.info("created " + artefactAmount + " artefacts in: " + (now - start) + " ms.");
		start = System.currentTimeMillis();
		List<AbstractArtefact> artList = epFrontendManager.getArtefactPoolForUser(ident1);
		now = System.currentTimeMillis();
		logger.info("querying all of them took: " + (now - start) + " ms.");
		assertEquals(artList.size(), artefactAmount);
		
		// filter artefacts by tags
		EPFilterSettings filterSettings = new EPFilterSettings();
		filterSettings.setTagFilter(new ArrayList<String>(Arrays.asList("Schule")));
		start = System.currentTimeMillis();
		artList = epFrontendManager.filterArtefactsByFilterSettings(filterSettings, ident1, Roles.userRoles(), Locale.ENGLISH);
		now = System.currentTimeMillis();
		logger.info("filter artefacts by one tag took: " + (now - start) + " ms.");
		assertEquals(artList.size(), artefactAmount/2);
		
		filterSettings.setTagFilter(tagList1);
		start = System.currentTimeMillis();
		artList = epFrontendManager.filterArtefactsByFilterSettings(filterSettings, ident1, Roles.userRoles(), Locale.ENGLISH);
		now = System.currentTimeMillis();
		logger.info("filter artefacts by tagList1 took: " + (now - start) + " ms.");
		assertEquals(artList.size(), artefactAmount/2);
		
	}

	/**
	 * create a heavy filled artefact!
	 * @param j number of artefact
	 * @return
	 */
	private AbstractArtefact createAndFillArtefact(int j) {
		AbstractArtefact artefact = epFrontendManager.createAndPersistArtefact(ident1, ForumArtefact.FORUM_ARTEFACT_TYPE);
		artefact.setFulltextContent(getLoremStringFactor512(2));
		artefact.setReflexion(getLoremStringFactor512(3));
		artefact.setDescription(LOREM_STRING_512);
		artefact.setTitle("Test Artefact number" + j);
		artefact.setSignature(60);
		artefact.setCollectionDate(new Date());
		artefact.setBusinessPath("a dummy businessPath entry");
		artefact.setSource("some Forum was my source");
		return epFrontendManager.updateArtefact(artefact);
	}
	
	@Test
	public void testMaps500(){
		internalTestCreateManyMaps(500);
	}
	
	private void deleteMaps(){
		List<PortfolioStructure> publicMaps = epFrontendManager.getStructureElementsFromOthers(ident2, null, ElementType.STRUCTURED_MAP, ElementType.DEFAULT_MAP);
		int i=1;
		for (PortfolioStructure portfolioStructure : publicMaps) {
			i++;
			epFrontendManager.deletePortfolioStructure(portfolioStructure);
			if (i % 100 == 0) {
				DBFactory.getInstance().closeSession();
			}
		}
	}
	
	private void internalTestCreateManyMaps(int mapAmount){		
		long start = System.currentTimeMillis();
		// prepare some artefacts to link to maps later
		ArrayList<AbstractArtefact> artefacts = new ArrayList<AbstractArtefact>(10);
		for (int i = 1; i < 11; i++) {
			artefacts.add(createAndFillArtefact(i));
		}
		
		for (int k = 1; k < mapAmount; k++) {
			PortfolioStructureMap map = epFrontendManager.createAndPersistPortfolioDefaultMap(ident1, "a test map number " + k, LOREM_STRING_512);
			// attach sites and structures to it
			ArrayList<PortfolioStructure> structs = new ArrayList<PortfolioStructure>();
			PortfolioStructure page1 = epFrontendManager.createAndPersistPortfolioPage(map, "test page1 for map " + k, LOREM_STRING_512);
			structs.add(page1);
			PortfolioStructure struct11 = epFrontendManager.createAndPersistPortfolioStructureElement(page1, "struct1 in page1 for map" + k, LOREM_STRING_512);
			structs.add(struct11);
			PortfolioStructure struct12 = epFrontendManager.createAndPersistPortfolioStructureElement(page1, "struct2 in page1 for map" + k, LOREM_STRING_512);
			structs.add(struct12);
			PortfolioStructure page2 = epFrontendManager.createAndPersistPortfolioPage(map, "test page2 for map " + k, LOREM_STRING_512);
			structs.add(page2);
			PortfolioStructure struct21 = epFrontendManager.createAndPersistPortfolioStructureElement(page2, "struct1 in page2 for map" + k, LOREM_STRING_512);
			structs.add(struct21);
			PortfolioStructure struct22 = epFrontendManager.createAndPersistPortfolioStructureElement(page2, "struct2 in page2 for map" + k, LOREM_STRING_512);	
			structs.add(struct22);
			
			// attach different artefacts to several places in map
			int l = 1;
			for (Iterator<PortfolioStructure> iterator = structs.iterator(); iterator.hasNext();) {
				PortfolioStructure portfolioStructure = iterator.next();
				epFrontendManager.addArtefactToStructure(ident1, artefacts.get(l), portfolioStructure);
				// add two artefacts 
				if (l % 2 == 0){
					epFrontendManager.addArtefactToStructure(ident1, artefacts.get(l+1), portfolioStructure);
				}
				l++;
			} // for attach
		
			// share the map with all users
			EPMapPolicy userPolicy = new EPMapPolicy();
			userPolicy.setType(Type.allusers);
			epFrontendManager.updateMapPolicies(map, Collections.singletonList(userPolicy));
			dbInstance.commitAndCloseSession();
			
		} // for maps
		long now = System.currentTimeMillis();
		logger.info("created " + mapAmount + " maps, attached artefacts and shared maps to public in: " + (now - start) + " ms.");

		// load all maps
		start = System.currentTimeMillis();
		List<PortfolioStructure> publicMaps = epFrontendManager.getStructureElementsFromOthers(ident2, null, ElementType.STRUCTURED_MAP, ElementType.DEFAULT_MAP);
		now = System.currentTimeMillis();
		logger.info("got all public maps in: " + (now - start) + " ms.");
		
		// simulate queries done in EPMultipleMapController for all public maps:
		start = System.currentTimeMillis();
		long sharedQ = 0;
		long countArtefactQ = 0;
		long countChildQ = 0;
		long qstart = 0;
		int j = 0;
		Runtime r = Runtime.getRuntime();
		for (PortfolioStructure map : publicMaps) {
			j++;
			qstart = System.currentTimeMillis();
			epFrontendManager.isMapShared((PortfolioStructureMap) map);
			sharedQ += System.currentTimeMillis() - qstart;
			qstart = System.currentTimeMillis();
			epFrontendManager.countArtefactsInMap((PortfolioStructureMap) map);
			countArtefactQ += System.currentTimeMillis() - qstart;
			// lookup structured maps: if received from a template, would also do a lookup on repository entry!
//			EPTargetResource resource = structMap.getTargetResource();
//			RepositoryEntry repoEntry = RepositoryManager.getInstance().lookupRepositoryEntry(resource.getOLATResourceable(), false);
			qstart = System.currentTimeMillis();
			epFrontendManager.countStructureChildren(map);
			countChildQ += System.currentTimeMillis() - qstart;
			
			if (j % 100 == 0){
				showStatsForStep(j, start, sharedQ, countArtefactQ, countChildQ, r);		
			}
		}
		logger.info("============= get overall stats ==============");
		showStatsForStep(mapAmount, start, sharedQ, countArtefactQ, countChildQ, r);		
	}

	/**
	 * @param mapAmount
	 * @param start
	 * @param sharedQ
	 * @param countArtefactQ
	 * @param countChildQ
	 */
	private void showStatsForStep(int step, long start, long sharedQ, long countArtefactQ, long countChildQ, Runtime r) {
		long now;
		now = System.currentTimeMillis();
		logger.info("---------------------------------------------");
		logger.info("show actual query stats for step with " + step + " maps processed.");
		logger.info(" free memory: " + r.freeMemory());
		logger.info(" simulated queries to show all public maps, took: " + (now - start) + " ms so far.");
		logger.info("  Q: share state average:			" + (sharedQ / step) 				+ "		total: " + sharedQ);
		logger.info("  Q: artefact count average:		" + (countArtefactQ / step) + "		total: " + countArtefactQ);
		logger.info("  Q: child count average: 		" + (countChildQ / step) 		+ "		total: " + countChildQ);		
	}
	
	
	
	private String getLoremStringFactor512(int factor){
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < factor; i++) {
			sb.append(LOREM_STRING_512);
		}
		return sb.toString();
	}
	
}
