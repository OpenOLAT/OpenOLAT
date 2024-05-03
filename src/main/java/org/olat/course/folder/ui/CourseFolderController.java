/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.folder.ui;

import java.util.List;

import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.services.folder.ui.FolderController;
import org.olat.core.commons.services.folder.ui.FolderControllerConfig;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.ICourse;
import org.olat.course.folder.CourseContainerOptions;
import org.olat.course.run.CourseRuntimeController;
import org.olat.course.tree.CourseInternalLinkTreeModel;

/**
 * 
 * Initial date: 3 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CourseFolderController extends BasicController implements Activateable2 {
	
	public static final Event OPEN_USAGE = new Event("open-usage");

	private Link showMemoryUsageLink;
	
	private final FolderController folderCtrl;

	public CourseFolderController(UserRequest ureq, WindowControl wControl, ICourse course, boolean overrideReadOnly) {
		super(ureq, wControl, Util.createPackageTranslator(CourseRuntimeController.class, ureq.getLocale()));
		
		VelocityContainer mainVC = createVelocityContainer("course_folder");
		putInitialPanel(mainVC);
		
		VFSContainer courseContainer = course.getCourseFolderContainer(ureq.getUserSession().getIdentityEnvironment(),
				CourseContainerOptions.all(), overrideReadOnly, Boolean.TRUE);
		VFSContainer namedCourseFolder = new NamedContainerImpl(translate("command.coursefolder"), courseContainer);
		CustomLinkTreeModel customLinkTreeModel = new CourseInternalLinkTreeModel(course.getEditorTreeModel());
		
		FolderControllerConfig config = FolderControllerConfig.builder()
				.withCustomLinkTreeModel(customLinkTreeModel)
				.build();
		folderCtrl = new FolderController(ureq, wControl, namedCourseFolder, config);
		listenTo(folderCtrl);
		mainVC.put("folder", folderCtrl.getInitialComponent());
	}

	public void initToolbar(TooledStackedPanel stackPanel) {
		showMemoryUsageLink = LinkFactory.createToolLink("viewMemoryUsage", translate("command.memory.usage"), this, "o_icon_hdd");
		stackPanel.addTool(showMemoryUsageLink, TooledStackedPanel.Align.right, false);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		folderCtrl.activate(ureq, entries, state);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == showMemoryUsageLink) {
			fireEvent(ureq, OPEN_USAGE);
		}
	}

}
