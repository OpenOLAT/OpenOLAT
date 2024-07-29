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
package org.olat.course.nodes.gta.model;

/**
 * 
 * Initial date: 21 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public record SessionStatistics(double progress, double min, double max, double average, double sum,
		double firstQuartile, double median, double thirdQuartile, int numOfQuestions, int maxSteps) {
	public static final SessionStatistics NO_STATISTICS = new SessionStatistics(0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d,0.0d, 0.0d, 0, 0);
	
	public static SessionStatistics noStatistics() {
		return NO_STATISTICS;
	}
}
