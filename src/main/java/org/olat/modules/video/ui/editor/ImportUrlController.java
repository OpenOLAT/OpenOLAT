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
package org.olat.modules.video.ui.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.fileresource.types.VideoFileResource;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-03-09<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ImportUrlController extends FormBasicController {
	private TextElement urlEl;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;

	protected ImportUrlController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("comment.add.import.url.description");

		urlEl = uifactory.addTextElement("upload", "comment.add.import.url.url", 128,
				null, formLayout);

		FormLayoutContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttonContainer",
				getTranslator());
		formLayout.add("buttonContainer", buttonContainer);
		uifactory.addFormSubmitButton("comment.add.import.file", buttonContainer);
		uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		urlEl.clearError();
		if (!StringHelper.containsNonWhitespace(urlEl.getValue())) {
			urlEl.setErrorKey("form.mandatory.hover");
			allOk = false;
		} else {
			String url = urlEl.getValue();
			boolean handlerFound = false;
			for (String type : repositoryHandlerFactory.getSupportedTypes()) {
				RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(type);
				if (handler.getSupportedType().equals(VideoFileResource.TYPE_NAME)) {
					ResourceEvaluation evaluation = handler.acceptImport(url);
					if (evaluation != null && evaluation.isValid()) {
						handlerFound = true;
						break;
					}
				}
			}
			if (!handlerFound) {
				urlEl.setErrorKey("form.common.error.invalidUrl");
				allOk = false;
			}
		}

		return allOk;
	}

	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	public String getUrl() {
		return urlEl.getValue();
	}
}
