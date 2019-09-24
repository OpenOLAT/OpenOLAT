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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.olat.modules.cl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.resource.OresHelper;

/**
 * Description:<br>
 * Manager for loading, saving and updating checklists and checkpoints.
 * 
 * <P>
 * Initial Date:  30.07.2009 <br>
 * @author bja <bja@bps-system.de>
 * @author skoeber <skoeber@bps-system.de>
 */
public class ChecklistManager {
	
	/** singleton */
	private static ChecklistManager INSTANCE = new ChecklistManager();
	
	private ChecklistManager() {
		// constructor
	}
	
	public static ChecklistManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Load checklist.
	 * @param checklist
	 * @return checklist
	 */
	public Checklist loadChecklist(Checklist cl) {
		Checklist checklist;
		try {
			// load from db
			checklist = (Checklist) DBFactory.getInstance().loadObject(cl);
		} catch (Exception e) {
			DBFactory.getInstance().closeSession();
			// in case of error create new object as fallback
			checklist = new Checklist();
		}
		return checklist;
	}
	
	/**
	 * Load checklist
	 * @param key
	 * @return checklist
	 */
	public Checklist loadChecklist(Long key) {
		Checklist checklist;
		try {
			// load from db
			checklist = DBFactory.getInstance().getCurrentEntityManager().find(Checklist.class, key);
		} catch (Exception e) {
			DBFactory.getInstance().closeSession();
			// in case of error create new object as fallback
			checklist = new Checklist();
		}
		return checklist;
	}
		
	/**
	 * Save new checklist.
	 * @param checklist
	 */
	public Checklist saveChecklist(Checklist cl) {
		cl.setLastModified(new Date());
		if(cl.getKey() == null) {
			DBFactory.getInstance().getCurrentEntityManager().persist(cl);
		} else {
			cl = DBFactory.getInstance().getCurrentEntityManager().merge(cl);
		}
		return cl;
	}
	
	/**
	 * Update checklist.
	 * @param checklist
	 */
	public Checklist updateChecklist(final Checklist cl) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Checklist.class, cl.getKey());
		return CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerCallback<Checklist>() {
			public Checklist execute() {
				cl.setLastModified(new Date());
				return DBFactory.getInstance().getCurrentEntityManager().merge(cl);
			}
		});
	}
	
	/**
	 * Delete checklist.
	 * @param checklist
	 */
	public void deleteChecklist(final Checklist cl) {
		final DB db = DBFactory.getInstance();
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Checklist.class, cl.getKey());
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerExecutor() {
				public void execute() {
					Checklist checklist = (Checklist) db.loadObject(cl);
					db.deleteObject(checklist);
				}
		});
	}

	/**
	 * Update checkpoint
	 * @param checkpoint
	 */
	public void updateCheckpoint(final Checkpoint cp) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Checkpoint.class, cp.getKey());
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerExecutor() {
			public void execute() {
				cp.setLastModified(new Date());
				DBFactory.getInstance().updateObject(cp);
			}
		});
	}
	
	/**
	 * Copy checklist without user data and results. Only in RAM, checklist will not be persisted.
	 * @param checklist to copy
	 * @return the new checklist
	 */
	public Checklist copyChecklistInRAM(final Checklist cl) {
		Checklist clCopy = new Checklist();
		clCopy.setTitle(cl.getTitle());
		clCopy.setDescription(cl.getDescription());
		List<Checkpoint> checkpoints = cl.getCheckpoints();
		List<Checkpoint> checkpointsCopy = new ArrayList<>();
		for(Checkpoint cp : checkpoints) {
			Checkpoint cpCopy = new Checkpoint();
			cpCopy.setChecklist(clCopy);
			cpCopy.setTitle(cp.getTitle());
			cpCopy.setDescription(cp.getDescription());
			cpCopy.setMode(cp.getMode());
			cpCopy.setLastModified(new Date());
			checkpointsCopy.add(cpCopy);
		}
		clCopy.setCheckpoints(checkpointsCopy);
		
		return clCopy;
	}
	
	/**
	 * Copy checklist without user data and results and save it.
	 * @param checklist to copy
	 * @return the new persisted checklist
	 */
	public Checklist copyChecklist(final Checklist cl) {
		Checklist clCopy = copyChecklistInRAM(cl);
		saveChecklist(clCopy);
		
		return clCopy;
	}
	
}
