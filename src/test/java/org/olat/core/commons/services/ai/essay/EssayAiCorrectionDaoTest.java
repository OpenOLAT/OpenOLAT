/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.ai.essay;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * DB tests for {@link EssayAiCorrectionDao}, with focus on the cleanup
 * paths: by identity (user data deletion), by storage path (QuizPart /
 * page deletion) and by question (single question removed from a quiz).
 *
 * Initial date: 2026-06-10<br>
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 */
public class EssayAiCorrectionDaoTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private EssayAiCorrectionDao correctionDao;

	@Test
	public void createAndLoad() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("ai-corr-1");
		EssayAiCorrection correction = correctionDao.create("path/quiz-1", "question-1",
				identity, 123L, "My answer text");
		dbInstance.commitAndCloseSession();

		EssayAiCorrection reloaded = correctionDao.loadByKey(correction.getKey());
		Assert.assertNotNull(reloaded);
		Assert.assertEquals(EssayAiCorrection.Status.PENDING, reloaded.getStatus());
		Assert.assertEquals("path/quiz-1", reloaded.getStoragePath());
		Assert.assertEquals("question-1", reloaded.getQuestionId());
		Assert.assertEquals("My answer text", reloaded.getStudentAnswer());
		Assert.assertEquals(Long.valueOf(123L), reloaded.getAssessmentItemSessionKey());
		Assert.assertEquals(identity.getKey(), reloaded.getIdentity().getKey());
	}

	@Test
	public void deleteByIdentity() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("ai-corr-2");
		Identity other = JunitTestHelper.createAndPersistIdentityAsRndUser("ai-corr-3");
		EssayAiCorrection mine1 = correctionDao.create("path/quiz-2", "q-1", identity, null, "answer 1");
		EssayAiCorrection mine2 = correctionDao.create("path/quiz-2", "q-2", identity, null, "answer 2");
		EssayAiCorrection others = correctionDao.create("path/quiz-2", "q-1", other, null, "answer 3");
		dbInstance.commitAndCloseSession();

		int deleted = correctionDao.deleteByIdentity(identity);
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(2, deleted);
		Assert.assertNull(correctionDao.loadByKey(mine1.getKey()));
		Assert.assertNull(correctionDao.loadByKey(mine2.getKey()));
		Assert.assertNotNull(correctionDao.loadByKey(others.getKey()));
	}

	@Test
	public void deleteByStoragePath() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("ai-corr-4");
		EssayAiCorrection inQuiz1 = correctionDao.create("path/quiz-4", "q-1", identity, null, "answer 1");
		EssayAiCorrection inQuiz2 = correctionDao.create("path/quiz-4", "q-2", identity, null, "answer 2");
		EssayAiCorrection otherQuiz = correctionDao.create("path/quiz-5", "q-1", identity, null, "answer 3");
		dbInstance.commitAndCloseSession();

		int deleted = correctionDao.deleteByStoragePath("path/quiz-4");
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(2, deleted);
		Assert.assertNull(correctionDao.loadByKey(inQuiz1.getKey()));
		Assert.assertNull(correctionDao.loadByKey(inQuiz2.getKey()));
		Assert.assertNotNull(correctionDao.loadByKey(otherQuiz.getKey()));
	}

	@Test
	public void deleteByQuestion() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("ai-corr-5");
		EssayAiCorrection q1run1 = correctionDao.create("path/quiz-6", "q-1", identity, null, "answer 1");
		EssayAiCorrection q1run2 = correctionDao.create("path/quiz-6", "q-1", identity, null, "answer 2");
		EssayAiCorrection q2 = correctionDao.create("path/quiz-6", "q-2", identity, null, "answer 3");
		dbInstance.commitAndCloseSession();

		int deleted = correctionDao.deleteByQuestion("path/quiz-6", "q-1");
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(2, deleted);
		Assert.assertNull(correctionDao.loadByKey(q1run1.getKey()));
		Assert.assertNull(correctionDao.loadByKey(q1run2.getKey()));
		Assert.assertNotNull(correctionDao.loadByKey(q2.getKey()));
	}
}
