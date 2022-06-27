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
package org.olat.repository.ui.catalog;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
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
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.repository.CatalogEntry;
import org.olat.repository.CatalogEntry.Style;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.CatalogManager;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.olat.repository.ui.CatalogEntryImageMapper;
import org.olat.repository.ui.list.RepositoryEntryListController;
import org.olat.resource.accesscontrol.ACService;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

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
	private RepositoryEntryListController closedEntryListController;

	private final boolean wrapInMainPanel;
	private final MapperKey mapperThumbnailKey;
	private final WindowControl rootwControl;
	
	@Autowired
	private MapperService mapperService;
	@Autowired
	private CatalogManager catalogManager;
	@Autowired
	private ACService acService;
	
	public CatalogNodeController(UserRequest ureq, WindowControl wControl, WindowControl rootwControl,
			CatalogEntry catalogEntry, BreadcrumbedStackedPanel stackPanel, boolean wrapInMainPanel) {
		// fallback translator to repository package to reduce redundant translations
		super(ureq, wControl, Util.createPackageTranslator(RepositoryManager.class, ureq.getLocale()));

		this.stackPanel = stackPanel;
		this.rootwControl = rootwControl;
		this.wrapInMainPanel = wrapInMainPanel;

		VelocityContainer mainVC = createVelocityContainer("node");
		mainVC.setDomReplacementWrapperRequired(false); // uses own DOM ID
		
		//one mapper for all users
		mapperThumbnailKey = mapperService.register(null, "catalogentryImage", new CatalogEntryImageMapper());
		mainVC.contextPut("mapperThumbnailUrl", mapperThumbnailKey.getUrl());
		if(catalogEntry.getStyle() != null) {
			mainVC.contextPut("listStyle", catalogEntry.getStyle().name());
		} else {
			mainVC.contextPut("listStyle", Style.tiles.name());
		}
		mainVC.contextPut("catalogEntryTitle", catalogEntry.getName());
		mainVC.contextPut("catalogEntryShortTitle", catalogEntry.getShortTitle());
		int level  = 0;
		CatalogEntry parent = catalogEntry.getParent();
		while (parent != null) {
			level++;
			parent = parent.getParent();			
		}
		mainVC.contextPut("catalogLevel", level);

		if(StringHelper.containsNonWhitespace(catalogEntry.getDescription())) {
			mainVC.contextPut("catalogEntryDesc", catalogEntry.getDescription());
		}
		VFSLeaf image = catalogManager.getImage(catalogEntry);
		if(image != null) {
			mainVC.contextPut("catThumbnail", image.getName());
		}
		
		List<CatalogEntry> childCe = catalogManager.getChildrenOf(catalogEntry);
		List<CatalogEntry> nodeEntries = childCe.stream().filter(entry -> entry != null && entry.getType() == CatalogEntry.TYPE_NODE).collect(Collectors.toList());
		List<String> subCategories = new ArrayList<>();
		int count = 0;
		boolean tiles = catalogEntry.getStyle() == Style.tiles;

		// Sort nodeEntries
		if (catalogManager.isCategorySortingManually(catalogEntry)) {
			Comparator<CatalogEntry> comparator = Comparator.comparingInt(CatalogEntry::getPosition);
			nodeEntries.sort(comparator);
		} else {
			Collator collator = Collator.getInstance(getLocale());
			collator.setStrength(Collator.IDENTICAL);

			// Sort depending on view type
			nodeEntries.sort(Comparator.comparing(tiles ? CatalogEntry::getShortTitle : CatalogEntry::getName, collator));
		}
		
		for (CatalogEntry entry : nodeEntries) {
			String cmpId = "cat_" + (++count);

			VFSLeaf img = catalogManager.getImage(entry);
			if(img != null) {
				String imgId = "image_" + count;
				mainVC.contextPut(imgId, img.getName());
			}
			mainVC.contextPut("k" + cmpId, entry.getKey());

			String title = StringHelper.escapeHtml(tiles ? entry.getShortTitle() : entry.getName());
			Link link = LinkFactory.createCustomLink(cmpId, "select_node", cmpId, Link.LINK + Link.NONTRANSLATED, mainVC, this);
			link.setCustomDisplayText(title);
			link.setIconLeftCSS("o_icon o_icon_catalog_sub");
			link.setUserObject(entry.getKey());
			subCategories.add(Integer.toString(count));
			String titleId = "title_" + count;
			mainVC.contextPut(titleId, title);
		}

		mainVC.contextPut("subCategories", subCategories);

		//catalog resources
		SearchMyRepositoryEntryViewParams searchParams
			= new SearchMyRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
		searchParams.setParentEntry(catalogEntry);
		searchParams.setEntryStatus(RepositoryEntryStatusEnum.preparationToPublished());
		searchParams.setOfferOrganisations(acService.getOfferOrganisations(searchParams.getIdentity()));
		searchParams.setOfferValidAt(new Date());
		
		entryListController = new RepositoryEntryListController(ureq, wControl, searchParams, true, false, false, false, "catalog", stackPanel);
		if(!entryListController.isEmpty() || searchParams.getFilters() != null) {
			mainVC.put("entries", entryListController.getInitialComponent());
		}
		listenTo(entryListController);
		
		//catalog closed resources
		SearchMyRepositoryEntryViewParams searchClosedParams
				= new SearchMyRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
		searchClosedParams.setParentEntry(catalogEntry);
		searchClosedParams.setEntryStatus(RepositoryEntryStatusEnum.closed());
		
		closedEntryListController = new RepositoryEntryListController(ureq, wControl, searchClosedParams, true, false, false, false, "catalog-closed", stackPanel);

		if(!closedEntryListController.isEmpty() || searchClosedParams.getFilters() != null) {
			mainVC.put("closedEntries", closedEntryListController.getInitialComponent());
		}
		listenTo(closedEntryListController);

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
				CatalogEntry entry = catalogManager.getCatalogNodeByKey(categoryNodeKey);
				selectCatalogEntry(ureq, entry);
			}
		} else if("img_select".equals(event.getCommand())) {
			String node = ureq.getParameter("node");
			if(StringHelper.isLong(node)) {
				try {
					Long categoryNodeKey = Long.valueOf(node);
					CatalogEntry entry = catalogManager.getCatalogNodeByKey(categoryNodeKey);
					selectCatalogEntry(ureq, entry);
				} catch (NumberFormatException e) {
					logWarn("Not a valid long: " + node, e);
				}
			}
		}
	}
	
	private CatalogNodeController selectCatalogEntry(UserRequest ureq, CatalogEntry entry) {
		if(entry != null && entry.getType() == CatalogEntry.TYPE_NODE) {
			removeAsListenerAndDispose(childNodeController);
			
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("CatalogEntry", entry.getKey());
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, rootwControl);
			
			childNodeController = new CatalogNodeController(ureq, bwControl, rootwControl, entry, stackPanel, wrapInMainPanel);
			listenTo(childNodeController);
			stackPanel.pushController(entry.getShortTitle(), childNodeController);
			
			addToHistory(ureq, childNodeController);
		}
		return childNodeController;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			return;
		}
		
		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		if("CatalogEntry".equalsIgnoreCase(type)) {
			Long entryKey = entry.getOLATResourceable().getResourceableId();
			if(entryKey != null && entryKey.longValue() > 0) {
				activateRoot(ureq, entryKey); 
			}
		} else if("Node".equalsIgnoreCase(type)) {
			//the "Node" is only for internal usage
			StateEntry stateEntry = entry.getTransientState();
			if(stateEntry instanceof CatalogStateEntry) {
				CatalogEntry catalogEntry = ((CatalogStateEntry)stateEntry).getEntry();
				CatalogNodeController nextCtrl = selectCatalogEntry(ureq, catalogEntry);
				if(nextCtrl != null && entries.size() > 1) {
					nextCtrl.activate(ureq, entries.subList(1, entries.size()), null);
				}
			}
		}
	}
	
	/**
	 * Build an internal business path made of "Node" with the category
	 * as state entry to prevent loading several times the same entries.
	 * 
	 * @param ureq
	 * @param entryKey
	 */
	private void activateRoot(UserRequest ureq, Long entryKey) {
		List<ContextEntry> parentLine = new ArrayList<>();
		for(CatalogEntry node = catalogManager.getCatalogEntryByKey(entryKey); node != null && node.getParent() != null; node=node.getParent()) {
			OLATResourceable nodeRes = OresHelper.createOLATResourceableInstance("Node", node.getKey());
			ContextEntry ctxEntry = BusinessControlFactory.getInstance().createContextEntry(nodeRes);
			ctxEntry.setTransientState(new CatalogStateEntry(node));
			parentLine.add(ctxEntry);
		}
		Collections.reverse(parentLine);
		activate(ureq, parentLine, null);
	}
}
