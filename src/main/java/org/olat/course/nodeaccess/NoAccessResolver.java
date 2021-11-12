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
package org.olat.course.nodeaccess;

import java.util.Date;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.Formatter;
import org.olat.course.nodes.CourseNode;

/**
 * 
 * Initial date: 12 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface NoAccessResolver {
	
	public enum NoAccessReason { condition, previousNotDone, startDateInFuture, unknown }
	
	public NoAccess getNoAccessMessage(CourseNode courseNode);
	
	public static NoAccess condition(String translatedMessage) {
		return new NoAccess(NoAccessReason.condition, translatedMessage, null, null);
	}
	
	public static NoAccess unknown() {
		return NoAccess.UNKNOWN;
	}
	
	public static NoAccess previousNotDone(String goToNodeIdent) {
		return new NoAccess(NoAccessReason.previousNotDone, null, goToNodeIdent, null);
	}
	
	public static NoAccess startDateInFuture(Date date) {
		return new NoAccess(NoAccessReason.startDateInFuture, null, null, date);
	}
	
	public class NoAccess {
		
		private static final NoAccess UNKNOWN = new NoAccess(NoAccessReason.unknown, null, null, null);
		
		private final NoAccessReason reason;
		private final String translatedMessage;
		private final String goToNodeIdent;
		private final Date date;
		
		private NoAccess(NoAccessReason reason, String translatedMessage, String goToNodeIdent, Date date) {
			this.reason = reason;
			this.translatedMessage = translatedMessage;
			this.goToNodeIdent = goToNodeIdent;
			this.date = date;
		}
		
		public NoAccessReason getReason() {
			return reason;
		}
		
		public String getTranslatedMessage() {
			return translatedMessage;
		}
		
		public String getGoToNodeIdent() {
			return goToNodeIdent;
		}
		
		public Date getDate() {
			return date;
		}
		
	}

	public static String translate(Translator translator, NoAccess noAccessMessage, boolean shortMessage) {
		if (noAccessMessage == null) return null;
		
		String i18nSuffix = shortMessage? ".short": ".long";
		String i18nKey = "no.access." + noAccessMessage.getReason().name() + i18nSuffix;
		
		String[] args = ArrayHelper.emptyStrings();
		if (!shortMessage) {
			// Translated message may be empty string.
			if (noAccessMessage.getTranslatedMessage() != null) {
				args = new String[] { noAccessMessage.getTranslatedMessage() };
			} else if (noAccessMessage.getDate() != null) {
				args = new String[] { Formatter.getInstance(translator.getLocale()).formatDateAndTime(noAccessMessage.getDate()) };
			}
		}

		return translator.translate(i18nKey, args);
	}

}
