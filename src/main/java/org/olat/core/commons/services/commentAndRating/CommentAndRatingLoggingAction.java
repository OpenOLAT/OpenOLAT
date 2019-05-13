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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.core.commons.services.commentAndRating;

import java.lang.reflect.Field;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ActionObject;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.logging.activity.ActionVerb;
import org.olat.core.logging.activity.BaseLoggingAction;
import org.olat.core.logging.activity.CrudAction;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ResourceableTypeList;

public class CommentAndRatingLoggingAction extends BaseLoggingAction {
	
	private static final Logger log = Tracing.createLoggerFor(CommentAndRatingLoggingAction.class);
	
	private static final ResourceableTypeList COMMENT_RATING_RESOURCES = new ResourceableTypeList().
	addMandatory(OlatResourceableType.course, OlatResourceableType.node, OlatResourceableType.feedItem)
	.or().addMandatory(OlatResourceableType.feedItem);

	public static final ILoggingAction COMMENT_CREATED = new CommentAndRatingLoggingAction(ActionType.tracking, CrudAction.create, ActionVerb.add,
			ActionObject.comment).setTypeList(COMMENT_RATING_RESOURCES);
	public static final ILoggingAction COMMENT_DELETED = new CommentAndRatingLoggingAction(ActionType.tracking, CrudAction.delete, ActionVerb.remove,
			ActionObject.comment).setTypeList(COMMENT_RATING_RESOURCES);
	public static final ILoggingAction RATING_CREATED = new CommentAndRatingLoggingAction(ActionType.tracking, CrudAction.create, ActionVerb.perform,
			ActionObject.rating).setTypeList(COMMENT_RATING_RESOURCES);
	public static final ILoggingAction RATING_UPDATED = new CommentAndRatingLoggingAction(ActionType.tracking, CrudAction.update, ActionVerb.perform,
			ActionObject.rating).setTypeList(COMMENT_RATING_RESOURCES);
	
	/**
	 * This static constructor's only use is to set the javaFieldIdForDebug on all
	 * of the LoggingActions defined in this class.
	 * <p>
	 * This is used to simplify debugging - as it allows to issue (technical) log
	 * statements where the name of the LoggingAction Field is written.
	 */
	static {
		Field[] fields = CommentAndRatingLoggingAction.class.getDeclaredFields();
		if (fields != null) {
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (field.getType() == CommentAndRatingLoggingAction.class) {
					try {
						CommentAndRatingLoggingAction aLoggingAction = (CommentAndRatingLoggingAction) field.get(null);
						aLoggingAction.setJavaFieldIdForDebug(field.getName());
					} catch (IllegalArgumentException | IllegalAccessException e) {
						log.error("", e);
					}
				}
			}
		}
	}

	
	protected CommentAndRatingLoggingAction(ActionType resourceActionType, CrudAction action, ActionVerb actionVerb, ActionObject actionObject) {
		super(resourceActionType, action, actionVerb, actionObject.name());
	}

}
