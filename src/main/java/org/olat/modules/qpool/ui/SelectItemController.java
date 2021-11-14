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
package org.olat.modules.qpool.ui;

import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.security.QPoolSecurityCallbackFactory;
import org.olat.modules.qpool.ui.datasource.DefaultItemsSource;
import org.olat.modules.qpool.ui.datasource.MarkedItemsSource;
import org.olat.modules.qpool.ui.datasource.MyItemsSource;
import org.olat.modules.qpool.ui.events.QItemViewEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectItemController extends BasicController {
	
	private Link myListsLink;
	private Link mySharesLink;
	private Link ownedItemsLink;
	private Link myCompetencesLink;
	private final Link markedItemsLink;
	private final SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;
	private ItemListController ownedItemsCtrl;
	private ItemListController markedItemsCtrl;
    private ItemListMyListsController myListsCtrl;
	private ItemListMySharesController mySharesCtrl;
	private ItemListMyCompetencesController myCompetencesCtrl;
	
	private final String restrictToFormat;
	private final List<QItemType> excludeTypes;
	
	private final QPoolSecurityCallback secCallback;
	
	@Autowired
	private QuestionPoolModule qpoolModule;
	@Autowired
	private QPoolSecurityCallbackFactory qPoolSecurityCallbackFactory;
	

	public SelectItemController(UserRequest ureq, WindowControl wControl, String restrictToFormat) {
		this(ureq, wControl, restrictToFormat, Collections.emptyList());
	}
	
	public SelectItemController(UserRequest ureq, WindowControl wControl, String restrictToFormat, List<QItemType> excludeTypes) {
		super(ureq, wControl);
		this.excludeTypes = excludeTypes;
		this.restrictToFormat = restrictToFormat;
		mainVC = createVelocityContainer("item_list_overview");
		
		UserSession usess = ureq.getUserSession();
		secCallback = qPoolSecurityCallbackFactory.createQPoolSecurityCallback(usess.getRoles());
		
		int marked = updateMarkedItems(ureq);
		if(marked <= 0) {
			updateOwnedGroups(ureq);
		}
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		markedItemsLink = LinkFactory.createLink("menu.database.favorit", mainVC, this);
		segmentView.addSegment(markedItemsLink, marked > 0);
		ownedItemsLink = LinkFactory.createLink("menu.database.my", mainVC, this);
		segmentView.addSegment(ownedItemsLink, marked <= 0);
        
		if(qpoolModule.isCollectionsEnabled()) {
			myListsLink = LinkFactory.createLink("my.list", mainVC, this);
        		segmentView.addSegment(myListsLink, false);
		}
        if(qpoolModule.isSharesEnabled() || qpoolModule.isPoolsEnabled()) {
        		mySharesLink = LinkFactory.createLink("my.share", mainVC, this);
			segmentView.addSegment(mySharesLink, false);
        }
		if(StringHelper.isLong(qpoolModule.getTaxonomyQPoolKey()) && qpoolModule.isReviewProcessEnabled()) {
			myCompetencesCtrl = new ItemListMyCompetencesController(ureq, getWindowControl(), secCallback, restrictToFormat, excludeTypes);
			listenTo(myCompetencesCtrl);
			if(myCompetencesCtrl.hasCompetences()) {
				myCompetencesLink = LinkFactory.createLink("my.competences", mainVC, this);
				segmentView.addSegment(myCompetencesLink, false);
			}
		}
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == markedItemsLink) {
					updateMarkedItems(ureq);
				} else if (clickedLink == ownedItemsLink){
					updateOwnedGroups(ureq);
				} else if (clickedLink == myListsLink) {
                    updateMyLists(ureq);
                } else if (clickedLink == mySharesLink) {
					updateMyShares(ureq);
				} else if (clickedLink == myCompetencesLink) {
					updateMyCompetences(ureq);
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof QItemViewEvent) {
			if("select-item".equals(event.getCommand())) {
				fireEvent(ureq, event);
			}	
		}
		super.event(ureq, source, event);
	}

	private int updateMarkedItems(UserRequest ureq) {
		if(markedItemsCtrl == null) {
			DefaultItemsSource source = new MarkedItemsSource(getIdentity(), ureq.getUserSession().getRoles(), getLocale(), "Fav");
			source.getDefaultParams().setFavoritOnly(true);
			source.getDefaultParams().setFormat(restrictToFormat);
			source.getDefaultParams().setExcludedItemTypes(excludeTypes);
			markedItemsCtrl = new ItemListController(ureq, getWindowControl(), secCallback, source, restrictToFormat, excludeTypes);
			listenTo(markedItemsCtrl);
		}
		int numOfMarkedItems = markedItemsCtrl.updateList();
		mainVC.put("itemList", markedItemsCtrl.getInitialComponent());
		return numOfMarkedItems;
	}
	
	private void updateOwnedGroups(UserRequest ureq) {
		if(ownedItemsCtrl == null) {
			DefaultItemsSource source = new MyItemsSource(getIdentity(), ureq.getUserSession().getRoles(), getLocale(), "My"); 
			source.getDefaultParams().setAuthor(getIdentity());
			source.getDefaultParams().setFormat(restrictToFormat);
			source.getDefaultParams().setExcludedItemTypes(excludeTypes);
			ownedItemsCtrl = new ItemListController(ureq, getWindowControl(), secCallback, source, restrictToFormat, excludeTypes);
			listenTo(ownedItemsCtrl);
		}
		ownedItemsCtrl.updateList();
		mainVC.put("itemList", ownedItemsCtrl.getInitialComponent());
	}

    private void updateMyLists(UserRequest ureq) {
        if(myListsCtrl == null) {
            myListsCtrl = new ItemListMyListsController(ureq, getWindowControl(), secCallback, restrictToFormat, excludeTypes);
            listenTo(myListsCtrl);
        }
        mainVC.put("itemList", myListsCtrl.getInitialComponent());
    }

	private void updateMyShares(UserRequest ureq) {
		if(mySharesCtrl == null) {
			mySharesCtrl = new ItemListMySharesController(ureq, getWindowControl(), secCallback, restrictToFormat, excludeTypes);
			listenTo(mySharesCtrl);
		}
		mainVC.put("itemList", mySharesCtrl.getInitialComponent());
	}
	
	private void updateMyCompetences(UserRequest ureq) {
		if(myCompetencesCtrl == null) {
			myCompetencesCtrl = new ItemListMyCompetencesController(ureq, getWindowControl(), secCallback, restrictToFormat, excludeTypes);
			listenTo(myCompetencesCtrl);
		}
		mainVC.put("itemList", myCompetencesCtrl.getInitialComponent());
	}
}