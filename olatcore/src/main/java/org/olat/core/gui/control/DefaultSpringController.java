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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;

/**
 * Description:<br>
 * TODO: patrickb Class Description for DefaultSpringController
 * 
 * <P>
 * Initial Date:  13.06.2006 <br>
 * @author patrickb
 */
public abstract class DefaultSpringController extends DefaultController implements SpringController {
	

	/**
	 * [used by spring]
	 * the caller must call init(ureq, windowcontrol) later
	 * brasato:::: replace with ControllerCreator
	 */
	public DefaultSpringController() {
		super(null);
		//via init: we use this.setOrigWControl(); to set the WControl later!!
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public abstract void event(UserRequest ureq, Component source, Event event);

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected abstract void doDispose();

	/** 
	 * all implementors do their "normal" init here, which would normally be done in the constructor.
	 * to use the windowcontrol in the doInit, use "getWindowControl()" 
	 */
	protected abstract void doInit(UserRequest ureq);
	
	/**
	 * 
	 * @see org.olat.core.gui.control.SpringController#init(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public final void init(UserRequest ureq, WindowControl wControl){
		this.setOrigWControl(wControl);
		doInit(ureq);
	}

}
