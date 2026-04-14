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
package org.olat.core.commons.services.ai;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Initial date: 07.04.2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class AiUsageLogSearchParams {

	private Date createdAfter;
	private Date createdBefore;
	private List<String> aiFeatures;
	private List<AiUsageLogStatus> statuses;
	private OrderBy order;
	private boolean orderAsc;
	private List<String> orderedValues;

	public Date getCreatedAfter() {
		return createdAfter;
	}

	public void setCreatedAfter(Date createdAfter) {
		this.createdAfter = createdAfter;
	}

	public Date getCreatedBefore() {
		return createdBefore;
	}

	public void setCreatedBefore(Date createdBefore) {
		this.createdBefore = createdBefore;
	}

	public List<String> getAiFeatures() {
		return aiFeatures;
	}

	public void setAiFeatures(List<String> aiFeatures) {
		this.aiFeatures = aiFeatures;
	}

	public List<AiUsageLogStatus> getStatuses() {
		return statuses;
	}

	public void setStatuses(List<AiUsageLogStatus> statuses) {
		this.statuses = statuses;
	}

	public OrderBy getOrder() {
		return order;
	}

	public void setOrder(OrderBy order) {
		this.order = order;
	}

	public boolean isOrderAsc() {
		return orderAsc;
	}

	public void setOrderAsc(boolean orderAsc) {
		this.orderAsc = orderAsc;
	}

	public List<String> getOrderedValues() {
		return orderedValues;
	}

	public void setOrderedValues(List<String> orderedValues) {
		this.orderedValues = orderedValues;
	}

	public enum OrderBy {
		creationDate,
		aiFeature,
		usageContextType,
		usageContextId,
		resourceType,
		resourceId,
		resourceSubId,
		locale,
		durationMillis,
		status,
		errorCode,
		errorMessage,
		modelProvider,
		requestModel,
		requestTemperature,
		requestTopP,
		requestMaxOutputTokens,
		invocationId,
		serviceInterface,
		serviceMethod,
		responseId,
		responseModel,
		responseFinishReason,
		inputTokens,
		outputTokens,
		totalTokens,
		cachedInputTokens,
		reasoningTokens,
		requestNumMessages,
		requestTextLength,
		cacheCreationInputTokens;
		
		private final static Map<String, OrderBy> secureValues = List.of(values()).stream()
				.collect(Collectors.toMap(OrderBy::name, Function.identity()));

		public static final OrderBy secureValueOf(String val) {
			return secureValues.getOrDefault(val, null);
		}
	}

}
