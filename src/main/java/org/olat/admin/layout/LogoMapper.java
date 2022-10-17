/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.admin.layout;

import java.io.File;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;

/**
 * 
 * Initial date: 20.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class LogoMapper implements Mapper {
	
	private final LayoutModule layoutModule;
	private long lastModified;
	private boolean scalingFailed = false;
	
	public LogoMapper(LayoutModule layoutModule) {
		this.layoutModule = layoutModule;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		File logo = layoutModule.getLogo();
		File dir = layoutModule.getLogoDirectory();
		File scaledLogo = new File(dir, relPath);
		if (lastModified < logo.lastModified() || (!scaledLogo.exists() && scalingFailed)) {
			// Serve logo resized to fit into a 400x200 box. Will be displayed
			// normally with 50px height, so there are plenty of pixels for HD screens
			ImageService imageService = CoreSpringFactory.getImpl(ImageService.class);
			File logoScaled = new File(dir, "optimized_" + logo.getName());
			Size size = imageService.scaleImage(logo, "png", logoScaled, 400, 200, false);
			// try this only once for this image
			if (size == null) {
				scalingFailed = true;
			} else {
				lastModified = logo.lastModified();
			}
		}
		return new FileMediaResource(scaledLogo);
	}
}
