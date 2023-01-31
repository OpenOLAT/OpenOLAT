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
import java.util.TimeZone;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.ui.event.VideoEvent;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 27 nov. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoHelper {
	
	private static final Logger log = Tracing.createLoggerFor(VideoHelper.class);
	private static final SimpleDateFormat displayDateFormat = new SimpleDateFormat("HH:mm:ss");
	static {
		displayDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	
	public static long durationInSeconds(RepositoryEntry entry, VideoDisplayController videoDisplayCtr) {
		String duration = entry.getExpenditureOfWork();
		if (!StringHelper.containsNonWhitespace(duration)) {
			VideoMeta metadata = videoDisplayCtr.getVideoMetadata();
			if(metadata != null) {
				duration = metadata.getLength();
			}
		}
		return durationInSeconds(duration);
	}
	
	public static long durationInSeconds(String duration) {
		long durationInSeconds = -1l;
		if(StringHelper.containsNonWhitespace(duration)) {
			try {
				if(duration.indexOf(':') == duration.lastIndexOf(':')) {
					duration = "00:" + duration;
				}
				durationInSeconds = parseTimeToSeconds(duration);
			} catch (Exception e) {
				log.warn("Cannot parse expenditure of work: " + duration, e);
			}
		}
		return durationInSeconds;
	}
	
	public static long durationInSeconds(VideoEvent videoEvent) {
		long durationInSeconds = -1l;
		if(StringHelper.containsNonWhitespace(videoEvent.getDuration()) && !"NaN".equals(videoEvent.getDuration())) {
			try {
				durationInSeconds = Math.round(Double.parseDouble(videoEvent.getDuration()));
			} catch (NumberFormatException e) {
				//don't panic
			}
		}
		return durationInSeconds;
	}
	
	public static synchronized String formatTime(Date date) {
		return displayDateFormat.format(date);
	}
	
	public static synchronized long parseTimeToSeconds(String duration) throws ParseException {
		return displayDateFormat.parse(duration).getTime() / 1000l;
	}

}
