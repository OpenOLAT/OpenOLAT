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
package org.olat.course.learningpath.obligation;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.Structure;
import org.olat.course.assessment.ScoreAccountingTriggerData;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.ObligationContext;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class UserPropertyExceptionalObligationHandler implements ExceptionalObligationHandler {
	
	public static final String TYPE = "user.property";
	public static final String USER_PROPS_ID = UserPropertyExceptionalObligationHandler.class.getName();
	
	@Autowired
	private UserManager userManager;
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public int getSortValue() {
		return 50;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getAddI18nKey() {
		return "exceptional.obligation.user.property.add";
	}
	
	@Override
	public boolean isShowAdd(RepositoryEntry courseEntry) {
		return isEnabled();
	}

	@Override
	public String getDisplayType(Translator translator, ExceptionalObligation exceptionalObligation) {
		return translator.translate("exceptional.obligation.user.property.type");
	}

	@Override
	public String getDisplayName(Translator translator, ExceptionalObligation exceptionalObligation, RepositoryEntry courseEntry) {
		if (exceptionalObligation instanceof UserPropertyExceptionalObligation) {
			UserPropertyExceptionalObligation userPropertyExceptionalObligation = (UserPropertyExceptionalObligation)exceptionalObligation;
			UserPropertyHandler userPropertyHandler = getUserPropertyHandler(userPropertyExceptionalObligation.getPropertyName());
			if (userPropertyHandler != null) {
				Translator trans = Util.createPackageTranslator(UserPropertyHandler.class, translator.getLocale(), translator);
				String translatedPropertyName = trans.translate(userPropertyHandler.i18nFormElementLabelKey());
				String value = userPropertyExceptionalObligation.getValue();
				return trans.translate("exceptional.obligation.user.property.name", new String[] {translatedPropertyName, value});
			}
		}
		return null;
	}
	
	@Override
	public String getDisplayText(Translator translator, ExceptionalObligation exceptionalObligation, RepositoryEntry courseEntry) {
		if (exceptionalObligation instanceof UserPropertyExceptionalObligation) {
			UserPropertyExceptionalObligation userPropertyExceptionalObligation = (UserPropertyExceptionalObligation)exceptionalObligation;
			UserPropertyHandler userPropertyHandler = getUserPropertyHandler(userPropertyExceptionalObligation.getPropertyName());
			if (userPropertyHandler != null) {
				Translator trans = Util.createPackageTranslator(UserPropertyHandler.class, translator.getLocale(), translator);
				String translatedPropertyName = trans.translate(userPropertyHandler.i18nFormElementLabelKey());
				String value = userPropertyExceptionalObligation.getValue();
				return translator.translate("exceptional.obligation.user.property.display", new String[] {translatedPropertyName, value});
			}
		}
		return null;
	}

	@Override
	public boolean hasScoreAccountingTrigger() {
		return true;
	}

	@Override
	public ScoreAccountingTriggerData getScoreAccountingTriggerData(ExceptionalObligation exceptionalObligation) {
		if (exceptionalObligation instanceof UserPropertyExceptionalObligation) {
			UserPropertyExceptionalObligation userPropertyExceptionalObligation = (UserPropertyExceptionalObligation)exceptionalObligation;
			ScoreAccountingTriggerData data = new ScoreAccountingTriggerData();
			data.setUserPropertyName(userPropertyExceptionalObligation.getPropertyName());
			data.setUserPropertyValue(userPropertyExceptionalObligation.getValue());
			return data;
		}
		return null;
	}

	@Override
	public boolean matchesIdentity(ExceptionalObligation exceptionalObligation, Identity identity,
			ObligationContext obligationContext, RepositoryEntryRef courseEntry, Structure runStructure, ScoreAccounting scoreAccounting) {
		if (exceptionalObligation instanceof UserPropertyExceptionalObligation) {
			UserPropertyExceptionalObligation userPropertyExceptionalObligation = (UserPropertyExceptionalObligation)exceptionalObligation;
			String propertyValue = identity.getUser().getProperty(userPropertyExceptionalObligation.getPropertyName());
			return userPropertyExceptionalObligation.getValue().equalsIgnoreCase(propertyValue);
		}
		return false;
	}

	@Override
	public ExceptionalObligationController createCreationController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, CourseNode courseNode) {
		return new UserPropertyExceptionalObligationController(ureq, wControl);
	}

	private UserPropertyHandler getUserPropertyHandler(String handlerName) {
		for (UserPropertyHandler userPropertyHandler : userManager.getUserPropertyHandlersFor(USER_PROPS_ID, false)) {
			if (handlerName.equals(userPropertyHandler.getName())) {
				return userPropertyHandler;
			}
		}
		return null;
	}

}
