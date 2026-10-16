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
package org.olat.modules.scorm.server.servermodels;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the calculation of the SCORM total time.
 *
 * Initial date: 9 oct. 2026<br>
 *
 */
public class ScoUtilsTest {

	private static final int MAX_SESSION_SECONDS = 24 * 60 * 60;

	@Test
	public void sessionTimeIsAddedOnce() {
		// the total time of the browser (launch + elapsed) must not matter, only launch + session time
		String total = ScoUtils.addCappedSessionTime("00:10:00", "00:30:00", MAX_SESSION_SECONDS);
		Assert.assertEquals("00:40:00", total);
	}

	@Test
	public void repeatedCommitsOfTheSameSessionAreIdempotent() {
		String launch = "00:10:00";
		String first = ScoUtils.addCappedSessionTime(launch, "0000:00:20.0", MAX_SESSION_SECONDS);
		String second = ScoUtils.addCappedSessionTime(launch, "0000:00:40.0", MAX_SESSION_SECONDS);
		String third = ScoUtils.addCappedSessionTime(launch, "0000:00:40.0", MAX_SESSION_SECONDS);
		Assert.assertEquals("00:10:20", first);
		Assert.assertEquals("00:10:40", second);
		Assert.assertEquals(second, third);
	}

	@Test
	public void rusticiSessionTimeFormat() {
		String total = ScoUtils.addCappedSessionTime("0000:00:00.00", "0000:03:00.0", MAX_SESSION_SECONDS);
		Assert.assertEquals("00:03:00", total);
	}

	@Test
	public void emptyOrMissingSessionTimeKeepsLaunchTotal() {
		Assert.assertEquals("01:00:00", ScoUtils.addCappedSessionTime("01:00:00", null, MAX_SESSION_SECONDS));
		Assert.assertEquals("01:00:00", ScoUtils.addCappedSessionTime("01:00:00", "", MAX_SESSION_SECONDS));
		Assert.assertEquals("01:00:00", ScoUtils.addCappedSessionTime("01:00:00", "  ", MAX_SESSION_SECONDS));
	}

	@Test
	public void malformedSessionTimeIsIgnored() {
		Assert.assertEquals("01:00:00", ScoUtils.addCappedSessionTime("01:00:00", "abc", MAX_SESSION_SECONDS));
		Assert.assertEquals("01:00:00", ScoUtils.addCappedSessionTime("01:00:00", "12:30", MAX_SESSION_SECONDS));
	}

	@Test
	public void longSessionIsCapped() {
		// a tab left open for 5 days counts as 24 hours
		String total = ScoUtils.addCappedSessionTime("00:00:00", "0120:00:00.0", MAX_SESSION_SECONDS);
		Assert.assertEquals("24:00:00", total);
		String exactlyAtLimit = ScoUtils.addCappedSessionTime("00:00:00", "24:00:00", MAX_SESSION_SECONDS);
		Assert.assertEquals("24:00:00", exactlyAtLimit);
	}

	@Test
	public void toSeconds() {
		Assert.assertEquals(0, ScoUtils.toSeconds("0000:00:00.00"));
		Assert.assertEquals(3723, ScoUtils.toSeconds("01:02:03.50"));
		Assert.assertEquals(-1, ScoUtils.toSeconds(null));
		Assert.assertEquals(-1, ScoUtils.toSeconds(" "));
		Assert.assertEquals(-1, ScoUtils.toSeconds("abc"));
		Assert.assertEquals(-1, ScoUtils.toSeconds("12:30"));
	}

	@Test
	public void formatSeconds() {
		Assert.assertEquals("00:00:00", ScoUtils.formatSeconds(0));
		Assert.assertEquals("01:02:03", ScoUtils.formatSeconds(3723));
		Assert.assertEquals("120:00:00", ScoUtils.formatSeconds(120 * 3600));
	}

	@Test
	public void malformedLaunchTotalTimeIsKept() {
		Assert.assertEquals("abc", ScoUtils.addCappedSessionTime("abc", "00:10:00", MAX_SESSION_SECONDS));
	}
}
