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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.portfolio.model.BinderRefImpl;
import org.olat.modules.portfolio.ui.event.OpenBinderEvent;
import org.olat.modules.portfolio.ui.event.OpenMyBindersEvent;
import org.olat.modules.portfolio.ui.event.OpenMyPagesEvent;
import org.olat.modules.portfolio.ui.event.OpenPageEvent;
import org.olat.modules.portfolio.ui.model.BinderRow;
import org.olat.modules.portfolio.ui.shared.MySharedItemsController;
import org.olat.modules.portfolio.ui.shared.SharedItemsOverviewController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 06.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PortfolioHomeController extends BasicController implements Activateable2 {
	
	private final Link myBindersLink;
	private final Link myEntriesLink;
	private final Link mediaCenterLink;
	private final Link goToTrashLink;
	private final Link showHelpLink;
	private final Link createNewEntryLink;
	private final Link mySharedItemsLink;
	private final Link sharedItemsLink;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackPanel;

	private TrashController deletedItemsCtrl;
	private MyPageListController myPageListCtrl;
	private MediaCenterController mediaCenterCtrl;
	private BinderListController myPortfolioListCtrl;
	private MySharedItemsController mySharedItemsCtrl;
	private SharedItemsOverviewController sharedWithMeCtrl;
	
	private LastPageListController lastPagesCtrl;
	private LastBinderListController lastBindersCtrl;
	
	@Autowired
	private PortfolioV2Module portfolioModule;
	@Autowired
	private PortfolioService portfolioService;
	
	public PortfolioHomeController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		stackPanel.setToolbarAutoEnabled(true);
		stackPanel.addListener(this);

		mainVC = createVelocityContainer("home");
		myBindersLink = LinkFactory.createLink("goto.my.binders", mainVC, this);
		myBindersLink.setIconLeftCSS("o_icon o_icon-fw o_icon_pf_binder");
		myBindersLink.setTitle("my.portfolio.binders.text");
		myBindersLink.setElementCssClass("o_sel_pf_my_binders");
		
		myEntriesLink = LinkFactory.createLink("goto.my.pages", mainVC, this);
		myEntriesLink.setIconLeftCSS("o_icon o_icon-fw o_icon_pf_entry");
		myEntriesLink.setTitle("my.entries.text");
		myEntriesLink.setElementCssClass("o_sel_pf_my_entries");
		
		mediaCenterLink = LinkFactory.createLink("goto.media.center", mainVC, this);
		mediaCenterLink.setIconLeftCSS("o_icon o_icon-fw o_icon_mediacenter");
		mediaCenterLink.setTitle("media.center");
		mediaCenterLink.setElementCssClass("o_sel_pf_media_center");
		
		goToTrashLink = LinkFactory.createLink("go.to.trash", mainVC, this);
		goToTrashLink.setIconLeftCSS("o_icon o_icon-fw o_icon_pf_trash");
		goToTrashLink.setElementCssClass("o_sel_pf_trash");
		
		createNewEntryLink = LinkFactory.createLink("new.entry", mainVC, this);
		createNewEntryLink.setIconLeftCSS("o_icon o_icon-fw o_icon_pf_new_entry");
		
		showHelpLink = LinkFactory.createLink("show.help.binder", mainVC, this);
		showHelpLink.setIconLeftCSS("o_icon o_icon-fw o_icon_help");
		
		mySharedItemsLink = LinkFactory.createLink("goto.my.shared.items", mainVC, this);
		mySharedItemsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_pf_my_shares");
		mySharedItemsLink.setTitle("my.shared.items.text");

		sharedItemsLink = LinkFactory.createLink("goto.shared.with.me", mainVC, this);
		sharedItemsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_pf_shared_with_me");
		sharedItemsLink.setTitle("shared.with.me.text");
		sharedItemsLink.setElementCssClass("o_sel_pf_shared_with_me");
		
		lastBindersCtrl = new LastBinderListController(ureq, getWindowControl(), 2);
		listenTo(lastBindersCtrl);
		mainVC.put("lastBinders", lastBindersCtrl.getInitialComponent());
		mainVC.contextPut("hasLastBinders", lastBindersCtrl.hasBinders());
		
		BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getCallbackForMyPageList();
		lastPagesCtrl = new LastPageListController(ureq, getWindowControl(), stackPanel, secCallback, 2);
		listenTo(lastPagesCtrl);
		mainVC.put("lastPages", lastPagesCtrl.getInitialComponent());
		mainVC.contextPut("hasLastPages", lastPagesCtrl.hasPages());
		

		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
        super.doDispose();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(myBindersLink == source) {
			BinderListController bindersCtrl = doOpenMyBinders(ureq);
			if(!portfolioModule.isLearnerCanCreateBinders() && bindersCtrl.getNumOfBinders() == 1) {
				BinderRow row = bindersCtrl.getFirstBinder();
				if(row != null && row.getKey() != null) {
					OLATResourceable resource = OresHelper.createOLATResourceableInstance(Binder.class, row.getKey());
					List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(resource);
					bindersCtrl.activate(ureq, entries, null);
				}
			}
		} else if(myEntriesLink == source) {
			doOpenMyPages(ureq);
		} else if(mySharedItemsLink == source) {
			doOpenMySharedItems(ureq);
		} else if(sharedItemsLink == source) {
			doOpenSharedWithMe(ureq).activate(ureq, null, null);
		} else if(mediaCenterLink == source) {
			doOpenMediaCenter(ureq);
		} else if(createNewEntryLink == source) {
			doNewEntry(ureq);
		} else if(showHelpLink == source) {
			// do nothing
		} else if(goToTrashLink == source) {
			doDeletedPages(ureq);
		} else if(stackPanel == source) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				if(pe.getController() == myPortfolioListCtrl || pe.getController() == myPageListCtrl) {
					lastPagesCtrl.loadModel(ureq, null);
					lastBindersCtrl.loadModel();
				}
			}
			
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(lastBindersCtrl == source) {
			if(event instanceof OpenBinderEvent) {
				OpenBinderEvent openEvent = (OpenBinderEvent)event;
				doOpenBinder(ureq, openEvent.getBinder());
			} else if(event instanceof OpenMyBindersEvent) {
				doOpenMyBinders(ureq);
			}
		} else if(lastPagesCtrl == source) {
			if(event instanceof OpenPageEvent) {
				OpenPageEvent openEvent = (OpenPageEvent)event;
				doOpenPage(ureq, openEvent.getPage());
			} else if(event instanceof OpenMyPagesEvent) {
				doOpenMyPages(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		List<ContextEntry> subEntries = entries.subList(1, entries.size());
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Binder".equalsIgnoreCase(resName)) {
			BinderRef binder = new BinderRefImpl(entries.get(0).getOLATResourceable().getResourceableId());
			if(portfolioService.isMember(binder, getIdentity(), PortfolioRoles.owner.name())) {
				doOpenMyBinders(ureq).activate(ureq, entries, entries.get(0).getTransientState());
			} else {
				doOpenSharedWithMe(ureq).activate(ureq, entries, entries.get(0).getTransientState());
			}
		} else if("MyBinders".equalsIgnoreCase(resName)) {
			doOpenMyBinders(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if("MediaCenter".equalsIgnoreCase(resName)) {
			doOpenMediaCenter(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if("SharedWithMe".equalsIgnoreCase(resName)) {
			doOpenSharedWithMe(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if("MySharedItems".equalsIgnoreCase(resName)) {
			doOpenMySharedItems(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if("MyPages".equalsIgnoreCase(resName)) {
			doOpenMyPages(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
		}
	}
	
	private MediaCenterController doOpenMediaCenter(UserRequest ureq) {
		removeAsListenerAndDispose(mediaCenterCtrl);
		stackPanel.popUpToRootController(ureq);
		
		OLATResourceable pagesOres = OresHelper.createOLATResourceableInstance("MediaCenter", 0l);
		WindowControl swControl = addToHistory(ureq, pagesOres, null);
		mediaCenterCtrl = new MediaCenterController(ureq, swControl, stackPanel);
		listenTo(mediaCenterCtrl);
		stackPanel.pushController(translate("media.center"), mediaCenterCtrl);
		return mediaCenterCtrl;
	}
	
	private SharedItemsOverviewController doOpenSharedWithMe(UserRequest ureq) {
		removeAsListenerAndDispose(sharedWithMeCtrl);
		stackPanel.popUpToRootController(ureq);
		
		OLATResourceable pagesOres = OresHelper.createOLATResourceableInstance("SharedWithMe", 0l);
		WindowControl swControl = addToHistory(ureq, pagesOres, null);
		sharedWithMeCtrl = new SharedItemsOverviewController(ureq, swControl, stackPanel);
		listenTo(sharedWithMeCtrl);
		stackPanel.pushController(translate("shared.with.me"), sharedWithMeCtrl);
		return sharedWithMeCtrl;
	}
	
	private MySharedItemsController doOpenMySharedItems(UserRequest ureq) {
		removeAsListenerAndDispose(mySharedItemsCtrl);
		stackPanel.popUpToRootController(ureq);
		
		OLATResourceable pagesOres = OresHelper.createOLATResourceableInstance("MySharedItems", 0l);
		WindowControl swControl = addToHistory(ureq, pagesOres, null);
		mySharedItemsCtrl = new MySharedItemsController(ureq, swControl, stackPanel);
		listenTo(mySharedItemsCtrl);
		stackPanel.pushController(translate("my.shared.items"), mySharedItemsCtrl);
		return mySharedItemsCtrl;
	}
	
	private void doOpenPage(UserRequest ureq, Page page) {
		if(page == null) {
			//show message
		} else if(page.getSection() == null) {
			MyPageListController ctrl = doOpenMyPages(ureq);
			ctrl.doOpenPage(ureq, page, false);
		} else {
			Binder binder = page.getSection().getBinder();
			List<ContextEntry> entries = new ArrayList<>();
			entries.add(BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableInstance(Binder.class, binder.getKey())));
			entries.add(BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableInstance(Page.class, page.getKey())));
			BinderListController ctrl = doOpenMyBinders(ureq);
			ctrl.activate(ureq, entries, null);
		}
	}
	
	private MyPageListController doOpenMyPages(UserRequest ureq) {
		removeAsListenerAndDispose(myPageListCtrl);
		stackPanel.popUpToRootController(ureq);
		
		OLATResourceable pagesOres = OresHelper.createOLATResourceableInstance("MyPages", 0l);
		WindowControl swControl = addToHistory(ureq, pagesOres, null);
		//owners of all pages
		BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getCallbackForMyPageList();
		myPageListCtrl = new MyPageListController(ureq, swControl, stackPanel, secCallback);
		listenTo(myPageListCtrl);
		stackPanel.pushController(translate("my.portfolio.pages.breadcrump"), myPageListCtrl);
		return myPageListCtrl;
	}
	
	private void doOpenBinder(UserRequest ureq, BinderRef binder) {
		List<ContextEntry> entries = new ArrayList<>();
		entries.add(BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableInstance(Binder.class, binder.getKey())));
		BinderListController ctrl = doOpenMyBinders(ureq);
		ctrl.activate(ureq, entries, null);
	}
	
	private BinderListController doOpenMyBinders(UserRequest ureq) {
		removeAsListenerAndDispose(myPortfolioListCtrl);
		stackPanel.popUpToRootController(ureq);
		
		OLATResourceable bindersOres = OresHelper.createOLATResourceableInstance("MyBinders", 0l);
		WindowControl swControl = addToHistory(ureq, bindersOres, null);
		myPortfolioListCtrl = new BinderListController(ureq, swControl, stackPanel);
		listenTo(myPortfolioListCtrl);
		stackPanel.pushController(translate("my.portfolio.binders.breadcrump"), myPortfolioListCtrl);
		return myPortfolioListCtrl;
	}
	
	private TrashController doDeletedPages(UserRequest ureq) {
		OLATResourceable bindersOres = OresHelper.createOLATResourceableInstance("Trash", 0l);
		WindowControl swControl = addToHistory(ureq, bindersOres, null);
		BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getCallbackForMyPageList();
		deletedItemsCtrl = new TrashController(ureq, swControl, stackPanel, secCallback);
		listenTo(deletedItemsCtrl);
		stackPanel.pushController(translate("deleted.pages.breadcrump"), deletedItemsCtrl);
		return deletedItemsCtrl;
	}

	private void doNewEntry(UserRequest ureq) {
		MyPageListController ctrl = doOpenMyPages(ureq);
		ctrl.doCreateNewPage(ureq);
	}
}
