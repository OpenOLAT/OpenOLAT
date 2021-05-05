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
package org.olat.modules.bigbluebutton.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.bigbluebutton.BigBlueButtonServer;
import org.olat.modules.bigbluebutton.model.BigBlueButtonServerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 7 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class BigBlueButtonServerDAO {
	
	@Autowired
	private DB dbInstance;
	
	public BigBlueButtonServer createServer(String url, String recordingUrl, String sharedSecret) {
		BigBlueButtonServerImpl server = new BigBlueButtonServerImpl();
		server.setCreationDate(new Date());
		server.setLastModified(server.getCreationDate());
		server.setUrl(url);
		server.setRecordingUrl(recordingUrl);
		server.setSharedSecret(sharedSecret);
		server.setEnabled(true);
		server.setManualOnly(false);
		server.setCapacityFactory(1.0d);
		dbInstance.getCurrentEntityManager().persist(server);
		return server;
	}
	
	public BigBlueButtonServer updateServer(BigBlueButtonServer server) {
		((BigBlueButtonServerImpl)server).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(server);
	}
	
	public void deleteServer(BigBlueButtonServer server) {
		BigBlueButtonServer reloadedServer = dbInstance.getCurrentEntityManager()
				.getReference(BigBlueButtonServerImpl.class, server.getKey());
		dbInstance.getCurrentEntityManager().remove(reloadedServer);
	}
	
	public List<BigBlueButtonServer> getServers() {
		String q = "select server from bigbluebuttonserver server";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, BigBlueButtonServer.class)
				.getResultList();
	}
	
	public BigBlueButtonServer getServer(Long serverKey) {
		String q = "select server from bigbluebuttonserver server where server.key=:serverKey";
		List<BigBlueButtonServer> servers = dbInstance.getCurrentEntityManager()
				.createQuery(q, BigBlueButtonServer.class)
				.setParameter("serverKey", serverKey)
				.getResultList();
		return servers == null || servers.isEmpty() ? null : servers.get(0);
	}

}
