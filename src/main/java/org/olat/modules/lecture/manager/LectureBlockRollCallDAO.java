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
package org.olat.modules.lecture.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.model.LectureBlockRollCallImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LectureBlockRollCallDAO {
	
	@Autowired
	private DB dbInstance;
	
	public LectureBlockRollCall createAndPersistRollCall(LectureBlock lectureBlock, Identity identity) {
		LectureBlockRollCallImpl rollCall = new LectureBlockRollCallImpl();
		rollCall.setCreationDate(new Date());
		rollCall.setLastModified(rollCall.getCreationDate());
		rollCall.setIdentity(identity);
		rollCall.setLectureBlock(lectureBlock);
		dbInstance.getCurrentEntityManager().persist(rollCall);
		return rollCall;
	}
	
	public LectureBlockRollCall loadByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rollcall from lectureblockrollcall rollcall")
		  .append(" inner join fetch rollcall.identity ident")
		  .append(" inner join fetch ident.user user")
		  .append(" inner join fetch rollcall.lectureBlock block")
		  .append(" where rollcall.key=:rollCallKey");
		
		List<LectureBlockRollCall> rollCalls = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockRollCall.class)
				.setParameter("rollCallKey", key)
				.getResultList();
		return rollCalls == null || rollCalls.isEmpty() ? null : rollCalls.get(0);
	}
}
