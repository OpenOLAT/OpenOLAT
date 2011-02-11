/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.modules.webFeed.portfolio;

import java.io.InputStream;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.date.DateComponentFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.webFeed.models.Item;
import org.olat.portfolio.manager.EPFrontendManager;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Description:<br>
 * Read-only view for a blog entry
 * 
 * <P>
 * Initial Date:  3 déc. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BlogArtefactDetailsController extends BasicController {
	
	private VelocityContainer mainVC;
	
	public BlogArtefactDetailsController(UserRequest ureq, WindowControl wControl, BlogArtefact artefact, boolean readOnlyMode) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("post");
		
		EPFrontendManager ePFMgr = (EPFrontendManager)CoreSpringFactory.getBean("epFrontendManager");
		VFSContainer artefactContainer = ePFMgr.getArtefactContainer(artefact);
		VFSLeaf itemXml = (VFSLeaf)artefactContainer.resolve(BlogArtefact.BLOG_FILE_NAME);
		if(itemXml != null) {
			InputStream in = itemXml.getInputStream();
			
			XStream xstream = XStreamHelper.createXStreamInstance();
			xstream.alias("item", Item.class);
			Item item = (Item)xstream.fromXML(in);
			FileUtils.closeSafely(in);
			
			String content = item.getContent();
			if (!StringHelper.containsNonWhitespace(content)) content = item.getDescription();
			String filteredText = FilterFactory.getHtmlTagAndDescapingFilter().filter(content);
			filteredText = Formatter.truncate(filteredText, 50);
			mainVC.contextPut("filteredText", filteredText);
			mainVC.contextPut("readOnlyMode", readOnlyMode);
			
			mainVC.contextPut("item", item);
			mainVC.contextPut("helper", new ItemHelper(""));
			
		// Add date component
			if(item.getDate() != null) {
				DateComponentFactory.createDateComponentWithYear("dateComp", item.getDate(), mainVC);
			}
		}
		
		putInitialPanel(mainVC);
	}
		
	@Override
	protected void doDispose() {
		//
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
				String basePath = baseUri + "/" + item.getGuid();
				Filter mediaUrlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(basePath);
				itemContent = mediaUrlFilter.filter(itemContent);
			}
			return itemContent;
		}
		
		public String getItemDescriptionForBrowser(Item item) {
			String itemDescription = item.getDescription();
			if (itemDescription != null) {
				//Add relative media base to media elements to display internal media files
				String basePath = baseUri + "/" + item.getGuid();
				Filter mediaUrlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(basePath);
				itemDescription = mediaUrlFilter.filter(itemDescription);
			}
			return itemDescription;
		}
	}
}