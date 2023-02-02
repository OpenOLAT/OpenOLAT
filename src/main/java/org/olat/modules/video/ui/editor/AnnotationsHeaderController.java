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

import java.util.Date;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMarker;
import org.olat.modules.video.VideoMarkers;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.model.VideoMarkerImpl;
import org.olat.modules.video.ui.component.SelectTimeCommand;
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
	private final String videoElementId;
	private SingleSelection annotationsDropdown;
	private SelectionValues annotationsKV = new SelectionValues();
	private FormLink addAnnotationButton;
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private VideoModule videoModule;
	private VideoMarkers annotations;
	private String annotationId;
	private String currentTimeCode;
	private final String newAnnotationColor;
	private FormLink commandsButton;
	private CommandsController commandsController;
	private CloseableCalloutWindowController ccwc;

	protected AnnotationsHeaderController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
										  String videoElementId) {
		super(ureq, wControl, "annotations_header");
		this.repositoryEntry = repositoryEntry;
		this.videoElementId = videoElementId;
		this.newAnnotationColor = videoModule.getMarkerStyles().get(0);

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
		annotationsDropdown = uifactory.addDropdownSingleselect("annotations", "form.annotation.title",
				formLayout, annotationsKV.keys(), annotationsKV.values());
		annotationsDropdown.addActionListener(FormEvent.ONCHANGE);

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
				.forEach(a -> annotationsKV.add(SelectionValues.entry(a.getId(), a.getText())));
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
				VideoMarker annotation = annotations.getMarkerById(annotationId);
				if (annotation != null) {
					fireEvent(ureq, new AnnotationSelectedEvent(annotation.getId(), annotation.getBegin().getTime()));
					long timeInSeconds = annotation.getBegin().getTime() / 1000;
					SelectTimeCommand selectTimeCommand = new SelectTimeCommand(videoElementId, timeInSeconds);
					getWindowControl().getWindowBackOffice().sendCommandTo(selectTimeCommand);
				}
			}
		}

		super.formInnerEvent(ureq, source, event);
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
		newAnnotation.setStyle(newAnnotationColor);

		annotationId = newAnnotation.getId();
		annotations.getMarkers().add(newAnnotation);
		videoManager.saveMarkers(annotations, repositoryEntry.getOlatResource());
		setValues();
		reloadMarkers(ureq);
		fireEvent(ureq, ANNOTATION_ADDED_EVENT);
	}

	private void doCommands(UserRequest ureq) {
		commandsController = new CommandsController(ureq, getWindowControl());
		listenTo(commandsController);
		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(), commandsController.getInitialComponent(),
				commandsButton.getFormDispatchId(), "", true, "");
		listenTo(ccwc);
		ccwc.activate();
	}

	private void reloadMarkers(UserRequest ureq) {
		fireEvent(ureq, AnnotationsController.RELOAD_MARKERS_EVENT);
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
			if (CommandsController.DELETE_EVENT.getCommand().equals(event.getCommand())) {
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
			annotationsDropdown.select(annotationId, true);
		}
	}

	public String getAnnotationId() {
		return annotationId;
	}

	public void handleDeleted(String annotationId) {
		annotations.getMarkers().removeIf(a -> a.getId().equals(annotationId));
		setAnnotations(annotations);
	}

	private static class CommandsController extends BasicController {
		private static final Event DELETE_EVENT = new Event("delete");
		private final Link deleteLink;

		protected CommandsController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl);

			VelocityContainer mainVC = createVelocityContainer("annotation_commands");

			deleteLink = LinkFactory.createLink("delete", "delete", getTranslator(), mainVC, this,
					Link.LINK);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete");
			mainVC.put("delete", deleteLink);

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if (deleteLink == source) {
				fireEvent(ureq, DELETE_EVENT);
			}
		}
	}
}
