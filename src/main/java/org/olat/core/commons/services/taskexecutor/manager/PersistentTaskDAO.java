/**
12 * <a href="http://www.openolat.org">
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
package org.olat.core.commons.services.taskexecutor.manager;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.LockModeType;
import javax.persistence.TemporalType;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.taskexecutor.Task;
import org.olat.core.commons.services.taskexecutor.TaskStatus;
import org.olat.core.commons.services.taskexecutor.model.PersistentTask;
import org.olat.core.commons.services.taskexecutor.model.PersistentTaskModifier;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.resource.OLATResource;
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
	static {
		XStreamHelper.allowDefaultPackage(xstream);
	}
	
	@Autowired
	private DB dbInstance;

	public PersistentTask createTask(String name, Serializable task) {
		PersistentTask ptask = new PersistentTask();
		Date currentDate = new Date();
		ptask.setCreationDate(currentDate);
		ptask.setLastModified(currentDate);
		ptask.setName(name);
		ptask.setStatus(TaskStatus.newTask);
		ptask.setTask(toXML(task));
		dbInstance.getCurrentEntityManager().persist(ptask);
		return ptask;
	}
	
	public PersistentTask createTask(String name, Serializable task,
			Identity creator, OLATResource resource, String resSubPath, Date scheduledDate) {
		PersistentTask ptask = new PersistentTask();
		Date currentDate = new Date();
		ptask.setCreationDate(currentDate);
		ptask.setLastModified(currentDate);
		ptask.setScheduledDate(scheduledDate);
		ptask.setName(name);
		ptask.setCreator(creator);
		ptask.setResource(resource);
		ptask.setResSubPath(resSubPath);
		ptask.setStatus(TaskStatus.newTask);
		ptask.setTask(toXML(task));
		dbInstance.getCurrentEntityManager().persist(ptask);
		return ptask;
	}
	
	public List<Long> tasksToDo() {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("taskToDos", Long.class)
				.setParameter("executorBootId", WebappHelper.getBootId())
				.setParameter("executorNode", Integer.toString(WebappHelper.getNodeId()))
				.setParameter("currentDate", new Date(), TemporalType.TIMESTAMP)
				.getResultList();
	}
	
	public List<Task> findTasks(OLATResource resource) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadTaskByResource", Task.class)
				.setParameter("resourceKey", resource.getKey())
				.getResultList();
	}
	
	public List<Task> findTasks(OLATResource resource, String resSubPath) {
		StringBuilder sb = new StringBuilder();
		sb.append("select task from extask task where task.resource.key=:resourceKey and task.resSubPath=:resSubPath");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Task.class)
				.setParameter("resourceKey", resource.getKey())
				.setParameter("resSubPath", resSubPath)
				.getResultList();
	}
	
	public PersistentTask loadTaskById(Long taskKey) {
		return dbInstance.getCurrentEntityManager().find(PersistentTask.class, taskKey);
	}
	
	public synchronized PersistentTask pickTaskForRun(PersistentTask task) {
		if(task != null) {// remove it from the cache
			dbInstance.getCurrentEntityManager().detach(task);
		} else {
			return null;
		}

		task = dbInstance.getCurrentEntityManager()
				.find(PersistentTask.class, task.getKey(), LockModeType.PESSIMISTIC_WRITE);
		if(task != null) {
			if(TaskStatus.newTask.equals(task.getStatus())) {
				task.setStatus(TaskStatus.inWork);
				task.setExecutorNode(Integer.toString(WebappHelper.getNodeId()));
				task.setExecutorBootId(WebappHelper.getBootId());
				task = dbInstance.getCurrentEntityManager().merge(task);
			} else if(TaskStatus.inWork.equals(task.getStatus())) {
				if(WebappHelper.getBootId().equals(task.getExecutorBootId())) {
					// someone has already pick it
					task = null;
				} else {
					// reboot of a task in work
					task.setExecutorNode(Integer.toString(WebappHelper.getNodeId()));
					task.setExecutorBootId(WebappHelper.getBootId());
					task = dbInstance.getCurrentEntityManager().merge(task);
				}
			} else if(TaskStatus.edition.equals(task.getStatus()) || TaskStatus.cancelled.equals(task.getStatus())) {
				task = null;
			}
		}
		dbInstance.commit();
		return task;
	}
	
	public PersistentTask pickTaskForEdition(Long taskKey) {
		PersistentTask task = dbInstance.getCurrentEntityManager()
				.find(PersistentTask.class, taskKey, LockModeType.PESSIMISTIC_WRITE);
		
		PersistentTask mtask;
		if(TaskStatus.inWork.equals(task.getStatus()) || TaskStatus.edition.equals(task.getStatus())) {
			mtask = null;//cannot pick
		} else {
			task.setStatusBeforeEditStr(task.getStatusStr());
			task.setStatus(TaskStatus.edition);
			mtask = dbInstance.getCurrentEntityManager().merge(task);
		}
		dbInstance.commit();
		return mtask;
	}
	
	public PersistentTask returnTaskAfterEdition(Long taskKey, TaskStatus wishedStatus) {
		PersistentTask task = dbInstance.getCurrentEntityManager()
				.find(PersistentTask.class, taskKey, LockModeType.PESSIMISTIC_WRITE);
		
		PersistentTask mtask;
		if(TaskStatus.inWork.equals(task.getStatus())) {
			mtask = null;//cannot pick
		} else {
			if(wishedStatus == null) {
				task.setStatusStr(task.getStatusBeforeEditStr());
				
			} else {
				task.setStatus(wishedStatus);
			}
			task.setStatusBeforeEditStr(null);
			mtask = dbInstance.getCurrentEntityManager().merge(task);
		}
		dbInstance.commit();
		return mtask;
	}
	
	public PersistentTask updateProgressTask(Task task, Double progress, String checkpoint) {
		PersistentTask ptask = dbInstance.getCurrentEntityManager()
				.find(PersistentTask.class, task.getKey(), LockModeType.PESSIMISTIC_WRITE);
		if(ptask != null) {
			ptask.setLastModified(new Date());
			ptask.setProgress(progress);
			ptask.setCheckpoint(checkpoint);
			dbInstance.commit();
		}
		return ptask;
	}
	
	public Double getProgress(Task task) {
		StringBuilder sb = new StringBuilder();
		sb.append("select task.progress from extask task where task.key=:taskKey");
		List<Double> progressList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Double.class)
				.setParameter("taskKey", task.getKey())
				.getResultList();
		return progressList != null && progressList.size() == 1 ? progressList.get(0) : null;
	}
	
	public TaskStatus getStatus(Task task) {
		StringBuilder sb = new StringBuilder();
		sb.append("select task.statusStr from extask task where task.key=:taskKey");
		List<String> progressList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setParameter("taskKey", task.getKey())
				.getResultList();
		return progressList != null && progressList.size() == 1 && StringHelper.containsNonWhitespace(progressList.get(0))
				? TaskStatus.valueOf(progressList.get(0)) : null;
	}
	
	public PersistentTask updateTask(Task task, Serializable runnableTask, Identity modifier, Date scheduledDate) {
		PersistentTask ptask = dbInstance.getCurrentEntityManager()
				.find(PersistentTask.class, task.getKey(), LockModeType.PESSIMISTIC_WRITE);
		if(ptask != null) {
			ptask.setLastModified(new Date());
			ptask.setScheduledDate(scheduledDate);
			ptask.setStatus(TaskStatus.newTask);
			ptask.setStatusBeforeEditStr(null);
			ptask.setTask(toXML(runnableTask));

			ptask = dbInstance.getCurrentEntityManager().merge(ptask);
			if(modifier != null) {
				//add to the list of modifier
				PersistentTaskModifier mod = new PersistentTaskModifier();
				mod.setCreationDate(new Date());
				mod.setModifier(modifier);
				mod.setTask(ptask);
				dbInstance.getCurrentEntityManager().persist(mod);
			}
			dbInstance.commit();
		}
		return ptask;
	}
	
	public List<Identity> getModifiers(Task task) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadTaskModifiers", Identity.class)
				.setParameter("taskKey", task.getKey())
				.getResultList();
	}
	
	public boolean delete(Task task) {
		PersistentTask reloadedTask = dbInstance.getCurrentEntityManager()
				.find(PersistentTask.class, task.getKey(), LockModeType.PESSIMISTIC_WRITE);
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from extaskmodifier taskmod where taskmod.task.key=:taskKey")
				.setParameter("taskKey", task.getKey())
				.executeUpdate();
		dbInstance.getCurrentEntityManager().remove(reloadedTask);
		dbInstance.commit();
		return true;
	}
	
	public void delete(OLATResource resource) {
		List<Task> tasksToDelete = findTasks(resource);
		for(Task taskToDelete:tasksToDelete) {
			delete(taskToDelete);
		}
	}
	
	public void delete(OLATResource resource, String resSubPath) {
		List<Task> tasksToDelete = findTasks(resource, resSubPath);
		for(Task taskToDelete:tasksToDelete) {
			delete(taskToDelete);
		}
	}
	
	public void taskDone(PersistentTask task) {
		delete(task);
	}
	
	public void taskFailed(PersistentTask task) {
		task = dbInstance.getCurrentEntityManager()
				.find(PersistentTask.class, task.getKey(), LockModeType.PESSIMISTIC_WRITE);
		task.setStatus(TaskStatus.failed);
		dbInstance.commit();
	}
	
	public void taskCancelled(PersistentTask task) {
		task = dbInstance.getCurrentEntityManager()
				.find(PersistentTask.class, task.getKey(), LockModeType.PESSIMISTIC_WRITE);
		task.setStatus(TaskStatus.cancelled);
		dbInstance.commit();
	}
	
	protected static String toXML(Serializable task) {
		return xstream.toXML(task);
	}
	
	public Runnable deserializeTask(PersistentTask task) {
		return (Runnable)xstream.fromXML(task.getTask());
	}
}
