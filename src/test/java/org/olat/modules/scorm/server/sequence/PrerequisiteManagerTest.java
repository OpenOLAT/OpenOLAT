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
package org.olat.modules.scorm.server.sequence;

import org.junit.Test;
import org.olat.modules.scorm.server.servermodels.SequencerModel;
import org.wildfly.common.Assert;

/**
 * 
 * Initial date: 18 mars 2026<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PrerequisiteManagerTest {
	
	@Test
	public void testAnd() {
		PrerequisiteManager mgr = new PrerequisiteManager();
		mgr.updatePrerequisites("sco1", SequencerModel.ITEM_COMPLETED, false);
		mgr.updatePrerequisites("sco2", SequencerModel.ITEM_INCOMPLETE, false);
		mgr.updatePrerequisites("sco3", SequencerModel.ITEM_NOT_ATTEMPTED, false);
		boolean cannotLaunch = mgr.canLaunchItem("sco3", "sco1&sco2");
		Assert.assertFalse(cannotLaunch);

		mgr.updatePrerequisites("sco2", SequencerModel.ITEM_COMPLETED, false);
		boolean canLaunch = mgr.canLaunchItem("sco3", "sco1&sco2");
		Assert.assertTrue(canLaunch);
	}
	
	@Test
	public void testOr() {
		PrerequisiteManager mgr = new PrerequisiteManager();
		mgr.updatePrerequisites("sco1", SequencerModel.ITEM_COMPLETED, false);
		mgr.updatePrerequisites("sco2", SequencerModel.ITEM_FAILED, false);
		mgr.updatePrerequisites("sco3", SequencerModel.ITEM_NOT_ATTEMPTED, false);
		mgr.updatePrerequisites("sco4", SequencerModel.ITEM_NOT_ATTEMPTED, false);
		boolean cannotLaunch = mgr.canLaunchItem("sco3", "sco1&(sco2|sco3)");
		Assert.assertFalse(cannotLaunch);

		mgr.updatePrerequisites("sco2", SequencerModel.ITEM_PASSED, false);
		boolean canLaunch = mgr.canLaunchItem("sco4", "sco1&(sco2|sco3)");
		Assert.assertTrue(canLaunch);
	}
	
	@Test
	public void testEqualsStatus() {
		PrerequisiteManager mgr = new PrerequisiteManager();
		mgr.updatePrerequisites("sco1", SequencerModel.ITEM_COMPLETED, false);
		mgr.updatePrerequisites("sco3", SequencerModel.ITEM_NOT_ATTEMPTED, false);
		boolean cannotLaunch = mgr.canLaunchItem("sco3", "sco1=\"passed\"");
		Assert.assertFalse(cannotLaunch);
		
		mgr.updatePrerequisites("sco1", SequencerModel.ITEM_PASSED, false);
		boolean canLaunch = mgr.canLaunchItem("sco3", "sco1=\"passed\"");
		Assert.assertTrue(canLaunch);
	}
}
