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

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.ai.AiUsageLog;
import org.olat.core.commons.services.ai.AiUsageLogSearchParams;
import org.olat.core.commons.services.ai.model.AiUsageLogStats;
import org.olat.core.commons.services.ai.AiUsageLogSearchParams.OrderBy;
import org.olat.core.commons.services.ai.AiUsageLogStatus;
import org.olat.core.commons.services.ai.model.AiUsageContext;
import org.olat.core.commons.services.ai.model.AiUsageLogImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Data access object for AI usage logging.
 *
 * Initial date: 31.03.2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class AiUsageLogDAO {

	@Autowired
	private DB dbInstance;

	public AiUsageLogImpl create(AiUsageLogImpl log) {
		log.setCreationDate(new Date());
		dbInstance.getCurrentEntityManager().persist(log);
		return log;
	}

	public void createErrorLog(String spiId, String modelName, String aiFeature, AiUsageContext context,
			long durationMillis, Exception error) {
		AiUsageLogImpl log = new AiUsageLogImpl();
		log.setModelProvider(spiId);
		log.setRequestModel(modelName);
		log.setAiFeature(aiFeature);
		log.setDurationMillis(durationMillis);
		log.setStatus(AiUsageLogStatus.FAILED);
		log.setErrorCode(error.getClass().getSimpleName());
		log.setErrorMessage(error.getMessage());
		if (context != null) {
			log.setUsageContextType(context.usageContextType());
			log.setUsageContextId(context.usageContextId());
			log.setIdentity(context.identity());
			log.setResourceType(context.resourceType());
			log.setResourceId(context.resourceId());
			log.setResourceSubId(context.resourceSubId());
			if (context.locale() != null) {
				log.setLocale(context.locale().toString());
			}
		}
		create(log);
	}

	public void updateInvocationFields(Long key, String invocationId, String serviceInterface, String serviceMethod) {
		if (key == null) {
			return;
		}
		AiUsageLogImpl log = dbInstance.getCurrentEntityManager().find(AiUsageLogImpl.class, key);
		if (log != null) {
			log.setInvocationId(invocationId);
			log.setServiceInterface(serviceInterface);
			log.setServiceMethod(serviceMethod);
		}
	}

	public AiUsageLogStats getStats(AiUsageLogSearchParams params) {
		QueryBuilder query = new QueryBuilder();
		query.append("select coalesce(sum(log.totalTokens), 0) from aiusagelog log");
		appendFilters(query, params);
		TypedQuery<Long> dbQuery = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Long.class);
		applyParameters(dbQuery, params);
		long totalTokens = dbQuery.getSingleResult();
		return new AiUsageLogStats(totalTokens);
	}

	public int getCount(AiUsageLogSearchParams params) {
		QueryBuilder query = new QueryBuilder();
		query.append("select count(log) from aiusagelog log");
		appendFilters(query, params);
		TypedQuery<Long> dbQuery = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Long.class);
		applyParameters(dbQuery, params);
		return dbQuery.getSingleResult().intValue();
	}

	public List<AiUsageLog> getUsageLogs(AiUsageLogSearchParams params, int firstResult, int maxResults) {
		QueryBuilder query = new QueryBuilder();
		query.append("select log from aiusagelog log");
		query.append(" left join fetch log.identity identity");
		query.append(" left join fetch identity.user");
		appendFilters(query, params);
		if (params.getOrder() != null) {
			appendOrderBy(query, params.getOrder(), params.isOrderAsc(), params.getOrderedValues());
		}
		TypedQuery<AiUsageLog> dbQuery = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), AiUsageLog.class);
		applyParameters(dbQuery, params);
		if (maxResults >= 0) {
			dbQuery.setMaxResults(maxResults);
		}
		return dbQuery
				.setFirstResult(firstResult)
				.getResultList();
	}

	private void appendFilters(QueryBuilder query, AiUsageLogSearchParams params) {
		if (params.getCreatedAfter() != null) {
			query.and().append("log.creationDate >= :createdAfter");
		}
		if (params.getCreatedBefore() != null) {
			query.and().append("log.creationDate < :createdBefore");
		}
		if (params.getAiFeatures() != null && !params.getAiFeatures().isEmpty()) {
			query.and().append("log.aiFeature in :aiFeatures");
		}
		if (params.getStatuses() != null && !params.getStatuses().isEmpty()) {
			query.and().append("log.status in :statuses");
		}
	}

	private void applyParameters(TypedQuery<?> dbQuery, AiUsageLogSearchParams params) {
		if (params.getCreatedAfter() != null) {
			dbQuery.setParameter("createdAfter", params.getCreatedAfter());
		}
		if (params.getCreatedBefore() != null) {
			dbQuery.setParameter("createdBefore", params.getCreatedBefore());
		}
		if (params.getAiFeatures() != null && !params.getAiFeatures().isEmpty()) {
			dbQuery.setParameter("aiFeatures", params.getAiFeatures());
		}
		if (params.getStatuses() != null && !params.getStatuses().isEmpty()) {
			dbQuery.setParameter("statuses", params.getStatuses());
		}
	}

	private void appendOrderBy(QueryBuilder sb, OrderBy orderBy, boolean asc, List<String> orderedValues) {
		switch (orderBy) {
			case creationDate:
				sb.append(" order by log.creationDate ").append("asc", "desc", asc);
				break;
			case aiFeature:
				if (orderedValues != null && !orderedValues.isEmpty()) {
					sb.append(" order by case");
					for (int i = 0; i < orderedValues.size(); i++) {
						String value = orderedValues.get(i);
						sb.append(" when log.aiFeature = '").append(value).append("' then ").append(i);
					}
					sb.append(" end ").append("asc", "desc", asc);
				} else {
					sb.append(" order by log.aiFeature ").append("asc", "desc", asc).append(" nulls last");
				}
				sb.append(" , log.creationDate desc");
				break;
			case resourceId:
				sb.append(" order by log.resourceId ").append("asc", "desc", asc).append(" nulls last");
				break;
			case resourceSubId:
				sb.append(" order by lower(log.resourceSubId) ").append("asc", "desc", asc).append(" nulls last");
				break;
			case locale:
				sb.append(" order by lower(log.locale) ").append("asc", "desc", asc).append(" nulls last");
				break;
			case durationMillis:
				sb.append(" order by log.durationMillis ").append("asc", "desc", asc).append(" nulls last");
				break;
			case status:
				sb.append(" order by lower(log.status) ").append("asc", "desc", asc).append(" nulls last");
				break;
			case errorCode:
				sb.append(" order by lower(log.errorCode) ").append("asc", "desc", asc).append(" nulls last");
				break;
			case errorMessage:
				sb.append(" order by lower(log.errorMessage) ").append("asc", "desc", asc).append(" nulls last");
				break;
			case modelProvider:
				sb.append(" order by lower(log.modelProvider) ").append("asc", "desc", asc).append(" nulls last");
				break;
			case requestModel:
				sb.append(" order by lower(log.requestModel) ").append("asc", "desc", asc).append(" nulls last");
				break;
			case requestTemperature:
				sb.append(" order by log.requestTemperature ").append("asc", "desc", asc).append(" nulls last");
				break;
			case requestTopP:
				sb.append(" order by log.requestTopP ").append("asc", "desc", asc).append(" nulls last");
				break;
			case requestMaxOutputTokens:
				sb.append(" order by log.requestMaxOutputTokens ").append("asc", "desc", asc).append(" nulls last");
				break;
			case invocationId:
				sb.append(" order by lower(log.invocationId) ").append("asc", "desc", asc).append(" nulls last");
				break;
			case serviceInterface:
				sb.append(" order by lower(log.serviceInterface) ").append("asc", "desc", asc).append(" nulls last");
				break;
			case serviceMethod:
				sb.append(" order by lower(log.serviceMethod) ").append("asc", "desc", asc).append(" nulls last");
				break;
			case responseId:
				sb.append(" order by lower(log.responseId) ").append("asc", "desc", asc).append(" nulls last");
				break;
			case responseModel:
				sb.append(" order by lower(log.responseModel) ").append("asc", "desc", asc).append(" nulls last");
				break;
			case responseFinishReason:
				sb.append(" order by lower(log.responseFinishReason) ").append("asc", "desc", asc).append(" nulls last");
				break;
			case inputTokens:
				sb.append(" order by log.inputTokens ").append("asc", "desc", asc).append(" nulls last");
				break;
			case outputTokens:
				sb.append(" order by log.outputTokens ").append("asc", "desc", asc).append(" nulls last");
				break;
			case totalTokens:
				sb.append(" order by log.totalTokens ").append("asc", "desc", asc).append(" nulls last");
				break;
			case cachedInputTokens:
				sb.append(" order by log.cachedInputTokens ").append("asc", "desc", asc).append(" nulls last");
				break;
			case reasoningTokens:
				sb.append(" order by log.reasoningTokens ").append("asc", "desc", asc).append(" nulls last");
				break;
			case requestNumMessages:
				sb.append(" order by log.requestNumMessages ").append("asc", "desc", asc).append(" nulls last");
				break;
			case requestTextLength:
				sb.append(" order by log.requestTextLength ").append("asc", "desc", asc).append(" nulls last");
				break;
			case cacheCreationInputTokens:
				sb.append(" order by log.cacheCreationInputTokens ").append("asc", "desc", asc).append(" nulls last");
				break;
			default: break;
		}
	}

}
