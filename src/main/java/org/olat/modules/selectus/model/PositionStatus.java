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
package org.olat.modules.selectus.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.StringHelper;

public enum PositionStatus {
	preparation("o_preparation"),
	published("o_published"),
	publishedAndInScreening("o_published_screening_back"),
	closedAndInScreening("o_closed_screening_back"),
	closedAndNoRating("o_closed_no_rating"),
	closed("o_closed"),
	reporting("o_reporting");
	
	private final String css;
	
	private PositionStatus(String css) {
		this.css = css;
	}
	
	public String getCss() {
		return css;
	}
	
	public static List<PositionStatus> toList(PositionStatus... status) {
		List<PositionStatus> statusList = new ArrayList<>();
		if(status != null) {
			for(PositionStatus state:status) {
				if(state != null) {
					statusList.add(state);
				}
			}
		}
		return statusList;
	}
	
	public static PositionStatus[] valueOfArray(String status) {
		PositionStatus[] statusArr;
		if(StringHelper.containsNonWhitespace(status)) {
			String[] roleArr = status.split("[,]");
			statusArr = new PositionStatus[roleArr.length];
			for(int i=roleArr.length; i-->0; ) {
				statusArr[i] = PositionStatus.valueOf(roleArr[i]);
			}
		} else {
			statusArr = new PositionStatus[0];
		}
		return statusArr;
	}
}
