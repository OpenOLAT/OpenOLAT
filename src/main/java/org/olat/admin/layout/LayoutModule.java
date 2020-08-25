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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.dispatcher.mapper.GlobalMapperRegistry;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * Manager to get/set properties which are used by 
 * FrentixFooterController / LogoWithLinkHeaderController
 * 
 * <P>
 * Initial Date:  19.06.2008 <br>
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
@Service
public class LayoutModule extends AbstractSpringModule  {
	
	public static final OLATResourceable layoutCustomizingOResourceable = OresHelper.createOLATResourceableType("LayoutCustomizing");
	private static final Logger log = Tracing.createLoggerFor(LayoutModule.class);

	private static final String LOGO_FILENAME = "logo.filename";
	private static final String LOGO_ALT = "logo.alt";
	private static final String LOGO_LINK_TYPE = "logo.link.type";
	private static final String LOGO_URI = "logo.uri";
	private static final String FOOTER_URI = "footer.uri";
	private static final String FOOTER_LINE = "footer.line";
	
	@Value("${logo.filename:}")
	private String logoFilename;
	@Value("${logo.alt:}")
	private String logoAlt;
	@Value("${logo.link.type:}")
	private String logoLinkType;
	@Value("${logo.uri:}")
	private String logoLinkUri;
	@Value("${footer.line:}")
	private String footerLine;
	@Value("${footer.uri:}")
	private String footerLinkUri;
	
	private String logoUri;

	@Autowired
	public LayoutModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	public String getConfigProperty(String property) {
		String propertyObj = getStringPropertyValue(property, true);
		if(StringHelper.containsNonWhitespace(propertyObj)) {
			return propertyObj;
		} else {
			return null;
		}
	}

	@Override
	public void init() {
		logoUri = GlobalMapperRegistry.getInstance().register("logo", new LogoMapper(this));
		logoFilename = getConfigProperty(LOGO_FILENAME);
		logoAlt = getConfigProperty(LOGO_ALT);
		logoLinkType = getConfigProperty(LOGO_LINK_TYPE);
		logoLinkUri = getConfigProperty(LOGO_URI);
		footerLinkUri = getConfigProperty(FOOTER_URI);
		footerLine = getConfigProperty(FOOTER_LINE);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}

	public String getLogoUri() {
		return logoUri + "/oo-logo@1x.png";
	}

	public String getLogoFilename() {
		return logoFilename;
	}
	//LogoUri
	public void setLogoFilename(String filename) {
		logoFilename = filename;
		setStringProperty(LOGO_FILENAME, filename, true);
	}

	public File getLogo() {
		File logo = null;
		String filename = getLogoFilename();
		if(StringHelper.containsNonWhitespace(filename)) {
			logo = Paths.get(WebappHelper.getUserDataRoot(), "customizing", "logo", filename).toFile();
		}
		return logo;
	}
	
	public File getLogoDirectory() {
		File dir = Paths.get(WebappHelper.getUserDataRoot(), "customizing", "logo").toFile();
		if(!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}

	private void removeAndLog(Path path) throws IOException {
		boolean deleteSuccess = Files.deleteIfExists(path);
		if (!deleteSuccess) {
			log.warn("Deleting file {} failed.", path);
		}
	}

	public void removeLogo() {
		File dir = getLogoDirectory();
		try {
			File logo = getLogo();

			Path logoDir = Paths.get(dir.getAbsolutePath());
			if (logo != null && FileUtils.directoryContains(dir, logo)) {
				removeAndLog(logo.toPath());
			}
			removeAndLog(logoDir.resolve("oo-logo@1x.png"));
			removeAndLog(logoDir.resolve("oo-logo@2x.png"));
			setLogoFilename("");
		} catch (IOException e) {
			log.warn("IO Error occured deleting logo in {}", dir);
		}
	}
	
	public String getLogoAlt() {
		return logoAlt;		
	}
	
	public void setLogoAlt(String alt){
		this.logoAlt = alt;
		setStringProperty(LOGO_ALT, alt, true);
	}

	public String getLogoLinkType() {
		return logoLinkType;
	}

	public void setLogoLinkType(String type) {
		this.logoLinkType = type;
		setStringProperty(LOGO_LINK_TYPE, type, true);
	}

	public String getLogoLinkUri() {
		return logoLinkUri;
	}

	public void setLogoLinkUri(String uri){
		this.logoLinkUri = uri;
		setStringProperty(LOGO_URI, uri, true);
	}
	
	public String getFooterLinkUri() {
		return footerLinkUri;
	}
	
	public void setFooterLinkUri(String uri) {
		this.footerLinkUri = uri;
		setStringProperty(FOOTER_URI, uri, true);
	}
	
	public String getFooterLine() {
		return footerLine;
	}
	
	public void setFooterLine(String line) {
		this.footerLine = line;
		setStringProperty(FOOTER_LINE, line, true);
	}
}