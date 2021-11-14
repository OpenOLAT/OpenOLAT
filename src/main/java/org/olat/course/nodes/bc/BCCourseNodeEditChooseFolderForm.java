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
package org.olat.course.nodes.bc;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.folder.FolderTreeModel;
import org.olat.core.util.vfs.VFSContainer;

/**
 * Initial Date: Dez 22, 2015
 *
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 */
public class BCCourseNodeEditChooseFolderForm extends BasicController {

	private final FolderTreeModel treeModel;
	private final MenuTree selectionTree;
	private final Link selectLink, cancelLink;
	private String subpath;

	public BCCourseNodeEditChooseFolderForm(UserRequest ureq, WindowControl wControl, VFSContainer namedContainer) {
		super(ureq, wControl);
		VelocityContainer chooseVC = createVelocityContainer("chooseFolder");

		treeModel = new FolderTreeModel(ureq.getLocale(), namedContainer,  true, false, true, true, new VFSFolderNodeFilter());
		selectionTree = new MenuTree("stTree");
		selectionTree.setTreeModel(treeModel);
		selectionTree.addListener(this);
		chooseVC.put("selectionTree", selectionTree);

		selectLink = LinkFactory.createButton("chooseFolder", chooseVC, this);
		cancelLink = LinkFactory.createButton("cancel", chooseVC, this);

		putInitialPanel(chooseVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == cancelLink){
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if(source == selectionTree) {
			TreeEvent te = (TreeEvent)event;
			subpath = treeModel.getSelectedPath(treeModel.getNodeById(te.getNodeId()));
		} else if(source == selectLink){
			fireEvent(ureq, new SelectFolderEvent(subpath));
		}
	}
}
