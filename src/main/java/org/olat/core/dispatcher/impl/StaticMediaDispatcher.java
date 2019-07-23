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
package org.olat.core.dispatcher.impl;

import org.olat.core.gui.render.StringOutput;
import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;

/**
 * <h3>Description:</h3> A dispatcher that delivers raw static files without any
 * servlet intervention or security checks directly from the webapp/static
 * directory.
 * <p>
 * The URL contains the web app version ID to make sure browsers always fetch
 * the newest version after a new release to prevent browser caching issues.
 * <p>
 * This should only be used to deliver basic files from the body.html and some
 * other static resource. When developing modules, put all your static files
 * like js libraries or other resource into the _static resources folder and
 * include them using the JSAndCSSComponent.java or get the URL to those
 * resources from the ClassPathStaticDispatcher.java
 * <p>
 * Initial Date: 16.05.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class StaticMediaDispatcher {
	public static final String STATIC_DIR_NAME = "/static";
	public static final String NOVERSION = "_noversion_";
	private static String mapperPath;
	private static int forceReloadCounter = 0;

	/**
	 * Constructor
	 * 
	 * @param mapperPathFromConfig
	 */
	public StaticMediaDispatcher(String mapperPathFromConfig) {
		mapperPath = mapperPathFromConfig;
	}

	/**
	 * Note: use only rarely - all non-generic js libs and css classes should be
	 * included using JsAndCssComponent, and all images should be referenced with
	 * the css background-image capability. <br>
	 * renders a uri which is mounted to the webapp/static/ directory of your web
	 * application.
	 * <p>
	 * This method will add a version ID to the path that guarantees that the
	 * browser fetches the file again when you release a new version of your
	 * application.
	 * 
	 * @param target
	 * @param URI e.g. img/specialimagenotpossiblewithcss.jpg
	 */
	public static void renderStaticURI(StringOutput target, String URI) {
		renderStaticURI(target, URI, true);
	}
	
	public static String getStaticURI(String uri) {
		StringOutput target = new StringOutput();
		renderStaticURI(target, uri, true);
		return target.toString();
	}

	/**
	 * Render a static URL to resource. This is only used in special cases, in
	 * most scenarios you should use the JSAndCssComponent
	 * 
	 * @param target The output target
	 * @param URI e.g. img/specialimagenotpossiblewithcss.jpg
	 * @param addVersionID true: the build version is added to the URL to force
	 *          browser reload the resource when releasing a new version; false:
	 *          don't add version (but allow browsers to cache even when resource
	 *          has changed). Only use false when really needed
	 */
	public static void renderStaticURI(StringOutput target, String URI, boolean addVersionID) {
		String root = WebappHelper.getServletContextPath();
		target.append(root); // e.g /olat
		target.append(mapperPath); // e.g. /raw/
		// Add version to make URL change after new release and force browser to
		// load new static files
		if (addVersionID) {
			if(StringHelper.containsNonWhitespace(WebappHelper.getRevisionNumber())) {
				target.append(WebappHelper.getRevisionNumber()).append(":").append(WebappHelper.getChangeSet());
			} else {
				target.append(Settings.getBuildIdentifier());
			}
			if (forceReloadCounter > 0) {
				target.append(":").append(forceReloadCounter);
			}
		} else {
			target.append(NOVERSION);			
		}
		target.append("/");			
		if (URI != null) target.append(URI);
	}

	/**
	 * Create a static URI for this relative URI. Helper method in case no String
	 * output is available. 
 	 * <p>
	 * This method will add a version ID to the path that guarantees that the
	 * browser fetches the file again when you release a new version of your
	 * application.

	 * @param URI e.g. img/specialimagenotpossiblewithcss.jpg
	 * @return
	 */
	public static String createStaticURIFor(String URI) {
		return createStaticURIFor(URI, true);
	}
	
	/**
	 * Create a static URI for this relative URI. Helper method in case no String
	 * output is available. 
	 * 
	 * @param URI e.g. img/specialimagenotpossiblewithcss.jpg
	 * @param addVersionID true: the build version is added to the URL to force
	 *          browser reload the resource when releasing a new version; false:
	 *          don't add version (but allow browsers to cache even when resource
	 *          has changed). Only use false when really needed
	 * @return
	 */
	public static String createStaticURIFor(String URI, boolean addVersionID) {
		StringOutput so = new StringOutput();
		renderStaticURI(so, URI, addVersionID);
		return so.toString();
	}
	
	/**
	 * Get the path to the static mapper. Everything after that path is
	 * delivered by the static mapper
	 * 
	 * @return
	 */
	public static String getStaticMapperPath() {
		return mapperPath;
	}

	/**
	 * Change the static media mapper path to force the browsers to load all
	 * media again. Note that this might have no effect to already initialized
	 * controllers. The old mapper path will still work. This force-reload
	 * mechanism is RAM only and not cluster save. It shall only be used rarely
	 * when static files change between releases, e.g. when modifying the theme.
	 */
	public static void forceReloadStaticMediaDelivery() {
		forceReloadCounter++;
	}
	
}
