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
package org.olat.course.duedate.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.render.ValidationResult;

/**
 * 
 * Initial date: 3 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DueDateConfigComponent extends FormBaseComponentImpl {

	private static final ComponentRenderer RENDERER =  new DueDateConfigComponentRenderer();
	
	private final DueDateFormItemImpl formItem;
	
	public DueDateConfigComponent(DueDateFormItemImpl formItem) {
		super(formItem.getName());
		this.formItem = formItem;
	}
	
	public DueDateFormItemImpl getFormItem() {
		return formItem;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		if (formItem.getAbsoluteDateEl() != null) {
			formItem.getAbsoluteDateEl().getComponent().validate(ureq, vr);
		}
	}

}
