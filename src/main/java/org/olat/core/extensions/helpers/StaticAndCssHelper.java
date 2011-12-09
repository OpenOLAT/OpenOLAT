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

package org.olat.core.extensions.helpers;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.extensions.css.CSSIncluder;
import org.olat.core.extensions.globalmapper.MapperProvider;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.media.ClasspathMediaResource;
import org.olat.core.gui.media.MediaResource;

/**
 * @deprecated
 * Description:<br>
 * Initial Date: 23.09.2005 <br>
 * @author Felix Jost
 */
public class StaticAndCssHelper {

	String mapPath; // increased visibilities to package local to avoid a synthetic
									// accessor method for the inner classes
	final Class baseClass;
	final String cssName;

	/**
	 * @param baseClass all static resources and the css file will be searched in
	 *          the subfolder (called "raw") of the class file of the baseClass
	 * @param cssName the name of the css file (e.g. stylesheet.css) to include in
	 *          each olat page, or null if not stylesheet
	 */
	public StaticAndCssHelper(final Class baseClass, final String cssName) {
		this.baseClass = baseClass;
		this.cssName = cssName;
	}

	public void initAndRegister(ExtensionElements extElements) {
		// --- create a mapper which serves the static resources (see next
		// codeblock)
		final Mapper mapper = new Mapper() {
			public MediaResource handle(String relPath, HttpServletRequest request) {
				// nothing under "raw" should be sensitive information since, by entering
				// the appropriate url, any resources in this folder and in subfolders is served.
				return new ClasspathMediaResource(baseClass, "raw" + relPath);
			}
		};

		// --- register a global mapper for static resource delegated to this
		// extension
		extElements.putExtensionElement(DispatcherAction.class.getName(), new MapperProvider() {
			public Mapper getMapper() {
				return mapper;
			}

			public void setMapPath(String path) {
				StaticAndCssHelper.this.mapPath = path;
			}

		});

		if (cssName != null) {
			// provide the link for the css stylesheet file used by this extension:
			// CSSIncluder for point Window
			extElements.putExtensionElement(Window.class.getName(), new CSSIncluder() {
				public String getStylesheetPath() {
					return StaticAndCssHelper.this.mapPath + "/" + cssName;
				}

			});
		}
	}

	/**
	 * @return Returns the mapPath.
	 */
	public String getMapPath() {
		return mapPath;
	}

}
