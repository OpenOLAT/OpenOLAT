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
 * JGS goodsolutions GmbH, http://www.goodsolutions.ch
 * <p>
 */
package ch.goodsolutions.olat.jmx;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import org.olat.core.gui.control.DefaultController;
import org.olat.core.helpers.Settings;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;

public class OLATIdentifier implements OLATIdentifierMBean {

	public static final String IDENTIFIER = "genuineOLAT";
	private static MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
	
	private OLATIdentifier() {
		//
	}
	
	public String getInstanceID() {
		return WebappHelper.getInstanceId();
	}

	public String getVersion() {
		return Settings.getVersion();
	}

	public String getBuild() {
		return Settings.getBuildIdentifier();
	}

	public String getContextPath() {
		return WebappHelper.getServletContextPath();
	}
	
	public String getWebAppUri() {
		return Settings.getServerContextPathURI();
	}

	public int getAuthenticatedUsersCount() {
		return UserSession.getAuthenticatedUserSessions().size();
	}
	
	public long getMemoryHeapUsageKB() {
		return mbean.getHeapMemoryUsage().getUsed() / 1024;
	}
	
	public long getControllerCount() {
		return DefaultController.getControllerCount();
	}
	
	public long getThreadCount() {
		ThreadGroup tg = Thread.currentThread().getThreadGroup();
		return tg.activeCount();
	}
}
