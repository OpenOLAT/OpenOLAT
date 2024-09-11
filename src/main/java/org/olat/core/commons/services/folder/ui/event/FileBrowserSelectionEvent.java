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
package org.olat.core.commons.services.folder.ui.event;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.control.Event;
import org.olat.core.util.vfs.VFSItem;

/**
 * 
 * Initial date: 17 Apr 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FileBrowserSelectionEvent extends Event {

	private static final long serialVersionUID = -5041930163208368339L;

	private final List<VFSItem> vfsItems;
	private final FileElement fileElement;
	
	public FileBrowserSelectionEvent(List<VFSItem> vfsItems) {
		super("file-browser-selection");
		this.vfsItems = vfsItems;
		this.fileElement = null;
	}

	public FileBrowserSelectionEvent(List<VFSItem> vfsItems, FileElement fileElement) {
		super("file-browser-selection");
		this.vfsItems = vfsItems;
		this.fileElement = fileElement;
	}
	
	public List<VFSItem> getVfsItems() {
		return vfsItems;
	}

	public FileElement getFileElement() {
		return fileElement;
	}
}
