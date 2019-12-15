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
package org.olat.course.nodes.livestream.paella;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 
 * Initial date: 13 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Sources {
	
	private Source[] hls;
	private Source[] mp4;

	public Source[] getHls() {
		return hls;
	}

	public void setHls(Source[] hls) {
		this.hls = hls;
	}

	public Source[] getMp4() {
		return mp4;
	}

	public void setMp4(Source[] mp4) {
		this.mp4 = mp4;
	}

}
