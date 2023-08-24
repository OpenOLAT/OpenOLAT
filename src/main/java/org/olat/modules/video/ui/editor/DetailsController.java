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
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 2022-11-18<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class DetailsController extends BasicController {

	private final TabbedPane tabbedPane;
	private final ChaptersController chaptersController;
	private final AnnotationsController annotationsController;
	private final SegmentsController segmentsController;
	private final CommentsController commentsController;
	private final QuestionsController questionsController;

	public DetailsController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
							 long durationInSeconds, String videoElementId, boolean restrictedEdit) {
		super(ureq, wControl);

		VelocityContainer mainVC = createVelocityContainer("details");

		tabbedPane = new TabbedPane("tabbedPane", getLocale());
		tabbedPane.addListener(this);

		chaptersController = new ChaptersController(ureq, wControl, repositoryEntry, durationInSeconds);
		listenTo(chaptersController);
		tabbedPane.addTab(translate("video.editor.panes.chapters"), chaptersController);

		annotationsController = new AnnotationsController(ureq, wControl, repositoryEntry, durationInSeconds,
				videoElementId);
		listenTo(annotationsController);
		tabbedPane.addTab(translate("video.editor.panes.annotations"), annotationsController);

		segmentsController = new SegmentsController(ureq, wControl, repositoryEntry, durationInSeconds, videoElementId, restrictedEdit);
		listenTo(segmentsController);
		tabbedPane.addTab(translate("video.editor.panes.segments"), segmentsController);

		commentsController = new CommentsController(ureq, wControl, repositoryEntry, durationInSeconds, videoElementId);
		listenTo(commentsController);
		tabbedPane.addTab(translate("video.editor.panes.comments"), commentsController);

		questionsController = new QuestionsController(ureq, wControl, repositoryEntry, durationInSeconds, videoElementId);
		listenTo(questionsController);
		tabbedPane.addTab(translate("video.editor.panes.quiz"), questionsController);

		mainVC.put("tabbedPane", tabbedPane);

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (tabbedPane == source) {
			if (event instanceof TabbedPaneChangedEvent tabbedPaneChangedEvent) {
				if (annotationsController == tabbedPaneChangedEvent.getNewController()) {
					annotationsController.sendSelectionEvent(ureq);
				} else if (questionsController == tabbedPaneChangedEvent.getNewController()) {
					questionsController.sendSelectionEvent(ureq);
				} else if (segmentsController == tabbedPaneChangedEvent.getNewController()) {
					segmentsController.sendSelectionEvent(ureq);
				} else if (commentsController == tabbedPaneChangedEvent.getNewController()) {
					commentsController.sendSelectionEvent(ureq);
				} else if (chaptersController == tabbedPaneChangedEvent.getNewController()) {
					chaptersController.sendSelectionEvent(ureq);
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source instanceof AnnotationsController) {
			fireEvent(ureq, event);
		} else if (source instanceof ChaptersController) {
			fireEvent(ureq, event);
		} else if (source instanceof SegmentsController) {
			fireEvent(ureq, event);
		} else if (source instanceof QuestionsController) {
			fireEvent(ureq, event);
		} else if (source instanceof CommentsController) {
			fireEvent(ureq, event);
		}
	}

	public void setCurrentTimeCode(String currentTimeCode) {
		chaptersController.setCurrentTimeCode(currentTimeCode);
		annotationsController.setCurrentTimeCode(currentTimeCode);
		questionsController.setCurrentTimeCode(currentTimeCode);
		segmentsController.setCurrentTimeCode(currentTimeCode);
		commentsController.setCurrentTimeCode(currentTimeCode);
	}

	public void showAnnotation(UserRequest ureq, String annotationId) {
		tabbedPane.setSelectedPane(ureq, 1);
		annotationsController.showAnnotation(annotationId);
	}

	public void showChapters(UserRequest ureq) {
		tabbedPane.setSelectedPane(ureq, 0);
	}

	public void showSegment(UserRequest ureq, String segmentId) {
		tabbedPane.setSelectedPane(ureq, 2);
		segmentsController.showSegment(segmentId);
	}

	public void showComment(UserRequest ureq, String commentId) {
		tabbedPane.setSelectedPane(ureq, 3);
		commentsController.showComment(commentId);
	}

	public void showQuestion(UserRequest ureq, String questionId) {
		tabbedPane.setSelectedPane(ureq, 4);
		questionsController.showQuestion(questionId);
	}

	public void updateQuestion(String questionId) {
		questionsController.updateQuestion(questionId);
	}

	public void setAnnotationSize(String annotationId, double width, double height) {
		annotationsController.setAnnotationSize(annotationId, width, height);
	}

	public void setAnnotationPosition(String annotationId, double top, double left) {
		annotationsController.setAnnotationPosition(annotationId, top, left);
	}

	public void handleDeleted(TimelineEventType type, String id) {
		switch (type) {
			case CHAPTER -> chaptersController.handleDeleted();
			case ANNOTATION -> annotationsController.handleDeleted(id);
			case SEGMENT -> segmentsController.handleDeleted(id);
			case COMMENT -> commentsController.handleDeleted(id);
			case QUIZ -> questionsController.handleDeleted(id);
		}
	}
}
