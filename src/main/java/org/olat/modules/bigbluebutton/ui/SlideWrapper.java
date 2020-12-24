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
package org.olat.modules.bigbluebutton.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 23 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SlideWrapper {
	
	private final VFSLeaf document;
	private FormLink deleteButton;
	private boolean deleted = false;
	private boolean temporary = false;
	
	public SlideWrapper(VFSLeaf document, boolean temporary) {
		this.document = document;
		this.temporary = temporary;
	}
	
	public String getFilename() {
		return document.getName();
	}
	
	public boolean isTemporary() {
		return temporary;
	}
	
	public String getLabel() {
		return document.getName() + " (" + Formatter.formatBytes(document.getSize()) + ")";
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public VFSLeaf getDocument() {
		return document;
	}

	public FormLink getDeleteButton() {
		return deleteButton;
	}

	public void setDeleteButton(FormLink deleteButton) {
		this.deleteButton = deleteButton;
		deleteButton.setUserObject(this);
	}

}
