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
package org.olat.upgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.persistence.DB;
import org.olat.upgrade.model.CurriculumElementPos;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_13_2_0 extends OLATUpgrade {
	
	private static final String VERSION = "OLAT_13.2.0";
	private static final String UPDATE_CURRICULUM_CHILDREN = "UPDATE CURRICULUM CHILDREN";
	
	@Autowired
	private DB dbInstance;
	
	public OLATUpgrade_13_2_0() {
		super();
	}
	
	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			return false;
		}
		
		boolean allOk = true;
		allOk &= updateCurriculumElementChildren(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_13_2_0 successfully!");
		} else {
			log.audit("OLATUpgrade_13_2_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	/**
	 * Recalculate pos of children
	 * 
	 * @param upgradeManager The upgrade manager
	 * @param uhd The upgrade history
	 * @return true if successful
	 */
	private boolean updateCurriculumElementChildren(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(UPDATE_CURRICULUM_CHILDREN)) {
			List<CurriculumElementPos> elements = getCurriculumElements();
			Map<Long, List<CurriculumElementPos>> parentToChildren = new HashMap<>();
			Map<Long, List<CurriculumElementPos>> curriculumToChildren = new HashMap<>();
			
			// pre warm the maps
			for(CurriculumElementPos element:elements) {
				parentToChildren.put(element.getKey(), new ArrayList<>());
			}
			for(CurriculumElementPos element:elements) {
				curriculumToChildren.put(element.getCurriculumKey(), new ArrayList<>());
			}
			
			// fill the data
			for(CurriculumElementPos element:elements) {
				if(element.getParentKey() != null) {
					parentToChildren.get(element.getParentKey()).add(element);
				} else {
					curriculumToChildren.get(element.getCurriculumKey()).add(element);
				}
			}
			
			// reorder curriculum elements
			for(List<CurriculumElementPos> children:parentToChildren.values()) {
				if(children.isEmpty()) continue;
				// reorder the children
				Collections.sort(children, new PosComparator());	
				// assign a position
				for(int i=0; i<children.size(); i++) {
					children.get(i).setPos(Long.valueOf(i));
				}
				// merge
				for(CurriculumElementPos child:children) {
					dbInstance.getCurrentEntityManager().merge(child);
				}
				dbInstance.commit();
			}
			dbInstance.commitAndCloseSession();
			
			// reorder root elements 
			for(List<CurriculumElementPos> children:curriculumToChildren.values()) {
				if(children.isEmpty()) continue;
				// reorder the roots
				Collections.sort(children, new PosComparator());	
				// assign a position under curriculum
				for(int i=0; i<children.size(); i++) {
					children.get(i).setPosCurriculum(Long.valueOf(i));
					children.get(i).setParentCurriculumKey(children.get(i).getCurriculumKey());
				}
				// merge
				for(CurriculumElementPos child:children) {
					dbInstance.getCurrentEntityManager().merge(child);
				}
				dbInstance.commit();
			}
			dbInstance.commitAndCloseSession();
			

			uhd.setBooleanDataValue(UPDATE_CURRICULUM_CHILDREN, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private List<CurriculumElementPos> getCurriculumElements() {
		String q = "select curEl from curriculumelementpos as curEl";
		return dbInstance.getCurrentEntityManager()
			.createQuery(q, CurriculumElementPos.class)
			.getResultList();
	}
	
	private static class PosComparator implements Comparator<CurriculumElementPos> {
		@Override
		public int compare(CurriculumElementPos o1, CurriculumElementPos o2) {
			Long p1 = o1.getPos();
			Long p2 = o2.getPos();
			
			int c = 0;
			if(p1 != null && p2 != null) {
				c = p1.compareTo(p2);
			} else if(p1 != null) {
				c = 1;
			} else if(p2 != null) {
				c = -1;
			}
			if(c == 0) {
				c = o1.getKey().compareTo(o2.getKey());
			}
			return c;
		}	
	}
}
