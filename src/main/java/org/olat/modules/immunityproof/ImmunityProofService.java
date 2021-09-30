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
package org.olat.modules.immunityproof;

import java.util.Date;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.modules.immunityproof.ImmunityProofModule.ImmunityProofLevel;
import org.olat.modules.immunityproof.ImmunityProofModule.ImmunityProofType;

/**
 * Initial date: 08.09.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public interface ImmunityProofService {
	
	public void createImmunityProof(Identity identity, ImmunityProofType type, Date safeUntil, boolean sendMail,
			boolean validated, boolean deleteOtherImmunityProof);
	
	public void createImmunityProofFromCertificate(Identity identity, Date safeUntil, boolean sendMail,
			boolean deleteOtherImmunityProof);

	public ImmunityProof updateImmunityProof(ImmunityProof immunityProof);
	
	public ImmunityProof getImmunityProof(Identity identity);
	
	public void deleteImmunityProof(Identity identity);
	
	public void pruneImmunityProofs(Date pruneUntil);
	
	public void deleteAllImmunityProofs(boolean notifyUser);
	
	public void deleteImmunityProof(ImmunityProof immunityProof);
	
	public List<ImmunityProof> getAllCertificates();
	
	public ImmunityProofLevel getImmunityProofLevel(ImmunityProof immunityProof);
	
	public long getImmunityProofCount();
	
}
