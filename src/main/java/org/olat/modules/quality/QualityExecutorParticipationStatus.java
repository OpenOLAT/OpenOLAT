/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.quality;

/**
 * 
 * Initial date: 17.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public enum QualityExecutorParticipationStatus {
	
	FUTURE(40, "executor.participation.status.future"),
	READY(10, "executor.participation.status.ready"),
	PARTICIPATING(20, "executor.participation.status.participating"),
	PARTICIPATED(30, "executor.participation.status.participated"),
	OVER(50, "executor.participation.status.over");
	
	private final int order;
	private final String i18nKey;
	
	private QualityExecutorParticipationStatus(int order, String i18nKey) {
		this.order = order;
		this.i18nKey = i18nKey;
	}
	
	public static QualityExecutorParticipationStatus getEnum(Integer order) {
		for (QualityExecutorParticipationStatus status: QualityExecutorParticipationStatus.values()) {
			if (order != null && order == status.order) {
				return status;
			}
		}
		return null;
	}

	public int getOrder() {
		return order;
	}

	public String getI18nKey() {
		return i18nKey;
	}

}
