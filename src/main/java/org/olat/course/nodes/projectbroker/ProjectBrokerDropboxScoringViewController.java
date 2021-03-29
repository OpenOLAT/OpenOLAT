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

package org.olat.course.nodes.projectbroker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.filters.VFSContainerFilter;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.nodes.projectbroker.service.ProjectGroupManager;
import org.olat.course.nodes.ta.DropboxController;
import org.olat.course.nodes.ta.DropboxScoringViewController;
import org.olat.course.nodes.ta.ReturnboxController;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * @author Christian Guretzki
 */

public class ProjectBrokerDropboxScoringViewController extends DropboxScoringViewController {

	private Project project;
	private final ProjectGroupManager projectGroupManager;
	
	/**
	 * Scoring view of the dropbox.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param node
	 * @param userCourseEnv
	 */
	public ProjectBrokerDropboxScoringViewController(Project project, UserRequest ureq, WindowControl wControl, CourseNode node, UserCourseEnvironment userCourseEnv) { 
		super(ureq, wControl, node, userCourseEnv, false);	
		this.project = project;
		projectGroupManager = CoreSpringFactory.getImpl(ProjectGroupManager.class);
		setVelocityRoot(Util.getPackageVelocityRoot(DropboxScoringViewController.class));
		Translator fallbackTranslator = Util.createPackageTranslator(this.getClass(), getLocale());
		Translator myTranslator = Util.createPackageTranslator(DropboxScoringViewController.class, getLocale(), fallbackTranslator);
		setTranslator(myTranslator);
		boolean hasNotification = projectGroupManager.isProjectManagerOrAdministrator(ureq, userCourseEnv, project);
		init(ureq, hasNotification);
	}
	
	@Override
	protected VFSContainer getDropboxFilePath(Identity assesseeIdentity) {
		String path = DropboxController.getDropboxPathRelToFolderRoot(userCourseEnv.getCourseEnvironment(), node)
				+ File.separator + project.getKey();
		VFSContainer rootContainer = VFSManager.olatRootContainer(path, null);
		List<VFSItem> items = rootContainer.getItems(new VFSContainerFilter());
		List<VFSContainer> namedContainers = new ArrayList<>(items.size());
		VFSSecurityCallback readOnly = getDropboxVfsSecurityCallback();
		for(VFSItem item: items) {
			if(item instanceof VFSContainer) {
				String name = StringHelper.escapeHtml(userManager.getUserDisplayName(item.getName()));
				NamedContainerImpl named = new NamedContainerImpl(name, (VFSContainer)item);
				named.setLocalSecurityCallback(readOnly);
				namedContainers.add(named);
			}
		}
		
		MergeSource container = new BoxSource(rootContainer, translate("dropbox.title"), namedContainers);
		container.setLocalSecurityCallback(readOnly);
		return container;
	}

	@Override
	protected VFSContainer getReturnboxFilePath(Identity assesseeIdentity) {
		String path = ReturnboxController.getReturnboxPathRelToFolderRoot(userCourseEnv.getCourseEnvironment(), node) 
				+ File.separator + project.getKey();
		VFSContainer rootContainer = VFSManager.olatRootContainer(path, null);
		List<VFSItem> items = rootContainer.getItems(new VFSContainerFilter());
		List<VFSContainer> namedContainers = new ArrayList<>(items.size());
		VFSSecurityCallback secCallback = getReturnboxVfsSecurityCallback(path, assesseeIdentity);
		for(VFSItem item: items) {
			if(item instanceof VFSContainer) {
				String name = StringHelper.escapeHtml(userManager.getUserDisplayName(item.getName()));
				NamedContainerImpl named = new NamedContainerImpl(name, (VFSContainer)item);
				named.setLocalSecurityCallback(secCallback);
				namedContainers.add(named);
			}
		}
		
		MergeSource container = new BoxSource(rootContainer, translate("returnbox.title"), namedContainers);
		container.setLocalSecurityCallback(secCallback);
		return container;
	}

	@Override
	protected VFSSecurityCallback getDropboxVfsSecurityCallback() {
		return new ReadOnlyCallback();
	}
	
	private static class BoxSource extends MergeSource {
		
		public BoxSource(VFSContainer rootContainer, String name, List<VFSContainer> containers) {
			super(rootContainer, name);
			setMergedContainers(containers);
		}
	}
}