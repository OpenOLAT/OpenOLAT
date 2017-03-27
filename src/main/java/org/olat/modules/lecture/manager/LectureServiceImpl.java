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

import java.util.List;

import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LectureServiceImpl implements LectureService {
	
	@Autowired
	private GroupDAO groupDao;
	
	@Autowired
	private LectureBlockDAO lectureBlockDao;

	@Override
	public LectureBlock createLectureBlock(RepositoryEntry entry) {
		return lectureBlockDao.createLectureBlock(entry);
	}

	@Override
	public LectureBlock save(LectureBlock lectureBlock) {
		return lectureBlockDao.update(lectureBlock);
	}

	@Override
	public LectureBlock getLectureBlock(LectureBlockRef block) {
		return lectureBlockDao.loadByKey(block.getKey());
	}

	@Override
	public List<LectureBlock> getLectureBlocks(RepositoryEntryRef entry) {
		return lectureBlockDao.loadByEntry(entry);
	}

	@Override
	public List<Identity> getTeachers(LectureBlock lectureBlock) {
		LectureBlockImpl block = (LectureBlockImpl)lectureBlock;
		return groupDao.getMembers(block.getTeacherGroup(), "teacher");
	}

	@Override
	public boolean hasLecturesAsTeacher(RepositoryEntryRef entry, Identity identity) {
		return lectureBlockDao.hasLecturesAsTeacher(entry, identity);
	}

	@Override
	public void addTeacher(LectureBlock lectureBlock, Identity teacher) {
		LectureBlockImpl block = (LectureBlockImpl)lectureBlock;
		if(!groupDao.hasRole(block.getTeacherGroup(), teacher, "teacher")) {
			groupDao.addMembershipOneWay(block.getTeacherGroup(), teacher, "teacher");
		}
	}

	@Override
	public void removeTeacher(LectureBlock lectureBlock, Identity teacher) {
		LectureBlockImpl block = (LectureBlockImpl)lectureBlock;
		groupDao.removeMembership(block.getTeacherGroup(), teacher);
	}
}