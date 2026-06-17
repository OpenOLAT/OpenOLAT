/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.logging.Tracing;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeToType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.resource.accesscontrol.model.OfferImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 23 Apr. 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OLATUpgrade_21_0_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_21_0_0.class);

	private static final int BATCH_SIZE = 1000;
	private static final String VERSION = "OLAT_21.0.0";
	private static final String MIGRATE_OFFER_VALID_STATUS = "MIGRATE OFFER VALID STATUS";
	private static final String MIGRATE_CURRICULUM_ELEMENT_TYPE = "MIGRATE CURRICULUM ELEMENT TYPE";

	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			return false;
		}

		boolean allOk = true;
		allOk &= migrateOfferValidStatus(upgradeManager, uhd);
		allOk &= migrateElementTypes(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_21_0_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_21_0_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean migrateOfferValidStatus(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_OFFER_VALID_STATUS)) {
			try {
				log.info("Migration offer valid status");

				int count = 0;
				List<OfferImpl> offers = getOffersWithoutValidStatus(BATCH_SIZE);
				do {
					for (OfferImpl offer : offers) {
						if ("CurriculumElement".equals(offer.getResourceTypeName())) {
							offer.setValidStatus(Set.of("preparation", "provisional", "confirmed", "active"));
						} else {
							offer.setValidStatus(Set.of("preparation", "review", "coachpublished", "published"));
						}
						dbInstance.getCurrentEntityManager().merge(offer);
						if (count++ % 25 == 0) {
							dbInstance.commitAndCloseSession();
						}
					}
					dbInstance.commitAndCloseSession();
					offers = getOffersWithoutValidStatus(BATCH_SIZE);
				} while (!offers.isEmpty());

				log.info("End migration offer valid status: {}", count);
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(MIGRATE_OFFER_VALID_STATUS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private List<OfferImpl> getOffersWithoutValidStatus(int maxResults) {
		String query = """
				select offer from acoffer offer
				 where offer.validStatus is null
				   and (offer.validFrom is not null or offer.validTo is not null)
				 order by offer.key""";
		return dbInstance.getCurrentEntityManager().createQuery(query, OfferImpl.class)
				.setFirstResult(0)
				.setMaxResults(maxResults)
				.getResultList();
	}
	

	private boolean migrateElementTypes(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_CURRICULUM_ELEMENT_TYPE)) {
			try {
				log.info("Migration curriculum element types");

				List<CurriculumElementType> elementTypes = curriculumService.getCurriculumElementTypes();
				for(CurriculumElementType elementType:elementTypes) {
					// Don't change the default type and the managed types
					if("default-curriculum-element-type".equals(elementType.getIdentifier())
							|| elementType.getManagedFlags().length > 0) {
						continue;
					}
					
					// As implementation: implOnly true, allowedAsRootElement true
					// As element: implOnly false, allowedAsRootElement false
					// Else: implOnly false, allowedAsRootElement true
					if(!elementType.isImplOnly() && elementType.isAllowedAsRootElement()) {
						elementType = migrateElementTypeImplementationOrElement(elementType);
					}
					
					// Course bundle: maxRepositoryEntryRelations -1
					// No content: maxRepositoryEntryRelations 0
					// Else: maxRepositoryEntryRelations 1
					elementType = migrateElementTypeContent(elementType);
					
					elementType = migrateElementTypeAllowedChildren(elementType);
				}

				log.info("End migration curriculum element types: {}", elementTypes.size());
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(MIGRATE_CURRICULUM_ELEMENT_TYPE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private CurriculumElementType migrateElementTypeImplementationOrElement(CurriculumElementType elementType) {
		boolean hasImplementation = hasImplementation(elementType);
		boolean hasElement = hasElement(elementType);
		if(hasImplementation && !hasElement) {
			elementType.setImplOnly(true);
			elementType.setAllowedAsRootElement(true);
			elementType = dbInstance.getCurrentEntityManager().merge(elementType);
			log.info("Migrate {} to implementation ({} implementations, {} elements)", elementType.getDisplayName(), hasImplementation, hasElement);
		} else if(!hasImplementation && hasElement) {
			elementType.setImplOnly(false);
			elementType.setAllowedAsRootElement(false);
			elementType = dbInstance.getCurrentEntityManager().merge(elementType);
			log.info("Migrate {} to element ({} implementations, {} elements)", elementType.getDisplayName(), hasImplementation, hasElement);
		}
		dbInstance.commit();
		return elementType;
	}
	
	private boolean hasImplementation(CurriculumElementType elementType) {
		String query = """
				select count(el.key) from curriculumelement el
				where el.type.key=:elementTypeKey and el.parent.key is null""";
		
		List<Long> count = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("elementTypeKey", elementType.getKey())
				.getResultList();
		return count != null && count.size() > 0 && count.get(0) != null && count.get(0).longValue() > 0;
	}
	
	private boolean hasElement(CurriculumElementType elementType) {
		String query = """
				select count(el.key) from curriculumelement el
				where el.type.key=:elementTypeKey and el.parent.key is not null""";
		
		List<Long> count = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("elementTypeKey", elementType.getKey())
				.getResultList();
		return count != null && count.size() > 0 && count.get(0) != null && count.get(0).longValue() > 0;
	}
	
	private CurriculumElementType migrateElementTypeContent(CurriculumElementType elementType) {
		long maxNumberOfCourses = maxContent(elementType);
		
		if((elementType.getMaxRepositoryEntryRelations() == 0 || elementType.getMaxRepositoryEntryRelations() == 1)
				&& maxNumberOfCourses > 1) {
			elementType.setMaxRepositoryEntryRelations(-1);
			elementType = dbInstance.getCurrentEntityManager().merge(elementType);
			log.info("Migrate {} to course bundle ({} courses)", elementType.getDisplayName(), maxNumberOfCourses);
		} else if(elementType.getMaxRepositoryEntryRelations() == 0 && maxNumberOfCourses == 1) {
			elementType.setMaxRepositoryEntryRelations(1);
			elementType = dbInstance.getCurrentEntityManager().merge(elementType);
			log.info("Migrate {} to single course ({} course)", elementType.getDisplayName(), maxNumberOfCourses);
		}
		
		dbInstance.commit();
		return elementType;
	}
	
	private long maxContent(CurriculumElementType elementType) {
		String query = """
				select el.key, el.displayName, count(reToGroup.key) as entries from curriculumelement el
				inner join el.group as elGroup
				inner join repoentrytogroup as reToGroup on (reToGroup.group.key=elGroup.key)
				inner join reToGroup.entry as v
				where el.type.key=:elementTypeKey and v.status<>'deleted'
				group by el.key, el.displayName order by entries desc""";
		
		List<Object[]> count = dbInstance.getCurrentEntityManager()
				.createQuery(query, Object[].class)
				.setParameter("elementTypeKey", elementType.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		if(count == null || count.isEmpty()) {
			return 0l;
		}
		return PersistenceHelper.extractPrimitiveLong(count.get(0), 2);
	}
	
	private CurriculumElementType migrateElementTypeAllowedChildren(CurriculumElementType elementType) {
		List<CurriculumElementType> effectiveElementTypes = getChildElements(elementType);
		List<CurriculumElementType> definedElementTypes = elementType.getAllowedSubTypes().stream()
				.map(CurriculumElementTypeToType::getAllowedSubType)
				.toList();
		
		effectiveElementTypes.removeAll(definedElementTypes);
		effectiveElementTypes.remove(elementType);// Not itself
		if(!effectiveElementTypes.isEmpty()) {
			List<CurriculumElementType> updatedElementTypes = new ArrayList<>();
			updatedElementTypes.addAll(definedElementTypes);
			updatedElementTypes.addAll(effectiveElementTypes);
			elementType = curriculumService.updateCurriculumElementType(elementType, updatedElementTypes);
			for(CurriculumElementType effectiveElementType:effectiveElementTypes) {
				log.info("Migrate {}, add allowed child element {}", elementType.getDisplayName(), effectiveElementType.getDisplayName());
			}
			dbInstance.commit();
		}

		return elementType;
	}
	
	private List<CurriculumElementType> getChildElements(CurriculumElementType elementType) {
		String query = """
				select distinct elType from curriculumelement el
				inner join el.type as elType
				inner join el.parent as parentEl
				where parentEl.type.key=:elementTypeKey""";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, CurriculumElementType.class)
				.setParameter("elementTypeKey", elementType.getKey())
				.getResultList();
	}
}
