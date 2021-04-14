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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.badge.Badge;
import org.olat.core.gui.components.form.flexible.FormBaseComponentIdProvider;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;

/**
 * Description:<br>
 * Use @see {@link LinkFactory} to get Link objects.
 * <P>
 * Initial Date: July 06, 2006 <br>
 * 
 * @author Alexander Schneider, Patrick Brunner
 */
public class Link extends AbstractComponent implements ComponentCollection {
	private static final Logger log = Tracing.createLoggerFor(Link.class);
	//single renderer for all users, lazy creation upon first object creation of this class.
	private static final ComponentRenderer RENDERER = new LinkRenderer();
	
	/**
	 * each can be combined with {@link Link#NONTRANSLATED}<br>
	 * can not be combined with each other!
	 */
	public static final int LINK_CUSTOM_CSS = 0;
	public static final int BUTTON_XSMALL = 1;
	public static final int BUTTON_SMALL = 2;
	public static final int BUTTON = 3;
	public static final int BUTTON_LARGE = 4;
	
	public static final int LINK_BACK = 5;
	public static final int LINK = 6;
	/**
	 * to be refactored later into own components
	 */
	public static final int TOOLENTRY_DEFAULT = 7;
	public static final int TOOLENTRY_CLOSE = 8;
		
	/**
	 * can be added to one of the following:
	 * 
	 */
	public static final int NONTRANSLATED = 16;
	public static final int FLEXIBLEFORMLNK = 32;
	
	private String command;
	private int presentation;
	private int presentationBeforeCustomCSS;
	private boolean primary;
	private boolean focus;
	private String i18n;
	private String title;
	private String ariaLabel;
	private final String elementId;
	private String textReasonForDisabling;
	private String customDisplayText;
	private String customEnabledLinkCSS;
	private String customDisabledLinkCSS;
	private String iconLeftCSS;
	private String iconRightCSS;
	private String labelCSS;
	private String target;
	private String url;
	private FormLink flexiLink;
	private Object userObject;
	private String accessKey;
	private boolean active = false;
	private boolean ajaxEnabled = true;
	private boolean registerForMousePositionEvent = false;
	private MouseEvent mouseEvent;
	private String javascriptHandlerFunction;
	//x y coordinates of the mouse position when clicked the link, works only if enabled by registerForMousePositionEvent(true)
	private int offsetX = 0;
	private int offsetY = 0;

	private boolean hasTooltip;
	private boolean suppressDirtyFormWarning = false;
	private boolean forceFlexiDirtyFormWarning = false;

	private Badge badge;
	private Component innerComponent;
	private boolean newWindow;
	private boolean newWindowAfterDispatchUrl;
	private LinkPopupSettings popup;

	/**
	 * 
	 * @param name
	 * @param command should not contain : 
	 * @param i18n
	 * @param presentation
	 * @param title
	 */
	protected Link(String name, String command, String i18n, int presentation, VelocityContainer vc, ComponentEventListener listeningController) {
		this(null, name, command, i18n, presentation, vc, listeningController);
	}
	
	/**
	 * Same as but with fix ID
	 * @param id A fix identifier for the link, must be unique or null
	 * @param name
	 * @param command
	 * @param i18n
	 * @param presentation
	 * @param vc
	 * @param listeningController
	 */
	protected Link(String id, String name, String command, String i18n, int presentation, VelocityContainer vc, ComponentEventListener listeningController) {
		this(id, name, command, i18n, presentation, null);
		if (listeningController == null) throw new AssertException("please provide a listening controller, listeningController is null at the moment");
		addListener(listeningController);
		if (vc != null) vc.put(getComponentName(), this);
		setSpanAsDomReplaceable(true);
	}

	/**
	 * the new flexible forms needs links where the VC and listener are unknown at construction time.
	 * @param id A fix identifier for the link, must be unique or null
	 * @param name
	 * @param command
	 * @param i18n
	 * @param presentation
	 * @param internalAttachedObj
	 */
	public Link(String id, String name, String command, String i18n, int presentation, FormLink flexiLink) {
		super(id, name);
		this.flexiLink = flexiLink;
		this.command = command;
		// Directly use the dispatch ID for DOM replacement to minimize DOM tree
		if(flexiLink == null) {
			elementId = "o_c".concat(getDispatchID());
		} else {
			elementId = FormBaseComponentIdProvider.DISPPREFIX.concat(getDispatchID());
		}
		if ( this.command == null || this.command.equals(""))  throw new AssertException("command string must be a valid string and not null");
		this.i18n = i18n;
		this.presentation = presentation;
		this.presentationBeforeCustomCSS = presentation;
		
		if(log.isDebugEnabled()){
			log.debug("***LINK_CREATED***" 
					+ " name: " + getComponentName()  
					+ " component: " + getComponentName() 
					+ " dispatchId: " + getDispatchID());
		}
		// use span wrappers - if the custom layout needs div wrappers this flag has
		// to be set manually
		setSpanAsDomReplaceable(true);
		setDomReplacementWrapperRequired(false);
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		setDirty(true);
		String cmd = ureq.getParameter(VelocityContainer.COMMAND_ID);
		
		if(log.isDebugEnabled()){
			log.debug("***LINK_CLICKED*** " 
					+ " dispatchID: " + ureq.getComponentID()
					+ " commandID: " + cmd);
		}
		
		dispatch(ureq, command);
	}

	/**
	 * @param ureq
	 * @param command2
	 */
	private void dispatch(UserRequest ureq, String cmd) {
		if(!command.equals(cmd)){
			throw new AssertException("hack attempt! command does not match the one from the UserRequest! Command recieved: " + cmd + " expected: " + command);
		}
		
		if (registerForMousePositionEvent) setXYOffest(ureq);
		fireEvent(ureq, new Event(cmd));
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	public String getCommand() {
		return command;
	}

	public String getI18n() {
		return i18n;
	}

	public boolean isPrimary() {
		return primary;
	}
	
	public void setPrimary(boolean isPrimary) {
		primary = isPrimary;
	}
	
	/**
	 * Sets the focus in the DOM tree to this link element if possible. Note that only one
	 * DOM element can have the focus, so this does not give any guarantee that the focus will
	 * be on that element in call cases. 
	 * 
	 * @param focus true: element should have focus in DOM; false: no focused
	 */
	public void setFocus(boolean focus){
		this.focus = focus;
	}

	/**
	 * @return true: element should have focus in DOM; false: no focused
	 */
	public boolean isFocus(){
		return focus;
	}

	public boolean isPopup() {
		return popup != null;
	}

	public void setPopup(boolean popup) {
		if(popup) {
			this.popup = new LinkPopupSettings();
		} else {
			this.popup = null;
		}
	}
	
	public LinkPopupSettings getPopup() {
		return popup;
	}
	
	public void setPopup(LinkPopupSettings popup) {
		this.popup = popup;
	}
	
	public boolean isNewWindow() {
		return newWindow;
	}
	
	public boolean isNewWindowAfterDispatchUrl() {
		return newWindowAfterDispatchUrl;
	}

	public void setNewWindow(boolean newWindow, boolean afterDispatchUrl) {
		this.newWindow = newWindow;
		this.newWindowAfterDispatchUrl = afterDispatchUrl;
	}

	public Badge getBadge() {
		return badge;
	}
	
	public void setBadge(String message, Badge.Level level) {
		if(badge == null) {
			badge = new Badge(getComponentName() + "_BADGE");
		}
		badge.setMessage(message);
		badge.setLevel(level);
		setDirty(true);
	}
	
	public void removeBadge() {
		badge = null;
		setDirty(true);
	}

	public int getPresentation() {
		return presentation;
	}

	/**
	 * The link title, text that shows up when mouse hovers over link. Not the
	 * link text. <br>
	 * If ((getPresentation() - Link.NONTRANSLATED) >= 0 ) the returned title is
	 * already translated, otherwise an untranslated i18n key is returned
	 * 
	 * @return
	 */
	public String getTitle() {
		return title;
	}
	
	public String getAriaLabel() {
		return ariaLabel;
	}
	
	FormLink getFlexiForm() {
		return flexiLink;
	}
	
	public Component getInnerComponent() {
		return innerComponent;
	}
	
	public void setInnerComponent(Component component) {
		this.innerComponent = component;
	}
	
	@Override
	public Component getComponent(String name) {
		if(badge != null && badge.getComponentName().equals(name)) {
			return badge;
		}
		if(innerComponent != null && innerComponent.getComponentName().equals(name)) {
			return innerComponent;
		}
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		List<Component> components = new ArrayList<>(2);
		if(badge != null) {
			components.add(badge);
		}
		if(innerComponent != null) {
			components.add(innerComponent);
		}
		return components;
	}

	MouseEvent getMouseEvent() {
		return mouseEvent;
	}

	String getJavascriptHandlerFunction() {
		return javascriptHandlerFunction;
	}

	boolean isRegisterForMousePositionEvent() {
		return registerForMousePositionEvent;
	}

	/**
	 * Set an link title which gets displayed when hovering over the link.
	 * <br>
	 * If ((getPresentation() - Link.NONTRANSLATED) >= 0 ) the provided title is
	 * already translated, otherwise its an untranslated i18n key 
	 * 
	 * @param the i18n key or the translated key depending on presentation mode
	 */
	public void setTitle(String i18nKey) {
		this.title = i18nKey;
	}
	
	public void setAriaLabel(String i18nKey) {
		this.ariaLabel = i18nKey;
	}

	/**
	 * Only used in olat flexi form stuff
	 * @return returns the custom setted element id
	 */
	protected String getElementId() {
		return elementId;
	}

	public boolean hasTooltip() {
		return hasTooltip;
	}
	
	/**
	 * @see org.olat.core.gui.components.Component#setEnabled(boolean)
	 * @param true or false
	 */
	@Override
	public void setEnabled(boolean b){
		super.setEnabled(b);
		setDirty(true);
	}

	protected String getTextReasonForDisabling() {
		return textReasonForDisabling;
	}

	public void setTextReasonForDisabling(String textReasonForDisabling) {
		this.textReasonForDisabling = textReasonForDisabling;
	}

	/**
	 * @return the custom CSS class used for a disabled link
	 */
	public String getCustomDisabledLinkCSS() {
		return customDisabledLinkCSS;
	}

	/**
	 * @param customDisabledLinkCSS the custom CSS class used for a disabled link
	 */
	public void setCustomDisabledLinkCSS(String customDisabledLinkCSS) {
		this.customDisabledLinkCSS = customDisabledLinkCSS;
		
		//check if it is a flexi.form link with custom css
		boolean flexiformlink = (presentation - Link.FLEXIBLEFORMLNK) >= 0;
		if (flexiformlink) {
			presentation = presentation - Link.FLEXIBLEFORMLNK;
		}
		
		boolean nontranslated = (presentation - Link.NONTRANSLATED) >= 0;
		if (nontranslated) {
			presentation = Link.NONTRANSLATED;
		} else {
			presentation = Link.LINK_CUSTOM_CSS;
		}
		//enable the flexi.form info again
		if(flexiformlink){
			presentation += Link.FLEXIBLEFORMLNK;
		}
		setDirty(true);
	}

	/**
	 * @return the custom CSS class used for a enabled link
	 */
	public String getCustomEnabledLinkCSS() {
		return customEnabledLinkCSS;
	}

	/**
	 * @param customEnabledLinkCSS the custom CSS class used for a enabled link
	 */
	public void setCustomEnabledLinkCSS(String customEnabledLinkCSS) {
		this.customEnabledLinkCSS = customEnabledLinkCSS;

		//check if it is a flexi.form link with custom css
		boolean flexiformlink = (presentation - Link.FLEXIBLEFORMLNK) >= 0;
		if (flexiformlink) {
			presentation = presentation - Link.FLEXIBLEFORMLNK;
		}
		
		boolean nontranslated = (presentation - Link.NONTRANSLATED) >= 0;
		if (nontranslated) {
			presentation = Link.NONTRANSLATED;
		} else {
			presentation = Link.LINK_CUSTOM_CSS;
		}
		//enable the flexi.form info again
		if(flexiformlink){
			presentation += Link.FLEXIBLEFORMLNK;
		}
		setDirty(true);
	}

	public void removeCSS(){
		this.presentation = presentationBeforeCustomCSS;
		setDirty(true);
	}
	
	public String getTarget() {
		return target;
	}

	/**
	 * allows setting an custom href target like "_blank" which
	 * opens an link in a new window.
	 * As ajax links never should open in a new window, the setTarget automatically disables
	 * the ajax feature in the link.
	 * @param target
	 */
	public void setTarget(String target) {
		this.target = target;
	}
	
	/**
	 * 
	 * @return Internal business path URL for "Open in new window"
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * @param url Set an alternative URL for "open in new window"
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	protected String getAccessKey() {
		return accessKey;
	}

	/**
	 * sets the accesskey, e.g. "5" -> Alt+5 then focusses on this link
	 * @param accessKey
	 */
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public void setAjaxEnabled(boolean ajaxEnabled) {
		this.ajaxEnabled = ajaxEnabled;
	}

	public boolean isAjaxEnabled() {
		return ajaxEnabled;
	}

	/**
	 * When pressing this button, should the system prevent the check for any
	 * unsubmitted forms?
	 * 
	 * @param suppressDirtyFormWarning true: don't check for dirt forms; false:
	 *          check for dirty forms (default)
	 */
	public void setSuppressDirtyFormWarning(boolean suppressDirtyFormWarning) {
		this.suppressDirtyFormWarning = suppressDirtyFormWarning;
	}

	/**
	 * @return true: don't check for dirt forms; false: check for dirty forms
	 *         (default)
	 */
	public boolean isSuppressDirtyFormWarning() {
		return suppressDirtyFormWarning;
	}
	
	/**
	 * @return true if a flexi link must do an extra check of the dirtiness of its form.
	 */
	public boolean isForceFlexiDirtyFormWarning() {
		return forceFlexiDirtyFormWarning;
	}

	/**
	 * 
	 * @param forceFlexiDirtyFormWarning true if the flexi link need to check if the form is dirty.
	 */
	public void setForceFlexiDirtyFormWarning(boolean forceFlexiDirtyFormWarning) {
		this.forceFlexiDirtyFormWarning = forceFlexiDirtyFormWarning;
	}

	/**
	 * The custom display text or null if not set
	 */
	public String getCustomDisplayText() {
		return customDisplayText;
	}

	public void setCustomDisplayText(String customDisplayText) {
		this.customDisplayText = customDisplayText;
		setDirty(true);
	}

	/**
	 * get the mouse position as event.command coded as x123y456 and appended to the UserRequest
	 * catch it inside the event method with the ureq.getModuleUri() method.<br>
	 * Uses prototype.js
	 * @param b
	 */
	public void registerForMousePositionEvent(boolean b) {
		this.registerForMousePositionEvent = b;
	}
	
	/**
	 * register a javascript function to an event of this link
	 * <br>
	 * Uses prototype.js
	 * @param event
	 * @param handlerFunction: A javascript function name
	 */
	public void registerMouseEvent(MouseEvent event, String handlerFunction) {
		this.mouseEvent = event;
		this.javascriptHandlerFunction = handlerFunction;
	}
	
	/**
	 * convenience method to set the x and y values you get by <code>link.registerForMousePositionEvent(true)</code>
	 * to x and y
	 * @param ureq
	 * @param offsetX
	 * @param offsetY
	 */
	public void setXYOffest(UserRequest ureq) {
		String xyOffset = ureq.getModuleURI();
		if(xyOffset != null) {
			try {
				offsetX = Integer.parseInt(xyOffset.substring(1, xyOffset
						.indexOf('y')));
				offsetY = Integer.parseInt(xyOffset.substring(xyOffset
						.indexOf('y') + 1, xyOffset.length()));
			} catch (NumberFormatException e) {
				offsetX = 0;
				offsetY = 0;
			}			
		}
	}
	
	/**
	 * valid events for the register mouse event stuff
	 */
	public enum MouseEvent {
		click,
		mousedown,
		mouseup,
		mouseover,
		mousemove,
		mouseout
	}

	/**
	 * returs the mouse position when the link was clicked.
	 * Only available if registerForMousePositionEvent is set true
	 * @return offset x of mouse position
	 */
	public int getOffsetX() {
		return offsetX;
	}
	
	/**
	 * returs the mouse position when the link was clicked.
	 * Only available if registerForMousePositionEvent is set true
	 * @return offset y of mouse position
	 */
	public int getOffsetY() {
		return offsetY;
	}
	
	/**
	 * Sets a tooltip out off the text from the provided i18n key. Tooltips are fastser appearing than normal title tags
	 * and can contain HTML tags. 
	 * @param sticky: sets the tooltip sticky, which means the user has to click the tip to disappear
	 */
	public void setTooltip(String tooltipI18nKey) {
		setTitle(tooltipI18nKey);
		this.hasTooltip = true;
		setDirty(true);
	}

	/**
	 * @param iconCSS The CSS classes used as icons in the i element on the left hand side of the link text
	 */
	public void setIconLeftCSS(String iconCSS) {
		this.iconLeftCSS = iconCSS;
		setDirty(true);
	}
	
	/**
	 * @return The icon CSS classes or NULL 
	 */
	public String getIconLeftCSS() {
		return iconLeftCSS;
	}

	/**
	 * @param iconCSS The CSS classes used as icons in the i element on the right hand side of the link text
	 */
	public void setIconRightCSS(String iconCSS) {
		this.iconRightCSS = iconCSS;
		setDirty(true);
	}
	
	/**
	 * @return The icon CSS classes or NULL 
	 */
	public String getIconRightCSS() {
		return iconRightCSS;
	}

	/**
	 * @return The label (span tag) CSS classes or NULL 
	 */
	public String getLabelCSS() {
		return labelCSS;
	}

	/**
	 * @param labelCSS The CSS classes used for the label (span tag)
	 */
	public void setLabelCSS(String labelCSS) {
		this.labelCSS = labelCSS;
	}

	/**
	 * Compare also with isEnabled();
	 * @return true if the link is active (only a rendering issue); false if link not active
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * Compare also with setEnabled)=
	 * @param isActive true: the link is currently active (only a rendering issue); false: the link is not active right now
	 */
	public void setActive(boolean isActive) {
		if (active == isActive) return;
		active = isActive;
		setDirty(true);
	}
	
	@Override
	public void setDirty(boolean dirty) {
		super.setDirty(dirty);
		if(flexiLink != null) {
			// particularly important for by setDirty false
			if(flexiLink.getExampleC() != null) {
				flexiLink.getExampleC().setDirty(dirty);
			}
			if(flexiLink.getErrorC() != null) {
				flexiLink.getErrorC().setDirty(dirty);
			}
			if(flexiLink.getLabelC() != null) {
				flexiLink.getLabelC().setDirty(dirty);
			}
		}
	}	
}
