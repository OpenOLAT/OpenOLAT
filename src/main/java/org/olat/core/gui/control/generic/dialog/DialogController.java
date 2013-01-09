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

package org.olat.core.gui.control.generic.dialog;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * Description:<BR>
 * Generic controller that displays a dialogue with two buttons, e.g. to to make
 * a delete confirmation dialogue. Fires the following events:
 * DialogController.EVENT_FIRSTBUTTON DialogController.EVENT_SECONDBUTTON
 * <P>
 * Initial Date: 2004/08/28 09:29:32
 * 
 * @author Felix Jost
 * @deprecated don't use this anymore. Use BasicController methods or
 *             controllers from org.core.gui.control.modal package
 */
public class DialogController extends DefaultController {
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(DialogController.class);
	private static final String PACKAGE = Util.getPackageName(DialogController.class);

	/** Event fired when first button is pressed * */
	public static final Event EVENT_FIRSTBUTTON = new Event("fb");
	/** Event fired when second button is pressed * */
	public static final Event EVENT_SECONDBUTTON = new Event("sb");
	/** Event fired when the close icon is pressed * */
	public static final Event EVENT_CLOSEICON = new Event("ci");

	private VelocityContainer myContent;
	private Object userObject;
	private Link firstButton;
	private Link secondButton;
	private Link backIcon;

	/**
	 * @param locale
	 * @param text
	 * @param cel
	 * @param userObject
	 * @return the controller
	 */
	public static DialogController createYesNoDialogController(WindowControl wControl, Locale locale, String text, ControllerEventListener cel, Object userObject) {
		Translator intTrans = new PackageTranslator(PACKAGE, locale);
		DialogController dc = new DialogController(wControl, locale, intTrans.translate("answer.yes"), intTrans.translate("answer.no"), text, userObject, true, null);
		// when used with basic controller listener has to be set with listenTo()
		if (cel != null) dc.addControllerListener(cel);
		return dc;
	}

	/**
	 * @param locale
	 * @param text
	 * @param cel
	 * @return the controller
	 */
	public static DialogController createOkCancelDialogController(WindowControl wControl, Locale locale, String text, ControllerEventListener cel) {
		Translator intTrans = new PackageTranslator(PACKAGE, locale);
		DialogController dc = new DialogController(wControl, locale, intTrans.translate("answer.ok"), intTrans.translate("answer.cancel"), text);
		// when used with basic controller listener has to be set with listenTo()		
		if (cel != null) dc.addControllerListener(cel);
		return dc;
	}

	/**
	 * @param locale
	 * @param firstButtonText
	 * @param text
	 */
	public DialogController(WindowControl wControl, Locale locale, String firstButtonText, String text) {
		this(wControl, locale, firstButtonText, null, text, null, true, null);
	}

	/**
	 * @param locale
	 * @param firstButtonText
	 * @param secondButtonText
	 * @param text
	 */
	public DialogController(WindowControl wControl, Locale locale, String firstButtonText, String secondButtonText, String text) {
		this(wControl, locale, firstButtonText, secondButtonText, text, null, true, null);
	}

	/**
	 * Constructor DialogController.
	 * 
	 * @param locale
	 * @param firstButtonText
	 * @param secondButtonText
	 * @param text the (non html) text to be rendered
	 * @param userObject any UserObject to be retrieved later if the dialog has
	 *          been answered by the user (convenience)
	 */
	public DialogController(WindowControl wControl, Locale locale, String firstButtonText, String secondButtonText, String text, Object userObject, boolean displayCloseIcon, String title) {
		super(wControl);
		this.userObject = userObject;
		myContent = new VelocityContainer("genericdialog", VELOCITY_ROOT + "/index.html", new PackageTranslator(PACKAGE, locale), this);
		
		firstButton = LinkFactory.createCustomLink("firstButton", "fb", firstButtonText, Link.BUTTON + Link.NONTRANSLATED, myContent, this);
		secondButton = LinkFactory.createCustomLink("secondButton", "sb", secondButtonText, Link.BUTTON + Link.NONTRANSLATED, myContent, this);
		if (displayCloseIcon){
			backIcon = LinkFactory.createIconClose("close", myContent, this);
			myContent.contextPut("closeIcon", Boolean.TRUE);
		}
		if (title != null){
			myContent.contextPut("title", title);
		}
		myContent.contextPut("text", text == null ? "" : text);
		if (secondButtonText != null) myContent.contextPut("secondButtonText", secondButtonText);
		
		setCustomHeader(null);
		setInitialComponent(myContent);
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == firstButton){
			fireEvent(ureq, EVENT_FIRSTBUTTON);
		}
		else if (source == secondButton){
			fireEvent(ureq, EVENT_SECONDBUTTON);
		}
		else if (source == backIcon){
			fireEvent(ureq, EVENT_CLOSEICON);
		}
	}

	/**
	 * @param customHeader set a custom hader. if set to null, the default header
	 *          is used
	 */
	public void setCustomHeader(String customHeader) {
		if (customHeader == null) {
			myContent.contextPut("useCustomHeader", Boolean.FALSE);
		} else {
			myContent.contextPut("useCustomHeader", Boolean.TRUE);
			myContent.contextPut("customHeader", customHeader);
		}
	}

	/**
	 * @return the userobject provided at construction time of this controller
	 */
	public Object getUserObject() {
		return userObject;
	}

	protected void doDispose() {
	// nothing to do yet
	}
}