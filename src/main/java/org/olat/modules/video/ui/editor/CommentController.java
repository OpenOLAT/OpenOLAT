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
import java.util.List;
import java.util.TimeZone;

import org.olat.core.commons.services.color.ColorService;
import org.olat.core.commons.services.color.ColorUIFactory;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.commons.services.video.ui.VideoAudioPlayerController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.video.VideoComment;
import org.olat.modules.video.VideoCommentType;
import org.olat.modules.video.VideoComments;
import org.olat.modules.video.VideoManager;
import org.olat.repository.RepositoryEntry;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-02-28<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CommentController extends FormBasicController {
	private final long videoDurationInSeconds;
	private final VideoComments comments;
	private final RepositoryEntry repositoryEntry;
	private final String videoElementId;
	private VideoComment comment;
	private TextElement startEl;
	private ColorPickerElement colorPickerEl;
	private RichTextElement textEl;
	private FormLink videoLink;

	private final SimpleDateFormat timeFormat;
	@Autowired
	private ColorService colorService;
	private CloseableModalController cmc;
	VideoAudioPlayerController videoAudioPlayerController;
	@Autowired
	private VideoManager videoManager;

	public CommentController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry,
							 VideoComment comment, VideoComments comments, long videoDurationInSeconds,
							 String videoElementId) {
		super(ureq, wControl, "comment");
		this.repositoryEntry = repositoryEntry;
		this.comment = comment;
		this.comments = comments;
		this.videoDurationInSeconds = videoDurationInSeconds;
		this.videoElementId = videoElementId;

		timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		initForm(ureq);
		setValues();
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(videoAudioPlayerController);
		cmc = null;
		videoAudioPlayerController = null;
	}

	public void setComment(VideoComment comment) {
		this.comment = comment;
		setValues();
	}

	public VideoComment getComment() {
		return comment;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		startEl = uifactory.addTextElement("start", "form.common.start", 8,
				"00:00:00", formLayout);
		startEl.setMandatory(true);

		ApplyPositionButtonController startApplyPositionButtonController = new ApplyPositionButtonController(ureq,
				getWindowControl(), startEl.getFormDispatchId(), videoElementId, mainForm.getDispatchFieldId(), false);
		flc.put("startApplyPosition", startApplyPositionButtonController.getInitialComponent());

		List<String> colorNames = colorService.getColors();
		List<ColorPickerElement.Color> colors = ColorUIFactory.createColors(colorNames, getLocale());

		colorPickerEl = uifactory.addColorPickerElement("color", "form.common.color", formLayout, colors);

		videoLink = uifactory.addFormLink("video", "", "form.common.video", formLayout,
				Link.LINK | Link.NONTRANSLATED);

		textEl = uifactory.addRichTextElementVeryMinimalistic("text", "form.common.text",
				"", 3, -1, true, null, formLayout,
				ureq.getUserSession(), getWindowControl());
		textEl.getEditorConfiguration().disableImageAndMovie();
		textEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.oneLine);

		uifactory.addFormSubmitButton("save", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	private void setValues() {
		if (comment == null) {
			return;
		}

		startEl.setValue(timeFormat.format(comment.getStart()));

		flc.contextPut("showText", true);
		flc.contextPut("showVideo", false);
		textEl.setMandatory(false);

		if (StringHelper.containsNonWhitespace(comment.getText())) {
			textEl.setValue(comment.getText());
		} else {
			textEl.setValue("");
		}

		if (StringHelper.containsNonWhitespace(comment.getFileName())) {
			if (VideoCommentType.safeValueOf(comment.getType()).equals(VideoCommentType.videoRecording)) {
				videoLink.setI18nKey(translate("comment.video.recording"));
			} else {
				videoLink.setI18nKey(comment.getFileName());
			}
			flc.contextPut("showVideo", true);
		} else if (StringHelper.containsNonWhitespace(comment.getUrl())) {
			videoLink.setI18nKey(comment.getUrl());
			flc.contextPut("showVideo", true);
		} else {
			videoLink.setI18nKey("");
			textEl.setMandatory(true);
		}

		colorPickerEl.setColor(comment.getColor());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (videoLink == source) {
			doShowVideo(ureq);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= validateTime(startEl);

		return allOk;
	}

	private boolean validateTime(TextElement timeEl) {
		boolean allOk = true;
		timeEl.clearError();
		if (!StringHelper.containsNonWhitespace(timeEl.getValue())) {
			timeEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		} else {
			try {
				long timeInSeconds = timeFormat.parse(timeEl.getValue()).getTime() / 1000;
				if (timeInSeconds < 0 || (videoDurationInSeconds > 0 && timeInSeconds > videoDurationInSeconds)) {
					timeEl.setErrorKey("form.error.timeNotValid");
					allOk = false;
				}
				allOk &= validateOnlyComment(timeEl);
			} catch (Exception e) {
				timeEl.setErrorKey("form.error.timeFormat");
				allOk = false;
			}
		}
		return allOk;
	}

	private boolean validateOnlyComment(TextElement timeEl) throws ParseException {
		if (comment == null) {
			return true;
		}

		long timeInSeconds = timeFormat.parse(timeEl.getValue()).getTime() / 1000;
		boolean allOk = comments.getComments().stream()
				.noneMatch(c -> !c.getId().equals(comment.getId()) && (c.getStart().getTime() / 1000) == timeInSeconds);

		if (!allOk) {
			timeEl.setErrorKey("form.error.anotherCommentExists");
		}
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (comment == null) {
			return;
		}

		try {
			comment.setStart(timeFormat.parse(startEl.getValue()));
			comment.setColor(colorPickerEl.getColor().id());
			comment.setText(textEl.getValue());
			fireEvent(ureq, Event.DONE_EVENT);
		} catch (ParseException e) {
			logError("", e);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (videoAudioPlayerController == source) {
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void doShowVideo(UserRequest ureq) {
		if (comment == null) {
			return;
		}

		if (StringHelper.containsNonWhitespace(comment.getFileName())) {
			VFSContainer masterContainer = videoManager.getCommentMediaContainer(repositoryEntry.getOlatResource());
			VFSItem item = masterContainer.resolve(comment.getFileName());
			if (item instanceof VFSLeaf vfsLeaf) {
				boolean inTranscoding = item.canMeta() == VFSConstants.YES && item.getMetaInfo() != null &&
						item.getMetaInfo().isInTranscoding();
				if (inTranscoding) {
					item = masterContainer.resolve(VFSTranscodingService.masterFilePrefix + comment.getFileName());
					if (item instanceof VFSLeaf) {
						vfsLeaf = (VFSLeaf) item;
					}
				}
				videoAudioPlayerController = new VideoAudioPlayerController(ureq, getWindowControl(),
						vfsLeaf, null, true, false);
				listenTo(videoAudioPlayerController);

				cmc = new CloseableModalController(getWindowControl(), translate("close"),
						videoAudioPlayerController.getInitialComponent(), true,
						translate("form.common.video"));
				listenTo(cmc);
				cmc.activate();
			}
		} else if (StringHelper.containsNonWhitespace(comment.getUrl())) {
			VideoAudioPlayerController videoAudioPlayerController = new VideoAudioPlayerController(ureq,
					getWindowControl(), null, comment.getUrl(), true, false);
			listenTo(videoAudioPlayerController);

			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					videoAudioPlayerController.getInitialComponent(), true,
					translate("form.common.video"));
			listenTo(cmc);
			cmc.activate();
		}
	}
}
