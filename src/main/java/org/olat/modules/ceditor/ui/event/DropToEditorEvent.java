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
package org.olat.modules.ceditor.ui.event;

import org.olat.core.gui.control.Event;
import org.olat.modules.ceditor.ui.component.ContentEditorComponent;

/**
 * 
 * Initial date: 18 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DropToEditorEvent extends Event {

	private static final long serialVersionUID = -1824866150334642515L;
	public static final String DROP_TO_EDITOR = "drop-to-editor";
	
	private final PositionEnum position;
	private final String sourceComponentId;
	private final ContentEditorComponent targetComponent;
	
	public DropToEditorEvent(String sourceComponentId, ContentEditorComponent targetComponent, PositionEnum position) {
		super(DROP_TO_EDITOR);
		this.sourceComponentId = sourceComponentId;
		this.targetComponent = targetComponent;
		this.position = position;
	}

	public String getSourceComponentId() {
		return sourceComponentId;
	}
	
	public PositionEnum getPosition() {
		return position;
	}

	public ContentEditorComponent getTargetComponent() {
		return targetComponent;
	}
}
