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
import java.nio.file.Paths;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.dispatcher.mapper.GlobalMapperRegistry;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.FileUtils;
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

	@Override
	public void init() {
		logoUri = GlobalMapperRegistry.getInstance().register("logo", new LogoMapper(this));
		
		String filenameObj = getStringPropertyValue(LOGO_FILENAME, true);
		if(StringHelper.containsNonWhitespace(filenameObj)) {
			logoFilename = filenameObj;
		} else {
			logoFilename = null;
		}
		String logoAltObj = getStringPropertyValue(LOGO_ALT, true);
		if(StringHelper.containsNonWhitespace(logoAltObj)) {
			logoAlt = logoAltObj;
		} else {
			logoAlt = null;
		}
		String logoLinkTypeObj = getStringPropertyValue(LOGO_LINK_TYPE, true);
		if(StringHelper.containsNonWhitespace(logoLinkTypeObj)) {
			logoLinkType = logoLinkTypeObj;
		} else {
			logoLinkType = null;
		}
		String logoUriObj = getStringPropertyValue(LOGO_URI, true);
		if(StringHelper.containsNonWhitespace(logoUriObj)) {
			logoLinkUri = logoUriObj;
		} else {
			logoLinkUri = null;
		}
		
		String footerUriObj = getStringPropertyValue(FOOTER_URI, true);
		if(StringHelper.containsNonWhitespace(footerUriObj)) {
			footerLinkUri = footerUriObj;
		} else {
			footerLinkUri = null;
		}
		String footerLineObj = getStringPropertyValue(FOOTER_LINE, true);
		if(StringHelper.containsNonWhitespace(footerLineObj)) {
			footerLine = footerLineObj;
		} else {
			footerLine = null;
		}
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
	
	public void updateLogo(String filename) {
		setLogoFilename(filename);
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
	
	public void removeLogo() {
		File logo = getLogo();
		if(logo != null && logo.exists()) {
			FileUtils.deleteFile(logo);
		}
		File dir = getLogoDirectory();
		File logo1x = new File(dir, "oo-logo@1x.png");
		if(logo1x.exists()) {
			FileUtils.deleteFile(logo1x);
		}
		File logo2x = new File(dir, "oo-logo@2x.png");
		if(logo2x.exists()) {
			FileUtils.deleteFile(logo2x);
		}
		setLogoFilename("");
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