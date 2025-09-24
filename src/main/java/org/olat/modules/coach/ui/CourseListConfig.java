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
package org.olat.modules.coach.ui;

/**
 * 
 * Initial date: 9 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CourseListConfig {
	
	private boolean withFilters;
	private boolean withPresetsFilters;
	private boolean withToolbar;
	
	private CourseListConfig(boolean withFilters, boolean withPresetsFilters, boolean withToolbar) {
		this.withFilters = withFilters;
		this.withToolbar = withToolbar;
		this.withPresetsFilters = withPresetsFilters;
	}
	
	public static CourseListConfig defaultConfig() {
		return new CourseListConfig(true, true, true);
	}
	
	public static CourseListConfig minimalConfig() {
		return new CourseListConfig(false, false, false);
	}
	
	public boolean withFilters() {
		return withFilters;
	}
	
	public CourseListConfig withFilters(boolean withFilters) {
		this.withFilters = withFilters;
		return this;
	}
	
	public boolean withPresetsFilters() {
		return withPresetsFilters;
	}
	
	public CourseListConfig setWithPresetsFilters(boolean withPresetsFilters) {
		this.withPresetsFilters = withPresetsFilters;
		return this;
	}

	public boolean withToolbar() {
		return withToolbar;
	}

	public CourseListConfig withToolbar(boolean withToolbar) {
		this.withToolbar = withToolbar;
		return this;
	}
}
