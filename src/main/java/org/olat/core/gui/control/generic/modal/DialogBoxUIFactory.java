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
*/
package org.olat.core.gui.control.generic.modal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.LockResult;
import org.olat.user.UserManager;

/**
 * <h3>Description:</h3>
 * Use this factory to generate certain generic dialog types
 * <p>
 * Initial Date: 26.11.2007<br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class DialogBoxUIFactory {
	
	/**
	 * The Yes-No dialog has two buttons. the following events are fired:
	 * <ul>
	 * <li>yes -> ButtonClickedEvent with position 0</li>
	 * <li>no -> ButtonClickedEvent with position 1</li>
	 * <li>Event.CANCELLED_EVENT: when user clicks the close icon in the window bar</li>
	 * </ul>
	 * <p>
	 * Initial Date: 26.11.2007<br>
	 * 
	 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
	 */
	public static DialogBoxController createYesNoDialog(UserRequest ureq, WindowControl wControl, String title, String text) {
		Translator trans = Util.createPackageTranslator(DialogBoxUIFactory.class, ureq.getLocale());
		List<String> yesNoButtons = new ArrayList<>();
		yesNoButtons.add(trans.translate("yes"));
		yesNoButtons.add(trans.translate("no"));
		return new DialogBoxController(ureq, wControl, title, text, yesNoButtons);
	}

	/**
	 * The Ok-Cancel dialog has two buttons. The following events are fired:
	 * <ul>
	 * <li>OK -> ButtonClickedEvent with position 0</li>
	 * <li>Cancel -> ButtonClickedEvent with position 1</li>
	 * <li>Event.CANCELLED_EVENT: when user clicks the close icon in the window bar or the cancel button</li>
	 * </ul>
	 * <p>
	 * Initial Date: 26.11.2007<br>
	 * 
	 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
	 */
	public static DialogBoxController createOkCancelDialog(UserRequest ureq, WindowControl wControl, String title, String text) {
		Translator trans = Util.createPackageTranslator(DialogBoxUIFactory.class, ureq.getLocale());
		List<String> okCancelButtons = new ArrayList<>();
		okCancelButtons.add(trans.translate("ok"));
		okCancelButtons.add(trans.translate("cancel"));
		return new DialogBoxController(ureq, wControl, title, text, okCancelButtons);
	}

	/**
	 * A generic dialog can have zero or more buttons to be pressed.
	 * @param ureq
	 * @param wControl
	 * @param title
	 * @param text
	 * @param buttonLabels
	 * @return
	 */
	public static DialogBoxController createGenericDialog(UserRequest ureq, WindowControl wControl, String title, String text,
			List<String> buttonLabels) {
		return new DialogBoxController(ureq, wControl, title, text, buttonLabels);
	}
	
	/**
	 * create the default info message shown for an unsuccessfully acquired lock - must not be called if lock was successfully acquired! 
	 * @param ureq
	 * @param wControl
	 * @param lockEntry must be not null
	 * @param i18nLockMsgKey must be not null and valid key in the given translator
	 * @param translator must be not null
	 * @return DialogController
	 */
	public static DialogBoxController createResourceLockedMessage(UserRequest ureq, WindowControl wControl, LockResult lockEntry,String i18nLockMsgKey, Translator translator) {
		if(lockEntry.isSuccess()){
			throw new AssertException("do not create a 'is locked message' if lock was succesfull! concerns lock:"+lockEntry.getOwner());
		}
		String fullName = CoreSpringFactory.getImpl(UserManager.class).getUserDisplayName(lockEntry.getOwner());
		String[] i18nParams = new String[] { StringHelper.escapeHtml(fullName),
				Formatter.getInstance(ureq.getLocale()).formatTime(new Date(lockEntry.getLockAquiredTime())) };
		String lockMsg = translator.translate(i18nLockMsgKey, i18nParams);
		
		Translator trans = Util.createPackageTranslator(DialogBoxUIFactory.class, ureq.getLocale());
		List<String> okButton = new ArrayList<>();
		okButton.add(trans.translate("ok"));
		
		DialogBoxController ctrl = new DialogBoxController(ureq, wControl, null, lockMsg, okButton);
		ctrl.setCssClass("o_warning");
		return ctrl;
	}
	

	/**
	 * checks if this event from a OkCancel Dialog is an OK event.
	 * @param event
	 * @return true if ok clicked, false if cancel clicked
	 */
	public static boolean isOkEvent(Event event) {
		if(event == Event.CANCELLED_EVENT){
			//dialogboxes can be canceled
			return false;
		}
		if(event instanceof ButtonClickedEvent){
			ButtonClickedEvent bce = (ButtonClickedEvent) event;
			return bce.getPosition() == 0;//see createYesNoDialog for order
		}else{
			throw new AssertException("expected a ButtonClickedEvent, but was "+event.getClass().getCanonicalName());
		}
	}

	/**
	 * checks if this event form a YesNo Dialog is an Yes event.
	 * @param event
	 * @return true if "yes" clicked, false if "no" clicked.
	 */
	public static boolean isYesEvent(Event event) {
		//instead of duplicating code, we can anyway not garantuee that the provided
		//event comes from an YesNo Dialog
		//this would also return true for the first button clicked
		//for any DialogBoxController
		return isOkEvent(event);
	}
	
	/**
	 * true if the dialog box was closed with the close icon.
	 * @param event
	 * @return
	 */
	public static boolean isClosedEvent(Event event) {
		return event == Event.CANCELLED_EVENT;
	}
	
	/**
	 * @param event
	 * @return position of which button was clicked
	 */
	public static int getButtonPos(Event event) {
		if(event instanceof ButtonClickedEvent){
			ButtonClickedEvent bce = (ButtonClickedEvent) event;
			return bce.getPosition();
		}else{
			throw new AssertException("expected a ButtonClickedEvent, but was "+event.getClass().getCanonicalName());
		}
	}



}
