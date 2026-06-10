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

import java.util.Date;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Data access for {@link EssayAiCorrection} result rows. Created on learner
 * submit from the ceditor QuizPart runtime, polled by the overlay UI,
 * updated by {@link EssayAiCorrectionService#runCorrection(Long)} on
 * completion.
 *
 * Initial date: 2026-06-10<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Service
public class EssayAiCorrectionDao {

	@Autowired
	private DB dbInstance;

	public EssayAiCorrection create(String storagePath, String questionId, Identity identity,
			Long assessmentItemSessionKey, String studentAnswer) {
		EssayAiCorrection correction = new EssayAiCorrection();
		Date now = new Date();
		correction.setCreationDate(now);
		correction.setLastModified(now);
		correction.setStoragePath(storagePath);
		correction.setQuestionId(questionId);
		correction.setIdentity(identity);
		correction.setAssessmentItemSessionKey(assessmentItemSessionKey);
		correction.setStudentAnswer(studentAnswer == null ? "" : studentAnswer);
		correction.setStatus(EssayAiCorrection.Status.PENDING);
		dbInstance.getCurrentEntityManager().persist(correction);
		return correction;
	}

	public EssayAiCorrection update(EssayAiCorrection correction) {
		correction.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(correction);
	}

	public EssayAiCorrection loadByKey(Long key) {
		if (key == null) {
			return null;
		}
		return dbInstance.getCurrentEntityManager().find(EssayAiCorrection.class, key);
	}

	public void delete(EssayAiCorrection correction) {
		if (correction == null || correction.getKey() == null) {
			return;
		}
		EssayAiCorrection managed = dbInstance.getCurrentEntityManager()
				.find(EssayAiCorrection.class, correction.getKey());
		if (managed != null) {
			dbInstance.getCurrentEntityManager().remove(managed);
		}
	}

	/**
	 * Delete all correction results of the given user. Called from the
	 * user-data-deletion lifecycle.
	 */
	public int deleteByIdentity(IdentityRef identity) {
		if (identity == null || identity.getKey() == null) {
			return 0;
		}
		String query = "delete from aiessaycorrection correction where correction.identity.key=:identityKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}

	/**
	 * Delete all correction results of all questions stored under the given
	 * QuizPart storage path. Called when the QuizPart (or its page) is
	 * deleted.
	 */
	public int deleteByStoragePath(String storagePath) {
		if (storagePath == null) {
			return 0;
		}
		String query = "delete from aiessaycorrection correction where correction.storagePath=:storagePath";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("storagePath", storagePath)
				.executeUpdate();
	}

	/**
	 * Delete all correction results of a single question. Called when one
	 * question is removed from a QuizPart.
	 */
	public int deleteByQuestion(String storagePath, String questionId) {
		if (storagePath == null || questionId == null) {
			return 0;
		}
		String query = "delete from aiessaycorrection correction"
				+ " where correction.storagePath=:storagePath and correction.questionId=:questionId";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("storagePath", storagePath)
				.setParameter("questionId", questionId)
				.executeUpdate();
	}
}
