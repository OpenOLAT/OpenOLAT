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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.style.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ConfigurationChangedListener;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.clone.CloneableController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.spacesaver.ExpandController;
import org.olat.core.gui.control.generic.spacesaver.ExpandableController;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.style.CourseStyleService;
import org.olat.course.style.Header;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class HeaderContentController extends BasicController
		implements CloneableController, Activateable2, TooledController, ConfigurationChangedListener {
	
	private final VelocityContainer mainVC;
	private final ExpandController collapseCtrl;
	private ExpandableController headerCtrl;
	private final Controller contentCtrl;
	
	private final UserCourseEnvironment userCourseEnv;
	private final CourseNode courseNode;
	private final String iconCssClass;
	
	@Autowired
	private CourseStyleService courseStyleService;


	public HeaderContentController(UserRequest ureq, WindowControl wControl, Controller contentCtrl,
			UserCourseEnvironment userCourseEnv, CourseNode courseNode, String iconCssClass) {
		super(ureq, wControl);
		this.contentCtrl = contentCtrl;
		this.userCourseEnv = userCourseEnv;
		this.courseNode = courseNode;
		this.iconCssClass = iconCssClass;
		
		mainVC = createVelocityContainer("header_content");
		
		collapseCtrl = new ExpandController(ureq, wControl, courseNode.getIdent());
		listenTo(collapseCtrl);
		reloadHeader(ureq);
		
		listenTo(contentCtrl);
		mainVC.put("content", contentCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	public void configurationChanged() {
		if(contentCtrl instanceof ConfigurationChangedListener) {
			((ConfigurationChangedListener)contentCtrl).configurationChanged();
		}
	}

	@Override
	public void initTools() {
		if(contentCtrl instanceof TooledController) {
			((TooledController)contentCtrl).initTools();
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(contentCtrl instanceof Activateable2) {
			((Activateable2)contentCtrl).activate(ureq, entries, state);
		}
	}

	public void reloadHeader(UserRequest ureq) {
		removeAsListenerAndDispose(headerCtrl);
		headerCtrl = null;
		
		Header header = courseStyleService.getHeader(userCourseEnv.getCourseEnvironment(), courseNode, iconCssClass);
		if (header != null && CourseStyleUIFactory.hasValues(header)) {
			headerCtrl = new HeaderController(ureq, getWindowControl(), header);
			listenTo(headerCtrl);
			collapseCtrl.setCollapsibleController(headerCtrl);
			mainVC.put("header", collapseCtrl.getInitialComponent());
		} else {
			mainVC.remove("header");
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		fireEvent(ureq, event);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public Controller cloneController(UserRequest ureq, WindowControl wControl) {
		if(contentCtrl == null || contentCtrl instanceof CloneableController) {
			Controller contentClone = ((CloneableController)contentCtrl).cloneController(ureq, wControl);
			Controller clone = new HeaderContentController(ureq, wControl, contentClone, userCourseEnv, courseNode, iconCssClass);
			return clone;
		}
		return null;
	}

}
