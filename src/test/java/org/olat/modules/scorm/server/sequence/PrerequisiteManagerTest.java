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
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
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

	/**
	 * The student may enter the item as long as SCO S35 has not been completed.
	 */
	@Test
	public void testNot() {
		PrerequisiteManager mgr = new PrerequisiteManager();
		mgr.updatePrerequisites("s34", SequencerModel.ITEM_NOT_ATTEMPTED, false);
		mgr.updatePrerequisites("s35", SequencerModel.ITEM_INCOMPLETE, false);
		// S35 is incomplete -> ~S35 is true -> can launch
		Assert.assertTrue(mgr.canLaunchItem("s34", "~s35"));

		mgr.updatePrerequisites("s35", SequencerModel.ITEM_COMPLETED, false);
		// S35 is complete -> ~S35 is false -> cannot launch
		Assert.assertFalse(mgr.canLaunchItem("s34", "~s35"));
	}

	/**
	 * ~S35 is equivalent to (S35&lt;&gt;"passed" &amp; S35&lt;"completed").
	 */
	@Test
	public void testNotEquals() {
		PrerequisiteManager mgr = new PrerequisiteManager();
		mgr.updatePrerequisites("s34", SequencerModel.ITEM_NOT_ATTEMPTED, false);
		mgr.updatePrerequisites("s35", SequencerModel.ITEM_FAILED, false);
		Assert.assertTrue(mgr.canLaunchItem("s34", "s35<>\"passed\" & s35<\"completed\""));

		mgr.updatePrerequisites("s35", SequencerModel.ITEM_PASSED, false);
		Assert.assertFalse(mgr.canLaunchItem("s34", "s35<>\"passed\" & s35<\"completed\""));
	}

	/**
	 * S34 &amp; S35 | S36: completing S36 by itself is enough (precedence).
	 */
	@Test
	public void testPrecedenceWithoutParenthesis() {
		PrerequisiteManager mgr = new PrerequisiteManager();
		mgr.updatePrerequisites("s34", SequencerModel.ITEM_INCOMPLETE, false);
		mgr.updatePrerequisites("s35", SequencerModel.ITEM_INCOMPLETE, false);
		mgr.updatePrerequisites("s36", SequencerModel.ITEM_COMPLETED, false);
		mgr.updatePrerequisites("s39", SequencerModel.ITEM_NOT_ATTEMPTED, false);
		Assert.assertTrue(mgr.canLaunchItem("s39", "s34 & s35 | s36"));
	}

	/**
	 * S34 &amp; (S35 | S36): S36 by itself is no longer enough.
	 */
	@Test
	public void testPrecedenceWithParenthesis() {
		PrerequisiteManager mgr = new PrerequisiteManager();
		mgr.updatePrerequisites("s34", SequencerModel.ITEM_INCOMPLETE, false);
		mgr.updatePrerequisites("s35", SequencerModel.ITEM_INCOMPLETE, false);
		mgr.updatePrerequisites("s36", SequencerModel.ITEM_COMPLETED, false);
		mgr.updatePrerequisites("s39", SequencerModel.ITEM_NOT_ATTEMPTED, false);
		Assert.assertFalse(mgr.canLaunchItem("s39", "s34 & (s35 | s36)"));

		mgr.updatePrerequisites("s34", SequencerModel.ITEM_PASSED, false);
		Assert.assertTrue(mgr.canLaunchItem("s39", "s34 & (s35 | s36)"));
	}

	/**
	 * 3*{S34, S36, S37, S39}: any three or more must be complete.
	 */
	@Test
	public void testSetCount() {
		PrerequisiteManager mgr = new PrerequisiteManager();
		mgr.updatePrerequisites("s34", SequencerModel.ITEM_COMPLETED, false);
		mgr.updatePrerequisites("s36", SequencerModel.ITEM_PASSED, false);
		mgr.updatePrerequisites("s37", SequencerModel.ITEM_INCOMPLETE, false);
		mgr.updatePrerequisites("s39", SequencerModel.ITEM_INCOMPLETE, false);
		mgr.updatePrerequisites("s38", SequencerModel.ITEM_NOT_ATTEMPTED, false);
		// only 2 complete -> cannot launch
		Assert.assertFalse(mgr.canLaunchItem("s38", "3*{s34, s36, s37, s39}"));

		mgr.updatePrerequisites("s37", SequencerModel.ITEM_COMPLETED, false);
		// 3 complete -> can launch
		Assert.assertTrue(mgr.canLaunchItem("s38", "3*{s34, s36, s37, s39}"));
	}

	/**
	 * A bare set requires every member to be complete.
	 */
	@Test
	public void testSetAll() {
		PrerequisiteManager mgr = new PrerequisiteManager();
		mgr.updatePrerequisites("s34", SequencerModel.ITEM_COMPLETED, false);
		mgr.updatePrerequisites("s36", SequencerModel.ITEM_PASSED, false);
		mgr.updatePrerequisites("s37", SequencerModel.ITEM_INCOMPLETE, false);
		mgr.updatePrerequisites("s38", SequencerModel.ITEM_NOT_ATTEMPTED, false);
		Assert.assertFalse(mgr.canLaunchItem("s38", "{s34, s36, s37}"));

		mgr.updatePrerequisites("s37", SequencerModel.ITEM_COMPLETED, false);
		Assert.assertTrue(mgr.canLaunchItem("s38", "{s34, s36, s37}"));
	}

	/**
	 * The manifest example: I1 &amp; I2 must be completed before launching.
	 */
	@Test
	public void testManifestExample() {
		PrerequisiteManager mgr = new PrerequisiteManager();
		mgr.updatePrerequisites("I1", SequencerModel.ITEM_COMPLETED, false);
		mgr.updatePrerequisites("I2", SequencerModel.ITEM_INCOMPLETE, false);
		mgr.updatePrerequisites("I3", SequencerModel.ITEM_NOT_ATTEMPTED, false);
		Assert.assertFalse(mgr.canLaunchItem("I3", "I1&I2"));

		mgr.updatePrerequisites("I2", SequencerModel.ITEM_PASSED, false);
		Assert.assertTrue(mgr.canLaunchItem("I3", "I1&I2"));
	}

	/**
	 * Identifiers containing a dash are valid.
	 */
	@Test
	public void testIdentifierWithDash() {
		PrerequisiteManager mgr = new PrerequisiteManager();
		mgr.updatePrerequisites("sco-1", SequencerModel.ITEM_COMPLETED, false);
		mgr.updatePrerequisites("sco-2", SequencerModel.ITEM_PASSED, false);
		mgr.updatePrerequisites("sco-3", SequencerModel.ITEM_NOT_ATTEMPTED, false);
		Assert.assertTrue(mgr.canLaunchItem("sco-3", "sco-1&sco-2"));
	}

	/**
	 * When a referenced identifier is unknown, the item can be launched.
	 */
	@Test
	public void testUnknownIdentifier() {
		PrerequisiteManager mgr = new PrerequisiteManager();
		mgr.updatePrerequisites("sco1", SequencerModel.ITEM_INCOMPLETE, false);
		mgr.updatePrerequisites("sco3", SequencerModel.ITEM_NOT_ATTEMPTED, false);
		// sco2 does not exist -> launchable
		Assert.assertTrue(mgr.canLaunchItem("sco3", "sco1&sco2"));
	}

	@Test
	public void testValidExpressions() {
		Assert.assertTrue(PrerequisiteManager.isValid("I1&I2"));
		Assert.assertTrue(PrerequisiteManager.isValid("s34 & (s35 | s36)"));
		Assert.assertTrue(PrerequisiteManager.isValid("3*{s34, s36, s37, s39}"));
		Assert.assertTrue(PrerequisiteManager.isValid("s35<>\"passed\""));
	}
	
	@Test
	public void testNotValidExpressions() {
		// unbalanced parenthesis
		Assert.assertFalse(PrerequisiteManager.isValid("s34 & (s35 | s36"));
		// odd number of quotes
		Assert.assertFalse(PrerequisiteManager.isValid("s35=\"passed"));
	}
}
