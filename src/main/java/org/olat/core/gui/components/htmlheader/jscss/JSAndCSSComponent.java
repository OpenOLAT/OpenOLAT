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
* Initial code contributed and copyrighted by<br>
* JGS goodsolutions GmbH, http://www.goodsolutions.ch
* <p>
*/
package org.olat.core.gui.components.htmlheader.jscss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.gui.render.ValidationResult;

/**
* Description:<br>
* a fake component which only serves to request additional .js or .css files for a controller/component. In order to activate it, this component here
* must be inserted into the rendertree, e.g. myVelocityContainer.put("jsAndCss", myJsAndCssComponent);
* 
* <P>
* Initial Date:  03.05.2006 <br>
*
* @author Felix Jost
*/
public class JSAndCSSComponent extends AbstractComponent {
	private static final ComponentRenderer RENDERER = new JSAndCSSComponentRenderer();
	private final String cssFileName;
	private final String[] jsFileNames;
	private final Class<?> baseClass;
	private final boolean forceCssRemove;
	private final int cssFileIndex;
	private final String rawHeader;

	private int refreshIntervall;
	private boolean fullPageRefresh = false;
	private List<String> cssPathNames = null;
	private Map<String, Integer> cssPathNamesIndexes;
	/**
	 * 
	 * @param componentName the name of the component
	 * @param baseClass the class of the controller (or from the package's Manager) from where the resources will be fetched: e.g. org/olat/demo/_static/js or /css respectively.
	 * @param refreshIntervall the time in miliseconds after which (in ajax mode) a refresh of the screen is needed. -1 means infinity/no refresh. use small times with caution, since it generates server load. after the given time, a poll (comet, push, hanging get for future release) is issued to the server to collect the dirty components and rerender them. when more than one interval is requested, then the minimum is taken.
	 */
	public JSAndCSSComponent(String componentName, Class<?> baseClass, int refreshIntervall) {
		super(componentName);
		this.baseClass = baseClass;
		this.jsFileNames = null;
		
		// filepath: whatever named global mapper for this here, then org_olat_baseClass_demo_bla/js/<jsFileName>
		// and                                                                               ..../css/<cssFileName>
		this.cssFileName = null;
		this.forceCssRemove = false;
		// use before theme as default configuration
		this.cssFileIndex = JSAndCSSAdder.CSS_INDEX_BEFORE_THEME;
		this.rawHeader = null;
		this.refreshIntervall = refreshIntervall;
	}

	/**
	 * 
	 * @param componentName the name of the component
	 * @param baseClass the class of the controller (or from the package's Manager) from where the resources will be fetched: e.g. org/olat/demo/_static/js or /css respectively.
	 * @param forceCssRemove if true, the given css will be removed if no longer necessary (e.g. for a custom css as in the course), otherwise it will stay in the html-header for the whole usersession
	 * @param rawHeader -only use if neither jsFileNames nor cssFileName can be used (only for dynamically created jslibs for example)-a string (e.g. "<script...." or "<link rel=...  those entries will be refreshed after each page, that is entries causes an full page reload in ajax mode
	 */
	public JSAndCSSComponent(String componentName, Class<?> baseClass, boolean forceCssRemove, String rawHeader) {
		super(componentName);
		this.baseClass = baseClass;
		this.jsFileNames = null;
		
		// filepath: whatever named global mapper for this here, then org_olat_baseClass_demo_bla/js/<jsFileName>
		// and                                                                               ..../css/<cssFileName>
		this.cssFileName = null;
		this.forceCssRemove = forceCssRemove;
		// use before theme as default configuration
		this.cssFileIndex = JSAndCSSAdder.CSS_INDEX_BEFORE_THEME;
		this.rawHeader = rawHeader;
		this.refreshIntervall = -1;
	}

	/**
	 * 
	 * @param componentName the name of the component
	 * @param baseClass the class of the controller (or from the package's Manager) from where the resources will be fetched: e.g. org/olat/demo/_static/js or /css respectively.
	 * @param forceCssRemove if true, the given css will be removed if no longer necessary (e.g. for a custom css as in the course), otherwise it will stay in the html-header for the whole usersession
	 */
	public JSAndCSSComponent(String componentName, Class<?> baseClass, boolean forceCssRemove) {
		this(componentName, baseClass, forceCssRemove, null);
	}
	
	/**
	 * 
	 * @param componentName the name of the component
	 * @param baseClass the class of the controller (or from the package's Manager) from where the resources will be fetched: e.g. org/olat/demo/_static/js or /css respectively.
	 * @param jsPath maybe <code>null</code> An array of the jsFilenames, one entry is e.g. "script.js", which will then be resolved to baseClass/_static/js/script.js
	 * @param cssPath maybe <code>null</code> the name of the cssFile to include: e.g. "style.css" will be resolved to loc of baseClass/_static/css/style.css
	  */
	public JSAndCSSComponent(String componentName, String[] jsPath, String[] cssPath) {
		super(componentName);

		cssFileName = null;
		baseClass = null;
		forceCssRemove = false;
		cssFileIndex = -1;
		rawHeader = null;
		jsFileNames = jsPath;
		
		if(cssPath != null) {
			cssPathNames = new ArrayList<>(cssPath.length);
			for(String css:cssPath) {
				cssPathNames.add(css);
			}
		}
	}

	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		
		JSAndCSSAdder jsadder = vr.getJsAndCSSAdder();
		if (jsFileNames != null) {
			int len = jsFileNames.length;
			for (int i = 0; i < len; i++) {
				jsadder.addRequiredStaticJsFile(jsFileNames[i]);
			}
		}
		if(cssFileName != null) {
			jsadder.addRequiredCSSPath("css/"+cssFileName, forceCssRemove, cssFileIndex);
		}
		if (cssPathNames != null) {
			for (String cssPath : cssPathNames) {
				Integer index = cssPathNamesIndexes != null ? cssPathNamesIndexes.get(cssPath) : null;
				if(index == null && baseClass == null) {
					jsadder.addRequiredCSSPath(cssPath, false, 0);
				} else {
					jsadder.addRequiredCSSPath(cssPath, true, index);
				}
			}
		}
		if (rawHeader != null) {
			jsadder.addRequiredRawHeader(baseClass, rawHeader);
		}
		if (fullPageRefresh) {
			jsadder.requireFullPageRefresh();
		}
		if (refreshIntervall != -1 && baseClass != null) {
			jsadder.setRequiredRefreshInterval(baseClass, refreshIntervall);
		}
	}

	/**
	 * @see org.olat.core.gui.components.Component#dispatchRequest(org.olat.core.gui.UserRequest)
	 */
	protected void doDispatchRequest(UserRequest ureq) {
	// do nothing
	}

	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	/**
	 * 
	 * used rather rarely, e.g. when you have a css with content which is dynamic (e.g. a custom course css in olat).<br>
	 * Use a Mapper/MapperRegistry to obtain a cssPath. The css is assumed to be flagged with autoremove, that is, it is removed as soon
	 * as no one requires it in the validation phase (since those special css are created by a mapper which in turn is created by a controller so that
	 * it makes sense to limit the css lifespan to the lifespan of the controller)
	 * <br>
	 * Normally use the cssFileName parameter in the constructor for class-based static css files.
	 * 
	 * @param cssPathName the path to the css, e.g. /olat/m/1001/mycss.css
	 * @param cssLoadIndex flag to indicate load order of this CSS. See JSAndCSSAdder.CSS_INDEX_*
	 * @see JsAndCssAdder.addRequiredCSSPath(String cssPath, boolean forceRemove);
	 */
	public void addAutoRemovedCssPathName(String cssPathName, int cssLoadIndex) {
		if (cssPathNames == null) { // only one gui thread has access to this object -> thread safe
			cssPathNames = new ArrayList<>(2);
			cssPathNamesIndexes = new HashMap<>(2);
		}
		cssPathNames.add(cssPathName);
		cssPathNamesIndexes.put(cssPathName, cssLoadIndex);			
	}
	
	/**
	 * 
	 * @param refreshIntervall - refresh intervall in ms, -1 no more polling i.e. <=1000 no more polling
	 * according to /olatcore/org/olat/core/gui/control/winmgr/_content/serverpart.html
	 */
	public void setRefreshIntervall(int refreshIntervall) {
		this.refreshIntervall = refreshIntervall;
	}

	/**
	 * 
	 * requires that a full page reload takes places.	
	 * sometimes eval'ing a more complex js lib (such as tiny mce) directly into global context does not work (timing issues?)
	 * this should be used only rarely when complex js is executed and has errors in it, 
	 * since a full page refresh is slower than a ajax call.
	 * <br>
	 * when this component is validated (last cycle before rendering), and a full page refresh is required, then a full page request command is 
	 * sent via JSON to the browser which then executes it using document.location.replace(...). Since this step involves two calls (JSON+reload),
	 * this is slower than a normal full page click (aka known as non-ajax mode). 
	 * 
	 */
	public void requireFullPageRefresh() {
		fullPageRefresh = true;
	}
}
