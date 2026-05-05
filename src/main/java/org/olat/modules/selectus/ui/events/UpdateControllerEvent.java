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
 * Initial Date:  3 mar. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class UpdateControllerEvent extends Event {

	private static final long serialVersionUID = -3392854280002408247L;
	
	public static final String UPDATE_CONTROLLER_EVENT = "update-crumb";
	
	private final String translatedName;
	
	private Controller controller;
	private ControllerCreator creator;

	public UpdateControllerEvent(String translatedName, Controller controller) {
		super(UPDATE_CONTROLLER_EVENT);
		this.controller = controller;
		this.translatedName = translatedName;
	}
	
	public UpdateControllerEvent(String translatedName, ControllerCreator creator) {
		super(UPDATE_CONTROLLER_EVENT);
		this.creator = creator;
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
