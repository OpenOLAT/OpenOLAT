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
package org.olat.core.gui.control.generic.messages;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.logging.AssertException;

/**
 * Description:<br>
 * Convenience controller if only some text must be displayed.
 * 
 * <P>
 * Initial Date:  02.12.2007 <br>
 * @author patrickb
 */
public class SimpleMessageController extends BasicController {

	public SimpleMessageController(UserRequest ureq, WindowControl wControl, String text, String cssClassName) {
		super(ureq, wControl);
		VelocityContainer simplemsg = createVelocityContainer("simplemsg");
		String msg;
		text = text != null ? text : "";
		if(cssClassName != null){
			msg = "<div class=\"" + cssClassName + "\">" + text + "</div>";
		} else {
			msg = text;
		}
		simplemsg.contextPut("text", msg);
		putInitialPanel(simplemsg);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// This is only simple text in this controller, there should no events come
		// along here.
		throw new AssertException("This is a simple Wrapper controller without meaning in event method: event from "+source.getComponentName()+" event:"+event.getCommand());
	}

}
