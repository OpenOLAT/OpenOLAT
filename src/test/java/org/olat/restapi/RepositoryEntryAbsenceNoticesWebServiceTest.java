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
package org.olat.restapi;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeTarget;
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.manager.AbsenceNoticeDAO;
import org.olat.modules.lecture.manager.AbsenceNoticeToLectureBlockDAO;
import org.olat.modules.lecture.manager.LectureBlockDAO;
import org.olat.modules.lecture.restapi.AbsenceNoticeVO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.restapi.support.ObjectFactory;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 juin 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntryAbsenceNoticesWebServiceTest extends OlatRestTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private AbsenceNoticeDAO absenceNoticeDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private AbsenceNoticeToLectureBlockDAO absenceNoticeToLectureBlockDao;
	
	@Test
	public void getAbsenceNoticesByIdentity()
	throws IOException, URISyntaxException {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-1");
		Identity notifier = JunitTestHelper.createAndPersistIdentityAsRndUser("notifier-1");
		Identity authorizer = JunitTestHelper.createAndPersistIdentityAsRndUser("authorizer-1");
		
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry, new Date(), DateUtils.addHours(new Date(), 1));
		repositoryEntryRelationDao.addRole(identity, entry, GroupRoles.participant.name());
		Group defGroup = repositoryEntryRelationDao.getDefaultGroup(lectureBlock.getEntry());
		lectureBlockDao.addGroupToLectureBlock(lectureBlock, defGroup);
		dbInstance.commit();
		
		Date now = new Date();
		Date start = DateUtils.getStartOfDay(now);
		Date end = DateUtils.getEndOfDay(now);
		AbsenceNotice notice = absenceNoticeDao.createAbsenceNotice(identity, AbsenceNoticeType.absence, AbsenceNoticeTarget.lectureblocks,
				start, end, null, "A very good reason", Boolean.TRUE, authorizer, notifier);
		absenceNoticeToLectureBlockDao.createRelation(notice, lectureBlock);
		dbInstance.commitAndCloseSession();
		
		URI uri = UriBuilder.fromUri(getContextURI())
				.path("repo").path("entries").path(entry.getKey().toString())
				.path("absencenotices").path(identity.getKey().toString())
				.queryParam("startDate", ObjectFactory.formatDate(DateUtils.addDays(start, -3)))
				.queryParam("endDate", ObjectFactory.formatDate(DateUtils.addDays(end, 2)))
				.build();
		
		RestConnection conn = new RestConnection("administrator", "openolat");

		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<AbsenceNoticeVO> notices = conn.parseList(response, AbsenceNoticeVO.class);
		Assertions.assertThat(notices)
			.isNotNull()
			.hasSize(1);
		
		AbsenceNoticeVO noticeVo = notices.get(0);
		Assert.assertEquals(notice.getKey(), noticeVo.getKey());
	}
	
	private LectureBlock createMinimalLectureBlock(RepositoryEntry entry, Date start, Date end) {
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry, null);
		lectureBlock.setStartDate(start);
		lectureBlock.setEndDate(end);
		lectureBlock.setTitle("Absence");
		lectureBlock.setPlannedLecturesNumber(4);
		lectureBlock.setEffectiveLecturesNumber(4);
		return lectureBlockDao.update(lectureBlock);
	}
}
