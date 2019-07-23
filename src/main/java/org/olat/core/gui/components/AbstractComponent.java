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

package org.olat.core.gui.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.util.CodeHelper;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public abstract class AbstractComponent implements Component {

	private static final Logger log_ = Tracing.createLoggerFor(AbstractComponent.class);

	private boolean spanReplaceable = false;

	private final String name;
	private final String dispatchID;
	private String elementCssClass;

	private long timestamp = 1l;
	private String timestampID = "1";
	private boolean staticCmp = true;
	
	private boolean visible = true;
	private boolean enabled = true;
	// true when contents have changed since last rendering
	private boolean dirty = false;
	private boolean domReplaceable = true;
	private boolean domReplacementWrapperRequired = true;

	private final List<ComponentEventListener> listeners;
	private Translator translator;
	// for debugging reasons to trace where the latest dispatch occured
	private Controller latestDispatchedController;
	// for debugging reasons to trace which event was latest fired before an
	// exception
	private Event latestFiredEvent;
	// watch only
	
	/**
	 * do not create a logger for this class otherwise millions of useless loggers are created which consumes
	 * quite some memory
	 */
	private ComponentCollection parent;
	/**
	 * 
	 * @param name the name of this component
	 */
	public AbstractComponent(String name) {
		this(null, name, null);
	}
	
	/**
	 * @param id The id of the component, must be unique
	 * @param name The name of this component
	 */
	public AbstractComponent(String id, String name) {
		this(id, name, null);
	}

	/**
	 * 
	 * @param name the name of this component
	 * @param translator the translator
	 */
	public AbstractComponent(String name, Translator translator) {
		this(null, name, translator);
		staticCmp = false;
	}
	
	/**
	 * 
	 * @param id The id of the component, must be unique
	 * @param name The name of this component
	 * @param translator The translator
	 */
	public AbstractComponent(String id, String name, Translator translator) {
		if(id == null) {
			dispatchID = Long.toString(CodeHelper.getRAMUniqueID());
			staticCmp = false;
		} else {
			// OO-98: dispatchID will get used in generated js-code. thus, make sure it
			// is valid as variable name.
			dispatchID = secureJSVarName(id);
			staticCmp = true;
		}
					
		this.name = name;
		this.translator = translator;
		listeners = new ArrayList<>(1);
	}
	
	/**
	 * OO-98 : a fix in FormUIFactory changed the id from "null" to
	 * "something.like.this" for selectionElements (like radio-buttons)
	 * this led to js-errors because output was:  var o_fisomething.like.this [..]
	 * now this method ensures that the id does not contain dots 
	 * 
	 * @param id
	 * @return a valid JS variableName
	 */
	private static String secureJSVarName(String id) {
		if(StringUtils.isBlank(id)) {
			return "o_" + Long.toString(CodeHelper.getRAMUniqueID());
		}
		id = id.replace("-", "_"); // no - 
		id =  id.replace(".", "_"); // no dots
		
		// no numbers at the beginning
		char c = id.charAt(0);
        if (c <='/' || c >= ':') {
        	id = "o"+id;
        }
		return id;
	}

	/**
	 * @return String
	 */
	public String getComponentName() {
		return name;
	}

	public String getElementCssClass() {
		return elementCssClass;
	}

	public void setElementCssClass(String elementCssClass) {
		this.elementCssClass = elementCssClass;
	}

	/**
	 * @return boolean
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * True by default: The component gets rendered<br>
	 * false: The componet gets not rendered.
	 * Sets the visible.
	 * 
	 * @param visible The visible to set
	 */
	public void setVisible(boolean visible) {
		if(visible ^ this.visible){
			this.visible = visible;
			setDirty(true);
		}
	}

	public void dispatchRequest(final UserRequest ureq) {
			doDispatchRequest(ureq);
	}
	
	protected abstract void doDispatchRequest(UserRequest ureq);

	public abstract ComponentRenderer getHTMLRendererSingleton();

	/**
	 * called just before the rendering of the -whole tree- takes place, so e.g.
	 * lazy fetching can be implemented, or issueing a request for a new moduleUri
	 * (e.g. for CPComponent, so that the browser loads images correctly). only
	 * called when the component is visible
	 */
	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		if (this.dirty) {
			if(!staticCmp) {
				timestamp++;
				timestampID = Long.toString(timestamp);
			}
		}
	}
	
	/**
	 * fires events to registered listeners of generic events.
	 * To see all events set this class and also AbstractEventBus and DefaultController to debug.
	 * @param event
	 * @param ores
	 */
	protected void fireEvent(final UserRequest ureq, final Event event) {
		ComponentEventListener[] listenerArray = new ComponentEventListener[listeners.size()];
		listeners.toArray(listenerArray);
		
		for (ComponentEventListener listenerA:listenerArray) {
			latestFiredEvent = event;
			if(listenerA instanceof Controller) {
				final Controller listener = (Controller)listenerA;
				latestDispatchedController = listener;
				try{
					listener.getUserActivityLogger().frameworkSetBusinessPathFromWindowControl(listener.getWindowControlForDebug());
				} catch(AssertException e) {
					log_.error("Error in setting up the businessPath on the IUserActivityLogger. listener="+listener, e);
					// still continue
				}

				ThreadLocalUserActivityLoggerInstaller.runWithUserActivityLogger(new Runnable() {
					public void run() {
						listener.dispatchEvent(ureq, AbstractComponent.this, event);
					}
					
				}, listener.getUserActivityLogger());
			} else {
				listenerA.dispatchEvent(ureq, AbstractComponent.this, event);
			}

			// clear the event for memory reasons, used only for debugging reasons in
			// case of an error
			// TODO:fj: may be useful for admin purposes
			// FIXME:fj:a rework events so we can get useful info out of it
			latestFiredEvent = null;

		}
	}

	/**
	 * 
	 * @return a list of the controllers listening (normally only one)
	 */
	@Override
	public List<ComponentEventListener> debuginfoGetListeners() {
		return listeners;
	}
	
	/**
	 * @param controller
	 */
	@Override
	public void addListener(ComponentEventListener controller) {
		// tests if the same controller was already registered to avoid
		// double-firing.
		// the contains method is fast since there is normally one one listener
		// (controller) in the listener list.
		if (listeners.contains(controller)) throw new AssertException("controller was already added to component '" + getComponentName()
				+ "', controller was: " + controller.toString());
		listeners.add(controller);
	}

	@Override
	public void removeListener(ComponentEventListener controller) {
		listeners.remove(controller);
	}

	/**
	 * @return Translator
	 */
	public Translator getTranslator() {
		return translator;
	}

	/**
	 * Sets the translator. (for framework internal use)
	 * 
	 * @param translator The translator to set
	 */
	public void setTranslator(Translator translator) {
		this.translator = translator;
	}

	/**
	 * @return long the dispatchid (which is assigned at construction time of the component and never changes)
	 */
	public String getDispatchID() {
		return dispatchID;
	}

	/**
	 * @return the extended debuginfo
	 */
	public String getExtendedDebugInfo() {
		// default impl to be overriden
		return "n/a";
	}

	/**
	 * @return
	 */
	public String getListenerInfo() {
		return "listener:" + listeners.toString();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getComponentName() + " " + super.toString();
	}

	/**
	 * true by default: The componet gets rendered and actions get dispatched
	 * if false: e.g. @see Link the link gets rendered but is not clickable
	 * @return Returns the enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled The enabled to set.
	 */
	public void setEnabled(boolean enabled) {
		if(enabled ^ this.enabled) {
			setDirty(true);
		}
		this.enabled = enabled;
	}

	/**
	 * only for debug purposes!!!
	 * 
	 * @return Returns the latestDispatchedController.
	 */
	public Controller getLatestDispatchedController() {
		return latestDispatchedController;
	}

	/**
	 * only for debugging reasons!!!
	 * 
	 * @return Returns the latestFiredEvent.
	 */
	public Event getAndClearLatestFiredEvent() {
		Event tmp = latestFiredEvent;
		latestFiredEvent = null; // gc
		return tmp;
	}

	/**
	 * @return returns whether the component needs to be rerendered or not
	 */
	public boolean isDirty() {
		return dirty;
	}
	
	/**
	 * used by the screenreader feature to determine whether the component has changed from a user's perspective.
	 * normally this is the same as isDirty(), but may differ, e.g. for a MenuTree (expanding the tree: true; activating a link: false)
	 * @see org.olat.core.gui.components.tree.MenuTree
	 * @return whether the component has changed from a user's perspective. 
	 */
	public boolean isDirtyForUser() {
		// default implementation
		return isDirty();
	}

	/**
	 * @param dirty The dirty to set.
	 */
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	/**
	 * @return Returns the domReplaceable.
	 */
	public boolean isDomReplaceable() {
		return domReplaceable;
	}

	/**
	 * if set to true(default), then this component can be swapped out in the
	 * browser dom tree if that capability is enabled
	 * 
	 * @param domReplaceable The domReplaceable to set.
	 */
	public void setDomReplaceable(boolean domReplaceable) {
		this.domReplaceable = domReplaceable;
	}
	
	
	public void setSpanAsDomReplaceable(boolean spanReplaceable){
		this.spanReplaceable = spanReplaceable;
	}
	public boolean getSpanAsDomReplaceable(){
		return this.spanReplaceable;
	}
	
	/**
	 * @return true: component does not print DOM ID on element; false:
	 *         component always outputs an element with the dispatch ID as DOM
	 *         ID
	 */
	public boolean isDomReplacementWrapperRequired() {
		return this.domReplacementWrapperRequired;
	}
	public void setDomReplacementWrapperRequired(boolean domReplacementWrapperRequired) {
		this.domReplacementWrapperRequired = domReplacementWrapperRequired;
	}

	/**
	 * to be called only by the container when a child is added
	 * @param parent
	 */
	public void setParent(ComponentCollection parent){
		this.parent = parent;
	}
	
	/**
	 * @return
	 */
	public ComponentCollection getParent(){
		return parent;
	}
	
	/**
	 * Return true if the component is changed without dirty marking. It's a special case which
	 * for special components which provide a protection against back button usage.
	 * @return
	 */
	public boolean isSilentlyDynamicalCmp() {
		return false;
	}
		
	/**
	 * to be used by Window.java to detect browser back in ajax-mode
	 * @return Returns the timestamp.
	 */
	public String getTimestamp() {
		if(staticCmp) {
			return "1";
		}
		return timestampID;
	}

}