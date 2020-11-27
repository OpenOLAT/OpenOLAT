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
package org.olat.modules.appointments.ui;

import static org.olat.modules.appointments.ui.StartDuration.getEnd;

import java.util.GregorianCalendar;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

/**
 * 
 * Initial date: 26 Nov 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class StartDurationTest {
	
	@Test
	public void shouldGetNextStattDuration() {
		SoftAssertions softly = new SoftAssertions();
		
		assertNext(softly,
				"nulls",
				null,
				null,
				StartDuration.none()
				);
		assertNext(softly,
				"previous start",
				StartDuration.of(new GregorianCalendar(2020, 5, 10, 10, 15, 0).getTime(), null),
				null,
				StartDuration.of(new GregorianCalendar(2020, 5, 10, 10, 15, 0).getTime(), null)
				);
		assertNext(softly,
				"previous start/duration",
				StartDuration.of(new GregorianCalendar(2020, 5, 10, 10, 15, 0).getTime(), 60),
				null,
				StartDuration.of(new GregorianCalendar(2020, 5, 10, 11, 15, 0).getTime(), 60)
				);
		// Round up to the same minute
		assertNext(softly,
				"previous start/duration round incl hour",
				StartDuration.of(new GregorianCalendar(2020, 5, 10, 10, 15, 0).getTime(), 30),
				null,
				StartDuration.of(new GregorianCalendar(2020, 5, 10, 11, 15, 0).getTime(), 30)
				);
		assertNext(softly,
				"previous start/duration round",
				StartDuration.of(new GregorianCalendar(2020, 5, 10, 10, 15, 0).getTime(), 50),
				null,
				StartDuration.of(new GregorianCalendar(2020, 5, 10, 11, 15, 0).getTime(), 50)
				);
		assertNext(softly,
				"previous start/duration, previous2 start",
				StartDuration.of(new GregorianCalendar(2020, 5, 10, 10, 15, 0).getTime(), 30),
				StartDuration.of(new GregorianCalendar(2020, 5, 10, 9, 15, 0).getTime(), null),
				StartDuration.of(new GregorianCalendar(2020, 5, 10, 11, 15, 0).getTime(), 30)
				);
		assertNext(softly,
				"previous start, previous2 start/duration, pause 10",
				StartDuration.of(new GregorianCalendar(2020, 5, 10, 10, 15, 0).getTime(), 45),
				StartDuration.of(new GregorianCalendar(2020, 5, 10, 9, 15, 0).getTime(), 50),
				StartDuration.of(new GregorianCalendar(2020, 5, 10, 11, 10, 0).getTime(), 45)
				);
		assertNext(softly,
				"previous start/duration, previous2 start/duration, pause 40",
				StartDuration.of(new GregorianCalendar(2020, 5, 10, 10, 45, 0).getTime(), null),
				StartDuration.of(new GregorianCalendar(2020, 5, 10, 9, 15, 0).getTime(), 50),
				StartDuration.of(new GregorianCalendar(2020, 5, 10, 12, 15, 0).getTime(), null)
				);
		
		softly.assertAll();
	}
	
	private void assertNext(SoftAssertions softly, String description, StartDuration previous, StartDuration previous2, StartDuration expected) {
		StartDuration next = StartDuration.next(previous, previous2);
		softly.assertThat(next.getStart()).as(description + ": start").isEqualTo(expected.getStart());
		softly.assertThat(next.getDuration()).as(description + ": duration").isEqualTo(expected.getDuration());
	}
	
	@Test
	public void shouldGetEnd() {
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(getEnd(StartDuration.of(new GregorianCalendar(2020, 5, 10, 10, 15, 0).getTime(), 45)))
				.isEqualTo(new GregorianCalendar(2020, 5, 10, 11, 00, 0).getTime());
		softly.assertThat(getEnd(StartDuration.of(new GregorianCalendar(2020, 5, 10, 10, 15, 0).getTime(), null)))
				.isNull();
		softly.assertThat(getEnd(StartDuration.of(null, null)))
				.isNull();
		softly.assertAll();
	}


}
