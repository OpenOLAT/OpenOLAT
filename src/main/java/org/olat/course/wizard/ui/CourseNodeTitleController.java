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
package org.olat.course.wizard.ui;

import java.util.function.Supplier;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.editor.EditorMainController;
import org.olat.course.editor.NodeConfigController;
import org.olat.course.nodes.CourseNodeHelper;
import org.olat.course.wizard.CourseNodeTitleContext;

/**
 * 
 * Initial date: 11 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeTitleController extends StepFormBasicController {
	
	private TextElement titleEl;
	private TextElement shortTitleEl;
	private RichTextElement descriptionEl;
	
	private final CourseNodeTitleContext context;

	public CourseNodeTitleController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, String runContextKey, Supplier<Object> contextCreator) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(EditorMainController.class, getLocale(), getTranslator()));
		context = (CourseNodeTitleContext)getOrCreateFromRunContext(runContextKey, contextCreator);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		titleEl = uifactory.addTextElement("nodeConfigForm.displaytitle", "nodeConfigForm.displaytitle", 255, null, formLayout);
		titleEl.setCheckVisibleLength(true);
		titleEl.setExampleKey("nodeConfigForm.max.length.recommended", new String[] {String.valueOf(NodeConfigController.LONG_TITLE_MAX_LENGTH)});
		titleEl.setMandatory(true);
		
		shortTitleEl = uifactory.addTextElement("nodeConfigForm.shorttitle", "nodeConfigForm.shorttitle",
				NodeConfigController.SHORT_TITLE_MAX_LENGTH, null, formLayout);
		shortTitleEl.setCheckVisibleLength(true);
		shortTitleEl.setExampleKey("nodeConfigForm.max.length", new String[] {String.valueOf(NodeConfigController.SHORT_TITLE_MAX_LENGTH)});
		shortTitleEl.enablePlaceholderUpdate(titleEl.getFormDispatchId(), NodeConfigController.SHORT_TITLE_MAX_LENGTH);

		descriptionEl = uifactory.addRichTextElementForStringData("nodeConfigForm.description",
				"nodeConfigForm.description", context.getDescription(), 10, -1, false, null, null, formLayout,
				ureq.getUserSession(), getWindowControl());
		descriptionEl.setMaxLength(4000);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		shortTitleEl.clearError();
		if (!StringHelper.containsNonWhitespace(shortTitleEl.getValue())) {
			shortTitleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if (titleEl.getValue().length() > NodeConfigController.LONG_TITLE_MAX_LENGTH) {
			titleEl.setErrorKey("error.title.too.long", true, new String[] {String.valueOf(NodeConfigController.LONG_TITLE_MAX_LENGTH)});
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String longTitle = titleEl.getValue();
		context.setLongTitle(longTitle);
		
		String shortTitle = shortTitleEl.getValue();
		if (!CourseNodeHelper.isCustomShortTitle(longTitle, shortTitle)) {
			shortTitle = null;
			shortTitleEl.setValue(null);
		}
		context.setShortTitle(shortTitle);
		
		String description = descriptionEl.getValue();
		context.setDescription(description);

		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
