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
package org.olat.modules.portfolio.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaHandler;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.manager.MetadataXStream;
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
	private PortfolioService portfolioService;
	
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
		
		MediaHandler handler = portfolioService.getMediaHandler(media.getType());
		String type = translate("artefact." + handler.getType());
		metaVC.contextPut("mediaType", type);
		String iconCssClass = handler.getIconCssClass(media);
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
		
		List<Category> categories = portfolioService.getCategories(media);
		if(categories != null && !categories.isEmpty()) {
			List<String> categoriesList = categories.stream().map(Category::getName)
					.collect(Collectors.toList());
			metaVC.contextPut("categoriesList", categoriesList);
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
}