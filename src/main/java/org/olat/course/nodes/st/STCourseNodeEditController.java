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

package org.olat.course.nodes.st;

import java.util.List;
import java.util.Set;

import org.olat.core.commons.controllers.filechooser.LinkFileCombiCalloutController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.gui.control.generic.iframe.DeliveryOptionsConfigurationController;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseEntryRef;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.condition.Condition;
import org.olat.course.config.CourseConfig;
import org.olat.course.editor.CourseEditorHelper;
import org.olat.course.editor.NodeEditController;
import org.olat.course.highscore.ui.HighScoreEditController;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.sp.SecuritySettingsForm;
import org.olat.course.run.tools.CourseToolLinkTreeModel;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseInternalLinkTreeModel;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.edusharing.VFSEdusharingProvider;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.ui.settings.LazyRepositoryEdusharingProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<BR/> Edit controller for a course node of type structure <P/>
 * 
 * Initial Date: Oct 12, 2004
 * @author gnaegi
 */
public class STCourseNodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {
	
	private static final Set<String> UPLOAD_MIME_TYPE = Set.of("text/html", "application/zip");
	private static final String[] SELECTION_SUFFIX = new String[] { "html", "htm" };
	
	public static final String PANE_TAB_ST_CONVENTIONAL_ASSESSMENT = "pane.tab.conventional.assessment";
	private static final String PANE_TAB_DELIVERYOPTIONS = "pane.tab.deliveryOptions";
	public static final String PANE_TAB_ST_CONFIG = "pane.tab.st_config";
	private static final String PANE_TAB_HIGHSCORE = "pane.tab.highscore";
	
	/** configuration key for the filename */
	public static final String CONFIG_KEY_FILE = "file";
	/**
	 * configuration key: should relative links like ../otherfolder/my.css be
	 * allowed? *
	 */
	public static final String CONFIG_KEY_ALLOW_RELATIVE_LINKS = "allowRelativeLinks";
	public static final String CONFIG_KEY_DELIVERYOPTIONS = "deliveryOptions";
	/** configuration key: should the students be allowed to edit the page? */
	public static final String CONFIG_KEY_ALLOW_COACH_EDIT = "allowCoachEdit";
	// key to store information on what to display in the run 
	public static final String CONFIG_KEY_DISPLAY_TYPE = "display";
	// display a custom file
	public static final String CONFIG_VALUE_DISPLAY_FILE = "file";
	// display a simple table on content
	public static final String CONFIG_VALUE_DISPLAY_TOC = "toc";
	// display a detailed peek view
	public static final String CONFIG_VALUE_DISPLAY_PEEKVIEW = "peekview";
	//do not display peek view, delegate to first child CourseNode
	public static final String CONFIG_VALUE_DISPLAY_DELEGATE = "delegate";
	/* Key to set the peekview filter*/
	public static final String CONFIG_KEY_PEEKVIEW_FILTER = "peekview.filter";
	public static final String CONFIG_VALUE_PEEKVIEW_ALL = "all";
	public static final String CONFIG_VALUE_PEEKVIEW_STRUCTURES = "structures";
	// key to enable the peekview course node selection */
	public static final String CONFIG_KEY_CHILDREN_FILTER = "children.filter";
	public static final String CONFIG_VALUE_CHILDREN_ALL = "all";
	public static final String CONFIG_VALUE_CHILDREN_SELECTION = "children.selection";
	// key to display the enabled child node peek views
	public static final String CONFIG_KEY_CHILDREN_IDENTS = "peekviewChildNodes";
	// key to store the number of columns
	public static final String CONFIG_KEY_COLUMNS = "columns";	
	
	private static final String[] paneKeys = { PANE_TAB_ST_CONVENTIONAL_ASSESSMENT, PANE_TAB_ST_CONFIG };

	private STCourseNode stNode;
	private List<CourseNode> assessableChildren;
	private STCourseNodeDisplayConfigFormController nodeDisplayConfigFormController;
	private STConventionalAssessmentConfigController conventionalAssessmentConfigCtrl;
	private HighScoreEditController highScoreNodeConfigController;

	
	private VelocityContainer configvc;
	private final CourseConfig courseConfig;
	private VFSContainer courseFolderContainer;
	private String chosenFile;
	private boolean allowRelativeLinks;
	private DeliveryOptions deliveryOptions;

	private LinkFileCombiCalloutController combiLinkCtr;
	private SecuritySettingsForm securitySettingForm;
	private DeliveryOptionsConfigurationController deliveryOptionsCtrl;

	private boolean editorEnabled = false;
	private UserCourseEnvironment euce;
	
	private TabbedPane myTabbedPane;
	private int highScoreTabPos;
	private CourseEditorTreeModel editorModel;
	private final Long repoKey;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private NodeAccessService nodeAccessService;

	public STCourseNodeEditController(UserRequest ureq, WindowControl wControl, STCourseNode stNode, ICourse course, UserCourseEnvironment euce) {
		super(ureq, wControl);
		this.stNode = stNode;
		this.courseFolderContainer = course.getCourseFolderContainer();
		this.courseConfig = course.getCourseConfig();
		this.euce = euce;
		this.editorModel = course.getEditorTreeModel();
		this.repoKey = RepositoryManager.getInstance().lookupRepositoryEntryKey(course, true);
		
		// Set the root as parent as a workaround for the STAssessmentHandler,
		// because the root node has some informations about the assessment configs.
		if (stNode.getParent() == null) {
			CourseNode rootNode = editorModel.getCourseNode(editorModel.getRootNode().getIdent());
			if (rootNode != null && !rootNode.getIdent().equals(stNode.getIdent())) {
				stNode.setParent(rootNode);
			}
		}
		
		Translator fallback = Util.createPackageTranslator(Condition.class, getLocale());
		Translator newTranslator = Util.createPackageTranslator(STCourseNodeEditController.class, getLocale(), fallback);
		setTranslator(newTranslator);
			
		configvc = createVelocityContainer("config");
		// type of display configuration: manual, auto, peekview etc
		nodeDisplayConfigFormController = new STCourseNodeDisplayConfigFormController(ureq, wControl, stNode.getModuleConfiguration(), editorModel.getCourseEditorNodeById(stNode.getIdent()));
		listenTo(nodeDisplayConfigFormController);
		configvc.put("nodeDisplayConfigFormController", nodeDisplayConfigFormController.getInitialComponent());

		// Load configured value for file if available and enable editor when in
		// file display move, even when no file is selected (this will display the
		// file selector button)
		chosenFile = (String) stNode.getModuleConfiguration().get(CONFIG_KEY_FILE); 
		editorEnabled = (CONFIG_VALUE_DISPLAY_FILE.equals(stNode.getModuleConfiguration().getStringValue(CONFIG_KEY_DISPLAY_TYPE)));
		
		allowRelativeLinks = stNode.getModuleConfiguration().getBooleanSafe(CONFIG_KEY_ALLOW_RELATIVE_LINKS);
		deliveryOptions = (DeliveryOptions)stNode.getModuleConfiguration().get(CONFIG_KEY_DELIVERYOPTIONS);

		if (editorEnabled) {
			addCustomFileConfigToView(ureq);
		}

		deliveryOptionsCtrl = new DeliveryOptionsConfigurationController(ureq, getWindowControl(), deliveryOptions, "Knowledge Transfer#_splayout", false);
		listenTo(deliveryOptionsCtrl);

		// Find assessable children nodes
		assessableChildren = AssessmentHelper.getAssessableNodes(new CourseEntryRef(euce), editorModel, stNode);
		
		// HighScore Controller
		highScoreNodeConfigController = new HighScoreEditController(ureq, wControl, stNode.getModuleConfiguration(), course);
		listenTo(highScoreNodeConfigController);
		
		
		if (nodeAccessService.isScoreCalculatorSupported(NodeAccessType.of(course))) {
			conventionalAssessmentConfigCtrl = new STConventionalAssessmentConfigController(ureq, wControl, euce, stNode, assessableChildren);
			listenTo(conventionalAssessmentConfigCtrl);
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source instanceof NodeEditController) {
			if(combiLinkCtr != null && combiLinkCtr.isDoProposal()){
				combiLinkCtr.setRelFilePath(CourseEditorHelper.createUniqueRelFilePathFromShortTitle(stNode, courseFolderContainer));
			}
		} else if (source == deliveryOptionsCtrl) {
			deliveryOptions = deliveryOptionsCtrl.getDeliveryOptions();
			stNode.getModuleConfiguration().set(CONFIG_KEY_DELIVERYOPTIONS, deliveryOptions);
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		} else if(source == combiLinkCtr){
			if(event == Event.DONE_EVENT){
				chosenFile = VFSManager.getRelativeItemPath(combiLinkCtr.getFile(), courseFolderContainer, null);
				stNode.getModuleConfiguration().set(CONFIG_KEY_FILE, chosenFile);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
				if(!myTabbedPane.containsTab(deliveryOptionsCtrl.getInitialComponent())) {
					myTabbedPane.addTab(translate(PANE_TAB_DELIVERYOPTIONS), deliveryOptionsCtrl.getInitialComponent());
				}
				configvc.contextPut("editorEnabled", combiLinkCtr.isEditorEnabled());
			}
		} else if(source == securitySettingForm){
			if(event == Event.DONE_EVENT){
				boolean relativeLinks = securitySettingForm.getAllowRelativeLinksConfig();
				stNode.getModuleConfiguration().set(CONFIG_KEY_ALLOW_RELATIVE_LINKS, relativeLinks);
				stNode.getModuleConfiguration().set(CONFIG_KEY_ALLOW_COACH_EDIT, securitySettingForm.getAllowCoachEditConfig());
				combiLinkCtr.setAllowEditorRelativeLinks(relativeLinks);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == nodeDisplayConfigFormController) {
			if (event == Event.DONE_EVENT) {
				// update the module configuration
				ModuleConfiguration moduleConfig = stNode.getModuleConfiguration();
				nodeDisplayConfigFormController.updateModuleConfiguration(moduleConfig);
				allowRelativeLinks = moduleConfig.getBooleanSafe(CONFIG_KEY_ALLOW_RELATIVE_LINKS);
				// update some class vars
				if (CONFIG_VALUE_DISPLAY_FILE.equals(moduleConfig.getStringValue(CONFIG_KEY_DISPLAY_TYPE))) {
					editorEnabled = true;
					addCustomFileConfigToView(ureq);
				} else { // user generated overview
					editorEnabled = false;
					removeCustomFileConfigFromView();
				}
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == conventionalAssessmentConfigCtrl) {
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			fireEvent(ureq, NodeEditController.REMINDER_VISIBILITY_EVENT);
			updateHighscoreTab();
		} else if (source == highScoreNodeConfigController){
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
	}

	private void addCustomFileConfigToView(UserRequest ureq) {
		// Read configuration
		boolean relFilPathIsProposal = false;
		String relFilePath = chosenFile;
		if(relFilePath == null){
			// Use calculated file and folder name as default when not yet configured
			relFilePath = CourseEditorHelper.createUniqueRelFilePathFromShortTitle(stNode, courseFolderContainer);
			relFilPathIsProposal = true;
		}
		// File create/select controller
		RepositoryEntry courseEntry = euce.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		VFSEdusharingProvider edusharingProvider = new LazyRepositoryEdusharingProvider(repoKey);
		combiLinkCtr = new LinkFileCombiCalloutController(ureq, getWindowControl(), courseFolderContainer, relFilePath,
				relFilPathIsProposal, allowRelativeLinks, false, new CourseInternalLinkTreeModel(editorModel),
				new CourseToolLinkTreeModel(courseConfig, courseEntry, getLocale()), edusharingProvider, UPLOAD_MIME_TYPE,
				SELECTION_SUFFIX, null);
		listenTo(combiLinkCtr);
		configvc.put("combiCtr", combiLinkCtr.getInitialComponent());		
		configvc.contextPut("editorEnabled", combiLinkCtr.isEditorEnabled());

		// Security configuration form
		boolean allowCoachEdit = stNode.getModuleConfiguration().getBooleanSafe(CONFIG_KEY_ALLOW_COACH_EDIT, false);
		securitySettingForm = new SecuritySettingsForm(ureq, getWindowControl(), allowRelativeLinks, allowCoachEdit);
		listenTo(securitySettingForm);
		configvc.put("allowRelativeLinksForm", securitySettingForm.getInitialComponent());
		
		// Add options tab
		if(myTabbedPane != null) {
			if(!myTabbedPane.containsTab(deliveryOptionsCtrl.getInitialComponent())) {
				myTabbedPane.addTab(translate(PANE_TAB_DELIVERYOPTIONS), deliveryOptionsCtrl.getInitialComponent());
			}
		}
	}

	private void removeCustomFileConfigFromView() {
		// Remove options tab
		if(myTabbedPane != null) {
			if(myTabbedPane.containsTab(deliveryOptionsCtrl.getInitialComponent())) {
				myTabbedPane.removeTab(deliveryOptionsCtrl.getInitialComponent());
			}
		}
		// Remove combi link
		if (combiLinkCtr != null) {
			configvc.remove(combiLinkCtr.getInitialComponent());	
			removeAsListenerAndDispose(combiLinkCtr);
			combiLinkCtr = null;			
		}
		// Remove security settings form
		if (securitySettingForm != null) {			
			configvc.remove(securitySettingForm.getInitialComponent());	
			removeAsListenerAndDispose(securitySettingForm);
			securitySettingForm = null;		
		}
	}
	
	private void updateHighscoreTab() {
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(new CourseEntryRef(euce), stNode);
		myTabbedPane.setEnabled(highScoreTabPos, Mode.none != assessmentConfig.getScoreMode());
	}
	
	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_ST_CONFIG), "o_sel_st_config", configvc);
		if (conventionalAssessmentConfigCtrl != null) {
			tabbedPane.addTab(translate("pane.tab.st_scorecalculation"), "o_sel_st_score", conventionalAssessmentConfigCtrl.getInitialComponent());
		}
		highScoreTabPos = tabbedPane.addTab(translate(PANE_TAB_HIGHSCORE), highScoreNodeConfigController.getInitialComponent());
		updateHighscoreTab();

		if(editorEnabled) {
			tabbedPane.addTab(translate(PANE_TAB_DELIVERYOPTIONS), deliveryOptionsCtrl.getInitialComponent());
		}
	}

	/**
	 * @param mc The module confguration
	 * @return The configured file name
	 */
	public static String getFileName(ModuleConfiguration mc) {
		return (String) mc.get(CONFIG_KEY_FILE);
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}

}