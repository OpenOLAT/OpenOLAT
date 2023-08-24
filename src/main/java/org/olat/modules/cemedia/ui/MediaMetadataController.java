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
package org.olat.modules.cemedia.ui;

import java.util.List;

import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaCenterLicenseHandler;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.manager.MetadataXStream;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * A basic with encapsulate most of the metadata of a media.
 * 
 * Initial date: 24 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaMetadataController extends BasicController {
	
	private Media media;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private MediaCenterLicenseHandler licenseHandler;
	
	public MediaMetadataController(UserRequest ureq, WindowControl wControl, Media media) {
		super(ureq, wControl);
		this.media = media;
		
		VelocityContainer mainVC = createVelocityContainer("media_details_metadata");
		putInitialPanel(mainVC);
		loadMetadata(mainVC);
	}

	private void loadMetadata(VelocityContainer metaVC) {
		metaVC.contextPut("media", media);
		String author = userManager.getUserDisplayName(media.getAuthor());
		metaVC.contextPut("author", author);
		
		MediaHandler handler = mediaService.getMediaHandler(media.getType());
		String type = translate("artefact." + handler.getType());
		metaVC.contextPut("mediaType", type);
		
		MediaVersion currentVersion = media.getVersions().get(0);
		String iconCssClass = handler.getIconCssClass(currentVersion);
		if(StringHelper.containsNonWhitespace(iconCssClass)) {
			metaVC.contextPut("mediaIconCssClass", iconCssClass);
		}
			
		if(media.getCollectionDate() != null) {
			String collectionDate = Formatter.getInstance(getLocale()).formatDate(media.getCollectionDate());
			metaVC.contextPut("collectionDate", collectionDate);
		}
		
		if(StringHelper.containsNonWhitespace(media.getMetadataXml())) {
			Object metadata = MetadataXStream.get().fromXML(media.getMetadataXml());
			metaVC.contextPut("metadata", metadata);
		}
		
		// License
		if (licenseModule.isEnabled(licenseHandler)) {
			License license = licenseService.loadOrCreateLicense(media);
			LicenseType licenseType = license.getLicenseType();
			if (!licenseService.isNoLicense(licenseType)) {
				metaVC.contextPut("license", LicenseUIFactory.translate(licenseType, getLocale()));
				metaVC.contextPut("licenseIconCss", LicenseUIFactory.getCssOrDefault(licenseType));
				String licensor = StringHelper.containsNonWhitespace(license.getLicensor())? license.getLicensor(): "";
				metaVC.contextPut("licensor", licensor);
				metaVC.contextPut("licenseText", LicenseUIFactory.getFormattedLicenseText(license));	
			}
		} 

		List<TaxonomyLevel> levels = mediaService.getTaxonomyLevels(media);
		List<String> levelsNames = levels.stream()
				.map(level ->  TaxonomyUIFactory.translateDisplayName(getTranslator(), level))
				.toList();
		metaVC.contextPut("taxonomyLevels", levelsNames);
		
		List<TagInfo> tagInfos = mediaService.getTagInfos(media, getIdentity(), false);
		List<String> tags = tagInfos.stream()
				.map(TagInfo::getDisplayName)
				.toList();
		metaVC.contextPut("tags", tags);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
}