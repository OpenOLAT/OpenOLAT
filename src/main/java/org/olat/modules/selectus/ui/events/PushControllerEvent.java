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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.modules.selectus.ui.events;

import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.creator.ControllerCreator;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  2 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PushControllerEvent extends Event {

	private static final long serialVersionUID = -9134795886791731887L;

	public static final String PUSH_CONTROLLER_EVENT = "push-controller-event";
	
	private final String translatedName;
	
	private Controller controller;
	private ControllerCreator creator;
	
	public PushControllerEvent(String translatedName, ControllerCreator creator) {
		super(PUSH_CONTROLLER_EVENT);
		this.creator = creator;
		this.translatedName = translatedName;
	}
	
	public PushControllerEvent(String translatedName, Controller controller) {
		super(PUSH_CONTROLLER_EVENT);
		this.controller = controller;
		this.translatedName = translatedName;
	}
	
	public String getTranslatedName() {
		return translatedName;
	}

	public ControllerCreator getControllerCreator() {
		return creator;
	}
	
	public Controller getController() {
		return controller;
	}
}
