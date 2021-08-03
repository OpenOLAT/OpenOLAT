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

package org.olat.course.nodes.sp;

import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.modules.singlepage.SinglePageController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlsite.NewInlineUriEvent;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.nodes.SPCourseNode;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.run.tools.CourseToolLinkTreeModel;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseInternalLinkTreeModel;
import org.olat.modules.ModuleConfiguration;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<br>
 * Displays a regular HTML page either in an iframe or integrated within OLAT.
 * If the user is course administrator or has course editor rights an edit links
 * allows the editing of the page.
 * <p>
 * EVENTS: to listening controllers: - OlatCmdEvent (which has to be accepted by
 * calling accept() on the event) A run controller for single page course nodes
 * Initial Date: Oct 12, 2004
 * 
 * @author Felix Jost
 */
public class SPRunController extends BasicController implements Activateable2 {
	
	private SPCourseNode courseNode;
	private Panel main;
	private SinglePageController spCtr;
	private ModuleConfiguration config;
		
	private VFSContainer courseFolderContainer;
	private String fileName;
	
	private final boolean hasEditRights;
	private CustomLinkTreeModel linkTreeModel;
	private CustomLinkTreeModel toolLinkTreeModel;
	private Long repoKey;

	private final UserCourseEnvironment userCourseEnv;
	
	private static final String[] EDITABLE_TYPES = new String[] { "html", "htm", "xml", "xhtml" };
	
	/**
	 * Constructor for single page run controller 
	 * @param wControl
	 * @param ureq
	 * @param userCourseEnv
	 * @param courseNode
	 * @param courseFolderPath The course folder which contains the single page html file
	 */
	public SPRunController(WindowControl wControl, UserRequest ureq,
			UserCourseEnvironment userCourseEnv, SPCourseNode courseNode, VFSContainer courseFolderContainer) {
		super(ureq,wControl);
		this.courseNode = courseNode;
		this.config = courseNode.getModuleConfiguration();
		this.userCourseEnv = userCourseEnv;
		this.repoKey = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry().getKey();
				
		addLoggingResourceable(LoggingResourceable.wrap(courseNode));

		// set up single page init parameters
		fileName = (String)config.get(SPEditController.CONFIG_KEY_FILE);
		if (fileName == null) throw new AssertException("bad configuration at lauchtime: fileName cannot be null in SinglePage!");
		this.courseFolderContainer = courseFolderContainer;
		
		hasEditRights = hasEditRights();

		if (hasEditRights) {
			linkTreeModel = new CourseInternalLinkTreeModel(userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode());
			toolLinkTreeModel = new CourseToolLinkTreeModel(userCourseEnv.getCourseEnvironment().getCourseConfig(), getLocale());
		}
		
		// init main panel and do start page or direct launch
		main = new Panel("sprunmain");
		doInlineIntegration(ureq, hasEditRights);		
		putInitialPanel(main);
	}
	
	private boolean hasEditRights() {
		if(userCourseEnv.isCourseReadOnly()) return false;
		
		if(fileName != null && fileName.startsWith("/_sharedfolder")) {
			if(userCourseEnv.getCourseEnvironment().getCourseConfig().isSharedFolderReadOnlyMount()) {
				return false;
			}
		}
		
		if(isFileTypeEditable(fileName)) {
			CourseGroupManager cgm = userCourseEnv.getCourseEnvironment().getCourseGroupManager();
			GroupRoles role = GroupRoles.owner;
			if (userCourseEnv.isParticipant()) {
				role = GroupRoles.participant;
			} else if (userCourseEnv.isCoach()) {
				role = GroupRoles.coach;
			}
			return (config.getBooleanSafe(SPEditController.CONFIG_KEY_ALLOW_COACH_EDIT, false) && userCourseEnv.isCoach())
					|| userCourseEnv.isAdmin() || cgm.hasRight(getIdentity(), CourseRights.RIGHT_COURSEEDITOR, role);

		}
		return false;
	}
	
	private boolean isFileTypeEditable(String filename) {
		String name = filename.toLowerCase();
		for (int i = 0; i < EDITABLE_TYPES.length; i++) {
			if (name.endsWith("." + EDITABLE_TYPES[i]) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == spCtr) {
			if (event instanceof OlatCmdEvent) {
				// refire to listening controllers
				fireEvent(ureq, event);
			} else if (event instanceof NewInlineUriEvent) {
				//do nothing
			}
		}
	}

	private void doInlineIntegration(UserRequest ureq, boolean hasEditRightsTo) {
		boolean allowRelativeLinks = config.getBooleanSafe(SPEditController.CONFIG_KEY_ALLOW_RELATIVE_LINKS);
		// create the possibility to float
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ICourse.class, userCourseEnv.getCourseEnvironment().getCourseResourceableId());

		DeliveryOptions deliveryOptions = (DeliveryOptions)config.get(SPEditController.CONFIG_KEY_DELIVERYOPTIONS);
		spCtr = new SinglePageController(ureq, getWindowControl(), courseFolderContainer, fileName,
				allowRelativeLinks, null, ores, deliveryOptions, userCourseEnv.getCourseEnvironment().isPreview(), repoKey);
		spCtr.setAllowDownload(true);
		
		// only for inline integration: register for controller event to forward a olatcmd to the course,
		// and also to remember latest position in the script		
		listenTo(spCtr);
		// enable edit mode if user has the according rights
		if (hasEditRightsTo) {
			spCtr.allowPageEditing();
			// set the link tree model to internal for the HTML editor
			if (linkTreeModel != null) {
				spCtr.setInternalLinkTreeModel(linkTreeModel);
			}
			if (toolLinkTreeModel != null) {
				spCtr.setToolLinkTreeModel(toolLinkTreeModel);
			}
		}		

		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, getWindowControl(), spCtr, userCourseEnv, courseNode, "o_sp_icon");
		main.setContent(ctrl.getInitialComponent());
	}
	
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty() || spCtr == null) return;
		// delegate to single page controller
		spCtr.activate(ureq, entries, state);
	}

	
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		//child controller registered with listenTo gets disposed in BasicController
	}

}