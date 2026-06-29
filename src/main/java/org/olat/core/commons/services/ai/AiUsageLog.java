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

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;

/**
 *
 * Initial date: 01.04.2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public interface AiUsageLog extends CreateInfo {

	public Long getKey();

	public void setCreationDate(java.util.Date creationDate);

	public String getUsageContextId();

	public void setUsageContextId(String usageContextId);

	public String getAiFeature();

	public void setAiFeature(String aiFeature);

	public String getUsageContextType();

	public void setUsageContextType(String contextType);

	public Identity getIdentity();

	public void setIdentity(Identity identity);

	public String getResourceType();

	public void setResourceType(String resourceType);

	public Long getResourceId();

	public void setResourceId(Long resourceId);

	public String getResourceSubId();

	public void setResourceSubId(String resourceSubId);

	public String getLocale();

	public void setLocale(String locale);

	public Long getDurationMillis();

	public void setDurationMillis(Long durationMillis);

	public AiUsageLogStatus getStatus();

	public void setStatus(AiUsageLogStatus status);

	public String getErrorCode();

	public void setErrorCode(String errorCode);

	public String getErrorMessage();

	public void setErrorMessage(String errorMessage);

	public String getModelProvider();

	public void setModelProvider(String modelProvider);

	public String getRequestModel();

	public void setRequestModel(String requestModel);

	public Double getRequestTemperature();

	public void setRequestTemperature(Double requestTemperature);

	public Double getRequestTopP();

	public void setRequestTopP(Double requestTopP);

	public Long getRequestMaxOutputTokens();

	public void setRequestMaxOutputTokens(Long requestMaxOutputTokens);

	public String getInvocationId();

	public void setInvocationId(String invocationId);

	public String getServiceInterface();

	public void setServiceInterface(String serviceInterface);

	public String getServiceMethod();

	public void setServiceMethod(String serviceMethod);

	public String getResponseId();

	public void setResponseId(String responseId);

	public String getResponseModel();

	public void setResponseModel(String responseModel);

	public String getResponseFinishReason();

	public void setResponseFinishReason(String responseFinishReason);

	public Long getInputTokens();

	public void setInputTokens(Long inputTokens);

	public Long getOutputTokens();

	public void setOutputTokens(Long outputTokens);

	public Long getTotalTokens();

	public void setTotalTokens(Long totalTokens);

	public Long getCachedInputTokens();

	public void setCachedInputTokens(Long cachedInputTokens);

	public Long getReasoningTokens();

	public void setReasoningTokens(Long reasoningTokens);

	public Long getRequestNumMessages();

	public void setRequestNumMessages(Long requestNumMessages);

	public Long getRequestTextLength();

	public void setRequestTextLength(Long requestTextLength);

	public Long getCacheCreationInputTokens();

	public void setCacheCreationInputTokens(Long cacheCreationInputTokens);

}
