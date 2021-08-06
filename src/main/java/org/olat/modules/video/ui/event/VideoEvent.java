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
package org.olat.modules.video.ui.event;

import org.olat.core.gui.control.Event;

/**
 * Event fired when the user interacts with the movie
 * Initial date: 22.04.2016<br>
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
public class VideoEvent extends Event {
	private static final long serialVersionUID = 5180562757687791093L;
	
	public static final String PLAY = "play";
	public static final String PAUSE = "pause";
	public static final String SEEKED = "seeked";
	public static final String ENDED = "ended";
	public static final String TIMEUPDATE = "timeupdate";
	public static final String PROGRESS = "progress";

	private final String timeCode;
	private final String duration;


	public VideoEvent(String command, String timeCode, String duration) {
		super(command);
		this.timeCode = timeCode;
		this.duration = duration;
	}

	/**
	 * @return The timecode for this event or NULL if not available
	 */
	public String getTimeCode() {
		return timeCode;
	}
	
	/**
	 * @return The duration for this event or NULL if not available
	 */
	public String getDuration() {
		return duration;
	}
	
	/**
	 * @return The position of this event as percentage in relation of the duration of the entire movie (0-1)
	 */
	public double getProgress() {
		double percent = 0d;
		try {
			double time = Double.parseDouble(timeCode);
			double dur = Double.parseDouble(duration);
			if (dur != 0d && time <= dur) {
				percent = (float)(time / dur);				
			}
		} catch(NumberFormatException e) {
			// 
		}
		return percent;
	}
	
}
