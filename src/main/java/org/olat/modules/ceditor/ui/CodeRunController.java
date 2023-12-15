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

import java.util.HashMap;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.model.CodeElement;
import org.olat.modules.ceditor.model.CodeLanguage;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;

/**
 * Initial date: 2023-12-14<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CodeRunController extends BasicController implements PageRunElement {


	private final VelocityContainer mainVC;
	private CodeElement codeElement;

	public CodeRunController(UserRequest ureq, WindowControl wControl, CodeElement codeElement) {
		super(ureq, wControl);
		this.codeElement = codeElement;
		mainVC = createVelocityContainer("code_run");
		putInitialPanel(mainVC);
		initUI();
		updateUI();
	}

	private void initUI() {
		HashMap<String, String> languageKeyToValue = new HashMap();
		for (CodeLanguage codeLanguage : CodeLanguage.values()) {
			languageKeyToValue.put(codeLanguage.name(), codeLanguage.getDisplayText(getLocale()));
		}
		mainVC.contextPut("languageKeyToValue", languageKeyToValue);
	}

	private void updateUI() {
		CodeLanguage codeLanguage = codeElement.getSettings().getCodeLanguage();
		if (codeLanguage.equals(CodeLanguage.auto)) {
			mainVC.contextRemove("codeLanguage");
		} else {
			mainVC.contextPut("codeLanguage", codeLanguage.name());
		}
		mainVC.contextPut("content", StringHelper.escapeHtml(codeElement.getContent()));
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof ChangePartEvent changePartEvent) {
			if (changePartEvent.getElement() instanceof CodeElement updatedCodeElement) {
				codeElement = updatedCodeElement;
				updateUI();
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public Component getComponent() {
		return getInitialComponent();
	}

	@Override
	public boolean validate(UserRequest ureq, List<ValidationMessage> messages) {
		return false;
	}
}
