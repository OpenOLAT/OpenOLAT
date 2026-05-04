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

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Data access for {@link EssayFeedbackJob} rows. Created on learner submit
 * from the ceditor QuizPart runtime, polled by the overlay UI, updated by
 * {@link EssayFeedbackLongRunnable} on completion.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Service
public class EssayFeedbackJobDao {

	@Autowired
	private DB dbInstance;

	public EssayFeedbackJob create(String storagePath, String questionId, Identity identity,
			Long assessmentItemSessionKey, String studentAnswer) {
		EssayFeedbackJob job = new EssayFeedbackJob();
		Date now = new Date();
		job.setCreationDate(now);
		job.setLastModified(now);
		job.setStoragePath(storagePath);
		job.setQuestionId(questionId);
		job.setIdentityKey(identity == null ? null : identity.getKey());
		job.setAssessmentItemSessionKey(assessmentItemSessionKey);
		job.setStudentAnswer(studentAnswer == null ? "" : studentAnswer);
		job.setState(EssayFeedbackJob.State.PENDING);
		dbInstance.getCurrentEntityManager().persist(job);
		return job;
	}

	public EssayFeedbackJob update(EssayFeedbackJob job) {
		job.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(job);
	}

	public EssayFeedbackJob loadByKey(Long key) {
		if (key == null) {
			return null;
		}
		return dbInstance.getCurrentEntityManager().find(EssayFeedbackJob.class, key);
	}

	public void delete(EssayFeedbackJob job) {
		if (job == null || job.getKey() == null) {
			return;
		}
		EssayFeedbackJob managed = dbInstance.getCurrentEntityManager().find(EssayFeedbackJob.class, job.getKey());
		if (managed != null) {
			dbInstance.getCurrentEntityManager().remove(managed);
		}
	}
}
