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
package org.olat.modules.portfolio.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.AssignmentType;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignmentEditController extends FormBasicController {

	private static final String[] typeKeys = new String[]{ AssignmentType.essay.name() };
	
	private TextElement titleEl;
	private SingleSelection typeEl;
	private RichTextElement summaryEl, contentEl;
	
	private Section section;
	private Assignment assignment;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public AssignmentEditController(UserRequest ureq, WindowControl wControl, Section section) {
		super(ureq, wControl);
		this.section = section;
		initForm(ureq);
	}
	
	public AssignmentEditController(UserRequest ureq, WindowControl wControl, Assignment assignment) {
		super(ureq, wControl);
		this.assignment = assignment;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String title = assignment == null ? null : assignment.getTitle();
		titleEl = uifactory.addTextElement("title", "assignment.title", 255, title, formLayout);
		titleEl.setMandatory(true);
		
		String summary = assignment == null ? null : assignment.getSummary();
		summaryEl = uifactory.addRichTextElementForStringDataCompact("summary", "assignment.summary", summary, 6, 60, null, formLayout,
				ureq.getUserSession(), getWindowControl());
		summaryEl.setPlaceholderKey("summary.placeholder", null);
		summaryEl.getEditorConfiguration().disableMedia();
		summaryEl.getEditorConfiguration().disableImageAndMovie();
		
		String content = assignment == null ? null : assignment.getContent();
		contentEl = uifactory.addRichTextElementForStringDataCompact("content", "assignment.content", content, 6, 60, null, formLayout,
				ureq.getUserSession(), getWindowControl());
		contentEl.getEditorConfiguration().disableMedia();
		contentEl.getEditorConfiguration().disableImageAndMovie();
		
		String[] typeValues = new String[]{ translate("assignment.type.essay") };
		typeEl = uifactory.addDropdownSingleselect("type", "assignment.type", formLayout, typeKeys, typeValues, null);
		typeEl.select(typeKeys[0], true);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);

		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		if(assignment == null) {
			String title = titleEl.getValue();
			String summary = summaryEl.getValue();
			String content = contentEl.getValue();
			AssignmentType type = AssignmentType.valueOf(typeEl.getSelectedKey());
			assignment = portfolioService.addAssignment(title, summary, content, type, section);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
