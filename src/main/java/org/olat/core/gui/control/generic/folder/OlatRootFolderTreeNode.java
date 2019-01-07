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

package org.olat.core.gui.control.generic.folder;

import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSItem;

/**
 * 
 * <h3>Description:</h3>
 * <p>
 * Initial Date:  26 nov. 2010 <br>
 * @author srosse, srosse@frentix.com, www.frentix.com
 */
public class OlatRootFolderTreeNode extends GenericTreeNode {
	
	private boolean loaded = false;
	private final VFSItem item;
	private final OlatRootFolderTreeModel model; 
	
	public OlatRootFolderTreeNode(VFSItem item, OlatRootFolderTreeModel model) {
		super();
		this.item = item;
		this.model = model;
	}

	@Override
	public int getChildCount() {
		int count = super.getChildCount();
		if(count == 0 && !loaded && item instanceof LocalFolderImpl) {
			model.makeChildren(this, (LocalFolderImpl)item);
			loaded = true;
			count = super.getChildCount();
		}
		return count;
	}
}