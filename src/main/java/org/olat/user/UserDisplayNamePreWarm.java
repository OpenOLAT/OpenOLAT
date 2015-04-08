package org.olat.user;

import org.olat.core.configuration.PreWarm;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.springframework.stereotype.Service;

/**
 * 
 * @author srosse
 *
 */
@Service
public class UserDisplayNamePreWarm implements PreWarm {
	
	private static final OLog log = Tracing.createLoggerFor(UserDisplayNamePreWarm.class);

	@Override
	public void run() {
		long start = System.nanoTime();
		log.info("Start filling the user displayname cache");
		
		int numOfNames = UserManager.getInstance().warmUp();

		log.info("Display name cache filled with " + numOfNames + " names in (ms): " + CodeHelper.nanoToMilliTime(start));
	}
}
