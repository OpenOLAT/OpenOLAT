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
package org.olat.modules.zoom;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public class ZoomCalendarEvent {

    private static final Logger log = Tracing.createLoggerFor(ZoomCalendarEvent.class);

    static final String ZOOM_DESCRIPTION_REGEX = "https://[a-z0-9.]*/j/([0-9]*)";
    static final Pattern ZOOM_DESCRIPTION_PATTERN = Pattern.compile(ZOOM_DESCRIPTION_REGEX);

    String name;
    String description;
    String contextId;
    Long timeStart;
    Long timeDuration;
    String meetingId;

    public ZoomCalendarEvent(HttpServletRequest request, int index) {
        String prefix = "events[" + index + "]";

        String name = request.getParameter(prefix + "[name]");
        String description = request.getParameter(prefix + "[description]");
        String contextId = request.getParameter(prefix + "[courseid]");
        String timeStart = request.getParameter(prefix + "[timestart]");
        String timeDuration = request.getParameter(prefix + "[timeduration]");
        init(name, description, contextId, timeStart, timeDuration);
    }

    ZoomCalendarEvent() {
        //
    }

    void init(String name, String description, String contextId, String timeStart, String timeDuration) {
        this.name = name;
        this.description = description;
        this.contextId = contextId;
        try {
            this.timeStart = Long.parseLong(timeStart);
            this.timeDuration = Long.parseLong(timeDuration);
        } catch (RuntimeException e) {
            log.warn("Parsing error in Zoom calendar event.");
        }
        Matcher meetingIdMatcher = ZOOM_DESCRIPTION_PATTERN.matcher(description);
        if (meetingIdMatcher.find()) {
            meetingId = meetingIdMatcher.group(1);
        }
    }

    public Date getStartDate() {
        return new Date(timeStart * 1000);
    }

    public Date getEndDate() {
        return new Date((timeStart + timeDuration) * 1000);
    }

    public String getMeetingId() {
        return meetingId;
    }
}
