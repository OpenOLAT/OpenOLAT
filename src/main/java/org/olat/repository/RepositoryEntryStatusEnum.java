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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 20 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum RepositoryEntryStatusEnum {
	
	preparation("cif.status.preparation", "o_icon_edit"),
	review("cif.status.review", "o_icon_review"),
	coachpublished("cif.status.coachpublished", "o_icon_published"),
	published("cif.status.published", "o_icon_published"),
	closed("cif.status.closed", "o_icon_closed"),
	trash("cif.status.trash", "o_icon_trash"),
	deleted("cif.status.deleted", "o_icon_deleted");
	
	private final String i18nKey;
	private final String cssClass;
	
	private RepositoryEntryStatusEnum(String i18nKey, String cssClass) {
		this.i18nKey = i18nKey;
		this.cssClass = cssClass;
	}
	
	public String i18nKey() {
		return i18nKey;
	}
	
	public String cssClass() {
		return cssClass;
	}
	
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
	
	public static RepositoryEntryStatusEnum[] reviewToPublished() {
		return new RepositoryEntryStatusEnum[] {
			RepositoryEntryStatusEnum.review, RepositoryEntryStatusEnum.coachpublished,
			RepositoryEntryStatusEnum.published
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
	
	public static RepositoryEntryStatusEnum[] closed() {
		return new RepositoryEntryStatusEnum[] {
				RepositoryEntryStatusEnum.closed
		};
	}
	
	public static boolean isInArray(RepositoryEntryStatusEnum val, RepositoryEntryStatusEnum[] array) {
		for(int i=array.length; i-->0; ) {
			if(val == array[i]) {
				return true;
			}
		}
		return false;
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
	
	public static RepositoryEntryStatusEnum[] toArray(List<String> status) {
		List<RepositoryEntryStatusEnum> list = new ArrayList<>();
		if(status != null && !status.isEmpty()) {
			for(String s:status) {
				if(isValid(s)) {
					list.add(valueOf(s));
				}
			}
		}
		return list.toArray(new RepositoryEntryStatusEnum[list.size()]);
	}
	
	public static ILoggingAction loggingAction(RepositoryEntryStatusEnum status) {
		switch(status) {
			case preparation: return LearningResourceLoggingAction.LEARNING_RESOURCE_STATUS_PREPARATION;
			case review: return LearningResourceLoggingAction.LEARNING_RESOURCE_STATUS_REVIEW;
			case coachpublished: return LearningResourceLoggingAction.LEARNING_RESOURCE_STATUS_COACHPUBLISH;
			case published: return LearningResourceLoggingAction.LEARNING_RESOURCE_STATUS_PUBLISH;
			case closed: return LearningResourceLoggingAction.LEARNING_RESOURCE_STATUS_CLOSE;
			case trash: return LearningResourceLoggingAction.LEARNING_RESOURCE_STATUS_TRASH;
			case deleted: return LearningResourceLoggingAction.LEARNING_RESOURCE_DELETE;
			default: return null;
		}
	}
}
