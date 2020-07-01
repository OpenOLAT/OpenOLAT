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
package org.olat.modules.coach.site;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.navigation.AbstractSiteInstance;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.StateSite;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.CoachingSecurity;
import org.olat.modules.coach.ui.CoachMainController;
import org.olat.modules.grading.GradingService;
import org.olat.modules.grading.model.GradingSecurity;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  7 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CoachSite extends AbstractSiteInstance {
	
	private static final OLATResourceable coachOres = OresHelper.createOLATResourceableInstance(CoachSite.class, 0l);
	private static final String coachBusinessPath = OresHelper.toBusinessPath(coachOres);
	
	private final NavElement origNavElem;
	private NavElement curNavElem;
	
	private GradingSecurity gradingSec;
	private CoachingSecurity coachingSec;
	
	/**
	 * @param loccale
	 */
	public CoachSite(SiteDefinition siteDef, CoachingSecurity coachingSec, GradingSecurity gradingSec, Locale locale) {
		super(siteDef);
		this.gradingSec = gradingSec;
		this.coachingSec = coachingSec;
		Translator trans = Util.createPackageTranslator(CoachMainController.class, locale);
		origNavElem = new DefaultNavElement(coachBusinessPath, trans.translate("site.title"),
				trans.translate("site.title.alt"), "o_site_coaching");
		curNavElem = new DefaultNavElement(origNavElem);
	}

	@Override
	public NavElement getNavElement() {
		return curNavElem;
	}

	@Override
	protected MainLayoutController createController(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(coachOres));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, coachOres, new StateSite(this), wControl, true);
		if(coachingSec == null) {
			coachingSec = CoreSpringFactory.getImpl(CoachingService.class).isCoach(ureq.getIdentity());
		}
		if(gradingSec == null) {
			Roles roles = ureq.getUserSession().getRoles();
			gradingSec = CoreSpringFactory.getImpl(GradingService.class).isGrader(ureq.getIdentity(), roles);
		}
		return new CoachMainController(ureq, bwControl, coachingSec, gradingSec);
	}

	@Override
	public void reset() {
		curNavElem = new DefaultNavElement(origNavElem);
	}
}
