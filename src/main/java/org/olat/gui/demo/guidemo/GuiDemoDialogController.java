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
package org.olat.gui.demo.guidemo;

import java.util.ArrayList;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.dev.controller.SourceViewController;
import org.olat.core.logging.AssertException;

/**
 * <h3>Description:</h3>
 * Demonstration of what you can do with dialogs
 * <h3>Events thrown by this controller:</h3>
 * <ul>
 * <li>ButtonClickedEvent: when user clicks a button provided in the
 * constructor</li>
 * <li>Event.CANCELLED_EVENT: when user clicks the close icon in the window bar</li>
 * </ul>
 * <p>
 * Initial Date: 26.11.2007<br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class GuiDemoDialogController extends BasicController {

	VelocityContainer vcMain;
	private Link yesNoButton, okCancelButton, genericDialogButton, noCloseButton, customCssButton;
	private DialogBoxController dialogBoxOne;
	private DialogBoxController dialogBoxTwo;
	private DialogBoxController dialogBoxThree;
	private DialogBoxController dialogBoxSpecialCSS;
	private DialogBoxController dialogBoxWithoutClose;
	private ArrayList<String> myButtons;
	private Link guimsgButton;
	private StackedPanel mainP;
	private MessageController guimsg;

	public GuiDemoDialogController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		vcMain = this.createVelocityContainer("guidemo-dialog");
		yesNoButton = LinkFactory.createButton("guidemo.dialog.yesno", vcMain, this);
		okCancelButton = LinkFactory.createButton("guidemo.dialog.okcancel", vcMain, this);
		genericDialogButton = LinkFactory.createButton("guidemo.dialog.generic", vcMain, this);
		customCssButton = LinkFactory.createButton("guidemo.dialog.customcss", vcMain, this);
		noCloseButton = LinkFactory.createButton("guidemo.dialog.noclose", vcMain, this);
		guimsgButton = LinkFactory.createButton("guidemo.dialog.guimsg", vcMain, this);
		
		//add source view control
		Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), vcMain);
		vcMain.put("sourceview", sourceview.getInitialComponent());
		
		mainP = putInitialPanel(vcMain);
	}

	public void event(UserRequest ureq, Component source, Event event) {
		if (source == yesNoButton) {
			dialogBoxOne = activateYesNoDialog(ureq, "Hello World",
					"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aliquam id quam in dui pellentesque sodales?", dialogBoxOne);
		}
		if (source == okCancelButton) {
			dialogBoxTwo = activateOkCancelDialog(ureq, "Hello World",
					"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aliquam id quam in dui pellentesque sodales?", dialogBoxTwo);
		}
		if (source == genericDialogButton) {
			// create list of internationalized button texsts
			myButtons = new ArrayList<>();
			myButtons.add("Lorem");
			myButtons.add("Ipsum");
			myButtons.add("Dolor");
			dialogBoxThree = activateGenericDialog(ureq, "Hello World",
					"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aliquam id quam in dui pellentesque sodales?", myButtons,
					dialogBoxThree);
		}
		if (source == customCssButton) {
			dialogBoxSpecialCSS = activateYesNoDialog(ureq, "Hello World",
					"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aliquam id quam in dui pellentesque sodales?", dialogBoxSpecialCSS);
			// use custom CSS: in this case with a special icon
			dialogBoxSpecialCSS.setCssClass("o_warning");
		}
		if (source == noCloseButton) {
			dialogBoxWithoutClose = activateYesNoDialog(ureq, "Hello World",
					"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aliquam id quam in dui pellentesque sodales?", dialogBoxWithoutClose);
			dialogBoxWithoutClose.setCloseWindowEnabled(false);
		}
		if(source == guimsgButton){
			guimsg = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), "Helau", "asdifasdlkf sdlfasfd asdf.");
			listenTo(guimsg);
			mainP.pushContent(guimsg.getInitialComponent());
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		String feedbackMessage = null;
		if (source == dialogBoxOne) {
			if (event == Event.CANCELLED_EVENT) {
				feedbackMessage = "close icon clicked";
			} else {
				if (DialogBoxUIFactory.isYesEvent(event)) {
					feedbackMessage = "yes clicked";
				} else {
					feedbackMessage = "no clicked";
				}
			}
		} else if (source == dialogBoxTwo) {
			if (event == Event.CANCELLED_EVENT) {
				feedbackMessage = "close icon clicked";
			} else {
				if (DialogBoxUIFactory.isOkEvent(event)) {
					feedbackMessage = "ok clicked";
				} else {
					feedbackMessage = "cancel clicked";
				}
			}
		} else if (source == dialogBoxThree) {
			if (event == Event.CANCELLED_EVENT) {
				feedbackMessage = "close icon clicked";
			} else {
				int pos = DialogBoxUIFactory.getButtonPos(event);
				feedbackMessage = myButtons.get(pos) + " clicked on position:" + pos;
			}
		} else if (source == dialogBoxSpecialCSS) {
			if (event == Event.CANCELLED_EVENT) {
				feedbackMessage = "close icon clicked";
			} else {
				if (DialogBoxUIFactory.isYesEvent(event)) {
					feedbackMessage = "yes clicked";
				} else {
					feedbackMessage = "no clicked";
				}
			}
		} else if (source == dialogBoxWithoutClose) {
			if (event == Event.CANCELLED_EVENT) {
				throw new AssertException("close icon pressed, but this should not be available.");
			} else {
				if (DialogBoxUIFactory.isYesEvent(event)) {
					feedbackMessage = "yes clicked";
				} else {
					feedbackMessage = "no clicked";
				}
			}
		}
		if (feedbackMessage != null) {
			getWindowControl().setInfo(feedbackMessage + "command was:" + event.getCommand());
		} else {
			throw new AssertException("feedback message is NULL, workflow error!");
		}
	}
}
