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
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.MainPanel;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OverviewAuthoringController extends BasicController implements Activateable2 {
	
	private MainPanel mainPanel;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link favoriteLink, myEntriesLink, searchLink;
	private AuthorListController markedCtrl, myEntriesCtrl, searchEntriesCtrl;
	private TooledStackedPanel markedStackedPanel, myEntriesStackedPanel, searchEntriesStackedPanel;
	
	private CloseableModalController cmc;
	private StepsMainRunController wizardCtrl;
	private ImportRepositoryEntryController importCtrl;
	private CreateRepositoryEntryController createCtrl;
	
	private Dropdown createDropdown;
	private Link importLink;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	
	public OverviewAuthoringController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		
		mainPanel = new MainPanel("authoringMainPanel");
		mainPanel.setDomReplaceable(false);
		mainVC = createVelocityContainer("overview");
		mainPanel.setContent(mainVC);
		
		importLink = LinkFactory.createLink("cmd.import.ressource", getTranslator(), this);
		importLink.setDomReplacementWrapperRequired(false);
		
		Set<String> types = repositoryHandlerFactory.getSupportedTypes();

		createDropdown = new Dropdown("cmd.create.ressource", "cmd.create.ressource", false, getTranslator());
		for(String type:types) {
			RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(type);
			if(handler != null && handler.isCreate()) {
				addCreateLink(handler, createDropdown);
			}
		}

		boolean markEmpty = doOpenMark(ureq).isEmpty();
		if(markEmpty) {
			doOpenMyEntries(ureq);
		}
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		favoriteLink = LinkFactory.createLink("search.mark", mainVC, this);
		segmentView.addSegment(favoriteLink, !markEmpty);
		myEntriesLink = LinkFactory.createLink("search.my", mainVC, this);
		segmentView.addSegment(myEntriesLink, markEmpty);
		searchLink = LinkFactory.createLink("search.generic", mainVC, this);
		segmentView.addSegment(searchLink, markEmpty);

		putInitialPanel(mainPanel);
	}
	
	private void addCreateLink(RepositoryHandler handler, Dropdown dropdown) {
		String name = handler.getSupportedTypes().get(0);
		Link createLink = LinkFactory.createLink(name, getTranslator(), this);
		createLink.setUserObject(handler);
		dropdown.addComponent(createLink);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
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
				}
			}
		} else if(importLink == source) {
			doStartImport(ureq);
		} else if(source instanceof Link && ((Link)source).getUserObject() instanceof RepositoryHandler) {
			RepositoryHandler resources = (RepositoryHandler)((Link)source).getUserObject();
			doCreate(ureq, resources);
		}
	}

	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(cmc == source) {
			cleanUp();
		} else if(createCtrl == source) {
			cmc.deactivate();
			if(Event.DONE_EVENT.equals(event)) {
				doOpenDetailsInMyEntries(ureq, createCtrl.getAddedEntry(), true);
				cleanUp();
			} else if(CreateRepositoryEntryController.CREATION_WIZARD.equals(event)) {
				doStartPostCreateWizard(ureq, createCtrl.getAddedEntry(), createCtrl.getHandler());
			} else {
				cleanUp();
			}
		}  else if(importCtrl == source) {
			cmc.deactivate();
			if(Event.DONE_EVENT.equals(event)) {
				doOpenDetailsInMyEntries(ureq, importCtrl.getImportedEntry(), true);
				cleanUp();
			} else {
				cleanUp();
			}
		} else if(wizardCtrl == source) {
			if (event.equals(Event.CHANGED_EVENT) || event.equals(Event.CANCELLED_EVENT)) {
				getWindowControl().pop();
				RepositoryEntry newEntry = (RepositoryEntry)wizardCtrl.getRunContext().get("authoringNewEntry");
				cleanUp();
				doOpenDetailsInMyEntries(ureq, newEntry, true);
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(createCtrl);
		removeAsListenerAndDispose(importCtrl);
		removeAsListenerAndDispose(wizardCtrl);
		removeAsListenerAndDispose(cmc);
		createCtrl = null;
		importCtrl = null;
		wizardCtrl = null;
		cmc = null;
	}

	private void doStartImport(UserRequest ureq) {
		if(importCtrl != null) return;

		removeAsListenerAndDispose(importCtrl);
		importCtrl = new ImportRepositoryEntryController(ureq, getWindowControl());
		listenTo(importCtrl);
		removeAsListenerAndDispose(cmc);
		
		String title = translate("cmd.import.ressource");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), importCtrl.getInitialComponent(),
				true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreate(UserRequest ureq, RepositoryHandler handler) {
		if(createCtrl != null) return;

		removeAsListenerAndDispose(createCtrl);
		createCtrl = new CreateRepositoryEntryController(ureq, getWindowControl(), handler);
		listenTo(createCtrl);
		removeAsListenerAndDispose(cmc);
		
		String title = translate(handler.getCreateLabelI18nKey());
		cmc = new CloseableModalController(getWindowControl(), translate("close"), createCtrl.getInitialComponent(),
				true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenDetailsInMyEntries(UserRequest ureq, RepositoryEntry newEntry, boolean edit) {
		myEntriesCtrl = doOpenMyEntries(ureq);
		
		String fullname = userManager.getUserDisplayName(newEntry.getInitialAuthor());
		AuthoringEntryRow row = new AuthoringEntryRow(newEntry, fullname);
		if(edit) {
			myEntriesCtrl.doOpenDetails(ureq, row);
		} else {
			myEntriesCtrl.doOpenDetails(ureq, row);
		}
	}
	
	private void doStartPostCreateWizard(UserRequest ureq, RepositoryEntry newEntry, RepositoryHandler handler) {
		if(wizardCtrl != null) return;
		
		cleanUp();
		wizardCtrl = handler.createWizardController(newEntry, ureq, getWindowControl());
		wizardCtrl.getRunContext().put("authoringNewEntry", newEntry);
		listenTo(wizardCtrl);
		getWindowControl().pushAsModalDialog(wizardCtrl.getInitialComponent());
	}

	private AuthorListController doOpenMark(UserRequest ureq) {
		if(markedCtrl == null) {
			SearchAuthorRepositoryEntryViewParams searchParams
				= new SearchAuthorRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
			searchParams.setMarked(Boolean.TRUE);

			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Favorits", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			markedStackedPanel = createStandardTooledPanel("markStackPanel");
			markedCtrl = new AuthorListController(ureq, bwControl, markedStackedPanel, searchParams);
			markedStackedPanel.pushController(translate("search.mark"), markedCtrl);
			listenTo(markedCtrl);
		}
		
		addToHistory(ureq, markedCtrl);
		mainVC.put("segmentCmp", markedStackedPanel);
		return markedCtrl;
	}
	
	private AuthorListController doOpenMyEntries(UserRequest ureq) {
		if(myEntriesCtrl == null) {
			SearchAuthorRepositoryEntryViewParams searchParams
				= new SearchAuthorRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());

			OLATResourceable ores = OresHelper.createOLATResourceableInstance("My", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			myEntriesStackedPanel = createStandardTooledPanel("myEntriesStackPanel");
			myEntriesCtrl = new AuthorListController(ureq, bwControl, myEntriesStackedPanel, searchParams);
			myEntriesStackedPanel.pushController(translate("search.my"), myEntriesCtrl);
			listenTo(myEntriesCtrl);
		}

		addToHistory(ureq, myEntriesCtrl);
		mainVC.put("segmentCmp", myEntriesStackedPanel);
		return myEntriesCtrl;
	}
	
	private AuthorListController doSearchEntries(UserRequest ureq) {
		if(searchEntriesCtrl == null) {
			SearchAuthorRepositoryEntryViewParams searchParams
				= new SearchAuthorRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());

			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Search", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			searchEntriesStackedPanel = createStandardTooledPanel("searchEntriesStackPanel");
			searchEntriesCtrl = new AuthorListController(ureq, bwControl, searchEntriesStackedPanel, searchParams);
			searchEntriesStackedPanel.pushController(translate("search.generic"), searchEntriesCtrl);
			listenTo(searchEntriesCtrl);
		}
		
		addToHistory(ureq, searchEntriesCtrl);
		mainVC.put("segmentCmp", searchEntriesStackedPanel);
		return searchEntriesCtrl;
	}
	
	
	private TooledStackedPanel createStandardTooledPanel(String name) {
		TooledStackedPanel stackedPanel = new TooledStackedPanel(name, getTranslator(), this);
		stackedPanel.addTool(importLink, true);
		stackedPanel.addTool(createDropdown, true);
		return stackedPanel;
	}
}