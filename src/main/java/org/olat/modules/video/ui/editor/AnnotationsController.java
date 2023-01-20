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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoMarker;
import org.olat.modules.video.VideoMarkers;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2022-11-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AnnotationsController extends BasicController {
	public static final Event RELOAD_MARKERS_EVENT = new Event("video.edit.reload.markers");
	private final VelocityContainer mainVC;
	private final RepositoryEntry repositoryEntry;
	private final AnnotationsHeaderController annotationsHeaderController;
	private final AnnotationController annotationController;
	private VideoMarkers annotations;
	private VideoMarker annotation;
	@Autowired
	private VideoManager videoManager;

	public AnnotationsController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
								 String videoElementId) {
		super(ureq, wControl);
		this.repositoryEntry = repositoryEntry;
		mainVC = createVelocityContainer("annotations");

		annotations = videoManager.loadMarkers(repositoryEntry.getOlatResource());
		annotation = annotations.getMarkers().stream().findFirst().orElse(null);

		annotationsHeaderController = new AnnotationsHeaderController(ureq, wControl, repositoryEntry, videoElementId);
		annotationsHeaderController.setAnnotations(annotations);
		listenTo(annotationsHeaderController);
		mainVC.put("header", annotationsHeaderController.getInitialComponent());

		annotationController = new AnnotationController(ureq, wControl, videoElementId, annotation);
		listenTo(annotationController);
		if (annotation != null) {
			mainVC.put("annotation", annotationController.getInitialComponent());
		} else {
			mainVC.remove("annotation");
		}

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (annotationController == source) {
			if (event == Event.DONE_EVENT) {
				annotation = annotationController.getAnnotation();
				videoManager.saveMarkers(annotations, repositoryEntry.getOlatResource());
				annotationsHeaderController.setAnnotations(annotations);
				reloadMarkers(ureq);
			}
		} else if (annotationsHeaderController == source) {
			if (event instanceof AnnotationSelectedEvent annotationSelectedEvent) {
				annotations.getMarkers().stream()
						.filter(a -> a.getId().equals(annotationSelectedEvent.getAnnotationId()))
						.findFirst().ifPresent(a -> {
							annotationController.setAnnotation(a);
							fireEvent(ureq, annotationSelectedEvent);
						});
			} else if (event == AnnotationsHeaderController.ANNOTATION_ADDED_EVENT) {
				this.annotations = annotationsHeaderController.getAnnotations();
				String newAnnotationId = annotationsHeaderController.getAnnotationId();
				annotations.getMarkers().stream().filter(a -> a.getId().equals(newAnnotationId)).findFirst().ifPresent(annotationController::setAnnotation);
				reloadMarkers(ureq);
			}
		}

		super.event(ureq, source, event);
	}

	private void reloadMarkers(UserRequest ureq) {
		fireEvent(ureq, RELOAD_MARKERS_EVENT);
	}

	public void setCurrentTimeCode(String currentTimeCode) {
		annotationsHeaderController.setCurrentTimeCode(currentTimeCode);
	}

	public void showAnnotation(String annotationId) {
		this.annotation = annotations.getMarkerById(annotationId);
		if (annotation != null) {
			annotationsHeaderController.setAnnotationId(annotation.getId());
			annotationController.setAnnotation(annotation);
			mainVC.put("annotation", annotationController.getInitialComponent());
		} else {
			annotationsHeaderController.setAnnotationId(null);
			mainVC.remove("annotation");
		}
	}

	public void handleDeleted(String annotationId) {
		annotationsHeaderController.handleDeleted(annotationId);
		String currentAnnotationId = annotationsHeaderController.getAnnotationId();
		showAnnotation(currentAnnotationId);
	}

	public void setAnnotationSize(String annotationId, double width, double height) {
		if (annotation == null) {
			return;
		}
		if (annotationId.equals(annotation.getId())) {
			VideoMarker videoMarker = annotations.getMarkerById(annotationId);
			videoMarker.setWidth(width);
			videoMarker.setHeight(height);
			videoManager.saveMarkers(annotations, repositoryEntry.getOlatResource());
			annotationController.setAnnotation(annotation);
		}
	}

	public void setAnnotationPosition(String annotationId, double top, double left) {
		if (annotation == null) {
			return;
		}
		if (annotationId.equals(annotation.getId())) {
			VideoMarker videoMarker = annotations.getMarkerById(annotationId);
			videoMarker.setTop(top);
			videoMarker.setLeft(left);
			videoManager.saveMarkers(annotations, repositoryEntry.getOlatResource());
			annotationController.setAnnotation(annotation);
		}
	}
}
