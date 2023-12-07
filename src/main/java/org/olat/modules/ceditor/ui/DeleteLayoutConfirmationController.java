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
package org.olat.modules.ceditor.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.ui.component.ContentEditorContainerComponent;

/**
 * Initial date: 2023-12-07<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class DeleteLayoutConfirmationController extends FormBasicController {
	public static final Event DELETE_EVERYTHING_EVENT = new Event("command.delete.everything");
	public static final Event ONLY_DELETE_LAYOUT_EVENT = new Event("command.only.delete.layout");
	private final ContentEditorContainerComponent layoutComponent;

	private FormLink deleteEverythingButton;

	public DeleteLayoutConfirmationController(UserRequest ureq, WindowControl wControl,
											  ContentEditorContainerComponent layoutComponent) {
		super(ureq, wControl);
		this.layoutComponent = layoutComponent;
		initForm(ureq);
	}

	public ContentEditorContainerComponent getLayoutComponent() {
		return layoutComponent;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormWarning("delete.layout.warning");

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		deleteEverythingButton = uifactory.addFormLink("delete.layout.delete.everything", buttonLayout, Link.BUTTON);
		deleteEverythingButton.setElementCssClass("btn-danger");
		uifactory.addFormSubmitButton("delete.layout.only.delete.layout", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if (source == deleteEverythingButton) {
			fireEvent(ureq, DELETE_EVERYTHING_EVENT);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, ONLY_DELETE_LAYOUT_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
