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

package org.olat.test.guidemo;

import org.olat.admin.user.UserSearchController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.text.TextComponent;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.dev.controller.SourceViewController;
import org.olat.core.logging.OLATRuntimeException;


public class GuiDemoLinksController extends BasicController {
	
	Panel p;
	VelocityContainer mainVC;
	
	private Link buttonXSmall;
	private Link buttonSmall;
	private Link button;
	private Link link;
	private Link linkExtern;	
	private Link linkBack;
	private Link linkMail;
	private Link iconButton;
	private Link buttonLongTrans;
	private Link buttonCloseIcon;
	private TextComponent counterText;
	private int counter;
	
	private Panel pFirstInvisible;
	private CloseableModalController cmc;
	
	public GuiDemoLinksController(UserRequest ureq, WindowControl wControl) {
		super(ureq,wControl);		
		mainVC = createVelocityContainer("guidemo-links");
		buttonXSmall = LinkFactory.createButtonXSmall("button.xsmall", mainVC, this);
		// to test correctness of ajax mode with invisble components
		pFirstInvisible = new Panel("firstinvisble");
		// for demo only
		UserSearchController usc = new UserSearchController(ureq, getWindowControl(), false);
		pFirstInvisible.setContent(usc.getInitialComponent());
		pFirstInvisible.setVisible(false);
		mainVC.put("ajaxtest", pFirstInvisible);
		
		buttonSmall = LinkFactory.createButtonSmall("button.small", mainVC, this);
		button = LinkFactory.createButton("button", mainVC, this);

		Link buttonDisabled = LinkFactory.createCustomLink("button.disabled", "button.disabled", "button.disabled", Link.BUTTON, mainVC, this);
		buttonDisabled.setEnabled(false);
		
		iconButton = LinkFactory.createCustomLink("sonne", "cmd.sonne", "", Link.NONTRANSLATED, mainVC, this);
		iconButton.setCustomEnabledLinkCSS("demoext_bild");
		iconButton.setCustomDisabledLinkCSS("demoext_bild");
		
		buttonLongTrans = LinkFactory.createButton("button.long.trans", mainVC, this);
		
		buttonCloseIcon = LinkFactory.createIconClose("This is the hovertext!", mainVC, this);
		
		// let the scripts (.js files) and css files be included when this controller's main component is rendered
		JSAndCSSComponent jscss = new JSAndCSSComponent("jsAndCssForDemo", this.getClass(), null, "style.css", false);
		mainVC.put("jsAndCssForDemo", jscss); // we include it in the render tree, so that the custom js and css are included
		
		link = LinkFactory.createLink("link", mainVC, this);
		linkBack = LinkFactory.createLinkBack(mainVC, this);
		linkExtern = LinkFactory.createCustomLink("link.ext", "link.ext", "link.ext", Link.LINK, mainVC, this);	
		linkExtern.setCustomEnabledLinkCSS("b_link_extern");
		linkMail = LinkFactory.createCustomLink("link.mail", "link.mail", "link.mail", Link.LINK, mainVC, this);	
		linkMail.setCustomEnabledLinkCSS("b_link_mailto");
		
		// add some text components
		TextFactory.createTextComponentFromString("text.simple", "Hello World, this text is hardcoded", null, true, mainVC);
		TextFactory.createTextComponentFromI18nKey("text.translated", "text.translated", getTranslator(), null, true, mainVC);
		counterText = TextFactory.createTextComponentFromString("text.simple.counter", "I'm counting events fron this controller: 0", null, true, mainVC);
		TextFactory.createTextComponentFromString("text.span", "I'm a text in a SPAN", "b_dimmed b_border_box", true, mainVC);
		TextFactory.createTextComponentFromString("text.div", "I'm a text in a DIV", "b_warning b_border_box", false, mainVC);
		
		//add sourceview control
		Controller sourceView = new SourceViewController(ureq, wControl, this.getClass(), mainVC);
		mainVC.put("sourceview", sourceView.getInitialComponent());
		
		p = putInitialPanel(mainVC);		
	}

	public void event(UserRequest ureq, Component source, Event event) {
		// update counter on each click to demo setter method on text component
		counter++;
		counterText.setText("I'm counting events fron this controller: " + counter);
		
		if (source == buttonXSmall){			
			//getWindowControl().setInfo("Hi "+ureq.getIdentity().getName()+ ", you clicked on the button xsmall");
			// to test ajax mode, switch visible/invisble
			pFirstInvisible.setVisible(!pFirstInvisible.isVisible());
		} else if (source == buttonSmall){
			//showInfo("info.button.small", ureq.getIdentity().getName());
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), "close me", link);
			listenTo(cmc);
			
			cmc.activate();
		} else if (source == button){
			showInfo("info.button.default", ureq.getIdentity().getName());			
		} else if (source == link){
			showInfo("info.button.link", ureq.getIdentity().getName());			
		} else if (source == linkBack){
			showInfo("info.button.link.back", ureq.getIdentity().getName());			
		} else if (source == iconButton){
			showInfo("info.button.icon", ureq.getIdentity().getName());			
		} else if (source == buttonLongTrans){
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				throw new OLATRuntimeException(this.getClass(), "Error while let OLAT sleeping for the purpose of demo", e);
			}
			showInfo("info.button.long.trans", ureq.getIdentity().getName());			
		} else if (source == buttonCloseIcon){
			showInfo("info.button.close.icon", ureq.getIdentity().getName());			
		}
	}

	protected void doDispose() {
		//
	}

}
