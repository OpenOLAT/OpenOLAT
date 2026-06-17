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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.todo.ui;

import java.util.Date;

import org.olat.core.util.DateUtils;

/**
 *
 * Initial date: 12 May 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
enum ToDoCreationDateFilter {

	today {
		@Override
		public Date getFrom(Date now) {
			return DateUtils.setTime(now, 0, 0, 0);
		}
	},
	last7Days {
		@Override
		public Date getFrom(Date now) {
			return DateUtils.setTime(DateUtils.addDays(now, -7), 0, 0, 0);
		}
	},
	last4Weeks {
		@Override
		public Date getFrom(Date now) {
			return DateUtils.setTime(DateUtils.addWeeks(now, -4), 0, 0, 0);
		}
	};

	public abstract Date getFrom(Date now);

}
