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
import org.olat.core.util.StringHelper;

/**
 * The Class ChapterEditController.
 * Initial Date: 25.10.2016
 * @author fkiefer fabian.kiefer@frentix.com
 * simple Controller to get current time from video resource and pass on content of alterable textfields
 */
public class ChapterEditController extends FormBasicController {

	private String time;
	private String chapter;
	private long durationInSeconds;
	private boolean chapterExists;
	private VideoChapterTableRow videoChapterTableRow;
	private SimpleDateFormat displayDateFormat = new SimpleDateFormat("HH:mm:ss");
	
	private TextElement chapterTitleEl;
	private TextElement beginEl;
	
	private List<VideoChapterTableRow> chapters;
	
	public ChapterEditController(UserRequest ureq, WindowControl wControl, VideoChapterTableRow videoChapterTableRow,
			boolean chapterExists, List<VideoChapterTableRow> chapters, long durationInSeconds) {
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
		this.durationInSeconds = durationInSeconds;
		this.chapter = videoChapterTableRow.getChapterName();
		this.chapterExists = chapterExists;
		
		initForm(ureq);
	}
	
	/**
	 * Gets the video chapter table row.
	 *
	 * @return the video chapter table row
	 */
	public VideoChapterTableRow getVideoChapterTableRow() {
		return videoChapterTableRow;
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
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("submit", "video.chapter." + (chapterExists ? "edit" : "add"), buttonGroupLayout);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		try {
			String chapterTitle = chapterTitleEl.getValue();
			videoChapterTableRow.setChapterName(chapterTitle);
			
			// parse and format because the parse accept such input 00:07:56sfgg and return a correct date
			String beginTime = beginEl.getValue();
			videoChapterTableRow.setBegin(displayDateFormat.parse(beginTime));
			String intervals = displayDateFormat.format(videoChapterTableRow.getBegin());
			videoChapterTableRow.setIntervals(intervals);
		} catch (ParseException e) {
			logWarn("The content of the TextElement cannot be parsed as a Date", e);
		}
		
		fireEvent(ureq, Event.DONE_EVENT);			
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		beginEl.clearError();
		if(StringHelper.containsNonWhitespace(beginEl.getValue())) {
			try {
				Date date = displayDateFormat.parse(beginEl.getValue());
				if (outOfRange(date)){
					beginEl.setErrorKey("chapter.error.out.of.range", null);
					allOk &= false;			
				} else if (timeAlreadyExists(date)) {
					beginEl.setErrorKey("chapter.error.already.exists", null);	
					allOk &= false;		
				}
			} catch (ParseException e) {
				beginEl.setErrorKey("chapter.error.format", null);
				allOk &= false;
			}
		} else {
			beginEl.setErrorKey("chapter.error.format", null);
		}

		chapterTitleEl.clearError();
		if(!StringHelper.containsNonWhitespace(chapterTitleEl.getValue())) {
			chapterTitleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if (chapterNameAlreadyExists(chapterTitleEl.getValue())){
			chapterTitleEl.setErrorKey("chapter.error.name.already.exists", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	/**
	 * Checks if the modification of the video time is not greater than the length of the video.
	 *
	 * @return true, if successful
	 */
	private boolean outOfRange(Date t) {
		if (durationInSeconds > 0) {	
			return t.getTime() > (durationInSeconds * 1000l);
		}
		return false;
	}
	
	/**
	 * Checks if a chapter with the same begin time already exists.
	 *
	 * @return true, if successful
	 */
	private boolean timeAlreadyExists(Date t) {
		if (chapters.size() > 0 && videoChapterTableRow != null) {
			String newTimeFormat = displayDateFormat.format(t);
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
	private boolean chapterNameAlreadyExists(String name) {
		String currentTitle = name.trim().toLowerCase();
		for (VideoChapterTableRow chapterRow : chapters) {
			if (currentTitle.equals(chapterRow.getChapterName().trim().toLowerCase())
					&& !chapterRow.equals(videoChapterTableRow)) {
				return true;
			}
		}
		return false;
	}
}