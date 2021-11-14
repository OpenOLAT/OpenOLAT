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

package org.olat.modules.webFeed.portfolio;

import java.io.InputStream;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.date.DateComponentFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSContainerMapper;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaRenderingHints;
import org.olat.modules.portfolio.manager.PortfolioFileStorage;
import org.olat.modules.portfolio.ui.MediaMetadataController;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.manager.FeedFileStorge;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Read-only view for a blog entry
 * 
 * <P>
 * Initial Date:  3 déc. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BlogEntryMediaController extends BasicController {
	
	@Autowired
	private PortfolioFileStorage fileStorage;
	
	public BlogEntryMediaController(UserRequest ureq, WindowControl wControl, Media media, MediaRenderingHints hints) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("media_post");
		if (StringHelper.containsNonWhitespace(media.getStoragePath())) {
			VFSContainer container = fileStorage.getMediaContainer(media);
			VFSItem item = container.resolve(media.getRootFilename());
			if(item instanceof VFSLeaf) {
				VFSLeaf itemLeaf = (VFSLeaf)item;
				try(InputStream in = itemLeaf.getInputStream()) {
					Item blogItem = (Item)FeedFileStorge.fromXML(in);
					if(blogItem.getDate() != null) {
						DateComponentFactory.createDateComponentWithYear("dateComp", blogItem.getDate(), mainVC);
					}

					String content = blogItem.getContent();
					if (!StringHelper.containsNonWhitespace(content)) {
						content = blogItem.getDescription();
					}
					
					mainVC.contextPut("content", content);
					mainVC.contextPut("readOnlyMode", Boolean.TRUE);
					mainVC.contextPut("item", blogItem);
					
					String mapperBase = registerMapper(ureq, new VFSContainerMapper(container));
					mainVC.contextPut("helper", new ItemHelper(mapperBase));
					
					if(hints.isExtendedMetadata()) {
						MediaMetadataController metaCtrl = new MediaMetadataController(ureq, wControl, media);
						listenTo(metaCtrl);
						mainVC.put("meta", metaCtrl.getInitialComponent());
					}
				} catch(Exception ex) {
					logError("", ex);
				}
			}
		} 
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public class ItemHelper {
		
		private final String baseUri;
		
		public ItemHelper(String baseUri) {
			this.baseUri = baseUri;
		}
		
		public String getItemContentForBrowser(Item item) {
			String itemContent = item.getContent();
			if (itemContent != null) {
				//Add relative media base to media elements to display internal media files
				Filter mediaUrlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(baseUri);
				itemContent = mediaUrlFilter.filter(itemContent);
			}
			return itemContent;
		}
		
		public String getItemDescriptionForBrowser(Item item) {
			String itemDescription = item.getDescription();
			if (itemDescription != null) {
				//Add relative media base to media elements to display internal media files
				Filter mediaUrlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(baseUri);
				itemDescription = mediaUrlFilter.filter(itemDescription);
			}
			return itemDescription;
		}
	}
}