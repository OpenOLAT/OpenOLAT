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
package org.olat.course.nodes.practice.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.practice.PracticeAssessmentItemGlobalRef;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeAssessmentItemGlobalRefDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PracticeAssessmentItemGlobalRefDAO practiceAssessmentItemGlobalRefDao;
	
	@Test
	public void createAssessmentItemGlobalRef() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("global-ref-1");
		String uuid = UUID.randomUUID().toString();
		
		PracticeAssessmentItemGlobalRef globalRef = practiceAssessmentItemGlobalRefDao.createAssessmentItemGlobalRefDAO(identity, uuid);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(globalRef);
		Assert.assertNotNull(globalRef.getKey());
		Assert.assertNotNull(globalRef.getCreationDate());
		Assert.assertNotNull(globalRef.getLastModified());
		Assert.assertEquals(uuid, globalRef.getIdentifier());
		Assert.assertEquals(identity, globalRef.getIdentity());
		Assert.assertEquals(0, globalRef.getLevel());
		Assert.assertEquals(0, globalRef.getAttempts());
		Assert.assertEquals(0, globalRef.getCorrectAnswers());
		Assert.assertEquals(0, globalRef.getIncorrectAnswers());
	}
	
	@Test
	public void getAssessmentItemGlobalRefByUuids() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("global-ref-1");
		String uuid = UUID.randomUUID().toString();
		String ref1 = uuid + "-1";
		String ref2 = uuid + "-2";
		
		PracticeAssessmentItemGlobalRef globalRef1 = practiceAssessmentItemGlobalRefDao
				.createAssessmentItemGlobalRefDAO(identity, ref1);
		PracticeAssessmentItemGlobalRef globalRef2 = practiceAssessmentItemGlobalRefDao
				.createAssessmentItemGlobalRefDAO(identity, ref2);
		dbInstance.commitAndCloseSession();
		
		List<String> uuids = List.of(ref1, ref2);
		List<PracticeAssessmentItemGlobalRef> globalRefs = practiceAssessmentItemGlobalRefDao
				.getAssessmentItemGlobalRefByUuids(identity, uuids, false);
		assertThat(globalRefs)
			.isNotNull()
			.isNotEmpty()
			.containsExactlyInAnyOrder(globalRef1, globalRef2);
		
	}

}
