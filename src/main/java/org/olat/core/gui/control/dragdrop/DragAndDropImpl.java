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
 * TODO: Felix Class Description for DragAndDropImpl
 * this class is not threadsafe.
 * <P>
 * Initial Date:  11.04.2006 <br>
 * @author Felix
 */
public class DragAndDropImpl implements DragAndDrop {

	private DroppableImpl droppable;
	private Draggable draggable;
	private final DraggableCreator ddc;
	
	
	public DragAndDropImpl(DraggableCreator ddc) {
		this.ddc = ddc;
		
	}
	/**
	 * @see org.olat.core.gui.control.dragdrop.DragAndDrop#activateDroppable()
	 */
	public Droppable activateDroppable() {
		if (droppable == null) {
			droppable = new DroppableImpl();
		}
		return droppable;
	}

	/**
	 * @see org.olat.core.gui.control.dragdrop.DragAndDrop#deactivateDroppable()
	 */
	public void deactivateDroppable() {
		droppable = null;
	}

	/**
	 * @see org.olat.core.gui.control.dragdrop.DragAndDrop#activateDraggable()
	 */
	public Draggable activateDraggable() {
		if (draggable == null) {
			draggable = ddc.createDraggable();
		}
		return draggable;
	}

	/**
	 * @see org.olat.core.gui.control.dragdrop.DragAndDrop#deactivateDraggable()
	 */
	public void deactivateDraggable() {
		draggable = null;
	}
	
	//non-interfaced methods
	
	public DroppableImpl getDroppableImpl() {
		return droppable;
	}

	/**
	 * @return Returns the draggable.
	 */
	public Draggable getDraggable() {
		return draggable;
	}

}
