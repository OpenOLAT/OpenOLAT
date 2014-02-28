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
package org.olat.core.commons.services.mark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

/**
 * 
 * Description:<br>
 * Test for the marking service
 * 
 * <P>
 * Initial Date:  8 juin 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class MarksTest extends OlatTestCase {
	
	private static boolean isInitialized = false;
	private static Identity ident1, ident2, ident3;
	private static Identity[] identities;
	private final static String identityTest1Name = "identityTest1";
	private final static String identityTest2Name = "identityTest2";
	private final static String identityTest3Name = "identityTest3";
	
	private final static String subPath1 = "sub-path-1";
	private final static String subPath2 = "sub-path-2";
	private final static String subPath3 = "sub-path-3";
	private final static String subPath4 = "sub-path-4";
	private final static String[] subPaths = {subPath1, subPath2, subPath3, subPath4};

	private static final OLATResourceable ores = OresHelper.createOLATResourceableInstance("testresource", Long.valueOf(1234l));
	private static MarkingService service;
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before
	public void setUp()throws Exception {
		if (isInitialized == false) {
			ident1 = JunitTestHelper.createAndPersistIdentityAsUser(identityTest1Name);
			ident2 = JunitTestHelper.createAndPersistIdentityAsUser(identityTest2Name);
			ident3 = JunitTestHelper.createAndPersistIdentityAsUser(identityTest3Name);
			DBFactory.getInstance().closeSession();
			identities = new Identity[]{ident1, ident2, ident3};
			//		
			service = (MarkingService) CoreSpringFactory.getBean(MarkingService.class);
			//
			isInitialized = true;
		}
	}
	
	@Test 
	public void testSetMark() {
		for(Identity ident:identities) {
			Mark mark = service.getMarkManager().setMark(ores, ident, subPath1, "");
			assertEquals(ident, mark.getCreator());
			assertEquals(subPath1, mark.getResSubPath());
			assertEquals(ores.getResourceableTypeName(), mark.getOLATResourceable().getResourceableTypeName());
			assertEquals(ores.getResourceableId(), mark.getOLATResourceable().getResourceableId());
			boolean marked = service.getMarkManager().isMarked(ores, ident, subPath1);
			assertTrue(marked);
		}
	}
	
	@Test
	public void testRemoveMark() {
		for(Identity ident:identities) {
			service.getMarkManager().setMark(ores, ident, subPath1, "");
		}
		service.getMarkManager().removeMark(ores, ident1, subPath1);
		boolean markedAfterRemove = service.getMarkManager().isMarked(ores, ident1, subPath1);
		assertEquals(markedAfterRemove, false);
		boolean marked = service.getMarkManager().isMarked(ores, ident2, subPath1);
		assertTrue(marked);
	}
	
	@Test
	public void testRemoveResource() {
		for(Identity ident:identities) {
			for(String subPath:subPaths) {
				service.getMarkManager().setMark(ores, ident, subPath, "");
			}
		}
		
		service.getMarkManager().deleteMarks(ores);

		boolean marked = false;
		for(Identity ident:identities) {
			for(String subPath:subPaths) {
				marked |= service.getMarkManager().isMarked(ores, ident, subPath);
			}
		}
		assertFalse(marked);
	}
	
	@Test
	public void testIdentityStats() {
		for(String subPath:subPaths) {
			if(subPath.equals(subPath3)) continue;
			service.getMarkManager().setMark(ores, ident1, subPath, "");
		}
		
		List<String> subPathList = Arrays.asList(subPaths);
		List<MarkResourceStat> stats = service.getMarkManager().getStats(ores, subPathList, ident1);
		assertEquals(3, stats.size());
		
		for(MarkResourceStat stat:stats) {
			assertEquals(1, stat.getCount());
		}
	}
	
	@Test
	public void testStats() {
		for(Identity ident:identities) {
			for(String subPath:subPaths) {
				if(subPath.equals(subPath3)) continue;
				service.getMarkManager().setMark(ores, ident, subPath, "");
			}
		}
		
		List<String> subPathList = Arrays.asList(subPath1,subPath2,subPath3);
		List<MarkResourceStat> stats = service.getMarkManager().getStats(ores, subPathList, null);
		assertEquals(2, stats.size());
		
		for(MarkResourceStat stat:stats) {
			assertEquals(3, stat.getCount());
		}
	}
	
	/*
	@Test
	public void testHeavyLoadStats() {
		List<Identity> loadIdentities = CoreSpringFactory.getImpl(BaseSecurity.class)
				.getVisibleIdentitiesByPowerSearch(null, null, false, null, null, null, null, null, 0, -1);

		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters();
		params.setRoles(new Roles(true, false, false, false, false, false, false));
		List<RepositoryEntry> loadOres = RepositoryManager.getInstance().genericANDQueryWithRolesRestriction(params, 0, -1, false);
		
		
		int count = 0;
		for(Identity ident:loadIdentities) {

			double r = Math.random() * loadOres.size();
			int pos = (int)Math.round(r);
			List<RepositoryEntry> toBookmarks = loadOres.subList(pos, Math.min(pos + 10, loadOres.size() - 1));
			
			for(RepositoryEntry toBookmark:toBookmarks) {
				service.getMarkManager().setMark(toBookmark, ident, null, "");
				if(++count % 20 == 0) {
					DBFactory.getInstance().intermediateCommit();
				}
			}

		}

	}
	*/
}