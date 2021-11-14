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
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;

/**
 * Description:<br>
 * There three kind of feedback to the user about info / warnings / errors.
 * <ol>
 * <li>showInfo / showWarn / showError as defined in BasicController</li>
 * <li>Info / Warn / Error messages which should stay at a defined place</li>
 * <li>info / warn / error messages within dialogs with a action required</li>
 * </ol>
 * The first kind is feedback which should get away on the next click - it 
 * reveals an information concerning the whole screen, it can not freely be placed<br>
 * A message with the same styling as the first kind, but it can be placed as
 * controller anywhere and resides there until removed.<br>
 * The last mentioned type is in fact a dialog and may not look like the other
 * two described.<br>
 * This MessageController implement the second kind of message.
 * <P>
 * Initial Date:  02.12.2007 <br>
 * @author patrickb
 */
public class MessageController extends BasicController{

	static final int INFO = 0;//"guimsginfo";
	static final int WARN = 1;//guimsgwarn";
	static final int ERROR = 2;//"guimsgerror";

	static final String[] VC_PAGES = new String[]{"guimsginfo", "guimsgwarn", "guimsgerror"};
	private VelocityContainer msgVC;
	
	/**
	 * Wrapper for simple info, warn, error text to be placed in the gui where it
	 * must be explicitly removed - compared to showInfo / showWarn / showError 
	 * methods on the basic controller.<br>
	 * @param ureq
	 * @param control
	 * @param msgType must be one of INFO, WARN, ERROR
	 * @param title if null it is omitted, if not null it is wrapped with h4 tags
	 * @param text can be null and gets then replaced with empty string.
	 */
	MessageController(UserRequest ureq, WindowControl control, int msgType, String title, String text) {
		super(ureq, control);
		if(msgType < 0 || msgType > VC_PAGES.length){
			throw new AssertException("provided message type is undefined, type given:"+msgType);
		}
		// use tranlsation keys from default package
		setTranslator(Util.createPackageTranslator(I18nManager.class, getLocale()));
		//
		msgVC = createVelocityContainer(VC_PAGES[msgType]);
		//
		String message;
		text = text != null ? text : "";
		if(title != null){
			message = "<h4>" + title + "</h4><br />" + text;
		}else{
			message = text;
		}
		//
		msgVC.contextPut("message", message);
		putInitialPanel(msgVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// This is only simple text in this controller, there should no events come
		// along here.
		throw new AssertException("This is a simple Wrapper controller without meaning in event method: event from "+source.getComponentName()+" event:"+event.getCommand());
	}

}
