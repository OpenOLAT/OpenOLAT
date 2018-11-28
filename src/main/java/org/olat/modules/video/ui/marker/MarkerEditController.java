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
package org.olat.modules.video.ui.marker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.video.VideoMarker;
import org.olat.modules.video.ui.VideoSettingsController;

/**
 * 
 * Initial date: 27 nov. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MarkerEditController extends FormBasicController {
	
	private RichTextElement markerTextEl;
	private TextElement beginEl;
	private TextElement durationEl;
	private TextElement colorEl;
	private TextElement topEl;
	private TextElement leftEl;
	private TextElement widthEl;
	private TextElement heightEl;
	
	private final VideoMarker marker;
	private final SimpleDateFormat displayDateFormat;
	private final long videoDurationInSecs;
	
	public MarkerEditController(UserRequest ureq, WindowControl wControl, VideoMarker marker, long videoDurationInSecs) {
		super(ureq, wControl, Util.createPackageTranslator(VideoSettingsController.class, ureq.getLocale()));
		this.marker = marker;
		this.videoDurationInSecs = videoDurationInSecs;
		displayDateFormat = new SimpleDateFormat("HH:mm:ss");
		displayDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		initForm(ureq);
	}
	
	public VideoMarker getMarker() {
		return marker;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String text = marker.getText();
		markerTextEl = uifactory.addRichTextElementForQTI21Match("text", "video.marker.text", text, 4, -1, null,
				formLayout, ureq.getUserSession(), getWindowControl());
		markerTextEl.getEditorConfiguration().disableImageAndMovie();
		markerTextEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.oneLine);
		if(!StringHelper.containsNonWhitespace(text)) {
			markerTextEl.setFocus(true);
		}
		markerTextEl.setMandatory(true);
		markerTextEl.setNotEmptyCheck("chapter.error.notitle");
		if(!StringHelper.containsNonWhitespace(text)) {
			markerTextEl.setFocus(true);
		}
		
		String time = null;
		try {
			time = displayDateFormat.format(marker.getBegin());
		} catch (Exception e) {
			//
		}
		beginEl = uifactory.addTextElement("begin","video.marker.begin", 10, time, formLayout);
		beginEl.setExampleKey("time.format", null);
		beginEl.setMandatory(true);
		
		String duration = Long.toString(marker.getDuration());
		durationEl = uifactory.addTextElement("duration", "video.marker.duration", 10, duration, formLayout);
		durationEl.setMandatory(true);
		
		String color = marker.getColor();
		colorEl = uifactory.addTextElement("color","video.marker.color", 10, color, formLayout);
		
		int topInPercent = (int)Math.round(marker.getTop() * 100.0d);
		topEl = uifactory.addTextElement("top", "video.marker.top", 3, Integer.toString(topInPercent), formLayout);
		topEl.setHelpText(translate("video.marker.position.hint"));
		int leftInPercent = (int)Math.round(marker.getLeft() * 100.0d);
		leftEl = uifactory.addTextElement("left", "video.marker.left", 3, Integer.toString(leftInPercent), formLayout);
		leftEl.setHelpText(translate("video.marker.position.hint"));
		
		int widthInPercent = (int)Math.round(marker.getWidth() * 100.0d);
		widthEl = uifactory.addTextElement("width", "video.marker.width", 3, Integer.toString(widthInPercent), formLayout);
		widthEl.setHelpText(translate("video.marker.size.hint"));
		int heightInPercent = (int)Math.round(marker.getHeight() * 100.0d);
		heightEl = uifactory.addTextElement("height", "video.marker.height", 3, Integer.toString(heightInPercent), formLayout);
		heightEl.setHelpText(translate("video.marker.size.hint"));

		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonGroupLayout);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		markerTextEl.clearError();
		if(!StringHelper.containsNonWhitespace(markerTextEl.getValue())) {
			markerTextEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		allOk &= validateBeginTime();
		allOk &= validateDuration();
		allOk &= validatePercent(topEl, true);
		allOk &= validatePercent(leftEl, true);
		allOk &= validatePercent(widthEl, false);
		allOk &= validatePercent(heightEl, false);
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
				if(val.getTime() > (videoDurationInSecs * 1000l)) {
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
	
	private boolean validateDuration() {
		boolean allOk = true;
		durationEl.clearError();
		if(!StringHelper.containsNonWhitespace(durationEl.getValue())) {
			durationEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(!StringHelper.isLong(durationEl.getValue())) {
			durationEl.setErrorKey("form.error.nointeger", null);
			allOk &= false;
		} else if(StringHelper.containsNonWhitespace(beginEl.getValue())) {
			try {
				Date begin = displayDateFormat.parse(beginEl.getValue());
				long end = Long.parseLong(durationEl.getValue()) + begin.getTime();
				if(end < 0 || end > (videoDurationInSecs * 1000l)) {
					durationEl.setErrorKey("chapter.error.out.of.range", null);
					allOk &= false;
				}
			} catch (NumberFormatException | ParseException e) {
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	private boolean validatePercent(TextElement el, boolean mandatory) {
		boolean allOk = true;
		
		el.clearError();
		if(mandatory && !StringHelper.containsNonWhitespace(el.getValue())) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(StringHelper.containsNonWhitespace(el.getValue())) {
			try {
				int val = Integer.parseInt(el.getValue());
				if(val < 0 || val > 100) {
					el.setErrorKey("error.percent.value", null);
					allOk &= false;
				}
			} catch(NumberFormatException e) {
				el.setErrorKey("error.percent.value", null);
				allOk &= false;
			}
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
			marker.setText(markerTextEl.getValue());
			
			String beginTime = beginEl.getValue();
			marker.setBegin(displayDateFormat.parse(beginTime));
			marker.setDuration(Long.parseLong(durationEl.getValue()));
			marker.setColor(colorEl.getValue());
			marker.setTop(Double.parseDouble(topEl.getValue()) / 100.0d);
			marker.setLeft(Double.parseDouble(leftEl.getValue()) / 100.0d);
			if(StringHelper.containsNonWhitespace(widthEl.getValue())) {
				marker.setWidth(Double.parseDouble(widthEl.getValue()) / 100.0d);		
			} else {
				marker.setWidth(-1.0d);
			}
			if(StringHelper.containsNonWhitespace(heightEl.getValue())) {
				marker.setHeight(Double.parseDouble(heightEl.getValue()) / 100.0d);		
			} else {
				marker.setHeight(-1.0d);
			}
			
			fireEvent(ureq, Event.DONE_EVENT);
		} catch (ParseException e) {
			beginEl.setErrorKey("form.legende.mandatory", null);
			logError("", e);
		}
	}
}
