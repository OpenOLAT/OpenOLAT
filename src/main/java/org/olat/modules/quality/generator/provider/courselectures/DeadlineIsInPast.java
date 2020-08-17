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
package org.olat.modules.quality.generator.provider.courselectures;

import static org.olat.modules.quality.generator.ProviderHelper.addDays;
import static org.olat.modules.quality.generator.ProviderHelper.addMinutes;

import java.util.Date;
import java.util.function.Predicate;

import org.olat.core.util.StringHelper;
import org.olat.modules.quality.generator.provider.courselectures.manager.LectureBlockInfo;

/**
 * 
 * Initial date: 17 Aug 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DeadlineIsInPast implements Predicate<LectureBlockInfo> {
	
	private final String minutesBeforeEnd;
	private final String duration;
	private final Date now;
	
	DeadlineIsInPast(String minutesBeforeEnd, String duration) {
		this.minutesBeforeEnd = minutesBeforeEnd;
		this.duration = duration;
		this.now = new Date();
	}

	@Override
	public boolean test(LectureBlockInfo lectureBlockInfo) {
		// calculate start and deadline
		Date dcStart = lectureBlockInfo.getLectureEndDate();
		String realMinutesBeforeEnd = StringHelper.containsNonWhitespace(minutesBeforeEnd)? minutesBeforeEnd: "0";
		dcStart = addMinutes(dcStart, "-" + realMinutesBeforeEnd);
		Date deadline = addDays(dcStart, duration);
		return deadline.before(now);
	}

}
