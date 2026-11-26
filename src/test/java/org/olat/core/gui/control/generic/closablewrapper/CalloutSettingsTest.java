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
package org.olat.core.gui.control.generic.closablewrapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings.CalloutAlignment;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings.CalloutOrientation;

/**
 * Checks the alignment of {@link CalloutSettings}: center by default, so the
 * existing callouts keep their placement.
 *
 * Initial date: 26 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 *
 */
public class CalloutSettingsTest {

	@Test
	public void alignmentIsCenterByDefault() {
		assertThat(new CalloutSettings().getAlignment()).isEqualTo(CalloutAlignment.center);
		assertThat(new CalloutSettings(false, CalloutOrientation.bottomOrTop, false, null).getAlignment())
				.isEqualTo(CalloutAlignment.center);
	}

	@Test
	public void setAlignment() {
		CalloutSettings settings = new CalloutSettings(false, CalloutOrientation.bottomOrTop, false, null);
		settings.setAlignment(CalloutAlignment.targetWidth);
		assertThat(settings.getAlignment()).isEqualTo(CalloutAlignment.targetWidth);
		settings.setAlignment(CalloutAlignment.edge);
		assertThat(settings.getAlignment()).isEqualTo(CalloutAlignment.edge);
		settings.setAlignment(null);
		assertThat(settings.getAlignment()).isEqualTo(CalloutAlignment.center);
	}
}
