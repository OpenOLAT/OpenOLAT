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
package org.olat.repository;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 20 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum RepositoryEntryStatusEnum {
	
	preparation,
	review,
	coachpublished,
	published,
	closed,
	trash,
	deleted;
	
	/**
	 * @return true if the value is closed, trash or deleted
	 */
	public boolean decommissioned() {
		return this == RepositoryEntryStatusEnum.closed
				|| this == RepositoryEntryStatusEnum.trash
				|| this == RepositoryEntryStatusEnum.deleted;
	}
	
	public static RepositoryEntryStatusEnum[] preparationToClosed() {
		return new RepositoryEntryStatusEnum[] {
			RepositoryEntryStatusEnum.preparation, RepositoryEntryStatusEnum.review,
			RepositoryEntryStatusEnum.coachpublished, RepositoryEntryStatusEnum.published,
			RepositoryEntryStatusEnum.closed
		};
	}
	
	public static RepositoryEntryStatusEnum[] preparationToPublished() {
		return new RepositoryEntryStatusEnum[] {
			RepositoryEntryStatusEnum.preparation, RepositoryEntryStatusEnum.review,
			RepositoryEntryStatusEnum.coachpublished, RepositoryEntryStatusEnum.published
		};
	}
	
	public static RepositoryEntryStatusEnum[] reviewToClosed() {
		return new RepositoryEntryStatusEnum[] {
			RepositoryEntryStatusEnum.review, RepositoryEntryStatusEnum.coachpublished,
			RepositoryEntryStatusEnum.published, RepositoryEntryStatusEnum.closed
		};
	}
	
	public static RepositoryEntryStatusEnum[] publishedAndClosed() {
		return new RepositoryEntryStatusEnum[] {
			RepositoryEntryStatusEnum.published, RepositoryEntryStatusEnum.closed
		};
	}
	
	public static RepositoryEntryStatusEnum[] coachPublishedToClosed() {
		return new RepositoryEntryStatusEnum[] {
				RepositoryEntryStatusEnum.coachpublished, RepositoryEntryStatusEnum.published, RepositoryEntryStatusEnum.closed
		};
	}
	
	public static RepositoryEntryStatusEnum[] authors() {
		return new RepositoryEntryStatusEnum[] {
			RepositoryEntryStatusEnum.preparation, RepositoryEntryStatusEnum.review,
			RepositoryEntryStatusEnum.coachpublished, RepositoryEntryStatusEnum.published,
			RepositoryEntryStatusEnum.closed
		};
	}
	
	public static RepositoryEntryStatusEnum[] users() {
		return new RepositoryEntryStatusEnum[] {
			RepositoryEntryStatusEnum.published, RepositoryEntryStatusEnum.closed
		};
	}
	
	public static boolean isValid(String string) {
		boolean allOk = false;
		if(StringHelper.containsNonWhitespace(string)) {
			for(RepositoryEntryStatusEnum status:values()) {
				if(status.name().equals(string)) {
					allOk = true;
					break;
				}
			}
		}
		return allOk;
	}	
}
