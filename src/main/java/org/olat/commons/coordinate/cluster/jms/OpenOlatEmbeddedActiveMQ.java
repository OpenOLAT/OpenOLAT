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
package org.olat.commons.coordinate.cluster.jms;

import java.util.concurrent.TimeUnit;

import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.core.config.FileDeploymentManager;
import org.apache.activemq.artemis.core.config.impl.FileConfiguration;
import org.apache.activemq.artemis.core.config.impl.LegacyJMSConfiguration;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnector;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * The sole purpose of this class is to start the embedded JMS server
 * with the directory loaded from the Spring configuration.
 * 
 * Initial date: 13 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenOlatEmbeddedActiveMQ extends EmbeddedActiveMQ {
	
	private static final Logger log = Tracing.createLoggerFor(OpenOlatEmbeddedActiveMQ.class);
	
	private String jmsDir;
	private boolean persistenceEnabled;

	public String getJmsDir() {
		return jmsDir;
	}

	public void setJmsDir(String jmsDir) {
		this.jmsDir = jmsDir;
		if(!jmsDir.endsWith("/")) {
			jmsDir += "/";
		}
	}

	public boolean isPersistenceEnabled() {
		return persistenceEnabled;
	}

	public void setPersistenceEnabled(boolean persistenceEnabled) {
		this.persistenceEnabled = persistenceEnabled;
	}

	@Override
	public void createActiveMQServer() throws Exception {
		if(activeMQServer != null) {
			return;
		}
		if (configuration == null) {
			if (configResourcePath == null) {
				configResourcePath = "broker.xml";
			}
			FileDeploymentManager deploymentManager = new FileDeploymentManager(configResourcePath);
			FileConfiguration config = new FileConfiguration();
			
			LegacyJMSConfiguration legacyJMSConfiguration = new LegacyJMSConfiguration(config);
			deploymentManager.addDeployable(config).addDeployable(legacyJMSConfiguration);
			deploymentManager.readConfiguration();
			
			config.setBindingsDirectory(jmsDir + "bindings");
			config.setCreateBindingsDir(true);
			config.setJournalDirectory(jmsDir + "journal");
			config.setCreateJournalDir(true);
			config.setPagingDirectory(jmsDir + "paging");
			config.setCreateJournalDir(true);
			config.setLargeMessagesDirectory(jmsDir + "largemessages");
			config.setPersistenceEnabled(persistenceEnabled);
			
			configuration = config;
		}
		super.createActiveMQServer();
	}

	@Override
	public EmbeddedActiveMQ stop() throws Exception {
		super.stop();
		try {// Hardcore shutdown for our server administrators
			InVMConnector.resetThreadPool();
			ActiveMQClient.clearThreadPools(1, TimeUnit.SECONDS);
		} catch (Exception e) {
			log.error("", e);
		}
		return this;
	}
}
