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

package org.olat.core.gui.control;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.info.WindowControlInfo;
import org.olat.core.gui.control.state.ControllerState;
import org.olat.core.gui.control.state.ExtendedControllerState;
/* <br>
 * @author Felix Jost
 */
import org.olat.core.gui.control.state.StateConstants;

public class WindowControlInfoImpl implements WindowControlInfo {
	private WindowControlInfo parentWindowControlInfo;
	private DefaultController defaultController;
	
	private List<WeakReference<WindowControlInfo>> children;
	
	
	/**
	 * @param an instance of defaultcontroller
	 * @param parentWindowControlInfo
	 */
	public WindowControlInfoImpl(DefaultController defaultController, WindowControlInfo parentWindowControlInfo) {
		this.defaultController = defaultController;
		this.parentWindowControlInfo = parentWindowControlInfo;
		if (parentWindowControlInfo != null) parentWindowControlInfo.addChild(this);
	}

	/**
	 * @see org.olat.core.gui.control.info.WindowControlInfo#getControllerClassName()
	 */
	public String getControllerClassName() {
		return defaultController.getClass().getName();
	}

	/**
	 * can be null if now windowcontrol given
	 * @see org.olat.core.gui.control.info.WindowControlInfo#getParentWindowControlInfo()
	 */
	public WindowControlInfo getParentWindowControlInfo() {
		return parentWindowControlInfo;
	}


	public void adjustControllerState(boolean back, ExtendedControllerState ecstate, UserRequest ureq) {
		ControllerState beforeState = ecstate.getPreviousState();
		ControllerState afterState = ecstate.getCurrentState();
		// TODO: should we move this check up? asking if a no-transition-info could maybe of any help here in some cases?
		// nothing to do if we don't have to change our state.
		if (beforeState.isSame(afterState)) return;
		
		ControllerState curState = defaultController.getState();
		
		if (back) {
			// move from after to before state - but curState must match the start state of the transition (which is afterState) and toState may not be the null/pre-initialized state
			if (afterState.isSame(curState) && !beforeState.isSame(StateConstants.NULL_STATE)) {
				defaultController.adjustState(beforeState, ureq);
			} // else we cannot revert -  
			else {
				System.out.println("back: cannot transition from "+afterState+" to "+beforeState+", since curState is:"+curState+", or to state is null");
			}
		} else { // forward
			// move from before to after state - but curState must match the start state of the transition (which is the beforeState)
			if (beforeState.isSame(curState) && !afterState.isSame(StateConstants.NULL_STATE)) {
				defaultController.adjustState(afterState, ureq);
			} else {
				System.out.println("forward: cannot transition from "+beforeState+" to "+afterState+", since curState is:"+curState+", or to state is null");
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.info.WindowControlInfo#getChildren()
	 */
	public List<WindowControlInfo> getChildren() {
		// returns all non-disposed children which can be reached over the weakreference 
		// (=not reclaimed yet by the GC since hard-referenced or the GC didn't run yet).
		// The weak reference is needed to avoid adding children over and over and never releasing them yet.
		// the release could also be done via controller.dispose -> wcontrol -> wcontrolinfo -> deregistermyselffrom parent,
		// but this is much more error prone.
		List<WindowControlInfo> active = new ArrayList<WindowControlInfo>();
		for (WeakReference wref : children) {
			WindowControlInfo wci = (WindowControlInfo) wref.get();
			if (wci != null && !wci.isControllerDisposed()) {
				active.add(wci);
			}
		}
		return active;
	}

	
	public ExtendedControllerState getExtendedControllerState() {
		return defaultController.createdExtendedControllerState();
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.info.WindowControlInfo#addChild(org.olat.core.gui.control.info.WindowControlInfo)
	 */
	public void addChild(WindowControlInfo child) {
		if (children == null) children = new ArrayList<WeakReference<WindowControlInfo>>(5);
		children.add(new WeakReference<WindowControlInfo>(child));
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.info.WindowControlInfo#isControllerDisposed()
	 */
	public boolean isControllerDisposed() {
		return defaultController.isDisposed();
	}
	
	public String toString() {
		return defaultController.getClass().getName()+":"+super.toString();
	}

}
