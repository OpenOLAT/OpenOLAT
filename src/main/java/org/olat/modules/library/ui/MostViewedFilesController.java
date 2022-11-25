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
package org.olat.modules.library.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.library.LibraryManager;
import org.olat.modules.library.model.CatalogItem;
import org.olat.modules.library.ui.event.OpenFileEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * This controller is responsible for displaying, handling and dispatching the
 * most viewed files in the library.
 * </p>
 * Initial Date: Sep 14, 2009 <br>
 * 
 * @author gwassmann, gwassmann@frentix.com, www.frentix.com
 */
public class MostViewedFilesController extends BasicController {
	private VelocityContainer mainVC;
	private List<Link> links;

	@Autowired
	private LibraryManager libraryManager;

	/**
	 * Constructor
	 * 
	 * @param libraryCtr
	 * @param mapperBaseURL
	 * @param ureq
	 * @param wControl
	 */
	protected MostViewedFilesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("files");
		mainVC.contextPut("cssClass", "o_library_most_views");
		updateView(ureq.getLocale());

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (links.contains(source)) {
			Link link = (Link) source;
			String cmd = link.getCommand();
			CatalogItem item = (CatalogItem) link.getUserObject();
			fireEvent(ureq, new OpenFileEvent(cmd, item));
		}
	}

	/**
	 * updates the list of newest files
	 * 
	 * @param locale The locale of the current session needed for date
	 *          localization
	 */
	public void updateView(Locale locale) {
		List<CatalogItem> items = libraryManager.getMostViewedCatalogItems(3, getIdentity());
		if(links != null && links.size() == items.size()) {
			for(int i=0;i<items.size(); i++) {
				Link link = links.get(i);
				CatalogItem item = items.get(i);
				link.setUserObject(item);
				link.setCustomDisplayText(item.getDisplayName() + " (" + item.getMetaInfo().getDownloadCount() + ")");
				link.setIconLeftCSS("o_icon ".concat(item.getCssClass()));
			}
		} else {
			links = new ArrayList<>(items.size());
			for (CatalogItem item : items) {
				Link link = LinkFactory.createCustomLink("link" + links.size(), "cmd", "", Link.LINK_CUSTOM_CSS, mainVC, this);
				link.setUserObject(item);
				link.setCustomDisplayText(item.getDisplayName() + " (" + item.getMetaInfo().getDownloadCount() + ")");
				link.setIconLeftCSS("o_icon ".concat(item.getCssClass()));
				mainVC.put(link.getComponentName(), link);
				links.add(link);
			}
		}
		mainVC.contextPut("links", links);
	}
}
