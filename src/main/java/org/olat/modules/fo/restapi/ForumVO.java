/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.modules.fo.restapi;

import org.olat.modules.fo.Forum;

/**
 * 
 * <h3>Description:</h3>
 * Wrapper class for Forum
 * <p>
 * Initial Date:  11 janv. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class ForumVO {

	private Long forumKey;
	
	public ForumVO() {
		//make JAXB happy
	}

	public ForumVO(Forum forum) {
		forumKey = forum.getKey();
	}

	public Long getForumKey() {
		return forumKey;
	}

	public void setForumKey(Long forumKey) {
		this.forumKey = forumKey;
	}
}
