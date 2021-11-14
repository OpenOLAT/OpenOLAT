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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.Cancel;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.ValidationStatus;

/**
 * Description:<br>
 * The form cancel triggers the form cancelled event and bypasses the normal
 * form event infrastructure by using a conventional link and an inner
 * controller that dispatches the link event and forwards it as an inner form
 * event.
 * 
 * <P>
 * Initial Date: 06.07.2009 <br>
 * 
 * @author gnaegi
 */
public class FormCancel extends FormItemImpl implements Disposable, Cancel {
	private final Link cancelLink;
	private Controller dispatchLinkController;
	private final FormCancel self;

	public FormCancel(String name, FormItemContainer formLayoutContainer,
			UserRequest ureq, WindowControl wControl) {
		super(name);
		self = this;
		// Create inner link dispatch controller as a hack to catch event that
		// should bypass the form infrastructure. This inner controller is
		// disposed by this form item.
		dispatchLinkController = new BasicController(ureq, wControl) {

			@Override
			protected void event(UserRequest ureq, Component source, Event event) {
				if (source == cancelLink) {
					getRootForm()
							.fireFormEvent(
									ureq,
									new FormEvent(
											org.olat.core.gui.components.form.Form.EVNT_FORM_CANCELLED,
											self, -1));
				}
			}
		};
		// The link component that is used by this form element
		cancelLink = LinkFactory.createButton(name,
				(VelocityContainer) formLayoutContainer.getComponent(),
				dispatchLinkController);
		cancelLink.setSuppressDirtyFormWarning(true);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormItemImpl#evalFormRequest(org.olat.core.gui.UserRequest)
	 */
	@Override
	public void evalFormRequest(UserRequest ureq) {
		// nothing to do
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormItemImpl#getFormItemComponent()
	 */
	@Override
	protected Component getFormItemComponent() {
		return cancelLink;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormItemImpl#reset()
	 */
	@Override
	public void reset() {
		// nothing to do
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormItemImpl#rootFormAvailable()
	 */
	@Override
	protected void rootFormAvailable() {
		// nothing to do
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormItemImpl#validate(java.util.List)
	 */
	@Override
	public void validate(List<ValidationStatus> validationResults) {
		// nothing to do
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.Cancel#setCustomDisabledLinkCSS(java.lang.String)
	 */
	@Override
	public void setCustomDisabledLinkCSS(String customDisabledLinkCSS) {
		cancelLink.setCustomDisabledLinkCSS(customDisabledLinkCSS);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.Cancel#setCustomEnabledLinkCSS(java.lang.String)
	 */
	@Override
	public void setCustomEnabledLinkCSS(String customEnabledLinkCSS) {
		cancelLink.setCustomEnabledLinkCSS(customEnabledLinkCSS);
	}

	@Override
	public void setElementCssClass(String elementCssClass) {
		if(cancelLink != null) {
			cancelLink.setElementCssClass(elementCssClass);
		}
		super.setElementCssClass(elementCssClass);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.Cancel#setI18nKey(java.lang.String)
	 */
	@Override
	public void setI18nKey(String i18n) {
		cancelLink.setCustomDisplayText(translator.translate(i18n));
	}

	/**
	 * @see org.olat.core.gui.control.Disposable#dispose()
	 */
	@Override
	public void dispose() {
		if (dispatchLinkController != null) {
			dispatchLinkController.dispose();
			dispatchLinkController = null;
		}
	}
}
