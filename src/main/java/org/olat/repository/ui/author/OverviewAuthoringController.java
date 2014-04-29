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
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.MainPanel;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryManager;
import org.olat.repository.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.controllers.RepositoryAddController;
import org.olat.util.logging.activity.LoggingResourceable;

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
	
	private Dropdown createDropdown;
	private Link importLink;

	public OverviewAuthoringController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		
		mainPanel = new MainPanel("authoringMainPanel");
		mainPanel.setDomReplaceable(false);
		mainVC = createVelocityContainer("overview");
		mainPanel.setContent(mainVC);
		
		importLink = LinkFactory.createLink("cmd.import.ressource", getTranslator(), this);
		importLink.setDomReplacementWrapperRequired(false);
		
		createDropdown = new Dropdown("cmd.create.ressource", "cmd.create.ressource", false, getTranslator());
		for(Resources resources:Resources.values()) {
			addCreateLink(resources, createDropdown);
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
	
	private void addCreateLink(Resources resources, Dropdown dropdown) {
		Link createLink = LinkFactory.createLink(resources.getI18nKey(), getTranslator(), this);
		createLink.setUserObject(resources);
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
		} else if(source instanceof Link && ((Link)source).getUserObject() instanceof Resources) {
			Resources resources = (Resources)((Link)source).getUserObject();
			doStartCreate(ureq, resources);
		}
	}
	
	private void doStartImport(UserRequest ureq) {
		
	}
	
	private CloseableModalController cmc;
	private RepositoryAddController addController;
	
	private void doStartCreate(UserRequest ureq, Resources resources) {
		
		removeAsListenerAndDispose(addController);
		addController = new RepositoryAddController(ureq, getWindowControl(), RepositoryAddController.ACTION_NEW_CP);
		listenTo(addController);
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), addController.getInitialComponent(),
				true, addController.getTitle());
		listenTo(cmc);
		cmc.activate();
		
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
	
	private enum Resources {
		courseLink("tools.new.createcourse"),
		cp("tools.new.createcp"),
		wiki("tools.new.wiki"),
		podcast("tools.new.podcast"),
		blog("tools.new.blog"),
		portfolio("tools.new.portfolio"),
		test("tools.new.createtest"),
		survey("tools.new.createsurvey"),
		sharedFolder("tools.new.createsharedfolder"),
		glossaryLink("tools.new.glossary");
		
		private final String i18nKey;
		
		private Resources(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		public String getI18nKey() {
			return i18nKey;
		}
	}
}