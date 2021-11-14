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

package org.olat.gui.demo.guidemo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.admin.user.UserSearchController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.text.TextComponent;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.components.updown.UpDown;
import org.olat.core.gui.components.updown.UpDown.Layout;
import org.olat.core.gui.components.updown.UpDownEvent;
import org.olat.core.gui.components.updown.UpDownEvent.Direction;
import org.olat.core.gui.components.updown.UpDownFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.dev.controller.SourceViewController;
import org.olat.core.logging.OLATRuntimeException;


public class GuiDemoLinksController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private Link buttonXSmall;
	private Link buttonSmall;
	private Link button;
	private Link buttonDirty;
	private Link buttonPreferred;
	
	private Link link;
	private Link linkExtern;	
	private Link linkBack, linkPos, linkTooltip;
	private Link linkMail;
	private Link iconButton;
	private Link buttonLongTrans;
	private Link buttonCloseIcon;
	
	private List<UpDownWrapper> upDowns;
	
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
		buttonDirty = LinkFactory.createButton("button.dirty", mainVC, this);
		buttonDirty.setCustomEnabledLinkCSS("btn btn-default o_button_dirty");
		buttonPreferred = LinkFactory.createButton("button.preferred", mainVC, this);
		buttonPreferred.setCustomEnabledLinkCSS("btn btn-primary");

		Link buttonDisabled = LinkFactory.createCustomLink("button.disabled", "button.disabled", "button.disabled", Link.BUTTON, mainVC, this);
		buttonDisabled.setEnabled(false);
		
		iconButton = LinkFactory.createCustomLink("sonne", "cmd.sonne", "", Link.NONTRANSLATED, mainVC, this);
		iconButton.setCustomEnabledLinkCSS("demoext_bild");
		iconButton.setCustomDisabledLinkCSS("demoext_bild");
		
		buttonLongTrans = LinkFactory.createButton("button.long.trans", mainVC, this);
		
		buttonCloseIcon = LinkFactory.createIconClose("This is the hovertext!", mainVC, this);
		
		link = LinkFactory.createLink("link", mainVC, this);
		linkBack = LinkFactory.createLinkBack(mainVC, this);
		linkExtern = LinkFactory.createCustomLink("link.ext", "link.ext", "link.ext", Link.LINK, mainVC, this);	
		linkExtern.setIconLeftCSS("o_icon o_icon_link_extern");
		linkMail = LinkFactory.createCustomLink("link.mail", "link.mail", "link.mail", Link.LINK, mainVC, this);	
		linkMail.setIconLeftCSS("o_icon o_icon_mail");
		
		linkPos = LinkFactory.createCustomLink("link.pos", "link.pos", "link.pos", Link.LINK, mainVC, this);
		linkPos.registerForMousePositionEvent(true);
		
		linkTooltip = LinkFactory.createCustomLink("link.tooltip", "link.tooltip", "link.tooltip", Link.LINK, mainVC, this);
		linkTooltip.setTooltip("link.tooltip.text");
		
		upDowns = new ArrayList<>();
		Layout layout = Layout.LINK_HORIZONTAL;
		UpDown upDownA = UpDownFactory.createUpDown("updown-a", layout, mainVC, this);
		upDownA.setUserObject("A");
		upDowns.add(new UpDownWrapper("A", upDownA));
		UpDown upDownB = UpDownFactory.createUpDown("updown-b", layout, mainVC, this);
		upDownA.setUserObject("B");
		upDowns.add(new UpDownWrapper("B", upDownB));
		UpDown upDownC = UpDownFactory.createUpDown("updown-c", layout, mainVC, this);
		upDownA.setUserObject("C");
		upDowns.add(new UpDownWrapper("C", upDownC));
		UpDown upDownD = UpDownFactory.createUpDown("updown-d",layout,  mainVC, this);
		upDownA.setUserObject("D");
		upDowns.add(new UpDownWrapper("D", upDownD));
		doDisableUpDowns();
		mainVC.contextPut("upDowns", upDowns);
		
		// add some text components
		TextFactory.createTextComponentFromString("text.simple", "Hello World, this text is hardcoded", null, true, mainVC);
		TextFactory.createTextComponentFromI18nKey("text.translated", "text.translated", getTranslator(), null, true, mainVC);
		counterText = TextFactory.createTextComponentFromString("text.simple.counter", "I'm counting events fron this controller: 0", null, true, mainVC);
		TextFactory.createTextComponentFromString("text.span", "I'm a text in a SPAN", null, true, mainVC);

		TextFactory.createTextComponentFromString("text.div.info", "I'm a text in a DIV (with optional CSS class <b>o_info</b>)", "o_info", false, mainVC);
		TextFactory.createTextComponentFromString("text.div.note", "I'm a text in a DIV (with optional CSS class <b>o_note</b>)", "o_note", false, mainVC);
		TextFactory.createTextComponentFromString("text.div.important", "I'm a text in a DIV (with optional CSS class <b>o_important</b>)", "o_important", false, mainVC);
		TextFactory.createTextComponentFromString("text.div.success", "I'm a text in a DIV (with optional CSS class <b>o_success</b>)", "o_success", false, mainVC);
		TextFactory.createTextComponentFromString("text.div.warning", "I'm a text in a DIV (with optional CSS class <b>o_warning</b>)", "o_warning", false, mainVC);
		TextFactory.createTextComponentFromString("text.div.error", "I'm a text in a DIV (with optional CSS class <b>o_error</b>)", "o_error", false, mainVC);
		
		//add sourceview control
		Controller sourceView = new SourceViewController(ureq, wControl, this.getClass(), mainVC);
		mainVC.put("sourceview", sourceView.getInitialComponent());
		
		// form buttons
		mainVC.put("formbuttonctr",new FormButtonsDemoController(ureq, wControl).getInitialComponent());
		
		putInitialPanel(mainVC);		
	}

	@Override
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
		} else if(source == linkPos) {
			int offsetX = linkPos.getOffsetX();
			int offsetY = linkPos.getOffsetY();
			showInfo("link.pos.info", "X:" + offsetX + "px - Y:" + offsetY + "px");
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
		} else if (event instanceof UpDownEvent) {
			UpDownEvent ude = (UpDownEvent) event;
			doMoveUpDownWrapper(source, ude.getDirection());
			mainVC.setDirty(true);
		}
	}

	private void doMoveUpDownWrapper(Component source, Direction direction) {
		Integer index = getUpDownWrapperIndxex(source);
		if (Direction.UP.equals(direction)) {
			Collections.swap(upDowns, index - 1, index);
		} else {
			Collections.swap(upDowns, index, index + 1);
		}
		doDisableUpDowns();
	}

	private void doDisableUpDowns() {
		for (UpDownWrapper upDownWrapper : upDowns) {
			upDownWrapper.getComponent().setTopmost(false);
			upDownWrapper.getComponent().setLowermost(false);
		}
		upDowns.get(0).getComponent().setTopmost(true);
		upDowns.get(upDowns.size()-1).getComponent().setLowermost(true);
	}

	private Integer getUpDownWrapperIndxex(Component source) {
		for (int i = 0; i < upDowns.size(); i++) {
			UpDownWrapper upDownWrapper = upDowns.get(i);
			if (source == upDownWrapper.getComponent()) {
				return i;
			}
		}
		return null;
	}
	
	/**
	 * displays a demo-form with buttons (toggle)
	 * 
	 * @author strentini
	 *
	 */
	class FormButtonsDemoController extends FormBasicController {

		public FormButtonsDemoController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl);
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			uifactory.addToggleButton("toggle1", "Toggle 1", formLayout, null, null);			
			FormToggle fi1 = uifactory.addToggleButton("toggle_slide1", "&nbsp;", formLayout, null, null);
			FormToggle fi2 = uifactory.addToggleButton("toggle_slide2", "&nbsp;", formLayout, null, null);
			fi1.setEnabled(true);
			fi2.setEnabled(true);
			fi1.toggleOff();
			fi2.toggleOff();
		}

		@Override
		protected void formOK(UserRequest ureq) {
			// do nothing
		}
	}
	
	public static final class UpDownWrapper {
		
		private final String text;
		private final UpDown component;
		
		public UpDownWrapper(String text, UpDown component) {
			this.text = text;
			this.component = component;
		}

		public String getText() {
			return text;
		}

		public UpDown getComponent() {
			return component;
		}
		
	}
}