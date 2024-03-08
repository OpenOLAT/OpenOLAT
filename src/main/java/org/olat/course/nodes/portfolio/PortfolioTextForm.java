/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */

package org.olat.course.nodes.portfolio;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * Description:<br>
 * This form edit the explanation text of the course building block
 *
 * <p>
 * Initial Date:  6 oct. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class PortfolioTextForm extends FormBasicController {

	private RichTextElement textEl;

	private final ModuleConfiguration config;

	public PortfolioTextForm(UserRequest ureq, WindowControl wControl, PortfolioCourseNode courseNode) {
		super(ureq, wControl);
		config = courseNode.getModuleConfiguration();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("pane.tab.portfolio_config.explanation");
		Object nodeText = config.get(PortfolioCourseNodeConfiguration.NODE_TEXT);
		String text = nodeText instanceof String nodeTextString ? nodeTextString : "";
		textEl = uifactory.addRichTextElementForStringDataMinimalistic("text", "explanation.text", text, 10, -1, formLayout, getWindowControl());

		if (formLayout instanceof FormLayoutContainer layoutContainer) {
			FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			buttonGroupLayout.setRootForm(mainForm);
			layoutContainer.add(buttonGroupLayout);
			uifactory.addFormSubmitButton("save", buttonGroupLayout);
		}
	}

	protected ModuleConfiguration getUpdatedConfig() {
		String text = textEl.getValue();
		config.set(PortfolioCourseNodeConfiguration.NODE_TEXT, text);
		return config;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
}