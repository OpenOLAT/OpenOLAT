/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.ims.qti;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.VetoPopEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.VetoableCloseController;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController;

/**
 * 
 * This particularly overview the veto of the QTI editor
 * 
 * Initial date: 15.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTIRuntimeController extends RepositoryEntryRuntimeController implements VetoableCloseController  {
	
	private Delayed delayedClose;
	
	public QTIRuntimeController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry re, RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator) {
		super(ureq, wControl, re, reSecurity, runtimeControllerCreator);
	}
	
	/**
	 * This is only used by the QTI editor
	 */
	@Override
	public boolean requestForClose(UserRequest ureq) {
		if(editorCtrl instanceof VetoableCloseController) {
			return ((VetoableCloseController)editorCtrl).requestForClose(ureq);
		}
		return true;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == editorCtrl && source instanceof VetoableCloseController) {
			if(event == Event.DONE_EVENT) {
				if(delayedClose != null) {
					switch(delayedClose) {
						case access: super.doAccess(ureq); break;
						case details: super.doDetails(ureq); break;
						case editSettings: super.doEditSettings(ureq); break;
						case catalog: super.doCatalog(ureq); break;
						case members: super.doMembers(ureq); break;
						case orders: super.doOrders(ureq); break;
						case close: super.doClose(ureq); break;
						case pop: {
							popToRoot(ureq);
							cleanUp();
							Controller runtimeCtrl = getRuntimeController();
							if(runtimeCtrl != null) {
								launchContent(ureq, reSecurity);
								initToolbar();
							}
							break;
						}
						default: {}
					}
					delayedClose = null;
				} else {
					fireEvent(ureq, Event.DONE_EVENT);
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(event == Event.CLOSE_EVENT) {
			if(requestForClose(ureq)) {
				super.event(ureq, source, event);
			} else {
				delayedClose = Delayed.close;
			}
		} else if(event instanceof VetoPopEvent) {
			if(requestForClose(ureq)) {
				super.event(ureq, source, event);
			} else {
				delayedClose = Delayed.pop;
			}
		} else {
			super.event(ureq, source, event);
		}
	}

	@Override
	protected void doAccess(UserRequest ureq) {
		if(requestForClose(ureq)) {
			super.doAccess(ureq);
		} else {
			delayedClose = Delayed.access; 
		}
	}

	@Override
	protected void doDetails(UserRequest ureq) {
		if(requestForClose(ureq)) {
			super.doDetails(ureq);
		} else {
			delayedClose = Delayed.details; 
		}
	}

	@Override
	protected void doEditSettings(UserRequest ureq) {
		if(requestForClose(ureq)) {
			super.doEditSettings(ureq);
		} else {
			delayedClose = Delayed.editSettings; 
		}
	}

	@Override
	protected void doCatalog(UserRequest ureq) {
		if(requestForClose(ureq)) {
			super.doCatalog(ureq);
		} else {
			delayedClose = Delayed.catalog; 
		}
	}

	@Override
	protected Activateable2 doMembers(UserRequest ureq) {
		if(requestForClose(ureq)) {
			return super.doMembers(ureq);
		} else {
			delayedClose = Delayed.members;
			return null;
		}
	}

	@Override
	protected void doOrders(UserRequest ureq) {
		if(requestForClose(ureq)) {
			super.doOrders(ureq);
		} else {
			delayedClose = Delayed.orders; 
		}
	}

	private enum Delayed {
		access,
		details,
		editSettings,
		catalog,
		members,
		orders,
		close,
		pop
	}
}
