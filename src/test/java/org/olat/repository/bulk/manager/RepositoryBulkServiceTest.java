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
package org.olat.repository.bulk.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.course.CourseFactory;
import org.olat.course.config.CourseConfig;
import org.olat.modules.curriculum.TaughtBy;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.manager.TaxonomyDAO;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.bulk.RepositoryBulkService;
import org.olat.repository.bulk.SettingsBulkEditable;
import org.olat.repository.bulk.model.SettingsContext;
import org.olat.repository.bulk.model.SettingsContext.LifecycleType;
import org.olat.repository.manager.RepositoryEntryEducationalTypeDAO;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 15 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryBulkServiceTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryBulkService repositoryBulkService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryEntryEducationalTypeDAO educationalTypeDao;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private RepositoryEntryLicenseHandler licenseHandler;

	@Test
	public void shouldUpdateMetadata() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("bulk-meta-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		RepositoryEntryEducationalType educationalType = educationalTypeDao.create(random());
		dbInstance.commitAndCloseSession();

		SettingsContext context = new SettingsContext(List.of(entry));
		context.select(SettingsBulkEditable.authors, true);
		context.setAuthors("Test Authors");
		context.select(SettingsBulkEditable.educationalType, true);
		context.setEducationalTypeKey(educationalType.getKey());
		context.select(SettingsBulkEditable.mainLanguage, true);
		context.setMainLanguage("Rumantsch");
		context.select(SettingsBulkEditable.expenditureOfWork, true);
		context.setExpenditureOfWork("4h");
		context.select(SettingsBulkEditable.location, true);
		context.setLocation("Zurich");
		repositoryBulkService.update(null, author, context);
		dbInstance.commitAndCloseSession();

		RepositoryEntry reloaded = repositoryService.loadByKey(entry.getKey());
		assertThat(reloaded.getAuthors()).isEqualTo("Test Authors");
		assertThat(reloaded.getEducationalType().getKey()).isEqualTo(educationalType.getKey());
		assertThat(reloaded.getMainLanguage()).isEqualTo("Rumantsch");
		assertThat(reloaded.getExpenditureOfWork()).isEqualTo("4h");
		assertThat(reloaded.getLocation()).isEqualTo("Zurich");
	}

	@Test
	public void shouldUpdateAuthorRightsAndOerPub() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("bulk-rights-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		dbInstance.commitAndCloseSession();
		boolean canReference = entry.getCanReference();
		boolean canCopy = entry.getCanCopy();
		boolean canDownload = entry.getCanDownload();
		boolean canIndexMetadata = entry.getCanIndexMetadata();

		SettingsContext context = new SettingsContext(List.of(entry));
		context.select(SettingsBulkEditable.authorRightReference, true);
		context.setAuthorRightReference(!canReference);
		context.select(SettingsBulkEditable.authorRightCopy, true);
		context.setAuthorRightCopy(!canCopy);
		context.select(SettingsBulkEditable.authorRightDownload, true);
		context.setAuthorRightDownload(!canDownload);
		context.select(SettingsBulkEditable.oerPub, true);
		context.setCanIndexMetadata(!canIndexMetadata);
		repositoryBulkService.update(null, author, context);
		dbInstance.commitAndCloseSession();

		RepositoryEntry reloaded = repositoryService.loadByKey(entry.getKey());
		assertThat(reloaded.getCanReference()).isEqualTo(!canReference);
		assertThat(reloaded.getCanCopy()).isEqualTo(!canCopy);
		assertThat(reloaded.getCanDownload()).isEqualTo(!canDownload);
		assertThat(reloaded.getCanIndexMetadata()).isEqualTo(!canIndexMetadata);
	}

	@Test
	public void shouldUpdateLifecycle() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("bulk-cycle-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		dbInstance.commitAndCloseSession();

		Date validFrom = new GregorianCalendar(2026, 8, 1).getTime();
		Date validTo = new GregorianCalendar(2026, 11, 31).getTime();
		SettingsContext privateContext = new SettingsContext(List.of(entry));
		privateContext.select(SettingsBulkEditable.lifecycleType, true);
		privateContext.setLifecycleType(LifecycleType.privateCycle);
		privateContext.setLifecycleValidFrom(validFrom);
		privateContext.setLifecycleValidTo(validTo);
		repositoryBulkService.update(null, author, privateContext);
		dbInstance.commitAndCloseSession();

		RepositoryEntry reloaded = repositoryService.loadByKey(entry.getKey());
		assertThat(reloaded.getLifecycle()).isNotNull();
		assertThat(reloaded.getLifecycle().isPrivateCycle()).isTrue();
		assertThat(reloaded.getLifecycle().getValidFrom()).hasSameTimeAs(validFrom);
		assertThat(reloaded.getLifecycle().getValidTo()).hasSameTimeAs(validTo);

		RepositoryEntryLifecycle publicCycle = lifecycleDao.create(random(), random(), false, validFrom, validTo);
		dbInstance.commitAndCloseSession();
		SettingsContext publicContext = new SettingsContext(List.of(reloaded));
		publicContext.select(SettingsBulkEditable.lifecycleType, true);
		publicContext.setLifecycleType(LifecycleType.publicCycle);
		publicContext.setLifecyclePublicKey(publicCycle.getKey());
		repositoryBulkService.update(null, author, publicContext);
		dbInstance.commitAndCloseSession();

		reloaded = repositoryService.loadByKey(entry.getKey());
		assertThat(reloaded.getLifecycle().getKey()).isEqualTo(publicCycle.getKey());

		SettingsContext noneContext = new SettingsContext(List.of(reloaded));
		noneContext.select(SettingsBulkEditable.lifecycleType, true);
		noneContext.setLifecycleType(LifecycleType.none);
		repositoryBulkService.update(null, author, noneContext);
		dbInstance.commitAndCloseSession();

		reloaded = repositoryService.loadByKey(entry.getKey());
		assertThat(reloaded.getLifecycle()).isNull();
	}

	@Test
	public void shouldUpdateOrganisations() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("bulk-org-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		Organisation organisation = organisationService.createOrganisation(random(), random(), null, null, null, author);
		dbInstance.commitAndCloseSession();

		SettingsContext addContext = new SettingsContext(List.of(entry));
		addContext.select(SettingsBulkEditable.organisationsAdd, true);
		addContext.setOrganisationAddKeys(Set.of(organisation.getKey()));
		repositoryBulkService.update(null, author, addContext);
		dbInstance.commitAndCloseSession();

		List<Organisation> organisations = repositoryService.getOrganisations(entry);
		assertThat(organisations).extracting(Organisation::getKey).contains(organisation.getKey());
		assertThat(organisations).hasSize(2);

		SettingsContext removeContext = new SettingsContext(List.of(entry));
		removeContext.select(SettingsBulkEditable.organisationsRemove, true);
		removeContext.setOrganisationRemoveKeys(Set.of(organisation.getKey()));
		repositoryBulkService.update(null, author, removeContext);
		dbInstance.commitAndCloseSession();

		organisations = repositoryService.getOrganisations(entry);
		assertThat(organisations).extracting(Organisation::getKey).doesNotContain(organisation.getKey());
		assertThat(organisations).hasSize(1);
	}

	@Test
	public void shouldUpdateTaxonomyLevels() {
		List<TaxonomyRef> previousTaxonomyRefs = repositoryModule.getTaxonomyRefs();
		try {
			Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("bulk-tax-1");
			RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
			Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
			TaxonomyLevel level1 = taxonomyLevelDao.createTaxonomyLevel(random(), random(), random(), null, null, null, null, null, taxonomy);
			TaxonomyLevel level2 = taxonomyLevelDao.createTaxonomyLevel(random(), random(), random(), null, null, null, null, null, taxonomy);
			repositoryModule.setTaxonomyRefs(List.of(taxonomy));
			dbInstance.commitAndCloseSession();

			SettingsContext addContext = new SettingsContext(List.of(entry));
			addContext.select(SettingsBulkEditable.taxonomyLevelsAdd, true);
			addContext.setTaxonomyLevelAddKeys(Set.of(level1.getKey(), level2.getKey()));
			repositoryBulkService.update(null, author, addContext);
			dbInstance.commitAndCloseSession();

			Map<RepositoryEntryRef, List<TaxonomyLevel>> taxonomyLevels = repositoryService.getTaxonomy(List.of(entry), false);
			assertThat(taxonomyLevels.values().iterator().next())
					.extracting(TaxonomyLevel::getKey)
					.containsExactlyInAnyOrder(level1.getKey(), level2.getKey());

			SettingsContext removeContext = new SettingsContext(List.of(entry));
			removeContext.select(SettingsBulkEditable.taxonomyLevelsRemove, true);
			removeContext.setTaxonomyLevelRemoveKeys(Set.of(level1.getKey()));
			repositoryBulkService.update(null, author, removeContext);
			dbInstance.commitAndCloseSession();

			taxonomyLevels = repositoryService.getTaxonomy(List.of(entry), false);
			assertThat(taxonomyLevels.values().iterator().next())
					.extracting(TaxonomyLevel::getKey)
					.containsExactly(level2.getKey());
		} finally {
			repositoryModule.setTaxonomyRefs(previousTaxonomyRefs);
		}
	}

	@Test
	public void shouldUpdateLicense() {
		boolean previousEnabled = licenseModule.isEnabled(licenseHandler);
		try {
			licenseModule.setEnabled(licenseHandler.getType(), true);
			LicenseType licenseType = licenseService.loadLicenseTypeByName("CC BY");
			licenseService.activate(licenseHandler, licenseType);
			Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("bulk-lic-1");
			RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
			dbInstance.commitAndCloseSession();

			SettingsContext context = new SettingsContext(List.of(entry));
			context.select(SettingsBulkEditable.license, true);
			context.setLicenseTypeKey(licenseType.getKey().toString());
			context.setLicensor("frentix GmbH");
			repositoryBulkService.update(null, author, context);
			dbInstance.commitAndCloseSession();

			ResourceLicense license = licenseService.loadLicense(entry.getOlatResource());
			assertThat(license).isNotNull();
			assertThat(license.getLicenseType().getKey()).isEqualTo(licenseType.getKey());
			assertThat(license.getLicensor()).isEqualTo("frentix GmbH");
		} finally {
			licenseModule.setEnabled(licenseHandler.getType(), previousEnabled);
		}
	}

	@Test
	public void shouldUpdateToolbarSettings() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("bulk-tool-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		dbInstance.commitAndCloseSession();
		CourseConfig courseConfig = CourseFactory.loadCourse(entry.getOlatResource()).getCourseConfig();
		boolean search = courseConfig.isCourseSearchEnabled();
		boolean participantList = courseConfig.isParticipantListEnabled();
		boolean participantInfo = courseConfig.isParticipantInfoEnabled();
		boolean email = courseConfig.isEmailEnabled();
		boolean forum = courseConfig.isForumEnabled();
		boolean documents = courseConfig.isDocumentsEnabled();
		boolean chat = courseConfig.isChatEnabled();

		SettingsContext context = new SettingsContext(List.of(entry));
		context.select(SettingsBulkEditable.toolSearch, true);
		context.setToolSearch(!search);
		context.select(SettingsBulkEditable.toolParticipantList, true);
		context.setToolParticipantList(!participantList);
		context.select(SettingsBulkEditable.toolParticipantInfo, true);
		context.setToolParticipantInfo(!participantInfo);
		context.select(SettingsBulkEditable.toolEmail, true);
		context.setToolEmail(!email);
		context.select(SettingsBulkEditable.toolForum, true);
		context.setToolForum(!forum);
		context.select(SettingsBulkEditable.toolDocuments, true);
		context.setToolDocuments(!documents);
		context.select(SettingsBulkEditable.toolChat, true);
		context.setToolChat(!chat);
		repositoryBulkService.update(null, author, context);
		dbInstance.commitAndCloseSession();

		CourseConfig updatedConfig = CourseFactory.loadCourse(entry.getOlatResource()).getCourseConfig();
		assertThat(updatedConfig.isCourseSearchEnabled()).isEqualTo(!search);
		assertThat(updatedConfig.isParticipantListEnabled()).isEqualTo(!participantList);
		assertThat(updatedConfig.isParticipantInfoEnabled()).isEqualTo(!participantInfo);
		assertThat(updatedConfig.isEmailEnabled()).isEqualTo(!email);
		assertThat(updatedConfig.isForumEnabled()).isEqualTo(!forum);
		assertThat(updatedConfig.isDocumentsEnabled()).isEqualTo(!documents);
		assertThat(updatedConfig.isChatEnabled()).isEqualTo(!chat);
	}

	@Test
	public void shouldUpdateInfoDisplaySettings() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("bulk-info-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		dbInstance.commitAndCloseSession();

		SettingsContext context = new SettingsContext(List.of(entry));
		context.select(SettingsBulkEditable.infoEvents, true);
		context.setInfoEvents(Boolean.TRUE);
		context.select(SettingsBulkEditable.infoCertificate, true);
		context.setInfoCertificate(Boolean.TRUE);
		repositoryBulkService.update(null, author, context);
		dbInstance.commitAndCloseSession();

		RepositoryEntry reloaded = repositoryService.loadByKey(entry.getKey());
		assertThat(reloaded.isShowLectures()).isTrue();
		assertThat(reloaded.isShowCertificateBenefit()).isTrue();
	}

	@Test
	public void shouldUpdateTaughtBy() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("bulk-info-2");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		dbInstance.commitAndCloseSession();

		SettingsContext context = new SettingsContext(List.of(entry));
		context.select(SettingsBulkEditable.infoMeetTeachers, true);
		context.setInfoMeetTeachers(Boolean.TRUE);
		context.select(SettingsBulkEditable.infoTaughtByTeachers, true);
		context.setInfoTaughtByTeachers(Boolean.TRUE);
		context.select(SettingsBulkEditable.infoTaughtByCoaches, true);
		context.setInfoTaughtByCoaches(Boolean.TRUE);
		repositoryBulkService.update(null, author, context);
		dbInstance.commitAndCloseSession();

		RepositoryEntry reloaded = repositoryService.loadByKey(entry.getKey());
		assertThat(reloaded.getTaughtBys()).containsExactlyInAnyOrder(TaughtBy.teachers, TaughtBy.coaches);

		SettingsContext removeContext = new SettingsContext(List.of(reloaded));
		removeContext.select(SettingsBulkEditable.infoTaughtByCoaches, true);
		removeContext.setInfoTaughtByCoaches(Boolean.FALSE);
		repositoryBulkService.update(null, author, removeContext);
		dbInstance.commitAndCloseSession();

		reloaded = repositoryService.loadByKey(entry.getKey());
		assertThat(reloaded.getTaughtBys()).containsExactly(TaughtBy.teachers);
	}

	@Test
	public void shouldRemoveTaughtByWhenMeetTeachersOff() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("bulk-info-3");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		entry.setTaughtBys(Set.of(TaughtBy.teachers, TaughtBy.owners));
		entry = dbInstance.getCurrentEntityManager().merge(entry);
		dbInstance.commitAndCloseSession();

		SettingsContext context = new SettingsContext(List.of(entry));
		context.select(SettingsBulkEditable.infoMeetTeachers, true);
		context.setInfoMeetTeachers(Boolean.FALSE);
		repositoryBulkService.update(null, author, context);
		dbInstance.commitAndCloseSession();

		RepositoryEntry reloaded = repositoryService.loadByKey(entry.getKey());
		assertThat(reloaded.getTaughtByValue()).isNull();
		assertThat(reloaded.getTaughtBys()).isEmpty();
	}

	@Test
	public void shouldNotChangeWhenNothingSelected() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("bulk-info-4");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		entry.setTaughtBys(Set.of(TaughtBy.coaches));
		entry = dbInstance.getCurrentEntityManager().merge(entry);
		dbInstance.commitAndCloseSession();
		boolean showLectures = entry.isShowLectures();

		SettingsContext context = new SettingsContext(List.of(entry));
		repositoryBulkService.update(null, author, context);
		dbInstance.commitAndCloseSession();

		RepositoryEntry reloaded = repositoryService.loadByKey(entry.getKey());
		assertThat(reloaded.isShowLectures()).isEqualTo(showLectures);
		assertThat(reloaded.getTaughtBys()).containsExactly(TaughtBy.coaches);
	}

	@Test
	public void shouldNotChangeNonCourse() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("bulk-info-5");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		boolean showLectures = entry.isShowLectures();

		SettingsContext context = new SettingsContext(List.of(entry));
		context.select(SettingsBulkEditable.infoEvents, true);
		context.setInfoEvents(Boolean.valueOf(!showLectures));
		repositoryBulkService.update(null, author, context);
		dbInstance.commitAndCloseSession();

		RepositoryEntry reloaded = repositoryService.loadByKey(entry.getKey());
		assertThat(reloaded.isShowLectures()).isEqualTo(showLectures);
	}

}
