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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureBlockToGroup;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.Reason;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.model.LectureBlockAndRollCall;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.modules.lecture.model.LectureStatistics;
import org.olat.modules.lecture.model.ParticipantAndLectureSummary;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.manager.RepositoryEntryDAO;
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
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private ReasonDAO reasonDao;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private LectureBlockToGroupDAO lectureBlockToGroupDao;
	@Autowired
	private LectureBlockRollCallDAO lectureBlockRollCallDao;
	@Autowired
	private LectureParticipantSummaryDAO lectureParticipantSummaryDao;
	@Autowired
	private RepositoryEntryLectureConfigurationDAO lectureConfigurationDao;
	
	
	@Override
	public RepositoryEntryLectureConfiguration getRepositoryEntryLectureConfiguration(RepositoryEntry entry) {
		RepositoryEntryLectureConfiguration config = lectureConfigurationDao.getConfiguration(entry);
		if(config == null) {
			RepositoryEntry reloadedEntry = repositoryEntryDao.loadForUpdate(entry);
			config = lectureConfigurationDao.getConfiguration(entry);
			if(config == null) {
				config = lectureConfigurationDao.createConfiguration(reloadedEntry);
			}
			dbInstance.commit();
		}
		return config;
	}

	@Override
	public RepositoryEntryLectureConfiguration updateRepositoryEntryLectureConfiguration(RepositoryEntryLectureConfiguration config) {
		return lectureConfigurationDao.update(config);
	}

	@Override
	public LectureBlock createLectureBlock(RepositoryEntry entry) {
		return lectureBlockDao.createLectureBlock(entry);
	}

	@Override
	public LectureBlock save(LectureBlock lectureBlock, List<Group> groups) {
		LectureBlockImpl block = (LectureBlockImpl)lectureBlockDao.update(lectureBlock);
		if(groups != null) {
			List<LectureBlockToGroup> lectureToGroups = lectureBlockToGroupDao.getLectureBlockToGroups(block);
			for(Group group:groups) {
				boolean found = false;
				for(LectureBlockToGroup lectureToGroup:lectureToGroups) {
					if(lectureToGroup.getGroup().equals(group)) {
						found = true;
						break;
					}
				}
				
				if(!found) {
					LectureBlockToGroup blockToGroup = lectureBlockToGroupDao.createAndPersist(block, group);
					lectureToGroups.add(blockToGroup);
				}
			}
			
			for(Iterator<LectureBlockToGroup> lectureToGroupIt=lectureToGroups.iterator(); lectureToGroupIt.hasNext(); ) {
				LectureBlockToGroup lectureBlockToGroup = lectureToGroupIt.next();
				if(!groups.contains(lectureBlockToGroup.getGroup())) {
					lectureBlockToGroupDao.remove(lectureBlockToGroup);
				}
			}
		}
		block.getTeacherGroup().getKey();
		return block;
	}

	@Override
	public LectureBlock getLectureBlock(LectureBlockRef block) {
		return lectureBlockDao.loadByKey(block.getKey());
	}

	@Override
	public List<Reason> getAllReasons() {
		return reasonDao.getReasons();
	}

	@Override
	public Reason createReason(String title, String description) {
		return reasonDao.createReason(title, description);
	}

	@Override
	public Reason updateReason(Reason reason) {
		return reasonDao.updateReason(reason);
	}

	@Override
	public List<Group> getLectureBlockToGroups(LectureBlockRef block) {
		return lectureBlockToGroupDao.getGroups(block);
	}
	
	@Override
	public List<Identity> getParticipants(LectureBlockRef block) {
		return lectureBlockDao.getParticipants(block);
	}

	@Override
	public List<Identity> startLectureBlock(Identity teacher, LectureBlock lectureBlock) {
		RepositoryEntry entry = lectureBlock.getEntry();
		Date now = new Date();

		List<ParticipantAndLectureSummary> participantsAndSummaries = lectureParticipantSummaryDao.getLectureParticipantSummaries(lectureBlock);
		Set<Identity> participants = new HashSet<>();
		for(ParticipantAndLectureSummary participantAndSummary:participantsAndSummaries) {
			if(participants.contains(participantAndSummary.getIdentity())) {
				continue;
			}
			if(participantAndSummary.getSummary() == null) {
				lectureParticipantSummaryDao.createSummary(entry, participantAndSummary.getIdentity(), now);
			}
			participants.add(participantAndSummary.getIdentity());
		}
		return new ArrayList<>(participants);
	}

	@Override
	public List<LectureBlockRollCall> getRollCalls(LectureBlockRef block) {
		return lectureBlockRollCallDao.getRollCalls(block);
	}

	@Override
	public LectureBlockRollCall createRollCall(Identity identity, LectureBlock lectureBlock, Boolean authorizedAbsence) {
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.getRollCall(lectureBlock, identity);
		if(rollCall == null) {//reload in case of concurrent usage
			rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity, authorizedAbsence, null, null);
		} else if(authorizedAbsence != null) {
			rollCall.setAbsenceAuthorized(authorizedAbsence);
			rollCall = lectureBlockRollCallDao.update(rollCall);
		}
		return rollCall;
	}

	@Override
	public LectureBlockRollCall updateRollCall(LectureBlockRollCall rollCall) {
		return lectureBlockRollCallDao.update(rollCall);
	}

	@Override
	public LectureBlockRollCall addRollCall(Identity identity, LectureBlock lectureBlock, LectureBlockRollCall rollCall, Integer... absences) {
		if(rollCall == null) {//reload in case of concurrent usage
			rollCall = lectureBlockRollCallDao.getRollCall(lectureBlock, identity);
		}
		if(rollCall == null) {
			rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity, null, null, null, absences);
		} else {
			rollCall = lectureBlockRollCallDao.addLecture(lectureBlock, rollCall, absences);
		}
		return rollCall;
	}
	
	@Override
	public LectureBlockRollCall addRollCall(Identity identity, LectureBlock lectureBlock, LectureBlockRollCall rollCall, String comment, Integer... absences) {
		if(rollCall == null) {//reload in case of concurrent usage
			rollCall = lectureBlockRollCallDao.getRollCall(lectureBlock, identity);
		}
		if(rollCall == null) {
			rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity, null, null, comment, absences);
		} else {
			if(comment != null) {
				rollCall.setComment(comment);
			}
			rollCall = lectureBlockRollCallDao.addLecture(lectureBlock, rollCall, absences);
		}
		return rollCall;
	}

	@Override
	public LectureBlockRollCall removeRollCall(Identity identity, LectureBlock lectureBlock, LectureBlockRollCall rollCall, Integer... absences) {
		if(rollCall == null) {//reload in case of concurrent usage
			rollCall = lectureBlockRollCallDao.getRollCall(lectureBlock, identity);
		}
		if(rollCall == null) {
			rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity, null, null, null, absences);
		} else {
			rollCall = lectureBlockRollCallDao.removeLecture(lectureBlock, rollCall, absences);
		}
		return rollCall;
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
	public List<LectureBlock> getLectureBlocks(RepositoryEntryRef entry, IdentityRef teacher) {
		return lectureBlockDao.getLecturesAsTeacher(entry, teacher);
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
	
	@Override
	public List<LectureStatistics> getParticipantLecturesStatistics(IdentityRef identity) {
		return lectureBlockRollCallDao.getStatistics(identity);
	}

	@Override
	public List<LectureBlockAndRollCall> getParticipantLectureBlocks(RepositoryEntryRef entry, IdentityRef participant) {
		return lectureBlockRollCallDao.getParticipantLectureBlockAndRollCalls(entry, participant);
	}
}