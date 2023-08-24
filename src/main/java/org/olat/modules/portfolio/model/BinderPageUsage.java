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
package org.olat.modules.portfolio.model;

import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.PageStatus;

/**
 * 
 * Initial date: 21.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderPageUsage {
	
	private Long binderKey;
	private String binderTitle;
	
	private Long pageKey;
	private String pageTitle;
	private String pageStatus;
	
	public BinderPageUsage(Long binderKey, String binderTitle, Long pageKey, String pageTitle, String pageStatus) {
		this.binderKey = binderKey;
		this.binderTitle = binderTitle;
		this.pageKey = pageKey;
		this.pageTitle = pageTitle;
		this.pageStatus = pageStatus;
	}
	
	public Long getBinderKey() {
		return binderKey;
	}
	
	public String getBinderTitle() {
		return binderTitle;
	}
	
	public Long getPageKey() {
		return pageKey;
	}
	
	public String getPageTitle() {
		return pageTitle;
	}
	
	public PageStatus getPageStatus() {
		return StringHelper.containsNonWhitespace(pageStatus) ? PageStatus.valueOf(pageStatus) : PageStatus.draft;
	}
}
