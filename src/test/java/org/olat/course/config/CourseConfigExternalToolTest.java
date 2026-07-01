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
 * Initial date: 01.07.2026<br>
 * @author uhensler, https://www.frentix.com
 */
package org.olat.course.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CourseConfigExternalToolTest {

	@Test
	public void defaults() {
		CourseConfig courseConfig = new CourseConfig();
		courseConfig.initDefaults();

		for (int toolIndex = 1; toolIndex <= CourseConfig.EXTERNAL_TOOL_COUNT; toolIndex++) {
			assertThat(courseConfig.isExternalToolEnabled(toolIndex)).as("enabled toolIndex=" + toolIndex).isFalse();
			assertThat(courseConfig.getExternalToolName(toolIndex)).as("name toolIndex=" + toolIndex).isEmpty();
			assertThat(courseConfig.getExternalToolUrl(toolIndex)).as("url toolIndex=" + toolIndex).isEmpty();
			assertThat(courseConfig.getExternalToolIcon(toolIndex)).as("icon toolIndex=" + toolIndex).isEqualTo("o_icon_link_extern");
			assertThat(courseConfig.isExternalToolVisible(toolIndex, ExternalToolVisibility.participant)).as("visParticipant toolIndex=" + toolIndex).isFalse();
			assertThat(courseConfig.isExternalToolVisible(toolIndex, ExternalToolVisibility.coach)).as("visCoach toolIndex=" + toolIndex).isFalse();
			assertThat(courseConfig.isExternalToolVisible(toolIndex, ExternalToolVisibility.owner)).as("visOwner toolIndex=" + toolIndex).isTrue();
		}
	}

	@Test
	public void gettersAndSetters() {
		CourseConfig courseConfig = new CourseConfig();
		courseConfig.initDefaults();

		for (int toolIndex = 1; toolIndex <= CourseConfig.EXTERNAL_TOOL_COUNT; toolIndex++) {
			courseConfig.setExternalToolEnabled(toolIndex, true);
			courseConfig.setExternalToolName(toolIndex, "Tool " + toolIndex);
			courseConfig.setExternalToolUrl(toolIndex, "https://tool" + toolIndex + ".example.com");
			courseConfig.setExternalToolIcon(toolIndex, "o_icon_globe");
			courseConfig.setExternalToolVisible(toolIndex, ExternalToolVisibility.participant, true);
			courseConfig.setExternalToolVisible(toolIndex, ExternalToolVisibility.coach, true);
			courseConfig.setExternalToolVisible(toolIndex, ExternalToolVisibility.owner, false);

			assertThat(courseConfig.isExternalToolEnabled(toolIndex)).as("enabled toolIndex=" + toolIndex).isTrue();
			assertThat(courseConfig.getExternalToolName(toolIndex)).as("name toolIndex=" + toolIndex).isEqualTo("Tool " + toolIndex);
			assertThat(courseConfig.getExternalToolUrl(toolIndex)).as("url toolIndex=" + toolIndex).isEqualTo("https://tool" + toolIndex + ".example.com");
			assertThat(courseConfig.getExternalToolIcon(toolIndex)).as("icon toolIndex=" + toolIndex).isEqualTo("o_icon_globe");
			assertThat(courseConfig.isExternalToolVisible(toolIndex, ExternalToolVisibility.participant)).as("visParticipant toolIndex=" + toolIndex).isTrue();
			assertThat(courseConfig.isExternalToolVisible(toolIndex, ExternalToolVisibility.coach)).as("visCoach toolIndex=" + toolIndex).isTrue();
			assertThat(courseConfig.isExternalToolVisible(toolIndex, ExternalToolVisibility.owner)).as("visOwner toolIndex=" + toolIndex).isFalse();
		}
	}

	@Test
	public void toolsAreIndependent() {
		CourseConfig courseConfig = new CourseConfig();
		courseConfig.initDefaults();

		courseConfig.setExternalToolEnabled(1, true);
		courseConfig.setExternalToolName(1, "Alpha");
		courseConfig.setExternalToolEnabled(2, false);
		courseConfig.setExternalToolName(2, "Beta");

		assertThat(courseConfig.isExternalToolEnabled(1)).isTrue();
		assertThat(courseConfig.getExternalToolName(1)).isEqualTo("Alpha");
		assertThat(courseConfig.isExternalToolEnabled(2)).isFalse();
		assertThat(courseConfig.getExternalToolName(2)).isEqualTo("Beta");
	}

	@Test
	public void clonePreservesAllFields() {
		CourseConfig original = new CourseConfig();
		original.initDefaults();

		for (int toolIndex = 1; toolIndex <= CourseConfig.EXTERNAL_TOOL_COUNT; toolIndex++) {
			original.setExternalToolEnabled(toolIndex, true);
			original.setExternalToolName(toolIndex, "Tool " + toolIndex);
			original.setExternalToolUrl(toolIndex, "https://tool" + toolIndex + ".example.com");
			original.setExternalToolIcon(toolIndex, "o_icon_chart_simple");
			original.setExternalToolVisible(toolIndex, ExternalToolVisibility.participant, true);
			original.setExternalToolVisible(toolIndex, ExternalToolVisibility.coach, false);
			original.setExternalToolVisible(toolIndex, ExternalToolVisibility.owner, true);
		}

		CourseConfig clone = original.clone();

		for (int toolIndex = 1; toolIndex <= CourseConfig.EXTERNAL_TOOL_COUNT; toolIndex++) {
			assertThat(clone.isExternalToolEnabled(toolIndex)).as("enabled toolIndex=" + toolIndex).isTrue();
			assertThat(clone.getExternalToolName(toolIndex)).as("name toolIndex=" + toolIndex).isEqualTo("Tool " + toolIndex);
			assertThat(clone.getExternalToolUrl(toolIndex)).as("url toolIndex=" + toolIndex).isEqualTo("https://tool" + toolIndex + ".example.com");
			assertThat(clone.getExternalToolIcon(toolIndex)).as("icon toolIndex=" + toolIndex).isEqualTo("o_icon_chart_simple");
			assertThat(clone.isExternalToolVisible(toolIndex, ExternalToolVisibility.participant)).as("visParticipant toolIndex=" + toolIndex).isTrue();
			assertThat(clone.isExternalToolVisible(toolIndex, ExternalToolVisibility.coach)).as("visCoach toolIndex=" + toolIndex).isFalse();
			assertThat(clone.isExternalToolVisible(toolIndex, ExternalToolVisibility.owner)).as("visOwner toolIndex=" + toolIndex).isTrue();
		}
	}

	@Test
	public void cloneIsIndependentCopy() {
		CourseConfig original = new CourseConfig();
		original.initDefaults();
		original.setExternalToolEnabled(1, true);
		original.setExternalToolName(1, "Original");

		CourseConfig clone = original.clone();
		clone.setExternalToolEnabled(1, false);
		clone.setExternalToolName(1, "Cloned");

		assertThat(original.isExternalToolEnabled(1)).isTrue();
		assertThat(original.getExternalToolName(1)).isEqualTo("Original");
	}

	@Test
	public void defaultsFromGetters_notFromStoredKeys() {
		CourseConfig courseConfig = new CourseConfig();

		for (int toolIndex = 1; toolIndex <= CourseConfig.EXTERNAL_TOOL_COUNT; toolIndex++) {
			assertThat(courseConfig.isExternalToolEnabled(toolIndex)).as("enabled toolIndex=" + toolIndex).isFalse();
			assertThat(courseConfig.getExternalToolName(toolIndex)).as("name toolIndex=" + toolIndex).isEmpty();
			assertThat(courseConfig.getExternalToolUrl(toolIndex)).as("url toolIndex=" + toolIndex).isEmpty();
			assertThat(courseConfig.getExternalToolIcon(toolIndex)).as("icon toolIndex=" + toolIndex).isEqualTo("o_icon_link_extern");
			assertThat(courseConfig.isExternalToolVisible(toolIndex, ExternalToolVisibility.participant)).as("visParticipant toolIndex=" + toolIndex).isFalse();
			assertThat(courseConfig.isExternalToolVisible(toolIndex, ExternalToolVisibility.coach)).as("visCoach toolIndex=" + toolIndex).isFalse();
			assertThat(courseConfig.isExternalToolVisible(toolIndex, ExternalToolVisibility.owner)).as("visOwner toolIndex=" + toolIndex).isTrue();
		}
	}

	@Test
	public void resetToDefault_removesStoredKey() {
		CourseConfig courseConfig = new CourseConfig();
		courseConfig.setExternalToolEnabled(1, true);
		courseConfig.setExternalToolName(1, "Tool");
		courseConfig.setExternalToolUrl(1, "https://example.com");
		courseConfig.setExternalToolIcon(1, "o_icon_globe");
		courseConfig.setExternalToolVisible(1, ExternalToolVisibility.owner, false);
		courseConfig.setExternalToolVisible(1, ExternalToolVisibility.coach, true);

		courseConfig.setExternalToolEnabled(1, false);
		courseConfig.setExternalToolName(1, "");
		courseConfig.setExternalToolUrl(1, null);
		courseConfig.setExternalToolIcon(1, null);
		courseConfig.setExternalToolVisible(1, ExternalToolVisibility.owner, true);
		courseConfig.setExternalToolVisible(1, ExternalToolVisibility.coach, false);

		assertThat(courseConfig.isExternalToolEnabled(1)).isFalse();
		assertThat(courseConfig.getExternalToolName(1)).isEmpty();
		assertThat(courseConfig.getExternalToolUrl(1)).isEmpty();
		assertThat(courseConfig.getExternalToolIcon(1)).isEqualTo("o_icon_link_extern");
		assertThat(courseConfig.isExternalToolVisible(1, ExternalToolVisibility.owner)).isTrue();
		assertThat(courseConfig.isExternalToolVisible(1, ExternalToolVisibility.coach)).isFalse();
	}

}
