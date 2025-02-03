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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticListElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ConfirmDeleteCurriculumElementListController extends ConfirmationController {
	
	private MultipleSelectionElement notifyEl;
	
	private final List<CurriculumElement> implementations;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	
	public ConfirmDeleteCurriculumElementListController(UserRequest ureq, WindowControl wControl,
			String message, String confirmation, String confirmButton,
			List<CurriculumElement> implementations) {
		super(ureq, wControl, message, confirmation, confirmButton, ButtonType.danger, false);
		setTranslator(Util.createPackageTranslator(ConfirmationController.class, getLocale(), getTranslator()));
		this.implementations = implementations;

		initForm(ureq);
	}
	
	private String getType(CurriculumElement implementation) {
		if(implementation.getType() != null) {
			return implementation.getType().getDisplayName();
		}
		return "";
	}
	
	@Override
	protected void initFormElements(FormLayoutContainer confirmCont) {
		List<String> values = new ArrayList<>();
		for(CurriculumElement implementation:implementations) {
			String type = getType(implementation);
			String val = translate("curriculums.implementations.bulk.delete.value",
					StringHelper.escapeHtml(implementation.getDisplayName()),
					StringHelper.escapeHtml(type),
					StringHelper.escapeHtml(implementation.getCurriculum().getDisplayName()));
			values.add(val);
		}

		StaticListElement deleteValuesEl = uifactory.addStaticListElement("delete.values", "curriculums.implementations.bulk.delete.values",
				values, confirmCont);
		deleteValuesEl.setEscapeMode(EscapeMode.none);
		
		StringBuilder impact = new StringBuilder();
		impact.append("<ul class='o_static_list'>");
		impact.append("<li>")
		      .append(translate("curriculums.implementations.bulk.delete.impacts.descendants"))
		      .append("</li>");
		impact.append("<li>")
	          .append(translate("curriculums.implementations.bulk.delete.impacts.entries"))
	          .append("</li>");
		impact.append("<li>")
	          .append(translate("curriculums.implementations.bulk.delete.impacts.members"))
	          .append("</li>");
		impact.append("</ul>");

		StaticTextElement impactEl = uifactory.addStaticTextElement("curriculums.elements.bulk.delete.impacts", impact.toString(), confirmCont);
		impactEl.setDomWrapperElement(DomWrapperElement.div);
		impactEl.setVisible(impact.length() > 10);

		notifyEl = uifactory.addCheckboxesHorizontal("curriculums.elements.bulk.delete.notify", "curriculums.elements.bulk.delete.notify", confirmCont,
				new String[] { "notify" }, new String[] { translate("curriculums.elements.bulk.delete.notify.val") });
	}
	
	@Override
	protected void doAction(UserRequest ureq) {
		boolean notify = notifyEl != null && notifyEl.isAtLeastSelected(1);
		for(CurriculumElement curriculumElement:implementations) {
			curriculumService.deleteSoftlyCurriculumElement(curriculumElement, getIdentity(), notify);
			dbInstance.commitAndCloseSession();
		}
		super.doAction(ureq);
	}
}
