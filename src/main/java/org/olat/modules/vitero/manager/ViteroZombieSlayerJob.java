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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.vitero.manager;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.modules.vitero.ViteroModule;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

/**
 * 
 * Description:<br>
 * Quartz job which try to slay the meeting zombies from deleted
 * resources/groups. Make is slayer's job only if the module
 * is enabled and a connection with the vms is possible.
 * 
 * <P>
 * Initial Date:  12 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@DisallowConcurrentExecution
public class ViteroZombieSlayerJob extends JobWithDB  {

	@Override
	public void executeWithDB(JobExecutionContext arg0) {
		ViteroModule module = (ViteroModule)CoreSpringFactory.getBean("viteroModule");
		if(!module.isEnabled()) return;

		ViteroManager viteroManager = (ViteroManager)CoreSpringFactory.getBean("viteroManager");
		if(viteroManager.checkConnection()) {
			viteroManager.slayZombies();
		}
	}
}
