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
package org.olat.modules.cemedia.ui;

import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.modules.cemedia.MediaToGroupRelation.MediaToGroupRelationType;
import org.olat.modules.cemedia.model.MediaShare;

/**
 * 
 * Initial date: 6 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaShareRow {
	
	private MediaShare share;
	private final String displayName;
	private FormToggle editableToggleButton;
	
	public MediaShareRow(MediaShare share, String displayName) {
		this.share = share;
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public MediaToGroupRelationType getType() {
		return share.getType();
	}

	public MediaShare getShare() {
		return share;
	}

	public void setShare(MediaShare share) {
		this.share = share;
	}
	
	public boolean isEditable() {
		return editableToggleButton != null && editableToggleButton.isOn();
	}

	public FormToggle getEditableToggleButton() {
		return editableToggleButton;
	}

	public void setEditableToggleButton(FormToggle editableToggleButton) {
		this.editableToggleButton = editableToggleButton;
	}
}
