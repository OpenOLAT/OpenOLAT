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

import java.util.List;

/**
 * Description:<br>
 * TODO: Felix Jost Class Description for Dragabble
 * 
 * <P>
 * Initial Date:  07.04.2006 <br>
 * @author Felix Jost
 */
public interface Draggable {
	/**
	 * used by the Droppable to add these ids (the ids of the div(s) surrounding the draggable elements) to the ids which are accepted to be dropped. 
	 * @return a list of ids(Strings)
	 */
	public List<String> getContainerIds();
	
	/**
	 * 
	 * @param dragElementId used by Droppable. this is the id the droppable js receives and which denotes the dragsource.
	 * @return null if not found or the DragSource when the Draggable knows that the dragElementId belongs to itself
	 */
	public DragSource find(String dragElementId);
	
}
