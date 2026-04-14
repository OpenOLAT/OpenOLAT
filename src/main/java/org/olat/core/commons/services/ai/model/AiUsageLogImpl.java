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
package org.olat.core.commons.services.ai.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.commons.services.ai.AiUsageLog;
import org.olat.core.commons.services.ai.AiUsageLogStatus;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;

@Entity(name = "aiusagelog")
@Table(name = "o_ai_usage_log")
public class AiUsageLogImpl implements AiUsageLog, Persistable {

	private static final long serialVersionUID = -5654494497327950164L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, insertable = true, updatable = false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creationdate", nullable = false, insertable = true, updatable = false)
	private Date creationDate;

	@Column(name = "a_usage_context_type", nullable = true, insertable = true, updatable = false)
	private String usageContextType;
	@Column(name = "a_usage_context_id", nullable = true, insertable = true, updatable = false)
	private String usageContextId;

	@Column(name = "a_resource_type", nullable = true, insertable = true, updatable = false)
	private String resourceType;
	@Column(name = "a_resource_id", nullable = true, insertable = true, updatable = false)
	private Long resourceId;
	@Column(name = "a_resource_sub_id", nullable = true, insertable = true, updatable = false)
	private String resourceSubId;

	@ManyToOne(targetEntity = IdentityImpl.class, fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "fk_identity", nullable = true, insertable = true, updatable = false)
	private Identity identity;

	@Column(name = "a_locale", nullable = true, insertable = true, updatable = false)
	private String locale;

	@Column(name = "a_ai_feature", nullable = false, insertable = true, updatable = false)
	private String aiFeature;

	@Column(name = "a_duration_millis", nullable = true, insertable = true, updatable = false)
	private Long durationMillis;
	@Column(name = "a_status", nullable = false, insertable = true, updatable = false)
	@Enumerated(EnumType.STRING)
	private AiUsageLogStatus status;
	@Column(name = "a_error_code", nullable = true, insertable = true, updatable = false)
	private String errorCode;
	@Column(name = "a_error_message", nullable = true, insertable = true, updatable = false)
	private String errorMessage;

	@Column(name = "a_model_provider", nullable = true, insertable = true, updatable = false)
	private String modelProvider;
	@Column(name = "a_req_model", nullable = true, insertable = true, updatable = false)
	private String requestModel;
	@Column(name = "a_req_temperature", nullable = true, insertable = true, updatable = false)
	private Double requestTemperature;
	@Column(name = "a_req_top_p", nullable = true, insertable = true, updatable = false)
	private Double requestTopP;
	@Column(name = "a_req_max_output_tokens", nullable = true, insertable = true, updatable = false)
	private Long requestMaxOutputTokens;
	@Column(name = "a_invocation_id", nullable = true, insertable = true, updatable = true)
	private String invocationId;
	@Column(name = "a_service_interface", nullable = true, insertable = true, updatable = true)
	private String serviceInterface;
	@Column(name = "a_service_method", nullable = true, insertable = true, updatable = true)
	private String serviceMethod;
	@Column(name = "a_resp_id", nullable = true, insertable = true, updatable = false)
	private String responseId;
	@Column(name = "a_resp_model", nullable = true, insertable = true, updatable = false)
	private String responseModel;
	@Column(name = "a_resp_finish_reason", nullable = true, insertable = true, updatable = false)
	private String responseFinishReason;
	@Column(name = "a_input_tokens", nullable = true, insertable = true, updatable = false)
	private Long inputTokens;
	@Column(name = "a_output_tokens", nullable = true, insertable = true, updatable = false)
	private Long outputTokens;
	@Column(name = "a_total_tokens", nullable = true, insertable = true, updatable = false)
	private Long totalTokens;
	@Column(name = "a_cached_input_tokens", nullable = true, insertable = true, updatable = false)
	private Long cachedInputTokens;
	@Column(name = "a_reasoning_tokens", nullable = true, insertable = true, updatable = false)
	private Long reasoningTokens;
	@Column(name = "a_req_num_messages", nullable = true, insertable = true, updatable = false)
	private Long requestNumMessages;
	@Column(name = "a_req_text_length", nullable = true, insertable = true, updatable = false)
	private Long requestTextLength;
	@Column(name = "a_cache_creation_input_tokens", nullable = true, insertable = true, updatable = false)
	private Long cacheCreationInputTokens;

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public int hashCode() {
		return key == null ? 8576849 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof AiUsageLogImpl) {
			AiUsageLogImpl log = (AiUsageLogImpl)obj;
			return key != null && key.equals(log.key);
		}
		return false;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	@Override
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public String getResourceType() {
		return resourceType;
	}

	@Override
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	@Override
	public Long getResourceId() {
		return resourceId;
	}

	@Override
	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

	@Override
	public String getResourceSubId() {
		return resourceSubId;
	}

	@Override
	public void setResourceSubId(String resourceSubId) {
		this.resourceSubId = resourceSubId;
	}

	@Override
	public String getAiFeature() {
		return aiFeature;
	}

	@Override
	public void setAiFeature(String aiFeature) {
		this.aiFeature = aiFeature;
	}

	@Override
	public String getUsageContextId() {
		return usageContextId;
	}

	@Override
	public void setUsageContextId(String usageContextId) {
		this.usageContextId = usageContextId;
	}

	@Override
	public String getUsageContextType() {
		return usageContextType;
	}

	@Override
	public void setUsageContextType(String usageContextType) {
		this.usageContextType = usageContextType;
	}

	@Override
	public String getLocale() {
		return locale;
	}

	@Override
	public void setLocale(String locale) {
		this.locale = locale;
	}

	@Override
	public Long getDurationMillis() {
		return durationMillis;
	}

	@Override
	public void setDurationMillis(Long durationMillis) {
		this.durationMillis = durationMillis;
	}

	@Override
	public AiUsageLogStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(AiUsageLogStatus status) {
		this.status = status;
	}

	@Override
	public String getErrorCode() {
		return errorCode;
	}

	@Override
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public String getModelProvider() {
		return modelProvider;
	}

	@Override
	public void setModelProvider(String modelProvider) {
		this.modelProvider = modelProvider;
	}

	@Override
	public String getRequestModel() {
		return requestModel;
	}

	@Override
	public void setRequestModel(String requestModel) {
		this.requestModel = requestModel;
	}

	@Override
	public Double getRequestTemperature() {
		return requestTemperature;
	}

	@Override
	public void setRequestTemperature(Double requestTemperature) {
		this.requestTemperature = requestTemperature;
	}

	@Override
	public Double getRequestTopP() {
		return requestTopP;
	}

	@Override
	public void setRequestTopP(Double requestTopP) {
		this.requestTopP = requestTopP;
	}

	@Override
	public Long getRequestMaxOutputTokens() {
		return requestMaxOutputTokens;
	}

	@Override
	public void setRequestMaxOutputTokens(Long requestMaxOutputTokens) {
		this.requestMaxOutputTokens = requestMaxOutputTokens;
	}

	@Override
	public String getInvocationId() {
		return invocationId;
	}

	@Override
	public void setInvocationId(String invocationId) {
		this.invocationId = invocationId;
	}

	@Override
	public String getServiceInterface() {
		return serviceInterface;
	}

	@Override
	public void setServiceInterface(String serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	@Override
	public String getServiceMethod() {
		return serviceMethod;
	}

	@Override
	public void setServiceMethod(String serviceMethod) {
		this.serviceMethod = serviceMethod;
	}

	@Override
	public String getResponseId() {
		return responseId;
	}

	@Override
	public void setResponseId(String responseId) {
		this.responseId = responseId;
	}

	@Override
	public String getResponseModel() {
		return responseModel;
	}

	@Override
	public void setResponseModel(String responseModel) {
		this.responseModel = responseModel;
	}

	@Override
	public String getResponseFinishReason() {
		return responseFinishReason;
	}

	@Override
	public void setResponseFinishReason(String responseFinishReason) {
		this.responseFinishReason = responseFinishReason;
	}

	@Override
	public Long getInputTokens() {
		return inputTokens;
	}

	@Override
	public void setInputTokens(Long inputTokens) {
		this.inputTokens = inputTokens;
	}

	@Override
	public Long getOutputTokens() {
		return outputTokens;
	}

	@Override
	public void setOutputTokens(Long outputTokens) {
		this.outputTokens = outputTokens;
	}

	@Override
	public Long getTotalTokens() {
		return totalTokens;
	}

	@Override
	public void setTotalTokens(Long totalTokens) {
		this.totalTokens = totalTokens;
	}

	@Override
	public Long getCachedInputTokens() {
		return cachedInputTokens;
	}

	@Override
	public void setCachedInputTokens(Long cachedInputTokens) {
		this.cachedInputTokens = cachedInputTokens;
	}

	@Override
	public Long getReasoningTokens() {
		return reasoningTokens;
	}

	@Override
	public void setReasoningTokens(Long reasoningTokens) {
		this.reasoningTokens = reasoningTokens;
	}

	@Override
	public Long getRequestNumMessages() {
		return requestNumMessages;
	}

	@Override
	public void setRequestNumMessages(Long requestNumMessages) {
		this.requestNumMessages = requestNumMessages;
	}

	@Override
	public Long getRequestTextLength() {
		return requestTextLength;
	}

	@Override
	public void setRequestTextLength(Long requestTextLength) {
		this.requestTextLength = requestTextLength;
	}

	@Override
	public Long getCacheCreationInputTokens() {
		return cacheCreationInputTokens;
	}

	@Override
	public void setCacheCreationInputTokens(Long cacheCreationInputTokens) {
		this.cacheCreationInputTokens = cacheCreationInputTokens;
	}
}
