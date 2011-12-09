/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package com.frentix.olat.vitero.manager;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.scheduler.JobWithDB;
import org.quartz.JobExecutionContext;

import com.frentix.olat.vitero.ViteroModule;

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
