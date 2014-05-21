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
package org.olat.core.gui.components.htmlheader.jscss;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.gui.render.ValidationResult;

/**
 * 
 * Description:<br>
 * CustomJSComponent allows to add js-files to the OLAT head-tag.<br />
 * Use this class, if you want to add js-files that do not reside under a
 * /package/_static/js folder. You can specify the url to the file directly.
 * 
 * <p>
 * Usage: <br />
 * <code>
 * CustomJSComponent customJS = new CustomJSComponent("customThemejs", new String[] { "/olat/raw/BUILDID/themes/frentix/theme.js" });
 * </code>
 * </p>
 * (originated from FXOLAT-310)
 * <P>
 * Initial Date: 28.10.2011 <br>
 * 
 * @author strentini, sergio.trentini@frentix.com, www.frentix.com
 */
public class CustomJSComponent extends AbstractComponent {

	private final String[] jsFilePaths;

	private static final ComponentRenderer RENDERER = new JSAndCSSComponentRenderer();

	/**
	 * 
	 * @param name
	 * @param jsFilePaths
	 */
	public CustomJSComponent(String name, String[] jsFilePaths) {
		super(name);
		this.jsFilePaths = jsFilePaths;
	}

	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		JSAndCSSAdder jsadder = vr.getJsAndCSSAdder();
		if (jsFilePaths != null) {
			int len = jsFilePaths.length;
			for (int i = 0; i < len; i++) {
				String jsFileP = jsFilePaths[i];
				jsadder.addRequiredStaticJsFile(jsFileP);
			}
		}
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		// do nothing here
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}