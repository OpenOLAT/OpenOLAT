/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
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
package org.olat.modules.reminder.rule;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntryStatusRuleSPITest extends OlatTestCase {
	
	@Autowired
	private RepositoryEntryStatusRuleSPI sut;

	@Test
	public void shouldEvaluateStatus() {
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.preparation, RepositoryEntryStatusEnum.preparation)).isTrue();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.preparation, RepositoryEntryStatusEnum.review)).isFalse();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.preparation, RepositoryEntryStatusEnum.coachpublished)).isFalse();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.preparation, RepositoryEntryStatusEnum.published)).isFalse();

		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.review, RepositoryEntryStatusEnum.preparation)).isFalse();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.review, RepositoryEntryStatusEnum.review)).isTrue();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.review, RepositoryEntryStatusEnum.coachpublished)).isFalse();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.review, RepositoryEntryStatusEnum.published)).isFalse();

		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.coachpublished, RepositoryEntryStatusEnum.preparation)).isFalse();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.coachpublished, RepositoryEntryStatusEnum.review)).isFalse();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.coachpublished, RepositoryEntryStatusEnum.coachpublished)).isTrue();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.coachpublished, RepositoryEntryStatusEnum.published)).isFalse();

		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.published, RepositoryEntryStatusEnum.preparation)).isFalse();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.published, RepositoryEntryStatusEnum.review)).isFalse();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.published, RepositoryEntryStatusEnum.coachpublished)).isFalse();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.published, RepositoryEntryStatusEnum.published)).isTrue();

		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.notPreparation, RepositoryEntryStatusEnum.preparation)).isFalse();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.notPreparation, RepositoryEntryStatusEnum.review)).isTrue();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.notPreparation, RepositoryEntryStatusEnum.coachpublished)).isTrue();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.notPreparation, RepositoryEntryStatusEnum.published)).isTrue();

		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.notReview, RepositoryEntryStatusEnum.preparation)).isTrue();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.notReview, RepositoryEntryStatusEnum.review)).isFalse();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.notReview, RepositoryEntryStatusEnum.coachpublished)).isTrue();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.notReview, RepositoryEntryStatusEnum.published)).isTrue();

		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.notCoachpublished, RepositoryEntryStatusEnum.preparation)).isTrue();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.notCoachpublished, RepositoryEntryStatusEnum.review)).isTrue();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.notCoachpublished, RepositoryEntryStatusEnum.coachpublished)).isFalse();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.notCoachpublished, RepositoryEntryStatusEnum.published)).isTrue();

		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.notPublished, RepositoryEntryStatusEnum.preparation)).isTrue();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.notPublished, RepositoryEntryStatusEnum.review)).isTrue();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.notPublished, RepositoryEntryStatusEnum.coachpublished)).isTrue();
		assertThat(sut.evaluateStatus(RepositoryEntryStatusRuleSPI.Status.notPublished, RepositoryEntryStatusEnum.published)).isFalse();
	}

}
