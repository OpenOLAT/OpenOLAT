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
package org.olat.course.nodes.pf.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.download.DownloadComponent;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.springframework.beans.factory.annotation.Autowired;
/**
*
* @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
*
*/
public class PFPeekviewController extends BasicController {
	
	// comparator to sort the messages list by creation date
	private static final Comparator<VFSLeaf> dateSortingComparator = (leaf1, leaf2) ->
		 Long.compare(leaf2.getLastModified(), leaf1.getLastModified()); //last first
	// the current course node id
	private final String nodeId;

	private static final VFSItemFilter attachmentExcludeFilter = new VFSSystemItemFilter();

	@Autowired
	private FolderModule folderModule;
	
	public PFPeekviewController(UserRequest ureq, WindowControl wControl, List<VFSContainer> folders, String nodeId, int itemsToDisplay) {
		super(ureq, wControl);
		this.nodeId = nodeId;		

		VelocityContainer peekviewVC = createVelocityContainer("peekview");
		// add items, only as many as configured
		List<VFSLeaf> allLeafs = new ArrayList<>();
		for(VFSContainer rootFolder:folders) {
			addItems(rootFolder, allLeafs);
		}
		
		// Sort messages by last modified date
		Collections.sort(allLeafs, dateSortingComparator);
		boolean forceDownload = folderModule.isForceDownload();
		
		// only take the configured amount of messages
		List<VFSLeaf> leafs = new ArrayList<>();
		for (int i = 0; i<allLeafs.size() && i<itemsToDisplay; i++) {
			VFSLeaf leaf = allLeafs.get(i);
			leafs.add(leaf);
			// add link to item
			// Add link to jump to course node
			if (leaf instanceof LocalFileImpl) {
				DownloadComponent dlComp = new DownloadComponent("nodeLinkDL_"+(i+1), leaf, forceDownload,
						leaf.getName() + " " + new Date(leaf.getLastModified()), translate("peekview.downloadfile"),
						CSSHelper.createFiletypeIconCssClassFor(leaf.getName()));
				dlComp.setElementCssClass("o_gotoNode");
				peekviewVC.put("nodeLinkDL_"+(i+1),dlComp);
			}
		}
		peekviewVC.contextPut("leafs", leafs);
		// Add link to show all items (go to node)
		Link allItemsLink = LinkFactory.createLink("peekview.allItemsLink", peekviewVC, this);
		allItemsLink.setIconRightCSS("o_icon o_icon_start");
		allItemsLink.setElementCssClass("pull-right");
		putInitialPanel(peekviewVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link nodeLink = (Link) source;
			String relPath = (String) nodeLink.getUserObject();
			if (relPath == null) {
				fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, nodeId));								
			} else {
				fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, nodeId + "/" + relPath));				
			}
		}
	}
	
	private void addItems(VFSContainer container, List<VFSLeaf> allLeafs) {
		// exclude files which are also excluded in FolderComponent
		for (VFSItem vfsItem : container.getItems(attachmentExcludeFilter)) {
			if (vfsItem instanceof VFSLeaf) {
				allLeafs.add((VFSLeaf)vfsItem);
			} else if (vfsItem instanceof VFSContainer) {
				// do it recursively for all children
				addItems((VFSContainer)vfsItem, allLeafs);
			}
		}
	}
}
