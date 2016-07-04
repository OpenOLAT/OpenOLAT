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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.ui.event.PublishEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageMetadataController extends BasicController {
	
	private Link publishButton;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public PageMetadataController(UserRequest ureq, WindowControl wControl, BinderSecurityCallback secCallback, Page page) {
		super(ureq, wControl);

		VelocityContainer mainVC = createVelocityContainer("page_meta");
		if(secCallback.canPublish(page)) {
			publishButton = LinkFactory.createButton("publish", mainVC, this);
		}
		
		mainVC.contextPut("pageTitle", page.getTitle());
		mainVC.contextPut("pageSummary", page.getSummary());
		mainVC.contextPut("status", page.getPageStatus());
		
		Date lastPublication = page.getLastPublicationDate();
		if(lastPublication == null) {
			mainVC.contextPut("hasLastPublicationDate", Boolean.FALSE);
		} else {
			mainVC.contextPut("hasLastPublicationDate", Boolean.TRUE);
			mainVC.contextPut("lastPublicationDate", lastPublication);
		}
		
		List<Category> categories = portfolioService.getCategories(page);
		List<String> categoryNames = new ArrayList<>(categories.size());
		for(Category category:categories) {
			categoryNames.add(category.getName());
		}
		mainVC.contextPut("pageCategories", categoryNames);
		
		mainVC.contextPut("lastModified", page.getLastModified());

		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(publishButton == source) {
			fireEvent(ureq, new PublishEvent());
		}
	}
}
