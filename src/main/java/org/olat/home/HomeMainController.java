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
* <p>
*/ 

package org.olat.home;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.bookmark.ManageBookmarkController;
import org.olat.commons.rss.RSSUtil;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.defaults.dispatcher.StaticMediaDispatcher;
import org.olat.core.extensions.ExtManager;
import org.olat.core.extensions.Extension;
import org.olat.core.extensions.action.ActionExtension;
import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.HtmlHeaderComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.portal.PortalImpl;
import org.olat.core.gui.control.state.ControllerState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.User;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.util.UserSession;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeHelper;
import org.olat.course.assessment.EfficiencyStatementsListController;
import org.olat.note.NoteListController;
import org.olat.notifications.NotificationUIFactory;
import org.olat.portfolio.EPUIFactory;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.user.HomePageConfigManager;
import org.olat.user.HomePageConfigManagerImpl;
import org.olat.user.PersonalFolderManager;
import org.olat.user.PersonalSettingsController;
import org.olat.user.UserInfoMainController;
import org.olat.util.logging.activity.LoggingResourceable;

import de.bps.olat.user.ChangeEMailExecuteController;

/**
 * <!--**************-->
 * <h3>Responsability:</h3>
 * display the first page the user sees after she logged in successfully. This
 * is the users individual dashboard within the learning management system.<br>
 * The guest login has it's own {@link org.olat.home.GuestHomeMainController first page} !
 * <p>
 * <!--**************-->
 * <h3>Workflow:</h3>
 * <ul>
 * <li><i>Mainflow:</i><br>
 * display portal.</li>
 * <li><i>Portal editing:</i><br>
 * Switch portal to edit mode.<br>
 * edit portal.<br>
 * switch to display mode.</li>
 * <li><i>Activate target XYZ:</i><br>
 * display activated component, i.e. jump to the personal briefcase</li>
 * </ul>
 * <p>
 * <!--**************-->
 * <h3>Activateable targets:</h3>
 * <ul>
 * <li><i>{@link #MENU_ROOT}</i>:<br>
 * main entry point, the portal view.</li>
 * <li><i>{@link #MENU_BC}</i>:<br>
 * jump to personal briefcase.</li>
 * <li><i>{@link #MENU_NOTE}</i>:<br>
 * list of user notes.</li>
 * <li><i>{@link #MENU_BOOKMARKS}</i>:<br>
 * users bookmarks list.</li>
 * <li><i>{@link #MENU_ADMINNOTIFICATIONS}</i>:<br>
 * notifications list.</li>
 * <li><i>{@link #MENU_OTHERUSERS}</i>:<br>
 * search other users workflow.</li>
 * <li><i>{@link #MENU_EFFICIENCY_STATEMENTS}</i>:<br>
 * list of users efficency statements.</li>
 * </ul>
 * <p>
 * <!--**************-->
 * <h3>Hints:</h3>
 * TODO:fg:a add here the hints/special notes for the HomeMainController
 * <p>
 * 
 * Initial Date: Apr 27, 2004
 * @author Felix Jost
 */
public class HomeMainController extends MainLayoutBasicController implements Activateable {

	// Menu commands. These are also used in velocity containers. When changing
	// the values make sure you performed a full text search first
	private static final String MENU_NOTE = "note";
	private static final String MENU_BC = "bc";
	private static final String MENU_CALENDAR = "cal";
	private static final String MENU_BOOKMARKS = "bookmarks";
	private static final String MENU_EFFICIENCY_STATEMENTS = "efficiencyStatements";
	private static final String MENU_ADMINNOTIFICATIONS = "adminnotifications";
	private static final String MENU_MYSETTINGS = "mysettings";
	private static final String MENU_ROOT = "root";
	private static final String MENU_OTHERUSERS = "otherusers";
	private static final String MENU_PORTFOLIO = "portfolio";
	private static final String MENU_PORTFOLIO_ARTEFACTS = "AbstractArtefact";
	private static final String MENU_PORTFOLIO_MY_MAPS = "portfolioMyMaps";
	private static final String MENU_PORTFOLIO_MY_STRUCTURED_MAPS = "portfolioMyStructuredMaps";
	private static final String MENU_PORTFOLIO_OTHERS_MAPS = "portfolioOthersMaps";
	private static final String PRESENTED_EMAIL_CHANGE_REMINDER = "presentedemailchangereminder";

	private static boolean extensionLogged = false;

	private MenuTree olatMenuTree;
	private VelocityContainer welcome;
	private Link portalBackButton;
	private Link portalEditButton;
	private VelocityContainer inclTitle;
	private Panel content;

	private LayoutMain3ColsController columnLayoutCtr;
	private Controller resC;
	private String titleStr;
	private PortalImpl myPortal;

	/**
	 * Constructor of the home main controller
	 * 
	 * @param ureq The user request
	 * @param wControl The current window controller
	 */
	public HomeMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, updateBusinessPath(ureq,wControl));

		addLoggingResourceable(LoggingResourceable.wrap(ureq.getIdentity()));

		olatMenuTree = new MenuTree("olatMenuTree");
		TreeModel tm = buildTreeModel(ureq);
		olatMenuTree.setTreeModel(tm);
		olatMenuTree.setSelectedNodeId(tm.getRootNode().getIdent());
		olatMenuTree.addListener(this);
		// Activate correct position in menu
		olatMenuTree.setSelectedNode(tm.getRootNode());
		setState("root");

		// prepare main panel
		content = new Panel("content");
		content.setContent(createRootComponent(ureq));

		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), olatMenuTree, null, content, "homemain");
		listenTo(columnLayoutCtr);// cleanup on dispose
		// add background image to home site
		columnLayoutCtr.addCssClassToMain("o_home");
		putInitialPanel(columnLayoutCtr.getInitialComponent());
		
		// check if an existing session was killed, this is detected in UserSession.signOn()
		Object killedExistingSession = ureq.getUserSession().getEntry(UserSession.STORE_KEY_KILLED_EXISTING_SESSION);
		if (killedExistingSession != null && (killedExistingSession instanceof Boolean) ) {
			if ( ((Boolean)killedExistingSession).booleanValue() ) {
				this.showInfo("warn.session.was.killed");
				ureq.getUserSession().removeEntry(UserSession.STORE_KEY_KILLED_EXISTING_SESSION);
			}
		}
		
		// check running of email change workflow
	  if (ureq.getIdentity().getUser().getProperty("emchangeKey", null) != null) {
	  	ChangeEMailExecuteController mm = new ChangeEMailExecuteController(ureq, wControl);
			if (mm.isLinkTimeUp()) {
				mm.deleteRegistrationKey();
			} else {
				if (mm.isLinkClicked()) {
					mm.changeEMail(wControl);
					activateContent(ureq, MENU_MYSETTINGS, null);
				} else {
      		Boolean alreadySeen = ((Boolean)ureq.getUserSession().getEntry(PRESENTED_EMAIL_CHANGE_REMINDER));
      		if (alreadySeen == null) {
      			getWindowControl().setWarning(mm.getPackageTranslator().translate("email.change.reminder"));
      			ureq.getUserSession().putEntry(PRESENTED_EMAIL_CHANGE_REMINDER, Boolean.TRUE);
      		}
				}
			}
		} else {
			User user = ureq.getIdentity().getUser();
			String value = user.getProperty("emailDisabled", null);
			if (value != null && value.equals("true")) {
				wControl.setWarning(translate("email.disabled"));
			}
		}
	}
	
		
	private static WindowControl updateBusinessPath(UserRequest ureq, WindowControl wControl) {
		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ureq.getIdentity());
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, wControl);
		return bwControl;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == portalBackButton){
			this.myPortal.setIsEditMode(ureq, Boolean.FALSE);
			welcome.contextPut("portalEditMode", Boolean.FALSE);
		} else if (source == portalEditButton){
			this.myPortal.setIsEditMode(ureq, Boolean.TRUE);
			welcome.contextPut("portalEditMode", Boolean.TRUE);
		} else if (source == olatMenuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				// process menu commands
				TreeNode selTreeNode = olatMenuTree.getSelectedNode();
				if (selTreeNode.getDelegate() != null) {
					selTreeNode = selTreeNode.getDelegate();
					olatMenuTree.setSelectedNode(selTreeNode); // enable right element
				}
				// test for extension commands
				Object uObj = selTreeNode.getUserObject();
				activateContent(ureq, uObj, null);
			} else { // FIXME:fj:b what is this...the action was not allowed anymore
				content.setContent(null); // display an empty field (empty panel)
			}
		} else {
			logWarn("Unhandled olatMenuTree event: " + event.getCommand(), null);
		}
	}

	/**
	 * Activate the content in the content area based on a user object
	 * representing the identifyer of the content
	 * 
	 * @param ureq
	 * @param uObj
	 * @param activation args null or argument that is passed to child
	 */
	private void activateContent(UserRequest ureq, Object uObj, String activationArgs) {
		if (uObj instanceof ActionExtension) {
			ActionExtension ae = (ActionExtension) uObj;
			removeAsListenerAndDispose(resC);
			Controller extC = ae.createController(ureq, getWindowControl(), null);
			content.setContent(extC.getInitialComponent());
			this.resC = extC;
			listenTo(resC);
			if(resC instanceof Activateable) {
				((Activateable)resC).activate(ureq, activationArgs);
			}
			
		} else {
			String cmd = (String) uObj;
			doActivate(cmd, ureq, activationArgs);
		}
	}
	
	private void doActivate(String cmd, UserRequest ureq, String activationArgs) {
		setState(cmd);
		if (cmd.equals(MENU_ROOT)) { // check for root node clicked
			content.setContent(createRootComponent(ureq));
		} else { // create a controller
			removeAsListenerAndDispose(resC);
			this.resC = createController(cmd, ureq);
			listenTo(resC);
			// activate certain state on controller
			if (activationArgs != null && resC instanceof Activateable){
				Activateable activatableCtr = (Activateable) resC;
				activatableCtr.activate(ureq, activationArgs);
			}
			Component resComp = resC.getInitialComponent();
			inclTitle = createVelocityContainer("incltitle");
			inclTitle.contextPut("titleString", titleStr);
			inclTitle.contextPut("command", cmd);
			inclTitle.put("exclTitle", resComp);
			content.setContent(inclTitle);
		}
	}
	
	protected void adjustState(ControllerState cstate, UserRequest ureq) {
		String cmd = cstate.getSerializedState();
		doActivate(cmd, ureq, null);
		// adjust the menu
		TreeNode tn = TreeHelper.findNodeByUserObject(cmd, olatMenuTree.getTreeModel().getRootNode());
		olatMenuTree.setSelectedNode(tn);
	}


	/**
	 * @param ureq
	 * @param source
	 * @param event
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == resC) {
			// TODO:as:a move to own controller (homepage whatever controller)
			if (event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent foundEvent = (SingleIdentityChosenEvent) event;
				Identity chosenIdentity = foundEvent.getChosenIdentity();
				if (chosenIdentity != null) {
					HomePageConfigManager hpcm = HomePageConfigManagerImpl.getInstance();
					OLATResourceable ores = hpcm.loadConfigFor(chosenIdentity.getName());
					DTabs dts = (DTabs)Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");
					//was brasato:: DTabs dts = getWindowControl().getDTabs();
					DTab dt = dts.getDTab(ores);
					if (dt == null) {
						// does not yet exist -> create and add
						dt = dts.createDTab(ores, chosenIdentity.getName());
						if (dt == null) return;
						UserInfoMainController uimc = new UserInfoMainController(ureq, dt.getWindowControl(), chosenIdentity);
						dt.setController(uimc);
						dts.addDTab(dt);
					}
					dts.activate(ureq, dt, null);
				}
			}
		}
	}

	private Component createRootComponent(UserRequest ureq) {
		// start screen
		welcome = createVelocityContainer("welcome");
		portalBackButton = LinkFactory.createButtonXSmall("command.portal.back", welcome, this);
		portalEditButton = LinkFactory.createButtonXSmall("command.portal.edit", welcome, this);
		
		// rss link
		String rssLink = RSSUtil.getPersonalRssLink(ureq);
		welcome.contextPut("rssLink", rssLink);
		StringOutput staticUrl = new StringOutput();
		StaticMediaDispatcher.renderStaticURI(staticUrl, "js/egg.js");
		welcome.put("htmlHeader", new HtmlHeaderComponent("rss", null, "<link rel=\"alternate\" type=\"application/rss+xml\" title=\""
				+ StringEscapeUtils.escapeHtml(translate("welcome.rss")) + "\" href=\"" + rssLink + "\" />\n" + "<script type=\"text/javascript\" src=\""
				+ staticUrl.toString() + "\"></script>"));

		// add portal
		if (myPortal == null) myPortal = ((PortalImpl)CoreSpringFactory.getBean("homeportal")).createInstance(getWindowControl(), ureq);
		welcome.put("myPortal", myPortal.getInitialComponent());
		welcome.contextPut("portalEditMode", Boolean.FALSE);

		return welcome;
	}

	private Controller createController(String uobject, UserRequest ureq) {
		if (uobject.equals(MENU_BC)) {
			titleStr = translate("menu.bc");
			return new FolderRunController(PersonalFolderManager.getInstance().getContainer(ureq.getIdentity()), true, true, ureq, getWindowControl());
		} else if (uobject.equals(MENU_MYSETTINGS)) {
			titleStr = translate("menu.mysettings");
			return new PersonalSettingsController(ureq, getWindowControl());
		} else if (uobject.equals(MENU_CALENDAR)) {
			titleStr = translate("menu.calendar");
			return new HomeCalendarController(ureq, getWindowControl());
		} else if (uobject.equals(MENU_BOOKMARKS)) {
			titleStr = translate("menu.bookmarks");
			return new ManageBookmarkController(ureq, getWindowControl(), true, ManageBookmarkController.SEARCH_TYPE_ALL);
		} else if (uobject.equals(MENU_EFFICIENCY_STATEMENTS)) {
			titleStr = translate("menu.efficiencyStatements");
			return new EfficiencyStatementsListController(getWindowControl(), ureq);
		} else if (uobject.equals(MENU_ADMINNOTIFICATIONS)) {
			titleStr = translate("menu.notifications");
			return NotificationUIFactory.createCombinedSubscriptionsAndNewsController(ureq.getIdentity(), ureq, getWindowControl());
		} else if (uobject.equals(MENU_NOTE)) {
			titleStr = translate("menu.note");
			return new NoteListController(ureq, getWindowControl());
		} else if (uobject.equals(MENU_OTHERUSERS)) {
			titleStr = translate("menu.otherusers");
			return new UserSearchController(ureq, getWindowControl(), false);
		} else if (uobject.equals(MENU_PORTFOLIO_ARTEFACTS)) {
			titleStr = "";
			return EPUIFactory.createPortfolioPoolController(ureq, getWindowControl());
		} else if (uobject.equals(MENU_PORTFOLIO_MY_MAPS)) {
			titleStr = "";
			return EPUIFactory.createPortfolioMapsController(ureq, getWindowControl());
		} else if (uobject.equals(MENU_PORTFOLIO_MY_STRUCTURED_MAPS)) {
			titleStr = "";
			return EPUIFactory.createPortfolioStructuredMapsController(ureq, getWindowControl());
		} else if (uobject.equals(MENU_PORTFOLIO_OTHERS_MAPS)) {
			titleStr = "";
			return EPUIFactory.createPortfolioMapsFromOthersController(ureq, getWindowControl());
		}
		return null;
	}

	private TreeModel buildTreeModel(UserRequest ureq) {
		GenericTreeNode root, gtn;

		GenericTreeModel gtm = new GenericTreeModel();
		root = new GenericTreeNode();
		root.setTitle(translate("menu.root"));
		root.setUserObject(MENU_ROOT);
		root.setAltText(translate("menu.root.alt"));
		gtm.setRootNode(root);

		gtn = new GenericTreeNode();
		gtn.setTitle(translate("menu.mysettings"));
		gtn.setUserObject(MENU_MYSETTINGS);
		gtn.setAltText(translate("menu.mysettings.alt"));
		root.addChild(gtn);

		gtn = new GenericTreeNode();
		gtn.setTitle(translate("menu.calendar"));
		gtn.setUserObject(MENU_CALENDAR);
		gtn.setAltText(translate("menu.calendar.alt"));
		root.addChild(gtn);

		gtn = new GenericTreeNode();
		gtn.setTitle(translate("menu.notifications"));
		gtn.setUserObject(MENU_ADMINNOTIFICATIONS);
		gtn.setAltText(translate("menu.notifications.alt"));
		root.addChild(gtn);

		gtn = new GenericTreeNode();
		gtn.setTitle(translate("menu.bookmarks"));
		gtn.setUserObject(MENU_BOOKMARKS);
		gtn.setAltText(translate("menu.bookmarks.alt"));
		root.addChild(gtn);

		gtn = new GenericTreeNode();
		gtn.setTitle(translate("menu.bc"));
		gtn.setUserObject(MENU_BC);
		gtn.setAltText(translate("menu.bc.alt"));
		root.addChild(gtn);

		gtn = new GenericTreeNode();
		gtn.setTitle(translate("menu.note"));
		gtn.setUserObject(MENU_NOTE);
		gtn.setAltText(translate("menu.note.alt"));
		root.addChild(gtn);

		gtn = new GenericTreeNode();
		gtn.setTitle(translate("menu.efficiencyStatements"));
		gtn.setUserObject(MENU_EFFICIENCY_STATEMENTS);
		gtn.setAltText(translate("menu.efficiencyStatements.alt"));
		root.addChild(gtn);

		//not yet active		
//		gtn = new GenericTreeNode();
//		gtn.setTitle(translate("menu.weblog"));
//		gtn.setUserObject(MENU_WEBLOG);
//		gtn.setAltText(translate("menu.weblog.alt"));
//		root.addChild(gtn);
		
		gtn = new GenericTreeNode();
		gtn.setTitle(translate("menu.otherusers"));
		gtn.setUserObject(MENU_OTHERUSERS);
		gtn.setAltText(translate("menu.otherusers.alt"));
		root.addChild(gtn);
		
		PortfolioModule portfolioModule = (PortfolioModule)CoreSpringFactory.getBean("portfolioModule");
		if(portfolioModule.isEnabled()) {
			//node portfolio
			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.portfolio"));
			gtn.setUserObject(MENU_PORTFOLIO);
			gtn.setAltText(translate("menu.portfolio.alt"));
			root.addChild(gtn);
			
			//my artefacts
			GenericTreeNode pgtn = new GenericTreeNode();
			pgtn.setTitle(translate("menu.portfolio.myartefacts"));
			pgtn.setUserObject(MENU_PORTFOLIO_ARTEFACTS);
			pgtn.setAltText(translate("menu.portfolio.myartefacts.alt"));
			gtn.setDelegate(pgtn);
			gtn.addChild(pgtn);
			
			//my maps
			pgtn = new GenericTreeNode();
			pgtn.setTitle(translate("menu.portfolio.mymaps"));
			pgtn.setUserObject(MENU_PORTFOLIO_MY_MAPS);
			pgtn.setAltText(translate("menu.portfolio.mymaps.alt"));
			gtn.addChild(pgtn);
			
			//my exercises
			pgtn = new GenericTreeNode();
			pgtn.setTitle(translate("menu.portfolio.mystructuredmaps"));
			pgtn.setUserObject(MENU_PORTFOLIO_MY_STRUCTURED_MAPS);
			pgtn.setAltText(translate("menu.portfolio.mystructuredmaps.alt"));
			gtn.addChild(pgtn);
			
			//others maps
			pgtn = new GenericTreeNode();
			pgtn.setTitle(translate("menu.portfolio.othermaps"));
			pgtn.setUserObject(MENU_PORTFOLIO_OTHERS_MAPS);
			pgtn.setAltText(translate("menu.portfolio.othermaps.alt"));
			gtn.addChild(pgtn);
		}

		// add extension menues
		ExtManager extm = ExtManager.getInstance();
		Class<? extends HomeMainController> extensionPointMenu = this.getClass();
		int cnt = extm.getExtensionCnt();
		for (int i = 0; i < cnt; i++) {
			Extension anExt = extm.getExtension(i);
			// check for extensions
			ActionExtension ae = (ActionExtension) anExt.getExtensionFor(extensionPointMenu.getName());
			if (ae != null) {
				if (anExt.isEnabled()) {
					gtn = new GenericTreeNode();
					String menuText = ae.getActionText(getLocale());
					gtn.setTitle(menuText);
					gtn.setUserObject(ae);
					gtn.setAltText(ae.getDescription(getLocale()));

					if (ae instanceof GenericActionExtension && ((GenericActionExtension) ae).getNodeIdentifierIfParent() != null) gtn.setIdent(((GenericActionExtension) ae).getNodeIdentifierIfParent());
					if (ae instanceof GenericActionExtension && ((GenericActionExtension) ae).getParentTreeNodeIdentifier() != null){
						GenericTreeNode parentNode = (GenericTreeNode) gtm.getNodeById(((GenericActionExtension) ae).getParentTreeNodeIdentifier());
						if (parentNode == null) throw new AssertException("could not find parent treeNode: " + ((GenericActionExtension) ae).getParentTreeNodeIdentifier() + ", make sure it gets loaded before child!");
						parentNode.addChild(gtn);
						if (parentNode.getDelegate() == null) parentNode.setDelegate(gtn);
					} else {
						root.addChild(gtn);
					}

				}
			}
		}

		return gtm;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// controllers are disposed in BasicController
		if (myPortal != null) {
			myPortal.dispose();
			myPortal = null;
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.dtabs.Activateable#activate(org.olat.core.gui.UserRequest,
	 *      java.lang.String)
	 */
	public void activate(UserRequest ureq, String viewIdentifier) {
		
		String subViewIdentifier = null;
		int firstDot = viewIdentifier.indexOf(".");
		if (firstDot != -1) {
			subViewIdentifier = viewIdentifier.substring(firstDot + 1, viewIdentifier.length());
			viewIdentifier = viewIdentifier.substring(0,firstDot);
		}
		String[] parsedViewIdentifyers = viewIdentifier.split(":");
				
		// find the menu node that has the user object that represents the
		// viewIdentifyer
		TreeNode rootNode = this.olatMenuTree.getTreeModel().getRootNode();
		TreeNode activatedNode = TreeHelper.findNodeByUserObject(parsedViewIdentifyers[0], rootNode);
		if(activatedNode == null) {
			activatedNode = findPortfolioNode(rootNode, parsedViewIdentifyers);
			if(activatedNode != null) {
				subViewIdentifier = parsedViewIdentifyers[1];
			}
		}

		if (activatedNode != null) {
			this.olatMenuTree.setSelectedNodeId(activatedNode.getIdent());
			activateContent(ureq, activatedNode.getUserObject(), subViewIdentifier);
		} else {
			// not found, activate the root node
			this.olatMenuTree.setSelectedNodeId(rootNode.getIdent());
			activateContent(ureq, rootNode.getUserObject(), subViewIdentifier);
		}
	}
	
	public TreeNode findPortfolioNode(TreeNode rootNode, String[] parsedViewIdentifyers) {
		String context = parsedViewIdentifyers[0];
		if("EPDefaultMap".equals(context) || "EPStructuredMap".equals(context)) {
			//it's my problem
			EPFrontendManager ePFMgr = (EPFrontendManager)CoreSpringFactory.getBean("epFrontendManager");
			
			Long key = Long.parseLong(parsedViewIdentifyers[1]);
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(context, key);
			boolean owner = ePFMgr.isMapOwner(getIdentity(), ores);
			if(owner) {
				if("EPDefaultMap".equals(context)) {
					return TreeHelper.findNodeByUserObject(MENU_PORTFOLIO_MY_MAPS, rootNode);
				} else if("EPStructuredMap".equals(context)) {
					return TreeHelper.findNodeByUserObject(MENU_PORTFOLIO_MY_STRUCTURED_MAPS, rootNode);
				} else {
					logWarn("Unhandled portfolio map type: " + parsedViewIdentifyers, null);
				}
			} else {
				return TreeHelper.findNodeByUserObject(MENU_PORTFOLIO_OTHERS_MAPS, rootNode);
			}
		}
		return null;
	}
}