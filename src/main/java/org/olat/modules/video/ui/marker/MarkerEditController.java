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
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.video.VideoMarker;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.ui.VideoSettingsController;
import org.springframework.beans.factory.annotation.Autowired;

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
	private SingleSelection styleEl;
	private TextElement topEl;
	private TextElement leftEl;
	private TextElement widthEl;
	private TextElement heightEl;
	
	private VideoMarker marker;
	private final SimpleDateFormat displayDateFormat;
	private Long videoDurationInSecs;
	
	@Autowired
	private VideoModule videoModule;
	
	public MarkerEditController(UserRequest ureq, WindowControl wControl, Long videoDurationInSecs) {
		super(ureq, wControl, "marker_edit", Util.createPackageTranslator(VideoSettingsController.class, ureq.getLocale()));
		this.videoDurationInSecs = videoDurationInSecs;
		displayDateFormat = new SimpleDateFormat("HH:mm:ss");
		displayDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		initForm(ureq);
	}
	
	public void setVideoDurationInSecs(Long videoDurationInSecs) {
		this.videoDurationInSecs = videoDurationInSecs;
	}
	
	public VideoMarker getMarker() {
		return marker;
	}
	
	public void setMarker(VideoMarker marker) {
		this.marker = marker;
		
		markerTextEl.setValue(marker.getText());
		
		String time = null;
		try {
			time = displayDateFormat.format(marker.getBegin());
		} catch (Exception e) {
			//
		}
		beginEl.setValue(time);
		String duration = Long.toString(marker.getDuration());
		durationEl.setValue(duration);
		String style = marker.getStyle();
		
		boolean found = false;
		for(String key:styleEl.getKeys()) {
			if(key.equals(style)) {
				styleEl.select(style, true);
				found = true;
			}
		}
		if(!found) {
			styleEl.select(styleEl.getKeys()[0], true);
		}
		
		topEl.setValue(toPercentValue(marker.getTop()));
		leftEl.setValue(toPercentValue(marker.getLeft()));
		widthEl.setValue(toPercentValue(marker.getWidth()));
		heightEl.setValue(toPercentValue(marker.getHeight()));
		
		flc.setDirty(true);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		markerTextEl = uifactory.addRichTextElementVeryMinimalistic("text", "video.marker.text", "", 4, -1, true, null,
				formLayout, ureq.getUserSession(), getWindowControl());
		markerTextEl.getEditorConfiguration().disableImageAndMovie();
		markerTextEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.oneLine);
		markerTextEl.setMandatory(true);
		markerTextEl.setNotEmptyCheck("chapter.error.notitle");
		
		beginEl = uifactory.addTextElement("begin", "video.marker.begin", 10, "", formLayout);
		inline(beginEl, 5);
		beginEl.setExampleKey("time.format", null);
		beginEl.setMandatory(true);
		
		durationEl = uifactory.addTextElement("duration", "video.marker.duration", 10, "", formLayout);
		inline(durationEl, 2);
		durationEl.setMandatory(true);

		String[] colorKeys = videoModule.getMarkerStyles().toArray(new String[0]);
		String[] colorValues = new String[colorKeys.length];
		for(int i=colorKeys.length; i-->0; ) {
			colorValues[i] = translate("video.marker.style.".concat(colorKeys[i]));
		}
		styleEl = uifactory.addDropdownSingleselect("color", "video.marker.color", formLayout, colorKeys, colorValues);
		styleEl.setDomReplacementWrapperRequired(false);
		
		topEl = uifactory.addTextElement("top", "video.marker.top", 3, "", formLayout);
		inline(topEl, 1);
		topEl.setHelpText(translate("video.marker.position.hint"));

		leftEl = uifactory.addTextElement("left", "video.marker.left", 3, "", formLayout);
		inline(leftEl, 1);
		leftEl.setHelpText(translate("video.marker.position.hint"));
		
		widthEl = uifactory.addTextElement("width", "video.marker.width", 3, "", formLayout);
		inline(widthEl, 1);
		widthEl.setHelpText(translate("video.marker.size.hint"));
		
		heightEl = uifactory.addTextElement("height", "video.marker.height", 3, "", formLayout);
		inline(heightEl, 1);
		heightEl.setHelpText(translate("video.marker.size.hint"));

		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", formLayout);
	}
	
	private void inline(TextElement element, int displaySize) {
		element.setDisplaySize(displaySize);
		element.setDomReplacementWrapperRequired(false);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		markerTextEl.clearError();
		if(!StringHelper.containsNonWhitespace(markerTextEl.getValue())) {
			markerTextEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		styleEl.clearError();
		if(!styleEl.isOneSelected()) {
			styleEl.setErrorKey("form.legende.mandatory", null);
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
		} else if(videoDurationInSecs != null) {
			try {
				Date val = displayDateFormat.parse(beginEl.getValue());
				if(val.getTime() > (videoDurationInSecs.longValue() * 1000l)) {
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
				long duration = Long.parseLong(durationEl.getValue());
				long end = duration + begin.getTime();
				if(duration == 0) {
					durationEl.setErrorKey("chapter.error.out.of.range", null);
					allOk &= false;
				} else if(videoDurationInSecs != null && (end < 1 || end > (videoDurationInSecs.longValue() * 1000l))) {
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
			marker.setStyle(styleEl.getSelectedKey());
			marker.setTop(fromPercent(topEl.getValue()));
			marker.setLeft(fromPercent(leftEl.getValue()));
			marker.setWidth(fromPercent(widthEl.getValue()));		
			marker.setHeight(fromPercent(heightEl.getValue()));		
			fireEvent(ureq, Event.DONE_EVENT);
		} catch (ParseException e) {
			beginEl.setErrorKey("form.legende.mandatory", null);
			logError("", e);
		}
	}
	
	private String toPercentValue(double val) {
		int widthInPercent = (int)Math.round(val * 100.0d);
		return String.valueOf(widthInPercent);
	}
	
	private double fromPercent(String val) {
		if(!StringHelper.containsNonWhitespace(val)) return -1.0d;
		
		try {
			return Double.parseDouble(val) / 100.0d;
		} catch (NumberFormatException e) {
			logWarn("", e);
			return 0.0d;
		}
	}
}
