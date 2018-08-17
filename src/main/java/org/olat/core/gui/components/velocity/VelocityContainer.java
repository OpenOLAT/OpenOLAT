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

package org.olat.core.gui.components.velocity;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.Container;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.render.velocity.VelocityComponent;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class VelocityContainer extends Container implements VelocityComponent {
	private static final ComponentRenderer RENDERER = new VelocityContainerRenderer();
	
	/**
	 * Comment for <code>COMMAND_ID</code>
	 */
	public static final String COMMAND_ID = "cid";

	private String page;
	private Context context = new VelocityContext();

	/**
	 * Constructor to create a VC container from a given file path
	 * @param componentName Name of component, displayed in debug mode
	 * @param pagePath Full path to velocity page
	 * @param trans Translator to be used
	 * @param listeningController the listenenController; may be null if the
	 *          caller has no need to register for events
	 */
	public VelocityContainer(String componentName, String pagePath, Translator trans, ComponentEventListener listeningController) {
		this(null, componentName, pagePath, trans, listeningController);
	}
	
	/**
	 * Constructor to create a VC container from a given file path
	 * @param id A fix identifier for state-less behavior
	 * @param componentName Name of component, displayed in debug mode
	 * @param pagePath Full path to velocity page
	 * @param trans Translator to be used
	 * @param listeningController the listenenController; may be null if the
	 *          caller has no need to register for events
	 */
	public VelocityContainer(String id, String componentName, String pagePath, Translator trans, ComponentEventListener listeningController) {
		super(id, componentName, trans);
		setPage(pagePath);
		if (listeningController != null) addListener(listeningController);
	}
	
	/**
	 * Constructor to create a VC container from a given base class and the page name
	 * @param componentName Name of component, displayed in debug mode
	 * @param baseClass
	 * @param pageName page name that is available within this base class package
	 * @param trans Translator to be used
	 * @param listeningController the listenenController; may be null if the
	 *          caller has no need to register for events
	 */
	public VelocityContainer(String componentName, Class<?> baseClass, String pageName, Translator trans, ComponentEventListener listeningController) {
		this(componentName, Util.getPackageVelocityRoot(baseClass) + "/" + pageName + ".html", trans, listeningController);
	}

	/**
	 * @see org.olat.core.gui.components.Component#dispatchRequest(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		// called when a page-link is clicked ($r.renderCommand)
		String commandString = ureq.getParameter(COMMAND_ID);
		
		
		// WAS: set component dirty by default.
		// if you are in ajax mode, and the link resulted in a external mediaresource, then you can (in your controller) manually reset dirty to false.
		// we decided that there are only very few cases for this, and otherwise that the chance that a developers forgets to call vc.setDirty(true) is
		// much higher
		
		// now: normal links are created using the Link component, which takes care of setting dirty itself (everytime it is dispatched)
		//setDirty(true);
		
		// notify listening controllers
		fireEvent(ureq, new Event(commandString));
	}	
	
	/**
	 * @return the Velocity Context
	 */
	@Override
	public Context getContext() {
		return context;
	}

	/**
	 * Add a variable to the velocity context. The methods of the objects accessed
	 * from the velocity file must be public. If not public you will get an error
	 * message in the olat.log that the reference is not valid.
	 * 
	 * @param key
	 * @param value
	 */
	public void contextPut(String key, Object value) {
		context.put(key, value);
		setDirty(true);
	}
	
	public Object contextGet(String key) {
		return context.get(key);
	}

	/**
	 * Remove a variable from the velocity context
	 * 
	 * @param key
	 */
	public void contextRemove(String key) {
		if (context.containsKey(key)) {
			context.remove(key);
			setDirty(true);
		}
	}

	/**
	 * @return String
	 */
	public String getPage() {
		return page;
	}

	/**
	 * Sets the page.
	 * @deprecated Rather use panels to swap views
	 * @param page The page to set
	 */
	public void setPage(String page) {
		this.page = page.intern(); //prevent thousands of same strings
		setDirty(true);
	}

	/**
	 * @see org.olat.core.gui.components.Component#getExtendedDebugInfo()
	 */
	public String getExtendedDebugInfo() {
		return "page:"+page+" "+super.getExtendedDebugInfo();
	}

	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	/**
	 * Sets the translator for this VC. Note that this should be called before
	 * adding any child components using put(component).
	 * <p>
	 * put(component) will set the current translator on the component if the
	 * component itself does not yet have a translator set. The translator of
	 * the components can't be modified afterwards.
	 * <p>
	 * This method overrides the protected setTranslator() method from the
	 * component interface and make it public to support workflows where the
	 * velocity container is created in the constructor (e.g.
	 * FormBasicController) but the translator might be modified later (using
	 * some special fallback translator or such)
	 * 
	 * @param translator
	 *            The translator to set
	 */
	@Override
	public void setTranslator(Translator newTranslator) {
		super.setTranslator(newTranslator);
	}


}
