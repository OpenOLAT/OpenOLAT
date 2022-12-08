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

package org.olat.core.gui.components.link;

import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * offer convenience methods to create and add buttons/links to Velocity Containers quick and easy</li>
 *
 * Typical usage (see also GuiDemoLinksController.java):
 * <ol>
 * <li>instantiate your VelocityContrainter<br>
 * <code>mainVC = createVelocityContainer("guidemo-links");</code></li>
 * <li>create your button<br>
 * <code>button = LinkFactory.createButton("button", mainVC, this);</code>
 * <li>save it as instance variable, that the controller can listeningController catch its events</li>
 * <li>add to your velocity page, that shows up the button <code>$r.render("button")</code></li>
 * <li>and finally listen to the events the button fired in the <code>listeningController.event(UserRequest ureq, Component source, Event event)</code><br>
 * <code>public void event(UserRequest ureq, Component source, Event event) {
 *	if (source == button){
 *		// do something
 *	}
 * }
 * </code>
 * </li>
 * </ol>
 * Initial Date: August 10, 2006 <br>
 * 
 * @author Alexander Schneider
 */
public class LinkFactory {

	/**
	 * add a back link to the <code>vc</code> Velocity Container and make the <code>listeningController</code> listen to this back link.<p>
	 * Follow these instructions to show the back link and catch its events:
	 * <ol>
	 * <li><code>$r.render("backLink")</code> in your velocity page, that the link shows up.</li>
	 * <li>save the returned link as a instance variable <code>myBackLink</code></li>
	 * <li>in the <code>listeningController.event(UserRequest ureq, Component source, Event event)</code> you catch the back link by<br><code>if(source == myBackLink){..your stuff here..}</code></li>
	 * </ol>
	 * 
	 * @param vc the VelocityContainer within you put this link
	 * @param listener
	 * @return the link component
	 */
	public static Link createLinkBack(VelocityContainer vc, ComponentEventListener listener){
		Link backLink = new Link("backLink", "back", "back", Link.LINK_BACK, vc, listener);
		backLink.setAccessKey("b");
		backLink.setIconLeftCSS("o_icon o_icon_back");
		return backLink;
	}
	
	/**
	 * add a close icon to the <code>vc</code> Velocity Container and make the <code>listeningController</code> listen to the user's click on the close icon.<p>
	 * Follow these instructions to show the close icon and catch its events:
	 * <ol>
	 * <li><code>$r.render("closeIcon")</code> in your velocity page, that the link shows up.</li>
	 * <li>save the returned link as a instance variable <code>myCloseIcon</code></li>
	 * <li>in the <code>listeningController.event(UserRequest ureq, Component source, Event event)</code> you catch the close icon by<br><code>if(source == close icon){..your stuff here..}</code></li>
	 * </ol>
	 * 
	 * @param title
	 * - displayed on hovering over the icon
	 * - can be null, then the standard close text is shown
	 * @param vc
	 * @param listener
	 * @return Link which display just the close icon
	 */
	public static Link createIconClose(String title, VelocityContainer vc, ComponentEventListener listener){
		Link closeIcon = new Link("closeIcon", "close", "", Link.LINK_CUSTOM_CSS + Link.NONTRANSLATED, vc, listener);
		closeIcon.setElementCssClass("close");
		closeIcon.setIconLeftCSS("o_icon o_icon_close");
		// a11y: set either custom or standard close title
		if(title != null){
			closeIcon.setTitle(title);
		}
		closeIcon.setAriaRole(Link.ARIA_ROLE_BUTTON);
		return closeIcon;
	}
	
	/**
	 * add a link to the <code>vc</code> Velocity Container and make the <code>listeningController</code> listen to this link.<p>
	 * Follow these instructions to show the link and catch its events:
	 * <ol>
	 * <li><code>$r.render("myLink")</code> in your velocity page, that the link shows up.</li>
	 * <li>save the returned link as a instance variable <code>myLink</code></li>
	 * <li>in the <code>listeningController.event(UserRequest ureq, Component source, Event event)</code> you catch the back link by<br><code>if(source == myLink){..your stuff here..}</code></li>
	 * </ol>
	 * 
	 * @param one string for name of component, command and i18n key
	 * @param vc the VelocityContainer within you put this link
	 * @param listener
	 * @return the link component
	 */
	public static Link createLink(String name, VelocityContainer vc, ComponentEventListener listener){
		return new Link(name, name, name, Link.LINK, vc, listener);
	}
	
	public static Link createLink(String name, Translator translator, ComponentEventListener listener){
		Link link = new Link(name, name, name, Link.LINK, null, listener);
		link.setTranslator(translator);
		return link;
	}

	public static Link createLink(String name, Translator translator, ComponentEventListener listener, int presentation) {
		Link link = new Link(name, name, name, presentation, null, listener);
		link.setTranslator(translator);
		return link;
	}
	
	public static Link createLink(String name, String cmd, Translator translator, VelocityContainer vc, ComponentEventListener listener, int presentation) {
		Link link = new Link(name, cmd, name, presentation, vc, listener);
		link.setTranslator(translator);
		return link;
	}
	
	public static Link createLink(String id, String name, String cmd, String i18n, Translator translator, VelocityContainer vc, ComponentEventListener listener, int presentation) {
		Link link = new Link(id, name, cmd, i18n, presentation, vc, listener);
		link.setTranslator(translator);
		return link;
	}
	
	/**
	 * add a link to the <code>vc</code> Velocity Container and make the <code>listeningController</code> listen to this link.<p>
	 * Follow these instructions to show the link and catch its events:
	 * <ol>
	 * <li><code>$r.render("myLink")</code> in your velocity page, that the link shows up.</li>
	 * <li>save the returned link as a instance variable <code>myLink</code></li>
	 * <li>in the <code>listeningController.event(UserRequest ureq, Component source, Event event)</code> you catch the back link by<br><code>if(source == myLink){..your stuff here..}</code></li>
	 * </ol>
	 * 
	 * @param id A fix identification for state-less behavior, must be unique
	 * @param one string for name of component, command and i18n key
	 * @param vc the VelocityContainer within you put this link
	 * @param listener
	 * @return the link component
	 */
	public static Link createLink(String id, String name, VelocityContainer vc, ComponentEventListener listener){
		return new Link(id, name, name, name, Link.LINK, vc, listener);
	}
	
	public static Link createLink(String id, String name, String cmd,  VelocityContainer vc, ComponentEventListener listener) {
		return new Link(id, name, cmd, name, Link.LINK, vc, listener);
	}

	/**
	 * add a customized link to the <code>vc</code> Velocity Container and make the <code>listeningController</code> listen to this link. A customized link means that you can
	 * configure everything by yourself using the constants of the link component e.g. <code>Link.NONTRANSLATED</code><p>
	 * Follow these instructions to show the customized link and catch its events:
	 * <ol>
	 * <li><code>$r.render("myCustomizedLink")</code> in your velocity page, that the link shows up.</li>
	 * <li>save the returned link as a instance variable <code>myCustomizedLink</code></li>
	 * <li>in the <code>listeningController.event(UserRequest ureq, Component source, Event event)</code> you catch the back link by<br><code>if(source == myLink){..your stuff here..}</code></li>
	 * </ol>
	 *  
	 * @param name of the link component
	 * @param cmd command, null or empty string are not allowed
	 * @param key if it's already translated, use at the next parameter Link.NONTRANSLATED, null is allowed
	 * @param presentation
	 * @param vc the VelocityContainer within you put this link
	 * @param listener 
	 * @return the link component
	 */
	
	public static Link createCustomLink(String name, String cmd, String i18nKey, int presentation, VelocityContainer vc, ComponentEventListener listener){
		return new Link(name, cmd, i18nKey, presentation, vc, listener);
	}
	

	
	public static Link createToolLink(String name, String label, ComponentEventListener listener){
		Link link = new Link(name, name, label, Link.LINK | Link.NONTRANSLATED, null, listener);
		link.setDomReplacementWrapperRequired(false);
		return link;
	}
	
	public static Link createToolLink(String name, String cmd, String label, ComponentEventListener listener){
		Link link = new Link(name, cmd, label, Link.LINK | Link.NONTRANSLATED, null, listener);
		link.setDomReplacementWrapperRequired(false);
		return link;
	}
	
	public static Link createToolLink(String name, String label, ComponentEventListener listener, String iconCssClass){
		Link link = new Link(name, name, label, Link.LINK | Link.NONTRANSLATED, null, listener);
		link.setDomReplacementWrapperRequired(false);
		if (iconCssClass != null) {			
			link.setIconLeftCSS("o_icon o_icon-fw " + iconCssClass);
		}
		return link;
	}
	
	/**
	 * add a button to the <code>vc</code> Velocity Container and make the <code>listeningController</code> listen to this button.<p>
	 * Follow these instructions to show the button and catch its events:
	 * <ol>
	 * <li><code>$r.render("myButton")</code> in your velocity page, that the button shows up.</li>
	 * <li>save the returned link as a instance variable <code>myButton</code></li>
	 * <li>in the <code>listeningController.event(UserRequest ureq, Component source, Event event)</code> you catch the button by<br><code>if(source == myButton){..your stuff here..}</code></li>
	 * </ol>
	 * 
	 * @param one string for name of component, command and i18n key
	 * @param vc the VelocityContainer within you put this link
	 * @param listener
	 * @return the link component
	 */
	public static Link createButtonLarge(String name, VelocityContainer vc, ComponentEventListener listener){
		Link link = new Link(name, name, name, Link.BUTTON_LARGE, vc, listener);
		link.setAriaRole(Link.ARIA_ROLE_BUTTON);
		return link;
	}

	/**
	 * add a button to the <code>vc</code> Velocity Container and make the <code>listeningController</code> listen to this button.<p>
	 * Follow these instructions to show the button and catch its events:
	 * <ol>
	 * <li><code>$r.render("myButton")</code> in your velocity page, that the button shows up.</li>
	 * <li>save the returned link as a instance variable <code>myButton</code></li>
	 * <li>in the <code>listeningController.event(UserRequest ureq, Component source, Event event)</code> you catch the button by<br><code>if(source == myButton){..your stuff here..}</code></li>
	 * </ol>
	 * 
	 * @param one string for name of component, command and i18n key
	 * @param vc the VelocityContainer within you put this link
	 * @param listener
	 * @return the link component
	 */
	public static Link createButton(String name, VelocityContainer vc, ComponentEventListener listener){
		Link link = new Link(name, name, name, Link.BUTTON, vc, listener);
		link.setAriaRole(Link.ARIA_ROLE_BUTTON);
		return link;
	}
	
	/**
	 * add a small button to the <code>vc</code> Velocity Container and make the <code>listeningController</code> listen to this small button.<p>
	 * Follow these instructions to show the small button and catch its events:
	 * <ol>
	 * <li><code>$r.render("mySmallButton")</code> in your velocity page, that the small button shows up.</li>
	 * <li>save the returned link as a instance variable <code>mySmallButton</code></li>
	 * <li>in the <code>listeningController.event(UserRequest ureq, Component source, Event event)</code> you catch the small button by<br><code>if(source == mySmallButton){..your stuff here..}</code></li>
	 * </ol>
	 * 
	 * @param one string for name of component, command and i18n key
	 * @param vc the VelocityContainer within you put this link
	 * @param listener
	 * @return the link component
	 */
	public static Link createButtonSmall(String name, VelocityContainer vc, ComponentEventListener listener){
		Link link = new Link(name, name, name, Link.BUTTON_SMALL, vc, listener);
		link.setAriaRole(Link.ARIA_ROLE_BUTTON);
		return link;
	}
	
	/**
	 * add a small button to the <code>vc</code> Velocity Container and make the <code>listeningController</code> listen to this small button.<p>
	 * Follow these instructions to show the small button and catch its events:
	 * <ol>
	 * <li><code>$r.render("mySmallButton")</code> in your velocity page, that the small button shows up.</li>
	 * <li>save the returned link as a instance variable <code>mySmallButton</code></li>
	 * <li>in the <code>listeningController.event(UserRequest ureq, Component source, Event event)</code> you catch the small button by<br><code>if(source == mySmallButton){..your stuff here..}</code></li>
	 * </ol>
	 * 
	 * @param one string for name of component, command and i18n key
	 * @param vc the VelocityContainer within you put this link
	 * @param listener
	 * @return the link component
	 */
	public static Link createButtonSmall(String name, String cmd, String i18n, VelocityContainer vc, ComponentEventListener listener){
		Link link = new Link(name, cmd, i18n, Link.BUTTON_SMALL, vc, listener);
		link.setAriaRole(Link.ARIA_ROLE_BUTTON);
		return link;
	}
	
	/**
	 * add a xsmall button to the <code>vc</code> Velocity Container and make the <code>listeningController</code> listen to this xsmall button.<p>
	 * Follow these instructions to show the xsmall button and catch its events:
	 * <ol>
	 * <li><code>$r.render("myXSmallButton")</code> in your velocity page, that the xsmall button shows up.</li>
	 * <li>save the returned link as a instance variable <code>myXSmallButton</code></li>
	 * <li>in the <code>listeningController.event(UserRequest ureq, Component source, Event event)</code> you catch the xsmall button by<br><code>if(source == myXSmallButton){..your stuff here..}</code></li>
	 * </ol>
	 * 
	 * @param one string for name of component, command and i18n key
	 * @param vc the VelocityContainer within you put this link
	 * @param listener
	 * @return the link component
	 */
	public static Link createButtonXSmall(String name, VelocityContainer vc, ComponentEventListener listener){
		Link link = new Link(name, name, name, Link.BUTTON_XSMALL, vc, listener);
		link.setAriaRole(Link.ARIA_ROLE_BUTTON);
		return link;
	}
	
	public static ExternalLink createExternalLink(String name, String label, String url) {
		ExternalLink link = new ExternalLink(name, label);
		link.setDomReplacementWrapperRequired(false);
		link.setTarget("_blank");
		link.setUrl(url);
		return link;
	}
}
