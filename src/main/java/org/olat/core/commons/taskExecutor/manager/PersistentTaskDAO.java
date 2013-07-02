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
package org.olat.core.commons.taskExecutor.manager;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.LockModeType;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.taskExecutor.TaskStatus;
import org.olat.core.commons.taskExecutor.model.PersistentTask;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Initial date: 02.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("persistentTaskDao")
public class PersistentTaskDAO {
	
	private static XStream xstream = XStreamHelper.createXStreamInstance();
	
	@Autowired
	private DB dbInstance;

	public PersistentTask createTask(String name, Serializable task) {
		PersistentTask ptask = new PersistentTask();
		Date currentDate = new Date();
		ptask.setCreationDate(currentDate);
		ptask.setLastModified(currentDate);
		ptask.setName(name);
		ptask.setStatus(TaskStatus.newTask.name());
		ptask.setTask(xstream.toXML(task));
		dbInstance.getCurrentEntityManager().persist(ptask);
		return ptask;
	}
	
	public List<Long> tasksToDo() {
		StringBuilder sb = new StringBuilder();
		sb.append("select task.key from extask task where task.status='newTask'")
		  .append(" or (task.status='inWork' and task.executorNode=:executorNode and task.executorBootId!=:executorBootId)");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("executorBootId", WebappHelper.getBootId())
				.setParameter("executorNode", Integer.toString(WebappHelper.getNodeId()))
				.getResultList();
	}
	
	public PersistentTask pickTask(Long taskKey) {
		PersistentTask task = dbInstance.getCurrentEntityManager()
				.find(PersistentTask.class, taskKey, LockModeType.PESSIMISTIC_WRITE);
		
		if(TaskStatus.newTask.name().equals(task.getStatus())) {
			task.setStatus(TaskStatus.inWork.name());
			task.setExecutorNode(Integer.toString(WebappHelper.getNodeId()));
			task.setExecutorBootId(WebappHelper.getBootId());
			task = dbInstance.getCurrentEntityManager().merge(task);
		} else if(TaskStatus.inWork.name().equals(task.getStatus())) {
			task.setExecutorNode(Integer.toString(WebappHelper.getNodeId()));
			task.setExecutorBootId(WebappHelper.getBootId());
			task = dbInstance.getCurrentEntityManager().merge(task);
		}
		dbInstance.commit();
		return task;
	}
	
	public void taskDone(PersistentTask task) {
		task = dbInstance.getCurrentEntityManager().getReference(PersistentTask.class, task.getKey());
		dbInstance.getCurrentEntityManager().remove(task);
	}
	
	public void taskFailed(PersistentTask task) {
		task = dbInstance.getCurrentEntityManager()
				.find(PersistentTask.class, task.getKey(), LockModeType.PESSIMISTIC_WRITE);
		task.setStatus(TaskStatus.failed.name());
		dbInstance.commit();
	}
	
	public Runnable deserializeTask(PersistentTask task) {
		return (Runnable)xstream.fromXML(task.getTask());
	}
}
