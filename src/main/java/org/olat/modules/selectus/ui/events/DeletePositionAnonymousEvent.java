/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.events;

import org.olat.core.gui.control.Event;

import org.olat.modules.selectus.model.Position;

/**
 * 
 * Initial date: 1 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DeletePositionAnonymousEvent extends Event {

	private static final long serialVersionUID = 3418848901921552389L;
	public static final String DELETE_POSITION = "delete-position-anonymous";
	
	private Position position;
	
	public DeletePositionAnonymousEvent(Position position) {
		super(DELETE_POSITION);
		this.position = position;
	}
	
	public Position getPosition() {
		return position;
	}
}
