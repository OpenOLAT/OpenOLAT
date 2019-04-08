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
package org.olat.core.commons.services.doceditor.wopi.model;

import java.util.List;

import org.olat.core.commons.services.doceditor.wopi.Discovery;
import org.olat.core.commons.services.doceditor.wopi.NetZone;
import org.olat.core.commons.services.doceditor.wopi.ProofKey;

/**
 * 
 * Initial date: 1 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DiscoveryImpl implements Discovery {
	
	private ProofKeyImpl proofKey;
	private List<NetZone> netZones;

	@Override
	public ProofKey getProofKey() {
		return proofKey;
	}

	public void setProofKey(ProofKeyImpl proofKey) {
		this.proofKey = proofKey;
	}

	@Override
	public List<NetZone> getNetZones() {
		return netZones;
	}

	public void setNetZones(List<NetZone> netZones) {
		this.netZones = netZones;
	}

}
