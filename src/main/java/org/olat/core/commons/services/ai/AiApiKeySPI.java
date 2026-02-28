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

import java.util.List;

/**
 * Mixin interface for AI service providers that authenticate via an API key.
 * Implementing this allows the provider to use the generic
 * {@code GenericAiApiKeyAdminController} instead of a custom admin controller.
 *
 * Initial date: 28.02.2026<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 */
public interface AiApiKeySPI {

	/**
	 * @return The currently stored API key, or empty/null if not set
	 */
	String getApiKey();

	/**
	 * Store the API key persistently. An empty or null value removes the key.
	 *
	 * @param apiKey the key to store
	 */
	void setApiKey(String apiKey);

	/**
	 * Verify the given API key against the provider's API.
	 *
	 * @param apiKey the key to verify
	 * @return list of available model names reported by the provider
	 * @throws Exception if the key is invalid or the API call fails
	 */
	List<String> verifyApiKey(String apiKey) throws Exception;

	/**
	 * @return i18n key for the admin form title (e.g. "ai.openai.title")
	 */
	String getAdminTitleI18nKey();

	/**
	 * @return i18n key for the admin form description (e.g. "ai.openai.desc")
	 */
	String getAdminDescI18nKey();

	/**
	 * @return i18n key for the API key field label (e.g. "ai.openai.apikey")
	 */
	String getAdminApiKeyI18nKey();

}
