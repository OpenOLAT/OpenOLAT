/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.modules.wiki;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.Dropdown.Spacer;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumCallback;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.modules.fo.ui.ForumController;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.portfolio.ui.component.MediaCollectorComponent;
import org.olat.modules.wiki.WikiPageSort.WikiFileComparator;
import org.olat.modules.wiki.WikiPageSort.WikiPageNameComparator;
import org.olat.modules.wiki.gui.components.wikiToHtml.ErrorEvent;
import org.olat.modules.wiki.gui.components.wikiToHtml.FilterUtil;
import org.olat.modules.wiki.gui.components.wikiToHtml.RequestImageEvent;
import org.olat.modules.wiki.gui.components.wikiToHtml.RequestMediaEvent;
import org.olat.modules.wiki.gui.components.wikiToHtml.RequestNewPageEvent;
import org.olat.modules.wiki.gui.components.wikiToHtml.RequestPageEvent;
import org.olat.modules.wiki.gui.components.wikiToHtml.WikiMarkupComponent;
import org.olat.modules.wiki.portfolio.WikiMediaHandler;
import org.olat.modules.wiki.versioning.ChangeInfo;
import org.olat.modules.wiki.versioning.HistoryTableDateModel;
import org.olat.search.SearchModule;
import org.olat.search.SearchServiceUIFactory;
import org.olat.search.SearchServiceUIFactory.DisplayOption;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * This controller creates the whole GUI for a wiki with a tabbed pane contaning
 * an article view, per page forum view, edit pane and versioning pane. The
 * rendering of the wiki syntax to html is done by @see
 * org.olat.core.gui.components.wikiToHtml.WikiMarkupComponent.
 * <P>
 * Initial Date: May 4, 2006 <br>
 * 
 * @author guido
 */
public class WikiMainController extends BasicController implements Activateable2 {

	private static final Logger log = Tracing.createLoggerFor(WikiMainController.class);

	private TabbedPane tabs;
	private WikiPage selectedPage;
	private GenericTreeModel wikiMenuModel;
	private String pageId;
	private VFSContainer wikiContainer;
	private OLATResourceable ores;
	private VelocityContainer articleContent, navigationContent, discussionContent, editContent, content,
			versioningContent, imageDisplay;
	private ForumController forumController;
	private WikiEditArticleForm wikiEditForm;
	private WikiMarkupComponent wikiArticleComp, wikiVersionDisplayComp;
	private ContextualSubscriptionController cSubscriptionCtrl;
	private TableController versioningTableCtr;
	private WikiFileUploadController wikiUploadFileCtr;
	private HistoryTableDateModel versioningTableModel;
	private DialogBoxController removePageDialogCtr, archiveWikiDialogCtr;
	private List<ChangeInfo> diffs = new ArrayList<>(2);
	private SubscriptionContext subsContext;
	private LockResult lockEntry;
	private Link archiveLink, closePreviewButton, createLink, toMainPageLink, a2zLink, changesLink, editMenuButton,
			revertVersionButton;
	private TableController mediaTableCtr;
	private MediaFilesTableModel mediaFilesTableModel;
	private TableGuiConfiguration tableConfig;
	private WikiSecurityCallback securityCallback;
	private Controller searchCtrl;
	private WikiArticleSearchForm createArticleForm;
	private CloseableCalloutWindowController calloutCtrl;
	private CloseableModalController cmc;
	private CloseableModalController mediaCmc;
	private StackedPanel mainPanel;

	private Dropdown wikiMenuDropdown, navigationDropdown, breadcrumpDropdown;
	private GenericTreeNode navMainPageNode, navAZNode, navChangesNode, wikiMenuNode;
	private WikiAssessmentProvider assessmentProvider;

	public static final String ACTION_COMPARE = "compare";
	public static final String ACTION_SHOW = "view.version";
	private static final String ACTION_EDIT_MENU = "editMenu";
	private static final String ACTION_CLOSE_PREVIEW = "preview.close";
	private static final String ACTION_DELETE_MEDIAS = "delete.medias";
	private static final String ACTION_DELETE_MEDIA = "delete.media";
	protected static final String ACTION_SHOW_MEDIA = "show.media";
	protected static final String METADATA_SUFFIX = ".metadata";
	private static final String MEDIA_FILE_FILENAME = "filename";
	private static final String MEDIA_FILE_CREATIONDATE = "creation.date";
	private static final String MEDIA_FILE_CREATED_BY = "created.by";
	private static final String MEDIA_FILE_DELETIONDATE = "deleted.at";
	private static final String MEDIA_FILE_DELETED_BY = "deleted.by";

	// indicates if user is already on image-detail-view-page (OLAT-6233)
	private boolean isImageDetailView = false;
	@Autowired
	private SearchModule searchModule;
	@Autowired
	private WikiMediaHandler wikiMediaHandler;
	@Autowired
	private PortfolioV2Module portfolioModule;
	@Autowired
	private ForumManager forumManager;
	@Autowired
	private NotificationsManager notificationsManager;

	public WikiMainController(UserRequest ureq, WindowControl wControl, OLATResourceable ores,
			WikiSecurityCallback securityCallback, WikiAssessmentProvider assessmentProvider, String initialPageName) {
		super(ureq, wControl);

		this.wikiContainer = WikiManager.getInstance().getWikiRootContainer(ores);
		this.ores = ores;
		this.securityCallback = securityCallback;
		this.subsContext = securityCallback.getSubscriptionContext();
		this.assessmentProvider = assessmentProvider;

		WikiPage page = null;
		Wiki wiki = getWiki();
		if (wiki == null) {
			VelocityContainer vc = createVelocityContainer("deleted");
			mainPanel = putInitialPanel(vc);
			return;
		}

		if (!ores.getResourceableTypeName().equals("BusinessGroup")) {
			addLoggingResourceable(LoggingResourceable.wrap(ores, OlatResourceableType.genRepoEntry));
		}
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_OPEN, getClass());
		// init the first page either startpage or an other page identified by
		// initial page name
		if (initialPageName != null && wiki.pageExists(WikiManager.generatePageId(initialPageName))) {
			page = wiki.getPage(initialPageName, true);
		} else {
			page = wiki.getPage(WikiPage.WIKI_INDEX_PAGE, true);
			if (initialPageName != null) {
				showError("wiki.error.page.not.found");
			}
		}
		this.pageId = page.getPageId();

		tabs = new TabbedPane("userTabP", ureq.getLocale());
		tabs.addListener(this);
		// init the tabbed pane container
		content = createVelocityContainer("index");

		// navigation container
		navigationContent = createVelocityContainer("navigation");
		navigationContent.contextPut("navigationEnabled", Boolean.TRUE);

		// add a history that displays visited pages
		breadcrumpDropdown = new Dropdown("breadcrump", "navigation.history", false, getTranslator());
		breadcrumpDropdown.setElementCssClass("o_menu");
		Link indexLink = LinkFactory.createToolLink(WikiPage.WIKI_INDEX_PAGE, "select-page", WikiPage.WIKI_INDEX_PAGE,
				this);
		breadcrumpDropdown.addComponent(indexLink);
		navigationContent.put("breadcrumb", breadcrumpDropdown);

		if (subsContext != null) {
			String businnessPath = wControl.getBusinessControl().getAsString();
			PublisherData data = new PublisherData(OresHelper.calculateTypeName(WikiPage.class), null, businnessPath);
			cSubscriptionCtrl = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, data);
			listenTo(cSubscriptionCtrl);
			navigationContent.put("subscription", cSubscriptionCtrl.getInitialComponent());
		}

		navigationDropdown = new Dropdown("navi", "navigation.navigation", false, getTranslator());
		navigationDropdown.setElementCssClass("o_menu");
		navigationContent.put("navi", navigationDropdown);

		toMainPageLink = LinkFactory.createLink("navigation.mainpage", navigationContent, this);
		toMainPageLink.setDomReplaceable(false);
		toMainPageLink.setIconLeftCSS("o_icon o_icon_home");
		navigationDropdown.addComponent(toMainPageLink);

		a2zLink = LinkFactory.createLink("navigation.a-z", navigationContent, this);
		a2zLink.setDomReplacementWrapperRequired(false);
		navigationDropdown.addComponent(a2zLink);

		changesLink = LinkFactory.createLink("navigation.changes", navigationContent, this);
		changesLink.setDomReplacementWrapperRequired(false);
		navigationDropdown.addComponent(changesLink);

		archiveLink = LinkFactory.createLink("archive.wiki", navigationContent, this);
		archiveLink.setIconLeftCSS("o_icon o_icon_archive_tool");
		archiveLink.setDomReplacementWrapperRequired(false);
		archiveLink.setTitle("archive.wiki.title");

		if (securityCallback.mayEditAndCreateArticle()) {
			createLink = LinkFactory.createLink("navigation.create.article", navigationContent, this);
			createLink.setIconLeftCSS("o_icon o_icon_create");
			createLink.setElementCssClass("o_sel_wiki_create_page");
			createLink.setDomReplacementWrapperRequired(false);
		}

		content.put("navigation", navigationContent);

		// search
		if (searchModule.isSearchAllowed(ureq.getUserSession().getRoles())) {
			SearchServiceUIFactory searchServiceUIFactory = (SearchServiceUIFactory) CoreSpringFactory
					.getBean(SearchServiceUIFactory.class);
			searchCtrl = searchServiceUIFactory.createInputController(ureq, wControl, DisplayOption.STANDARD, null);
			listenTo(searchCtrl);
			navigationContent.put("search_article", searchCtrl.getInitialComponent());
		}

		// attach menu
		wikiMenuDropdown = new Dropdown("wikiMenu", "navigation.menu", false, getTranslator());
		wikiMenuDropdown.setElementCssClass("o_menu");
		if (securityCallback.mayEditWikiMenu()) {
			editMenuButton = LinkFactory.createLink("edit.menu", navigationContent, this);
			editMenuButton.setIconLeftCSS("o_icon o_icon_edit");
		}
		navigationContent.put("wikiMenuDropdown", wikiMenuDropdown);
		updateWikiMenu(wiki);

		// attach index article
		wikiArticleComp = new WikiMarkupComponent("wikiArticle", ores, 300);
		wikiArticleComp.addListener(this);
		wikiArticleComp.setImageMapperUri(ureq, wikiContainer);
		navigationContent.put("wikiArticle", wikiArticleComp);

		/***************************************************************************
		 * wiki component
		 **************************************************************************/
		articleContent = createVelocityContainer("article");
		articleContent.put("wikiArticle", wikiArticleComp);
		tabs.addTab(translate("tab.article"), articleContent);

		/***************************************************************************
		 * discussion container
		 **************************************************************************/
		discussionContent = createVelocityContainer("discuss");
		tabs.addTab(translate("tab.discuss"), discussionContent);

		/***************************************************************************
		 * edit container
		 **************************************************************************/
		editContent = createVelocityContainer("edit");
		imageDisplay = createVelocityContainer("imagedisplay");
		closePreviewButton = LinkFactory.createButtonSmall(ACTION_CLOSE_PREVIEW, editContent, this);

		editContent.contextPut("isGuest", Boolean.valueOf(ureq.getUserSession().getRoles().isGuestOnly()));
		wikiEditForm = new WikiEditArticleForm(ureq, wControl, page, securityCallback);
		listenTo(wikiEditForm);
		editContent.contextPut("editformid", "ofo_" + wikiEditForm.hashCode());

		editContent.put("editForm", wikiEditForm.getInitialComponent());

		JSAndCSSComponent js = new JSAndCSSComponent("js", new String[] { "js/openolat/wiki.js" }, null);
		content.put("js", js);
		
		updateFileAndLinkList(wiki);

		tabs.addTab(translate("tab.edit"), editContent);

		/***************************************************************************
		 * version container
		 **************************************************************************/
		versioningContent = createVelocityContainer("versions");
		wikiVersionDisplayComp = new WikiMarkupComponent("versionDisplay", ores, 300);
		wikiVersionDisplayComp.addListener(this);
		wikiVersionDisplayComp.setImageMapperUri(ureq, wikiContainer);
		tabs.addTab(translate("tab.versions"), versioningContent);
		if (securityCallback.mayEditAndCreateArticle()) {
			revertVersionButton = LinkFactory.createButton("revert.old.version", versioningContent, this);
		}

		tableConfig = new TableGuiConfiguration();
		tableConfig.setPageingEnabled(true);
		tableConfig.setResultsPerPage(10);
		tableConfig.setSelectedRowUnselectable(true);

		content.put("wikiTabs", tabs);
		// if not content yet switch to the edit tab
		if (page.getContent().equals("") && securityCallback.mayEditAndCreateArticle()) {
			tabs.setSelectedPane(ureq, 2);
			tryToSetEditLock(page, ureq, ores);
		}
		updatePageContext(ureq, page);
		setTabsEnabled(true); // apply security settings to tabs by may
								// disabling edit tab
		mainPanel = putInitialPanel(content);

		// set pageId to the latest used
		this.pageId = page.getPageId();
	}
	
	private void updateFileAndLinkList(Wiki wiki) {
		List<VFSItem> mediaFiles = wiki.getMediaFileList();
		Collections.sort(mediaFiles, new WikiFileComparator(getLocale()));
		editContent.contextPut("fileList", mediaFiles);
		List<String> linkList = wiki.getAllPages().stream()
				.filter(Wiki.REGULAR_PAGE_FILTER)
				.map(WikiPage::getPageName)
				.sorted(new WikiPageNameComparator(getLocale()))
				.collect(Collectors.toList());
		editContent.contextPut("linkList", linkList);
	}

	private void updateWikiMenu(Wiki wiki) {
		List<WikiPage> pages = wiki.getAllPages().stream()
				.filter(Wiki.REGULAR_PAGE_FILTER)
				.sorted(WikiPageSort.PAGENAME_ORDER)
				.collect(Collectors.toList());
		
		if (wikiMenuNode != null) {
			wikiMenuNode.removeAllChildren();
			for (WikiPage page : pages) {
				String link = page.getPageName();
				String ident = "w" + Encoder.md5hash(link);
				GenericTreeNode menuItemNode = new GenericTreeNode(ident, link, link);
				String cssClass = getNodeCssClass(page.getPageId());
				menuItemNode.setCssClass(cssClass);
				wikiMenuNode.addChild(menuItemNode);
			}
		}
		
		wikiMenuDropdown.removeAllComponents();
		for (WikiPage page : pages) {
			String link = page.getPageName();
			Link menuLink = LinkFactory.createToolLink(link, "select-page", link, this);
			wikiMenuDropdown.addComponent(menuLink);
		}
		if (editMenuButton != null) {
			wikiMenuDropdown.addComponent(new Spacer("wiki-spacer"));
			wikiMenuDropdown.addComponent(editMenuButton);
		}
	}

	public GenericTreeModel getAndUseExternalTree() {
		final String resId = ores.getResourceableId().toString();
		Wiki wiki = getWiki();
		wikiMenuModel = new GenericTreeModel();

		String root = "wiki-" + resId;
		GenericTreeNode rootNode = new GenericTreeNode(root);
		wikiMenuModel.setRootNode(rootNode);

		// Index
		String navMainItem = "nav-main-item-" + resId;
		navMainPageNode = new GenericTreeNode(navMainItem, translate("navigation.mainpage"), navMainItem);
		navMainPageNode.setCssClass(getNodeDoneCssClass());
		rootNode.addChild(navMainPageNode);

		// Wiki-Menu
		String wikiMenuTitle = translate("navigation.menu");
		String wikiMenuItem = "menu-item-" + resId;
		wikiMenuNode = new GenericTreeNode(wikiMenuItem, wikiMenuTitle, wikiMenuItem);
		wikiMenuNode.setCssClass(getNodeDoneCssClass());
		rootNode.addChild(wikiMenuNode);

		String navAZItem = "nav-az-item-" + resId;
		navAZNode = new GenericTreeNode(navAZItem, translate("navigation.a-z"), navAZItem);
		navAZNode.setCssClass(getNodeDoneCssClass());
		rootNode.addChild(navAZNode);

		String navChangesItem = "nav-changes-item-" + resId;
		navChangesNode = new GenericTreeNode(navChangesItem, translate("navigation.changes"), navChangesItem);
		navChangesNode.setCssClass(getNodeDoneCssClass());
		rootNode.addChild(navChangesNode);

		updateWikiMenu(wiki);

		navigationDropdown.setVisible(false);
		wikiMenuDropdown.setVisible(false);

		navigationContent.contextPut("navigationEnabled", Boolean.FALSE);
		return wikiMenuModel;
	}
	
	private String getNodeCssClass(String pageId) {
		AssessmentEntryStatus status = assessmentProvider.getStatus(pageId);
		return getNodeCssClass(status);
	}

	private String getNodeCssClass(AssessmentEntryStatus status) {
		if (assessmentProvider.isLearningPathCSS()) {
			if (AssessmentEntryStatus.done.equals(status)) {
				return getNodeDoneCssClass();
			}
			return "o_lp_ready o_lp_not_in_sequence o_lp_contains_no_sequence";
		}
		return "";
	}
	
	private String getNodeDoneCssClass() {
		return assessmentProvider.isLearningPathCSS()
				? "o_lp_done o_lp_not_in_sequence o_lp_contains_no_sequence"
				: "";
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty())
			return;

		Wiki wiki = getWiki();
		if (wiki == null) {
			return;
		}

		ContextEntry ce = entries.get(0);
		String typ = ce.getOLATResourceable().getResourceableTypeName();
		if ("az".equalsIgnoreCase(typ)) {
			openAtoZPage(ureq, wiki);
		} else if ("lastChanges".equalsIgnoreCase(typ)) {
			openLastChangesPage(ureq, wiki);
		} else if ("index".equalsIgnoreCase(typ)) {
			WikiPage page = openIndexPage(ureq, wiki);
			pageId = page.getPageId();
		} else if ("Forum".equalsIgnoreCase(typ)) {
			Long forumKey = ce.getOLATResourceable().getResourceableId();
			for (WikiPage page : wiki.getAllPagesWithContent()) {
				if (forumKey.longValue() == page.getForumKey()) {
					if (page != null) {
						this.pageId = page.getPageId();
					}
					updatePageContext(ureq, page);

					OLATResourceable tabOres = OresHelper.createOLATResourceableInstance("tab", 1l);
					ContextEntry tabCe = BusinessControlFactory.getInstance().createContextEntry(tabOres);
					tabs.activate(ureq, Collections.singletonList(tabCe), null);
					if (forumController != null && entries.size() > 1) {
						List<ContextEntry> subEntries = entries.subList(1, entries.size());
						forumController.activate(ureq, subEntries, null);
					}
					break;
				}
			}
		} else {
			String path = BusinessControlFactory.getInstance().getPath(ce);
			if (path.startsWith("page=")) {
				path = path.substring(5, path.length());
			}
			String activatePageId = WikiManager.generatePageId(FilterUtil.normalizeWikiLink(path));
			if (wiki.pageExists(activatePageId)) {
				WikiPage page = wiki.getPage(activatePageId, true);
				if (page != null) {
					this.pageId = page.getPageId();
				}
				updatePageContext(ureq, page);

				if (entries.size() > 1) {
					List<ContextEntry> subEntries = entries.subList(1, entries.size());
					String subTyp = subEntries.get(0).getOLATResourceable().getResourceableTypeName();
					if ("tab".equalsIgnoreCase(subTyp)) {
						tabs.activate(ureq, subEntries, ce.getTransientState());
					} else if ("message".equalsIgnoreCase(subTyp)) {
						OLATResourceable tabOres = OresHelper.createOLATResourceableInstance("tab", 1l);
						ContextEntry tabCe = BusinessControlFactory.getInstance().createContextEntry(tabOres);
						tabs.activate(ureq, Collections.singletonList(tabCe), null);

						forumController.activate(ureq, subEntries, null);
					}
				}
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {

		String command = event.getCommand();
		setTabsEnabled(true);
		// to make shure we use the lateste page, reload from cache
		Wiki wiki = getWiki();
		if (wiki == null) {
			mainPanel.setContent(createVelocityContainer("deleted"));
			return;
		}
		WikiPage page = null;

		// FIXME:gs images and media should also be wiki pages -> see jamwiki
		if (!(event instanceof RequestNewPageEvent) && !(event instanceof RequestMediaEvent)
				&& !(event instanceof RequestImageEvent)) {
			page = wiki.getPage(pageId, true);
			// set recent page id to the page currently used
			if (page != null) {
				pageId = page.getPageId();
			}
		}

		if (source == content) {
			// noting yet
		} else if (source == tabs) {
			/*************************************************************************
			 * tabbed pane events
			 ************************************************************************/

			TabbedPaneChangedEvent tabEvent = (TabbedPaneChangedEvent) event;
			Component comp = tabEvent.getNewComponent();
			String compName = comp.getComponentName();
			selectTab(ureq, command, compName, page, wiki);
		} else if (source == wikiArticleComp) {
			/*************************************************************************
			 * wiki component events
			 ************************************************************************/
			if (event instanceof RequestPageEvent) {
				RequestPageEvent pageEvent = (RequestPageEvent) event;
				page = openPage(ureq, pageEvent.getCommand(), wiki);
			} else if (event instanceof RequestNewPageEvent) {
				page = handleRequestNewPageEvent(ureq, (RequestNewPageEvent) event, wiki);
			} else if (event instanceof ErrorEvent) {
				showWarning(event.getCommand());
			} else if (event instanceof RequestMediaEvent) {
				deliverMediaFile(ureq, event.getCommand());
			} else if (event instanceof RequestImageEvent) {
				// OLAT-6233 if image-view page is shown 2nd time (click on
				// image ), return to content-wiki-page
				// instead of linking to the image-view-page itself
				if (isImageDetailView) {
					page = wiki.getPage(pageId, true);
					updatePageContext(ureq, page);
					isImageDetailView = false;
				} else {
					final WikiPage imagePage = new WikiPage(event.getCommand());
					imagePage.setContent("[[Image:" + event.getCommand() + "]]");
					articleContent.contextPut("page", imagePage);
					wikiArticleComp.setWikiContent(imagePage.getContent());
					setTabsEnabled(false);
					isImageDetailView = true;
				}
			}
		} else if (source == navigationContent) {
			/*************************************************************************
			 * article container events
			 ************************************************************************/
			if (command.equals(ACTION_EDIT_MENU)) {
				page = wiki.getPage(WikiPage.WIKI_MENU_PAGE);
				updateFileAndLinkList(wiki);
				tryToSetEditLock(page, ureq, ores);
				updatePageContext(ureq, page);
				tabs.setSelectedPane(ureq, 2);
			}
		} else if (source == toMainPageLink) { // home link
			page = openIndexPage(ureq, wiki);
		} else if (source == a2zLink) {
			openAtoZPage(ureq, wiki);
		} else if (source == changesLink) {
			openLastChangesPage(ureq, wiki);
		} else if (source == editMenuButton) {
			page = wiki.getPage(WikiPage.WIKI_MENU_PAGE);
			updateFileAndLinkList(wiki);
			tryToSetEditLock(page, ureq, ores);
			updatePageContext(ureq, page);
			// wikiEditForm.setPage(page);
			tabs.setSelectedPane(ureq, 2);
		} else if (source == archiveLink) {
			// archive a snapshot of the wiki in the users personal folder
			archiveWikiDialogCtr = activateOkCancelDialog(ureq, null, translate("archive.question"),
					archiveWikiDialogCtr);
			return;
		} else if (source == createLink) {
			removeAsListenerAndDispose(calloutCtrl);
			removeAsListenerAndDispose(createArticleForm);

			createArticleForm = new WikiArticleSearchForm(ureq, getWindowControl());
			listenTo(createArticleForm);
			calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					createArticleForm.getInitialComponent(), createLink, "", true, null);
			listenTo(calloutCtrl);
			calloutCtrl.activate();
		} else if (source == versioningContent) {
			// nothing to do
		} else if (source == editContent) {
			// nothing to do
		} else if (source == closePreviewButton) {
			editContent.remove(wikiVersionDisplayComp);
		} else if (source == revertVersionButton) {
			wikiEditForm.setPage(selectedPage);
			tabs.setSelectedPane(ureq, 2);
			tryToSetEditLock(page, ureq, ores);
		} else if (source instanceof Link && "select-page".equals(command)) {
			String name = source.getComponentName();
			page = openPage(ureq, name, wiki);
		}

		// set recent page id to the page currently used
		if (page != null) {
			this.pageId = page.getPageId();
		}
	}

	private void selectTab(UserRequest ureq, String command, String compName, WikiPage page, Wiki wiki) {
		// first release a potential lock on this page. only when the edit tab
		// is acitve
		// a lock will be created. in all other cases it is save to release an
		// existing lock
		doReleaseEditLock();
		if (command.equals(TabbedPaneChangedEvent.TAB_CHANGED)) {
			updatePageContext(ureq, page);
		}

		if (command.equals(TabbedPaneChangedEvent.TAB_CHANGED) && compName.equals("vc_article")) {
			/***********************************************************************
			 * tabbed pane change to article
			 **********************************************************************/
			// if(page.getContent().equals(""))
			// wikiArticleComp.setVisible(false);
			// FIXME:guido: ... && comp == articleContent)) etc.
		} else if (command.equals(TabbedPaneChangedEvent.TAB_CHANGED) && compName.equals("vc_edit")) {
			/***********************************************************************
			 * tabbed pane change to edit tab
			 **********************************************************************/
			wikiEditForm.resetUpdateComment();
			updateFileAndLinkList(wiki);
			// try to edit acquire lock for this page
			tryToSetEditLock(page, ureq, ores);
		} else if (command.equals(TabbedPaneChangedEvent.TAB_CHANGED) && compName.equals("vc_versions")) {
			/***********************************************************************
			 * tabbed pane change to versioning tab
			 **********************************************************************/
			versioningTableModel = new HistoryTableDateModel(wiki.getHistory(page), getTranslator());
			removeAsListenerAndDispose(versioningTableCtr);
			versioningTableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
			listenTo(versioningTableCtr);
			versioningTableModel.addColumnDescriptors(versioningTableCtr);
			versioningTableCtr.setTableDataModel(versioningTableModel);
			versioningTableCtr.modelChanged();
			versioningTableCtr.setSortColumn(1, false);
			versioningContent.put("versions", versioningTableCtr.getInitialComponent());
			versioningContent.contextPut("diffs", diffs);
		} else if (command.equals(TabbedPaneChangedEvent.TAB_CHANGED) && compName.equals("vc_discuss")) {
			/***********************************************************************
			 * tabbed pane change to discussion tab
			 **********************************************************************/
			Forum forum = null;
			if (page.getForumKey() > 0) {
				forum = forumManager.loadForum(Long.valueOf(page.getForumKey()));
			}
			if (forum == null) {
				forum = forumManager.addAForum();
				page.setForumKey(forum.getKey().longValue());
				WikiManager.getInstance().updateWikiPageProperties(ores, page);
			}

			ForumCallback forumCallback = securityCallback.getForumCallback();
			ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(forum);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce,
					getWindowControl());

			removeAsListenerAndDispose(forumController);
			forumController = new ForumController(ureq, bwControl, forum, forumCallback, false);
			listenTo(forumController);
			discussionContent.put("articleforum", forumController.getInitialComponent());
		}

		OLATResourceable pageRes = OresHelper.createOLATResourceableTypeWithoutCheck("path=" + page.getPageName());
		WindowControl wc = addToHistory(ureq, pageRes, null);
		OLATResourceable tabOres = tabs.getTabResource();
		addToHistory(ureq, tabOres, null, wc, true);
	}

	private WikiPage openPage(UserRequest ureq, String name, Wiki wiki) {
		WikiPage page = wiki.getPage(name, true);
		page.incrementViewCount();
		updatePageContext(ureq, page);
		Link pageLink = LinkFactory.createToolLink(page.getPageName(), "select-page", page.getPageName(), this);
		breadcrumpDropdown.addComponent(pageLink);
		setTabsEnabled(true);
		tabs.setSelectedPane(ureq, 0);
		return page;
	}

	private WikiPage openIndexPage(UserRequest ureq, Wiki wiki) {
		WikiPage page = wiki.getPage(WikiPage.WIKI_INDEX_PAGE, true);
		page.incrementViewCount();
		Link pageLink = LinkFactory.createToolLink(page.getPageName(), "select-page", page.getPageName(), this);
		breadcrumpDropdown.addComponent(pageLink);
		updatePageContext(ureq, page);
		setTabsEnabled(true);
		tabs.setSelectedPane(ureq, 0);
		addToHistory(ureq, OresHelper.createOLATResourceableTypeWithoutCheck("index"), null);
		return page;
	}

	private void openLastChangesPage(UserRequest ureq, Wiki wiki) {
		WikiPage recentChanges = wiki.getPage(WikiPage.WIKI_RECENT_CHANGES_PAGE);
		recentChanges.setContent(translate("nav.changes.desc") + wiki.getRecentChanges(ureq.getLocale()));
		clearPortfolioLink();
		articleContent.contextPut("page", recentChanges);
		wikiArticleComp.setWikiContent(recentChanges.getContent());
		setTabsEnabled(false);
		tabs.setSelectedPane(ureq, 0);
		addToHistory(ureq, OresHelper.createOLATResourceableTypeWithoutCheck("lastChanges"), null);
	}

	private void openAtoZPage(UserRequest ureq, Wiki wiki) {
		WikiPage a2zPage = wiki.getPage(WikiPage.WIKI_A2Z_PAGE);
		articleContent.contextPut("page", a2zPage);
		a2zPage.setContent(translate("nav.a-z.desc") + wiki.getAllPageNamesSorted());
		wikiArticleComp.setWikiContent(a2zPage.getContent());
		clearPortfolioLink();
		setTabsEnabled(false);
		tabs.setSelectedPane(ureq, 0);
		addToHistory(ureq, OresHelper.createOLATResourceableTypeWithoutCheck("az"), null);
	}

	private void deliverMediaFile(UserRequest ureq, String command) {
		VFSItem item = WikiManager.getInstance().getMediaFolder(ores).resolve(command);
		if (item == null) {
			// try to replace blanck with _
			item = WikiManager.getInstance().getMediaFolder(ores).resolve(command.replace(" ", "_"));
		}
		if (item instanceof VFSLeaf) {
			ureq.getDispatchResult().setResultingMediaResource(new VFSMediaResource((VFSLeaf) item));
		} else {
			showError("wiki.error.file.not.found");
		}
	}

	private void refreshTableDataModel(UserRequest ureq, Wiki wiki) {

		removeAsListenerAndDispose(mediaTableCtr);
		mediaTableCtr = new TableController(new TableGuiConfiguration(), ureq, getWindowControl(), getTranslator());
		listenTo(mediaTableCtr);

		mediaTableCtr.setMultiSelect(true);
		mediaTableCtr.addMultiSelectAction(ACTION_DELETE_MEDIAS, ACTION_DELETE_MEDIAS);

		List<VFSItem> filelist = wiki.getMediaFileListWithMetadata();
		Map<String, MediaFileElement> files = new HashMap<>();
		for (Iterator<VFSItem> iter = filelist.iterator(); iter.hasNext();) {
			VFSLeaf elem = (VFSLeaf) iter.next();
			if (elem.getName().endsWith(METADATA_SUFFIX)) { // *.metadata files
															// go here
				Properties p = new Properties();
				try {
					p.load(elem.getInputStream());
					MediaFileElement mediaFileElement = new MediaFileElement(elem.getName(),
							p.getProperty(MEDIA_FILE_CREATED_BY), p.getProperty(MEDIA_FILE_CREATIONDATE));
					mediaFileElement.setDeletedBy(p.getProperty(MEDIA_FILE_DELETED_BY));
					mediaFileElement.setDeletionDate(p.getProperty(MEDIA_FILE_DELETIONDATE));
					files.put(p.getProperty(MEDIA_FILE_FILENAME), mediaFileElement);
				} catch (IOException e) {
					throw new OLATRuntimeException("Could'n read properties from media file: " + elem.getName(), e);
				}
			}
		}
		for (Iterator<VFSItem> iter = filelist.iterator(); iter.hasNext();) {
			VFSLeaf elem = (VFSLeaf) iter.next();
			if (!elem.getName().endsWith(METADATA_SUFFIX)) {
				if (!files.containsKey(elem.getName())) {
					// legacy file without metadata
					files.put(elem.getName(), new MediaFileElement(elem.getName(), 0, elem.getLastModified()));
				} else {
					// file with metadata, update name
					MediaFileElement element = files.get(elem.getName());
					element.setFileName(elem.getName());
				}
			}
		}

		mediaFilesTableModel = new MediaFilesTableModel(new ArrayList<>(files.values()),
				getTranslator());
		mediaFilesTableModel.addColumnDescriptors(mediaTableCtr);
		mediaTableCtr.setTableDataModel(mediaFilesTableModel);
		mediaTableCtr.setSortColumn(3, false);
		mediaTableCtr.modelChanged();
	}

	private WikiPage handleRequestNewPageEvent(UserRequest ureq, RequestNewPageEvent requestPage, Wiki wiki) {
		if (!securityCallback.mayEditAndCreateArticle()) {
			if (ureq.getUserSession().getRoles().isGuestOnly()) {
				showInfo("guest.no.edit");
			} else {
				showInfo("no.edit");
			}
			return null;
		}
		// first check if no page exist
		WikiPage page = wiki.findPage(requestPage.getCommand());
		if (page.getPageName().equals(Wiki.NEW_PAGE)) {
			// create new page
			log.debug("Page does not exist, create a new one...");
			page = new WikiPage(requestPage.getCommand());
			page.setCreationTime(System.currentTimeMillis());
			page.setInitalAuthor(ureq.getIdentity().getKey().longValue());
			wiki.addPage(page);
			WikiManager.getInstance().saveWikiPage(ores, page, false, wiki, true, getIdentity());
			log.debug("Safe new page=" + page);
			log.debug("Safe new pageId=" + page.getPageId());
		}
		updatePageContext(ureq, page);
		doReleaseEditLock();
		tryToSetEditLock(page, ureq, ores);
		tabs.setSelectedPane(ureq, 2);
		return page;
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		Wiki wiki = getWiki();
		if (wiki == null) {
			mainPanel.setContent(createVelocityContainer("deleted"));
			return;
		}
		// reload page from cache
		WikiPage page = wiki.getPage(pageId, true);
		// set recent page id to the page currently used
		this.pageId = page.getPageId();

		if (event instanceof TreeEvent) {
			TreeEvent te = (TreeEvent) event;
			String nodeId = te.getNodeId();
			if (navAZNode.getIdent().equals(nodeId)) {
				openAtoZPage(ureq, wiki);
			} else if (navChangesNode.getIdent().equals(nodeId)) {
				openLastChangesPage(ureq, wiki);
			} else if (navMainPageNode.getIdent().equals(nodeId)) {
				page = openIndexPage(ureq, wiki);
			} else if (wikiMenuNode.getIdent().equals(nodeId)) {
				page = openPage(ureq, WikiPage.WIKI_MENU_PAGE, wiki);
			} else {
				TreeNode node = wikiMenuModel.getNodeById(nodeId);
				if (node != null && node.getUserObject() instanceof String) {
					String link = (String) node.getUserObject();
					page = openPage(ureq, link, wiki);
				}
			}

			if (page != null) {
				this.pageId = page.getPageId();
			}
		} else if (source == versioningTableCtr) {
			/*************************************************************************
			 * history table events
			 ************************************************************************/
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				if (te.getActionId().equals(ACTION_COMPARE)) {
					selectedPage = versioningTableModel.getObject(te.getRowId());
					diffs = wiki.getDiff(page, selectedPage.getVersion() - 1, selectedPage.getVersion());
					versioningContent.contextPut("diffs", diffs);
					versioningContent.remove(wikiVersionDisplayComp);
					versioningContent.contextPut("page", selectedPage);
				} else if (te.getActionId().equals(ACTION_SHOW)) {
					versioningContent.contextRemove("diffs");
					selectedPage = versioningTableModel.getObject(te.getRowId());
					selectedPage = wiki.loadVersion(selectedPage, selectedPage.getVersion());
					wikiVersionDisplayComp.setWikiContent(selectedPage.getContent());
					wikiVersionDisplayComp.setImageMapperUri(ureq, wikiContainer);
					versioningContent.put("versionDisplay", wikiVersionDisplayComp);
					versioningContent.contextPut("page", selectedPage);
				}
			}
		} else if (source == wikiUploadFileCtr) {
			if (event.getCommand().equals(FolderEvent.UPLOAD_EVENT)) {
				FolderEvent fEvent = (FolderEvent) event;
				createMediaMetadataFile(fEvent.getFilename(), ureq.getIdentity().getKey());
				updateFileAndLinkList(wiki);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == removePageDialogCtr) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				wiki.removePage(page);
				breadcrumpDropdown.removeComponent(page.getPageName());
				WikiManager.getInstance().deleteWikiPage(ores, page);
				page = wiki.getPage(WikiPage.WIKI_INDEX_PAGE);
				updatePageContext(ureq, page);
				tabs.setSelectedPane(ureq, 0);
			}
		} else if (source == mediaTableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				if (te.getActionId().equals(ACTION_DELETE_MEDIA)) {
					List<MediaFileElement> list = new ArrayList<>(1);
					list.add(mediaFilesTableModel.getObject(te.getRowId()));
					deleteMediaFile(list, ureq);
				} else if (te.getActionId().equals(ACTION_SHOW_MEDIA)) {
					// show a selected file from the media folder
					MediaFileElement element = mediaFilesTableModel.getObject(te.getRowId());
					if (isImage(element.getFilename())) { // show images inline
															// as modal overlay
						imageDisplay.contextPut("mediaElement", element);
						imageDisplay.contextPut("imageUri", wikiArticleComp.getImageBaseUri());

						removeAsListenerAndDispose(mediaCmc);
						mediaCmc = new CloseableModalController(getWindowControl(), translate("close"), imageDisplay);
						listenTo(mediaCmc);

						mediaCmc.activate();
					} else {
						deliverMediaFile(ureq, element.getFilename());
					}
				}
			} else if (event.getCommand().equals(Table.COMMAND_MULTISELECT)) {
				TableMultiSelectEvent tmse = (TableMultiSelectEvent) event;
				if (tmse.getAction().equals(ACTION_DELETE_MEDIAS)) {
					deleteMediaFile(mediaFilesTableModel.getObjects(tmse.getSelection()), ureq);
					updateFileAndLinkList(wiki);
				}
			}
		} else if (source == archiveWikiDialogCtr) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				WikiToCPResource rsrc = new WikiToCPResource(ores, getIdentity(), getTranslator());
				ureq.getDispatchResult().setResultingMediaResource(rsrc);
			}
		} else if (source == createArticleForm) {
			calloutCtrl.deactivate();

			String query = createArticleForm.getQuery();
			if (!StringHelper.containsNonWhitespace(query)) {
				query = WikiPage.WIKI_INDEX_PAGE;
			}
			page = wiki.findPage(query);
			pageId = page.getPageId();
			if (page.getPageName().equals(Wiki.NEW_PAGE)) {
				setTabsEnabled(false);
			}
			page.incrementViewCount();
			updatePageContext(ureq, page);
			if (!page.getPageName().startsWith("O_")) {
				Link pageLink = LinkFactory.createToolLink(page.getPageName(), "select-page", page.getPageName(), this);
				breadcrumpDropdown.addComponent(pageLink);
			}
			tabs.setSelectedPane(ureq, 0);
		} else if (source == wikiEditForm) {
			// set recent page id to the page currently used
			this.pageId = page.getPageId();

			boolean wantPreview = false;
			boolean wantSave = false;
			boolean wantClose = false;
			if (event == Event.CANCELLED_EVENT) {
				wantClose = true;
			} else if (event == Event.DONE_EVENT) {
				wantSave = true;
			} else if (event.getCommand().equals("save.and.close")) {
				wantClose = true;
				wantSave = true;
				event = Event.DONE_EVENT;
			} else if (event.getCommand().equals("preview")) {
				wantPreview = true;
				event = Event.DONE_EVENT;
			} else if (event.getCommand().equals("delete.page")) {
				String msg = translate("question", page.getPageName());
				removePageDialogCtr = activateOkCancelDialog(ureq, null, msg, removePageDialogCtr);
				return;
			} else if (event.getCommand().equals("media.upload")) {
				doUploadFiles(ureq);
				return;
			} else if (event.getCommand().equals("manage.media")) {
				doManageMedias(ureq, wiki);
				return;
			}

			boolean dirty = !wikiEditForm.getWikiContent().equals(page.getContent());
			if (wantPreview) {
				WikiPage preview = new WikiPage("temp");
				preview.setContent(wikiEditForm.getWikiContent());
				wikiVersionDisplayComp.setWikiContent(preview.getContent());
				editContent.put("versionDisplay", wikiVersionDisplayComp);
			}

			if (wantSave && dirty) {
				editContent.contextPut("isDirty", Boolean.valueOf(false));
				page.setContent(wikiEditForm.getWikiContent());
				page.setModifyAuthor(getIdentity().getKey().longValue());
				page.setUpdateComment(wikiEditForm.getUpdateComment());
				if (page.getInitalAuthor() == 0)
					page.setInitalAuthor(getIdentity().getKey().longValue());
				// menu page only editable by admin and owner set new content if
				// changed
				if (page.getPageName().equals(WikiPage.WIKI_MENU_PAGE)) {
					updateWikiMenu(wiki);
				}
				WikiManager.getInstance().saveWikiPage(ores, page, true, wiki, true, getIdentity());
				// inform subscription context about changes
				notificationsManager.markPublisherNews(subsContext, ureq.getIdentity(), true);

				updatePageContext(ureq, page);
			}

			if (dirty && wantPreview && !wantSave) {
				editContent.contextPut("isDirty", Boolean.valueOf(dirty));
			}

			if (wantClose) {
				tabs.setSelectedPane(ureq, 0);
				doReleaseEditLock();
			}
		} else if(mediaCmc == source) {
			removeAsListenerAndDispose(mediaCmc);
			mediaCmc = null;
		} else if(cmc == source) {
			cleanUp();
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(mediaTableCtr);
		removeAsListenerAndDispose(wikiUploadFileCtr);

		cmc = null;
		mediaTableCtr = null;
		wikiUploadFileCtr = null;
	}

	private boolean isImage(String filename) {
		String fileSuffix = filename.substring(filename.lastIndexOf(".") + 1, filename.length()).toLowerCase();
		if (fileSuffix.equals("jpg"))
			return true;
		if (fileSuffix.equals("jpeg"))
			return true;
		if (fileSuffix.equals("gif"))
			return true;
		if (fileSuffix.equals("png"))
			return true;
		return false;
	}

	private void createMediaMetadataFile(String filename, Long author) {
		VFSContainer mediaFolder = WikiManager.getInstance().getMediaFolder(ores);
		// only create metadatafile if base file exists
		if ((VFSLeaf) mediaFolder.resolve(filename) != null) {
			// metafile may exists when files get overwritten
			VFSLeaf metaFile = (VFSLeaf) mediaFolder.resolve(filename + METADATA_SUFFIX);
			if (metaFile == null) {
				// metafile does not exist => create one
				metaFile = mediaFolder.createChildLeaf(filename + METADATA_SUFFIX);
			}
			Properties p = new Properties();
			p.setProperty(MEDIA_FILE_FILENAME, filename);
			p.setProperty(MEDIA_FILE_CREATIONDATE, String.valueOf(System.currentTimeMillis()));
			p.setProperty(MEDIA_FILE_CREATED_BY, String.valueOf(author));
			try {
				p.store(metaFile.getOutputStream(false), "wiki media files meta properties");
			} catch (IOException e) {
				throw new OLATRuntimeException(WikiManager.class, "failed to save media files properties for file: "
						+ filename + " and olatresource: " + ores.getResourceableId(), e);
			}
		}
	}

	private void deleteMediaFile(List<MediaFileElement> toDelete, UserRequest ureq) {
		for (Iterator<MediaFileElement> iter = toDelete.iterator(); iter.hasNext();) {
			VFSContainer mediaFolder = WikiManager.getInstance().getMediaFolder(ores);
			MediaFileElement element = iter.next();
			if (log.isDebugEnabled())
				log.debug("deleting media file: " + element.getFilename());
			if (!element.getFilename().endsWith(METADATA_SUFFIX)) {
				VFSLeaf file = (VFSLeaf) mediaFolder.resolve(element.getFilename());
				if (file != null) {
					file.delete();
					VFSLeaf metadata = (VFSLeaf) mediaFolder.resolve(element.getFilename() + METADATA_SUFFIX);
					if (metadata != null) {
						Properties p = new Properties();
						try {
							p.load(metadata.getInputStream());
							p.setProperty(MEDIA_FILE_DELETIONDATE, String.valueOf(System.currentTimeMillis()));
							p.setProperty(MEDIA_FILE_DELETED_BY, String.valueOf(ureq.getIdentity().getKey()));
							OutputStream os = metadata.getOutputStream(false);
							p.store(os, "wiki media file meta properties");
							os.close();
						} catch (IOException e) {
							throw new OLATRuntimeException(
									"Could'n read properties from media file: " + metadata.getName(), e);
						}
					}
				}
			}
		}
		getWindowControl().pop();
	}

	private void setTabsEnabled(boolean enable) {
		tabs.setEnabled(1, enable);
		if (enable && securityCallback.mayEditAndCreateArticle()) {
			tabs.setEnabled(2, enable);
		} else {
			tabs.setEnabled(2, false);
		}
		tabs.setEnabled(3, enable);
	}

	private void doUploadFiles(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(wikiUploadFileCtr);

		wikiUploadFileCtr = new WikiFileUploadController(ureq, getWindowControl(),
				WikiManager.getInstance().getMediaFolder(ores));
		listenTo(wikiUploadFileCtr);

		String title = translate("media.upload");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				wikiUploadFileCtr.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doManageMedias(UserRequest ureq, Wiki wiki) {
		if (!wiki.getMediaFileListWithMetadata().isEmpty()) {
			refreshTableDataModel(ureq, wiki);
			removeAsListenerAndDispose(cmc);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), mediaTableCtr.getInitialComponent(), true,
					translate("manage.media"));
			listenTo(cmc);
			cmc.activate();
		}
	}

	@Override
	protected void doDispose() {
		if (wikiArticleComp != null) {
			wikiArticleComp.dispose();
		}
		if (wikiVersionDisplayComp != null) {
			wikiVersionDisplayComp.dispose();
		}

		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_CLOSE, getClass());
		doReleaseEditLock();
        super.doDispose();
	}

	private void doReleaseEditLock() {
		if (lockEntry != null && lockEntry.isSuccess()) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
			lockEntry = null;
		}
	}

	/**
	 * Try to aquire a lock on the page
	 * 
	 * @param ureq
	 * @param wikiOres
	 */
	private void tryToSetEditLock(WikiPage page, UserRequest ureq, OLATResourceable wikiOres) {
		lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(wikiOres, ureq.getIdentity(),
				page.getPageName(), getWindow());
		editContent.contextPut("lockEntry", lockEntry);
	}

	/**
	 * update depended velocity contexts and componetens with latest globally
	 * used page
	 *
	 */
	private void updatePageContext(UserRequest ureq, WikiPage page) {

		if (page.getPageName().equals(WikiPage.WIKI_ERROR)) {
			wikiArticleComp.setWikiContent(translate(page.getContent()));
		} else {
			wikiArticleComp.setWikiContent(page.getContent());
		}

		wikiEditForm.setPage(page);
		diffs.clear();
		content.contextPut("page", page);

		articleContent.contextPut("page", page);
		discussionContent.contextPut("page", page);

		editContent.remove(wikiVersionDisplayComp);
		editContent.contextPut("page", page);

		versioningContent.remove(wikiVersionDisplayComp);
		versioningContent.contextPut("page", page);

		boolean userIsPageCreator = getIdentity().getKey().equals(page.getInitalAuthor());
		if (userIsPageCreator) {
			if (portfolioModule.isEnabled()) {
				String subPath = page.getPageName();
				String businessPath = getWindowControl().getBusinessControl().getAsString();
				businessPath += "[page=" + subPath + ":0]";
				MediaCollectorComponent collectorCmp = new MediaCollectorComponent("portfolio-link", getWindowControl(),
						page, wikiMediaHandler, businessPath);
				navigationContent.put("portfolio-link", collectorCmp);
			}
		} else {
			clearPortfolioLink();
		}

		OLATResourceable pageRes = OresHelper.createOLATResourceableInstanceWithoutCheck("path=" + page.getPageName(),
				0l);
		addToHistory(ureq, pageRes, null);
		
		assessmentProvider.setStatusDone(page.getPageId());
		updateWikiMenu(getWiki());
	}
	
	private void clearPortfolioLink() {
		navigationContent.put("portfolio-link", new Panel("empty"));
	}

	private Wiki getWiki() {
		return WikiManager.getInstance().getOrLoadWiki(ores);
	}
}
