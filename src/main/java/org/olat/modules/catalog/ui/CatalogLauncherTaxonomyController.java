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
package org.olat.modules.catalog.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogLauncherTaxonomyController extends BasicController {
	
	@Autowired
	private CatalogV2Module catalogModule;

	public CatalogLauncherTaxonomyController(UserRequest ureq, WindowControl wControl, List<TaxonomyLevel> taxonomyLevels, String title) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("launch_taxonomy");
		
		if (taxonomyLevels.isEmpty()) {
			EmptyState emptyState = EmptyStateFactory.create("emptyState", mainVC, this);
			emptyState.setIconCss("o_CourseModule_icon");
		}
		
		mainVC.contextPut("square", CatalogV2Module.TAXONOMY_LEVEL_LAUNCHER_STYLE_SQUARE.equals(catalogModule.getLauncherTaxonomyLevelStyle()));
		
		List<LauncherItem> items = new ArrayList<>(taxonomyLevels.size());
		for (TaxonomyLevel taxonomyLevel : taxonomyLevels) {
			String selectLinkName = "o_tl_" + taxonomyLevel.getKey();
			Link selectLink = LinkFactory.createCustomLink(selectLinkName, "select_tax", selectLinkName, Link.LINK + Link.NONTRANSLATED, mainVC, this);
			selectLink.setCustomDisplayText(taxonomyLevel.getDisplayName());
			selectLink.setUserObject(taxonomyLevel.getKey());
			
			LauncherItem item = new LauncherItem(taxonomyLevel.getKey(), selectLinkName, null);
			items.add(item);
		}
		mainVC.contextPut("items", items);
		
		mainVC.contextPut("title", title);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link link = (Link)source;
			if("select_tax".equals(link.getCommand())) {
				Long key = (Long)link.getUserObject();
				fireEvent(ureq, new OpenTaxonomyEvent(Long.valueOf(key)));
			}
		} else if ("select".equals(event.getCommand())) {
			String key = ureq.getParameter("key");
			fireEvent(ureq, new OpenTaxonomyEvent(Long.valueOf(key)));
		}
	}
	
	public static final class LauncherItem {
		
		private final Long key;
		private final String selectLinkName;
		private final String thumbnailRelPath;
		
		public LauncherItem(Long key, String selectLinkName, String thumbnailRelPath) {
			this.key = key;
			this.selectLinkName = selectLinkName;
			this.thumbnailRelPath = thumbnailRelPath;
		}

		public Long getKey() {
			return key;
		}

		public String getSelectLinkName() {
			return selectLinkName;
		}

		public String getThumbnailRelPath() {
			return thumbnailRelPath;
		}
		
	}

}
