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
package org.olat.user.manager;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.taskexecutor.LongRunnable;
import org.olat.core.logging.Tracing;
import org.olat.user.UserDataExportService;

/**
 * 
 * Initial date: 23 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserDataExportTask implements LongRunnable {

	private static final long serialVersionUID = 6931074116105090545L;

	private static final Logger log = Tracing.createLoggerFor(UserDataExportTask.class);
	
	private final Long exportKey;
	
	public UserDataExportTask(Long exportKey) {
		this.exportKey = exportKey;
	}
	
	@Override
	public Queue getExecutorsQueue() {
		return Queue.lowPriority;
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		UserDataExportService exportService = CoreSpringFactory.getImpl(UserDataExportService.class);
		exportService.exportData(exportKey);
		log.info("Finished data export thread for={} in {} (ms)", exportKey, (System.currentTimeMillis() - startTime));
	}
}
