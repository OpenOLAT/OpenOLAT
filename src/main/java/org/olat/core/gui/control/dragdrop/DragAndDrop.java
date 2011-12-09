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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.gui.control.dragdrop;

/**
 * Description:<br>
 * this interface can either be offered by a concrete component (e.g. a panel, a menutree) or by a controller
 * (e.g. toolcontroller)
 * if an operation is not supported (e.g. drag is supported, but drop not) it must be declared in the documentation and the method must throw
 * an unsupportedoperation - exception.
 * 
 * 
 * <P>
 * Initial Date:  11.04.2006 <br>
 * @author Felix
 */
public interface DragAndDrop {
	// Drop parts
	public Droppable activateDroppable();
	
	public void deactivateDroppable();
	
	// Drag part
	public Draggable activateDraggable();
	
	public void deactivateDraggable();
	
	
}
