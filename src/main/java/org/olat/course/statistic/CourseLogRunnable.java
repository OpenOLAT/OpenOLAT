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
package org.olat.course.statistic;

import java.util.Date;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.taskexecutor.LowPriority;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;

/**
 * 
 * Initial date: 21 avr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseLogRunnable implements LowPriority {

	private static final Logger log = Tracing.createLoggerFor(CourseLogRunnable.class);
	
	private final Identity identity;
	private final Long oresID;
	private final String exportDir;
	private final Date begin;
	private final Date end;
	private final boolean adminLog;
	private final boolean userLog;
	private final boolean statisticLog;
	private final Locale locale;
	private final String email;
	private final boolean isAdministrativeUser;
	
	public CourseLogRunnable(Identity identity, Long oresID, String exportDir, Date begin, Date end,
			boolean adminLog, boolean userLog, boolean statisticLog, Locale locale, String email, boolean isAdministrativeUser) {
		this.identity = identity;
		this.oresID = oresID;
		this.exportDir = exportDir;
		this.begin = begin;
		this.end = end;
		this.adminLog = adminLog;
		this.userLog = userLog;
		this.statisticLog = statisticLog;
		this.locale = locale;
		this.email = email;
		this.isAdministrativeUser = isAdministrativeUser;
	}

	@Override
	public void run() {
		AsyncExportManager asyncExportManager = CoreSpringFactory.getImpl(AsyncExportManager.class);
		ExportManager exportManager = CoreSpringFactory.getImpl(ExportManager.class);
		try {
			asyncExportManager.register(identity);
			log.info("asyncArchiveCourseLogFiles: user {} starts archiving...", identity.getKey());
			long start = System.nanoTime();
			exportManager.archiveCourseLogFiles(oresID, exportDir, begin, end, adminLog, userLog, statisticLog, locale, email, isAdministrativeUser);
			log.info("asyncArchiveCourseLogFiles: user {} finished archiving in {} (s)", identity.getKey(), CodeHelper.nanoToSecond(start));
		} finally {
			asyncExportManager.deregister(identity);
			
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(CourseLogRunnable.class, oresID);
			CourseLogRunEvent event = new CourseLogRunEvent(identity.getKey(), oresID);
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.fireEventToListenersOf(event, ores);
		}
	}
}
