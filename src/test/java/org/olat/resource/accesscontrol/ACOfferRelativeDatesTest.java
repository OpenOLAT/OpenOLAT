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
package org.olat.resource.accesscontrol;

import static org.olat.test.JunitTestHelper.random;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.util.DateUtils;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.manager.ACOfferDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2026-04-22
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class ACOfferRelativeDatesTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private ACOfferDAO acOfferDao;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDao;

	@Test
	public void materializeDates_curriculumElement_afterBeginAfterEnd() {
		Date beginDate = DateUtils.addDays(new Date(), -10);
		Date endDate = DateUtils.addDays(new Date(), 10);
		CurriculumElement element = createCurriculumElement(beginDate, endDate);
		dbInstance.commitAndCloseSession();

		Offer offer = acService.createOffer(element.getResource(), random());
		offer.setValidDateConfig(dateConfig(5, OfferDateUnit.DAYS, OfferDateRef.AFTER_BEGIN, 3, OfferDateUnit.DAYS, OfferDateRef.AFTER_END));

		acService.materializeDates(offer);

		Assert.assertTrue(DateUtils.isSameDay(DateUtils.addDays(beginDate, 5), offer.getValidFrom()));
		Assert.assertTrue(DateUtils.isSameDay(DateUtils.addDays(endDate, 3), offer.getValidTo()));
	}

	@Test
	public void materializeDates_curriculumElement_beforeBeginBeforeEnd() {
		Date beginDate = DateUtils.addDays(new Date(), -10);
		Date endDate = DateUtils.addDays(new Date(), 10);
		CurriculumElement element = createCurriculumElement(beginDate, endDate);
		dbInstance.commitAndCloseSession();

		Offer offer = acService.createOffer(element.getResource(), random());
		offer.setValidDateConfig(dateConfig(7, OfferDateUnit.DAYS, OfferDateRef.BEFORE_BEGIN, 2, OfferDateUnit.DAYS, OfferDateRef.BEFORE_END));

		acService.materializeDates(offer);

		Assert.assertTrue(DateUtils.isSameDay(DateUtils.addDays(beginDate, -7), offer.getValidFrom()));
		Assert.assertTrue(DateUtils.isSameDay(DateUtils.addDays(endDate, -2), offer.getValidTo()));
	}

	@Test
	public void materializeDates_curriculumElement_afterEndBeforeBegin() {
		Date beginDate = DateUtils.addDays(new Date(), -10);
		Date endDate = DateUtils.addDays(new Date(), 10);
		CurriculumElement element = createCurriculumElement(beginDate, endDate);
		dbInstance.commitAndCloseSession();

		Offer offer = acService.createOffer(element.getResource(), random());
		offer.setValidDateConfig(dateConfig(3, OfferDateUnit.DAYS, OfferDateRef.AFTER_END, 4, OfferDateUnit.DAYS, OfferDateRef.BEFORE_BEGIN));

		acService.materializeDates(offer);

		Assert.assertTrue(DateUtils.isSameDay(DateUtils.addDays(endDate, 3), offer.getValidFrom()));
		Assert.assertTrue(DateUtils.isSameDay(DateUtils.addDays(beginDate, -4), offer.getValidTo()));
	}

	@Test
	public void materializeDates_curriculumElement_sameDay() {
		Date beginDate = DateUtils.addDays(new Date(), -10);
		Date endDate = DateUtils.addDays(new Date(), 10);
		CurriculumElement element = createCurriculumElement(beginDate, endDate);
		dbInstance.commitAndCloseSession();

		Offer offer = acService.createOffer(element.getResource(), random());
		offer.setValidDateConfig(dateConfig(0, OfferDateUnit.SAME_DAY, OfferDateRef.AFTER_BEGIN, 0, OfferDateUnit.SAME_DAY, OfferDateRef.AFTER_END));

		acService.materializeDates(offer);

		Assert.assertTrue(DateUtils.isSameDay(beginDate, offer.getValidFrom()));
		Assert.assertTrue(DateUtils.isSameDay(endDate, offer.getValidTo()));
	}

	@Test
	public void materializeDates_curriculumElement_noBeginDate() {
		CurriculumElement element = createCurriculumElement(null, null);
		dbInstance.commitAndCloseSession();

		Offer offer = acService.createOffer(element.getResource(), random());
		offer.setValidDateConfig(dateConfig(5, OfferDateUnit.DAYS, OfferDateRef.AFTER_BEGIN, 5, OfferDateUnit.DAYS, OfferDateRef.AFTER_END));

		acService.materializeDates(offer);

		Assert.assertNull(offer.getValidFrom());
		Assert.assertNull(offer.getValidTo());
	}

	@Test
	public void materializeDates_repositoryEntry_withLifecycle() {
		Date lifecycleFrom = DateUtils.addDays(new Date(), -5);
		Date lifecycleTo = DateUtils.addDays(new Date(), 5);
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntryLifecycle lifecycle = lifecycleDao.create(random(), random(), true, lifecycleFrom, lifecycleTo);
		repositoryManager.setLocationAndLifecycle(re, null, lifecycle);
		dbInstance.commitAndCloseSession();

		Offer offer = acService.createOffer(re.getOlatResource(), random());
		offer.setValidDateConfig(dateConfig(3, OfferDateUnit.DAYS, OfferDateRef.AFTER_BEGIN, 1, OfferDateUnit.DAYS, OfferDateRef.BEFORE_END));

		acService.materializeDates(offer);

		Assert.assertTrue(DateUtils.isSameDay(DateUtils.addDays(lifecycleFrom, 3), offer.getValidFrom()));
		Assert.assertTrue(DateUtils.isSameDay(DateUtils.addDays(lifecycleTo, -1), offer.getValidTo()));
	}

	@Test
	public void materializeDates_repositoryEntry_noLifecycle() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();

		Offer offer = acService.createOffer(re.getOlatResource(), random());
		offer.setValidDateConfig(dateConfig(3, OfferDateUnit.DAYS, OfferDateRef.AFTER_BEGIN, 3, OfferDateUnit.DAYS, OfferDateRef.AFTER_END));

		acService.materializeDates(offer);

		Assert.assertNull(offer.getValidFrom());
		Assert.assertNull(offer.getValidTo());
	}

	@Test
	public void materializeDates_absoluteMode_unchanged() {
		Date absoluteFrom = DateUtils.addDays(new Date(), -3);
		Date absoluteTo = DateUtils.addDays(new Date(), 3);
		CurriculumElement element = createCurriculumElement(new Date(), new Date());
		dbInstance.commitAndCloseSession();

		Offer offer = acService.createOffer(element.getResource(), random());
		offer.setValidFrom(absoluteFrom);
		offer.setValidTo(absoluteTo);

		acService.materializeDates(offer);

		Assert.assertTrue(DateUtils.isSameDay(absoluteFrom, offer.getValidFrom()));
		Assert.assertTrue(DateUtils.isSameDay(absoluteTo, offer.getValidTo()));
	}

	@Test
	public void materializeDates_invalidOffer_unchanged() {
		Date beginDate = DateUtils.addDays(new Date(), -10);
		CurriculumElement element = createCurriculumElement(beginDate, null);
		dbInstance.commitAndCloseSession();

		Offer offer = acService.createOffer(element.getResource(), random());
		offer.setValidDateConfig(dateConfig(5, OfferDateUnit.DAYS, OfferDateRef.AFTER_BEGIN, null, null, null));
		offer.setValidFrom(null);
		((org.olat.resource.accesscontrol.model.OfferImpl) offer).setValid(false);

		acService.materializeDates(offer);

		Assert.assertNull(offer.getValidFrom());
	}

	@Test
	public void loadRelativeDateOffers_returnsOnlyRelative() {
		CurriculumElement element = createCurriculumElement(new Date(), new Date());

		Offer absoluteOffer = acService.createOffer(element.getResource(), random());
		absoluteOffer = acService.save(absoluteOffer);

		Offer relativeOffer = acService.createOffer(element.getResource(), random());
		relativeOffer.setValidDateConfig(dateConfig(5, OfferDateUnit.DAYS, OfferDateRef.AFTER_BEGIN, null, null, null));
		relativeOffer = acService.save(relativeOffer);

		dbInstance.commitAndCloseSession();

		List<Offer> offers = acOfferDao.loadRelativeDateOffers(List.of(element.getResource()));

		Assert.assertFalse(offers.contains(absoluteOffer));
		Assert.assertTrue(offers.contains(relativeOffer));
	}

	@Test
	public void loadRelativeDateOffers_excludesInvalidOffers() {
		CurriculumElement element = createCurriculumElement(new Date(), new Date());

		Offer relativeOffer = acService.createOffer(element.getResource(), random());
		relativeOffer.setValidDateConfig(dateConfig(5, OfferDateUnit.DAYS, OfferDateRef.AFTER_BEGIN, null, null, null));
		relativeOffer = acService.save(relativeOffer);
		acService.deleteOffer(relativeOffer);

		dbInstance.commitAndCloseSession();

		List<Offer> offers = acOfferDao.loadRelativeDateOffers(List.of(element.getResource()));

		Assert.assertFalse(offers.contains(relativeOffer));
	}

	@Test
	public void recomputeOffersForResource_updatesOfferWhenCeDatesChange() {
		Date initialBeginDate = DateUtils.addDays(new Date(), -10);
		Date initialEndDate = DateUtils.addDays(new Date(), 10);
		CurriculumElement element = createCurriculumElement(initialBeginDate, initialEndDate);

		Offer offer = acService.createOffer(element.getResource(), random());
		offer.setValidDateConfig(dateConfig(5, OfferDateUnit.DAYS, OfferDateRef.AFTER_BEGIN, 2, OfferDateUnit.DAYS, OfferDateRef.BEFORE_END));
		offer = acService.save(offer);
		dbInstance.commitAndCloseSession();

		Assert.assertTrue(DateUtils.isSameDay(DateUtils.addDays(initialBeginDate, 5), offer.getValidFrom()));
		Assert.assertTrue(DateUtils.isSameDay(DateUtils.addDays(initialEndDate, -2), offer.getValidTo()));

		Date newBeginDate = DateUtils.addDays(new Date(), -3);
		Date newEndDate = DateUtils.addDays(new Date(), 20);
		element = curriculumService.getCurriculumElement(element);
		element.setBeginDate(newBeginDate);
		element.setEndDate(newEndDate);
		curriculumService.updateCurriculumElement(element);
		dbInstance.commitAndCloseSession();

		List<Offer> offers = acOfferDao.loadRelativeDateOffers(List.of(element.getResource()));
		Assert.assertEquals(1, offers.size());
		Assert.assertTrue(DateUtils.isSameDay(DateUtils.addDays(newBeginDate, 5), offers.get(0).getValidFrom()));
		Assert.assertTrue(DateUtils.isSameDay(DateUtils.addDays(newEndDate, -2), offers.get(0).getValidTo()));
	}

	@Test
	public void updateRelativeValidDates_updatesOfferWhenLifecycleDatesChange() {
		Date initialFrom = DateUtils.addDays(new Date(), -5);
		Date initialTo = DateUtils.addDays(new Date(), 5);
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntryLifecycle lifecycle = lifecycleDao.create(random(), random(), false, initialFrom, initialTo);
		repositoryManager.setLocationAndLifecycle(re, null, lifecycle);
		dbInstance.commitAndCloseSession();

		Offer offer = acService.createOffer(re.getOlatResource(), random());
		offer.setValidDateConfig(dateConfig(3, OfferDateUnit.DAYS, OfferDateRef.AFTER_BEGIN, 1, OfferDateUnit.DAYS, OfferDateRef.BEFORE_END));
		offer = acService.save(offer);
		dbInstance.commitAndCloseSession();

		Assert.assertTrue(DateUtils.isSameDay(DateUtils.addDays(initialFrom, 3), offer.getValidFrom()));
		Assert.assertTrue(DateUtils.isSameDay(DateUtils.addDays(initialTo, -1), offer.getValidTo()));

		Date newFrom = DateUtils.addDays(new Date(), -2);
		Date newTo = DateUtils.addDays(new Date(), 15);
		lifecycle.setValidFrom(newFrom);
		lifecycle.setValidTo(newTo);
		lifecycleDao.updateLifecycle(lifecycle);
		dbInstance.commitAndCloseSession();

		List<OLATResource> resources = lifecycleDao.loadResources(lifecycle);
		acService.updateRelativeValidDates(resources, newFrom, newTo);
		dbInstance.commitAndCloseSession();

		List<Offer> offers = acOfferDao.loadRelativeDateOffers(List.of(re.getOlatResource()));
		Assert.assertEquals(1, offers.size());
		Assert.assertTrue(DateUtils.isSameDay(DateUtils.addDays(newFrom, 3), offers.get(0).getValidFrom()));
		Assert.assertTrue(DateUtils.isSameDay(DateUtils.addDays(newTo, -1), offers.get(0).getValidTo()));
	}

	private static OfferDateConfig dateConfig(Integer fromValue, OfferDateUnit fromUnit, OfferDateRef fromRef,
			Integer toValue, OfferDateUnit toUnit, OfferDateRef toRef) {
		OfferDateConfig config = new OfferDateConfig();
		config.setFromValue(fromValue);
		config.setFromUnit(fromUnit);
		config.setFromRef(fromRef);
		config.setToValue(toValue);
		config.setToUnit(toUnit);
		config.setToRef(toRef);
		return config;
	}

	private CurriculumElement createCurriculumElement(Date beginDate, Date endDate) {
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, null);
		return curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, beginDate, endDate,
				null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
	}
}
