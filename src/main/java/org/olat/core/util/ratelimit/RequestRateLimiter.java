/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.util.ratelimit;

/**
 * 
 * Initial date: 23 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public interface RequestRateLimiter {
	
	/**
	 * Takes a slot for a parallel request. If the method returns true, the
	 * caller must call {@link #release(String)} in a finally block.
	 *
	 * @param key The key of the subject
	 * @param maxParallel The number of allowed parallel requests
	 * @return true if the slot was taken
	 */
	boolean acquire(String key, int maxParallel);
	
	/**
	 * Counts the request in the current window of the key. A rejected
	 * request counts too.
	 *
	 * @param key The key of the subject, e.g. "rest:id:123"
	 * @param limitPerMinute The number of allowed requests per window
	 * @return The decision, never null
	 */
	RateLimitDecision check(String key, int limitPerMinute);
	
	/**
	 * Releases a slot. The key is removed if no request is in flight
	 * to prevent the map to grow with the number of subjects.
	 *
	 * @param key The key of the subject
	 */
	void release(String key);

}
