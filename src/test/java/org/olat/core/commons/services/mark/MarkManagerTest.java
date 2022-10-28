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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Test for the marking service
 * 
 * <P>
 * Initial Date:  8 juin 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class MarkManagerTest extends OlatTestCase {
	
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
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MarkManager markManager;
	
	@Before
	public void setUp()throws Exception {
		if (isInitialized == false) {
			ident1 = JunitTestHelper.createAndPersistIdentityAsUser(identityTest1Name);
			ident2 = JunitTestHelper.createAndPersistIdentityAsUser(identityTest2Name);
			ident3 = JunitTestHelper.createAndPersistIdentityAsUser(identityTest3Name);
			dbInstance.closeSession();
			identities = new Identity[]{ident1, ident2, ident3};
			isInitialized = true;
		}
	}
	
	@Test
	public void getMarks() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mark-1");
		Mark mark = markManager.setMark(ores, id, "AC-234", "B-Path");
		dbInstance.commitAndCloseSession();
		
		List<Mark> marks = markManager.getMarks(ores, id, null);
		assertThat(marks)
			.hasSize(1)
			.containsExactly(mark);
	}
	
	@Test
	public void getMarksWithSubPath() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mark-2");
		Mark mark = markManager.setMark(ores, id, "AC-235", "B-Path");
		dbInstance.commitAndCloseSession();
		
		List<Mark> marks = markManager.getMarks(ores, id, List.of("AC-234", "AC-235"));
		assertThat(marks)
			.hasSize(1)
			.containsExactly(mark);
	}
	
	@Test
	public void getMarksResourceId() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mark-3");
		Mark mark = markManager.setMark(ores, id, "AC-236", "B-Path");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mark);
		
		List<Long> resourceIds = markManager.getMarksResourceId(id, ores.getResourceableTypeName());
		assertThat(resourceIds)
			.hasSize(1)
			.containsExactly(ores.getResourceableId());
	}
	
	@Test
	public void getMarkResourceIds() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mark-4");
		Mark mark = markManager.setMark(ores, id, "AC-238", "B-Path");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mark);
		
		Set<Long> resourceIds = markManager.getMarkResourceIds(id, ores.getResourceableTypeName(), List.of());
		assertThat(resourceIds)
			.hasSize(1)
			.containsExactly(ores.getResourceableId());
	}
	
	@Test
	public void getMarkResourceIdsWithSubPath() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mark-5");
		Mark mark = markManager.setMark(ores, id, "AC-239", "B-Path");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mark);
		
		Set<Long> resourceIds = markManager.getMarkResourceIds(id, ores.getResourceableTypeName(), List.of("AC-239"));
		assertThat(resourceIds)
			.hasSize(1)
			.containsExactly(ores.getResourceableId());
	}
	
	@Test
	public void getMarksByType() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mark-6");
		Mark mark = markManager.setMark(ores, id, "AC-240", "B-Path");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mark);
		
		List<Mark> resourceIds = markManager.getMarks(id, List.of(ores.getResourceableTypeName()));
		assertThat(resourceIds)
			.hasSize(1)
			.containsExactly(mark);
	}
	
	@Test 
	public void testSetMark() {
		for(Identity ident:identities) {
			Mark mark = markManager.setMark(ores, ident, subPath1, "");
			assertEquals(ident, mark.getCreator());
			assertEquals(subPath1, mark.getResSubPath());
			assertEquals(ores.getResourceableTypeName(), mark.getOLATResourceable().getResourceableTypeName());
			assertEquals(ores.getResourceableId(), mark.getOLATResourceable().getResourceableId());
			boolean marked = markManager.isMarked(ores, ident, subPath1);
			assertTrue(marked);
		}
	}
	
	@Test
	public void testRemoveMark() {
		for(Identity ident:identities) {
			markManager.setMark(ores, ident, subPath1, "");
		}
		markManager.removeMark(ores, ident1, subPath1);
		boolean markedAfterRemove = markManager.isMarked(ores, ident1, subPath1);
		assertEquals(markedAfterRemove, false);
		boolean marked = markManager.isMarked(ores, ident2, subPath1);
		assertTrue(marked);
	}
	
	@Test
	public void testRemoveResource() {
		for(Identity ident:identities) {
			for(String subPath:subPaths) {
				markManager.setMark(ores, ident, subPath, "");
			}
		}
		
		markManager.deleteMarks(ores);

		boolean marked = false;
		for(Identity ident:identities) {
			for(String subPath:subPaths) {
				marked |= markManager.isMarked(ores, ident, subPath);
			}
		}
		assertFalse(marked);
	}
	
	@Test
	public void testIdentityStats() {
		for(String subPath:subPaths) {
			if(subPath.equals(subPath3)) continue;
			markManager.setMark(ores, ident1, subPath, "");
		}
		
		List<String> subPathList = Arrays.asList(subPaths);
		List<MarkResourceStat> stats = markManager.getStats(ores, subPathList, ident1);
		assertEquals(3, stats.size());
		
		for(MarkResourceStat stat:stats) {
			assertEquals(1, stat.getCount());
		}
	}
	
	@Test
	public void getStats() {
		for(Identity ident:identities) {
			for(String subPath:subPaths) {
				if(subPath.equals(subPath3)) continue;
				markManager.setMark(ores, ident, subPath, "");
			}
		}
		
		List<String> subPathList = Arrays.asList(subPath1,subPath2,subPath3);
 		List<MarkResourceStat> stats = markManager.getStats(ores, subPathList, null);
		assertEquals(2, stats.size());
		
		for(MarkResourceStat stat:stats) {
			assertEquals(3, stat.getCount());
		}
	}
	
	@Test
	public void getStatsIdentityOnly() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mark-6");
		OLATResourceable testOres = OresHelper.createOLATResourceableInstance("testresource", CodeHelper.getRAMUniqueID());
		markManager.setMark(testOres, id, "Path only", "");
		
 		List<MarkResourceStat> stats = markManager.getStats(testOres, null, id);
		assertThat(stats)
			.hasSize(1);
	}
	
	/**
	 * Check only the syntax of the query.
	 */
	@Test
	public void getStatsWithoutSubPath() {
 		List<MarkResourceStat> stats = markManager.getStats(ores, null, null);
		Assert.assertNotNull(stats);
	}
	
	
	
}