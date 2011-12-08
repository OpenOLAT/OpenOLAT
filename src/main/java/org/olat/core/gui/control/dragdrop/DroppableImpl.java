/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.gui.control.dragdrop;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:<br>
 * TODO: Felix Jost Class Description for DroppableImpl
 * 
 * <P>
 * Initial Date:  12.04.2006 <br>
 * @author Felix Jost
 */
public class DroppableImpl implements Droppable {
	private List<Draggable> accepted = new ArrayList<Draggable>();
	
	/**
	 * @see org.olat.core.gui.control.dragdrop.Droppable#addAcceptedDraggable(org.olat.core.gui.control.dragdrop.Draggable)
	 */
	public void addAcceptedDraggable(Draggable d) {
		accepted.add(d);
	}

	/**
	 * 
	 * @return a list of accepted Dragables
	 */
	public List<Draggable> getAccepted() {
		return accepted;
	}
	
}
