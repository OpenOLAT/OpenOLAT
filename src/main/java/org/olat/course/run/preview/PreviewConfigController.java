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

package org.olat.course.run.preview;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.config.CourseConfig;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class PreviewConfigController extends MainLayoutBasicController {

	private VelocityContainer configVc;
	private PreviewSettingsForm psf;
	private PreviewRunController prc;

	private IdentityEnvironment simIdentEnv;
	private CourseEnvironment simCourseEnv;
	private String role = PreviewSettingsForm.ROLE_STUDENT;
	private LayoutMain3ColsController previewLayoutCtr;
	private final OLATResourceable ores;
	
	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private BusinessGroupService businessGroupService;

	/**
	 * Constructor for the run main controller
	 * 
	 * @param ureq The user request
	 * @param wControl The current window controller
	 * @param course the real course/courseEnvironment (this controller will
	 *          generate a preview-Environment)
	 */
	public PreviewConfigController(UserRequest ureq, WindowControl wControl, ICourse course) { 
		super(ureq, wControl);
		this.ores = course;
		
		psf = new PreviewSettingsForm(ureq, wControl, course);
		listenTo(psf);
		
		configVc = createVelocityContainer("config");
		
		configVc.put("previewsettingsform", psf.getInitialComponent());
		// Use layout wrapper for proper display. Use col3 as main column
		previewLayoutCtr = new LayoutMain3ColsController(ureq, wControl, null, configVc, null);
		previewLayoutCtr.addCssClassToMain("o_preview");
		listenTo(previewLayoutCtr); // for later auto disposal
		StackedPanel initialPanel = putInitialPanel(new SimpleStackedPanel("coursePreviewPanel", "o_edit_mode"));
		initialPanel.setContent(previewLayoutCtr.getInitialComponent());
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == prc && event.getCommand().equals("command.config")) {
			// use config form in preview controller
			previewLayoutCtr.setCol1(null);
			previewLayoutCtr.setCol2(null);
			previewLayoutCtr.setCol3(configVc);
			
		} else if (source == previewLayoutCtr && event == Event.BACK_EVENT) {
			fireEvent(ureq, Event.DONE_EVENT);
			
		} else if (source == psf && event == Event.DONE_EVENT) {
			// start preview as soon as we have valid values
			generateEnvironment();
			
			removeAsListenerAndDispose(prc);
			prc = new PreviewRunController(ureq, getWindowControl(), simIdentEnv, simCourseEnv, role, previewLayoutCtr);
			listenTo(prc);		
			 
		}
	}

	private void generateEnvironment() {
		List<BGArea> tmpAreas = areaManager.loadAreas(psf.getAreaKeys());
		List<BusinessGroup> groups = businessGroupService.loadBusinessGroups(psf.getGroupKeys());
		// get learning areas for groups
		Set<BGArea> areas = new HashSet<>();
		areas.addAll(tmpAreas);
		List<BGArea> areaByGroups = areaManager.findBGAreasOfBusinessGroups(groups);
		areas.addAll(areaByGroups);
		
		role = psf.getRole();
		ICourse course = CourseFactory.loadCourse(ores);
		
		// default is student
		boolean isCoach = false;
		boolean isCourseAdmin = false;
		Roles previewRoles = Roles.userRoles();
		if (role.equals(PreviewSettingsForm.ROLE_GUEST)) {
			previewRoles = Roles.guestRoles();
		} else if (role.equals(PreviewSettingsForm.ROLE_COURSECOACH)) {
			isCoach = true;
		} else if (role.equals(PreviewSettingsForm.ROLE_COURSEADMIN)) {
			isCourseAdmin = true;
		} else if (role.equals(PreviewSettingsForm.ROLE_GLOBALAUTHOR)) {
			previewRoles = Roles.authorRoles();
		}
		
		final RepositoryEntry courseResource = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		final CourseGroupManager cgm = new PreviewCourseGroupManager(courseResource, new ArrayList<>(groups), new ArrayList<>(areas), isCoach, isCourseAdmin);
		final UserNodeAuditManager auditman = new PreviewAuditManager();
		final AssessmentManager am = new PreviewAssessmentManager();
		final CoursePropertyManager cpm = new PreviewCoursePropertyManager();
		final Structure runStructure = course.getEditorTreeModel().createStructureForPreview();
		final String title = course.getCourseTitle();
		final CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();

		simCourseEnv = new PreviewCourseEnvironment(title, runStructure, psf.getDate(), course.getCourseFolderContainer(), course
				.getCourseBaseContainer(),course.getResourceableId(), cpm, cgm, auditman, am, courseConfig);
		simIdentEnv = new IdentityEnvironment();
		
		
		simIdentEnv.setRoles(previewRoles);
		final Identity ident = new PreviewIdentity();
		simIdentEnv.setIdentity(ident);
		//identity must be set before attributes OLAT-4811
		simIdentEnv.setAttributes(psf.getAttributesMap());
	}
}