/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramRef;
import org.olat.modules.certificationprogram.CertificationProgramToOrganisation;
import org.olat.modules.certificationprogram.model.CertificationProgramToOrganisationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CertificationProgramToOrganisationDAO {

	@Autowired
	private DB dbInstance;
	
	public CertificationProgramToOrganisation createRelation(CertificationProgram program, Organisation organisation) {
		CertificationProgramToOrganisationImpl rel = new CertificationProgramToOrganisationImpl();
		rel.setCreationDate(new Date());
		rel.setCertificationProgram(program);
		rel.setOrganisation(organisation);
		dbInstance.getCurrentEntityManager().persist(rel);
		return rel;
	}
	
	public void removeOrganisation(CertificationProgramRef program, Organisation organisation) {
		String query = """
				delete from certificationprogramtoorganisation as rel
				where rel.certificationProgram.key=:certificationProgramKey
				and rel.organisation.key=:organisationKey""";
		dbInstance.getCurrentEntityManager().createQuery(query)
				.setParameter("certificationProgramKey", program.getKey())
				.setParameter("organisationKey", organisation.getKey())
				.executeUpdate();
	}
	
	public List<Organisation> getOrganisations(CertificationProgramRef program) {
		String query = """
				select org from certificationprogramtoorganisation as rel
				inner join rel.organisation as org
				where rel.certificationProgram.key=:certificationProgramKey""";
		return dbInstance.getCurrentEntityManager().createQuery(query, Organisation.class)
				.setParameter("certificationProgramKey", program.getKey())
				.getResultList();
	}
}
