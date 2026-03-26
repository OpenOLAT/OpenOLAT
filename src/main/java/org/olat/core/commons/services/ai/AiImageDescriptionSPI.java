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
import java.util.Locale;

import org.olat.core.commons.services.ai.model.AiImageDescriptionResponse;

/**
 * Feature interface for AI services that can generate image descriptions.
 * Implement this interface in addition to AiSPI to provide image description generation.
 *
 * Initial date: 19.03.2026<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public interface AiImageDescriptionSPI {

	String FEATURE_ID = "image-description-generator";

	/**
	 * Generate a description for the given image including title, alt text,
	 * color tags, category tags, and keywords.
	 *
	 * @param imageBase64 The image encoded as base64 string
	 * @param mimeType The MIME type of the image (e.g. "image/jpeg")
	 * @param locale The locale for the response language
	 * @return The response containing the image description data
	 */
	AiImageDescriptionResponse generateImageDescription(String imageBase64, String mimeType, Locale locale);

	/**
	 * Set the vision model to use for image description generation.
	 * Only rebuilds the underlying model if the name actually changes.
	 *
	 * @param model The model name
	 */
	void setImageDescriptionModel(String model);

	/**
	 * @return The vision model used for image description generation
	 */
	String getImageDescriptionModel();

	/**
	 * @return The list of available model names for image description generation.
	 *         Used to populate the model selection dropdown in the admin UI.
	 */
	List<String> getAvailableImageDescriptionModels();

}
