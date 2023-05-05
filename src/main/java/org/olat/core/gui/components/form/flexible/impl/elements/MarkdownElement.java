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
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 4 May 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MarkdownElement extends AbstractTextElement {
	
	private final MarkdownComponent component;
	private boolean autosave;

	public MarkdownElement(String name) {
		super(name);
		this.component = new MarkdownComponent(name, this);
	}

	@Override
	public void setDomReplacementWrapperRequired(boolean required) {
		component.setDomReplacementWrapperRequired(required);
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		String paramId = component.getFormDispatchId();
		String paramValue = getRootForm().getRequestParameter(paramId);
		if (paramValue != null) {
			setValue(paramValue);
		} else {
			String autosave = getRootForm().getRequestParameter("autosave");
			if (StringHelper.containsNonWhitespace(autosave)) {
				value = autosave;
				getRootForm().fireFormEvent(ureq, new MarkdownAutosaveEvent(this, autosave));
			}
		}
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	public boolean isAutosave() {
		return autosave;
	}

	public void setAutosave(boolean autosave) {
		this.autosave = autosave;
	}
	
	/**
	 * This event is fired without refresh of the GUI!
	 * 
	 * Initial date: 5 May 2023<br>
	 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
	 *
	 */
	public static class MarkdownAutosaveEvent extends FormEvent {

		private static final long serialVersionUID = 3323978400930044102L;

		private final String text;
		
		public MarkdownAutosaveEvent(FormItem source, String text) {
			super("markdown-autosave", source, ONCHANGE);
			this.text = text;
		}
		
		public String getText() {
			return text;
		}
		
	}

}
