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

import java.time.Duration;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

import dev.langchain4j.model.chat.ChatModel;

/**
 *
 * Base AI service provider interface. Implement this interface to register an
 * AI service provider.
 *
 * Initial date: 22.05.2024<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public interface AiSPI {

	public String getId();

	public String getName();

	public boolean isEnabled();

	public void setEnabled(boolean enabled);

	public Controller createAdminController(UserRequest ureq, WindowControl wControl);

	public ChatModel buildChatModel(String modelName, int maxTokens);

	/**
	 * Build a {@link ChatModel} with an explicit per-call timeout. The
	 * {@code timeout} is forwarded to the LangChain4j ChatModel builder's
	 * {@code .timeout(Duration)} setter and ends up as a per-request
	 * socket-timeout override on the underlying Apache HttpClient (see
	 * {@link org.olat.core.commons.services.ai.manager.LangChain4jHttpClientBuilder}).
	 * A {@code null} timeout falls back to the global default socket
	 * timeout from {@code HttpClientModule}. The default implementation
	 * ignores the timeout and delegates to
	 * {@link #buildChatModel(String, int)} for backwards compatibility with
	 * providers that do not support per-call timeouts.
	 */
	public default ChatModel buildChatModel(String modelName, int maxTokens, Duration timeout) {
		return buildChatModel(modelName, maxTokens);
	}

	public List<String> getAvailableModels();

}
