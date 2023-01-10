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
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Util;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMarker;
import org.olat.modules.video.VideoMarkers;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.model.VideoMarkerImpl;
import org.olat.modules.video.ui.VideoSettingsController;
import org.olat.modules.video.ui.component.SelectTimeCommand;
import org.olat.modules.video.ui.marker.VideoMarkerRowComparator;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2022-11-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AnnotationsController extends FormBasicController {
	public static final Event RELOAD_MARKERS_EVENT = new Event("video.edit.reload.markers");

	@Autowired
	private VideoManager videoManager;

	@Autowired
	private VideoModule videoModule;

	private VideoMarkers videoMarkers;
	private String videoMarkerId;

	private SingleSelection annotationsDropdown;
	private SelectionValues annotationsKV = new SelectionValues();
	private FormLink addAnnotationButton;
	private TextElement startEl;
	private TextElement endEl;
	private TextElement durationEl;
	private RichTextElement textEl;
	private SingleSelection colorDropdown;
	private SelectionValues colorsKV;
	private SliderElement fontSize;
	private TextElement fontSizeEl;
	private FormSubmit saveButton;
	private FormCancel cancelButton;
	private final RepositoryEntry repositoryEntry;
	private final SimpleDateFormat timeFormat;
	private final Translator videoTranslator;
	private final String videoElementId;
	private String currentTimeCode;

	public AnnotationsController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
								 String videoElementId) {
		super(ureq, wControl, "annotations");
		videoTranslator = Util.createPackageTranslator(VideoSettingsController.class, ureq.getLocale());
		this.repositoryEntry = repositoryEntry;
		this.videoElementId = videoElementId;
		timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		colorsKV = new SelectionValues();
		for (String color : videoModule.getMarkerStyles()) {
			colorsKV.add(SelectionValues.entry(color, videoTranslator.translate("video.marker.style.".concat(color))));
		}

		initForm(ureq);
		loadModel();
		loadDefaultAnnotation();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		annotationsDropdown = uifactory.addDropdownSingleselect("annotations", "form.annotation.title",
				formLayout, annotationsKV.keys(), annotationsKV.values());
		annotationsDropdown.addActionListener(FormEvent.ONCHANGE);
		addAnnotationButton = uifactory.addFormLink("addAnnotation", "form.annotation.add",
				"form.annotation.add", formLayout, Link.BUTTON);

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

		fontSizeEl = uifactory.addTextElement("fontSizeValue", "form.annotation.fontSize", 3, "100", formLayout);
		fontSizeEl.setExampleKey("form.annotation.fontSize.hint", null);
		fontSizeEl.addActionListener(FormEvent.ONBLUR);

		saveButton = uifactory.addFormSubmitButton("save", formLayout);
		cancelButton = uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	private void loadModel() {
		annotationsKV = new SelectionValues();
		videoMarkers = videoManager.loadMarkers(repositoryEntry.getOlatResource());
		videoMarkers
				.getMarkers()
				.stream()
				.sorted(new VideoMarkerRowComparator())
				.forEach((m) -> annotationsKV.add(SelectionValues.entry(m.getId(), m.getText())));
		annotationsDropdown.setKeysAndValues(annotationsKV.keys(), annotationsKV.values(), null);
		annotationsDropdown.setEscapeHtml(false);
		if (videoMarkerId != null) {
			VideoMarker videoMarker = videoMarkers.getMarkerById(videoMarkerId);
			setValues(videoMarker);
			annotationsDropdown.select(videoMarkerId, true);
		}
	}

	private void loadDefaultAnnotation() {
		if (!videoMarkers.getMarkers().isEmpty()) {
			if (videoMarkerId == null) {
				videoMarkerId = annotationsKV.keys()[0];
			}
			annotationsDropdown.select(videoMarkerId, true);
			setValues(videoMarkers.getMarkerById(videoMarkerId));
		}
	}

	private void setValues(VideoMarker videoMarker) {
		startEl.setValue(timeFormat.format(videoMarker.getBegin()));
		Date end = DateUtils.addSeconds(videoMarker.getBegin(), (int)videoMarker.getDuration());
		endEl.setValue(timeFormat.format(end));
		durationEl.setValue(Long.toString(videoMarker.getDuration()));
		textEl.setValue(videoMarker.getText());
		if (videoMarker.getStyle() != null) {
			colorDropdown.select(videoMarker.getStyle(), true);
			colorDropdown.getComponent().setDirty(true);
		}
		if (!colorDropdown.isOneSelected() && !colorsKV.isEmpty()) {
			colorDropdown.select(colorsKV.keys()[0], true);
			colorDropdown.getComponent().setDirty(true);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (addAnnotationButton == source) {
			doAddAnnotation(ureq);
		}
		if (annotationsDropdown == source) {
			videoMarkerId = annotationsDropdown.getSelectedKey();
			VideoMarker videoMarker = videoMarkers.getMarkerById(videoMarkerId);
			if (videoMarker != null) {
				setValues(videoMarker);
				selectStartTime();
			}
		}
		if (fontSize == source) {
			fontSizeEl.setValue(Integer.toString((int)fontSize.getValue()));
		}
		if (fontSizeEl == source) {
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
			}
		}
		if (startEl == source || durationEl == source) {
			try {
				Date start = timeFormat.parse(startEl.getValue());
				int duration = Integer.parseInt(durationEl.getValue());
				Date end = DateUtils.addSeconds(start, duration);
				endEl.setValue(timeFormat.format(end));
			} catch (Exception e) {
			}
		}
		if (startEl == source) {
			try {
				selectStartTime();
			} catch (Exception e) {
			}
		}
		if (endEl == source) {
			try {
				Date start = timeFormat.parse(startEl.getValue());
				Date end = timeFormat.parse(endEl.getValue());
				double duration = (end.getTime() - start.getTime()) / 1000;
				durationEl.setValue(Integer.toString((int)duration));
			} catch (Exception e) {
			}
		}
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
		try {
			VideoMarker videoMarker = videoMarkers.getMarkerById(videoMarkerId);
			videoMarker.setBegin(timeFormat.parse(startEl.getValue()));
			videoMarker.setDuration(Long.parseLong(durationEl.getValue()));
			videoMarker.setStyle(colorDropdown.getSelectedKey());
			videoMarker.setText(textEl.getValue());
			videoManager.saveMarkers(videoMarkers, repositoryEntry.getOlatResource());
			loadModel();
			reloadMarkers(ureq);
			// selectStartTime();
		} catch (ParseException e) {
			logError("", e);
		}
	}

	public void setCurrentTimeCode(String currentTimeCode) {
		this.currentTimeCode = currentTimeCode;
	}

	private void doAddAnnotation(UserRequest ureq) {
		VideoMarkerImpl newVideoMarker = new VideoMarkerImpl();
		newVideoMarker.setId(UUID.randomUUID().toString());
		newVideoMarker.setDuration(5);
		if (currentTimeCode != null) {
			long time = Math.round(Double.parseDouble(currentTimeCode)) * 1000;
			newVideoMarker.setBegin(new Date(time));
		} else {
			newVideoMarker.setBegin(new Date(0));
		}
		newVideoMarker.setText(timeFormat.format(newVideoMarker.getBegin()));
		newVideoMarker.setLeft(25);
		newVideoMarker.setTop(25);
		newVideoMarker.setWidth(50);
		newVideoMarker.setHeight(50);
		newVideoMarker.setStyle(colorsKV.keys()[0]);

		videoMarkerId = newVideoMarker.getId();
		videoMarkers.getMarkers().add(newVideoMarker);
		videoManager.saveMarkers(videoMarkers, repositoryEntry.getOlatResource());
		loadModel();
		reloadMarkers(ureq);
	}

	private void reloadMarkers(UserRequest ureq) {
		fireEvent(ureq, RELOAD_MARKERS_EVENT);
	}

	public void setAnnotationId(String annotationId) {
		videoMarkerId = annotationId;
		VideoMarker videoMarker = videoMarkers.getMarkerById(videoMarkerId);
		setValues(videoMarker);
		annotationsDropdown.select(videoMarkerId, true);
		annotationsDropdown.getComponent().setDirty(true);
	}
}
