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
package org.olat.upgrade;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_20_0_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_20_0_0.class);
	
	private static final String VERSION = "OLAT_20.0.0";

	private static final String UPDATE_CURRICULUM_ELEMENT = "UPDATE CURRICULUM ELEMENT";
	private static final String UPDATE_CURRICULUM_ELEMENT_RESOURCE = "UPDATE CURRICULUM ELEMENT RESOURCE";
	private static final String UPDATE_CURRICULUM_ELEMENT_NUMBERING = "UPDATE CURRICULUM ELEMENT NUMBERING";
	
	private static final int BATCH_SIZE = 1000;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OLATResourceManager olatResourceManager;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;

	public OLATUpgrade_20_0_0() {
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
		allOk &= updateCurriculumElement(upgradeManager, uhd);
		allOk &= updateCurriculumElementResource(upgradeManager, uhd);
		allOk &= updateCurriculumElementNumbering(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_20_0_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_20_0_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean updateCurriculumElement(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(UPDATE_CURRICULUM_ELEMENT)) {
			try {
				CurriculumElementType defaultType = curriculumService.getDefaultCurriculumElementType();
				if(defaultType == null) {
					allOk = false;
				} else {
					log.info("Start check of mandatory curriculum element type.");

					int counter = 0;
					List<CurriculumElement> elements;
					do {
						elements = getCurriculumElements(counter, BATCH_SIZE);
						if(!elements.isEmpty()) {
							for(int i=0; i<elements.size(); i++) {
								CurriculumElement element = elements.get(i);
								boolean update = false;
								if(element.getType() == null) {
									element.setType(defaultType);
									update = true;
								}
								if(element.getElementStatus() == CurriculumElementStatus.inactive) {
									((CurriculumElementImpl)element).setElementStatus(CurriculumElementStatus.finished);
									update = true;
								}
								if(update) {
									curriculumService.updateCurriculumElement(element);
								}
								
								if(i % 25 == 0) {
									dbInstance.commitAndCloseSession();
								}
							}
							counter += elements.size();
							log.info(Tracing.M_AUDIT, "Check type of {} curriculum elements", counter);
						}
						dbInstance.commitAndCloseSession();
					} while (!elements.isEmpty());
					
					log.info("Add default type to all curriculum elements.");
				}
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
		
			uhd.setBooleanDataValue(UPDATE_CURRICULUM_ELEMENT, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private boolean updateCurriculumElementResource(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(UPDATE_CURRICULUM_ELEMENT_RESOURCE)) {
			try {

				log.info("Start add resource to curriculum elements.");

				int counter = 0;
				int newResources = 0;
				List<CurriculumElement> elements;
				do {
					elements = getCurriculumElements(counter, BATCH_SIZE);
					if(!elements.isEmpty()) {
						for(int i=0; i<elements.size(); i++) {
							CurriculumElement element = elements.get(i);
							if(updateCurriculumElementResource(element)) {
								newResources++;
							}
							if(i % 25 == 0) {
								dbInstance.commitAndCloseSession();
							}
						}
						counter += elements.size();
						log.info(Tracing.M_AUDIT, "Check resource of {} curriculum elements", counter);
					}
					dbInstance.commitAndCloseSession();
				} while (!elements.isEmpty());
				
				log.info("Add resource to {} curriculum elements.", newResources);
		
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
		
			uhd.setBooleanDataValue(UPDATE_CURRICULUM_ELEMENT_RESOURCE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private boolean updateCurriculumElementResource(CurriculumElement element) {
		if(element.getResource() != null) return false;
			
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(CurriculumElement.class, element.getKey());
		OLATResource resource = olatResourceManager.findResourceable(ores);
		if(resource == null) {
			curriculumElementDao.createResource(element);
		} else {
			((CurriculumElementImpl)element).setResource(resource);
		}
		curriculumService.updateCurriculumElement(element);
		return true;
	}
	
	private List<CurriculumElement> getCurriculumElements(int offset, int maxResults) {
		String query = """
				select el from curriculumelement el
				left join fetch el.type curElementType
				left join fetch el.resource curElRes
				order by el.key""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, CurriculumElement.class)
				.setFirstResult(offset)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	private boolean updateCurriculumElementNumbering(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(UPDATE_CURRICULUM_ELEMENT_NUMBERING)) {
			try {
				List<CurriculumElement> elements = getCurriculumRootElements();
				log.info("Start numbering {} curriculum elements implementations.", elements.size());
				
				for(CurriculumElement element:elements) {
					curriculumService.numberRootCurriculumElement(element);
				}
				
				log.info("End numbering {} curriculum elements implementations.", elements.size());
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(UPDATE_CURRICULUM_ELEMENT_NUMBERING, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private List<CurriculumElement> getCurriculumRootElements() {
		String query = """
				select el from curriculumelement el
				where el.parent is null
				order by el.key""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, CurriculumElement.class)
				.getResultList();
	}
}
