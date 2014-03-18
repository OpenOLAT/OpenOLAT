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
package org.olat.portfolio;

import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;

/**
 * 
 * Description:<br>
 * each artefact type has its handler
 * 
 * <P>
 * Initial Date:  28.07.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public interface EPArtefactHandler<U extends AbstractArtefact> extends ConfigOnOff {
	
	/**
	 * get an artefact-type identifier, should be according to OLATresourcable.getResourceableTypeName()
	 * @return
	 */
	public String getType();
	
	public U createArtefact();
	
	/**
	 * as each artefact handler knows best how to convert the source to an artefact, he should take care of 
	 * pre-filling the artefact
	 * @param artefact
	 * @param source
	 */
	public void prefillArtefactAccordingToSource(AbstractArtefact artefact, Object source);
	
	/**
	 * get back an translator setup for the corresponding handler
	 * @param fallBackTrans - your yet existing translator
	 * @return
	 */
	public Translator getHandlerTranslator(Translator fallBackTrans);
	
	/**
	 * create a controller to present / manipulate artefact-specific stuff
	 * @param ureq
	 * @return
	 */
	public Controller createDetailsController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact, boolean readOnlyMode);

	/**
	 * override with true, if your artefact-type provides a non-generic view while displayed in a map
	 * this is yet used for live-blog-artefact
	 * @return
	 */
	public boolean isProvidingSpecialMapViewController();
	
	/**
	 * return the controller to display the non-generic view
	 * is only used if isProvidingSpecialMapViewController() is true
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public Controller getSpecialMapViewController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact);
	
	/**
	 * Text from the original source - can be used for tag-proposition, as reflexion or description
	 * @param artefact
	 * @return
	 */
	public OlatDocument getIndexerDocument(SearchResourceContext resourceContext, AbstractArtefact artefact, EPFrontendManager ePFManager);
	
}
