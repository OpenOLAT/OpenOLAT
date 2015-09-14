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
package org.olat.modules.qpool.ui.metadata;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.manager.MetadataConverterHelper;
import org.olat.modules.qpool.model.LOMDuration;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.events.QPoolEvent;

/**
 * 
 * Initial date: 05.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EducationalMetadataController extends FormBasicController {
	
	private FormLink editLink;
	private StaticTextElement contextEl, learningTimeEl;
	
	private final boolean edit;

	public EducationalMetadataController(UserRequest ureq, WindowControl wControl, QuestionItem item, boolean edit) {
		super(ureq, wControl, "view");
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));
		this.edit = edit;
		initForm(ureq);
		setItem(item);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("educational");
		if(edit) {
			editLink = uifactory.addFormLink("edit", "edit", null, formLayout, Link.BUTTON_XSMALL);
			editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		}

		contextEl = uifactory.addStaticTextElement("educational.context", "", formLayout);
		learningTimeEl = uifactory.addStaticTextElement("educational.learningTime", "", formLayout);
	}

	public void setItem(QuestionItem item) {
		QEducationalContext context = item.getEducationalContext();
		if(context == null || context.getLevel() == null) {
			contextEl.setValue("");
		} else {
			String translation = translate("item.level." + context.getLevel().toLowerCase());
			if(translation.length() > 128) {
				translation = context.getLevel();
			}
			contextEl.setValue(translation);
		}
		
		String learningTime = durationToString(item);
		learningTimeEl.setValue(learningTime);
	}
	
	private String durationToString(QuestionItem item) {
		if(item == null) return "";
		String timeStr = item.getEducationalLearningTime();
		LOMDuration duration = MetadataConverterHelper.convertDuration(timeStr);
		
		StringBuilder sb = new StringBuilder();
		if(duration.getDay() > 0) {
			sb.append(duration.getDay()).append("d ");
		}
		if(duration.getHour() > 0 || duration.getMinute() > 0 || duration.getSeconds() > 0) {
			int hour = duration.getHour() < 0 ? 0 : duration.getHour();
			sb.append(hour).append(":")
			  .append(doubleZero(duration.getMinute())).append(":")
			  .append(doubleZero(duration.getSeconds()));
		}
		return sb.toString();
	}
	
	private String doubleZero(int val) {
		if(val < 0) {
			return "00";
		} else if(val < 10) {
			return "0" + val;
		}
		return Integer.toString(val);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(editLink == source) {
			fireEvent(ureq, new QPoolEvent(QPoolEvent.EDIT));
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
}
