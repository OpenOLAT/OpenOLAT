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
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.AbstractFlexiTableRenderer;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.video.VideoComment;
import org.olat.modules.video.VideoComments;
import org.olat.modules.video.VideoManager;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-02-28<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CommentsController extends BasicController {
	public static final Event RELOAD_COMMENTS_EVENT = new Event("video.edit.reload.comments");
	private final VelocityContainer mainVC;
	private final RepositoryEntry repositoryEntry;
	private final CommentsHeaderController commentsHeaderController;
	private final CommentController commentController;
	private VideoComment comment;
	private VideoComments comments;
	@Autowired
	private VideoManager videoManager;


	public CommentsController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
							  long videoDurationInSeconds) {
		super(ureq, wControl);
		this.repositoryEntry = repositoryEntry;
		mainVC = createVelocityContainer("comments");

		comments = videoManager.loadComments(repositoryEntry.getOlatResource());
		comments.getComments().sort(new CommentComparator());
		comment = comments.getComments().stream().min(new CommentComparator()).orElse(null);

		commentsHeaderController = new CommentsHeaderController(ureq, wControl, repositoryEntry,
				videoDurationInSeconds);
		commentsHeaderController.setComments(comments);
		listenTo(commentsHeaderController);
		mainVC.put("header", commentsHeaderController.getInitialComponent());

		commentController = new CommentController(ureq, wControl, repositoryEntry, comment, comments,
				videoDurationInSeconds);
		listenTo(commentController);
		if (comment != null) {
			mainVC.put("comment", commentController.getInitialComponent());
		} else {
			mainVC.remove("comment");
		}

		Translator tableTranslator = Util.createPackageTranslator(AbstractFlexiTableRenderer.class, ureq.getLocale());
		EmptyStateConfig emptyStateConfig = EmptyStateConfig
				.builder()
				.withIconCss("o_icon_empty_objects")
				.withIndicatorIconCss("o_icon_empty_indicator")
				.withMessageTranslated(tableTranslator.translate("default.tableEmptyMessage"))
				.build();
		EmptyStateFactory.create("emptyState", mainVC, this, emptyStateConfig);

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (commentController == source) {
			if (event == Event.DONE_EVENT) {
				comment = commentController.getComment();
				videoManager.saveComments(comments, repositoryEntry.getOlatResource());
				commentsHeaderController.setComments(comments);
				reloadComments(ureq);
				fireEvent(ureq, new CommentSelectedEvent(comment.getId(), comment.getStart().getTime()));
			}
		} else if (commentsHeaderController == source) {
			if (event instanceof CommentSelectedEvent commentSelectedEvent) {
				comments.getComments().stream()
						.filter(c -> c.getId().equals(commentSelectedEvent.getId()))
						.findFirst().ifPresent(c -> {
							commentController.setComment(c);
							fireEvent(ureq, commentSelectedEvent);
						});
			} else if (event == CommentsHeaderController.COMMENT_ADDED_EVENT ||
					event == CommentsHeaderController.COMMENT_DELETED_EVENT) {
				this.comments = commentsHeaderController.getComments();
				String newCommentId = commentsHeaderController.getCommentId();
				showComment(newCommentId);
				commentController.setComment(comment);
				videoManager.saveComments(comments, repositoryEntry.getOlatResource());
				if (event == CommentsHeaderController.COMMENT_DELETED_EVENT) {
					videoManager.deleteUnusedCommentFiles(comments, repositoryEntry.getOlatResource());
				}
				reloadComments(ureq);
				if (comment != null) {
					fireEvent(ureq, new CommentSelectedEvent(comment.getId(), comment.getStart().getTime()));
				}
			}
		}

		super.event(ureq, source, event);
	}

	private void reloadComments(UserRequest ureq) {
		fireEvent(ureq, RELOAD_COMMENTS_EVENT);
	}

	public void setCurrentTimeCode(String currentTimeCode) {
		commentsHeaderController.setCurrentTimeCode(currentTimeCode);
		commentController.setCurrentTimeCode(currentTimeCode);
	}

	public void showComment(String commentId) {
		this.comment = comments.getComments().stream().filter(c -> c.getId().equals(commentId)).findFirst()
				.orElse(null);
		if (comment != null) {
			commentsHeaderController.setCommentId(comment.getId());
			commentController.setComment(comment);
			mainVC.put("comment", commentController.getInitialComponent());
		} else {
			commentsHeaderController.setCommentId(null);
			mainVC.remove("comment");
		}
	}

	public void handleDeleted(String commentId) {
		commentsHeaderController.handleDeleted(commentId);
		String currentCommentId = commentsHeaderController.getCommentId();
		showComment(currentCommentId);
	}

	public void sendSelectionEvent(UserRequest ureq) {
		if (comment != null) {
			fireEvent(ureq, new CommentSelectedEvent(comment.getId(), comment.getStart().getTime()));
		}
	}
}
