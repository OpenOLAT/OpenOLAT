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
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

import org.olat.core.commons.services.color.ColorService;
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
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.gta.ui.AVDoneEvent;
import org.olat.modules.video.VideoComment;
import org.olat.modules.video.VideoCommentType;
import org.olat.modules.video.VideoComments;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.model.VideoCommentImpl;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-02-28<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CommentsHeaderController extends FormBasicController {
	public final static Event COMMENT_ADDED_EVENT = new Event("comment.added");
	public final static Event COMMENT_DELETED_EVENT = new Event("comment.deleted");
	private final RepositoryEntry repositoryEntry;
	private VideoComments comments;
	private String commentId;
	private String currentTimeCode;
	private FormLink previousCommentButton;
	private SelectionValues commentsKV = new SelectionValues();
	private SingleSelection commentsDropdown;
	private FormLink nextCommentButton;
	private FormLink addCommentButton;
	private FormLink commandsButton;
	private HeaderCommandsController commandsController;
	private CloseableCalloutWindowController ccwc;
	private AddCommentCalloutController addCommentController;
	private final SimpleDateFormat timeFormat;
	private CloseableModalController cmc;

	@Autowired
	private ColorService colorService;
	private final long videoDurationInSeconds;
	private ImportFileController importFileController;
	private ImportUrlController importUrlController;
	private RecordCommentController recordCommentController;
	@Autowired
	private VideoManager videoManager;
	public CommentsHeaderController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
									long videoDurationInSeconds) {
		super(ureq, wControl, "comments_header");
		this.repositoryEntry = repositoryEntry;
		this.videoDurationInSeconds = videoDurationInSeconds;

		timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		initForm(ureq);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(ccwc);
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(commandsController);
		removeAsListenerAndDispose(addCommentController);
		removeAsListenerAndDispose(importFileController);
		removeAsListenerAndDispose(importUrlController);
		removeAsListenerAndDispose(recordCommentController);
		commandsController = null;
		addCommentController = null;
		importFileController = null;
		importUrlController = null;
		recordCommentController = null;
		ccwc = null;
		cmc = null;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		previousCommentButton = uifactory.addFormLink("previousComment", "", "",
				formLayout, Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
		previousCommentButton.setIconRightCSS("o_icon o_icon_back");
		previousCommentButton.setForceOwnDirtyFormWarning(true);

		commentsDropdown = uifactory.addDropdownSingleselect("comments", "", formLayout,
				commentsKV.keys(), commentsKV.values());
		commentsDropdown.setEscapeHtml(false);
		commentsDropdown.addActionListener(FormEvent.ONCHANGE);

		nextCommentButton = uifactory.addFormLink("nextComment", "", "",
				formLayout, Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
		nextCommentButton.setIconRightCSS("o_icon o_icon_start");
		nextCommentButton.setForceOwnDirtyFormWarning(true);

		addCommentButton = uifactory.addFormLink("addComment", "form.add", "form.add", formLayout, Link.BUTTON);
		addCommentButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		addCommentButton.setIconRightCSS("o_icon o_icon_caret o_video_add_comment");


		commandsButton = uifactory.addFormLink("commands", "", "", formLayout,
				Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
		commandsButton.setIconRightCSS("o_icon o_icon_commands");
	}

	public void setComments(VideoComments comments) {
		this.comments = comments;
		setValues();
	}

	public VideoComments getComments() {
		return comments;
	}

	private void setValues() {
		commentsKV = new SelectionValues();
		comments.getComments()
				.stream()
				.sorted(new CommentComparator())
				.forEach(c -> commentsKV.add(SelectionValues.entry(c.getId(), timeFormat.format(c.getStart()) + " - " + c.getDisplayText(getTranslator()))));
		flc.contextPut("hasComments", !commentsKV.isEmpty());
		commentsDropdown.setKeysAndValues(commentsKV.keys(), commentsKV.values(), null);

		if (comments.getComments().stream().noneMatch(c -> c.getId().equals(commentId))) {
			commentId = null;
		}
		if (commentId == null && !commentsKV.isEmpty()) {
			commentId = commentsKV.keys()[0];
		}
		if (commentId != null) {
			commentsDropdown.select(commentId, true);
		}

		int selectedIndex = -1;
		for (int i = 0; i < commentsKV.size(); i++) {
			if (commentsKV.keys()[i].equals(commentId)) {
				selectedIndex = i;
				break;
			}
		}
		if (selectedIndex != -1) {
			previousCommentButton.setEnabled(selectedIndex > 0);
			nextCommentButton.setEnabled(selectedIndex < (commentsKV.size() - 1));
		}

		commandsButton.setEnabled(!commentsKV.isEmpty());
 	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (addCommentButton == source) {
			doAddComment(ureq);
		} else if (commandsButton == source) {
			doCommands(ureq);
		} else if (commentsDropdown == source) {
			if (commentsDropdown.isOneSelected()) {
				commentId = commentsDropdown.getSelectedKey();
				setValues();
				handleCommentSelected(ureq);
			}
		} else if (nextCommentButton == source) {
			doNextComment(ureq);
		} else if (previousCommentButton == source) {
			doPreviousComment(ureq);
		}

		super.formInnerEvent(ureq, source, event);
	}

	private void handleCommentSelected(UserRequest ureq) {
		getOptionalComment()
				.ifPresent(c -> fireEvent(ureq, new CommentSelectedEvent(c.getId(), c.getStart().getTime())));
	}

	private Optional<VideoComment> getOptionalComment() {
		if (commentId == null) {
			return Optional.empty();
		}
		return comments.getComments().stream().filter(c -> commentId.equals(c.getId())).findFirst();
	}

	private void doPreviousComment(UserRequest ureq) {
		String[] keys = commentsDropdown.getKeys();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			if (commentId != null && commentId.equals(key)) {
				int newIndex = i - 1;
				if (newIndex >= 0) {
					commentId = keys[newIndex];
					setValues();
					handleCommentSelected(ureq);
				}
				break;
			}
		}
	}

	private void doNextComment(UserRequest ureq) {
		String[] keys = commentsDropdown.getKeys();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			if (commentId != null && commentId.equals(key)) {
				int newIndex = i + 1;
				if (newIndex < keys.length) {
					commentId = keys[newIndex];
					setValues();
					handleCommentSelected(ureq);
				}
				break;
			}
		}
	}

	private void doCommands(UserRequest ureq) {
		commandsController = new HeaderCommandsController(ureq, getWindowControl());
		listenTo(commandsController);
		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(), commandsController.getInitialComponent(),
				commandsButton.getFormDispatchId(), "", true, "");
		listenTo(ccwc);
		ccwc.activate();
	}

	private void doAddComment(UserRequest ureq) {
		addCommentController = new AddCommentCalloutController(ureq, getWindowControl());
		listenTo(addCommentController);
		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(), addCommentController.getInitialComponent(),
				addCommentButton.getFormDispatchId(), "", true, "", new CalloutSettings(false));
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
				doDeleteComment(ureq);
			}
			ccwc.deactivate();
			cleanUp();
		} else if (addCommentController == source) {
			ccwc.deactivate();
			cleanUp();
			if (AddCommentCalloutController.TEXT_EVENT == event) {
				doAddText(ureq);
			} else if (AddCommentCalloutController.IMPORT_FILE_EVENT == event) {
				doAddFileComment(ureq);
			} else if (AddCommentCalloutController.IMPORT_URL_EVENT == event) {
				doAddUrlComment(ureq);
			} else if (AddCommentCalloutController.RECORD_VIDEO_EVENT == event) {
				doRecordComment(ureq);
			}
		} else if (cmc == source) {
			cleanUp();
		} else if (importFileController == source) {
			if (event == Event.DONE_EVENT) {
				doAddFile(ureq, importFileController.getFileName(), false);
			}
			cmc.deactivate();
			cleanUp();
		} else if (importUrlController == source) {
			if (event == Event.DONE_EVENT) {
				doAddUrl(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if (recordCommentController == source) {
			if (event instanceof AVDoneEvent avDoneEvent) {
				doAddFile(ureq, avDoneEvent.getRecording().getName(), true);
			}
			cmc.deactivate();
			cleanUp();
		}
	}

	private String wrapInPTag(String text) {
		if (StringHelper.containsNonWhitespace(text)) {
			if (!text.startsWith("<p")) {
				return "<p>" + text + "</p>";
			}
		}
		return text;
	}

	private void doAddText(UserRequest ureq) {
		VideoCommentImpl newComment = createBaseComment();
		newComment.setType(VideoCommentType.text.name());
		newComment.setText(wrapInPTag(translate("comment.add.new")));

		commentId = newComment.getId();
		comments.getComments().add(newComment);
		setValues();
		fireEvent(ureq, COMMENT_ADDED_EVENT);
	}

	private void doAddFile(UserRequest ureq, String fileName, boolean isRecording) {
		VideoCommentImpl newComment = createBaseComment();
		newComment.setType(isRecording ? VideoCommentType.videoRecording.name() : VideoCommentType.videoFile.name());
		newComment.setFileName(fileName);
		newComment.setText(null);

		commentId = newComment.getId();
		comments.getComments().add(newComment);
		setValues();
		fireEvent(ureq, COMMENT_ADDED_EVENT);
	}

	private void doAddUrl(UserRequest ureq) {
		VideoCommentImpl newComment = createBaseComment();
		newComment.setType(VideoCommentType.videoUrl.name());
		newComment.setUrl(importUrlController.getUrl());

		commentId = newComment.getId();
		comments.getComments().add(newComment);
		setValues();
		fireEvent(ureq, COMMENT_ADDED_EVENT);
	}

	private void doAddFileComment(UserRequest ureq) {
		if (guardModalController(importFileController)) {
			return;
		}

		importFileController = new ImportFileController(ureq, getWindowControl(), repositoryEntry.getOlatResource());
		listenTo(importFileController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				importFileController.getInitialComponent(), true, translate("comment.add.import.file"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddUrlComment(UserRequest ureq) {
		if (guardModalController(importUrlController)) {
			return;
		}

		importUrlController = new ImportUrlController(ureq, getWindowControl());
		listenTo(importUrlController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				importUrlController.getInitialComponent(), true, translate("comment.add.import.url"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doRecordComment(UserRequest ureq) {
		if (guardModalController(recordCommentController)) {
			return;
		}

		recordCommentController = new RecordCommentController(ureq, getWindowControl(),
				videoManager.getCommentMediaContainer(repositoryEntry.getOlatResource()));
		listenTo(recordCommentController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				recordCommentController.getInitialComponent(), true,
				translate("comment.add.record.video"));
		listenTo(cmc);
		cmc.activate();
	}

	private VideoCommentImpl createBaseComment() {
		VideoCommentImpl comment = new VideoCommentImpl();
		comment.setId(UUID.randomUUID().toString());
		comment.setAuthor(getIdentity() != null ? getIdentity().getName() : "-");
		comment.setColor(colorService.getColors().get(0));
		if (currentTimeCode != null) {
			long timeInSeconds = Math.round(Double.parseDouble(currentTimeCode));
			Set<Long> usedTimes = comments.getComments().stream().map(c -> c.getStart().getTime() / 1000).collect(Collectors.toSet());
			long nearestSecond = HeaderHelper.findNearestSecondWithoutEvent(timeInSeconds, videoDurationInSeconds, usedTimes);
			comment.setStart(new Date(nearestSecond * 1000));
		} else {
			comment.setStart(new Date(0));
		}
		return comment;
	}

	private void doDeleteComment(UserRequest ureq) {
		if (commentId != null) {
			getOptionalComment().ifPresent(c -> {
				comments.getComments().remove(c);
				if (comments.getComments().isEmpty()) {
					commentId = null;
				} else {
					commentId = comments.getComments().get(0).getId();
				}
				setValues();
				fireEvent(ureq, COMMENT_DELETED_EVENT);
			});
		}
	}

	public void setCurrentTimeCode(String currentTimeCode) {
		this.currentTimeCode = currentTimeCode;
	}

	public void setCommentId(String commentId) {
		this.commentId = commentId;
		if (commentId != null) {
			setValues();
		}
	}

	public String getCommentId() {
		return commentId;
	}

	public void handleDeleted(String commentId) {
		comments.getComments().removeIf(c -> c.getId().equals(commentId));
		setComments(comments);
	}
}

