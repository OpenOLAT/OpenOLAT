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

import java.util.Set;

/**
 * Initial date: 2023-08-22<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class HeaderHelper {

	/**
	 * Finds the nearest second within a video timeline that does not have an event, starting from a specified time.
	 * The method searches forward and backward from the given time, respecting certain increments based on video 
	 * duration.
	 *
	 * @param timeInSeconds The starting point in seconds within the video timeline to begin the search.
	 * @param videoDurationInSeconds The total duration of the video in seconds, which determines the search granularity.
	 * @param usedTimes A set of seconds in the video timeline that are already associated with events and should be skipped.
	 * @return The nearest second without an event. If no such second is found, the input starting time is returned.
	 */
	public static long findNearestSecondWithoutEvent(long timeInSeconds, long videoDurationInSeconds, Set<Long> usedTimes) {

		if (videoDurationInSeconds >= 600) {
			for (long t = timeInSeconds; t < videoDurationInSeconds; t += 30) {
				if (!usedTimes.contains(t)) {
					return t;
				}
			}
		}

		if (videoDurationInSeconds >= 60) {
			for (long t = timeInSeconds; t < videoDurationInSeconds; t += 5) {
				if (!usedTimes.contains(t)) {
					return t;
				}
			}
		}

		for (long t = timeInSeconds; t < videoDurationInSeconds; t++) {
			if (!usedTimes.contains(t)) {
				return t;
			}
		}

		for (long t = timeInSeconds; t >= 0; t--) {
			if (!usedTimes.contains(t)) {
				return t;
			}
		}

		return timeInSeconds;
	}
}
