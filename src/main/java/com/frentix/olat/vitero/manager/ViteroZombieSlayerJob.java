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
