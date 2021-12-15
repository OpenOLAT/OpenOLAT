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

package org.olat.course.nodes.cp;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.course.editor.NodeEditController;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CPCourseNode;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.cp.CPManager;
import org.olat.ims.cp.ui.CPPackageConfig;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.cp.CPAssessmentProvider;
import org.olat.modules.cp.CPDisplayController;
import org.olat.modules.cp.CPManifestTreeModel;
import org.olat.modules.cp.CPUIFactory;
import org.olat.modules.cp.DryRunAssessmentProvider;
import org.olat.modules.cp.PersistingAssessmentProvider;
import org.olat.modules.cp.TreeNodeEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: <BR/>Run controller for content packaging course nodes <P/>
 * 
 * Initial Date: Oct 13, 2004
 * @author Felix Jost
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class CPRunController extends BasicController implements ControllerEventListener, Activateable2 {
	private static final Logger log = Tracing.createLoggerFor(CPRunController.class);

	private ModuleConfiguration config;
	private File cpRoot;
	private Panel main;
	private Link showCPButton;
	
	private CPDisplayController cpDispC;
	private CPCourseNode cpNode;
	
	// for external menu representation
	private CPManifestTreeModel treeModel;
	private ControllerEventListener treeNodeClickListener;
	private String nodecmd;
	private String selNodeId;
	private boolean preview;
	private OLATResourceable courseResource;
	private final UserCourseEnvironment userCourseEnv;
	private CPAssessmentProvider cpAssessmentProvider;
	
	@Autowired
	private CPManager cpManager;

	/**
	 * Use this constructor to launch a CP via Repository reference key set in the
	 * ModuleConfiguration. On the into page a title and the learning objectives
	 * can be placed.
	 * 
	 * @param config
	 * @param ureq
	 * @param userCourseEnv
	 * @param wControl
	 * @param cpNode
	 * @param userCourseEnv 
	 */
	public CPRunController(ModuleConfiguration config, UserRequest ureq, WindowControl wControl, CPCourseNode cpNode, String nodecmd,
			OLATResourceable course, boolean preview, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		this.nodecmd = nodecmd;
		this.courseResource = OresHelper.clone(course);
		// assertion to make sure the moduleconfig is valid
		if (!CPEditController.isModuleConfigValid(config)) throw new AssertException("cprun controller had an invalid module config:"	+ config.toString());
		this.config = config;
		this.cpNode = cpNode;
		this.preview = preview;
		this.userCourseEnv = userCourseEnv;
		addLoggingResourceable(LoggingResourceable.wrap(cpNode));

		// jump to either the forum or the folder if the business-launch-path says so.
		BusinessControl bc = getWindowControl().getBusinessControl();
		ContextEntry ce = bc.popLauncherContextEntry();
		if ( ce != null ) { // a context path is left for me
			log.debug("businesscontrol (for further jumps) would be:{}", bc);
			OLATResourceable popOres = ce.getOLATResourceable();
			log.debug("OLATResourceable={}", popOres);
			String typeName = popOres.getResourceableTypeName();
			// typeName format: 'path=/test1/test2/readme.txt'
			// First remove prefix 'path='
			String path = typeName.substring("path=".length());
			if  (path.length() > 0) {
			  log.debug("direct navigation to container-path={}", path);
			  this.nodecmd = path;
			}
		}

		main = new Panel("cprunmain");
		doLaunch(ureq);
		putInitialPanel(main);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == showCPButton) { // those must be links
			fireEvent(ureq, Event.CHANGED_EVENT);
			doLaunch(ureq);
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == null) { // external source (from the course at this time being)
			if (event instanceof TreeEvent) {
				cpDispC.switchToPage(ureq, (TreeEvent)event);
			}
		}
		else if (source == cpDispC && treeNodeClickListener != null && (event instanceof TreeNodeEvent)) {
			// propagate TreeNodeEvent to the listener
			fireEvent(ureq, event);
		}
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		cpDispC.activate(ureq, entries, state);
	}

	private void doLaunch(UserRequest ureq) {
		DeliveryOptions deliveryOptions = (DeliveryOptions)config.get(CPEditController.CONFIG_DELIVERYOPTIONS);
		
		if (cpRoot == null) {
			// it is the first time we start the contentpackaging from this instance
			// of this controller.
			// need to be strict when launching -> "true"
			RepositoryEntry re = CPEditController.getCPReference(config, false);
			if (re == null) {
				showError(CPEditController.NLS_ERROR_CPREPOENTRYMISSING);
				return;
			}
			cpRoot = FileResourceManager.getInstance().unzipFileResource(re.getOlatResource());
			// should always exist because references cannot be deleted as long as
			// nodes reference them
			if (cpRoot == null) {
				showError(CPEditController.NLS_ERROR_CPREPOENTRYMISSING);
				return;
			}
			
			if(deliveryOptions != null && deliveryOptions.getInherit() != null && deliveryOptions.getInherit().booleanValue()) {
				CPPackageConfig packageConfig = cpManager.getCPPackageConfig(re.getOlatResource());
				if(packageConfig != null && packageConfig.getDeliveryOptions() != null) {
					deliveryOptions = packageConfig.getDeliveryOptions();
				}
			}
			
			boolean learningPath = LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(userCourseEnv).getType())
					&& (userCourseEnv.getCourseEnvironment().getCourseConfig().isMenuPathEnabled()
							|| userCourseEnv.isParticipant());
			cpAssessmentProvider = userCourseEnv.getIdentityEnvironment().getRoles().isGuestOnly()
					? DryRunAssessmentProvider.create()
					: PersistingAssessmentProvider.create(re, getIdentity(), learningPath, userCourseEnv.isParticipant());
		}
		// else cpRoot is already set (save some db access if the user opens /
		// closes / reopens the cp from the same CPRuncontroller instance)
		boolean activateFirstPage = true;
		if ( (nodecmd != null) && !nodecmd.equals("") ) {
 		  activateFirstPage = false; 
		}
		boolean showNavigation = !config.getBooleanSafe(NodeEditController.CONFIG_COMPONENT_MENU);
		
		cpDispC = CPUIFactory.getInstance().createContentOnlyCPDisplayController(ureq, getWindowControl(),
				new LocalFolderImpl(cpRoot), activateFirstPage, showNavigation, deliveryOptions, nodecmd,
				courseResource, cpNode.getIdent(), preview, cpAssessmentProvider);
		cpDispC.setContentEncoding(deliveryOptions.getContentEncoding());
		cpDispC.setJSEncoding(deliveryOptions.getJavascriptEncoding());
		cpDispC.addControllerListener(this);

		main.setContent(cpDispC.getInitialComponent());
		if (isExternalMenuConfigured()) {
			treeModel = cpDispC.getTreeModel();
			treeNodeClickListener = this;
			if(activateFirstPage) {
				selNodeId = cpDispC.getInitialSelectedNodeId();
			} else {
				String uri = nodecmd;
				if(uri.startsWith("/")) {
					uri = uri.substring(1, uri.length());
				}
				selNodeId = cpDispC.getNodeByUri(uri);
			}
		}
	}

	/**
	 * @return true if there is a treemodel and an event listener ready to be used
	 *         in outside this controller
	 */
	private boolean isExternalMenuConfigured() {
		return (config.getBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU).booleanValue());
	}
	
	@Override
	protected void doDispose() {
		if (cpDispC != null) {
			cpDispC.dispose();
			cpDispC = null;
		}
        super.doDispose();
	}

	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, String selectedNodeId) {
		NodeRunConstructionResult ncr;
		if (isExternalMenuConfigured()) {
			// integrate it into the olat menu
			Controller ctrl = TitledWrapperHelper.getWrapper(ureq, getWindowControl(), this, userCourseEnv, cpNode, "o_cp_icon");
			if(treeModel.getFlattedTree().size() == 1) {
				selNodeId = cpNode.getIdent();
			} else if(StringHelper.containsNonWhitespace(selectedNodeId) && treeModel.getNodeById(selectedNodeId) != null) {
				selNodeId = selectedNodeId;
			}
			ncr = new NodeRunConstructionResult(ctrl, treeModel, selNodeId, treeNodeClickListener);
		} else { // no menu to integrate
			Controller ctrl = TitledWrapperHelper.getWrapper(ureq, getWindowControl(), this, userCourseEnv, cpNode, "o_cp_icon");
			ncr = new NodeRunConstructionResult(ctrl);
		}
		return ncr;
	}
}