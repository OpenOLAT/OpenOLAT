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
package org.olat.modules.cemedia.ui.event;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 28 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddMediaEvent extends Event {
	
	private static final long serialVersionUID = -2592085491380177671L;
	public static final String ADD_MEDIA = "media-add";
	
	private boolean editMode;
	
	public AddMediaEvent(boolean editMode) {
		super(ADD_MEDIA);
		this.editMode = editMode;
	}

	public boolean isEditMode() {
		return editMode;
	}
	
	
}
