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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.model.CodeElement;
import org.olat.modules.ceditor.model.CodeLanguage;
import org.olat.modules.ceditor.model.CodeSettings;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;

/**
 * Initial date: 2023-12-14<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CodeRunController extends BasicController implements PageRunElement {

	private static final long MAX_LINES_TO_EXPAND = 100;
	private final VelocityContainer mainVC;
	private CodeElement codeElement;
	private final boolean editable;
	private Link expandCollapseButton;
	private boolean expanded;

	public CodeRunController(UserRequest ureq, WindowControl wControl, CodeElement codeElement, boolean editable) {
		super(ureq, wControl);
		this.codeElement = codeElement;
		this.editable = editable;
		mainVC = createVelocityContainer("code_run");
		mainVC.setElementCssClass("o_code_run_element_css_class");
		setBlockLayoutClass(codeElement.getSettings());
		putInitialPanel(mainVC);
		initUI();
		updateUI();
	}

	private void setBlockLayoutClass(CodeSettings codeSettings) {
		mainVC.contextPut("blockLayoutClass", BlockLayoutClassFactory.buildClass(codeSettings, false));
	}

	private void initUI() {
		HashMap<String, String> languageKeyToValue = new HashMap();
		for (CodeLanguage codeLanguage : CodeLanguage.values()) {
			languageKeyToValue.put(codeLanguage.name(), codeLanguage.getDisplayText(getLocale()));
		}
		mainVC.contextPut("languageKeyToValue", languageKeyToValue);

		expandCollapseButton = LinkFactory.createCustomLink("expandCollapseButton", "expandCollapseButton",
				null, Link.BUTTON | Link.NONTRANSLATED, mainVC, this);
		expandCollapseButton.setElementCssClass("o_button_details");
	}

	private void updateUI() {
		CodeSettings settings = codeElement.getSettings();
		String content = codeElement.getContent();
		long numberOfLines = content.lines().count();
		CodeLanguage codeLanguage = settings.getCodeLanguage();
		if (codeLanguage.equals(CodeLanguage.auto)) {
			mainVC.contextRemove("codeLanguage");
		} else {
			mainVC.contextPut("codeLanguage", codeLanguage.name());
		}
		mainVC.contextPut("lineNumbersEnabled", settings.isLineNumbersEnabled());
		if (!settings.isDisplayAllLines() && settings.getNumberOfLinesToDisplay() > 0 && numberOfLines > settings.getNumberOfLinesToDisplay()) {
			mainVC.contextPut("height", (settings.getNumberOfLinesToDisplay() * 17) + "px");
		} else {
			mainVC.contextRemove("height");
		}
		mainVC.contextPut("expanded", expanded);
		expandCollapseButton.setIconLeftCSS("o_icon o_icon_lg " + (expanded ? "o_icon_details_collaps" : "o_icon_details_expand"));
		expandCollapseButton.setTitle(translate((expanded ? "details.collapse" : "details.expand")));
		mainVC.contextPut("allowExpand", !editable && numberOfLines <= MAX_LINES_TO_EXPAND);
		mainVC.contextPut("content", StringHelper.escapeHtml(content));
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof ChangePartEvent changePartEvent) {
			if (changePartEvent.getElement() instanceof CodeElement updatedCodeElement) {
				codeElement = updatedCodeElement;
				setBlockLayoutClass(codeElement.getSettings());
				updateUI();
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == expandCollapseButton) {
			expanded = !expanded;
			updateUI();
		}
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
