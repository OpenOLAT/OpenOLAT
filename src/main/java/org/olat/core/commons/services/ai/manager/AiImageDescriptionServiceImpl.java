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

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.ai.AiFeature;
import org.olat.core.commons.services.ai.AiImageDescriptionService;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.AiSPI;
import org.olat.core.commons.services.ai.model.AiImageDescriptionResponse;
import org.olat.core.commons.services.ai.model.AiUsageContext;
import org.olat.core.commons.services.ai.service.ImageDescriptionAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.langchain4j.data.message.Content;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;

/**
 * Spring service implementation for image description generation via AI.
 *
 * Initial date: 31.03.2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class AiImageDescriptionServiceImpl implements AiImageDescriptionService {

	private static final int MAX_TOKENS = 2000;

	@Autowired
	private AiModule aiModule;
	@Autowired
	private AiUsageLogDAO aiUsageLogDAO;

	private volatile CachedChatModel cachedAiService;

	@Override
	public boolean isEnabled() {
		return aiModule.isImageDescriptionGeneratorEnabled();
	}

	@Override
	public AiImageDescriptionResponse generateImageDescription(AiUsageContext usageContext, String imageBase64, String mimeType, Locale locale) {
		return generateImageDescription(usageContext, imageBase64, mimeType, locale, aiModule.getImgDescSpiId(), aiModule.getImgDescModel());
	}

	@Override
	public AiImageDescriptionResponse generateImageDescription(AiUsageContext usageContext, String imageBase64, String mimeType, Locale locale, String spiId, String modelName) {
		AiImageDescriptionResponse response = new AiImageDescriptionResponse();
		AiSPI spi = aiModule.resolveProvider(spiId);
		if (spi == null) {
			response.setError("AI provider is not configured or not available.");
			return response;
		}
		long startTime = System.currentTimeMillis();
		try {
			cachedAiService = CachedChatModel.getOrRefresh(cachedAiService, spi, spiId, modelName, MAX_TOKENS);
			ChatModel chatModel = cachedAiService.chatModel();

			List<Content> contents = ImageDescriptionAiService.buildContents(locale, imageBase64, mimeType);
			AiServices<ImageDescriptionAiService> builder = AiServices.builder(ImageDescriptionAiService.class);
			AiLoggingChatModel.configureBuilder(builder, chatModel, aiUsageLogDAO, spiId, AiFeature.ImageDescriptionGenerator.getType(), usageContext);
			ImageDescriptionAiService service = builder.build();

			response.setDescription(service.describeImage(contents));

		} catch (Exception e) {
			Exception cause = e instanceof AiUsageLoggedException ? (Exception) e.getCause() : e;
			response.setError(cause.getMessage() != null ? cause.getMessage() : cause.getClass().getName());
			if (!(e instanceof AiUsageLoggedException)) {
				aiUsageLogDAO.createErrorLog(spiId, modelName, AiFeature.ImageDescriptionGenerator.getType(), usageContext,
						System.currentTimeMillis() - startTime, cause);
			}
		}
		return response;
	}

}
