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
package org.olat.course.nodes.pf.ui;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.course.nodes.PFCourseNode;

/**
 * 
 * Initial date: 7 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PFUIHelper {

	private static final long TWO_DAYS_IN_MILLISEC = 2l * 24l * 60l * 60l * 1000l;
	private static final long ONE_DAY_IN_MILLISEC = 24l * 60l * 60l * 1000l;
	
	protected static TimerComponent initTimeframeMessage(UserRequest ureq, PFCourseNode pfNode, VelocityContainer mainVC,
			Controller ctrl, Translator translator) {
		
		TimerComponent timerCmp = null;
		
		if(pfNode.hasDropboxTimeFrameConfigured() && pfNode.getDateStart() != null && pfNode.getDateEnd() != null) {
			Date start = pfNode.getDateStart();
			Date end = pfNode.getDateEnd();
			Date now = ureq.getRequestTimestamp();

			Formatter formatter = Formatter.getInstance(translator.getLocale());
			String[] args = new String[] {
				formatter.formatDate(start), 		// 0 start date
				formatter.formatTimeShort(start), 	// 1 start time
				formatter.formatDate(end),			// 2 end date
				formatter.formatTimeShort(end)		// 3 end time
			};
			
			String i18nKey;
			String cssClass;
			if(now.before(start)) {
				cssClass = "o_info";
				i18nKey = "msg.period.before";
			} else if(now.after(start) && now.before(end)) {
				long timeDiff = end.getTime() - now.getTime();
				if(timeDiff <= 0) {
					cssClass = "o_info";
					i18nKey = "msg.period.after";
				} else if(timeDiff > TWO_DAYS_IN_MILLISEC) {// 2 days		
					cssClass = "o_info";
					i18nKey = "msg.period.within";
				} else if(timeDiff > ONE_DAY_IN_MILLISEC) {				
					cssClass = "o_warning";
					i18nKey = "msg.period.within";
				} else {
					cssClass = "o_error";
					i18nKey = "msg.period.within.oneday";
					timerCmp = new TimerComponent("timer", end);
					timerCmp.addListener(ctrl);
					mainVC.put("timer", timerCmp);
				}
			} else {
				cssClass = "o_info";
				i18nKey = "msg.period.after";
			}
			
			String msg = translator.translate(i18nKey, args);
			mainVC.contextPut("msg", msg);
			mainVC.contextPut("msgCssClass", cssClass);
		}
		return timerCmp;
	}

}
