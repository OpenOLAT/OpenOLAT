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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMarker;
import org.olat.modules.video.VideoMarkers;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.model.VideoMarkerImpl;
import org.olat.modules.video.ui.marker.VideoMarkerRowComparator;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-01-20<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AnnotationsHeaderController extends FormBasicController {
	public final static Event ANNOTATION_ADDED_EVENT = new Event("annotation.added");
	public final static Event ANNOTATION_DELETED_EVENT = new Event("annotation.deleted");
	private final RepositoryEntry repositoryEntry;
	private FormLink previousAnnotationButton;
	private SingleSelection annotationsDropdown;
	private SelectionValues annotationsKV = new SelectionValues();
	private FormLink nextAnnotationButton;
	private FormLink addAnnotationButton;
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private VideoModule videoModule;
	private VideoMarkers annotations;
	private String annotationId;
	private String currentTimeCode;
	private FormLink commandsButton;
	private HeaderCommandsController commandsController;
	private CloseableCalloutWindowController ccwc;
	private final SimpleDateFormat timeFormat;

	protected AnnotationsHeaderController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		super(ureq, wControl, "annotations_header");
		this.repositoryEntry = repositoryEntry;

		timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		initForm(ureq);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(ccwc);
		removeAsListenerAndDispose(commandsController);
		commandsController = null;
		ccwc = null;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		previousAnnotationButton = uifactory.addFormLink("previousAnnotation", "", "",
				formLayout, Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
		previousAnnotationButton.setIconRightCSS("o_icon o_icon_back");

		annotationsDropdown = uifactory.addDropdownSingleselect("annotations", "form.annotation.title",
				formLayout, annotationsKV.keys(), annotationsKV.values());
		annotationsDropdown.addActionListener(FormEvent.ONCHANGE);

		nextAnnotationButton = uifactory.addFormLink("nextAnnotation", "", "",
				formLayout, Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
		nextAnnotationButton.setIconRightCSS("o_icon o_icon_start");

		addAnnotationButton = uifactory.addFormLink("addAnnotation", "form.annotation.add",
				"form.annotation.add", formLayout, Link.BUTTON);

		commandsButton = uifactory.addFormLink("commands", "", "", formLayout,
				Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
		commandsButton.setIconRightCSS("o_icon o_icon_commands");
	}

	public void setAnnotations(VideoMarkers annotations) {
		this.annotations = annotations;
		setValues();
	}

	public VideoMarkers getAnnotations() {
		return annotations;
	}

	private void setValues() {
		annotationsKV = new SelectionValues();
		annotations
				.getMarkers()
				.stream()
				.sorted(new VideoMarkerRowComparator())
				.forEach(a -> annotationsKV.add(SelectionValues.entry(a.getId(), timeFormat.format(a.getBegin()) + " - " + a.getText())));
		flc.contextPut("hasAnnotations", !annotationsKV.isEmpty());
		annotationsDropdown.setKeysAndValues(annotationsKV.keys(), annotationsKV.values(), null);
		annotationsDropdown.setEscapeHtml(false);

		if (annotations.getMarkers().stream().noneMatch(a -> a.getId().equals(annotationId))) {
			annotationId = null;
		}
		if (annotationId == null && !annotationsKV.isEmpty()) {
			annotationId = annotationsKV.keys()[0];
		}
		if (annotationId != null) {
			annotationsDropdown.select(annotationId, true);
		}

		int selectedIndex = -1;
		for (int i = 0; i < annotationsKV.size(); i++) {
			if (annotationsKV.keys()[i].equals(annotationId)) {
				selectedIndex = i;
				break;
			}
		}

		if (selectedIndex != -1) {
			previousAnnotationButton.setEnabled(selectedIndex > 0);
			nextAnnotationButton.setEnabled(selectedIndex < (annotationsKV.size() - 1));
		}

		commandsButton.setEnabled(!annotationsKV.isEmpty());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (addAnnotationButton == source) {
			doAddAnnotation(ureq);
		} else if (commandsButton == source) {
			doCommands(ureq);
		} else if (annotationsDropdown == source) {
			if (annotationsDropdown.isOneSelected()) {
				annotationId = annotationsDropdown.getSelectedKey();
				setValues();
				handleAnnotationSelected(ureq);
			}
		} else if (nextAnnotationButton == source) {
			doNextAnnotation(ureq);
		} else if (previousAnnotationButton == source) {
			doPreviousAnnotation(ureq);
		}

		super.formInnerEvent(ureq, source, event);
	}

	private void handleAnnotationSelected(UserRequest ureq) {
		getOptionalAnnotation()
				.ifPresent(a -> fireEvent(ureq, new AnnotationSelectedEvent(a.getId(), a.getBegin().getTime(), a.getDuration())));
	}

	private Optional<VideoMarker> getOptionalAnnotation() {
		if (annotationId == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(annotations.getMarkerById(annotationId));
	}

	private void doPreviousAnnotation(UserRequest ureq) {
		String[] keys = annotationsDropdown.getKeys();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			if (annotationId != null && annotationId.equals(key)) {
				int newIndex = i - 1;
				if (newIndex >= 0) {
					annotationId = keys[newIndex];
					setValues();
					handleAnnotationSelected(ureq);
				}
				break;
			}
		}
	}

	private void doNextAnnotation(UserRequest ureq) {
		String[] keys = annotationsDropdown.getKeys();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			if (annotationId != null && annotationId.equals(key)) {
				int newIndex = i + 1;
				if (newIndex < keys.length) {
					annotationId = keys[newIndex];
					setValues();
					handleAnnotationSelected(ureq);
				}
				break;
			}
		}
	}

	private void doAddAnnotation(UserRequest ureq) {
		VideoMarkerImpl newAnnotation = new VideoMarkerImpl();
		newAnnotation.setId(UUID.randomUUID().toString());
		newAnnotation.setDuration(5);
		if (currentTimeCode != null) {
			long time = Math.round(Double.parseDouble(currentTimeCode)) * 1000;
			newAnnotation.setBegin(new Date(time));
		} else {
			newAnnotation.setBegin(new Date(0));
		}
		newAnnotation.setText(translate("form.annotation.new"));
		newAnnotation.setLeft(0.25);
		newAnnotation.setTop(0.25);
		newAnnotation.setWidth(0.50);
		newAnnotation.setHeight(0.50);
		newAnnotation.setStyle(videoModule.getMarkerStyles().get(0));

		annotationId = newAnnotation.getId();
		annotations.getMarkers().add(newAnnotation);
		videoManager.saveMarkers(annotations, repositoryEntry.getOlatResource());
		setValues();
		fireEvent(ureq, ANNOTATION_ADDED_EVENT);
	}

	private void doCommands(UserRequest ureq) {
		commandsController = new HeaderCommandsController(ureq, getWindowControl());
		listenTo(commandsController);
		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(), commandsController.getInitialComponent(),
				commandsButton.getFormDispatchId(), "", true, "");
		listenTo(ccwc);
		ccwc.activate();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (ccwc == source) {
			cleanUp();
		} else if (commandsController == source) {
			if (HeaderCommandsController.DELETE_EVENT.getCommand().equals(event.getCommand())) {
				doDeleteAnnotation(ureq);
			}
			ccwc.deactivate();
			cleanUp();
		}
	}

	private void doDeleteAnnotation(UserRequest ureq) {
		if (annotationId == null) {
			return;
		}

		VideoMarker annotation = annotations.getMarkerById(annotationId);
		if (annotation == null) {
			return;
		}

		annotations.getMarkers().remove(annotation);
		if (annotations.getMarkers().isEmpty()) {
			annotationId = null;
		} else {
			annotationId = annotations.getMarkers().get(0).getId();
		}
		setValues();
		fireEvent(ureq, ANNOTATION_DELETED_EVENT);
	}

	public void setCurrentTimeCode(String currentTimeCode) {
		this.currentTimeCode = currentTimeCode;
	}

	public void setAnnotationId(String annotationId) {
		this.annotationId = annotationId;
		if (annotationId != null) {
			setValues();
		}
	}

	public String getAnnotationId() {
		return annotationId;
	}

	public void handleDeleted(String annotationId) {
		annotations.getMarkers().removeIf(a -> a.getId().equals(annotationId));
		setAnnotations(annotations);
	}
}
