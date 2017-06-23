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
package org.olat.modules.video.ui;

import java.util.Date;

/**
 * 
 * Initial date: 21.10.2016<br>
 * @author fkiefer, fabian.kiefer@frentix.com, http://www.frentix.com
 *
 */
public class VideoChapterTableRow {

	private String chapterName;
	private String intervals;
	private Date begin,end;

	public VideoChapterTableRow(String chapterName, String intervals, Date begin, Date end) {
		this.chapterName = chapterName;
		this.intervals = intervals;
		this.begin = begin;
		this.end = end;
	}
	
	public String getChapterName() {
		return chapterName;
	}

	public void setChapterName(String chapterName) {
		this.chapterName = chapterName;
	}

	public String getIntervals() {
		return intervals;
	}

	public void setIntervals(String intervals) {
		this.intervals = intervals;
	}

	public Date getBegin() {
		return begin;
	}

	public void setBegin(Date begin) {
		this.begin = begin;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}
}