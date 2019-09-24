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

import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.model.OLATResourceableRating;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.modules.library.LibraryManager;
import org.olat.modules.library.ui.event.OpenFileEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * <h3>Description:</h3>
 * This controller shows the 5 better rated files of the library
 * <p>
 * <h4>Events fired by this Controller</h4>
 * <p>
 * Initial Date:  17 déc. 2010 <br>
 * @author srosse, srosse@frentix.com, www.frentix.com
 */
public class MostRatedFilesController extends BasicController {
	private VelocityContainer mainVC;
	private List<Link> links;
	
	private OLATResourceable libraryOres;

	@Autowired
	private LibraryManager libraryManager;
	@Autowired
	private CommentAndRatingService commentAndRatingService;

	/**
	 * Constructor
	 * 
	 * @param libraryCtr
	 * @param mapperBaseURL
	 * @param ureq
	 * @param wControl
	 */
	protected MostRatedFilesController(UserRequest ureq, WindowControl wControl, OLATResourceable libraryOres) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("files");
		mainVC.contextPut("cssClass", "o_library_best_rating");
		this.libraryOres = libraryOres;
		updateView(ureq.getLocale());
		putInitialPanel(mainVC);
	}
	
	protected void updateRepositoryEntry(UserRequest ureq, OLATResourceable newLibraryOres) {
		this.libraryOres = newLibraryOres;
		updateView(ureq.getLocale());
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (links.contains(source)) {
			Link link = (Link) source;
			String cmd = link.getCommand();
			ItemAndRating item = (ItemAndRating) link.getUserObject();
			fireEvent(ureq, new OpenFileEvent(cmd, item.getItem()));
		}
	}

	/**
	 * updates the list of newest files
	 * 
	 * @param locale The locale of the current session needed for date
	 *          localization
	 */
	public void updateView(Locale locale) {
		if(commentAndRatingService == null || libraryOres == null) {
			return;
		}
		
		List<OLATResourceableRating> ratings = commentAndRatingService.getMostRatedResourceables(libraryOres, 15);

		List<ItemAndRating> securedRatings = new ArrayList<>();
		for(OLATResourceableRating rating:ratings) {
			String resSubPath = rating.getResSubPath();
			if(StringHelper.containsNonWhitespace(resSubPath)) {
				CatalogItem item = libraryManager.getCatalogItemByUUID(resSubPath, locale);
				if(item != null) {
					securedRatings.add(new ItemAndRating(item, rating.getRating()));
					if(securedRatings.size() >= 5) {
						break;
					}
				} else {
					commentAndRatingService.deleteAll(libraryOres, resSubPath);
				}
			}
		}
		
		if(links != null && links.size() == securedRatings.size()) {
			for(int i=0;i<securedRatings.size(); i++) {
				Link link = links.get(i);
				ItemAndRating item = securedRatings.get(i);
				link.setUserObject(item);
				link.setCustomDisplayText(item.getDisplayName() + " (" + item.getRating() + ")");
				link.setIconLeftCSS("o_icon " + item.getCssClass());
			}
		} else {
			links = new ArrayList<>(securedRatings.size());
			for (ItemAndRating item : securedRatings) {
				Link link = LinkFactory.createCustomLink("link" + links.size(), "cmd", "", Link.LINK_CUSTOM_CSS, mainVC, this);
				link.setUserObject(item);
				link.setCustomDisplayText(item.getDisplayName() + " (" + item.getRating() + ")");
				link.setIconLeftCSS("o_icon " + item.getCssClass());
				mainVC.put(link.getComponentName(), link);
				links.add(link);
			}
		}
		mainVC.contextPut("links", links);
	}
	
	private static class ItemAndRating {
		private final CatalogItem item;
		private final Double rating;
		
		public ItemAndRating(CatalogItem item, Double rating) {
			this.item = item;
			this.rating = rating;
		}

		public String getDisplayName() {
			return item.getDisplayName();
		}
		
		public String getCssClass() {
			return item.getCssClass();
		}

		public Double getRating() {
			return rating;
		}
		
		public CatalogItem getItem() {
			return item;
		}
	}
}