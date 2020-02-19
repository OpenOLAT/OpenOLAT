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
package org.olat.user.manager;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.user.AbsenceLeave;
import org.olat.user.AbsenceLeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 14 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AbsenceLeaveServiceImpl implements AbsenceLeaveService {
	
	@Autowired
	private AbsenceLeaveDAO absenceLeaveDao;

	@Override
	public AbsenceLeave getOrCreateAbsenceLeave(Identity identity, Date from, Date to, OLATResourceable resource, String subIdent) {
		return absenceLeaveDao.createAbsenceLeave(identity, from, to, resource, subIdent);
	}

	@Override
	public List<AbsenceLeave> getAbsenceLeaves(IdentityRef identity) {
		return absenceLeaveDao.getAbsenceLeaves(identity);
	}
	
	
}
