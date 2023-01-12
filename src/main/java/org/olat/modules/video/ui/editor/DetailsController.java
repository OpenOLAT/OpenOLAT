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

	private final String videoElementId;
	private final TabbedPane tabbedPane;
	private final ChaptersController chaptersController;
	private final AnnotationsController annotationsController;
	private final SegmentController segmentController;
	private final QuizController quizController;

	public DetailsController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
							 String videoElementId, long durationInSeconds) {
		super(ureq, wControl);

		this.videoElementId = videoElementId;

		VelocityContainer mainVC = createVelocityContainer("details");

		tabbedPane = new TabbedPane("tabbedPane", getLocale());
		tabbedPane.addListener(this);

		chaptersController = new ChaptersController(ureq, wControl, repositoryEntry, durationInSeconds);
		listenTo(chaptersController);
		tabbedPane.addTab(translate("video.editor.panes.chapters"), chaptersController);

		annotationsController = new AnnotationsController(ureq, wControl, repositoryEntry, videoElementId);
		listenTo(annotationsController);
		tabbedPane.addTab(translate("video.editor.panes.annotations"), annotationsController);

		segmentController = new SegmentController(ureq, wControl, repositoryEntry, videoElementId);
		listenTo(segmentController);
		tabbedPane.addTab(translate("video.editor.panes.segments"), segmentController);

		quizController = new QuizController(ureq, wControl, repositoryEntry, videoElementId);
		listenTo(quizController);
		tabbedPane.addTab(translate("video.editor.panes.quiz"), quizController);

		mainVC.put("tabbedPane", tabbedPane);

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source instanceof AnnotationsController) {
			fireEvent(ureq, event);
		} else if (source instanceof ChaptersController) {
			fireEvent(ureq, event);
		} else if (source instanceof SegmentController) {
			fireEvent(ureq, event);
		} else if (source instanceof QuizController) {
			fireEvent(ureq, event);
		}
	}

	public void setCurrentTimeCode(String currentTimeCode) {
		annotationsController.setCurrentTimeCode(currentTimeCode);
		quizController.setCurrentTimeCode(currentTimeCode);
	}

	public void setAnnotationId(String annotationId) {
		annotationsController.setAnnotationId(annotationId);
	}

	public void showAnnotations(UserRequest ureq) {
		tabbedPane.setSelectedPane(ureq, 1);
	}

	public void showChapters(UserRequest ureq) {
		tabbedPane.setSelectedPane(ureq, 0);
	}

	public void showSegments(UserRequest ureq) {
		tabbedPane.setSelectedPane(ureq, 2);
	}

	public void showQuiz(UserRequest ureq) {
		tabbedPane.setSelectedPane(ureq, 3);
	}

	public void updateQuestion(String questionId) {
		quizController.updateQuestion(questionId);
	}
}
