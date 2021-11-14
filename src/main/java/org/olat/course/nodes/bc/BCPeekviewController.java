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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryModule;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.ui.media.VFSMetadataMediaResource;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.download.DownloadComponent;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <h3>Description:</h3> The folder peekview controller displays the configurable
 * amount of the newest files in this briefcase
 * <p>
 * <h4>Events fired by this Controller</h4>
 * <ul>
 * <li>OlatCmdEvent to notify that a jump to the course node is desired</li>
 * </ul>
 * <p>
 * Initial Date: 29.09.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class BCPeekviewController extends BasicController implements Controller {
	
	private int count = 0;
	private final String nodeId;
	private final boolean forceDownload;
	
	private final VelocityContainer mainVC;

	@Autowired
	private FolderModule folderModule;
	@Autowired
	private VFSRepositoryModule vfsModule;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	/**
	 * Constructor
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param rootFolder The root folder of this briefcase
	 * @param nodeId The course node ID
	 * @param itemsToDisplay number of items to be displayed, must be > 0
	 */
	public BCPeekviewController(UserRequest ureq, WindowControl wControl, VFSContainer rootFolder, String nodeId, int itemsToDisplay) {		
		super(ureq, wControl);
		this.nodeId = nodeId;
		mainVC = createVelocityContainer("peekview");
		forceDownload = folderModule.isForceDownload();
		
		List<DownloadComponent> links = new ArrayList<>(itemsToDisplay);
		
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(rootFolder);
		// don't force migration here to prevent overloading big OpenOLAT instances
		if(vfsModule.isMigrated() || "migrated".equals(metadata.getMigrated())) {
			fileMetadata(links, metadata, itemsToDisplay);
		} else {
			fileFallback(links, rootFolder, itemsToDisplay);
		}
		mainVC.contextPut("links", links);
		
		// Add link to show all items (go to node)
		Link allItemsLink = LinkFactory.createLink("peekview.allItemsLink", mainVC, this);
		allItemsLink.setIconRightCSS("o_icon o_icon_start");
		allItemsLink.setElementCssClass("pull-right");
		putInitialPanel(mainVC);
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
	
	private void fileMetadata(List<DownloadComponent> links, VFSMetadata metadata, int itemsToDisplay) {
		List<VFSMetadata> newestData = vfsRepositoryService.getNewest(metadata, itemsToDisplay);
		for(VFSMetadata newData:newestData) {
			if (newData.isDeleted()) continue;
			String name = "nodeLinkDL_"+ (count++);
			VFSMetadataMediaResource media = new VFSMetadataMediaResource(newData);
			media.setDownloadable(forceDownload);
			DownloadComponent dlComp = new DownloadComponent(name, media, newData.getFilename(),
					translate("preview.downloadfile"), CSSHelper.createFiletypeIconCssClassFor(newData.getFilename()));
			dlComp.setElementCssClass("o_gotoNode");
			mainVC.put(name, dlComp);
			links.add(dlComp);
		}
	}
	
	private void fileFallback(List<DownloadComponent> links, VFSContainer rootFolder, int itemsToDisplay) {
		List<VFSLeaf> leafs = collectFiles(rootFolder, itemsToDisplay);
		for(VFSLeaf leaf:leafs) {
			String name = "nodeLinkDL_"+ (count++);
			DownloadComponent dlComp = new DownloadComponent(name, leaf, forceDownload, leaf.getName(),
					translate("preview.downloadfile"), CSSHelper.createFiletypeIconCssClassFor(leaf.getName()));
			dlComp.setElementCssClass("o_gotoNode");
			mainVC.put(name, dlComp);
			links.add(dlComp);
		}
	}
	
	private List<VFSLeaf> collectFiles(VFSContainer rootFolder, int itemsToDisplay) {
		// add items, only as many as configured
		List<VFSLeaf> allLeafs = new ArrayList<>();
		addItems(rootFolder, allLeafs);
		// Sort messages by last modified date
		Collections.sort(allLeafs, new LastModifiedcomparator());
		
		int size = Math.min(allLeafs.size(), itemsToDisplay);
		List<VFSLeaf> lastLeafs = allLeafs.subList(0, size);
		return new ArrayList<>(lastLeafs);
	}

	/**
	 * Private helper method to get all files in a directory. Traverses the directory tree recursively
	 * @param container
	 * @param allLeafs
	 */
	private void addItems(VFSContainer container, List<VFSLeaf> allLeafs) {
		for (VFSItem vfsItem : container.getItems(new VFSSystemItemFilter())) {
			if (vfsItem instanceof VFSLeaf) {
				allLeafs.add((VFSLeaf)vfsItem);
			} else if (vfsItem instanceof VFSContainer) {
				VFSContainer childContainer = (VFSContainer) vfsItem;
				addItems(childContainer, allLeafs);
			}
		}
	}
	
	private static class LastModifiedcomparator implements Comparator<VFSLeaf> {
		@Override
		public int compare(final VFSLeaf leaf1, final VFSLeaf leaf2) {
			return Long.compare(leaf2.getLastModified(), leaf1.getLastModified());
		}
	}
}
