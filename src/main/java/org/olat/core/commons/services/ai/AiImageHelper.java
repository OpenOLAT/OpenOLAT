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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Helper service for preparing images for AI processing. Handles scaling
 * to an optimal size and base64 encoding.
 *
 * Initial date: 19.03.2026<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class AiImageHelper {
	private static final Logger log = Tracing.createLoggerFor(AiImageHelper.class);
	private static final int MAX_DIMENSION = 1024;

	@Autowired
	private ImageService imageService;

	/**
	 * Scale the given image to a max dimension of 1024px and return it as a
	 * base64 encoded string. Returns null if the image could not be processed.
	 *
	 * @param imageFile The source image file
	 * @param suffix The file extension (e.g. "jpg", "png")
	 * @return The base64 encoded scaled image, or null on error
	 */
	public String prepareImageBase64(File imageFile, String suffix) {
		if (imageFile == null || !imageFile.exists()) {
			return null;
		}
		File scaledFile = null;
		try {
			// Check if scaling is needed
			Size originalSize = imageService.getSize(imageFile, suffix);
			if (originalSize != null && originalSize.getWidth() <= MAX_DIMENSION && originalSize.getHeight() <= MAX_DIMENSION) {
				// No scaling needed, read original
				byte[] bytes = Files.readAllBytes(imageFile.toPath());
				return Base64.getEncoder().encodeToString(bytes);
			}

			// Scale image
			scaledFile = File.createTempFile("ai_img_", "." + suffix);
			Size scaledSize = imageService.scaleImage(imageFile, suffix, scaledFile, MAX_DIMENSION, MAX_DIMENSION, false);
			if (scaledSize == null) {
				log.warn("Could not scale image for AI processing: {}", imageFile.getName());
				return null;
			}
			byte[] bytes = Files.readAllBytes(scaledFile.toPath());
			return Base64.getEncoder().encodeToString(bytes);

		} catch (IOException e) {
			log.warn("Error preparing image for AI processing: {}", imageFile.getName(), e);
			return null;
		} finally {
			if (scaledFile != null) {
				try {
					Files.deleteIfExists(scaledFile.toPath());
				} catch (IOException e) {
					log.debug("Could not delete temp file: {}", scaledFile.getAbsolutePath());
				}
			}
		}
	}

	/**
	 * Get the MIME type for the given file suffix.
	 *
	 * @param suffix The file extension (e.g. "jpg", "png")
	 * @return The MIME type, or null if unknown
	 */
	public String getMimeType(String suffix) {
		if (suffix == null) return null;
		return switch (suffix.toLowerCase()) {
			case "jpg", "jpeg" -> "image/jpeg";
			case "png" -> "image/png";
			case "gif" -> "image/gif";
			case "webp" -> "image/webp";
			default -> null;
		};
	}
}
