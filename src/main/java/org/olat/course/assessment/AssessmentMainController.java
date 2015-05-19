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

package org.olat.course.assessment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.ShortName;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
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
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.NodeTableDataModel.Cols;
import org.olat.course.assessment.bulk.BulkAssessmentOverviewController;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.certificate.CertificateEvent;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.ui.CertificatesWizardController;
import org.olat.course.condition.Condition;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.condition.interpreter.OnlyGroupConditionInterpreter;
import org.olat.course.config.CourseConfig;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.AssessmentToolOptions;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.ProjectBrokerCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import de.bps.onyx.plugin.OnyxModule;
import de.bps.webservices.clients.onyxreporter.OnyxReporterConnector;
import de.bps.webservices.clients.onyxreporter.OnyxReporterException;
import de.bps.webservices.clients.onyxreporter.ReporterRole;

/**
 * Initial Date:  Jun 18, 2004
 *
 * @author gnaegi
 *
 * Comment: 
 * This contoller can be used to control and change user score, passed, attempts and comment variables.
 * It provides a menu that allows three different access paths to the same data: user centric, group 
 * centric or course node centric.
 */
public class AssessmentMainController extends MainLayoutBasicController implements Activateable2, GenericEventListener {
	private static final OLog log = Tracing.createLoggerFor(AssessmentMainController.class);

	private static final String CMD_INDEX = "cmd.index";
	private static final String CMD_USERFOCUS = "cmd.userfocus";
	private static final String CMD_GROUPFOCUS = "cmd.groupfocus";
	private static final String CMD_NODEFOCUS = "cmd.nodefocus";
	private static final String CMD_BULKFOCUS = "cmd.bulkfocus";
	private static final String CMD_EFF_STATEMENT = "cmd.effstatement";

	private static final String CMD_CHOOSE_GROUP = "cmd.choose.group";
	private static final String CMD_CHOOSE_USER = "cmd.choose.user";
	private static final String CMD_SELECT_NODE = "cmd.select.node";
	private static final String CMD_SHOW_ONYXREPORT = "cmd.show.onyxreport";

	public static final String KEY_IS_ONYX = "isOnyx";
	
	private static final int MODE_USERFOCUS		= 0;
	private static final int MODE_GROUPFOCUS	= 1;
	private static final int MODE_NODEFOCUS		= 2;
	private static final int MODE_BULKFOCUS		= 3;
	private static final int MODE_EFF_STATEMENT = 4;
	private int mode;

	private IAssessmentCallback callback;
	private MenuTree menuTree;
	private Panel main;
	
	private VelocityContainer index, groupChoose, userChoose, nodeChoose, wrapper;

	private VelocityContainer onyxReporterVC;
	
	private NodeTableDataModel nodeTableModel;
	private TableController groupListCtr, userListCtr, nodeListCtr;
	private List<ShortName> nodeFilters;
	private List<Identity> identitiesList;

	// Course assessment notification support fields	
	private Controller csc;
	private Controller certificateSubscriptionCtrl;
	private CertificatesWizardController certificateWizardCtrl;
	
	// Hash map to keep references to already created user course environments
	// Serves as a local cache to reduce database access - not shared by multiple threads
	final Map<Long, UserCourseEnvironment> localUserCourseEnvironmentCache; // package visibility for avoiding synthetic accessor method
	final Map<Long, Date> initialLaunchDates;
	// List of groups to which the user has access rights in this course
	private List<BusinessGroup> coachedGroups;
	//Is tutor from the security group of repository entry
	private boolean repoTutor = false;

	// some state variables
	private AssessableCourseNode currentCourseNode;
	private AssessedIdentityWrapper assessedIdentityWrapper;
	private AssessmentEditController assessmentEditController;
	private IdentityAssessmentEditController identityAssessmentController;
	private BusinessGroup currentGroup;
	
	private Thread assessmentCachPreloaderThread;
	private Link backLinkUC;
	private Link backLinkGC;
	private Link allUsersButton;
	
	//back button for the Onyx Reporter
	private Link backLinkOR;
	//backbutton needs information where it should go back
	private String onyxReporterBackLocation;
	
	private boolean isAdministrativeUser;
	private boolean mayViewAllUsersAssessments = false;
	private Translator propertyHandlerTranslator;
	
	private boolean isFiltering = true;
	private Link showAllCourseNodesButton;
	private Link filterCourseNodesButton;

	private EfficiencyStatementAssessmentController esac;
	private BulkAssessmentOverviewController bulkAssOverviewCtrl;
	private final TooledStackedPanel stackPanel;

	private RepositoryEntry re;
	private OLATResourceable ores;
	private final boolean hasAssessableNodes;
	
	@Autowired
	private OnyxModule onyxModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private CertificatesManager certificatesManager;
	
	/**
	 * Constructor for the assessment tool controller. 
	 * @param ureq
	 * @param wControl
	 * @param course
	 * @param assessmentCallback
	 */
	public AssessmentMainController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			OLATResourceable ores, IAssessmentCallback assessmentCallback) {
		super(ureq, wControl);	

		getUserActivityLogger().setStickyActionType(ActionType.admin);
		this.stackPanel = stackPanel;
		this.ores = ores;
		this.callback = assessmentCallback;
		localUserCourseEnvironmentCache = new ConcurrentHashMap<Long, UserCourseEnvironment>();
		initialLaunchDates = new ConcurrentHashMap<Long,Date>();
		
		//use the PropertyHandlerTranslator	as tableCtr translator
		propertyHandlerTranslator = UserManager.getInstance().getPropertyHandlerTranslator(getTranslator());
		
		Roles roles = ureq.getUserSession().getRoles();
		BaseSecurityModule securityModule = CoreSpringFactory.getImpl(BaseSecurityModule.class);
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		//was: (roles.isAuthor() || roles.isGroupManager() || roles.isUserManager() || roles.isOLATAdmin());		
		
		main = new Panel("assessmentmain");

		// Intro page, static
		index = createVelocityContainer("assessment_index");
		
		Identity focusOnIdentity = null;
		ICourse course = CourseFactory.loadCourse(ores);
		hasAssessableNodes = course.hasAssessableNodes();
		boolean hasCertificates = course.getCourseConfig().isAutomaticCertificationEnabled()
				|| course.getCourseConfig().isManualCertificationEnabled();
		
		BusinessControl bc = getWindowControl().getBusinessControl();
		ContextEntry ceIdentity = bc.popLauncherContextEntry();
		if (ceIdentity != null) {
			OLATResourceable oresIdentity = ceIdentity.getOLATResourceable();
			if (OresHelper.isOfType(oresIdentity, Identity.class)) {
				Long identityKey = oresIdentity.getResourceableId();
				focusOnIdentity = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey);
			}
		}
			
		if (hasAssessableNodes) {
			index.contextPut("hasAssessableNodes", new Boolean(hasAssessableNodes));
			
			// --- assessment notification subscription ---
			csc = AssessmentUIFactory.createContextualSubscriptionController(ureq, wControl, course);
			if (csc != null) {
				listenTo(csc); // cleanup on dispose
				index.put("assessmentSubscription", csc.getInitialComponent());
			}
		}
		
		SubscriptionContext subsContext = certificatesManager.getSubscriptionContext(course);
		if (subsContext != null) {
			String businessPath = wControl.getBusinessControl().getAsString();
			PublisherData pData = certificatesManager.getPublisherData(course, businessPath);
			certificateSubscriptionCtrl = new ContextualSubscriptionController(ureq, wControl, subsContext, pData);
			listenTo(certificateSubscriptionCtrl);
			index.put("certificationSubscription", certificateSubscriptionCtrl.getInitialComponent());
		}
			
		
		// Wrapper container: adds header
		wrapper = createVelocityContainer("wrapper");
		
		// Init the group and the user chooser view velocity container
		groupChoose = createVelocityContainer("groupchoose");
		allUsersButton = LinkFactory.createButtonSmall("cmd.all.users", groupChoose, this);
		groupChoose.contextPut("isFiltering", Boolean.TRUE);
		backLinkGC = LinkFactory.createLinkBack(groupChoose, this);
		backLinkGC.setIconLeftCSS("o_icon o_icon_back");

		userChoose = createVelocityContainer("userchoose");

		showAllCourseNodesButton = LinkFactory.createButtonSmall("cmd.showAllCourseNodes", userChoose, this);
		filterCourseNodesButton  = LinkFactory.createButtonSmall("cmd.filterCourseNodes", userChoose, this);
		userChoose.contextPut("isFiltering", Boolean.TRUE);
		backLinkUC = LinkFactory.createLinkBack(userChoose, this);
		backLinkUC.setIconLeftCSS("o_icon o_icon_back");
		
		onyxReporterVC = createVelocityContainer("onyxreporter");
		backLinkOR = LinkFactory.createLinkBack(onyxReporterVC, this);
		backLinkOR.setIconLeftCSS("o_icon o_icon_back");

		nodeChoose = createVelocityContainer("nodechoose");

		// Initialize all groups that the user is allowed to coach
		coachedGroups = getAllowedGroupsFromGroupmanagement(ureq.getIdentity());
		
		re = RepositoryManager.getInstance().lookupRepositoryEntry(ores, true);
		repoTutor = repositoryService.hasRole(getIdentity(), re, GroupRoles.coach.name());

		// preload the assessment cache to speed up everything as background thread
		// the thread will terminate when finished
		assessmentCachPreloaderThread = new AssessmentCachePreloadThread("assessmentCachPreloader-" + course.getResourceableId());
		assessmentCachPreloaderThread.setDaemon(true); 
		assessmentCachPreloaderThread.start();

			
		// Navigation menu
		menuTree = new MenuTree("menuTree");
		TreeModel tm = buildTreeModel(hasAssessableNodes, hasCertificates);
		menuTree.setTreeModel(tm);
		menuTree.setSelectedNodeId(tm.getRootNode().getIdent());
		menuTree.addListener(this);

		// Start on index page
		main.setContent(index);
		LayoutMain3ColsController columLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, main, "course" + course.getResourceableId());
		listenTo(columLayoutCtr); // cleanup on dispose
		putInitialPanel(columLayoutCtr.getInitialComponent());
		
		if(focusOnIdentity != null) {
			//fill the user list for the 
			this.mode = MODE_USERFOCUS;
			this.identitiesList = getAllAssessableIdentities();
			doUserChooseWithData(ureq, identitiesList, null, null);
			
			GenericTreeModel menuTreeModel = (GenericTreeModel)menuTree.getTreeModel();
			TreeNode userNode = menuTreeModel.findNodeByUserObject(CMD_USERFOCUS);
			if(userNode != null) {
				menuTree.setSelectedNode(userNode);
			}
			
			// select user
			assessedIdentityWrapper = AssessmentHelper.wrapIdentity(focusOnIdentity, localUserCourseEnvironmentCache, initialLaunchDates, course, null);
			
			identityAssessmentController = new IdentityAssessmentEditController(getWindowControl(), ureq, stackPanel,
					assessedIdentityWrapper.getIdentity(), course, true, true, false);
			listenTo(identityAssessmentController);
			setContent(identityAssessmentController.getInitialComponent());
		}
		
		// Register for assessment changed events
		course.getCourseEnvironment().getAssessmentManager().registerForAssessmentChangeEvents(this, ureq.getIdentity());
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), CertificatesManager.ORES_CERTIFICATE_EVENT);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == menuTree) {
			disposeChildControllerAndReleaseLocks(); // first cleanup old locks
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeNode selTreeNode = menuTree.getSelectedNode();
				String cmd = (String) selTreeNode.getUserObject();
				// reset helper variables
				this.currentCourseNode = null;
				this.currentGroup = null;
				this.isFiltering = true;
				if (cmd.equals(CMD_INDEX)) {
					main.setContent(index);
				} else if (cmd.equals(CMD_USERFOCUS)) {
					mode = MODE_USERFOCUS;
					identitiesList = getAllAssessableIdentities();
					//fxdiff FXOLAT-108: improve results table of tests
					doUserChooseWithData(ureq, identitiesList, null, null);
				} else if (cmd.equals(CMD_GROUPFOCUS)) {
					mode = MODE_GROUPFOCUS;
					doGroupChoose(ureq);
				} else if (cmd.equals(CMD_NODEFOCUS)) {
					mode = MODE_NODEFOCUS;
					doNodeChoose(ureq);
				} else if (cmd.equals(CMD_BULKFOCUS)){
					mode = MODE_BULKFOCUS;
					doBulkAssessment(ureq);
				} else if (cmd.equals(CMD_EFF_STATEMENT)){
					if(callback.mayRecalculateEfficiencyStatements()) {
						mode = MODE_EFF_STATEMENT;
						doEfficiencyStatement(ureq);
					}
				}
			}
		} else if (source == allUsersButton){	
			this.identitiesList = getAllAssessableIdentities();
			// Init the user list with this identitites list
			this.currentGroup = null;
			doUserChooseWithData(ureq, this.identitiesList, null, this.currentCourseNode);
		} else if (source == backLinkGC){
			setContent(nodeListCtr.getInitialComponent());
		} else if (source == backLinkUC){
			if((repoTutor && coachedGroups.isEmpty()) || (callback.mayAssessAllUsers() || callback.mayViewAllUsersAssessments())) {
				if(mode == MODE_GROUPFOCUS) {
					setContent(groupListCtr.getInitialComponent());
				} else {
					setContent(nodeListCtr.getInitialComponent());
				}
			} else {
				setContent(groupChoose);
			}
		} else if (source == showAllCourseNodesButton) {
			enableFilteringCourseNodes(false);
		}  else if (source == filterCourseNodesButton) {
			enableFilteringCourseNodes(true);

		} else if (source == backLinkOR) {
			if (onyxReporterBackLocation.equals("userChoose")) {
				setContent(userChoose);
			} else if (onyxReporterBackLocation.equals("nodeListCtr")) {
				setContent(nodeListCtr.getInitialComponent());
			}
		}
	}

	/**
	 * This methods calls the OnyxReporter and shows it in an iframe.
	 * 
	 * @param ureq The UserRequest for getting the identity and role of the current user.
	 */
	private boolean showOnyxReporter(final UserRequest ureq) {
		if (OnyxModule.isOnyxTest(currentCourseNode.getReferencedRepositoryEntry().getOlatResource())) {
			//<ONYX-705>
			OnyxReporterConnector onyxReporter = null;
			try {
				onyxReporter = new OnyxReporterConnector();
			} catch (OnyxReporterException e){
				log.error("unable to connect to onyxreporter!", e);
			}
			//</ONYX-705>
			if (onyxReporter != null) {
				if (identitiesList == null) {
					identitiesList = getAllAssessableIdentities();
				}
				String iframeSrc = "";
				try {
					//<ONYX-705>
					iframeSrc = onyxReporter.startReporterGUI(ureq.getIdentity(), identitiesList, currentCourseNode, null, ReporterRole.ASSESSMENT);
				} catch (OnyxReporterException orE) {
					// </ONYX-705>
					if (orE.getMessage().equals("noresults")) {
						onyxReporterVC.contextPut("iframeOK", Boolean.FALSE);
						onyxReporterVC.contextPut("showBack", Boolean.TRUE);
						onyxReporterVC.contextPut("message", translate("no.testresults"));
						setContent(onyxReporterVC);
						return true;
					}
					return false;
				}
				onyxReporterVC.contextPut("showBack", Boolean.TRUE);
				onyxReporterVC.contextPut("iframeOK", Boolean.TRUE);
				onyxReporterVC.contextPut("onyxReportLink", iframeSrc);
				setContent(onyxReporterVC);
				return true;
			} else {
				userChoose.contextPut("iframeOK", Boolean.FALSE);
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Enable/disable filtering of course-nodes in user-selection table and update new course-node-list. (Assessemnt-tool =>
	 * 
	 * @param enableFiltering
	 */
	private void enableFilteringCourseNodes(boolean enableFiltering) {
		ICourse course = CourseFactory.loadCourse(ores);
		this.isFiltering = enableFiltering;
		userChoose.contextPut("isFiltering", enableFiltering);
		nodeFilters = getNodeFilters(course.getRunStructure().getRootNode(), currentGroup);
		userListCtr.setFilters(nodeFilters, null);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == groupListCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals(CMD_CHOOSE_GROUP)) {
					int rowid = te.getRowId();
					GroupAndContextTableModel groupListModel = (GroupAndContextTableModel)groupListCtr.getTableDataModel();
					currentGroup = groupListModel.getObject(rowid);
					identitiesList = getGroupIdentitiesFromGroupmanagement(currentGroup);
					// Init the user list with this identitites list
					doUserChooseWithData(ureq, identitiesList, currentGroup, currentCourseNode);
				}
			}
		}
		else if (source == userListCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals(CMD_CHOOSE_USER)) {
					int rowid = te.getRowId();
					ICourse course = CourseFactory.loadCourse(ores);
					if (userListCtr.getTableDataModel() instanceof UserTableDataModel) {
						// in user MODE_USERFOCUS, a simple identity table is used, no wrapped identites
						UserTableDataModel userListModel = (UserTableDataModel) userListCtr.getTableDataModel();
						Identity assessedIdentity = userListModel.getIdentityAt(rowid);
						assessedIdentityWrapper = AssessmentHelper.wrapIdentity(assessedIdentity, 
								localUserCourseEnvironmentCache, initialLaunchDates, course, null);
					} else {
						// all other cases where user can be choosen the assessed identity wrapper is used
						AssessedIdentitiesTableDataModel userListModel = (AssessedIdentitiesTableDataModel) userListCtr.getTableDataModel();
						this.assessedIdentityWrapper = userListModel.getObject(rowid);
					}
					// init edit controller for this identity and this course node 
					// or use identity assessment overview if no course node is defined
					initIdentityEditController(ureq, course);
				}
			} else if (event.equals(TableController.EVENT_FILTER_SELECTED)) {
				ShortName filter = userListCtr.getActiveFilter();
				if(filter instanceof AssessableCourseNode) {
					currentCourseNode = (AssessableCourseNode)filter; 
				} else {
					currentCourseNode = null;
				}
				doUserChooseWithData(ureq, identitiesList, currentGroup, currentCourseNode);
			}
		}
		else if (source == nodeListCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals(CMD_SELECT_NODE)) {
					ICourse course = CourseFactory.loadCourse(ores);
					int rowid = te.getRowId();
					NodeTableRow nodeData = nodeTableModel.getObject(rowid);
					CourseNode node = course.getRunStructure().getNode(nodeData.getIdent());
					this.currentCourseNode = (AssessableCourseNode) node;
					// cast should be save, only assessable nodes are selectable
					if((repoTutor && coachedGroups.isEmpty()) || (callback.mayAssessAllUsers() || callback.mayViewAllUsersAssessments())) {
						identitiesList = getAllAssessableIdentities();
						doUserChooseWithData(ureq, identitiesList, null, currentCourseNode);
					} else {
						doGroupChoose(ureq);
					}
				} else if (actionid.equals(CMD_SHOW_ONYXREPORT)) {
					int rowid = te.getRowId();
					ICourse course = CourseFactory.loadCourse(ores);
					NodeTableRow nodeData = nodeTableModel.getObject(rowid);
					CourseNode node = course.getRunStructure().getNode(nodeData.getIdent());
					this.currentCourseNode = (AssessableCourseNode) node;
					this.onyxReporterBackLocation = "nodeListCtr";
					if (!showOnyxReporter(ureq)) {
						getWindowControl().setError(translate("onyxreporter.error"));
					}
				}
			} else if (event.equals(TableController.EVENT_FILTER_SELECTED)) {
				ShortName filter = nodeListCtr.getActiveFilter();
				if(filter instanceof AssessableCourseNode) {
					currentCourseNode = (AssessableCourseNode)filter;
				} else {
					currentCourseNode = null;
				}
				doUserChooseWithData(ureq, identitiesList, null, currentCourseNode);
			}
		}
		else if (source == assessmentEditController) {
			if (event.equals(Event.CHANGED_EVENT) || event.equals(Event.DONE_EVENT)) {
				// refresh identity in list model
				if (userListCtr != null 
						&& userListCtr.getTableDataModel() instanceof AssessedIdentitiesTableDataModel) {
					AssessedIdentitiesTableDataModel atdm = (AssessedIdentitiesTableDataModel) userListCtr.getTableDataModel();
					List<AssessedIdentityWrapper> aiwList = atdm.getObjects();
					if (aiwList.contains(this.assessedIdentityWrapper)) {
						ICourse course = CourseFactory.loadCourse(ores);
						aiwList.remove(this.assessedIdentityWrapper);
						assessedIdentityWrapper = AssessmentHelper.wrapIdentity(assessedIdentityWrapper.getIdentity(),
						localUserCourseEnvironmentCache, initialLaunchDates, course, currentCourseNode);
						aiwList.add(this.assessedIdentityWrapper);
						userListCtr.modelChanged();
					}
				}
			} // else nothing special to do
			setContent(userChoose);
		} else if (source == identityAssessmentController) {
			if (event.equals(Event.CANCELLED_EVENT)) {
				setContent(userChoose);
			}
		} else if(source == certificateWizardCtrl) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				if(userListCtr != null) {
					ConcurrentMap<Long, CertificateLight> certificates = getCertificates(CourseFactory.loadCourse(ores));
					((AssessedIdentitiesTableDataModel)userListCtr.getTableDataModel()).setCertificates(certificates);
					userListCtr.modelChanged();
				}
			}
		} else if(userChoose != null && userChoose.contains(source.getInitialComponent())) {
			if(userListCtr != null) {
				if(event == Event.CHANGED_EVENT) {
					userListCtr.modelChanged();
				}
			}
		}
	}

	@Override
	public void event(Event event) {
		if ((event instanceof AssessmentChangedEvent) &&  event.getCommand().equals(AssessmentChangedEvent.TYPE_SCORE_EVAL_CHANGED)) {
			AssessmentChangedEvent ace = (AssessmentChangedEvent) event;
			doUpdateLocalCacheAndUserModelFromAssessmentEvent(ace);
		} else if(event instanceof CertificateEvent) {
			CertificateEvent ce = (CertificateEvent)event;
			if(re.getOlatResource().getKey().equals(ce.getResourceKey())
					&& localUserCourseEnvironmentCache.containsKey(ce.getOwnerKey())) {
				updateCertificate(ce.getCertificateKey());
			}
		}
	}
	
	private void updateCertificate(Long certificateKey) {
		if (userListCtr != null 
				&& userListCtr.getTableDataModel() instanceof AssessedIdentitiesTableDataModel) {
			CertificateLight certificate = certificatesManager.getCertificateLightById(certificateKey);
			((AssessedIdentitiesTableDataModel)userListCtr
					.getTableDataModel()).putCertificate(certificate);
			userChoose.setDirty(true);
		}
	}
	
	/**
	 * Updates the local user course environment cache if the given event is for an identity
	 * cached in the local cache. Also updates the user list table model if the identity from
	 * the event is in the model.
	 * @param ace
	 */
	private void doUpdateLocalCacheAndUserModelFromAssessmentEvent(AssessmentChangedEvent ace) {
		String assessmentChangeType = ace.getCommand();
		// do not re-evaluate things if only comment has been changed
		if (assessmentChangeType.equals(AssessmentChangedEvent.TYPE_SCORE_EVAL_CHANGED)
				|| assessmentChangeType.equals(AssessmentChangedEvent.TYPE_ATTEMPTS_CHANGED)) {

			// Check if the identity in the event is in our local user course environment
			// cache. If so, update the look-uped users score accounting information. 
			//Identity identityFromEvent = ace.getIdentity();
			Long identityKeyFromEvent = ace.getIdentityKey();
			if (localUserCourseEnvironmentCache.containsKey(identityKeyFromEvent)) {
				UserCourseEnvironment uce = localUserCourseEnvironmentCache.get(identityKeyFromEvent);					
				// 1) update score accounting
				if (uce != null) {
					uce.getScoreAccounting().evaluateAll();
				}
				// 2) update user table model
				if (userListCtr != null 
						&& userListCtr.getTableDataModel() instanceof AssessedIdentitiesTableDataModel) {
					// 2.1) search wrapper object in model
					AssessedIdentitiesTableDataModel aitd = (AssessedIdentitiesTableDataModel) userListCtr.getTableDataModel();
					List<AssessedIdentityWrapper> wrappers = aitd.getObjects();
					Iterator<AssessedIdentityWrapper> iter = wrappers.iterator();
					AssessedIdentityWrapper wrappedIdFromModel = null;
					while (iter.hasNext()) {
						AssessedIdentityWrapper wrappedId =  iter.next();
						if (wrappedId.getIdentity().getKey().equals(identityKeyFromEvent)) {
							wrappedIdFromModel = wrappedId;
						}
					}
					// 2.2) update wrapper object
					if (wrappedIdFromModel != null) {
						wrappers.remove(wrappedIdFromModel);
						
						Date initialLaunchDate;
						if(initialLaunchDates.containsKey(identityKeyFromEvent)) {
							initialLaunchDate = initialLaunchDates.get(identityKeyFromEvent);
						} else {
							UserCourseInformationsManager userCourseInformationsManager = CoreSpringFactory.getImpl(UserCourseInformationsManager.class);
							initialLaunchDate = userCourseInformationsManager.getInitialLaunchDate(ores.getResourceableId(),  wrappedIdFromModel.getIdentity());
						}
						wrappedIdFromModel = AssessmentHelper.wrapIdentity(wrappedIdFromModel.getUserCourseEnvironment(), initialLaunchDate, currentCourseNode);
						wrappers.add(wrappedIdFromModel);
						userListCtr.modelChanged();								
					}
				}
			}
			// else user not in our local cache -> nothing to do
		}
	}

	/**
	 * @param selectedGroup
	 * @return List of participant identities from this group
	 */
	private List<Identity> getGroupIdentitiesFromGroupmanagement(BusinessGroup selectedGroup) {
		return  businessGroupService.getMembers(selectedGroup, GroupRoles.participant.name()); 
	}

	/**
	 * Load the identities which are participants of a group attached to the course,
	 * participants of the course as members and all users which have make the tests.
	 * @return List of identities
	 */
	private List<Identity> getAllAssessableIdentities() {
		Set<Identity> duplicateKiller = new HashSet<>();
		List<Identity> assessableIdentities = new ArrayList<>();
		
		List<Identity> participants = businessGroupService.getMembers(coachedGroups, GroupRoles.participant.name());
		for(Identity participant:participants) {
			if(!duplicateKiller.contains(participant)) {
				assessableIdentities.add(participant);
				duplicateKiller.add(participant);
			}
		}
		
		if((repoTutor && coachedGroups.isEmpty()) || (callback.mayAssessAllUsers() || callback.mayViewAllUsersAssessments())) {
			List<Identity> courseParticipants = repositoryService.getMembers(re, GroupRoles.participant.name());
			for(Identity participant:courseParticipants) {
				if(!duplicateKiller.contains(participant)) {
					assessableIdentities.add(participant);
					duplicateKiller.add(participant);
				}
			}
		}

		if(callback.mayViewAllUsersAssessments() && participants.size() < 500) {
			mayViewAllUsersAssessments = true;
			ICourse course = CourseFactory.loadCourse(ores);
			CoursePropertyManager pm = course.getCourseEnvironment().getCoursePropertyManager();
			List<Identity> assessedUsers = pm.getAllIdentitiesWithCourseAssessmentData(participants);
			for(Identity assessedUser:assessedUsers) {
				if(!duplicateKiller.contains(assessedUser)) {
					assessableIdentities.add(assessedUser);
					duplicateKiller.add(assessedUser);
				}
			}
		}
		
		return assessableIdentities;
	}
	
	private void fillAlternativeToAssessableIdentityList(AssessmentToolOptions options) {
		List<Group> baseGroups = new ArrayList<>();
		if((repoTutor && coachedGroups.isEmpty()) || (callback.mayAssessAllUsers() || callback.mayViewAllUsersAssessments())) {
			baseGroups.add(repositoryService.getDefaultGroup(re));
		}
		if(coachedGroups.size() > 0) {
			for(BusinessGroup coachedGroup:coachedGroups) {
				baseGroups.add(coachedGroup.getBaseGroup());
			}
		}
		options.setAlternativeToIdentities(baseGroups, mayViewAllUsersAssessments);
	}
	
	/**
	 * @param identity
	 * @return List of all course groups if identity is course admin, else groups that 
	 * are coached by this identity
	 */
	private List<BusinessGroup> getAllowedGroupsFromGroupmanagement(Identity identity) {
		ICourse course = CourseFactory.loadCourse(ores);
		CourseGroupManager gm = course.getCourseEnvironment().getCourseGroupManager();
		if (callback.mayAssessAllUsers() || callback.mayViewAllUsersAssessments()) {
			return gm.getAllBusinessGroups();
		} else if (callback.mayAssessCoachedUsers()) {
			return gm.getOwnedBusinessGroups(identity);
		} else {
			throw new OLATSecurityException("No rights to assess or even view any groups");
		}
	}
	
	/**
	 * Init edit controller for this identity and this course node or use identity assessment
	 * overview if no course node is defined. (Rely on the instance variable currentCourseNode
	 * and assessedIdentityWrapper)
	 * @param ureq
	 * @param course
	 */
	private void initIdentityEditController(UserRequest ureq, ICourse course) {
		if (currentCourseNode == null) {
			removeAsListenerAndDispose(identityAssessmentController);
			identityAssessmentController = new IdentityAssessmentEditController(getWindowControl(),ureq, stackPanel,
					assessedIdentityWrapper.getIdentity(), course, true, true, false);
			listenTo(identityAssessmentController);
			setContent(identityAssessmentController.getInitialComponent());
		} else {
			removeAsListenerAndDispose(assessmentEditController);
			assessmentEditController = new AssessmentEditController(ureq, getWindowControl(), stackPanel, course, currentCourseNode,
					assessedIdentityWrapper, true, false, true);
			listenTo(assessmentEditController);
			main.setContent(assessmentEditController.getInitialComponent());
		}
	}

	/**
	 * Initialize the group list table according to the users access rights
	 * @param ureq The user request
	 */
	private void doGroupChoose(UserRequest ureq) {
		removeAsListenerAndDispose(groupListCtr);
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("groupchoose.nogroups"));
		tableConfig.setPreferencesOffered(true, "assessmentGroupList");
		groupListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(groupListCtr);
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.group.name", 0, CMD_CHOOSE_GROUP, ureq.getLocale()));
		
		
		DefaultColumnDescriptor desc = new DefaultColumnDescriptor("table.group.desc", 1, null, ureq.getLocale());
		desc.setEscapeHtml(EscapeMode.antisamy);
		
		
		
		
		
		groupListCtr.addColumnDescriptor(desc);

		// loop over all groups to filter depending on condition
		List<BusinessGroup> currentGroups = new ArrayList<BusinessGroup>();
		for (BusinessGroup group:coachedGroups) {
			if ( !isFiltering || isVisibleAndAccessable(currentCourseNode, group) ) {
				currentGroups.add(group);
			}
		}
		GroupAndContextTableModel groupTableDataModel = new GroupAndContextTableModel(currentGroups);
		groupListCtr.setTableDataModel(groupTableDataModel);
		groupChoose.put("grouplisttable", groupListCtr.getInitialComponent());
		
		// render all-groups button only if goups are available
		if (coachedGroups.size() > 0) {
			groupChoose.contextPut("hasGroups", Boolean.TRUE);
		} else {
			groupChoose.contextPut("hasGroups", Boolean.FALSE);
		}

		if (mode == MODE_NODEFOCUS) {
			groupChoose.contextPut("showBack", Boolean.TRUE);
		} else {
			groupChoose.contextPut("showBack", Boolean.FALSE);
		}
		
		// set main content to groupchoose
		setContent(groupChoose);
	}

	private void doUserChooseWithData(UserRequest ureq, List<Identity> identities, BusinessGroup group, AssessableCourseNode courseNode) {
		ICourse course = CourseFactory.loadCourse(ores);
		if (mode == MODE_GROUPFOCUS || mode == MODE_USERFOCUS) {
			nodeFilters = getNodeFilters(course.getRunStructure().getRootNode(), group);
		}		
		// Init table headers
		removeAsListenerAndDispose(userListCtr);
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("userchoose.nousers"));
		if(mode == MODE_USERFOCUS) {
			tableConfig.setPreferencesOffered(true, "assessmentSimpleUserList");
		} else if(mode == MODE_GROUPFOCUS){
			tableConfig.setPreferencesOffered(true, "assessmentGroupUsersNode");
		} else if (mode == MODE_NODEFOCUS) {
			tableConfig.setPreferencesOffered(true, "assessmentUserNodeList");
		}
		
		if (mode == MODE_GROUPFOCUS || mode == MODE_USERFOCUS) {
			userListCtr = new TableController(tableConfig, ureq, getWindowControl(), 
					this.nodeFilters, courseNode, 
					translate("nodesoverview.filter.title"), null,propertyHandlerTranslator);
		} else {
			userListCtr = new TableController(tableConfig, ureq, getWindowControl(), propertyHandlerTranslator);
		}
		listenTo(userListCtr);
    		
		// Wrap identities with user course environment and user score view
		List<AssessedIdentityWrapper> wrappedIdentities = new ArrayList<AssessedIdentityWrapper>();
		for (int i = 0; i < identities.size(); i++) {
			Identity identity = identities.get(i);
			// if course node is null the wrapper will only contain the identity and no score information
			AssessedIdentityWrapper aiw = AssessmentHelper.wrapIdentity(identity,
					localUserCourseEnvironmentCache, initialLaunchDates, course, courseNode);
			wrappedIdentities.add(aiw);
		}
		
		if(userListCtr == null) {
			//takes too long -> controller disposed
			return;
		}
		
		ConcurrentMap<Long, CertificateLight> certificates;
		CourseConfig courseConfig = course.getCourseConfig();
		boolean showCertificate =  mode == MODE_USERFOCUS && (courseConfig.isAutomaticCertificationEnabled() || courseConfig.isManualCertificationEnabled());
		if(showCertificate) {
			certificates = getCertificates(course);
		} else {
			certificates = new ConcurrentHashMap<>();
		}
		
		// Add the wrapped identities to the table data model
		AssessedIdentitiesTableDataModel tdm = new AssessedIdentitiesTableDataModel(wrappedIdentities, certificates,  courseNode, getLocale(), isAdministrativeUser);
		tdm.addColumnDescriptors(userListCtr, CMD_CHOOSE_USER, mode == MODE_NODEFOCUS || mode == MODE_GROUPFOCUS || mode == MODE_USERFOCUS, showCertificate);
		userListCtr.setTableDataModel(tdm);
		
		int count = 0;
		List<String> toolCmpNames = new ArrayList<>(3);
		if(courseNode != null) {
			CourseEnvironment courseEnv = course.getCourseEnvironment();
			AssessmentToolOptions options = new AssessmentToolOptions();
			if(group == null) {
				options.setIdentities(identities);
				fillAlternativeToAssessableIdentityList(options);
			} else {
				options.setGroup(group);
			}
			List<Controller> tools = courseNode.createAssessmentTools(ureq, getWindowControl(), stackPanel, courseEnv, options);
			
			for(Controller tool:tools) {
				listenTo(tool);
				String toolCmpName = "ctrl_" + (count++);
				userChoose.put(toolCmpName, tool.getInitialComponent());
				toolCmpNames.add(toolCmpName);
				if(tool instanceof BreadcrumbPanelAware) {
					((BreadcrumbPanelAware)tool).setBreadcrumbPanel(stackPanel);
				}
			}
		}
		if(courseConfig.isManualCertificationEnabled()) {
			if(courseNode == null || courseNode == course.getRunStructure().getRootNode()) {
				removeAsListenerAndDispose(certificateWizardCtrl);
				certificateWizardCtrl = new CertificatesWizardController(ureq, getWindowControl(), tdm, ores, hasAssessableNodes);
				listenTo(certificateWizardCtrl);

				String toolCmpName = "ctrl_" + (count++);
				userChoose.put(toolCmpName, certificateWizardCtrl.getInitialComponent());
				toolCmpNames.add(toolCmpName);
			}
		}
		userChoose.contextPut("toolCmpNames", toolCmpNames);

		if (mode == MODE_USERFOCUS) {
			userChoose.contextPut("showBack", Boolean.FALSE);
		} else {
			userChoose.contextPut("showBack", Boolean.TRUE);

			if (currentCourseNode != null && currentCourseNode.getReferencedRepositoryEntry() != null
					&& currentCourseNode.getReferencedRepositoryEntry().getOlatResource() != null
					&& OnyxModule.isOnyxTest(currentCourseNode.getReferencedRepositoryEntry().getOlatResource())) {
				userChoose.contextPut("showOnyxReporterButton", Boolean.TRUE);
			} else {
				userChoose.contextPut("showOnyxReporterButton", Boolean.FALSE);
			}

			if (mode == MODE_NODEFOCUS) {
				userChoose.contextPut("showFilterButton", Boolean.FALSE);
			} else {
				userChoose.contextPut("showFilterButton", Boolean.TRUE);
			}
		}
		
		
		if (group == null) {
			userChoose.contextPut("showGroup", Boolean.FALSE);			
		} else {
			userChoose.contextPut("showGroup", Boolean.TRUE);			
			userChoose.contextPut("groupName", StringEscapeUtils.escapeHtml(group.getName()));			
		}
		
		userChoose.put("userlisttable", userListCtr.getInitialComponent());
		// set main vc to userchoose
		setContent(userChoose);
	}
	
	private ConcurrentMap<Long, CertificateLight> getCertificates(ICourse course) {
		ConcurrentMap<Long, CertificateLight> certificates =  new ConcurrentHashMap<>();
		OLATResource resource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
		List<CertificateLight> certificateList = certificatesManager.getLastCertificates(resource);
		for(CertificateLight certificate:certificateList) {
			CertificateLight currentCertificate = certificates.get(certificate.getIdentityKey());
			if(currentCertificate == null || currentCertificate.getCreationDate().before(certificate.getCreationDate())) {
				certificates.put(certificate.getIdentityKey(), certificate);
			}
		}
		return certificates;
	}

	private void doNodeChoose(UserRequest ureq) {
		ICourse course = CourseFactory.loadCourse(ores);
		removeAsListenerAndDispose(nodeListCtr);
		// table configuraton
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("nodesoverview.nonodes"));
		tableConfig.setDownloadOffered(false);
		tableConfig.setSortingEnabled(true);
		tableConfig.setDisplayTableHeader(true);
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		tableConfig.setPreferencesOffered(true, "assessmentNodeList");
		
		nodeListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(nodeListCtr);
		
		final IndentedNodeRenderer nodeRenderer = new IndentedNodeRenderer() {
			@Override
			public boolean isIndentationEnabled() {
				return nodeListCtr.getTableSortAsc() && nodeListCtr.getTableSortCol() == 0;
			}
		};
		
		// table columns		
		nodeListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.node", Cols.data.ordinal(),
				null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, nodeRenderer){
			@Override
			public int compareTo(int rowa, int rowb) {
				//the order is already ok
				return rowa - rowb;
			}
		});
		nodeListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.action.select", Cols.select.ordinal(), CMD_SELECT_NODE, getLocale()));
		nodeListCtr.addColumnDescriptor(false, new CustomRenderColumnDescriptor("table.header.min", Cols.min.ordinal(), null, getLocale(),
				ColumnDescriptor.ALIGNMENT_RIGHT, new ScoreCellRenderer()));
		nodeListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.max", Cols.max.ordinal(), null, getLocale(),
				ColumnDescriptor.ALIGNMENT_RIGHT, new ScoreCellRenderer()));
		if(onyxModule.isEnabled()) {
			nodeListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.overallselect", Cols.onyxReport.ordinal(),
					CMD_SHOW_ONYXREPORT, ureq.getLocale()));
		}
		
		
		// get list of course node data and populate table data model 
		CourseNode rootNode = course.getRunStructure().getRootNode();		
		List<NodeTableRow> nodesTableObjectArrayList = addAssessableNodesAndParentsToList(0, rootNode);
		
		// only populate data model if data available
		if (nodesTableObjectArrayList == null) {
			String text = translate("nodesoverview.nonodes");
			Controller messageCtr = MessageUIFactory.createSimpleMessage(ureq, getWindowControl(), text);
			listenTo(messageCtr);//dispose if this one gets disposed
			nodeChoose.put("nodeTable", messageCtr.getInitialComponent());
		} else {
			nodeTableModel = new NodeTableDataModel(nodesTableObjectArrayList, getTranslator());
			nodeListCtr.setTableDataModel(nodeTableModel);
			nodeChoose.put("nodeTable", nodeListCtr.getInitialComponent());
		}
		
		// set main content to nodechoose, do not use wrapper
		main.setContent(nodeChoose);
	}
	
	private void doBulkAssessment(UserRequest ureq) {
		bulkAssOverviewCtrl = new BulkAssessmentOverviewController(ureq, getWindowControl(), ores);
		listenTo(bulkAssOverviewCtrl);
		main.setContent(bulkAssOverviewCtrl.getInitialComponent());
	}
	
	private void doEfficiencyStatement(UserRequest ureq) {
		removeAsListenerAndDispose(esac);
		esac = new EfficiencyStatementAssessmentController(ureq, getWindowControl(), ores);
		listenTo(esac);
		main.setContent(esac.getInitialComponent());
	}

	/**
	 * Recursive method that adds assessable nodes and all its parents to a list
	 * @param recursionLevel
	 * @param courseNode
	 * @return A list of maps containing the node data
	 */
	private List<NodeTableRow> addAssessableNodesAndParentsToList(int recursionLevel, CourseNode courseNode) {
		// 1) Get list of children data using recursion of this method
		List<NodeTableRow> childrenData = new ArrayList<>();
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			CourseNode child = (CourseNode) courseNode.getChildAt(i);
			List<NodeTableRow> childData = addAssessableNodesAndParentsToList( (recursionLevel + 1),  child);
			if (childData != null)
				childrenData.addAll(childData);
		}
		
		boolean hasDisplayableValuesConfigured = false;
		if ( (childrenData.size() > 0 || courseNode instanceof AssessableCourseNode) && !(courseNode instanceof ProjectBrokerCourseNode) ) {
			// TODO:cg 04.11.2010 ProjectBroker : no assessment-tool in V1.0 , remove projectbroker completely form assessment-tool gui			// Store node data in hash map. This hash map serves as data model for 
			// the user assessment overview table. Leave user data empty since not used in
			// this table. (use only node data)
			NodeTableRow nodeData = new NodeTableRow(recursionLevel, courseNode);
			if (courseNode.getReferencedRepositoryEntry() != null) {
				if (OnyxModule.isOnyxTest(courseNode.getReferencedRepositoryEntry().getOlatResource())) {
					nodeData.setOnyx(true);
					if (getAllAssessableIdentities().size() <= 0) {
						nodeData.setOnyx(false);
					}
				} else {
					nodeData.setOnyx(false);
				}
			}

			
			if (courseNode instanceof AssessableCourseNode) {
				AssessableCourseNode assessableCourseNode = (AssessableCourseNode) courseNode;
				if ( assessableCourseNode.hasDetails()
					|| assessableCourseNode.hasAttemptsConfigured()
					|| assessableCourseNode.hasScoreConfigured()
					|| assessableCourseNode.hasPassedConfigured()
					|| assessableCourseNode.hasCommentConfigured()) {
					hasDisplayableValuesConfigured = true;
				}
				
				//fxdiff VCRP-4: assessment overview with max score
				if(assessableCourseNode.hasScoreConfigured()) {
					if(!(courseNode instanceof STCourseNode)) {
						Float min = assessableCourseNode.getMinScoreConfiguration();
						nodeData.setMinScore(min);
						Float max = assessableCourseNode.getMaxScoreConfiguration();
						nodeData.setMaxScore(max);
					}
				}

				if (assessableCourseNode.isEditableConfigured()) {
					// Assessable course nodes are selectable when they are aditable
					nodeData.setSelectable(true);
				} else if (courseNode instanceof STCourseNode 
						&& (assessableCourseNode.hasScoreConfigured()
						|| assessableCourseNode.hasPassedConfigured())) {
					// st node is special case: selectable on node choose list as soon as it 
					// has score or passed
					nodeData.setSelectable(true);
				} else {
					// assessable nodes that do not have score or passed are not selectable
					// (e.g. a st node with no defined rule
					nodeData.setSelectable(false);
				}
			} else {
				// Not assessable nodes are not selectable. (e.g. a node that 
				// has an assessable child node but is itself not assessable)
				nodeData.setSelectable(false);
			}
			// 3) Add data of this node to mast list if node assessable or children list has any data.
			// Do only add nodes when they have any assessable element, otherwhise discard (e.g. empty course, 
			// structure nodes without scoring rules)! When the discardEmptyNodes flag is set then only
			// add this node when there is user data found for this node.
			if (childrenData.size() > 0 || hasDisplayableValuesConfigured) {
				List<NodeTableRow> nodeAndChildren = new ArrayList<>();
				nodeAndChildren.add(nodeData);
				// 4) Add children data list to master list
				nodeAndChildren.addAll(childrenData);
				return nodeAndChildren;
			}
		}
		return null;
	}

	/**
	 * Recursive method to add all assessable course nodes to a list
	 * @param courseNode
	 * @return List of course Nodes
	 */
	private List<ShortName> getNodeFilters(CourseNode courseNode, BusinessGroup group) {
		List<ShortName> filters = new ArrayList<ShortName>();
		filters.add(new FilterName(translate("nodesoverview.filter.showEmptyNodes")));
		filters.addAll(addAssessableNodesToList(courseNode, group));
		return filters;
	}

	private List<ShortName> addAssessableNodesToList(CourseNode courseNode, BusinessGroup group) {
		List<ShortName> result = new ArrayList<ShortName>();
		if (courseNode instanceof AssessableCourseNode && !(courseNode instanceof ProjectBrokerCourseNode)) {
			// TODO:cg 04.11.2010 ProjectBroker : no assessment-tool in V1.0 , remove projectbroker completely form assessment-tool gui			AssessableCourseNode assessableCourseNode = (AssessableCourseNode) courseNode;
			AssessableCourseNode assessableCourseNode = (AssessableCourseNode) courseNode;
			if ( assessableCourseNode.hasDetails()
				|| assessableCourseNode.hasAttemptsConfigured()
				|| assessableCourseNode.hasScoreConfigured()
				|| assessableCourseNode.hasPassedConfigured()
				|| assessableCourseNode.hasCommentConfigured()) {
				if ( !isFiltering || isVisibleAndAccessable(assessableCourseNode,group) ) {
					result.add(assessableCourseNode);
				}
			}
		}
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			CourseNode child = (CourseNode) courseNode.getChildAt(i);
			result.addAll(addAssessableNodesToList(child, group));
		}
		return result;
	}
	
	/**
	 * Check if a course node is visiable and accessibale for certain group.
	 * Because the condition-interpreter works with identities, take the frist 
	 * identity from list of participants. 
	 * 
	 * @param courseNode
	 * @param group
	 * @return
	 */
	private boolean isVisibleAndAccessable(CourseNode courseNode, BusinessGroup group) {
		if ( (courseNode == null) || ( group == null ) ) {
			return true;
		}
		List<Identity> identities = businessGroupService.getMembers(group, GroupRoles.participant.name());		
		if (identities.isEmpty()) {
			// group has no participant, can not evalute  
			return false;
		}
		ICourse course = CourseFactory.loadCourse(ores);
		// check if course node is visible for group
		// get first identity to use this identity for condition interpreter
		Identity identity = identities.get(0);
		IdentityEnvironment identityEnvironment = new IdentityEnvironment();
		identityEnvironment.setIdentity(identity);
		UserCourseEnvironment uce = new UserCourseEnvironmentImpl(identityEnvironment, course.getCourseEnvironment());
		OnlyGroupConditionInterpreter ci = new OnlyGroupConditionInterpreter(uce);
		List<ConditionExpression> listOfConditionExpressions = courseNode.getConditionExpressions();
		boolean allConditionAreValid = true;
		// loop over all conditions, all must be true
		for (Iterator<ConditionExpression> iter = listOfConditionExpressions.iterator(); iter.hasNext();) {
			ConditionExpression conditionExpression = iter.next();
			logDebug("conditionExpression=" + conditionExpression, null);
			logDebug("conditionExpression.getId()=" + conditionExpression.getId(), null);
			Condition condition = new Condition();
			condition.setConditionId(conditionExpression.getId());
			condition.setConditionExpression(conditionExpression.getExptressionString());
			if ( !ci.evaluateCondition(condition) ) {
				allConditionAreValid = false;
			}
		}
		return allConditionAreValid;
	}
	
	/**
	 * @param content Content to put in wrapper and set to main
	 */
	private void setContent(Component content) {
		if (this.currentCourseNode == null) {
			wrapper.contextRemove("courseNode");
		} else {
			wrapper.contextPut("courseNode", this.currentCourseNode);
			// push node css class
			wrapper.contextPut("courseNodeCss", CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(currentCourseNode.getType()).getIconCSSClass());

		}
		wrapper.put("content", content);
		main.setContent(wrapper);
	}
	
	/**
	 * @param assessableNodes true: show menu, false: hide menu
	 * @return The tree model
	 */
	private TreeModel buildTreeModel(boolean assessableNodes, boolean certificate) {
		GenericTreeNode root, gtn;

		GenericTreeModel gtm = new GenericTreeModel();
		root = new GenericTreeNode();
		root.setTitle(translate("menu.index"));
		root.setUserObject(CMD_INDEX);
		root.setAltText(translate("menu.index.alt"));
		gtm.setRootNode(root);

		// show real menu only when there are some assessable nodes
		if (assessableNodes) {
			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.groupfocus"));
			gtn.setUserObject(CMD_GROUPFOCUS);
			gtn.setAltText(translate("menu.groupfocus.alt"));
			gtn.setCssClass("o_sel_assessment_tool_groups");
			root.addChild(gtn);
	
			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.nodefocus"));
			gtn.setUserObject(CMD_NODEFOCUS);
			gtn.setAltText(translate("menu.nodefocus.alt"));
			gtn.setCssClass("o_sel_assessment_tool_nodes");
			root.addChild(gtn);
		}
		
		if (assessableNodes || certificate) {
			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.userfocus"));
			gtn.setUserObject(CMD_USERFOCUS);
			gtn.setAltText(translate("menu.userfocus.alt"));
			gtn.setCssClass("o_sel_assessment_tool_users");
			root.addChild(gtn);
		}

		if (assessableNodes) {
			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.bulkfocus"));
			gtn.setUserObject(CMD_BULKFOCUS);
			gtn.setAltText(translate("menu.bulkfocus.alt"));
			root.addChild(gtn);
			
			if(callback.mayRecalculateEfficiencyStatements()) {
				gtn = new GenericTreeNode();
				gtn.setTitle(translate("menu.efficiency.statment"));
				gtn.setUserObject(CMD_EFF_STATEMENT);
				gtn.setAltText(translate("menu.efficiency.statment.alt"));
				root.addChild(gtn);
			}
		}
		
		return gtm;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		// controllers disposed by BasicController
		userListCtr = null;
		nodeListCtr = null;
		groupListCtr = null;
		csc = null;
		assessmentEditController = null;
		identityAssessmentController = null;
		
		// deregister from assessment changed events
		ICourse course = CourseFactory.loadCourse(ores);
		course.getCourseEnvironment().getAssessmentManager().deregisterFromAssessmentChangeEvents(this);

		// stop assessment cache preloader thread if still running
		if (assessmentCachPreloaderThread != null && assessmentCachPreloaderThread.isAlive()) {
			assessmentCachPreloaderThread.interrupt();
			if (log.isDebug()) {
				log.debug("Interrupting assessment cache preload in course::" + course.getResourceableId() + " while in doDispose()");
			}
		}
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, CertificatesManager.ORES_CERTIFICATE_EVENT);
	}
	
	/**
	 * Release resources used by child controllers. This must be called
	 * to release locks produced by certain child controllers and to help the
	 * garbage collector.
	 */
	private void disposeChildControllerAndReleaseLocks() {
		removeAsListenerAndDispose(assessmentEditController);
		assessmentEditController = null;
		removeAsListenerAndDispose(identityAssessmentController);
		identityAssessmentController = null;
		}		

	/**
	 * Description:<BR>
	 * Thread that preloads the assessment cache and the user environment cache
	 * <P>
	 * Initial Date:  Mar 2, 2005
	 *
	 * @author gnaegi
	 */
	private class AssessmentCachePreloadThread extends Thread {
		/**
		 * @param name Thread name
		 */
		AssessmentCachePreloadThread(String name) {
			super(name);
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			boolean success = false;
			try{
				ICourse course = CourseFactory.loadCourse(ores);
				// 1) preload assessment cache with database properties
				long start = 0;
				boolean logDebug = log.isDebug();
				if(logDebug) start = System.currentTimeMillis();
				List<Identity> identities = getAllAssessableIdentities();
				course.getCourseEnvironment().getAssessmentManager().preloadCache(identities);
	
				UserCourseInformationsManager mgr = CoreSpringFactory.getImpl(UserCourseInformationsManager.class);
				initialLaunchDates.putAll(mgr.getInitialLaunchDates(course.getResourceableId(), identities));
				
				for (Identity identity : identities) {
					AssessmentHelper.wrapIdentity(identity, localUserCourseEnvironmentCache, initialLaunchDates, course, null);
					if (Thread.interrupted()) break;
				}
				if (logDebug) {
					log.debug("Preloading of user course environment cache for course::" + course.getResourceableId() + " for "
							+ localUserCourseEnvironmentCache.size() + " user course environments. Loading time::" + (System.currentTimeMillis() - start)
							+ "ms");
				}
				// finished in this thread, close database session of this thread!
				DBFactory.getInstance(false).commitAndCloseSession();
				success = true;
			} finally {
				if (!success) {
					DBFactory.getInstance(false).rollbackAndCloseSession();
				}
			}
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		ContextEntry firstEntry = entries.get(0);
		String type = firstEntry.getOLATResourceable().getResourceableTypeName();
		Long resId = firstEntry.getOLATResourceable().getResourceableId();
		if("node-choose".equals(type)) {
			doNodeChoose(ureq);
		} else if("Identity".equals(type)) {
			TreeNode userNode = TreeHelper.findNodeByUserObject(CMD_USERFOCUS, menuTree.getTreeModel().getRootNode());
			if(userNode != null) {

				ICourse course = CourseFactory.loadCourse(ores);
				if(entries.size() > 1) {
					ContextEntry secondEntry = entries.get(1);
					String secondType = secondEntry.getOLATResourceable().getResourceableTypeName();
					Long secondResId = secondEntry.getOLATResourceable().getResourceableId();
					if("CourseNode".equals(secondType)) {
						CourseNode node = course.getRunStructure().getNode(secondResId.toString());
						currentCourseNode = (AssessableCourseNode) node;
					}
				} else {
					currentCourseNode = null;
				}
				
				mode = MODE_USERFOCUS;
				identitiesList = getAllAssessableIdentities();
				doUserChooseWithData(ureq, identitiesList, null, currentCourseNode);
				menuTree.setSelectedNode(userNode);

				assessedIdentityWrapper = null;	
				TableDataModel userListModel = userListCtr.getTableDataModel();
				for(int i=userListModel.getRowCount(); i-->0; ) {
					Object id = userListModel.getObject(i);
					if(id instanceof AssessedIdentityWrapper && ((AssessedIdentityWrapper)id).getIdentity().getKey().equals(resId)) {
						assessedIdentityWrapper = (AssessedIdentityWrapper)id;
						break;
					}
				}
				
				if(assessedIdentityWrapper != null) {
					initIdentityEditController(ureq, course);
				}
			}
		}
	}
}
