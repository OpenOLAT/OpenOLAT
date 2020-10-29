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
package org.olat.course.nodes.gta.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.gta.IdentityMark;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.model.IdentityMarkImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 03.10.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GTAIdentityMarkDAO {
	
	@Autowired
	private DB dbInstance;

	public IdentityMark createAndPersisitMark(TaskList taskList, Identity marker, Identity participant) {
		IdentityMarkImpl mark = new IdentityMarkImpl();
		Date creationDate = new Date();
		mark.setCreationDate(creationDate);
		mark.setLastModified(creationDate);
		mark.setTaskList(taskList);
		mark.setMarker(marker);
		mark.setParticipant(participant);
		dbInstance.getCurrentEntityManager().persist(mark);
		return mark;	
	}
	
	public List<IdentityMark> loadMarks(TaskList taskList, Identity marker) {
		if (taskList == null || taskList.getKey() == null || marker == null || marker.getKey() == null)
			return new ArrayList<>();

		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadByMarker", IdentityMark.class)
				.setParameter("taskListKey", taskList.getKey())
				.setParameter("markerKey", marker.getKey())
				.getResultList();
	}
	
	public boolean isMarked(TaskList taskList, Identity marker, Identity participant) {
		if (taskList == null || taskList.getKey() == null || marker == null || marker.getKey() == null
				|| participant == null || participant.getKey() == null) {
			return false;
		}

		List<IdentityMark> marks = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadByMarkerAndParticipant", IdentityMark.class)
				.setParameter("taskListKey", taskList.getKey())
				.setParameter("markerKey", marker.getKey())
				.setParameter("participantKey", participant.getKey())
				.getResultList();
		
		return !marks.isEmpty();
	}

	public boolean hasMarks(TaskList taskList, Identity marker) {
		if (taskList == null || taskList.getKey() == null || marker == null || marker.getKey() == null)
			return false;

		List<IdentityMark> marks = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadByMarker", IdentityMark.class)
				.setParameter("taskListKey", taskList.getKey())
				.setParameter("markerKey", marker.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		
		return !marks.isEmpty();
	}

	public void deleteMark(TaskList taskList, Identity marker, Identity participant) {
		if (taskList == null || taskList.getKey() == null || marker == null || marker.getKey() == null
				|| participant == null || participant.getKey() == null) {
			return;
		}

		dbInstance.getCurrentEntityManager()
				.createNamedQuery("deleteByMarker")
				.setParameter("taskListKey", taskList.getKey())
				.setParameter("markerKey", marker.getKey())
				.setParameter("participantKey", participant.getKey())
				.executeUpdate();
	}

	public int deleteMark(TaskList taskList) {
		if (taskList == null || taskList.getKey() == null) return 0;
		
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("deleteByTaskList")
				.setParameter("taskListKey", taskList.getKey())
				.executeUpdate();
	}

	public int deleteMark(List<Long> taskKeys) {
		if (taskKeys == null || taskKeys.isEmpty()) return 0;
		
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("deleteByTaskKeys")
				.setParameter("taskKeys", taskKeys)
				.executeUpdate();
	}
}
