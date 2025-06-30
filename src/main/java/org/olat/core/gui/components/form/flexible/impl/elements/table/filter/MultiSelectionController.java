/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.form.flexible.impl.elements.table.filter;

import java.util.Collection;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: Jun 27, 2025<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class MultiSelectionController extends FormBasicController {

	private FormLink updateButton;

	private FlexiFilterExtendedController formCtrl;

	private final SelectionValues availableValues;
	private final Collection<String> selectedKeys;

	public MultiSelectionController(UserRequest ureq, WindowControl wControl,
			SelectionValues availableValues, Collection<String> selectedKeys) {
		super(ureq, wControl, "multi_selection",
				Util.createPackageTranslator(FlexiTableElementImpl.class, ureq.getLocale()));
		this.availableValues = availableValues;
		this.selectedKeys = selectedKeys;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formCtrl = new MultiSelectionFormController(ureq, getWindowControl(), mainForm, availableValues, selectedKeys);
		listenTo(formCtrl);
		formLayout.add("form", formCtrl.getInitialFormItem());
		
		updateButton = uifactory.addFormLink("update", formLayout, Link.BUTTON_SMALL);
		updateButton.setElementCssClass("o_sel_flexiql_update");
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == formCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (updateButton == source) {
			formCtrl.doUpdate(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		formCtrl.doUpdate(ureq);
	}

	public static class MultiSelectionFormController extends AbstractMultiSelectionController {
		
		public MultiSelectionFormController(UserRequest ureq, WindowControl wControl, Form form,
				SelectionValues availableValues, Collection<String> preselectedKeys) {
			super(ureq, wControl, form, availableValues, preselectedKeys);
		}
		
		@Override
		protected Event createChangedEvent(Set<String> selectedKeys) {
			return new KeysSelectedEvent(selectedKeys);
		}
		
		public static final class KeysSelectedEvent extends Event {
			
			private static final long serialVersionUID = 2195120197173429621L;
			
			private final Set<String> selectedKeys;
			
			public KeysSelectedEvent(Set<String> selectedKeys) {
				super("keys-selected");
				this.selectedKeys = selectedKeys;
			}
			
			public Set<String> getSelectedKeys() {
				return selectedKeys;
			}
			
		}
		
	}

}
