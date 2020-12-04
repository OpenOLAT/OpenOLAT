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
package org.olat.user;

import org.apache.logging.log4j.Logger;
import org.olat.core.configuration.PreWarm;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 09.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class UserDisplayNamePreWarm implements PreWarm {
	
	private static final Logger log = Tracing.createLoggerFor(UserDisplayNamePreWarm.class);
	
	@Autowired
	private UserManager userManager;

	@Override
	public void run() {
		long start = System.nanoTime();
		log.info("Start filling the user displayname cache");
		int numOfNames = userManager.warmUp();
		log.info("Display name cache filled with {} names in (ms): {}", numOfNames, CodeHelper.nanoToMilliTime(start));
	}
}
