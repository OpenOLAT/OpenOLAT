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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.core.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
 
@RunWith(Suite.class)
@Suite.SuiteClasses({
	
	// Testcases for the OLAT core application framework
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
	org.olat.core.gui.render.TestRenderStaticURLCacheHeaders.class,
	/**
	 * 
	 * Place tests which load their own Spring context
	 * with @ContextConfiguration below the others as they may taint the 
	 * cached Spring context
	 */
	org.olat.core.commons.scheduler.SchedulerTest.class
	
})

public class AllTestsOlatCoreJunit4 {
	//
}
		