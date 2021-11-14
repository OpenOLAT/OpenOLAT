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

package org.olat.core.util.controller;

import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;

/**
 * Description:<BR/>
 * Please use only from within Manager-Classes!
 * This class autodisposes the contained controller, when a OLATResourceableJustBeforeDeletedEvent for the given olatresourceable arrives.
 * 
 * 
 * <P/>
 * Initial Date:  03.09.2004
 *
 * @author Felix Jost 
 */
public class OLATResourceableListeningWrapperController extends MainLayoutBasicController implements GenericEventListener, Activateable2 {
	private Controller realController;
	private OLATResourceable ores;
	private Activateable2 delegate;//fxdiff BAKS-7 Resume function
	/**
	 * !Use only from within Manager-Classes.
	 * 
	 * @param ores
	 * @param realController
	 * @param owner
	 * 
	 */
	public OLATResourceableListeningWrapperController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, Controller realController, Activateable2 delegate, Identity owner) {
		super(ureq, wControl);
		this.realController = realController;
		this.delegate = delegate;//fxdiff BAKS-7 Resume function
		this.ores = ores;
		realController.addControllerListener(this);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, owner, ores);
	}

	/**
	 * @see org.olat.core.gui.control.Controller#getInitialComponent()
	 */
	public Component getInitialComponent() {
		return realController.getInitialComponent();
	}

	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, ores);
		realController.dispose();
        super.doDispose();
	}

	@Override
	public void event(Event event) {
		if (event instanceof OLATResourceableJustBeforeDeletedEvent) {
			OLATResourceableJustBeforeDeletedEvent orj = (OLATResourceableJustBeforeDeletedEvent)event;
			if (!orj.targetEquals(ores)) throw new AssertException("disposingwrappercontroller only listens to del event for resource "+
					ores.getResourceableTypeName()+" / "+ores.getResourceableId()+", but event was for "+orj.getDerivedOres());
			dispose();
		}
		
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source != realController) throw new AssertException("wrappercontroller can only listen to events from its contained controller");
		// call the overriden method
		fireEvent(ureq, event);
	}
	
	/**
	 * needs to be overridden since the originator of the event is not me, but the wrapped controller
	 * @see org.olat.core.gui.control.DefaultController#fireEvent(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Event)
	 */
	protected void fireEvent(UserRequest ureq, Event event) {
		List<ControllerEventListener> listeners = getListeners();
		if (listeners == null) return;
		for (Iterator<ControllerEventListener> iter = listeners.iterator(); iter.hasNext();) {
			ControllerEventListener listener = iter.next();
			//was: listener.dispatchEvent(ureq, this, event);
			// needs to be:
			listener.dispatchEvent(ureq, realController, event);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		throw new AssertException("wrapperComponent should never listen to a componenent! source="+source.getComponentName()+", event "+event);
	}
	
	@Override
	//fxdiff BAKS-7 Resume function
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(delegate != null) {
			delegate.activate(ureq, entries, state);
		} else if(realController instanceof Activateable2) {
			((Activateable2)realController).activate(ureq, entries, state);
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return " ores listening wrappercontroller ("+hashCode()+") of: "+realController.toString();
	}

}

