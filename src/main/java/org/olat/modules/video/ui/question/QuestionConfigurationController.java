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
package org.olat.modules.video.ui.question;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoQuestion;
import org.olat.modules.video.ui.VideoSettingsController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 déc. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionConfigurationController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on", "retry" };

	private TextElement beginEl;
	private TextElement timeLimitEl;
	private SingleSelection styleEl;
	private MultipleSelectionElement skippingEl;
	
	private VideoQuestion question;
	private final SimpleDateFormat displayDateFormat;
	private Long videoDurationInSecs;
	
	@Autowired
	private VideoModule videoModule;
	
	public QuestionConfigurationController(UserRequest ureq, WindowControl wControl, VideoQuestion question, Long videoDurationInSecs) {
		super(ureq, wControl, Util.createPackageTranslator(VideoSettingsController.class, ureq.getLocale()));
		this.question = question;
		this.videoDurationInSecs = videoDurationInSecs;
		displayDateFormat = new SimpleDateFormat("HH:mm:ss");
		displayDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		initForm(ureq);
	}
	
	public VideoQuestion getQuestion() {
		return question;
	}
	
	public void setVideoDurationInSecs(Long videoDurationInSecs) {
		if(videoDurationInSecs == null || videoDurationInSecs.longValue() <= 0l) return;
		
		this.videoDurationInSecs = videoDurationInSecs;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String time = null;
		try {
			time = displayDateFormat.format(question.getBegin());
		} catch (Exception e) {
			//
		}
		beginEl = uifactory.addTextElement("begin", "video.question.begin", 10, time, formLayout);
		beginEl.setExampleKey("time.format", null);
		beginEl.setMandatory(true);
		
		String timeLimit = question.getTimeLimit() > 0 ? Long.toString(question.getTimeLimit()) : "";
		timeLimitEl = uifactory.addTextElement("timeLimit", "video.question.timeLimit", 10, timeLimit, formLayout);
		
		String[] onValues = new String[] { translate("video.question.allow.skipping"), translate("video.question.allow.retry") };
		skippingEl = uifactory.addCheckboxesHorizontal("skipping", "video.question.skipping", formLayout, onKeys, onValues);
		if(question.isAllowSkipping()) {
			skippingEl.select("on", true);
		}
		if(question.isAllowNewAttempt()) {
			skippingEl.select("retry", true);
		}
		
		String[] colorKeys = videoModule.getMarkerStyles().toArray(new String[0]);
		String[] colorValues = new String[colorKeys.length];
		for(int i=colorKeys.length; i-->0; ) {
			colorValues[i] = translate("video.marker.style.".concat(colorKeys[i]));
		}
		styleEl = uifactory.addDropdownSingleselect("color", "video.marker.color", formLayout, colorKeys, colorValues);
		styleEl.setDomReplacementWrapperRequired(false);
		
		boolean found = false;
		String style = question.getStyle();
		for(String key:styleEl.getKeys()) {
			if(key.equals(style)) {
				styleEl.select(style, true);
				found = true;
			}
		}
		if(!found) {
			styleEl.select(styleEl.getKeys()[0], true);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		styleEl.clearError();
		if(!styleEl.isOneSelected()) {
			styleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		allOk &= validateBeginTime();
		allOk &= validateTimelimit();
		return allOk;
	}
	
	private boolean validateBeginTime() {
		boolean allOk = true;
		beginEl.clearError();
		if(!StringHelper.containsNonWhitespace(beginEl.getValue())) {
			beginEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else {
			try {
				Date val = displayDateFormat.parse(beginEl.getValue());
				if(videoDurationInSecs != null && (val.getTime() > (videoDurationInSecs.longValue() * 1000l))) {
					beginEl.setErrorKey("chapter.error.out.of.range", null);
					allOk &= false;
				}
			} catch(Exception e) {
				beginEl.setErrorKey("chapter.error.format", null);
				allOk &= false;
			}
		}
		return allOk;
	}
	
	private boolean validateTimelimit() {
		boolean allOk = true;
		timeLimitEl.clearError();
		if(StringHelper.containsNonWhitespace(timeLimitEl.getValue()) && !StringHelper.isLong(timeLimitEl.getValue())) {
			timeLimitEl.setErrorKey("form.error.nointeger", null);
			allOk &= false;
		}
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		try {
			String beginTime = beginEl.getValue();
			question.setBegin(displayDateFormat.parse(beginTime));
			if(StringHelper.containsNonWhitespace(timeLimitEl.getValue())
					&& StringHelper.isLong(timeLimitEl.getValue())) {
				question.setTimeLimit(Long.parseLong(timeLimitEl.getValue()));
			} else {
				question.setTimeLimit(-1l);
			}
			question.setStyle(styleEl.getSelectedKey());
			question.setAllowSkipping(skippingEl.isSelected(0));
			question.setAllowNewAttempt(skippingEl.isSelected(1));
			fireEvent(ureq, Event.CHANGED_EVENT);
		} catch (ParseException e) {
			beginEl.setErrorKey("form.legende.mandatory", null);
			logError("", e);
		}
	}
}
