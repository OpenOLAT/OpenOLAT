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
package org.olat.modules.video.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * The Class ChapterEditController.
 * Initial Date: 25.10.2016
 * @author fkiefer fabian.kiefer@frentix.com
 * simple Controller to get current time from video resource and pass on content of alterable textfields
 */
public class ChapterEditController extends FormBasicController {

	private static final OLog log = Tracing.createLoggerFor(ChapterEditController.class);

	private String time;
	private String chapter;
	private boolean chapterExists;
	private VideoChapterTableRow videoChapterTableRow;
	private SimpleDateFormat displayDateFormat = new SimpleDateFormat("HH:mm:ss");
	
	private TextElement chapterTitleEl;
	private TextElement beginEl;
	
	private List<VideoChapterTableRow> chapters;
	private String duration;
	
	public ChapterEditController(UserRequest ureq, WindowControl wControl, VideoChapterTableRow videoChapterTableRow,
			boolean chapterExists, List<VideoChapterTableRow> chapters, String duration) {
		super(ureq, wControl);
		displayDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		try {
			Date begin = new Date();
			// incoming time is second, but millis required
			begin.setTime((long) Float.parseFloat(videoChapterTableRow.getIntervals()) * 1000);
			this.time = displayDateFormat.format(begin);
		} catch (Exception e) {
			this.time = videoChapterTableRow.getIntervals();
		}
		this.videoChapterTableRow = videoChapterTableRow;
		this.chapters = chapters;
		this.duration = duration;
		this.chapter = videoChapterTableRow.getChapterName();
		this.chapterExists = chapterExists;
		
		initForm(ureq);
	}
	
	@Override
	protected void doDispose() {
		// only formInnerEvent()
	}


	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		chapterTitleEl = uifactory.addTextElement("title", "video.chapter.name", 50, "", formLayout);
		chapterTitleEl.setValue(chapter);
		chapterTitleEl.setMandatory(true);
		chapterTitleEl.setNotEmptyCheck("chapter.error.notitle");
	
		beginEl = uifactory.addTextElement("begin","video.chapter.from", 10, "",formLayout);
		beginEl.setValue(time);
		beginEl.setMandatory(true);
		beginEl.setNotEmptyCheck("chapter.error.notime");
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonGroupLayout);
		uifactory.addFormSubmitButton("submit", "video.chapter." + (chapterExists ? "edit" : "add"), buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
	}

	/**
	 * Checks if the modification of the video time is not greater than the length of the video.
	 *
	 * @return true, if successful
	 */
	private boolean outOfRange() {
		if (duration != null) {
			long durationLong = (long) Float.parseFloat(duration) * 1000;
			Date durationDate = new Date(durationLong);			
			return videoChapterTableRow.getBegin().after(durationDate);
		} else {
			return false;			
		}
	}
	
	/**
	 * Checks if a chapter with the same begin time already exists.
	 *
	 * @return true, if successful
	 */
	private boolean timeAlreadyExists() {
		if (chapters.size() > 0 && videoChapterTableRow != null) {
			String newTimeFormat = displayDateFormat.format(videoChapterTableRow.getBegin());
			for (VideoChapterTableRow chapterRow : chapters) {
				String beginFormat = displayDateFormat.format(chapterRow.getBegin());
				if (beginFormat.equals(newTimeFormat) && !chapterRow.equals(videoChapterTableRow)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Check if Chapter name already exists.
	 *
	 * @return true, if successful
	 */
	private boolean chapterNameAlreadyExists(){
		for (VideoChapterTableRow chapterRow : chapters) {
			String currentTitle = chapterTitleEl.getValue().trim().toLowerCase();
			if (currentTitle.equals(chapterRow.getChapterName().trim().toLowerCase())
					&& !chapterRow.equals(videoChapterTableRow)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		if (setTextElementValuesAndCheckFormat()) {
			beginEl.setErrorKey("chapter.error.format", null);	
		} else if (chapterNameAlreadyExists()){
			chapterTitleEl.setErrorKey("chapter.error.name.already.exists", null);
		} else if (outOfRange()){
			beginEl.setErrorKey("chapter.error.out.of.range", null);			
		} else if (timeAlreadyExists()) {
			beginEl.setErrorKey("chapter.error.already.exists", null);			
		} else {
			this.fireEvent(ureq, Event.DONE_EVENT);			
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	/**
	 * Gets the video chapter table row.
	 *
	 * @return the video chapter table row
	 */
	public VideoChapterTableRow getVideoChapterTableRow() {
		setTextElementValuesAndCheckFormat();
		return videoChapterTableRow;
	}
	
	/**
	 * Sets the text element values and check format.
	 * only alter table if format is correct 
	 * @return true, if successful
	 */
	private boolean setTextElementValuesAndCheckFormat (){
		boolean incorrectTimeFormat = false;
		String time = beginEl.getValue();
		String chapterTitle = chapterTitleEl.getValue();
		try {
			videoChapterTableRow.setBegin(displayDateFormat.parse(time));
			videoChapterTableRow.setChapterName(chapterTitle);		
			videoChapterTableRow.setIntervals(time);
		} catch (ParseException e) {
			log.error("The content of the TextElement cannot be parsed as a Date", e);
			incorrectTimeFormat = true;
		}
		return incorrectTimeFormat;
	}
	
	
	
	
	

}
