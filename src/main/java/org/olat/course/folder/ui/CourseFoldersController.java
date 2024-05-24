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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.services.folder.ui.FolderController;
import org.olat.core.commons.services.folder.ui.FolderControllerConfig;
import org.olat.core.commons.services.folder.ui.FolderUIFactory;
import org.olat.core.commons.services.folder.ui.event.FolderRootEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VirtualContainer;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.folder.CourseContainerOptions;
import org.olat.course.run.CourseRuntimeController;
import org.olat.course.tree.CourseInternalLinkTreeModel;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 24 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CourseFoldersController extends BasicController implements Activateable2 {

	private static final String CMD_OPEN = "open";
	
	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackedPanel;
	private final List<Link> links;

	private FolderController folderCtrl;

	private final RepositoryEntry repositoryEntry;
	private final ICourse course;
	private final boolean overrideReadOnly;
	private final FolderControllerConfig config;
	private int counter = 0;

	public CourseFoldersController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackedPanel,
			RepositoryEntry repositoryEntry, ICourse course, boolean overrideReadOnly) {
		super(ureq, wControl, Util.createPackageTranslator(CourseRuntimeController.class, ureq.getLocale()));
		this.stackedPanel = stackedPanel;
		this.repositoryEntry = repositoryEntry;
		this.course = course;
		this.overrideReadOnly = overrideReadOnly;
		
		velocity_root = Util.getPackageVelocityRoot(FolderUIFactory.class);
		mainVC = createVelocityContainer("browser_mega_buttons");
		putInitialPanel(mainVC);
		
		CustomLinkTreeModel customLinkTreeModel = new CourseInternalLinkTreeModel(course.getEditorTreeModel());
		config = FolderControllerConfig.builder()
				.withFileHub(true)
				.withCustomLinkTreeModel(customLinkTreeModel)
				.build();
		
		links = new ArrayList<>();
		links.add(createLink(null, translate("command.coursefolder")));
		
		course.getCourseFolderContainer(ureq.getUserSession().getIdentityEnvironment(),
				CourseContainerOptions.withoutCourseFolder(), overrideReadOnly, Boolean.TRUE)
				.getItems().stream()
				.sorted((i1, i2) -> i1.getName().compareToIgnoreCase(i2.getName()))
				.map(i -> createLink((VFSContainer)i, i.getName()))
				.forEach(link -> links.add(link));
		mainVC.contextPut("links", links);
	}

	private Link createLink(VFSContainer vfsContainer, String name) {
		Link link = LinkFactory.createCustomLink("cont_" + counter++, CMD_OPEN, null, Link.LINK_CUSTOM_CSS + Link.NONTRANSLATED, mainVC, this);
		link.setElementCssClass("btn btn-default o_button_mega o_sel_" + name.replace(" ", "_"));
		link.setIconLeftCSS("o_icon o_icon-xl " + "o_filetype_folder");
		String text = "<div class=\"o_mega_headline\">" + name + "</div>";
		text += "<div class=\"o_mega_subline\">" + "</div>";
		link.setCustomDisplayText(text);
		link.setUserObject(vfsContainer);
		return link;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String path = BusinessControlFactory.getInstance().getPath(entries.get(0));
		if (StringHelper.containsNonWhitespace(path)) {
			String[] pathParts = path.split("/");
			if (pathParts.length >= 2) {
				String containerName = pathParts[1];
				if ("coursefolder".equals(containerName)) {
					doOpenCourseFolder(ureq);
				} else {
					for (Link link: links) {
						if (link.getUserObject() instanceof VFSContainer vfsContainer) {
							if (containerName.equalsIgnoreCase(vfsContainer.getName())) {
								doOpen(ureq, (VFSContainer)link.getUserObject());
							}
						}
					}
				}
				if (folderCtrl != null) {
					folderCtrl.activate(ureq, entries, state);
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link) {
			String command = link.getCommand();
			if (CMD_OPEN.equals(command)) {
				if (link.getUserObject() instanceof VFSContainer vfsContainer) {
					doOpen(ureq, vfsContainer);
				} else {
					doOpenCourseFolder(ureq);
				}
			}
		}
	}
	
	private void doOpenCourseFolder(UserRequest ureq) {
		// Load course folder directly to get full access
		VFSContainer courseContainer = CourseFactory.loadIsolatedCourseFolder(repositoryEntry);
		VFSContainer namedCourseContainer = new NamedContainerImpl(translate("command.coursefolder"), courseContainer);
		VirtualContainer courseFolder = new VirtualContainer(translate("command.coursefiles"));
		courseFolder.addItem(namedCourseContainer);
		
		folderCtrl = new FolderController(ureq, getWindowControl(), courseFolder, config);
		listenTo(folderCtrl);
		folderCtrl.updateCurrentContainer(ureq, namedCourseContainer, true);
		
		stackedPanel.pushController(courseFolder.getName(), folderCtrl);
		stackedPanel.setInvisibleCrumb(2);
	}

	private void doOpen(UserRequest ureq, VFSContainer vfsContainer) {
		VFSContainer courseContainer = course.getCourseFolderContainer(ureq.getUserSession().getIdentityEnvironment(),
				CourseContainerOptions.withoutCourseFolder(), overrideReadOnly, Boolean.TRUE);
		VFSContainer namedCourseContainer = new NamedContainerImpl(translate("command.coursefiles"), courseContainer);
		folderCtrl = new FolderController(ureq, getWindowControl(), namedCourseContainer, config);
		listenTo(folderCtrl);
		folderCtrl.updateCurrentContainer(ureq, vfsContainer.getName(), true);
		
		stackedPanel.pushController(namedCourseContainer.getName(), folderCtrl);
		stackedPanel.setInvisibleCrumb(2);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == folderCtrl) {
			if (event == FolderRootEvent.EVENT) {
				stackedPanel.setInvisibleCrumb(1);
				stackedPanel.popUpToRootController(ureq);
			}
		}
		super.event(ureq, source, event);
	}

}
