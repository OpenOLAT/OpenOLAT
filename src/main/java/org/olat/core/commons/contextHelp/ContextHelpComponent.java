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
package org.olat.core.commons.contextHelp;
import java.util.Locale;

/**

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
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * 
 * A link to the context help
 * 
 * Initial date: 17.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContextHelpComponent extends AbstractComponent {

	private static ComponentRenderer RENDERER ;
	
	private final Locale locale;

	public ContextHelpComponent(String name, Locale locale) {
		super(name);
		this.locale = locale;
		setTranslator(Util.createPackageTranslator(ContextHelpComponent.class, locale));
		RENDERER = new ContextHelpComponentRenderer(name);
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	public void setTranslator(Translator translator) {
		if(translator == null) {
			System.out.println();
		}
		super.setTranslator(translator);
	}

	/**
	 * 
	 * @param name Name of the component
	 * @param packageName Package name where the help is
	 * @param pageName The name of the page of this specific context help
	 * @param hoverTextKey i18n key of the tooltip
	 * @param locale Locale of the user
	 */


	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
