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
package org.olat.catalog;

import java.util.Locale;

import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.dispatcher.jumpin.JumpInHandlerFactory;
import org.olat.core.dispatcher.jumpin.JumpInReceptionist;
import org.olat.core.dispatcher.jumpin.JumpInResult;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.site.RepositorySite;

/**
 * Description:<br>
 * Jump in handler factory for bookmark jump in url.
 * 
 * <P>
 * Initial Date: 28.05.2008 <br>
 * 
 * @author gnaegi
 */
public class CatalogJumpInHandlerFactory implements JumpInHandlerFactory {

	public static final String CONST_CAID = "caid";
	private static final String CONST_EXTLINK = "cata/go";

	public JumpInReceptionist createJumpInHandler(UserRequest ureq) {
		String catEntryId = ureq.getParameter(CONST_CAID);
		return new CatalogJumpInReceptionist(catEntryId);
	}

	/**
	 * Build a dispatch URI which a user can use to call re directly by entering
	 * the dispatch URI into his/her browser location bar.
	 * 
	 * @param catEntryId The catalog entry key
	 * @return Complete dispatch URI.
	 */
	public static String buildRepositoryDispatchURI(long catEntryId) {
		StringBuffer sb = new StringBuffer();
		sb.append(Settings.getServerContextPathURI()).append(DispatcherAction.PATH_AUTHENTICATED).append(CONST_EXTLINK).append("?").append(
				CONST_CAID).append("=").append(catEntryId);
		return sb.toString();
	}

	/**
	 * Build a dispatch URI which a guest user can use to call re directly by
	 * entering the dispatch URI into his/her browser location bar.
	 * <p>
	 * When displaying the URL in a HTML page, don't forget to format the URL with
	 * Formatter.esc() because this URL contains & characters.
	 * 
	 * @param catEntryId The catalog entry key
	 * @param loc The users language
	 * @return Complete dispatch URI.
	 */
	public static String buildRepositoryDispatchGuestURI(long catEntryId, Locale loc) {
		StringBuffer sb = new StringBuffer();
		sb.append(buildRepositoryDispatchURI(catEntryId)).append("&guest=true&lang=").append(loc.toString());
		return sb.toString();
	}

}

/**
 * Description:<br>
 * Implementation of the jump in receptionist for catalog entries
 * <P>
 * Initial Date: 28.05.2008 <br>
 * 
 * @author gnaegi
 */
class CatalogJumpInReceptionist implements JumpInReceptionist {
	private String catalogId;
	// the ores for the jump in calls is the repository site. This one should be
	// activated when a user jumps into olat using such a link
	private static final OLATResourceable ores = OresHelper.createOLATResourceableType(RepositorySite.class);

	/**
	 * Constructor
	 * 
	 * @param catalogId The catalog key as String
	 */
	protected CatalogJumpInReceptionist(String catalogId) {
		this.catalogId = catalogId;
	}

	/**
	 * @see org.olat.core.dispatcher.jumpin.JumpInReceptionist#createJumpInResult(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	public JumpInResult createJumpInResult(UserRequest ureq, @SuppressWarnings("unused")
	WindowControl control) {
		String nodeId = ureq.getParameter(CatalogJumpInHandlerFactory.CONST_CAID);
		// encode sub view identifyer using ":" character
		return new JumpInResult(null, "search.catalog:" + nodeId);
	}

	/**
	 * @see org.olat.core.dispatcher.jumpin.JumpInReceptionist#extractActiveViewId(org.olat.core.gui.UserRequest)
	 */
	public String extractActiveViewId(UserRequest ureq) {
		String nodeId = ureq.getParameter(CatalogJumpInHandlerFactory.CONST_CAID);
		return nodeId;
	}

	/**
	 * @see org.olat.core.dispatcher.jumpin.JumpInReceptionist#getOLATResourceable()
	 */
	public OLATResourceable getOLATResourceable() {
		// return the RepositorySite ores
		return ores;
	}

	/**
	 * @see org.olat.core.dispatcher.jumpin.JumpInReceptionist#getTitle()
	 */
	public String getTitle() {
		return this.catalogId;
	}
}
