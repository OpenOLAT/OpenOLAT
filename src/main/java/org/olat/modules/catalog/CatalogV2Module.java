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
package org.olat.modules.catalog;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.modules.catalog.site.CatalogContextEntryControllerCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.comm
 *
 */
@Service
public class CatalogV2Module extends AbstractSpringModule implements ConfigOnOff {
	
	public static final Set<String> HEADER_BG_IMAGE_MIME_TYPES = Set.of("image/gif", "image/jpg", "image/jpeg", "image/png");
	public static final String TAXONOMY_LEVEL_LAUNCHER_STYLE_RECTANGLE = "rectangle";
	public static final String TAXONOMY_LEVEL_LAUNCHER_STYLE_SQUARE = "square";
	private static final Path HEADER_BG_DIR = Paths.get(WebappHelper.getUserDataRoot(), "customizing", "catalog", "header", "background");
	
	private static final String KEY_ENABLED = "catalog.v2.enabled";
	private static final String KEY_WEB_PUBLISH_ENABLED = "catalog.v2.web.publish.enabled";
	private static final String KEY_HEADER_BG_IMAGE_URI = "catalog.v2.header.bg.image.filename";
	private static final String KEY_LAUNCHER_TAXONOMY_LEVEL_STYLE = "catalog.v2.launcher.taxonomy.level.style";

	@Value("${catalog.v2.enabled:false}")
	private boolean enabled;
	@Value("${catalog.v2.web.publish.enabled:false}")
	private boolean webPublishEnabled;
	@Value("${catalog.v2.header.bg.image.filename:}")
	private String headerBgImageFilename;
	@Value("${catalog.v2.launcher.taxonomy.levelstyle:rectangle}")
	private String launcherTaxonomyLevelStyle;
	
	@Autowired
	public CatalogV2Module(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		NewControllerFactory.getInstance().addContextEntryControllerCreator("Catalog",
				new CatalogContextEntryControllerCreator());
		
		String enabledObj = getStringPropertyValue(KEY_ENABLED, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String webPublishEnabledObj = getStringPropertyValue(KEY_WEB_PUBLISH_ENABLED, true);
		if (StringHelper.containsNonWhitespace(webPublishEnabledObj)) {
			webPublishEnabled = "true".equals(webPublishEnabledObj);
		}
		
		headerBgImageFilename = getStringPropertyValue(KEY_HEADER_BG_IMAGE_URI, headerBgImageFilename);
		launcherTaxonomyLevelStyle = getStringPropertyValue(KEY_LAUNCHER_TAXONOMY_LEVEL_STYLE, launcherTaxonomyLevelStyle);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setBooleanProperty(KEY_ENABLED, enabled, true);
	}

	public boolean isWebPublishEnabled() {
		return webPublishEnabled;
	}

	public void setWebPublishEnabled(boolean webPublishEnabled) {
		this.webPublishEnabled = webPublishEnabled;
		setBooleanProperty(KEY_WEB_PUBLISH_ENABLED, webPublishEnabled, true);
	}
	
	public File getHeaderBgDirectory() {
		File dir = HEADER_BG_DIR.toFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
	
	public boolean hasHeaderBgImage() {
		return StringHelper.containsNonWhitespace(headerBgImageFilename);
	}
	
	public File getHeaderBgImage() {
		return hasHeaderBgImage()
				? HEADER_BG_DIR.resolve(headerBgImageFilename).toFile()
				: null;
	}
	
	public void setHeaderBgImageFilename(String headerBgImageFilename) {
		if (headerBgImageFilename == null) {
			deleteHeaderBgImage();
		} else {
			int pos = headerBgImageFilename.lastIndexOf(".");
			String extension = "";
			String normalizedName = headerBgImageFilename;
			if (pos > 0) {
				extension = headerBgImageFilename.substring(pos);
				normalizedName = FileUtils.normalizeFilename(headerBgImageFilename.substring(0, pos));
			} else {
				normalizedName = FileUtils.normalizeFilename(headerBgImageFilename);
			}
			File origFile =  HEADER_BG_DIR.resolve(headerBgImageFilename).toFile();
			File normalizedFile = HEADER_BG_DIR.resolve(normalizedName + extension).toFile();
			origFile.renameTo(normalizedFile);
			
			this.headerBgImageFilename = normalizedFile.getName();
			setStringProperty(KEY_HEADER_BG_IMAGE_URI, this.headerBgImageFilename, true);
		}
	}
	
	public void deleteHeaderBgImage() {
		FileUtils.deleteDirsAndFiles(getHeaderBgDirectory(), true, false);
		headerBgImageFilename = null;
		removeProperty(KEY_HEADER_BG_IMAGE_URI, true);
	}

	public String getLauncherTaxonomyLevelStyle() {
		return launcherTaxonomyLevelStyle;
	}

	public void setLauncherTaxonomyLevelStyle(String launcherTaxonomyLevelStyle) {
		this.launcherTaxonomyLevelStyle = launcherTaxonomyLevelStyle;
		setStringProperty(KEY_LAUNCHER_TAXONOMY_LEVEL_STYLE, launcherTaxonomyLevelStyle, true);
	}
	
}
