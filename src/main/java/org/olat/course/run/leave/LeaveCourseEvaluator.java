/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
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
package org.olat.course.run.leave;

import org.olat.core.util.DateUtils;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryEntryRuntimeType;

/**
 * Initial date: 12.03.2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class LeaveCourseEvaluator {

	public LeaveCourseStatus evaluate(LeaveCourseContext ctx) {
		if (ctx.getRuntimeType() != RepositoryEntryRuntimeType.standalone) {
			return LeaveCourseStatus.HIDDEN;
		}

		if (ctx.getAllowToLeave() == RepositoryEntryAllowToLeaveOptions.never) {
			return LeaveCourseStatus.HIDDEN;
		}

		if (ctx.isGuest()) {
			return LeaveCourseStatus.HIDDEN;
		}

		if (ctx.getParticipations() == null || ctx.getParticipations().isEmpty()) {
			return LeaveCourseStatus.HIDDEN;
		}

		if (ctx.isAssessmentMode()) {
			return LeaveCourseStatus.HIDDEN;
		}

		for (LeaveCourseParticipation p : ctx.getParticipations()) {
			if (p.origin() == LeaveCourseParticipation.Origin.CPL) {
				return LeaveCourseStatus.HIDDEN;
			}
			if (p.origin() == LeaveCourseParticipation.Origin.GROUP && p.linkedCourseCount() > 1) {
				return LeaveCourseStatus.HIDDEN;
			}
			if (p.origin() == LeaveCourseParticipation.Origin.GROUP && p.enrollmentGroup() && !p.delistingPermitted()) {
				return LeaveCourseStatus.HIDDEN;
			}
		}

		if (ctx.getAllowToLeave() == RepositoryEntryAllowToLeaveOptions.atAnyTime) {
			return LeaveCourseStatus.ENABLED;
		}

		if (ctx.getEntryStatus().decommissioned()) {
			return LeaveCourseStatus.ENABLED;
		}

		if (ctx.getLifecycleEndDate() != null && DateUtils.toLocalDate(ctx.getNow()).isAfter(DateUtils.toLocalDate(ctx.getLifecycleEndDate()))) {
			return LeaveCourseStatus.ENABLED;
		}

		return LeaveCourseStatus.DISABLED;
	}
}
