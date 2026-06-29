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
package org.olat.core.commons.services.ai.manager;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.ai.AiFeature;
import org.olat.core.commons.services.ai.AiUsageLog;
import org.olat.core.commons.services.ai.AiUsageLogSearchParams;
import org.olat.core.commons.services.ai.AiUsageLogStatus;
import org.olat.core.commons.services.ai.model.AiUsageLogImpl;
import org.olat.core.commons.services.ai.model.AiUsageLogStats;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

public class AiUsageLogDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private AiUsageLogDAO aiUsageLogDAO;

	@Test
	public void createLogEntry_success() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("ai-usage-log-1");

		AiUsageLogImpl log = new AiUsageLogImpl();
		log.setIdentity(identity);
		log.setAiFeature("test-feature");
		log.setUsageContextId(UUID.randomUUID().toString());
		log.setResourceType("QTI-Item");
		log.setResourceId(123L);
		log.setResourceSubId("resource-sub-123");
		log.setStatus(AiUsageLogStatus.SUCCESS);
		log.setModelProvider("openai");
		log.setRequestModel("gpt-4");
		log.setRequestTemperature(0.5);
		log.setRequestTopP(0.9);
		log.setRequestMaxOutputTokens(4000L);
		log.setResponseId("response-123");
		log.setResponseModel("gpt-4");
		log.setResponseFinishReason("STOP");
		log.setInputTokens(100L);
		log.setOutputTokens(50L);
		log.setTotalTokens(150L);
		log.setDurationMillis(1000L);
		log.setRequestNumMessages(2L);
		log.setRequestTextLength(512L);
		log.setCacheCreationInputTokens(10L);

		AiUsageLogImpl created = aiUsageLogDAO.create(log);
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(created);
		Assert.assertNotNull(created.getKey());
		Assert.assertNotNull(created.getCreationDate());
		Assert.assertEquals(identity.getKey(), created.getIdentity().getKey());
		Assert.assertEquals("test-feature", created.getAiFeature());
		Assert.assertEquals(AiUsageLogStatus.SUCCESS, created.getStatus());
		Assert.assertEquals(Long.valueOf(100L), created.getInputTokens());
		Assert.assertEquals(Long.valueOf(2L), created.getRequestNumMessages());
		Assert.assertEquals(Long.valueOf(512L), created.getRequestTextLength());
		Assert.assertEquals(Long.valueOf(10L), created.getCacheCreationInputTokens());
	}

	@Test
	public void createLogEntry_nullIdentity() {
		AiUsageLogImpl log = new AiUsageLogImpl();
		log.setAiFeature("test-feature");
		log.setUsageContextId(UUID.randomUUID().toString());
		log.setStatus(AiUsageLogStatus.SUCCESS);
		log.setDurationMillis(500L);

		AiUsageLogImpl created = aiUsageLogDAO.create(log);
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(created);
		Assert.assertNotNull(created.getKey());
		Assert.assertNull(created.getIdentity());
		Assert.assertEquals(AiUsageLogStatus.SUCCESS, created.getStatus());
	}

	@Test
	public void createLogEntry_failed() {
		AiUsageLogImpl log = new AiUsageLogImpl();
		log.setAiFeature("test-feature");
		log.setUsageContextId(UUID.randomUUID().toString());
		log.setStatus(AiUsageLogStatus.FAILED);
		log.setErrorCode("ApiException");
		log.setErrorMessage("API connection failed");
		log.setDurationMillis(2000L);

		AiUsageLogImpl created = aiUsageLogDAO.create(log);
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(created);
		Assert.assertNotNull(created.getKey());
		Assert.assertEquals(AiUsageLogStatus.FAILED, created.getStatus());
		Assert.assertEquals("ApiException", created.getErrorCode());
		Assert.assertEquals("API connection failed", created.getErrorMessage());
	}

	@Test
	public void getCount_createdAfter() {
		String uniqueFeature = UUID.randomUUID().toString();
		AiUsageLogImpl log = createMinimalLog();
		log.setAiFeature(uniqueFeature);
		aiUsageLogDAO.create(log);
		dbInstance.commitAndCloseSession();

		AiUsageLogSearchParams params = new AiUsageLogSearchParams();
		params.setAiFeatures(List.of(uniqueFeature));
		params.setCreatedAfter(DateUtils.addDays(new Date(), -1));
		int count = aiUsageLogDAO.getCount(params);
		Assert.assertTrue(count >= 1);

		params.setCreatedAfter(DateUtils.addDays(new Date(), 1));
		count = aiUsageLogDAO.getCount(params);
		Assert.assertEquals(0, count);
	}

	@Test
	public void getCount_createdBefore() {
		String uniqueFeature = UUID.randomUUID().toString();
		AiUsageLogImpl log = createMinimalLog();
		log.setAiFeature(uniqueFeature);
		aiUsageLogDAO.create(log);
		dbInstance.commitAndCloseSession();

		AiUsageLogSearchParams params = new AiUsageLogSearchParams();
		params.setAiFeatures(List.of(uniqueFeature));
		params.setCreatedBefore(DateUtils.addDays(new Date(), 1));
		int count = aiUsageLogDAO.getCount(params);
		Assert.assertTrue(count >= 1);

		params.setCreatedBefore(DateUtils.addDays(new Date(), -1));
		count = aiUsageLogDAO.getCount(params);
		Assert.assertEquals(0, count);
	}

	@Test
	public void getResults_createdAfter() {
		AiUsageLogImpl log = createMinimalLog();
		AiUsageLogImpl created = aiUsageLogDAO.create(log);
		dbInstance.commitAndCloseSession();

		AiUsageLogSearchParams params = new AiUsageLogSearchParams();
		params.setCreatedAfter(DateUtils.addDays(new Date(), -1));
		List<AiUsageLog> results = aiUsageLogDAO.getUsageLogs(params, 0, -1);
		Assert.assertTrue(results.stream().anyMatch(r -> r.getKey().equals(created.getKey())));

		params.setCreatedAfter(DateUtils.addDays(new Date(), 1));
		results = aiUsageLogDAO.getUsageLogs(params, 0, -1);
		Assert.assertTrue(results.stream().noneMatch(r -> r.getKey().equals(created.getKey())));
	}

	@Test
	public void getResults_createdBefore() {
		AiUsageLogImpl log = createMinimalLog();
		AiUsageLogImpl created = aiUsageLogDAO.create(log);
		dbInstance.commitAndCloseSession();

		AiUsageLogSearchParams params = new AiUsageLogSearchParams();
		params.setCreatedBefore(DateUtils.addDays(new Date(), 1));
		List<AiUsageLog> results = aiUsageLogDAO.getUsageLogs(params, 0, -1);
		Assert.assertTrue(results.stream().anyMatch(r -> r.getKey().equals(created.getKey())));

		params.setCreatedBefore(DateUtils.addDays(new Date(), -1));
		results = aiUsageLogDAO.getUsageLogs(params, 0, -1);
		Assert.assertTrue(results.stream().noneMatch(r -> r.getKey().equals(created.getKey())));
	}

	private AiUsageLogImpl createMinimalLog() {
		AiUsageLogImpl log = new AiUsageLogImpl();
		log.setAiFeature("test-feature");
		log.setUsageContextId(UUID.randomUUID().toString());
		log.setStatus(AiUsageLogStatus.SUCCESS);
		log.setDurationMillis(100L);
		return log;
	}

	@Test
	public void getCount_aiFeatures() {
		AiUsageLogImpl logMc = createMinimalLog();
		logMc.setAiFeature(AiFeature.MCQuestionGenerator.getType());
		aiUsageLogDAO.create(logMc);
		AiUsageLogImpl logImg = createMinimalLog();
		logImg.setAiFeature(AiFeature.ImageDescriptionGenerator.getType());
		aiUsageLogDAO.create(logImg);
		dbInstance.commitAndCloseSession();

		AiUsageLogSearchParams params = new AiUsageLogSearchParams();
		params.setAiFeatures(List.of(AiFeature.MCQuestionGenerator.getType()));
		int count = aiUsageLogDAO.getCount(params);
		Assertions.assertThat(count).isGreaterThanOrEqualTo(1);

		params.setAiFeatures(List.of("unknown-feature"));
		count = aiUsageLogDAO.getCount(params);
		Assertions.assertThat(count).isEqualTo(0);
	}

	@Test
	public void getCount_statuses() {
		AiUsageLogImpl logSuccess = createMinimalLog();
		logSuccess.setStatus(AiUsageLogStatus.SUCCESS);
		aiUsageLogDAO.create(logSuccess);
		AiUsageLogImpl logFailed = createMinimalLog();
		logFailed.setStatus(AiUsageLogStatus.FAILED);
		aiUsageLogDAO.create(logFailed);
		dbInstance.commitAndCloseSession();

		AiUsageLogSearchParams params = new AiUsageLogSearchParams();
		params.setStatuses(List.of(AiUsageLogStatus.SUCCESS));
		int count = aiUsageLogDAO.getCount(params);
		Assertions.assertThat(count).isGreaterThanOrEqualTo(1);

		params.setStatuses(List.of(AiUsageLogStatus.FAILED));
		count = aiUsageLogDAO.getCount(params);
		Assertions.assertThat(count).isGreaterThanOrEqualTo(1);
	}

	@Test
	public void getResults_aiFeatures() {
		AiUsageLogImpl logMc = createMinimalLog();
		logMc.setAiFeature(AiFeature.MCQuestionGenerator.getType());
		AiUsageLogImpl createdMc = aiUsageLogDAO.create(logMc);
		AiUsageLogImpl logImg = createMinimalLog();
		logImg.setAiFeature(AiFeature.ImageDescriptionGenerator.getType());
		AiUsageLogImpl createdImg = aiUsageLogDAO.create(logImg);
		dbInstance.commitAndCloseSession();

		AiUsageLogSearchParams params = new AiUsageLogSearchParams();
		params.setAiFeatures(List.of(AiFeature.MCQuestionGenerator.getType()));
		List<AiUsageLog> results = aiUsageLogDAO.getUsageLogs(params, 0, 1000);
		Assertions.assertThat(results).anyMatch(r -> r.getKey().equals(createdMc.getKey()));
		Assertions.assertThat(results).noneMatch(r -> r.getKey().equals(createdImg.getKey()));
	}

	@Test
	public void getResults_statuses() {
		AiUsageLogImpl logSuccess = createMinimalLog();
		logSuccess.setStatus(AiUsageLogStatus.SUCCESS);
		AiUsageLogImpl createdSuccess = aiUsageLogDAO.create(logSuccess);
		AiUsageLogImpl logFailed = createMinimalLog();
		logFailed.setStatus(AiUsageLogStatus.FAILED);
		AiUsageLogImpl createdFailed = aiUsageLogDAO.create(logFailed);
		dbInstance.commitAndCloseSession();

		AiUsageLogSearchParams params = new AiUsageLogSearchParams();
		params.setStatuses(List.of(AiUsageLogStatus.SUCCESS));
		List<AiUsageLog> results = aiUsageLogDAO.getUsageLogs(params, 0, 1000);
		Assertions.assertThat(results).anyMatch(r -> r.getKey().equals(createdSuccess.getKey()));
		Assertions.assertThat(results).noneMatch(r -> r.getKey().equals(createdFailed.getKey()));

		params.setStatuses(List.of(AiUsageLogStatus.FAILED));
		results = aiUsageLogDAO.getUsageLogs(params, 0, 1000);
		Assertions.assertThat(results).anyMatch(r -> r.getKey().equals(createdFailed.getKey()));
		Assertions.assertThat(results).noneMatch(r -> r.getKey().equals(createdSuccess.getKey()));
	}

	@Test
	public void getStats_totalTokens() {
		String uniqueFeature = UUID.randomUUID().toString();
		AiUsageLogImpl log1 = createMinimalLog();
		log1.setAiFeature(uniqueFeature);
		log1.setTotalTokens(100L);
		aiUsageLogDAO.create(log1);
		AiUsageLogImpl log2 = createMinimalLog();
		log2.setAiFeature(uniqueFeature);
		log2.setTotalTokens(250L);
		aiUsageLogDAO.create(log2);
		AiUsageLogImpl log3 = createMinimalLog();
		log3.setAiFeature(uniqueFeature);
		aiUsageLogDAO.create(log3);
		dbInstance.commitAndCloseSession();

		AiUsageLogSearchParams params = new AiUsageLogSearchParams();
		params.setAiFeatures(List.of(uniqueFeature));
		AiUsageLogStats stats = aiUsageLogDAO.getStats(params);

		Assertions.assertThat(stats.getTotalTokens()).isEqualTo(350L);
	}

	@Test
	public void updateInvocationFields() {
		AiUsageLogImpl log = new AiUsageLogImpl();
		log.setAiFeature("test-feature");
		log.setStatus(AiUsageLogStatus.SUCCESS);
		AiUsageLogImpl created = aiUsageLogDAO.create(log);
		dbInstance.commitAndCloseSession();

		aiUsageLogDAO.updateInvocationFields(created.getKey(), "inv-123", "MCQuestionAiService", "generateQuestions");
		dbInstance.commitAndCloseSession();

		AiUsageLogImpl reloaded = dbInstance.getCurrentEntityManager().find(AiUsageLogImpl.class, created.getKey());
		Assert.assertEquals("inv-123", reloaded.getInvocationId());
		Assert.assertEquals("MCQuestionAiService", reloaded.getServiceInterface());
		Assert.assertEquals("generateQuestions", reloaded.getServiceMethod());
	}
}
