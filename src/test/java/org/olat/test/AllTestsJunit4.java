/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.test;

/**
 * Description:<br>
 * JUnit suite runner
 * There are basically three types of tests:
 *** Tests that extend from the olatTestCase (testcase loads a full olat before running the tests -- very slow and is an integration test)
 *** Tests that load their own little spring context with @ContextConfiguration (that's how it should be done)
 *** Tests that do not need any Spring context
 * As tests with @ContextConfiguration can taint the context from olattestcase they must be placed on the end of the list!
 * <P>
 * Initial Date:  15.02.2010 <br>
 * @author guido
 */

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
 
@RunWith(Suite.class)
@Suite.SuiteClasses({
	org.olat.core.util.i18n.I18nTest.class,
	// org.olat.core.util.mail.MailTest.class, // redisabled since mails are sent despite the fact that the whitelist is enabled
	org.olat.core.util.locks.SynchManagerTest.class,
	org.olat.core.gui.components.table.MultiSelectColumnDescriptorTest.class,
	org.olat.core.gui.components.table.TableEventTest.class,
	org.olat.core.gui.components.table.TableMultiSelectEventTest.class,
	org.olat.core.commons.chiefcontrollers.ChiefControllerMessageEventTest.class,
	org.olat.core.util.vfs.VFSManagerTest.class,
	org.olat.core.util.filter.impl.XSSFilterTest.class,
	org.olat.core.util.filter.impl.AddBaseURLToMediaRelativeURLFilterTest.class,
	org.olat.core.util.filter.impl.SimpleHTMLTagsFilterTest.class,
	org.olat.core.util.filter.impl.NekoHTMLFilterTest.class,
	org.olat.core.util.filter.impl.ConditionalHtmlCommentsFilterTest.class,
	org.olat.core.helpers.SettingsTest.class,
	org.olat.core.util.coordinate.LockEntryTest.class,
	org.olat.core.util.StringHelperTest.class,
	org.olat.core.id.context.BusinessControlFactoryTest.class,

	org.olat.note.NoteTest.class,//ok
	org.olat.user.UserPropertiesPerformanceTest.class,//ok
	org.olat.user.EmailCheckPerformanceTest.class,//fail
	org.olat.user.UserTest.class,//ok
	org.olat.user.UserPropertiesTest.class,//ok
	org.olat.commons.calendar.ui.components.WeeklyCalendarComponentTest.class,//ok
	org.olat.commons.calendar.test.CalendarUtilsTest.class,//ok
	org.olat.commons.calendar.ICalFileCalendarManagerTest.class,//ok
	org.olat.commons.lifecycle.LifeCycleManagerTest.class,//fail christian fragen...
	org.olat.commons.coordinate.cluster.jms.JMSTest.class,//ok
	org.olat.commons.coordinate.cluster.lock.LockTest.class,//ok
	org.olat.commons.coordinate.CoordinatorTest.class,//ok
	org.olat.admin.user.delete.service.UserDeletionManagerTest.class,//ok
	org.olat.group.test.BGRightManagerTest.class,//ok
	org.olat.group.test.BGAreaManagerTest.class,//ok
	org.olat.group.test.BusinessGroupServiceTest.class,//ok
	org.olat.group.test.BusinessGroupDAOTest.class,//ok
	org.olat.group.test.BusinessGroupRelationDAOTest.class,//ok
	org.olat.resource.lock.pessimistic.PLockTest.class,//ok
	org.olat.resource.references.ReferenceManagerTest.class,//ok
	org.olat.resource.OLATResourceManagerTest.class,//ok
	org.olat.basesecurity.SecurityManagerTest.class,//ok
	org.olat.basesecurity.BaseSecurityTest.class,//ok
	org.olat.basesecurity.BaseSecurityManagerTest.class,//ok
	org.olat.repository.RepositoryManagerTest.class,//ok
	org.olat.repository.RepositoryManagerConcurrentTest.class,//ok
	org.olat.repository.RepositoryManagerQueryTest.class,//ok
	org.olat.instantMessaging.IMUnitTest.class,//ok
	org.olat.instantMessaging.IMPrefsUnitTest.class,//ok
	org.olat.course.nodes.en.EnrollmentManagerTest.class,//ok
	org.olat.course.assessment.AssessmentManagerTest.class,//ok
	org.olat.course.config.CourseConfigManagerImplTest.class,//ok
	org.olat.course.groupsandrights.CourseGroupManagementTest.class,//ok
	org.olat.course.editor.PublishProcessTest.class,//ok
	org.olat.modules.fo.ForumManagerTest.class,//fail
	org.olat.modules.wiki.WikiUnitTest.class,//ok
	org.olat.modules.wiki.versioning.diff.CookbookDiffTest.class,//ok
	org.olat.properties.PropertyTest.class,//ok
	org.olat.search.service.document.file.FileDocumentFactoryTest.class,
	org.olat.search.service.document.file.PDFDocumentTest.class,
	org.olat.catalog.CatalogManagerTest.class,//ok
	org.olat.bookmark.BookmarkManagerTest.class,//ok
	org.olat.notifications.NotificationsManagerTest.class,//fail
	org.olat.registration.RegistrationManagerTest.class,//ok
	org.olat.commons.coordinate.singlevm.SingleVMLockerTest.class,//ok
	org.olat.course.nodes.projectbroker.ProjectBrokerManagerTest.class,
	org.olat.core.commons.persistence.DBTest.class,
	org.olat.modules.ims.cp.CPManagerTest.class,
	org.olat.modules.webFeed.FeedManagerImplTest.class,
	org.olat.basesecurity.IdentityTest.class,
	org.olat.ldap.LDAPLoginTest.class,
	org.olat.core.commons.service.mark.MarksTest.class,
//	org.olat.test.OlatJerseyTestCase.class, // NO TEST METHODS 
	org.olat.test.SpringInitDestroyVerficationTest.class,
	org.olat.course.statistic.TestLoggingVersionManagerImpl.class,
	org.olat.course.statistic.weekly.TestWeeklyStatisticManager_fillGaps.class,
	org.olat.core.commons.service.usercomments.UserCommentsTest.class,
	org.olat.core.commons.service.usercomments.UserRatingsTest.class,
	org.olat.course.auditing.UserNodeAuditManagerTest.class,
	org.olat.shibboleth.util.ShibbolethAttributeTest.class,
	org.olat.portfolio.PortfolioModuleTest.class,
	org.olat.portfolio.EPArtefactManagerTest.class,
	org.olat.portfolio.EPFrontendManagerTest.class,
	org.olat.portfolio.EPStructureManagerTest.class,
	org.olat.portfolio.EPStructureToArtefactTest.class,
	org.olat.portfolio.EPImportTest.class,
	org.olat.commons.info.InfoManagerTest.class,
	org.olat.core.commons.service.tagging.SimpleTagProposalManagerTest.class,
	org.olat.commons.coordinate.singlevm.SingleVMLockerTest.class,
	org.olat.core.commons.service.tagging.TaggingManagerTest.class,
	org.olat.restapi.AuthenticationTest.class,
	org.olat.restapi.CatalogTest.class,
	org.olat.restapi.CourseGroupMgmtTest.class,
	org.olat.restapi.CoursesContactElementTest.class,
	org.olat.restapi.CourseSecurityTest.class,
	org.olat.restapi.CoursesElementsTest.class,
	org.olat.restapi.CoursesFoldersTest.class,
	org.olat.restapi.CoursesForumsTest.class,
	org.olat.restapi.CoursesResourcesFoldersTest.class,
	org.olat.restapi.CoursesTest.class,
	org.olat.restapi.CourseTest.class,
	org.olat.restapi.FolderTest.class,
	org.olat.restapi.ForumTest.class,
	org.olat.restapi.GroupFoldersTest.class,
	org.olat.restapi.GroupMgmtTest.class,
	org.olat.restapi.I18nTest.class,
	org.olat.restapi.NotificationsTest.class,
	org.olat.restapi.RepositoryEntriesTest.class,
	org.olat.restapi.RestApiLoginFilterTest.class,
	org.olat.restapi.UserAuthenticationMgmtTest.class,
	org.olat.restapi.UserMgmtTest.class,
	org.olat.restapi.ContactsTest.class,
	org.olat.restapi.SystemTest.class,
	de.bps.olat.portal.institution.InstitutionPortletTest.class,
	org.olat.group.manager.BusinessGroupImportExportXStreamTest.class,
	org.olat.group.test.BusinessGroupImportExportTest.class,
	org.olat.resource.accesscontrol.ACFrontendManagerTest.class,
	org.olat.resource.accesscontrol.ACMethodManagerTest.class,
	org.olat.resource.accesscontrol.ACOfferManagerTest.class,
	org.olat.resource.accesscontrol.ACOrderManagerTest.class,
	org.olat.resource.accesscontrol.ACTransactionManagerTest.class,
	org.olat.resource.accesscontrol.ACReservationDAOTest.class,
	org.olat.core.util.vfs.VersionManagerTest.class,
	/**
	 * Pure JUnit test without need of framework
	 */
	org.olat.modules.fo.WordCountTest.class,
	/**
	 * 
	 * Place tests which load their own Spring context
	 * with @ContextConfiguration below the others as they may taint the 
	 * cached Spring context
	 * 
	 * IMPORTANT: If you create mock spring contexts in the test source tree of olatcore and
	 * you like to use them in olat3 you have to copy them to the test source tree of olat3
	 * as well as the tests on hudson run agains a jar version of olatcore where the test source
	 * tree is not available
	 */
	org.olat.core.commons.scheduler.SchedulerTest.class,
	org.olat.instantMessaging.IMUnitTestWithoutOLAT.class,
	org.olat.course.TestDeployableRepositoryExport.class,
	org.olat.upgrade.UpgradeDefinitionTest.class
})
public class AllTestsJunit4 {
	//
}
