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
package org.olat.core.gui.components.form.flexible.impl.elements;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.AutoCompleter;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.gui.render.ValidationResult;

/**
 * 
 * Initial date: 20.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AutoCompleterComponent extends FormBaseComponentImpl {
	
	private static final AutoCompleterRenderer RENDERER = new AutoCompleterRenderer();
	
	private final AutoCompleterImpl autoCompleter;
	
	public AutoCompleterComponent(String id, String name, AutoCompleterImpl autoCompleter) {
		super(id, name);
		this.autoCompleter = autoCompleter;
		setTranslator(autoCompleter.getTranslator());
	}

	@Override
	public AutoCompleter getFormItem() {
		return autoCompleter;
	}
	
	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		JSAndCSSAdder jsa = vr.getJsAndCSSAdder();
		jsa.addRequiredStaticJsFile("js/jquery/typeahead/typeahead.bundle.min.js");
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		String cmd = ureq.getParameter("cid");
		if("select".equals(cmd)) {
			String key = ureq.getParameter("key");
			String value = ureq.getParameter("value");
			autoCompleter.setKey(key);
			autoCompleter.setValue(value);
			fireEvent(ureq, new AutoCompleteEvent(AutoCompleteEvent.SELECT_EVENT, key));
			autoCompleter.dispatchFormRequest(ureq);
		}
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
