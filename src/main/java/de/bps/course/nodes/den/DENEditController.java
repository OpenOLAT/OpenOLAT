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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.den;



import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.id.OLATResourceable;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

import de.bps.course.nodes.DENCourseNode;

public class DENEditController extends ActivateableTabbableDefaultController {
	
	public static final String PANE_TAB_DENCONFIG = "pane.tab.denconfig";

	private ModuleConfiguration moduleConfiguration;
	private OLATResourceable ores;
	private DENCourseNode courseNode;
	private VelocityContainer editVc;
	
	private CloseableModalController manageDatesModalCntrll, listParticipantsModalCntrll;
	private Link manageDatesButton, manageParticipantsButton;
	
	private DENEditForm dateFormContr;
	private TabbedPane tabPane;
	final static String[] paneKeys = {PANE_TAB_DENCONFIG};
	
	private final UserCourseEnvironment userCourseEnv;
	
	public DENEditController(ModuleConfiguration moduleConfiguration,
								UserRequest ureq,
								WindowControl wControl,
								DENCourseNode courseNode,
								OLATResourceable ores,
								UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		
		this.moduleConfiguration = moduleConfiguration;
		this.ores = ores;
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		
		editVc = this.createVelocityContainer("edit");
		
		ICourse course = CourseFactory.loadCourse(ores);
		moduleConfiguration.set(DENCourseNode.CONF_COURSE_ID, course.getResourceableId());
		
		dateFormContr = new DENEditForm(ureq, getWindowControl(), this.moduleConfiguration);
		dateFormContr.addControllerListener(this);
		
		manageDatesButton = LinkFactory.createButton("config.dates", editVc, this);
		manageParticipantsButton = LinkFactory.createButton("run.enrollment.list", editVc, this);
		
		editVc.put("dateform", dateFormContr.getInitialComponent());
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return tabPane;
	}

	@Override
	protected void doDispose() {
		if(dateFormContr != null) {
			dateFormContr.dispose();
			dateFormContr = null;
		}
		if(manageDatesModalCntrll != null) {
			manageDatesModalCntrll.dispose();
			manageDatesModalCntrll = null;
		}
		if(listParticipantsModalCntrll != null) {
			listParticipantsModalCntrll.dispose();
			listParticipantsModalCntrll = null;
		}
        super.doDispose();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == dateFormContr) {
			moduleConfiguration = dateFormContr.getModuleConfiguration();
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		} if(source == manageDatesModalCntrll) {
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		} if(source == listParticipantsModalCntrll) {
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		} 
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == manageDatesButton) {
			//management of dates
			DENManageDatesController datesCtr = new DENManageDatesController(ureq, getWindowControl(), ores, courseNode);
			manageDatesModalCntrll = new CloseableModalController(getWindowControl(), "close", datesCtr.getInitialComponent(), true, translate("config.dates"));
			manageDatesModalCntrll.addControllerListener(this);
			manageDatesModalCntrll.activate();
		} else if (source == manageParticipantsButton) {
			//list of participants
			DENManageParticipantsController partsCtr = new DENManageParticipantsController(ureq, getWindowControl(), ores, courseNode, userCourseEnv.isCourseReadOnly());
			listParticipantsModalCntrll = new CloseableModalController(getWindowControl(), "close", partsCtr.getInitialComponent(), true, translate("dates.table.list"));
			listParticipantsModalCntrll.addControllerListener(this);
			listParticipantsModalCntrll.activate();
		}
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		tabPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_DENCONFIG), editVc);
	}
	
	public static boolean isConfigValid(ModuleConfiguration moduleConfig) {
		DENManager manager = DENManager.getInstance();
		Long courseId = (Long)moduleConfig.get(DENCourseNode.CONF_COURSE_ID);
		String courseNodeId = (String)moduleConfig.get(DENCourseNode.CONF_COURSE_NODE_ID);
		
		return (manager.getEventCount(courseId, courseNodeId) != 0);
	}

}
