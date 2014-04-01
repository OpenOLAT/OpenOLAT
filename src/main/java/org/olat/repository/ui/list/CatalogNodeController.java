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
package org.olat.repository.ui.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.catalog.CatalogEntry;
import org.olat.catalog.CatalogManager;
import org.olat.catalog.ui.CatalogEntryComparator;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.MainPanel;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.repository.RepositoryManager;
import org.olat.repository.SearchMyRepositoryEntryViewParams;
import org.olat.repository.ui.CatalogEntryImageMapper;

/**
 * 
 * Initial date: 28.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogNodeController extends BasicController implements Activateable2 {

	private CatalogNodeController childNodeController;
	private BreadcrumbedStackedPanel stackPanel;
	private RepositoryEntryListController entryListController;

	private final CatalogManager cm;
	private final boolean wrapInMainPanel;
	private final String mapperThumbnailUrl;
	
	public CatalogNodeController(UserRequest ureq, WindowControl wControl, CatalogEntry catalogEntry,
			BreadcrumbedStackedPanel stackPanel, boolean wrapInMainPanel) {
		// fallback translator to repository package to reduce redundant translations
		super(ureq, wControl, Util.createPackageTranslator(RepositoryManager.class, ureq.getLocale()));

		cm = CatalogManager.getInstance();
		this.stackPanel = stackPanel;
		this.wrapInMainPanel = wrapInMainPanel;

		VelocityContainer mainVC = createVelocityContainer("node");
		
		mapperThumbnailUrl = registerCacheableMapper(ureq, "catalogentryImage", new CatalogEntryImageMapper());
		mainVC.contextPut("mapperThumbnailUrl", mapperThumbnailUrl);
		
		mainVC.contextPut("catalogEntryName", catalogEntry.getName());
		if(StringHelper.containsNonWhitespace(catalogEntry.getDescription())) {
			mainVC.contextPut("catalogEntryDesc", catalogEntry.getDescription());
		}
		VFSLeaf image = cm.getImage(catalogEntry);
		if(image != null) {
			mainVC.contextPut("catThumbnail", catalogEntry.getKey());
		}
		
		List<CatalogEntry> childCe = cm.getNodesChildrenOf(catalogEntry);
		Collections.sort(childCe, new CatalogEntryComparator(getLocale()));
		List<String> subCategories = new ArrayList<>();
		int count = 0;
		for (CatalogEntry entry : childCe) {
			if(entry.getType() == CatalogEntry.TYPE_NODE) {
				String cmpId = "cat_" + (++count);
				
				VFSLeaf img = cm.getImage(entry);
				if(img != null) {
					String imgId = "image_" + count;
					mainVC.contextPut(imgId, entry.getKey());
				}
				
				Link link = LinkFactory.createCustomLink(cmpId, "select_node", cmpId, Link.LINK + Link.NONTRANSLATED, mainVC, this);
				link.setCustomDisplayText(entry.getName());
				link.setUserObject(entry.getKey());
				subCategories.add(Integer.toString(count));
			}
		}
		mainVC.contextPut("subCategories", subCategories);

		//catalog resources
		SearchMyRepositoryEntryViewParams searchParams
			= new SearchMyRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles(), "CourseModule");
		searchParams.setParentEntry(catalogEntry);
		entryListController = new RepositoryEntryListController(ureq, wControl, searchParams, stackPanel);
		mainVC.put("entries", entryListController.getInitialComponent());
		listenTo(entryListController);

		if(wrapInMainPanel) {
			MainPanel mainPanel = new MainPanel("myCoursesMainPanel");
			mainPanel.setContent(mainVC);
			putInitialPanel(mainPanel);
		} else {
			putInitialPanel(mainVC);
		}
	}


	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link) {
			Link link = (Link)source;
			if("select_node".equals(link.getCommand())) {
				Long categoryNodeKey = (Long)link.getUserObject();
				CatalogEntry entry = cm.getCatalogNodeByKey(categoryNodeKey);
				if(entry != null && entry.getType() == CatalogEntry.TYPE_NODE) {
					removeAsListenerAndDispose(childNodeController);
					childNodeController = new CatalogNodeController(ureq, getWindowControl(), entry, stackPanel, wrapInMainPanel);
					listenTo(childNodeController);
					stackPanel.pushController(entry.getName(), childNodeController);	
				}
			}
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
	}
}