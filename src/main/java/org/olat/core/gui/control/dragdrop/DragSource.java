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

/**
 * Description:<br>
 * TODO: Felix Class Description for DragSource
 * 
 * <P>
 * Initial Date:  11.04.2006 <br>
 * @author Felix
 */
public interface DragSource {
	/**
	 * The source of the Dragged object can either be a controller or a component
	 * @return the source of the drag
	 */
	public Object getSource();
	
	/**
	 * 
	 * @return a subid (e.g. the position of an entry in the toolcontroller). Used by the concrete dragsource's controller or component to resolve the details.
	 */
	public String getSubId();

}
