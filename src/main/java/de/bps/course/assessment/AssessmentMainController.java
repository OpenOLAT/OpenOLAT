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
* <p>
*/
package de.bps.course.assessment;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.admin.securitygroup.gui.UserControllerFactory;
import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
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
import org.olat.core.gui.control.generic.dtabs.Activateable;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.util.Util;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessedIdentitiesTableDataModel;
import org.olat.course.assessment.AssessedIdentityWrapper;
import org.olat.course.assessment.AssessmentChangedEvent;
import org.olat.course.assessment.AssessmentEditController;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentUIFactory;
import org.olat.course.assessment.BulkAssessmentMainController;
import org.olat.course.assessment.GroupAndContextTableModel;
import org.olat.course.assessment.IAssessmentCallback;
import org.olat.course.assessment.IdentityAssessmentEditController;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.condition.Condition;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.condition.interpreter.OnlyGroupConditionInterpreter;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.ui.context.BGContextTableModel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserManager;

import de.bps.onyx.plugin.OnyxModule;
import de.bps.webservices.clients.onyxreporter.OnyxReporterConnector;
import de.bps.webservices.clients.onyxreporter.OnyxReporterException;

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
public class AssessmentMainController extends MainLayoutBasicController implements Activateable, GenericEventListener {
	OLog log = Tracing.createLoggerFor(AssessmentMainController.class);

	private static final String CMD_INDEX 			= "cmd.index";
	private static final String CMD_USERFOCUS 	= "cmd.userfocus";
	private static final String CMD_GROUPFOCUS 	= "cmd.groupfocus";
	private static final String CMD_NODEFOCUS 	= "cmd.nodefocus";
	private static final String CMD_BULKFOCUS 	= "cmd.bulkfocus";

	private static final String CMD_CHOOSE_GROUP = "cmd.choose.group";
	private static final String CMD_CHOOSE_USER = "cmd.choose.user";
	private static final String CMD_SELECT_NODE = "cmd.select.node";
	
	private static final String CMD_SHOW_ONYXREPORT = "cmd.show.onyxreport";
	public static final String KEY_IS_ONYX = "isOnyx";
	//<OLATCE-1124>
	private static final String CMD_SHOW_ONYXREPORT_SURVEY = "cmd.show.onyxreport_survey";
	protected static final String KEY_IS_ONYX_SURVEY = "isOnyxSurvey";
	//</OLATCE-1124>
	

	private static final int MODE_USERFOCUS		= 0;
	private static final int MODE_GROUPFOCUS	= 1;
	private static final int MODE_NODEFOCUS		= 2;
	private static final int MODE_BULKFOCUS		= 3;
	private int mode;

	private IAssessmentCallback callback;
	private MenuTree menuTree;
	private Panel main;

	private ToolController toolC;
	private VelocityContainer index, groupChoose, userChoose, nodeChoose, wrapper;
	
	private VelocityContainer onyxReporterVC;
	
	private NodeTableDataModelOnyx nodeTableModel;
	private TableController groupListCtr, userListCtr, nodeListCtr;
	private List nodeFilters;
	private List<Identity> identitiesList;

	// Course assessment notification support fields
	private Controller csc;

	// Hash map to keep references to already created user course environments
	// Serves as a local cache to reduce database access - not shared by multiple threads
	final Map<Long, UserCourseEnvironment> localUserCourseEnvironmentCache; // package visibility for avoiding synthetic accessor method
	final Map<Long, Date> initialLaunchDates;
	// List of groups to which the user has access rights in this course
	private List<BusinessGroup> coachedGroups;

	// some state variables
	private AssessableCourseNode currentCourseNode;
	private AssessedIdentityWrapper assessedIdentityWrapper;
	private AssessmentEditController assessmentEditController;
	private IdentityAssessmentEditController identityAssessmentController;
	private BusinessGroup currentGroup;

	private Thread assessmentCachPreloaderThread;
	private Link backLinkUC;
	private Link backLinkGC;
	
	//back button for the Onyx Reporter
	private Link backLinkOR;
	//backbutton needs information where it should go back
	private String onyxReporterBackLocation;
	
	private Link allUsersButton;

	private boolean isAdministrativeUser;
	private Translator propertyHandlerTranslator;

	private boolean isFiltering = true;
	private Link showAllCourseNodesButton;
	private Link filterCourseNodesButton;

	private BulkAssessmentMainController bamc;

	private OLATResourceable ores;

	
	private Link showOnyxReporterButton;
	private LayoutMain3ColsController columLayoutCtr;
	//<OLATCE-1124>
	private OnyxReporterConnector connector;
	private List<Identity> allUsersList;
	//</OLATCE-1124>

	/**
	 * Constructor for the assessment tool controller.
	 * @param ureq
	 * @param wControl
	 * @param course
	 * @param assessmentCallback
	 */
AssessmentMainController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, IAssessmentCallback assessmentCallback) {
		super(ureq, wControl);
		
		Translator translator = Util.createPackageTranslator(org.olat.course.assessment.AssessmentMainController.class, getTranslator().getLocale(), getTranslator());
		setTranslator(translator);

		getUserActivityLogger().setStickyActionType(ActionType.admin);
		this.ores = ores;
		this.callback = assessmentCallback;
		this.localUserCourseEnvironmentCache = new HashMap<Long, UserCourseEnvironment>();
		this.initialLaunchDates = new HashMap<Long, Date>();

    //use the PropertyHandlerTranslator	as tableCtr translator
		propertyHandlerTranslator = UserManager.getInstance().getPropertyHandlerTranslator(getTranslator());

		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = (roles.isAuthor() || roles.isGroupManager() || roles.isUserManager() || roles.isOLATAdmin());

		main = new Panel("assessmentmain");

		// Intro page, static
		index = createVelocityContainer("assessment_index");

		Identity focusOnIdentity = null;
		ICourse course = CourseFactory.loadCourse(ores);
		//<OLATCE-1124>
		boolean hasAssessableNodes = course.hasAssessableNodes() || checkForQTI21SurveyInTree(course.getRunStructure().getRootNode());
		//</OLATCE-1124>
		if (hasAssessableNodes) {
			BusinessControl bc = getWindowControl().getBusinessControl();
			ContextEntry ceIdentity = bc.popLauncherContextEntry();
			if (ceIdentity != null) {
				OLATResourceable oresIdentity = ceIdentity.getOLATResourceable();
				if (OresHelper.isOfType(oresIdentity, Identity.class)) {
					Long identityKey = oresIdentity.getResourceableId();
					focusOnIdentity = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey);
				}
			}

			index.contextPut("hasAssessableNodes", Boolean.TRUE);

			// --- assessment notification subscription ---
			csc = AssessmentUIFactory.createContextualSubscriptionController(ureq, wControl, course);
			if (csc != null) {
				listenTo(csc); // cleanup on dispose
				index.put("assessmentSubscription", csc.getInitialComponent());
			}

			// Wrapper container: adds header
			wrapper = createVelocityContainer("wrapper");

			// Init the group and the user chooser view velocity container
			groupChoose = createVelocityContainer("groupchoose");
			allUsersButton = LinkFactory.createButtonSmall("cmd.all.users", groupChoose, this);
			groupChoose.contextPut("isFiltering", Boolean.TRUE);
			backLinkGC = LinkFactory.createLinkBack(groupChoose, this);

			userChoose = createVelocityContainer("userchoose");
			
			showOnyxReporterButton = LinkFactory.createButtonSmall("cmd.showOnyxReporter", userChoose, this);
			
			showAllCourseNodesButton = LinkFactory.createButtonSmall("cmd.showAllCourseNodes", userChoose, this);
			filterCourseNodesButton  = LinkFactory.createButtonSmall("cmd.filterCourseNodes", userChoose, this);
			userChoose.contextPut("isFiltering", Boolean.TRUE);
			backLinkUC = LinkFactory.createLinkBack(userChoose, this);
			
			onyxReporterVC = createVelocityContainer("onyxreporter");
			backLinkOR = LinkFactory.createLinkBack(onyxReporterVC, this);
			

			nodeChoose = createVelocityContainer("nodechoose");

			// Initialize all groups that the user is allowed to coach
			coachedGroups = getAllowedGroupsFromGroupmanagement(ureq.getIdentity());

			// preload the assessment cache to speed up everything as background thread
			// the thread will terminate when finished
			assessmentCachPreloaderThread = new AssessmentCachePreloadThread("assessmentCachPreloader-" + course.getResourceableId());
			assessmentCachPreloaderThread.setDaemon(true);
			assessmentCachPreloaderThread.start();

		} else {
			index.contextPut("hasAssessableNodes", Boolean.FALSE);
		}

		// Navigation menu
		menuTree = new MenuTree("menuTree");
		TreeModel tm = buildTreeModel(hasAssessableNodes);
		menuTree.setTreeModel(tm);
		menuTree.setSelectedNodeId(tm.getRootNode().getIdent());
		menuTree.addListener(this);

		// Tool and action box
		toolC = ToolFactory.createToolController(getWindowControl());
		listenTo(toolC);
		toolC.addHeader(translate("tool.name"));
		toolC.addLink("cmd.close", translate("command.closeassessment"), null, "b_toolbox_close");

		// Start on index page
		main.setContent(index);
		
		columLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, toolC.getInitialComponent(), main, "course" + course.getResourceableId());
		
		listenTo(columLayoutCtr); // cleanup on dispose
		putInitialPanel(columLayoutCtr.getInitialComponent());

		if(focusOnIdentity != null) {
			//fill the user list for the
			this.mode = MODE_USERFOCUS;
			this.identitiesList = getAllIdentitisFromGroupmanagement();
			doSimpleUserChoose(ureq, this.identitiesList);

			GenericTreeModel menuTreeModel = (GenericTreeModel)menuTree.getTreeModel();
			TreeNode userNode = menuTreeModel.findNodeByUserObject(CMD_USERFOCUS);
			if(userNode != null) {
				menuTree.setSelectedNode(userNode);
			}

			// select user
			assessedIdentityWrapper = AssessmentHelper.wrapIdentity(focusOnIdentity, localUserCourseEnvironmentCache, initialLaunchDates, course, null);
			identityAssessmentController = new IdentityAssessmentEditController(getWindowControl(),ureq, focusOnIdentity, course, true);
			listenTo(identityAssessmentController);
			setContent(identityAssessmentController.getInitialComponent());
		}

		// Register for assessment changed events
		course.getCourseEnvironment().getAssessmentManager().registerForAssessmentChangeEvents(this, ureq.getIdentity());
	}

	//<OLATCE-1124>
	private boolean checkForQTI21Survey(CourseNode node){
		boolean isSurvey = false;
		if(node instanceof IQSURVCourseNode){
			RepositoryEntry entry = node.getReferencedRepositoryEntry();
			if (entry != null && OnyxModule.isOnyxTest(entry.getOlatResource())) {
				isSurvey = true;
			}
		}
		return isSurvey;
	}

	private boolean checkForQTI21SurveyInTree(CourseNode node){
		boolean isSurvey = checkForQTI21Survey(node);
		if(!isSurvey){
			int count = node.getChildCount();
			for (int i = 0; i < count; i++) {
				CourseNode cn = (CourseNode) node.getChildAt(i);
				isSurvey = checkForQTI21SurveyInTree(cn);
				if(isSurvey)break;
			}
		}
		return isSurvey;
	}
	//</OLATCE-1124>
	
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
				
				resetToolController();
				
				// reset helper variables
				this.currentCourseNode = null;
				this.currentGroup = null;
				this.isFiltering = true;
				if (cmd.equals(CMD_INDEX)) {
					main.setContent(index);
				} else if (cmd.equals(CMD_USERFOCUS)) {
					this.mode = MODE_USERFOCUS;
					this.identitiesList = getAllIdentitisFromGroupmanagement();
					doSimpleUserChoose(ureq, this.identitiesList);
				} else if (cmd.equals(CMD_GROUPFOCUS)) {
					this.mode = MODE_GROUPFOCUS;
					doGroupChoose(ureq);
				} else if (cmd.equals(CMD_NODEFOCUS)) {
					this.mode = MODE_NODEFOCUS;
					doNodeChoose(ureq);
				} else if (cmd.equals(CMD_BULKFOCUS)){
					this.mode = MODE_BULKFOCUS;
					doBulkChoose(ureq);
				}
			}
		} else if (source == allUsersButton){
			this.identitiesList = getAllIdentitisFromGroupmanagement();
			// Init the user list with this identitites list
			this.currentGroup = null;
			doUserChooseWithData(ureq, this.identitiesList, null, this.currentCourseNode);
		} else if (source == backLinkGC){
			setContent(nodeListCtr.getInitialComponent());
		} else if (source == backLinkUC){
			setContent(groupChoose);
		} else if (source == showAllCourseNodesButton) {
			enableFilteringCourseNodes(false);
		}  else if (source == filterCourseNodesButton) {
			enableFilteringCourseNodes(true);
		
		} else if (source == showOnyxReporterButton) {
			this.identitiesList = getGroupIdentitiesFromGroupmanagement(this.currentGroup);
			this.onyxReporterBackLocation = "userChoose";
			//<OLATCE-1124>
			if (!showOnyxReporter(ureq, currentCourseNode, false)) {
			//</OLATCE-1124>
				getWindowControl().setError(translate("onyxreporter.error"));
			}
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
	 * @param ureq The UserRequest for getting the identity and role of the current user.
	 */
	//<OLATCE-1124>
	private boolean showOnyxReporter(UserRequest ureq, CourseNode node, boolean forSurvey) {
		if (OnyxModule.isOnyxTest(node.getReferencedRepositoryEntry().getOlatResource())) {
	//</OLATCE-1124>
			//<ONYX-705>
			OnyxReporterConnector onyxReporter = null;
			
			try{
				onyxReporter = new OnyxReporterConnector();
			} catch (OnyxReporterException e){
				log.error("unable to connect to onyxreporter!", e);
			}
			//</ONYX-705>
			if (onyxReporter != null) {
				if (this.identitiesList == null) {
					this.identitiesList = getAllIdentitisFromGroupmanagement();
				}
				String iframeSrc = "";
				try {
					//<OLATCE-1124>
					if(forSurvey){
						ICourse course = CourseFactory.loadCourse(ores);
						if(node instanceof IQSURVCourseNode){
							iframeSrc = onyxReporter.startReporterGUIForSurvey(ureq.getIdentity(), node, course.getCourseBaseContainer()
								.getBasefile() + File.separator + node.getIdent() + File.separator);
						} else {
							iframeSrc = onyxReporter.startReporterGUI(ureq.getIdentity(), this.identitiesList, node, null, false, true);	
						}
					} else {
						iframeSrc = onyxReporter.startReporterGUI(ureq.getIdentity(), this.identitiesList, node, null, false, false);
					}
					//</OLATCE-1124>
				} catch (OnyxReporterException orE) {
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
	 * Enable/disable filtering of course-nodes in user-selection table
	 * and update new course-node-list.
	 * (Assessemnt-tool =>
	 * @param enableFiltering
	 */
	private void enableFilteringCourseNodes(boolean enableFiltering) {
		ICourse course = CourseFactory.loadCourse(ores);
		this.isFiltering = enableFiltering;
		userChoose.contextPut("isFiltering", enableFiltering);
		this.nodeFilters = addAssessableNodesToList(course.getRunStructure().getRootNode(), this.currentGroup);
		userListCtr.setFilters(this.nodeFilters, null );
	}

	/**
	 * Enable/disable filtering of groups in
	 * @param enableFiltering
	 * @param ureq
	 */
	private void enableFilteringGroups(boolean enableFiltering, UserRequest ureq) {
		this.isFiltering = enableFiltering;
		groupChoose.contextPut("isFiltering", enableFiltering);
		doGroupChoose(ureq);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == toolC) {
			if (event.getCommand().equals("cmd.close")) {
				disposeChildControllerAndReleaseLocks(); // cleanup locks from children
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
		else if (source == groupListCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals(CMD_CHOOSE_GROUP)) {
					int rowid = te.getRowId();
					GroupAndContextTableModel groupListModel = (GroupAndContextTableModel) groupListCtr.getTableDataModel();
					this.currentGroup = groupListModel.getBusinessGroupAt(rowid);
					this.identitiesList = getGroupIdentitiesFromGroupmanagement(this.currentGroup);
					// Init the user list with this identitites list
					doUserChooseWithData(ureq, this.identitiesList, this.currentGroup, this.currentCourseNode);
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
						this.assessedIdentityWrapper = userListModel.getWrappedIdentity(rowid);
					}
					// init edit controller for this identity and this course node
					// or use identity assessment overview if no course node is defined
					if (this.currentCourseNode == null) {
						Identity assessedIdentity = assessedIdentityWrapper.getIdentity();
						removeAsListenerAndDispose(identityAssessmentController);
						identityAssessmentController = new IdentityAssessmentEditController(getWindowControl(),ureq, assessedIdentity, course, true);
						listenTo(identityAssessmentController);
						setContent(identityAssessmentController.getInitialComponent());
					} else {
						removeAsListenerAndDispose(assessmentEditController);
						assessmentEditController = new AssessmentEditController(ureq, getWindowControl(),course, currentCourseNode, assessedIdentityWrapper);
						listenTo(assessmentEditController);
						main.setContent(assessmentEditController.getInitialComponent());
					}
				}
			} else if (event.equals(TableController.EVENT_FILTER_SELECTED)) {
				this.currentCourseNode = (AssessableCourseNode) userListCtr.getActiveFilter();
				doUserChooseWithData(ureq, this.identitiesList, this.currentGroup, this.currentCourseNode);
			}
		}
		else if (source == nodeListCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				 //moved variable declaration for course above the if-block
				ICourse course = CourseFactory.loadCourse(ores);
				if (actionid.equals(CMD_SELECT_NODE)) {
					int rowid = te.getRowId();
					Map<String,Object> nodeData = (Map<String,Object>) nodeTableModel.getObject(rowid);
					CourseNode node = course.getRunStructure().getNode((String) nodeData.get(AssessmentHelper.KEY_IDENTIFYER));
					this.currentCourseNode = (AssessableCourseNode) node;
					// cast should be save, only assessable nodes are selectable
					doGroupChoose(ureq);
				
				} else if (actionid.equals(CMD_SHOW_ONYXREPORT)) {
					int rowid = te.getRowId();
					Map<String,Object> nodeData = (Map<String,Object>) nodeTableModel.getObject(rowid);
					CourseNode node = course.getRunStructure().getNode((String) nodeData.get(AssessmentHelper.KEY_IDENTIFYER));
					this.currentCourseNode = (AssessableCourseNode) node;
					this.onyxReporterBackLocation = "nodeListCtr";
					//<OLATCE-1124>
					if (!showOnyxReporter(ureq, node, false)) {
						getWindowControl().setError(translate("onyxreporter.error"));
					}
				} else if (actionid.equals(CMD_SHOW_ONYXREPORT_SURVEY)) {
					int rowid = te.getRowId();
					Map<String,Object> nodeData = (Map<String,Object>) nodeTableModel.getObject(rowid);
					CourseNode node = course.getRunStructure().getNode((String) nodeData.get(AssessmentHelper.KEY_IDENTIFYER));
					this.onyxReporterBackLocation = "nodeListCtr";
					if (!showOnyxReporter(ureq, node, true)) {
					//<OLATCE-1124>
						getWindowControl().setError(translate("onyxreporter.error"));
					}
				}
				
			} else if (event.equals(TableController.EVENT_FILTER_SELECTED)) {
				this.currentCourseNode = (AssessableCourseNode) nodeListCtr.getActiveFilter();
				doUserChooseWithData(ureq, this.identitiesList, null, this.currentCourseNode);
			}
		}
		else if (source == assessmentEditController) {
			if (event.equals(Event.CHANGED_EVENT)) {
				// refresh identity in list model
				if (userListCtr != null
						&& userListCtr.getTableDataModel() instanceof AssessedIdentitiesTableDataModel) {
					AssessedIdentitiesTableDataModel				
 atdm = (AssessedIdentitiesTableDataModel) userListCtr.getTableDataModel();
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
		}
		else if (source == identityAssessmentController) {
			if (event.equals(Event.CANCELLED_EVENT)) {
				setContent(userChoose);
			}
	  }
	}

	/**
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {
		if ((event instanceof AssessmentChangedEvent) &&  event.getCommand().equals(AssessmentChangedEvent.TYPE_SCORE_EVAL_CHANGED)) {
			AssessmentChangedEvent ace = (AssessmentChangedEvent) event;
			doUpdateLocalCacheAndUserModelFromAssessmentEvent(ace);
		}
	}

	
	public void setToolController(ToolController toolC) {
		if(toolC!=null&&columLayoutCtr!=null){
			columLayoutCtr.setCol2(toolC.getInitialComponent());
		}
	}

	public void resetToolController() {
		if(toolC!=null&&columLayoutCtr!=null){
			columLayoutCtr.setCol2(toolC.getInitialComponent());
		}
	}
	

	/**
	 * Notify subscribers when test are passed or attemps count change
	 * EXPERIMENTAL!!!!!
	 */
	/*private void doNotifyAssessmentEvent(AssessmentChangedEvent ace) {
		String assessmentChangeType = ace.getCommand();
		// notify only comment has been changed
		if (assessmentChangeType == AssessmentChangedEvent.TYPE_PASSED_CHANGED
			|| assessmentChangeType == AssessmentChangedEvent.TYPE_ATTEMPTS_CHANGED)
		{
			// if notification is enabled -> notify the publisher about news
			if (subsContext != null)
			{
				NotificationsManagerImpl.getInstance().markPublisherNews(subsContext, ace.getIdentity());
			}
		}
	}*/


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
		SecurityGroup selectedSecurityGroup = selectedGroup.getPartipiciantGroup();
		return BaseSecurityManager.getInstance().getIdentitiesOfSecurityGroup(selectedSecurityGroup);
	}

	/**
	 * @return List of all course participants
	 */
	List<Identity> getAllIdentitisFromGroupmanagement() {
		//<OLATCE-1124>
		if(allUsersList == null){
			allUsersList = new ArrayList<Identity>();
			BaseSecurity secMgr = BaseSecurityManager.getInstance();
			Iterator<BusinessGroup> iter = this.coachedGroups.iterator();
			while (iter.hasNext()) {
				BusinessGroup group = iter.next();
				SecurityGroup secGroup = group.getPartipiciantGroup();
				List<Identity> identities = secMgr.getIdentitiesOfSecurityGroup(secGroup);
				for (Iterator<Identity> identitiyIter = identities.iterator(); identitiyIter.hasNext();) {
					Identity identity = identitiyIter.next();
					if (!PersistenceHelper.listContainsObjectByKey(allUsersList, identity)) {
						// only add if not already in list
						allUsersList.add(identity);
					}
				}
			}
			//fxdiff VCRP-1,2: access control of resources
			RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(ores, false);
			if(re.getParticipantGroup() != null) {
				for (Identity identity : secMgr.getIdentitiesOfSecurityGroup(re.getParticipantGroup())) {
					if (!PersistenceHelper.listContainsObjectByKey(allUsersList, identity)) {
						allUsersList.add(identity);
					}
				}
			}
		}
		//</OLATCE-1124>
		return allUsersList;
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
			return gm.getAllLearningGroupsFromAllContexts();
		} else if (callback.mayAssessCoachedUsers()) {
			return  gm.getOwnedLearningGroupsFromAllContexts(identity);
		} else {
			throw new OLATSecurityException("No rights to assess or even view any groups");
		}
	}


	/**
	 * Initialize the group list table according to the users access rights
	 * @param ureq The user request
	 */
	private void doGroupChoose(UserRequest ureq) {
		ICourse course = CourseFactory.loadCourse(ores);
		removeAsListenerAndDispose(groupListCtr);
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("groupchoose.nogroups"));
		groupListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(groupListCtr);
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.group.name", 0, CMD_CHOOSE_GROUP, ureq.getLocale()));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.group.desc", 1, null, ureq.getLocale()));
		CourseGroupManager gm = course.getCourseEnvironment().getCourseGroupManager();
		if (gm.getLearningGroupContexts().size() > 1) {
		// show groupcontext row only if multiple contexts are found
			groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.group.context", 2, null, ureq.getLocale()));
		}

		Translator defaultContextTranslator = new PackageTranslator(Util.getPackageName(BGContextTableModel.class), ureq.getLocale());
		// loop over all groups to filter depending on condition
		List<BusinessGroup> currentGroups = new ArrayList<BusinessGroup>();
		for (Iterator iter = this.coachedGroups.iterator(); iter.hasNext();) {
			BusinessGroup group = (BusinessGroup) iter.next();
			if ( !isFiltering || isVisibleAndAccessable(this.currentCourseNode, group) ) {
				currentGroups.add(group);
			}
		}
		GroupAndContextTableModel groupTableDataModel = new GroupAndContextTableModel(currentGroups, defaultContextTranslator);
		groupListCtr.setTableDataModel(groupTableDataModel);
		groupChoose.put("grouplisttable", groupListCtr.getInitialComponent());

		// render all-groups button only if goups are available
		if (this.coachedGroups.size() > 0) groupChoose.contextPut("hasGroups", Boolean.TRUE);
		else groupChoose.contextPut("hasGroups", Boolean.FALSE);

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
		
		userChoose.contextPut("iframeOK", Boolean.FALSE);
		
		if (mode == MODE_GROUPFOCUS) {
			this.nodeFilters = addAssessableNodesToList(course.getRunStructure().getRootNode(), group);
			if (courseNode == null && this.nodeFilters.size() > 0) {
				this.currentCourseNode = (AssessableCourseNode) this.nodeFilters.get(0);
				courseNode = this.currentCourseNode;
			}
		}
		// Init table headers
		removeAsListenerAndDispose(userListCtr);
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("userchoose.nousers"));

		if (mode == MODE_GROUPFOCUS) {
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
			Identity identity = (Identity) identities.get(i);
			// if course node is null the wrapper will only contain the identity and no score information
			AssessedIdentityWrapper aiw = AssessmentHelper.wrapIdentity(identity,
					localUserCourseEnvironmentCache, initialLaunchDates, course, courseNode);
			wrappedIdentities.add(aiw);
		}
		// Add the wrapped identities to the table data model
		AssessedIdentitiesTableDataModel tdm = new AssessedIdentitiesTableDataModel(wrappedIdentities, courseNode, ureq.getLocale(), isAdministrativeUser);
		tdm.addColumnDescriptors(userListCtr, CMD_CHOOSE_USER, mode == MODE_NODEFOCUS || mode == MODE_GROUPFOCUS);
		userListCtr.setTableDataModel(tdm);


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

	private void doSimpleUserChoose(UserRequest ureq, List<Identity> identities) {
		
		userChoose.contextPut("iframeOK", Boolean.FALSE);
		
		// Init table headers
		removeAsListenerAndDispose(userListCtr);
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setPreferencesOffered(true, "assessmentSimpleUserList");
		tableConfig.setTableEmptyMessage(translate("userchoose.nousers"));

		userListCtr = UserControllerFactory.createTableControllerFor(tableConfig, identities, ureq, getWindowControl(), CMD_CHOOSE_USER);
		listenTo(userListCtr);

		userChoose.contextPut("showBack", Boolean.FALSE);
		userChoose.contextPut("showGroup", Boolean.FALSE);

		userChoose.put("userlisttable", userListCtr.getInitialComponent());
		// set main vc to userchoose
		setContent(userChoose);
	}

	private void doNodeChoose(UserRequest ureq) {
		ICourse course = CourseFactory.loadCourse(ores);
		removeAsListenerAndDispose(nodeListCtr);
		// table configuraton
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("nodesoverview.nonodes"));
		tableConfig.setDownloadOffered(false);
		tableConfig.setColumnMovingOffered(false);
		tableConfig.setSortingEnabled(false);
		tableConfig.setDisplayTableHeader(true);
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);

		nodeListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(nodeListCtr);
		// table columns
		nodeListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.node", 0,
				null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, new IndentedNodeRenderer()));
		nodeListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.action.select", 1,
				CMD_SELECT_NODE, ureq.getLocale()));
		
		nodeListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.overallselect", 2,
				CMD_SHOW_ONYXREPORT, ureq.getLocale()));
		//<OLATCE-1124>
		nodeListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.surveyselect", 3,
				CMD_SHOW_ONYXREPORT_SURVEY, ureq.getLocale()));
		//</OLATCE-1124>

		// get list of course node data and populate table data model
		CourseNode rootNode = course.getRunStructure().getRootNode();
		List<Map<String, Object>> nodesTableObjectArrayList = addAssessableNodesAndParentsToList(0, rootNode);

		// only populate data model if data available
		if (nodesTableObjectArrayList == null) {
			String text = translate("nodesoverview.nonodes");
			Controller messageCtr = MessageUIFactory.createSimpleMessage(ureq, getWindowControl(), text);
			listenTo(messageCtr);//dispose if this one gets disposed
			nodeChoose.put("nodeTable", messageCtr.getInitialComponent());
		} else {
			nodeTableModel = new NodeTableDataModelOnyx(nodesTableObjectArrayList, getTranslator());
			nodeListCtr.setTableDataModel(nodeTableModel);
			nodeChoose.put("nodeTable", nodeListCtr.getInitialComponent());
		}

		// set main content to nodechoose, do not use wrapper
		main.setContent(nodeChoose);
	}

	private void doBulkChoose(UserRequest ureq) {
		ICourse course = CourseFactory.loadCourse(ores);
		List<Identity> allowedIdentities = getAllIdentitisFromGroupmanagement();
		removeAsListenerAndDispose(bamc);
		bamc = new BulkAssessmentMainController(ureq, getWindowControl(), course, allowedIdentities);
		listenTo(bamc);
		main.setContent(bamc.getInitialComponent());
	}

	/**
	 * Recursive method that adds assessable nodes and all its parents to a list
	 * @param recursionLevel
	 * @param courseNode
	 * @return A list of maps containing the node data
	 */
	private List<Map<String, Object>> addAssessableNodesAndParentsToList(int recursionLevel, CourseNode courseNode) {
		// 1) Get list of children data using recursion of this method
		List<Map<String, Object>> childrenData = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			CourseNode child = (CourseNode) courseNode.getChildAt(i);
			List<Map<String, Object>> childData = addAssessableNodesAndParentsToList( (recursionLevel + 1),  child);
			if (childData != null)
				childrenData.addAll(childData);
		}

		boolean hasDisplayableValuesConfigured = false;
		//<OLATCE-1124>
		if (childrenData.size() > 0 || courseNode instanceof AssessableCourseNode || courseNode instanceof IQSURVCourseNode) {
		//</OLATCE-1124>
			// Store node data in hash map. This hash map serves as data model for
			// the user assessment overview table. Leave user data empty since not used in
			// this table. (use only node data)
			Map<String,Object> nodeData = new HashMap<String, Object>();
			// indent
			nodeData.put(AssessmentHelper.KEY_INDENT, new Integer(recursionLevel));
			// course node data
			nodeData.put(AssessmentHelper.KEY_TYPE, courseNode.getType());
			nodeData.put(AssessmentHelper.KEY_TITLE_SHORT, courseNode.getShortTitle());
			
			
			nodeData.put(AssessmentHelper.KEY_TITLE_LONG, courseNode.getLongTitle());
			nodeData.put(AssessmentHelper.KEY_IDENTIFYER, courseNode.getIdent());

			if (courseNode instanceof AssessableCourseNode) {
				//<OLATCE-1124>
				if (courseNode.getReferencedRepositoryEntry() != null) {
					if (OnyxModule.isOnyxTest(courseNode.getReferencedRepositoryEntry().getOlatResource())) {
						nodeData.put(KEY_IS_ONYX, Boolean.FALSE);

						List<Identity> identities = getAllIdentitisFromGroupmanagement();
						if (identities.size() > 0) {
							try{
								if(connector == null){
									connector = new OnyxReporterConnector();									
								}
							} catch (OnyxReporterException ore){
								getWindowControl().setWarning("reporter.unavailable");
							}
							
							if(connector != null){
								boolean hasResults = connector.hasAnyResults(false, identities , null , courseNode);
								if(hasResults){
									nodeData.put(KEY_IS_ONYX, Boolean.TRUE);
									nodeData.put(KEY_IS_ONYX_SURVEY, Boolean.TRUE);
								}
							}
						}
					}
				}
				//</OLATCE-1124>
				AssessableCourseNode assessableCourseNode = (AssessableCourseNode) courseNode;
				if ( assessableCourseNode.hasDetails()
					|| assessableCourseNode.hasAttemptsConfigured()
					|| assessableCourseNode.hasScoreConfigured()
					|| assessableCourseNode.hasPassedConfigured()
					|| assessableCourseNode.hasCommentConfigured()) {
					hasDisplayableValuesConfigured = true;
				}
				if (assessableCourseNode.isEditableConfigured()) {
					// Assessable course nodes are selectable when they are aditable
					nodeData.put(AssessmentHelper.KEY_SELECTABLE, Boolean.TRUE);
				} else if (courseNode instanceof STCourseNode
						&& (assessableCourseNode.hasScoreConfigured()
						|| assessableCourseNode.hasPassedConfigured())) {
					// st node is special case: selectable on node choose list as soon as it
					// has score or passed
					nodeData.put(AssessmentHelper.KEY_SELECTABLE, Boolean.TRUE);
				} else {
					// assessable nodes that do not have score or passed are not selectable
					// (e.g. a st node with no defined rule
					nodeData.put(AssessmentHelper.KEY_SELECTABLE, Boolean.FALSE);
				}
			//<OLATCE-1124>
			} else if (checkForQTI21Survey(courseNode)){
				nodeData.put(KEY_IS_ONYX_SURVEY, Boolean.FALSE);
				List<Identity> identities = getAllIdentitisFromGroupmanagement();
				if (identities.size() > 0) {
					try{
						if(connector == null){
							connector = new OnyxReporterConnector();									
						}
					} catch (OnyxReporterException ore){
						getWindowControl().setWarning("reporter.unavailable");
					}
					
					if(connector != null){
						ICourse course = CourseFactory.loadCourse(ores);
						
						boolean hasResults = connector.hasAnyResults(true, null , course.getCourseBaseContainer().getBasefile() + File.separator + courseNode.getIdent() + File.separator , courseNode);
						if(hasResults){
							nodeData.put(KEY_IS_ONYX_SURVEY, Boolean.TRUE);
						}
					}
				}
				hasDisplayableValuesConfigured = true;
			//</OLATCE-1124>
			} else {
				// Not assessable nodes are not selectable. (e.g. a node that
				// has an assessable child node but is itself not assessable)
				nodeData.put(AssessmentHelper.KEY_SELECTABLE, Boolean.FALSE);
			}
			// 3) Add data of this node to mast list if node assessable or children list has any data.
			// Do only add nodes when they have any assessable element, otherwhise discard (e.g. empty course,
			// structure nodes without scoring rules)! When the discardEmptyNodes flag is set then only
			// add this node when there is user data found for this node.
			if (childrenData.size() > 0 || hasDisplayableValuesConfigured) {
				List<Map<String, Object>> nodeAndChildren = new ArrayList<Map<String, Object>>();
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
	private List addAssessableNodesToList(CourseNode courseNode, BusinessGroup group) {
		List result = new ArrayList();
		if (courseNode instanceof AssessableCourseNode) {
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
		if (getGroupIdentitiesFromGroupmanagement(group).size()==0) {
			// group has no participant, can not evalute
			return false;
		}
		ICourse course = CourseFactory.loadCourse(ores);
		// check if course node is visible for group
		// get first identity to use this identity for condition interpreter
		Identity identity = getGroupIdentitiesFromGroupmanagement(group).get(0);
		IdentityEnvironment identityEnvironment = new IdentityEnvironment();
		identityEnvironment.setIdentity(identity);
		UserCourseEnvironment uce = new UserCourseEnvironmentImpl(identityEnvironment, course.getCourseEnvironment());
		OnlyGroupConditionInterpreter ci = new OnlyGroupConditionInterpreter(uce);
		List listOfConditionExpressions = courseNode.getConditionExpressions();
		boolean allConditionAreValid = true;
		// loop over all conditions, all must be true
		for (Iterator iter = listOfConditionExpressions.iterator(); iter.hasNext();) {
			ConditionExpression conditionExpression = (ConditionExpression) iter.next();
			Tracing.logDebug("conditionExpression=" + conditionExpression, this.getClass());
			Tracing.logDebug("conditionExpression.getId()=" + conditionExpression.getId(), this.getClass());
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
	 * @param hasAssessableNodes true: show menu, false: hide menu
	 * @return The tree model
	 */
	private TreeModel buildTreeModel(boolean hasAssessableNodes) {
		GenericTreeNode root, gtn;

		GenericTreeModel gtm = new GenericTreeModel();
		root = new GenericTreeNode();
		root.setTitle(translate("menu.index"));
		root.setUserObject(CMD_INDEX);
		root.setAltText(translate("menu.index.alt"));
		gtm.setRootNode(root);

		// show real menu only when there are some assessable nodes
		if (hasAssessableNodes) {
			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.groupfocus"));
			gtn.setUserObject(CMD_GROUPFOCUS);
			gtn.setAltText(translate("menu.groupfocus.alt"));
			root.addChild(gtn);

			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.nodefocus"));
			gtn.setUserObject(CMD_NODEFOCUS);
			gtn.setAltText(translate("menu.nodefocus.alt"));
			root.addChild(gtn);

			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.userfocus"));
			gtn.setUserObject(CMD_USERFOCUS);
			gtn.setAltText(translate("menu.userfocus.alt"));
			root.addChild(gtn);

			gtn = new GenericTreeNode();
			gtn.setTitle(translate("menu.bulkfocus"));
			gtn.setUserObject(CMD_BULKFOCUS);
			gtn.setAltText(translate("menu.bulkfocus.alt"));
			root.addChild(gtn);
		}

		return gtm;
	}

	public ToolController getToolController() {
		return this.toolC;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// controllers disposed by BasicController
			toolC = null;
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
	class AssessmentCachePreloadThread extends Thread {
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
			List<Identity> identities = getAllIdentitisFromGroupmanagement();
			
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
      // TODO: cg(04.09.2008): replace 'commit/closeSession' with doInManagedBlock
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

	/**
	 *
	 * @param ureq
	 * @param viewIdentifier if 'node-choose' does activate node-choose view
	 */
	public void activate(UserRequest ureq, String viewIdentifier) {
		if (viewIdentifier != null && viewIdentifier.equals("node-choose")) {
      // jump to state node-choose
			doNodeChoose(ureq);
		}
	}
}
