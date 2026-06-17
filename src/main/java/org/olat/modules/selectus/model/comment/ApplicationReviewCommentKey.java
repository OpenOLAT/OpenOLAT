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
package org.olat.modules.selectus.model.comment;

/**
 * 
 * Initial date: 21 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationReviewCommentKey {
	
	private final Long applicationKey;
	private final Long reviewKey;
	
	public ApplicationReviewCommentKey(Long applicationKey, Long reviewKey) {
		this.applicationKey = applicationKey;
		this.reviewKey = reviewKey;
	}

	public Long getApplicationKey() {
		return applicationKey;
	}

	public Long getReviewKey() {
		return reviewKey;
	}

	@Override
	public int hashCode() {
		return applicationKey.hashCode() + reviewKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof ApplicationReviewCommentKey) {
			ApplicationReviewCommentKey key = (ApplicationReviewCommentKey)obj;
			return applicationKey.equals(key.applicationKey) && reviewKey.equals(key.reviewKey);
		}
		return true;
	}
}
