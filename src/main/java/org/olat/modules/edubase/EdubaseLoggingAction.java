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
package org.olat.modules.edubase;

import java.lang.reflect.Field;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingLoggingAction;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ActionObject;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.logging.activity.ActionVerb;
import org.olat.core.logging.activity.BaseLoggingAction;
import org.olat.core.logging.activity.CrudAction;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ResourceableTypeList;

/**
 *
 * Initial date: 20.09.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdubaseLoggingAction extends BaseLoggingAction {
	
	private static final Logger log = Tracing.createLoggerFor(EdubaseLoggingAction.class);

	private static final ResourceableTypeList EDUBASE_RESOURCES = new ResourceableTypeList().
			addMandatory(OlatResourceableType.course, OlatResourceableType.node, OlatResourceableType.edubase);

	public static final ILoggingAction BOOK_SECTION_LAUNCHED =
			new EdubaseLoggingAction(ActionType.tracking, CrudAction.retrieve, ActionVerb.launch, ActionObject.bookSection)
					.setTypeList(EDUBASE_RESOURCES);

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


	protected EdubaseLoggingAction(ActionType resourceActionType, CrudAction action, ActionVerb actionVerb, ActionObject actionObject) {
		super(resourceActionType, action, actionVerb, actionObject.name());
	}
}
