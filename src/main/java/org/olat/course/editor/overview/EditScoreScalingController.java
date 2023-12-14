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
package org.olat.course.editor.overview;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.editor.EditorMainController;
import org.olat.course.run.scoring.ScoreScalingHelper;

/**
 * 
 * Initial date: 28 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditScoreScalingController extends FormBasicController {
	
	private FormLink closeButton;
	private TextElement scoreScalingEl;
	
	private final OverviewRow row;
	
	public EditScoreScalingController(UserRequest ureq, WindowControl wControl, OverviewRow row) {
		super(ureq, wControl, "scorescaling", Util.createPackageTranslator(EditorMainController.class, ureq.getLocale()));
		this.row = row;
		initForm(ureq);
	}
	
	public String getScale() {
		return scoreScalingEl.getValue();
	}
	
	public OverviewRow getRow() {
		return row;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String scaling = row.getAssessmentConfig().getScoreScale();
		scoreScalingEl = uifactory.addTextElement("score.scaling", 10, scaling, formLayout);
		scoreScalingEl.setExampleKey("score.scaling.example", null);
		scoreScalingEl.setFocus(true);
		
		FormSubmit submit = uifactory.addFormSubmitButton("save", "save", formLayout);
		submit.setIconLeftCSS("o_icon o_icon_submit");
		closeButton = uifactory.addFormLink("cancel", formLayout, Link.BUTTON);
		closeButton.setIconLeftCSS("o_icon o_icon_close");
		closeButton.setElementCssClass(scaling);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= ScoreScalingHelper.validateScoreScaling(scoreScalingEl);
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(closeButton == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT); 
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
