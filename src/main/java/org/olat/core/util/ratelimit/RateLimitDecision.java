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
package org.olat.core.util.ratelimit;

/**
 * Result of one check of the fixed window.
 *
 * @param allowed true if the request is under the limit
 * @param limit The configured number of requests per window
 * @param remaining The requests left in the current window, never negative
 * @param resetEpochSeconds The start of the next window in epoch seconds
 * @param retryAfterSeconds The seconds until the next window, 0 if allowed
 * @param firstRejection true only for the first rejected request of the window
 *
 * Initial date: 23 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public record RateLimitDecision(boolean allowed, int limit, int remaining, long resetEpochSeconds, int retryAfterSeconds,
		boolean firstRejection) {
	//
}
