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
package org.olat.repository.ui.author;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.MainPanel;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * Initial date: 28.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OverviewAuthoringController extends BasicController implements Activateable2, GenericEventListener {
	
	private static final String REPOSITORY_PATH = "[RepositorySite:0]";
	
	private MainPanel mainPanel;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private Link deletedLink;
	private Link favoriteLink;
	private final Link searchLink;
	private final Link myEntriesLink;
	private AuthorListController markedCtrl;
	private AuthorListController currentCtrl;
	private AuthorListController myEntriesCtrl;
	private AuthorListController searchEntriesCtrl;
	private AuthorDeletedListController deletedEntriesCtrl;

	private final boolean isAdministrator;
	private final boolean isGuestOnly;
	private boolean myDirty;
	private boolean favoritDirty;
	private boolean deletedDirty;
	private final EventBus eventBus;
	
	public OverviewAuthoringController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		
		Roles roles = ureq.getUserSession().getRoles();
		isGuestOnly = roles.isGuestOnly();
		isAdministrator = roles.isAdministrator() || roles.isLearnResourceManager();
		
		mainPanel = new MainPanel("authoringMainPanel");
		mainPanel.setDomReplaceable(false);
		mainVC = createVelocityContainer("overview");
		mainPanel.setContent(mainVC);

		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setReselect(true);
		
		BusinessControlFactory bFactory = BusinessControlFactory.getInstance();
		if(!isGuestOnly) {
			favoriteLink = LinkFactory.createLink("search.mark", mainVC, this);
			favoriteLink.setUrl(bFactory.getAuthenticatedURLFromBusinessPathStrings(REPOSITORY_PATH, "[Favorits:0]"));
			segmentView.addSegment(favoriteLink, false);
		}
		myEntriesLink = LinkFactory.createLink("search.my", mainVC, this);
		myEntriesLink.setUrl(bFactory.getAuthenticatedURLFromBusinessPathStrings(REPOSITORY_PATH, "[My:0]"));
		segmentView.addSegment(myEntriesLink, false);
		searchLink = LinkFactory.createLink("search.generic", mainVC, this);
		searchLink.setUrl(bFactory.getAuthenticatedURLFromBusinessPathStrings(REPOSITORY_PATH, "[Search:0]"));
		segmentView.addSegment(searchLink, false);
		if(roles.isAuthor() || isAdministrator) {
			deletedLink = LinkFactory.createLink("search.deleted", mainVC, this);
			deletedLink.setUrl(bFactory.getAuthenticatedURLFromBusinessPathStrings(REPOSITORY_PATH, "[Deleted:0]"));
			segmentView.addSegment(deletedLink, false);
		}

		eventBus = ureq.getUserSession().getSingleUserEventCenter();
		eventBus.registerFor(this, getIdentity(), RepositoryService.REPOSITORY_EVENT_ORES);
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), RepositoryService.REPOSITORY_EVENT_ORES);
		putInitialPanel(mainPanel);
	}
	
	@Override
	protected void doDispose() {
		eventBus.deregisterFor(this, RepositoryService.REPOSITORY_EVENT_ORES);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, RepositoryService.REPOSITORY_EVENT_ORES);
	}
	
	@Override
	public void event(Event event) {
		if(EntryChangedEvent.CHANGE_CMD.equals(event.getCommand()) && event instanceof EntryChangedEvent) {
			EntryChangedEvent ece = (EntryChangedEvent)event;
			if(ece.getChange() == Change.addBookmark
					|| ece.getChange() == Change.removeBookmark
					|| ece.getChange() == Change.added
					|| ece.getChange() == Change.deleted) {
				if(markedCtrl != null && !markedCtrl.getI18nName().equals(ece.getSource())) {
					favoritDirty = true;
				}
				if(myEntriesCtrl != null && !myEntriesCtrl.getI18nName().equals(ece.getSource())) {
					myDirty = true;
				}
				if(deletedEntriesCtrl != null && !deletedEntriesCtrl.getI18nName().equals(ece.getSource())) {
					deletedDirty = true;
				}
			} else if(ece.getChange() == Change.modifiedAccess
					|| ece.getChange() == Change.modifiedAtPublish
					|| ece.getChange() == Change.modifiedDescription) {
				if(markedCtrl != null) {
					markedCtrl.addDirtyRows(ece.getRepositoryEntryKey());
				}
				if(myEntriesCtrl != null) {
					myEntriesCtrl.addDirtyRows(ece.getRepositoryEntryKey());
				}
				if(deletedEntriesCtrl != null) {
					deletedEntriesCtrl.addDirtyRows(ece.getRepositoryEntryKey());
				}
			} else if(ece.getChange() == Change.restored) {
				if(deletedEntriesCtrl != null) {
					deletedDirty = true;
				}
			}
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			if(currentCtrl == null) {
				if(isGuestOnly) {
					doOpenMyEntries(ureq);
					segmentView.select(myEntriesLink);
				} else {
					boolean markEmpty = doOpenMark(ureq).isEmpty();
					if(markEmpty) {
						doOpenMyEntries(ureq);
						segmentView.select(myEntriesLink);
					} else {
						segmentView.select(favoriteLink);
					}
				}
			}
			if(favoritDirty && markedCtrl != null) {
				markedCtrl.reloadRows();
			} else if(markedCtrl != null) {
				markedCtrl.reloadDirtyRows();
			}
			if(myDirty && myEntriesCtrl != null) {
				myEntriesCtrl.reloadRows();
			} else if(myEntriesCtrl != null) {
				myEntriesCtrl.reloadDirtyRows();
			}
			if(deletedDirty && deletedEntriesCtrl != null) {
				deletedEntriesCtrl.reloadRows();
			} else if(deletedEntriesCtrl != null) {
				deletedEntriesCtrl.reloadDirtyRows();
			}
			addToHistory(ureq, currentCtrl);
		} else {
			ContextEntry entry = entries.get(0);
			String segment = entry.getOLATResourceable().getResourceableTypeName();
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			if("Favorits".equals(segment)) {
				if(isGuestOnly) {
					doOpenMyEntries(ureq).activate(ureq, subEntries, entry.getTransientState());
					segmentView.select(myEntriesLink);
				} else {
					doOpenMark(ureq).activate(ureq, subEntries, entry.getTransientState());
					segmentView.select(favoriteLink);
				}
			} else if("My".equals(segment)) {
				doOpenMyEntries(ureq).activate(ureq, subEntries, entry.getTransientState());
				segmentView.select(myEntriesLink);
			} else if("Search".equals(segment)) {
				doSearchEntries(ureq).activate(ureq, subEntries, entry.getTransientState());
				segmentView.select(searchLink);
			} else if("Deleted".equals(segment) && deletedLink != null) {
				doOpenDeletedEntries(ureq).activate(ureq, subEntries, entry.getTransientState());
				segmentView.select(deletedLink);
			} else {
				doOpenMyEntries(ureq).activate(ureq, subEntries, entry.getTransientState());
				segmentView.select(myEntriesLink);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == favoriteLink) {
					doOpenMark(ureq);
				} else if (clickedLink == myEntriesLink) {
					doOpenMyEntries(ureq);
				} else if (clickedLink == searchLink) {
					doSearchEntries(ureq);
				} else if(clickedLink == deletedLink) {
					doOpenDeletedEntries(ureq);
				}
			}
		}
	}

	private AuthorListController doOpenMark(UserRequest ureq) {
		if(markedCtrl == null) {
			SearchAuthorRepositoryEntryViewParams searchParams
				= new SearchAuthorRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
			searchParams.setMarked(Boolean.TRUE);
			searchParams.setOwnedResourcesOnly(false);
	
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Favorits", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			markedCtrl = new AuthorListController(ureq, bwControl, "search.mark", searchParams, false, true);
			listenTo(markedCtrl);
		} else if(favoritDirty) {
			markedCtrl.reloadRows();
		} else {
			markedCtrl.reloadDirtyRows();
		}
		favoritDirty = false;
		
		currentCtrl = markedCtrl;
		addToHistory(ureq, markedCtrl);
		mainVC.put("segmentCmp", markedCtrl.getStackPanel());
		return markedCtrl;
	}
	
	private AuthorListController doOpenMyEntries(UserRequest ureq) {
		if(myEntriesCtrl == null) {
			SearchAuthorRepositoryEntryViewParams searchParams
				= new SearchAuthorRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
			searchParams.setOwnedResourcesOnly(true);
	
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("My", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			myEntriesCtrl = new AuthorListController(ureq, bwControl, "search.my", searchParams, false, true);
			listenTo(myEntriesCtrl);	
		} else if(myDirty) {
			myEntriesCtrl.reloadRows();
		} else {
			myEntriesCtrl.reloadDirtyRows();
		}
		myDirty = false;
		
		currentCtrl = myEntriesCtrl;
		addToHistory(ureq, myEntriesCtrl);
		mainVC.put("segmentCmp", myEntriesCtrl.getStackPanel());
		return myEntriesCtrl;
	}
	
	private AuthorListController doSearchEntries(UserRequest ureq) {
		if(searchEntriesCtrl == null) {
			SearchAuthorRepositoryEntryViewParams searchParams
				= new SearchAuthorRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
			searchParams.setOwnedResourcesOnly(false);
	
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Search", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			searchEntriesCtrl = new AuthorListController(ureq, bwControl, "search.generic", searchParams, true, false);
			listenTo(searchEntriesCtrl);
		}
		
		currentCtrl = searchEntriesCtrl;
		addToHistory(ureq, searchEntriesCtrl);
		mainVC.put("segmentCmp", searchEntriesCtrl.getStackPanel());
		return searchEntriesCtrl;
	}
	
	private AuthorListController doOpenDeletedEntries(UserRequest ureq) {
		if(deletedEntriesCtrl == null) {
			SearchAuthorRepositoryEntryViewParams searchParams
				= new SearchAuthorRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
			if(!isAdministrator) {
				searchParams.setOwnedResourcesOnly(true);
			}
			searchParams.setDeleted(true);
	
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Deleted", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			deletedEntriesCtrl = new AuthorDeletedListController(ureq, bwControl, "search.deleted", searchParams, false);
			listenTo(deletedEntriesCtrl);	
		} else if(deletedDirty) {
			deletedEntriesCtrl.reloadRows();
		} else {
			deletedEntriesCtrl.reloadDirtyRows();
		}
		deletedDirty = false;
		
		currentCtrl = deletedEntriesCtrl;
		addToHistory(ureq, deletedEntriesCtrl);
		mainVC.put("segmentCmp", deletedEntriesCtrl.getStackPanel());
		return deletedEntriesCtrl;
	}
}