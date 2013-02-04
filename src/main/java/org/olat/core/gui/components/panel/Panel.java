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

package org.olat.core.gui.components.panel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.Container;
import org.olat.core.gui.control.dragdrop.DragAndDrop;
import org.olat.core.gui.control.dragdrop.DragAndDropImpl;
import org.olat.core.gui.control.dragdrop.DragSource;
import org.olat.core.gui.control.dragdrop.DragSourceImpl;
import org.olat.core.gui.control.dragdrop.Draggable;
import org.olat.core.gui.control.dragdrop.DraggableCreator;
import org.olat.core.gui.control.dragdrop.DropEvent;
import org.olat.core.gui.control.dragdrop.DropTarget;
import org.olat.core.gui.control.dragdrop.DropTargetImpl;
import org.olat.core.gui.control.dragdrop.DroppableImpl;
import org.olat.core.logging.AssertException;

/**
 * Description: <br>
 * The panel implements a place holder component with a stack to hold zero, one
 * or more components. Only the highest component on the stack is shown.
 * 
 * @author Felix Jost
 */
public class Panel extends Container {
	private static final ComponentRenderer RENDERER = new PanelRenderer();
		
	
	private Component curContent;
	protected final List<Component> stackList = new ArrayList<Component>(3);; // allow access to extending classes
	
	private DragAndDropImpl dragAndDropImpl; 

	/**
	 * @param name
	 */
	public Panel(String name) {
		super(name);
		curContent = null;
	}

	/**
	 * since the Panel does and shown nothing (is only a convenient boundary to
	 * put components into, and to swap them), we dispatch the request to the
	 * delegate
	 * @param ureq
	 */
	protected void doDispatchRequest(UserRequest ureq) {
		if (dragAndDropImpl != null) {
			//fxdiff
			// a drop is dispatched to the panel
			DroppableImpl di = dragAndDropImpl.getDroppableImpl();
			if (di != null) {
				String dropid = ureq.getParameter("v");
				for (Draggable dr:di.getAccepted()) {
					DragSource ds = dr.find(dropid);
					if (ds != null) {
						// found!
						DropTarget dt = new DropTargetImpl(this);
						fireEvent(ureq, new DropEvent(ds, dt));
						return;
					}
					
				}
			} else {
				throw new AssertException("no droppable defined, but a request dispatched to the panel: ureq=" + ureq);
			}
		} else {
			throw new AssertException("a panel should never dispatch a request (unless it has droppables, which it has not), ureq = "+ureq);
		}
	}

	/**
	 * @return
	 */
	public Component getContent() {
		return curContent;
	}

	/**
	 * @see org.olat.core.gui.components.Container#put(org.olat.core.gui.components.Component)
	 */
	public void put(Component component) {
		throw new AssertException("please don't use put(comp) in a panel, but setContent(component) or pushContent(component)");
	}

	/**
	 * clears the stack and sets the base content anew.
	 * 
	 * @param newContent the newContent. if null, then the panel will be empty
	 */
	public void setContent(Component newContent) {
		stackList.clear();
		getComponents().clear();
		if (newContent != null) {
			pushContent(newContent);
		} else {
			curContent = null;
		}
		setDirty(true);
	}

	/**
	 * @param newContent may not be null
	 */
	public void pushContent(Component newContent) {
		if (curContent != null) super.remove(curContent);
		super.put("pc", newContent); // add in tree for later rendering;
		stackList.add(newContent);
		curContent = newContent;
		setDirty(true);
	}

	/**
	 * 
	 */
	public void popContent() {
		int stackHeight = stackList.size();
		if (stackHeight < 1) throw new AssertException("stack was empty!");
		if (curContent == null) throw new AssertException("stackHeight not zero, but curContent was null!");
		// remove the current active component as the containers child
		super.remove(curContent);
		stackList.remove(stackHeight - 1); // remove the top component
		if (stackHeight == 1) { // after pop, the content is null
			curContent = null;
		} else { // stackHeight > 1
			curContent = stackList.get(stackHeight - 2);
			super.put("pc", curContent); // set it as the container's child
		}
		setDirty(true);
	}

	/**
	 * @see org.olat.core.gui.components.Container#getExtendedDebugInfo()
	 */
	public String getExtendedDebugInfo() {
		StringBuilder sb = new StringBuilder();
		int size = stackList.size();
		for (int i = 0; i < size; i++) {
			Component comp = stackList.get(i); // may be null
			String compName = (comp == null ? "NULL" : comp.getComponentName());
			sb.append(compName).append(" | ");
		}
		return "stacksize:" + size + ", active:" + sb.toString();
	}

	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	/**
	 * @return Returns the dragAndDrop (it is created if it was null)
	 * as usual, all methods here are not threadsafe
	 */
	public DragAndDrop getDragAndDrop() {
		// a space saver, since only a few panels will support drag and drop.
		if (dragAndDropImpl == null) {
			dragAndDropImpl = new DragAndDropImpl(new DraggableCreator() {
				public Draggable createDraggable() {
					Draggable drag = new Draggable() {
						@Override
						public List<String> getContainerIds() {
							return Panel.this.draggableGetContainerIds();
						}

						@Override
						public DragSource find(String dragElementId) {
							return Panel.this.draggableFind(dragElementId);
						}};
					return drag;
				}});
		}
		return dragAndDropImpl;
	}
	
	/**
	 * to be accessed by the renderer only
	 */
	protected DragAndDropImpl doGetDragAndDrop() {
		return dragAndDropImpl;
	}

	/**
	 * @param dragElementId
	 * @return
	 */
	protected DragSource draggableFind(String dragElementId) {
		//fxdiff
		Component toRender = getContent();
		if (toRender != null) {
			String id = "o_c" + toRender.getDispatchID();
			if (dragElementId.equals(id)) {
				return new DragSourceImpl(this);
			}
		}
		String id = "o_c" + getDispatchID();
		if (dragElementId.equals(id)) {
			return new DragSourceImpl(this);
		}
		// else: the object dropped disappear in the meantime...? TODO:double-check
		return null;
	}

	/**
	 * @return
	 */
	protected List<String> draggableGetContainerIds() {
		//fxdiff
		return Collections.singletonList("o_c" + getDispatchID());
	}
	
	

}