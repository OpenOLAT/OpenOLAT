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
package org.olat.modules.video.ui.editor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Util;
import org.olat.modules.video.VideoMarker;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.ui.VideoSettingsController;
import org.olat.modules.video.ui.component.SelectTimeCommand;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-01-20<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AnnotationController extends FormBasicController {

	private final String videoElementId;
	private VideoMarker annotation;
	private TextElement startEl;
	private TextElement endEl;
	private TextElement durationEl;
	private RichTextElement textEl;
	private SingleSelection colorDropdown;
	private final SelectionValues colorsKV;
	private final SimpleDateFormat timeFormat;
	private SliderElement fontSize;
	private TextElement fontSizeEl;
	private StaticTextElement positionSizeEl;
	private FormLink positionSizeEditLink;
	@Autowired
	private VideoModule videoModule;

	public AnnotationController(UserRequest ureq, WindowControl wControl,
								String videoElementId, VideoMarker annotation) {
		super(ureq, wControl, "annotation");
		this.videoElementId = videoElementId;
		this.annotation = annotation;

		timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		colorsKV = new SelectionValues();
		Translator videoTranslator = Util.createPackageTranslator(VideoSettingsController.class, ureq.getLocale());
		for (String color : videoModule.getMarkerStyles()) {
			colorsKV.add(SelectionValues.entry(color, videoTranslator.translate("video.marker.style.".concat(color))));
		}

		initForm(ureq);
		setValues();
	}

	public void setAnnotation(VideoMarker annotation) {
		this.annotation = annotation;
		setValues();
	}

	public VideoMarker getAnnotation() {
		return annotation;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		startEl = uifactory.addTextElement("start", "form.annotation.startEnd", 8,
				"00:00:00", formLayout);
		startEl.setMandatory(true);
		startEl.addActionListener(FormEvent.ONBLUR);

		endEl = uifactory.addTextElement("end", "form.annotation.startEnd", 8,
				"00:00:00", formLayout);
		endEl.setMandatory(true);
		endEl.addActionListener(FormEvent.ONBLUR);

		durationEl = uifactory.addTextElement("duration", "form.annotation.duration", 2,
				"00", formLayout);
		durationEl.setExampleKey("form.annotation.duration.hint", null);
		durationEl.setMandatory(true);
		durationEl.addActionListener(FormEvent.ONBLUR);

		textEl = uifactory.addRichTextElementVeryMinimalistic("text", "form.annotation.text",
				"", 3, -1, true, null, formLayout,
				ureq.getUserSession(), getWindowControl());
		textEl.getEditorConfiguration().disableImageAndMovie();
		textEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.oneLine);
		textEl.setMandatory(true);

		colorDropdown = uifactory.addDropdownSingleselect("color", "form.annotation.color", formLayout,
				colorsKV.keys(), colorsKV.values());

		fontSize = uifactory.addSliderElement("fontSize", "form.annotation.fontSize", formLayout);
		fontSize.setMinValue(50);
		fontSize.setMaxValue(150);
		fontSize.setValue(100);
		fontSize.setStep(10);
		fontSize.addActionListener(FormEvent.ONCHANGE);

		fontSizeEl = uifactory.addTextElement("fontSizeValue", "form.annotation.fontSize", 3,
				"100", formLayout);
		fontSizeEl.setExampleKey("form.annotation.fontSize.hint", null);
		fontSizeEl.addActionListener(FormEvent.ONBLUR);

		positionSizeEl = uifactory.addStaticTextElement("positionSize", "form.annotation.positionSize",
				translate("form.annotation.positionSize.value", "", "", "", ""), formLayout);
		positionSizeEditLink = uifactory.addFormLink("editPositionSize", "", null, formLayout,
				Link.LINK_CUSTOM_CSS | Link.NONTRANSLATED);
		positionSizeEditLink.setIconLeftCSS("o_icon o_iqtest_icon");

		uifactory.addFormSubmitButton("save", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	private void setValues() {
		if (annotation == null) {
			return;
		}

		startEl.setValue(timeFormat.format(annotation.getBegin()));
		Date end = DateUtils.addSeconds(annotation.getBegin(), (int)annotation.getDuration());
		endEl.setValue(timeFormat.format(end));
		durationEl.setValue(Long.toString(annotation.getDuration()));
		textEl.setValue(annotation.getText());
		if (annotation.getStyle() != null) {
			colorDropdown.select(annotation.getStyle(), true);
			colorDropdown.getComponent().setDirty(true);
		}
		if (!colorDropdown.isOneSelected() && !colorsKV.isEmpty()) {
			colorDropdown.select(colorsKV.keys()[0], true);
			colorDropdown.getComponent().setDirty(true);
		}
		positionSizeEl.setValue(translate("form.annotation.positionSize.value",
				formatDouble(annotation.getTop()),
				formatDouble(annotation.getLeft()),
				formatDouble(annotation.getWidth()),
				formatDouble(annotation.getHeight())));
	}

	private String formatDouble(Double value) {
		if (value == null || Double.isNaN(value)) {
			return "";
		}
		String stringValue = String.format("%.2f", value * 100.0);
		if (stringValue.endsWith(".00")) {
			stringValue = stringValue.substring(0, stringValue.length() - 3);
		}
		return stringValue;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (fontSize == source) {
			fontSizeEl.setValue(Integer.toString((int)fontSize.getValue()));
		} else if (fontSizeEl == source) {
			try {
				double value = Double.parseDouble(fontSizeEl.getValue());
				value = Double.max(value, 50);
				value = Double.min(value, 150);
				value = Math.round(value / 10);
				int intValue = (int)value;
				intValue *= 10;
				fontSize.setValue(intValue);
				fontSizeEl.setValue(Integer.toString(intValue));
			} catch (Exception e) {
				//
			}
		} else if (startEl == source || durationEl == source) {
			try {
				Date start = timeFormat.parse(startEl.getValue());
				int duration = Integer.parseInt(durationEl.getValue());
				Date end = DateUtils.addSeconds(start, duration);
				endEl.setValue(timeFormat.format(end));
				if (startEl == source) {
					selectStartTime();
				}
			} catch (Exception e) {
				//
			}
		} else if (endEl == source) {
			try {
				Date start = timeFormat.parse(startEl.getValue());
				Date end = timeFormat.parse(endEl.getValue());
				double duration = (end.getTime() - start.getTime()) / 1000.0;
				durationEl.setValue(Integer.toString((int)duration));
			} catch (Exception e) {
				//
			}
		} else if (positionSizeEditLink == source) {
			System.err.println("edit position");
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void selectStartTime() {
		try {
			Date start = timeFormat.parse(startEl.getValue());
			long timeInSeconds = start.getTime() / 1000;
			SelectTimeCommand selectTimeCommand = new SelectTimeCommand(videoElementId, timeInSeconds);
			getWindowControl().getWindowBackOffice().sendCommandTo(selectTimeCommand);
		} catch (ParseException e) {
			logError("", e);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (annotation == null) {
			return;
		}

		try {
			annotation.setBegin(timeFormat.parse(startEl.getValue()));
			annotation.setDuration(Long.parseLong(durationEl.getValue()));
			annotation.setStyle(colorDropdown.getSelectedKey());
			annotation.setText(textEl.getValue());
			fireEvent(ureq, Event.DONE_EVENT);
		} catch (ParseException e) {
			logError("", e);
		}
	}
}
