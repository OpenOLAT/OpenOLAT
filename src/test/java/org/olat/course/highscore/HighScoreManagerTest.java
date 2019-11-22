/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.course.highscore;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
/**
 * Description:<br>
 * HighScoreManagerTest
 * Initial Date:  26.08.2016 <br>
 * @author fkiefer
 */
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.highscore.manager.HighScoreManager;
import org.olat.course.highscore.ui.HighScoreTableEntry;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

public class HighScoreManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private HighScoreManager highScoreManager;
	@Autowired
	private AssessmentEntryDAO courseNodeAssessmentDao;

	@Test
	public void highscoreTest() {
		List<AssessmentEntry> assessEntries = new ArrayList<>();
		int[] scores = {1,23,10};
		Identity assessedIdentity = null;
		//Create entries, add to List
		for (int i = 0; i < scores.length; i++) {
			assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-2");
			RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
			String subIdent = UUID.randomUUID().toString();
			AssessmentEntry nodeAssessment = courseNodeAssessmentDao
					.createAssessmentEntry(assessedIdentity, subIdent, entry, subIdent, null, entry);
			nodeAssessment.setScore(new BigDecimal(scores[i]));
			dbInstance.commitAndCloseSession();
			AssessmentEntry reloadedAssessment = courseNodeAssessmentDao.loadAssessmentEntryById(nodeAssessment.getKey());
			assessEntries.add(reloadedAssessment);
		}

		List<Integer> ownIdIndices = new ArrayList<>();
		List<HighScoreTableEntry> allMembers = new ArrayList<>();
		List<HighScoreTableEntry> ownIdMembers = new ArrayList<>();
		List<List<HighScoreTableEntry>> allPodium = new ArrayList<>();
		allPodium.add(new ArrayList<>());
		allPodium.add(new ArrayList<>());
		allPodium.add(new ArrayList<>());
		
		double[] allScores = highScoreManager.sortRankByScore(assessEntries, allMembers, ownIdMembers, allPodium,
				ownIdIndices, 5, JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-2"))
				.getScores();
		
		assertNotNull(allScores);
		assertEquals(allMembers.size(), scores.length);
		assertTrue(allScores[0] > 20);
		assertTrue(allScores[2] < 10);
		
		double[] histogramData = highScoreManager.processHistogramData(allScores, 0F, 30F).getModifiedScores();
		assertNotNull(histogramData);

		long classwidth = highScoreManager.processHistogramData(allScores, 0F, 30F).getClasswidth();
		assertEquals(2L, classwidth);
	}
}