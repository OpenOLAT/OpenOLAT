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

package org.olat.repository.controllers;

import java.util.List;

import org.olat.catalog.CatalogEntry;
import org.olat.catalog.CatalogModule;
import org.olat.catalog.ui.CatalogController;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.portal.PortletFactory;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.gui.control.generic.wizard.WizardController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeHelper;
import org.olat.course.CourseModule;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.fileresource.types.GlossaryResource;
import org.olat.fileresource.types.ImsCPFileResource;
import org.olat.fileresource.types.PodcastFileResource;
import org.olat.fileresource.types.ScormCPFileResource;
import org.olat.fileresource.types.SharedFolderFileResource;
import org.olat.fileresource.types.WikiResource;
import org.olat.ims.qti.fileresource.SurveyFileResource;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.portfolio.EPTemplateMapResource;
import org.olat.portfolio.PortfolioModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryTableModel;
import org.olat.repository.delete.TabbedPaneController;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;

import de.bps.olat.repository.controllers.WizardAddOwnersController;

/**
 * Description: <br>
 * 
 * Activatable:<br>
 * Supported commands are: 
 * "search.home": root entry point 
 * "search.catalog": catalog view 
 * "search.generic": search form 
 * "search.my": list of owned resources 
 * "search.REPOTYPE": list of all resources of given type, where
 * REPOTYPE is something like course, test, survey, cp, scorm, sharedfolder
 * 
 * @date Initial Date: Oct 21, 2004 <br>
 * @author Felix Jost
 */
public class RepositoryMainController extends MainLayoutBasicController implements Activateable2{

	private static final OLog log = Tracing.createLoggerFor(RepositoryMainController.class);
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(RepositoryManager.class);

	static final String ACTION_NEW_CREATECOURSE = "cco";
	static final String ACTION_NEW_CREATETEST = "cte";
	static final String ACTION_NEW_CREATESURVEY = "csu";
	static final String ACTION_NEW_CREATESHAREDFOLDER = "csf";
	static final String ACTION_NEW_CREATECP = "ccp";
	static final String ACTION_NEW_WIKI = "wiki";
	static final String ACTION_NEW_PODCAST = "podcast";
	static final String ACTION_NEW_BLOG = "blog";
	static final String ACTION_NEW_GLOSSARY = "glossary";
	static final String ACTION_NEW_PORTFOLIO = "portfolio";
	private static final String ACTION_DELETE_RESOURCE = "deleteresource";
	private static final String ACTION_ADD_OWNERS = "addowners";

	private Panel mainPanel;
	private VelocityContainer main;
	private LayoutMain3ColsController columnsLayoutCtr;
	private ToolController mainToolC;
	private MenuTree menuTree;

	private String myEntriesNodeId;

	private RepositoryAddController addController;
	//fxdiff BAKS-7 Resume function
	private WindowControl searchWindowControl;
	private RepositorySearchController searchController;
	private RepositoryDetailsController detailsController;
	private DialogBoxController launchEditorDialog;
	private boolean isAuthor;
	private CatalogController catalogCtrl;
	
	
	// REVIEW:pb:concept for jumping between activateables, instead of hardcoding
	// each dependency
	// REVIEW:pb:like jumpfromcourse, backtocatalog, etc.
	private boolean backtocatalog = false;
	private TabbedPaneController deleteTabPaneCtr;
	private CloseableModalController cmc;
	private String lastUserObject = null; // to detect double click on catalog
																				// menu item
	private WizardController wc;
	private RepositoryAddChooseStepsController chooseStepsController;
	private Controller creationWizardController;
	private final PortfolioModule portfolioModule;
	private final CatalogModule catalogModule;
	private final RepositoryModule repositoryModule;

	/**
	 * The check for author rights is executed on construction time and then
	 * cached during the objects lifetime.
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public RepositoryMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		if (log.isDebug()) {
			log.debug("Constructing ReposityMainController for user::" + ureq.getIdentity());
		}
		portfolioModule = CoreSpringFactory.getImpl(PortfolioModule.class);
		catalogModule = CoreSpringFactory.getImpl(CatalogModule.class);
		repositoryModule = CoreSpringFactory.getImpl(RepositoryModule.class);

		// use i18n from RepositoryManager level
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, ureq.getLocale()));

		// main component layed out in panel
		// use veloctiy pages from RepositoryManager level
		main = new VelocityContainer("vc_index", RepositoryManager.class, "index", getTranslator(), this);

		// if the user has the role InstitutionalResourceManager (and has the same
		// university like the author) then set isAuthor to true
		isAuthor = ureq.getUserSession().getRoles().isAuthor()
				|| (ureq.getUserSession().getRoles().isInstitutionalResourceManager());
		main.contextPut("isAuthor", Boolean.valueOf(isAuthor));

		mainPanel = new Panel("repopanel");
		mainPanel.setContent(main);

		searchController = new RepositorySearchController(translate("details.header"), ureq, getWindowControl(), false, true, false);
		listenTo(searchController);
		main.put("searchcomp", searchController.getInitialComponent());

		detailsController = new RepositoryDetailsController(ureq, getWindowControl());
		listenTo(detailsController);
		detailsController.addControllerListener(searchController); // to catch
		// delete
		// events

		createRepoToolController(isAuthor, ureq.getUserSession().getRoles().isOLATAdmin());

		menuTree = new MenuTree("repoTree");
		menuTree.setTreeModel(buildTreeModel(isAuthor));
		TreeNode rootNode = menuTree.getTreeModel().getRootNode();
		menuTree.setSelectedNode(rootNode);
		menuTree.addListener(this);
		menuTree.setRootVisible(false);

		Component toolComp = (mainToolC == null ? null : mainToolC.getInitialComponent());
		columnsLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, toolComp, mainPanel, "repomain");
		columnsLayoutCtr.addCssClassToMain("o_repository");
		listenTo(columnsLayoutCtr);
		
		if (isAuthor || ureq.getUserSession().getRoles().isOLATAdmin()) {
			activateContent(ureq, "search.my", null, null);
			TreeNode activatedNode = TreeHelper.findNodeByUserObject("search.my", rootNode);
			menuTree.setSelectedNode(activatedNode);
		} else if(catalogModule.isCatalogRepoEnabled()) {
			activateContent(ureq, "search.catalog", null, null);
			TreeNode activatedNode = TreeHelper.findNodeByUserObject("search.catalog", rootNode);
			menuTree.setSelectedNode(activatedNode);
		}
		
		putInitialPanel(columnsLayoutCtr.getInitialComponent());
	}

	/**
	 */
	private void createRepoToolController(boolean isAuthor, boolean bIsAdmin) {
		if (isAuthor) {
			mainToolC = ToolFactory.createToolController(getWindowControl());
			listenTo(mainToolC);
			//CP, SCORM, Wiki, Podcast, Blog, Test, Questionnaire, Glossary, other formats 

			mainToolC.addHeader(translate("tools.add.header"));
			mainToolC.addLink(RepositoryAddController.ACTION_ADD_COURSE, translate("tools.add.course"), RepositoryAddController.ACTION_ADD_COURSE, "o_toolbox_course o_sel_repo_import_course");
			mainToolC.addLink(RepositoryAddController.ACTION_ADD_CP, translate("tools.add.cp"), RepositoryAddController.ACTION_ADD_CP, "o_toolbox_content o_sel_repo_import_cp");
			mainToolC.addLink(RepositoryAddController.ACTION_ADD_SCORM, translate("tools.add.scorm"), RepositoryAddController.ACTION_ADD_SCORM, "o_toolbox_scorm o_sel_repo_import_scorm");
			mainToolC.addLink(RepositoryAddController.ACTION_ADD_WIKI, translate("tools.add.wiki"), RepositoryAddController.ACTION_ADD_WIKI, "o_toolbox_wiki o_sel_repo_import_wiki");
			mainToolC.addLink(RepositoryAddController.ACTION_ADD_PODCAST, translate("tools.add.podcast"), RepositoryAddController.ACTION_ADD_PODCAST, "o_toolbox_podcast o_sel_repo_import_podcast");
			mainToolC.addLink(RepositoryAddController.ACTION_ADD_BLOG, translate("tools.add.blog"), RepositoryAddController.ACTION_ADD_BLOG, "o_toolbox_blog o_sel_repo_import_blog");
			mainToolC.addLink(RepositoryAddController.ACTION_ADD_TEST, translate("tools.add.test"), RepositoryAddController.ACTION_ADD_TEST, "o_toolbox_test o_sel_repo_import_test");
			mainToolC.addLink(RepositoryAddController.ACTION_ADD_SURVEY, translate("tools.add.survey"), RepositoryAddController.ACTION_ADD_SURVEY, "o_toolbox_questionnaire o_sel_repo_import_questionnaire");
			mainToolC.addLink(RepositoryAddController.ACTION_ADD_GLOSSARY, translate("tools.add.glossary"), RepositoryAddController.ACTION_ADD_GLOSSARY, "o_toolbox_glossary o_sel_repo_import_glossary");
			mainToolC.addLink(RepositoryAddController.ACTION_ADD_DOC, translate("tools.add.webdoc"), RepositoryAddController.ACTION_ADD_DOC, "b_toolbox_doc o_sel_repo_import_doc");

			mainToolC.addHeader(translate("tools.new.header"));
			mainToolC.addLink(ACTION_NEW_CREATECOURSE, translate("tools.new.createcourse"), ACTION_NEW_CREATECOURSE, "o_toolbox_course o_sel_repo_new_course");
			mainToolC.addLink(ACTION_NEW_CREATECP, translate("tools.new.createcp"), ACTION_NEW_CREATECP, "o_toolbox_content o_sel_repo_new_cp");
			mainToolC.addLink(ACTION_NEW_WIKI, translate("tools.new.wiki"), ACTION_NEW_WIKI, "o_toolbox_wiki o_sel_repo_new_wiki");
			mainToolC.addLink(ACTION_NEW_PODCAST, translate("tools.new.podcast"), ACTION_NEW_PODCAST, "o_toolbox_podcast o_sel_repo_new_podcast");
			mainToolC.addLink(ACTION_NEW_BLOG, translate("tools.new.blog"), ACTION_NEW_BLOG, "o_toolbox_blog o_sel_repo_new_blog");
			if (portfolioModule.isEnabled()){
				mainToolC.addLink(ACTION_NEW_PORTFOLIO, translate("tools.new.portfolio"), ACTION_NEW_PORTFOLIO, "o_toolbox_portfolio o_sel_repo_new_portfolio");
			}
			mainToolC.addLink(ACTION_NEW_CREATETEST, translate("tools.new.createtest"), ACTION_NEW_CREATETEST, "o_toolbox_test o_sel_repo_new_test");
			mainToolC.addLink(ACTION_NEW_CREATESURVEY, translate("tools.new.createsurvey"), ACTION_NEW_CREATESURVEY, "o_toolbox_questionnaire o_sel_repo_new_questionnaire");
			mainToolC.addLink(ACTION_NEW_CREATESHAREDFOLDER, translate("tools.new.createsharedfolder"), ACTION_NEW_CREATESHAREDFOLDER,
					"o_toolbox_sharedfolder o_sel_repo_new_sharedfolder");
			mainToolC.addLink(ACTION_NEW_GLOSSARY, translate("tools.new.glossary"), ACTION_NEW_GLOSSARY, "o_toolbox_glossary o_sel_repo_new_glossary");
			if (bIsAdmin || isAuthor) {
				mainToolC.addHeader(translate("tools.administration.header"));
				if (bIsAdmin) {
					mainToolC.addLink(ACTION_DELETE_RESOURCE, translate("tools.delete.resource"));
				}
				mainToolC.addLink(ACTION_ADD_OWNERS, translate("tools.add.owners"));
			}
		} else {
			mainToolC = null;
		}
	}

	private TreeModel buildTreeModel(boolean bIsAuthor) {
		GenericTreeModel gtm = new GenericTreeModel();
		GenericTreeNode rootNode = new GenericTreeNode(translate("search.home"), "search.home");
		rootNode.setCssClass("o_sel_repo_home");
		gtm.setRootNode(rootNode);

		GenericTreeNode node;
		if(catalogModule.isCatalogRepoEnabled()) {
			node= new GenericTreeNode(translate("search.catalog"), "search.catalog");
			node.setCssClass("o_sel_repo_catalog");
			rootNode.addChild(node);
		}

		// check if repository portlet is configured in olat_portals.xml
		boolean repoPortletOn = PortletFactory.containsPortlet("RepositoryPortletStudent");
		// add default searches
		node = new GenericTreeNode(translate("search.generic"), "search.generic");
		node.setCssClass("o_sel_repo_search_generic");
		rootNode.addChild(node);
		if (bIsAuthor) {
			GenericTreeNode myEntriesTn = new GenericTreeNode(translate("search.my"), "search.my");
			myEntriesTn.setCssClass("o_sel_repo_my");
			myEntriesNodeId = myEntriesTn.getIdent();
			rootNode.addChild(myEntriesTn);
		}
		// add repository search also used by portlets
		if (repoPortletOn) {
			node = new GenericTreeNode(translate("search.mycourses.student"), "search.mycourses.student");
			node.setCssClass("o_sel_repo_my_student");
			rootNode.addChild(node);
			// for authors or users with group rights also show the teacher portlet
			if(bIsAuthor || RepositoryManager.getInstance().hasLearningResourcesAsTeacher(getIdentity())) {
				node = new GenericTreeNode(translate("search.mycourses.teacher"), "search.mycourses.teacher");
				node.setCssClass("o_sel_repo_my_teacher");
				rootNode.addChild(node);
			}
		}
		
		if(repositoryModule.isListAllCourses()) {
			node = new GenericTreeNode(translate("search.course"), "search.course");
			node.setCssClass("o_sel_repo_course");
			rootNode.addChild(node);
		}
		
		if (bIsAuthor && repositoryModule.isListAllResourceTypes()) {
			//cp, scorm, wiki, podcast, portfolie, test, questionn, resource folder, glossary
			node = new GenericTreeNode(translate("search.cp"), "search.cp");
			node.setCssClass("o_sel_repo_cp");
			rootNode.addChild(node);
			node = new GenericTreeNode(translate("search.scorm"), "search.scorm");
			node.setCssClass("o_sel_repo_scorm");
			rootNode.addChild(node);
			node = new GenericTreeNode(translate("search.wiki"), "search.wiki");
			node.setCssClass("o_sel_repo_wiki");
			rootNode.addChild(node);
			node = new GenericTreeNode(translate("search.podcast"), "search.podcast" );
			node.setCssClass("o_sel_repo_podcast");
			rootNode.addChild(node);
			node = new GenericTreeNode(translate("search.blog"), "search.blog" );
			node.setCssClass("o_sel_repo_blog");
			rootNode.addChild(node);
			if (portfolioModule.isEnabled()){
				node = new GenericTreeNode(translate("search.portfolio"), "search.portfolio");
				node.setCssClass("o_sel_repo_portfolio");
				rootNode.addChild(node);
			}
			node = new GenericTreeNode(translate("search.test"), "search.test");
			node.setCssClass("o_sel_repo_test");
			rootNode.addChild(node);
			node = new GenericTreeNode(translate("search.survey"), "search.survey");
			node.setCssClass("o_sel_repo_survey");
			rootNode.addChild(node);
			node = new GenericTreeNode(translate("search.sharedfolder"), "search.sharedfolder");
			node.setCssClass("o_sel_repo_sharefolder");
			rootNode.addChild(node);
			node = new GenericTreeNode(translate("search.glossary"), "search.glossary");
			node.setCssClass("o_sel_repo_glossary");
			rootNode.addChild(node);
		}

		return gtm;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		long start = 0;
		if(isLogDebugEnabled()) start = System.currentTimeMillis(); // TODO: add check if debug logging 
		
		if (source == menuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				Component toolComp = (mainToolC == null ? null : mainToolC.getInitialComponent());
				columnsLayoutCtr.setCol2(toolComp);
				mainPanel.setContent(main);
				TreeEvent te = (TreeEvent) event;
				TreeNode clickedNode = menuTree.getTreeModel().getNodeById(te.getNodeId());
				Object userObject = clickedNode.getUserObject();
				long duration1 = System.currentTimeMillis() - start;
				log.info("Repo-Perf: duration1=" + duration1);
				activateContent(ureq, userObject, null, null);
			}
		}
		
		if(start != 0) {
			long duration = System.currentTimeMillis() - start;
			log.info("Repo-Perf: event duration=" + duration);
		}
	}

	/**
	 * Activate the content in the content area based on a user object
	 * representing the identifyer of the content
	 * 
	 * @param ureq
	 * @param uObj
	 * @param subViewIdentifyer optional view identifyer for a sub controller
	 */
	private void activateContent(UserRequest ureq, Object userObject, List<ContextEntry> entries, StateEntry state) {
		log.info("activateContent userObject=" + userObject);
		if (userObject.equals("search.home")) { // the
			// home
			main.setPage(VELOCITY_ROOT + "/index.html");
			mainPanel.setContent(main);
		} else if (userObject.equals("search.generic")) { // new
			// search
			main.setPage(VELOCITY_ROOT + "/index2.html");
			mainPanel.setContent(main);
			searchController.displaySearchForm();
			searchController.enableBackToSearchFormLink(true);
		} else if (userObject.equals("search.catalog")) {
			// enter catalog browsing
			activateCatalogController(ureq, entries, state);
			mainPanel.setContent(catalogCtrl.getInitialComponent());
		} else if (userObject.equals("search.my")) { // search
			// own resources
			main.setPage(VELOCITY_ROOT + "/index2.html");
			mainPanel.setContent(main);
			searchController.doSearchByOwner(ureq.getIdentity());
			searchController.enableBackToSearchFormLink(false);
		} else if (userObject.equals("search.course")) { // search
			// courses
			main.setPage(VELOCITY_ROOT + "/index2.html");
			mainPanel.setContent(main);
			searchController.doSearchByTypeLimitAccess(CourseModule.getCourseTypeName(), ureq);
			searchController.enableBackToSearchFormLink(false);
		} else if (userObject.equals("search.test")) { // search
			// tests
			main.setPage(VELOCITY_ROOT + "/index2.html");
			mainPanel.setContent(main);
			searchController.doSearchByTypeLimitAccess(TestFileResource.TYPE_NAME, ureq);
			searchController.enableBackToSearchFormLink(false);

		} else if (userObject.equals("search.survey")) { // search
			// surveys
			main.setPage(VELOCITY_ROOT + "/index2.html");
			mainPanel.setContent(main);
			searchController.doSearchByTypeLimitAccess(SurveyFileResource.TYPE_NAME, ureq);
			searchController.enableBackToSearchFormLink(false);
		} else if (userObject.equals("search.cp")) { // search
			// cp
			main.setPage(VELOCITY_ROOT + "/index2.html");
			mainPanel.setContent(main);
			searchController.doSearchByTypeLimitAccess(ImsCPFileResource.TYPE_NAME, ureq);
			searchController.enableBackToSearchFormLink(false);
		} else if (userObject.equals("search.scorm")) { // search
			// scorm
			main.setPage(VELOCITY_ROOT + "/index2.html");
			mainPanel.setContent(main);
			searchController.doSearchByTypeLimitAccess(ScormCPFileResource.TYPE_NAME, ureq);
			searchController.enableBackToSearchFormLink(false);
		} else if (userObject.equals("search.sharedfolder")) { // search
			// shared folder
			main.setPage(VELOCITY_ROOT + "/index2.html");
			mainPanel.setContent(main);
			searchController.doSearchByTypeLimitAccess(SharedFolderFileResource.TYPE_NAME, ureq);
			searchController.enableBackToSearchFormLink(false);
		} else if (userObject.equals("search.wiki")) { // search
			// wiki
			main.setPage(VELOCITY_ROOT + "/index2.html");
 			mainPanel.setContent(main);
			searchController.doSearchByTypeLimitAccess(WikiResource.TYPE_NAME, ureq);
			searchController.enableBackToSearchFormLink(false);
		} else if (userObject.equals("search.podcast")) { // search
			// podcast
			main.setPage(VELOCITY_ROOT + "/index2.html");
 			mainPanel.setContent(main);
			searchController.doSearchByTypeLimitAccess(PodcastFileResource.TYPE_NAME, ureq);
			searchController.enableBackToSearchFormLink(false);
		} else if (userObject.equals("search.blog")) { // search
			// blog
			main.setPage(VELOCITY_ROOT + "/index2.html");
			mainPanel.setContent(main);
			searchController.doSearchByTypeLimitAccess(BlogFileResource.TYPE_NAME, ureq);
			searchController.enableBackToSearchFormLink(false);
		} else if (userObject.equals("search.glossary")) { // search
			// glossary
			main.setPage(VELOCITY_ROOT + "/index2.html");
			mainPanel.setContent(main);
			searchController.doSearchByTypeLimitAccess(GlossaryResource.TYPE_NAME, ureq);
			searchController.enableBackToSearchFormLink(false);
		}	else if (userObject.equals("search.portfolio")) { // search
			// portfolio template maps
			main.setPage(VELOCITY_ROOT + "/index2.html");
			mainPanel.setContent(main);
			searchController.doSearchByTypeLimitAccess(EPTemplateMapResource.TYPE_NAME, ureq);
			searchController.enableBackToSearchFormLink(false);
		} else if (userObject.equals("search.mycourses.student")) {
			// my courses as student
			main.setPage(VELOCITY_ROOT + "/index2.html");
			mainPanel.setContent(main);
			searchController.doSearchMyCoursesStudent(ureq);
			searchController.enableBackToSearchFormLink(false);
		} else if (userObject.equals("search.mycourses.teacher")) {
			// my courses as student
			main.setPage(VELOCITY_ROOT + "/index2.html");
			mainPanel.setContent(main);
			searchController.doSearchMyCoursesTeacher(ureq);
			searchController.enableBackToSearchFormLink(false);
		}
		// encode sub view identifyer into state, attach separated by ":"
		lastUserObject = userObject.toString();
		removeAsListenerAndDispose(deleteTabPaneCtr);
		//fxdiff BAKS-7 Resume function
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(lastUserObject, 0l);
		searchWindowControl = addToHistory(ureq, ores, null);
	}

	private void activateCatalogController(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(!catalogModule.isCatalogRepoEnabled()) return;
		
		// create new catalog controller with given node if none exists
		// create also new catalog controller when the user clicked twice on the
		// catalog link in the menu
		if (catalogCtrl == null || lastUserObject.equals("search.catalog")) {
			removeAsListenerAndDispose(catalogCtrl);
			//fxdiff BAKS-7 Resume function
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("search.catalog", 0l);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			catalogCtrl = new CatalogController(ureq, bwControl);
			listenTo(catalogCtrl);
		}
		catalogCtrl.activate(ureq, entries, state);

		// set correct tool controller
		ToolController ccToolCtr = catalogCtrl.createCatalogToolController();
		Component toolComp = (ccToolCtr == null ? null : ccToolCtr.getInitialComponent());
		columnsLayoutCtr.setCol2(toolComp);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == chooseStepsController) {
			if (event.equals(RepositoryAddChooseStepsController.CREATION_WIZARD)) {
				cmc.deactivate();
				RepositoryEntry addedEntry = chooseStepsController.getCourseRepositoryEntry();
				RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(addedEntry);
				creationWizardController = handler.createWizardController(addedEntry, urequest, getWindowControl());
				listenTo(creationWizardController);
				getWindowControl().pushAsModalDialog(creationWizardController.getInitialComponent());
			} else if (event.equals(RepositoryAddChooseStepsController.DETAILS_VIEW)) {
				// show details view
				cmc.deactivate();
			} else if (event.equals(RepositoryAddChooseStepsController.COURSE_EDIT)) {
				// show course editor
				cmc.deactivate();
				ToolController toolC = detailsController.setEntry(chooseStepsController.getCourseRepositoryEntry(), urequest, false);
				Component toolComp = (toolC == null ? null : toolC.getInitialComponent());
				columnsLayoutCtr.setCol2(toolComp);
				mainPanel.setContent(detailsController.getInitialComponent());
				detailsController.doEdit(urequest);
			} else if (event.equals(Event.CANCELLED_EVENT)) {
				cmc.deactivate();
			}
			removeAsListenerAndDispose(chooseStepsController);
		} else if (source == creationWizardController) {
			if (event.equals(Event.CHANGED_EVENT)) {
				getWindowControl().pop();
				removeAsListenerAndDispose(creationWizardController);
				RepositoryEntry re = chooseStepsController.getCourseRepositoryEntry();
				detailsController.setEntry(re, urequest, false);
				detailsController.doLaunch(urequest, re);
			} else if (event.equals(Event.CANCELLED_EVENT)) {
				// delete entry when the wizard was cancelled
				detailsController.deleteRepositoryEntry(urequest, getWindowControl(), (RepositoryEntry) chooseStepsController.getCourseRepositoryEntry());
				getWindowControl().pop();
				removeAsListenerAndDispose(creationWizardController);
				mainPanel.setContent(main);
			}
		} else if (source == addController) { // process add controller finished()
			if (event.equals(Event.DONE_EVENT)) {
				// default to myEntries search
				cmc.deactivate();
				ToolController toolC = detailsController.setEntry(addController.getAddedEntry(), urequest, false);
				Component toolComp = (toolC == null ? null : toolC.getInitialComponent());
				mainPanel.setContent(detailsController.getInitialComponent());
				columnsLayoutCtr.setCol2(toolComp);
				menuTree.setSelectedNodeId(myEntriesNodeId);

				RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(addController.getAddedEntry());
				// ask if we should start the editor/wizard right now.
				if (handler.supportsEdit(addController.getAddedEntry())) {
					// check if wizard is supported and if the repo entry is new (not imported)
					boolean isNew = addController.getActionProcess().equals(RepositoryAddController.PROCESS_NEW);
					if (handler.supportsWizard(addController.getAddedEntry()) && isNew) {
						removeAsListenerAndDispose(chooseStepsController);
						chooseStepsController = new RepositoryAddChooseStepsController(urequest, getWindowControl(), addController.getAddedEntry());
						listenTo(chooseStepsController);
						cmc = new CloseableModalController(getWindowControl(), translate("close"), chooseStepsController.getInitialComponent());
						cmc.activate();
					} else {
						removeAsListenerAndDispose(launchEditorDialog); // cleanup old instance
						launchEditorDialog = activateYesNoDialog(urequest, null, translate("add.launchedit.msg"), launchEditorDialog);
						launchEditorDialog.setUserObject(addController.getAddedEntry());
					}
					removeAsListenerAndDispose(addController);
					addController = null;
					return;
				}
			} else if (event.equals(Event.CANCELLED_EVENT)) {
				cmc.deactivate();
				removeAsListenerAndDispose(addController);
				addController = null;
			} else if (event.equals(Event.FAILED_EVENT)) {
				getWindowControl().setError(translate("add.failed"));
				mainPanel.setContent(main);
			}
		} else if (source == searchController) {
			// FXOLAT-398  searchControlle.getSelectedEntry can be null, catch that to prevent NPE
			RepositoryEntry selEntry =  searchController.getSelectedEntry();
			if(selEntry == null){
				showWarning("repositoryentry.not.existing");
				return;
			}
			RepositoryEntry selectedEntry = RepositoryManager.getInstance().lookupRepositoryEntry(searchController.getSelectedEntry().getKey());
			if (selectedEntry == null) {
				showWarning("warn.entry.meantimedeleted");
				main.setPage(VELOCITY_ROOT + "/index.html");
				return;
			}
			if (event.getCommand().equals(RepositoryTableModel.TABLE_ACTION_SELECT_ENTRY)) {
				// entry has been selected to be launched in the
				// searchController

				if (selectedEntry.getCanLaunch()) {
					if(!detailsController.doLaunch(urequest, selectedEntry)) {
						//cannot launch -> open details
						ToolController toolC = detailsController.setEntry(selectedEntry, urequest, false);
						Component toolComp = (toolC == null ? null : toolC.getInitialComponent());
						columnsLayoutCtr.setCol2(toolComp);
						mainPanel.setContent(detailsController.getInitialComponent());
					}
				} else if (selectedEntry.getCanDownload()) {
					detailsController.doDownload(urequest, selectedEntry, false);
				} else { // offer details view
					ToolController toolC = detailsController.setEntry(selectedEntry, urequest, false);
					Component toolComp = (toolC == null ? null : toolC.getInitialComponent());
					columnsLayoutCtr.setCol2(toolComp);
					mainPanel.setContent(detailsController.getInitialComponent());
				}
			} else if (event.getCommand().equals(RepositoryTableModel.TABLE_ACTION_SELECT_LINK)) {
				// entry has been selected in the searchController
				ToolController toolC = detailsController.setEntry(selectedEntry, urequest, false);
				Component toolComp = (toolC == null ? null : toolC.getInitialComponent());
				columnsLayoutCtr.setCol2(toolComp);
				mainPanel.setContent(detailsController.getInitialComponent());
				//fxdiff BAKS-7 Resume function
				addToHistory(urequest, selectedEntry, null, searchWindowControl, true);
			}
		} else if (source == detailsController) { // back from details
			//fxdiff FXOLAT-128: back/resume function
			if (event.equals(Event.DONE_EVENT)) {
				if (backtocatalog) {
					backtocatalog = false;
					ToolController toolC = catalogCtrl.createCatalogToolController();
					Component toolComp = (toolC == null ? null : toolC.getInitialComponent());
					columnsLayoutCtr.setCol2(toolComp);
					mainPanel.setContent(catalogCtrl.getInitialComponent());
					catalogCtrl.updateHistory(urequest);
				} else {
					Component toolComp = (mainToolC == null ? null : mainToolC.getInitialComponent());
					columnsLayoutCtr.setCol2(toolComp);
					mainPanel.setContent(main);
					addToHistory(urequest, searchWindowControl);
				}
			} else if (event instanceof EntryChangedEvent) {
				Component toolComp = (mainToolC == null ? null : mainToolC.getInitialComponent());
				columnsLayoutCtr.setCol2(toolComp);
				mainPanel.setContent(main);
			} else if (event.equals(Event.CHANGED_EVENT)) {
				ToolController toolC = detailsController.getDetailsToolController();
				Component toolComp = (toolC == null ? null : toolC.getInitialComponent());
				columnsLayoutCtr.setCol2(toolComp);
			}
		} else if (source == mainToolC) {
			// handles the tools events for the Repository Actions
			handleToolEvents(urequest, event);
		} else if (source == launchEditorDialog) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				// do
				// launch
				// editor
				ToolController toolC = detailsController.setEntry((RepositoryEntry) launchEditorDialog.getUserObject(), urequest, false);
				Component toolComp = (toolC == null ? null : toolC.getInitialComponent());
				columnsLayoutCtr.setCol2(toolComp);
				mainPanel.setContent(detailsController.getInitialComponent());
				detailsController.doEdit(urequest);
			} else {
				// no, don't launch editor }
			}
		}
		/*
		 * from the catalog controller its sending change events if the tools
		 * available have changed, e.g. no longer localTreeAdmin
		 */
		else if (source == catalogCtrl) {
			if (event == Event.CHANGED_EVENT) {
				ToolController toolC = catalogCtrl.createCatalogToolController();
				Component toolComp = (toolC == null ? null : toolC.getInitialComponent());
				columnsLayoutCtr.setCol2(toolComp);
			} else if (event instanceof EntryChangedEvent) {
				// REVIEW:pb:signal from the catalog to show Detail!
				// REVIEW:pb:the first event is also a change event, signalling a change
				// in the tools
				// REVIEW:pb:the EntryChangedEvent is misused to signal "show details"
				EntryChangedEvent entryChangedEvent = (EntryChangedEvent) event;
				RepositoryEntry e = RepositoryManager.getInstance().lookupRepositoryEntry(entryChangedEvent.getChangedEntryKey());
				if (e != null) {
					backtocatalog = true;
					ToolController toolC = detailsController.setEntry(e, urequest, false);
					Component toolComp = (toolC == null ? null : toolC.getInitialComponent());
					columnsLayoutCtr.setCol2(toolComp);
					mainPanel.setContent(detailsController.getInitialComponent());
				}
			}

		} else if (source == wc) {
			if (event == Event.CANCELLED_EVENT) {
				wc.dispose();
				cmc.deactivate();
			}
			if (event == Event.DONE_EVENT) {
				wc.dispose();
				cmc.deactivate();
			}
		} else if (source == cmc) { //user closes the overlay and not the cancel button -> clean up repo entry
			if (addController != null) {
				removeAsListenerAndDispose(addController);//does the clean up 
				addController = null;
			}
			fireEvent(urequest, Event.CANCELLED_EVENT);
		}
	}

	/**
	 * @param urequest
	 * @param event
	 */
	private void handleToolEvents(UserRequest urequest, Event event) {
		/*
		 * Repository Tools
		 */
		if (event.getCommand().startsWith(RepositoryAddController.ACTION_ADD_PREFIX)) {
			removeAsListenerAndDispose(addController);
			addController = new RepositoryAddController(urequest, getWindowControl(), event.getCommand());
			listenTo(addController);
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), addController.getInitialComponent(),
					true, addController.getTitle());
			listenTo(cmc);
			cmc.activate();
			return;
		} else if (event.getCommand().equals(ACTION_NEW_CREATECOURSE)) {
			removeAsListenerAndDispose(addController);
			addController = new RepositoryAddController(urequest, getWindowControl(), RepositoryAddController.ACTION_NEW_COURSE);
			listenTo(addController);
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), addController.getInitialComponent(),
					true, addController.getTitle());
			listenTo(cmc);
			cmc.activate();
			return;
		} else if (event.getCommand().equals(ACTION_NEW_CREATETEST)) {
			removeAsListenerAndDispose(addController);
			addController = new RepositoryAddController(urequest, getWindowControl(), RepositoryAddController.ACTION_NEW_TEST);
			listenTo(addController);
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), addController.getInitialComponent(),
					true, addController.getTitle());
			listenTo(cmc);
			cmc.activate();
			return;
		} else if (event.getCommand().equals(ACTION_NEW_CREATESURVEY)) {
			removeAsListenerAndDispose(addController);
			addController = new RepositoryAddController(urequest, getWindowControl(), RepositoryAddController.ACTION_NEW_SURVEY);
			listenTo(addController);
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), addController.getInitialComponent(),
					true, addController.getTitle());
			listenTo(cmc);
			cmc.activate();
			return;
		} else if (event.getCommand().equals(ACTION_NEW_CREATESHAREDFOLDER)) {
			removeAsListenerAndDispose(addController);
			addController = new RepositoryAddController(urequest, getWindowControl(), RepositoryAddController.ACTION_NEW_SHAREDFOLDER);
			listenTo(addController);
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), addController.getInitialComponent(),
					true, addController.getTitle());
			listenTo(cmc);
			cmc.activate();
			return;
		} else if (event.getCommand().equals(ACTION_NEW_WIKI)) {
			removeAsListenerAndDispose(addController);
			addController = new RepositoryAddController(urequest, getWindowControl(), RepositoryAddController.ACTION_NEW_WIKI);
			listenTo(addController);
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), addController.getInitialComponent(),
					true, addController.getTitle());
			listenTo(cmc);
			cmc.activate();
			return;
		} else if (event.getCommand().equals(ACTION_NEW_PODCAST)) {
			removeAsListenerAndDispose(addController);
			addController = new RepositoryAddController(urequest, getWindowControl(), RepositoryAddController.ACTION_NEW_PODCAST);
			listenTo(addController);
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), addController.getInitialComponent(),
					true, addController.getTitle());
			listenTo(cmc);
			cmc.activate();
			return;
		} else if (event.getCommand().equals(ACTION_NEW_BLOG)) {
			removeAsListenerAndDispose(addController);
			addController = new RepositoryAddController(urequest, getWindowControl(), RepositoryAddController.ACTION_NEW_BLOG);
			listenTo(addController);
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), addController.getInitialComponent(),
					true, addController.getTitle());
			listenTo(cmc);
			cmc.activate();
			return;
		} else if (event.getCommand().equals(ACTION_NEW_GLOSSARY)) {
			removeAsListenerAndDispose(addController);
			addController = new RepositoryAddController(urequest, getWindowControl(), RepositoryAddController.ACTION_NEW_GLOSSARY);
			listenTo(addController);
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), addController.getInitialComponent(),
					true, addController.getTitle());
			listenTo(cmc);
			cmc.activate();
			return;
		} else if (event.getCommand().equals(ACTION_NEW_PORTFOLIO)) {
			removeAsListenerAndDispose(addController);
			addController = new RepositoryAddController(urequest, getWindowControl(), RepositoryAddController.ACTION_NEW_PORTFOLIO);
			listenTo(addController);
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), addController.getInitialComponent(),
					true, addController.getTitle());
			listenTo(cmc);
			cmc.activate();
			return;
		} else if (event.getCommand().equals(ACTION_DELETE_RESOURCE)) {
			removeAsListenerAndDispose(deleteTabPaneCtr);
			deleteTabPaneCtr = new TabbedPaneController(urequest, getWindowControl());
			listenTo(deleteTabPaneCtr);
			mainPanel.setContent(deleteTabPaneCtr.getInitialComponent());
		} else if (event.getCommand().equals(ACTION_NEW_CREATECP)) {
			removeAsListenerAndDispose(addController);
			addController = new RepositoryAddController(urequest, getWindowControl(), RepositoryAddController.ACTION_NEW_CP);
			listenTo(addController);
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), addController.getInitialComponent(),
					true, addController.getTitle());
			listenTo(cmc);
			cmc.activate();
			return;
		} else if (event.getCommand().equals(ACTION_ADD_OWNERS)) {
			removeAsListenerAndDispose(wc);
			wc = new WizardAddOwnersController(urequest, getWindowControl());
			listenTo(wc);
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), wc.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
			return;
		}
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		//activate the catalog
		if(CatalogEntry.class.getSimpleName().equals(type)) {
			if(catalogModule.isCatalogRepoEnabled()) {
				TreeNode rootNode = menuTree.getTreeModel().getRootNode();
				TreeNode activatedNode = TreeHelper.findNodeByUserObject("search.catalog", rootNode);
				if (activatedNode != null) {
					menuTree.setSelectedNodeId(activatedNode.getIdent());
					activateContent(ureq, "search.catalog", entries, entry.getTransientState());
				}
			}
		} else if (RepositoryEntry.class.getSimpleName().equals(type)) {
			Long key = entry.getOLATResourceable().getResourceableId();
			RepositoryEntry selectedEntry = RepositoryManager.getInstance().lookupRepositoryEntry(key);
			if (selectedEntry != null) {
				ToolController toolC = detailsController.setEntry(selectedEntry, ureq, true);
				Component toolComp = (toolC == null ? null : toolC.getInitialComponent());
				columnsLayoutCtr.setCol2(toolComp);
				mainPanel.setContent(detailsController.getInitialComponent());
			}
		} else {
			TreeNode rootNode = menuTree.getTreeModel().getRootNode();
			TreeNode activatedNode = TreeHelper.findNodeByUserObject(type, rootNode);
			if (activatedNode != null) {
				menuTree.setSelectedNodeId(activatedNode.getIdent());
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				activateContent(ureq, type, subEntries, entry.getTransientState());
				if(!subEntries.isEmpty()) {
					ContextEntry nextEntry = subEntries.get(0);
					String subType = nextEntry.getOLATResourceable().getResourceableTypeName();
					if(RepositoryEntry.class.getSimpleName().equals(subType)) {
						searchController.activate(ureq, subEntries, nextEntry.getTransientState());
						detailsController.activate(ureq, subEntries.subList(1, subEntries.size()), nextEntry.getTransientState());
					} else if(CatalogEntry.class.getSimpleName().equals(subType)
							&& catalogModule.isCatalogRepoEnabled()) {
						catalogCtrl.activate(ureq, subEntries, entry.getTransientState());
					}
				}
			}
		}
	}
}