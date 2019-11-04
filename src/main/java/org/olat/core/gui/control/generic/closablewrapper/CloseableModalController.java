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

package org.olat.core.gui.control.generic.closablewrapper;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.util.ZIndexWrapper;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * @author Felix Jost<br>
 *         Comment: this controller takes a component in its contructor and
 *         wraps a velocity container around it with a single link/button (with
 *         a userdefined displayname) which closes the dialog. <br>
 *         Important: the method getMainComponent is overridden and throws an
 *         Exception, since there is a different method to be used:
 *         activate(WindowController wControl). This reason is the this
 *         controller is intended to be used only as "a popup"/modal dialog
 *         (since it offers the 'close' button) and after clicking that button,
 *         it should disappear by itself. Therefore you can only use it in
 *         conjunction with a WindowsController.
 * 
 * </pre>
 * 
 */
public class CloseableModalController extends DefaultController {
	/**
	 * Comment for <code>CLOSE_MODAL_EVENT</code>
	 */
	public static final Event CLOSE_MODAL_EVENT = new Event("CLOSE_MODAL_EVENT");
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(CloseableModalController.class);
	
	private Link closeIcon;
	private boolean displayAsOverlay;
	private VelocityContainer myContent;


	/**
	 * @param wControl
	 * @param closeButtonText
	 * @param modalContent
	 */
	public CloseableModalController(WindowControl wControl, String closeButtonText, Component modalContent) {
		this(wControl, closeButtonText, modalContent, true, null);
	}
	
	public CloseableModalController(WindowControl wControl, String closeButtonText, Component modalContent, boolean displayAsOverlay, String title) {
		 this(wControl, closeButtonText, modalContent, displayAsOverlay, title, true);
	}

	public CloseableModalController(WindowControl wControl, String closeButtonText, Component modalContent, boolean showCloseIcon) {
		 this(wControl, closeButtonText, modalContent, true, null, showCloseIcon);
	}

	/**
	 * Additional constructor if display of content as overlay is not suitable. 
	 * @param wControl
	 * @param closeButtonText
	 * @param modalContent
	 * @param showAsOverlay
	 * @param showCloseIcon make visibility of close-button optional
	 */
	public CloseableModalController(WindowControl wControl, String closeButtonText, Component modalContent, boolean displayAsOverlay, String title, boolean showCloseIcon) {
		super(wControl);
		final Panel guiMsgPlace = new Panel("guimessage_place");
		myContent = new VelocityContainer("closeablewrapper", VELOCITY_ROOT + "/index.html", null, this) {
			public void validate(UserRequest ureq, ValidationResult vr) {
				super.validate(ureq, vr);
				// just before rendering, we need to tell the windowbackoffice that we are a favorite for accepting gui-messages.
				// the windowbackoffice doesn't know about guimessages, it is only a container that keeps them for one render cycle
				WindowBackOffice wbo = getWindowControl().getWindowBackOffice();
				List<ZIndexWrapper> zindexed = wbo.getGuiMessages();
				zindexed.add(new ZIndexWrapper(guiMsgPlace, 20));
			}
		};
		myContent.put("guimessage", guiMsgPlace);
		if (showCloseIcon) {
			closeIcon = LinkFactory.createIconClose(closeButtonText, myContent, this);
			closeIcon.setDomReplacementWrapperRequired(false);
		}
		if (title != null) {
			myContent.contextPut("title", StringHelper.escapeHtml(title));
		}
		myContent.put("modalContent", modalContent); // use our own name
		this.displayAsOverlay = displayAsOverlay;

		setInitialComponent(myContent);
	}
	
	/**
	 * Suppress the form warning on close. This can be used for selection
	 * popup.
	 * 
	 */
	public void suppressDirtyFormWarningOnClose() {
		if(closeIcon != null) {
			closeIcon.setSuppressDirtyFormWarning(true);
		}
	}
	
	public void setCustomCSS(String className){
		myContent.contextPut("cssClass", className);
	}
	
	public void setCustomWindowCSS(String cssClass){
		myContent.contextPut("windowCssClass", cssClass);
	}
	
	public void setContextHelp(Translator trans, String pageName) {
		myContent.contextPut("off_chelp_url", pageName);
		// unfortunately the closable modal controller is instantiated without
		// ureq or locale, thus does not have a translator
		myContent.setTranslator(trans);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == closeIcon){
			getWindowControl().pop();
			fireEvent(ureq, CLOSE_MODAL_EVENT);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#getInitialComponent()
	 */
	public Component getInitialComponent() {
		throw new RuntimeException("please use activate() instead");
	}

	public void activate() {
		if (displayAsOverlay) getWindowControl().pushAsModalDialog(myContent);
		else getWindowControl().pushToMainArea(myContent);
	}
	
	/**
	 * deactivates the modal controller. please do use this method here instead of getWindowControl().pop() !
	 *
	 */
	public void deactivate() {
		getWindowControl().pop();
	}

	@Override
	protected void doDispose() {
		getWindowControl().removeModalDialog(myContent);
	}
}