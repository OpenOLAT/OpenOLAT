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

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;

/**
 * 
 * Description:<br>
 * reason to have this abstract between interface and concrete implementation is to swap out common code here.
 * 
 * <P>
 * Initial Date:  11.06.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public abstract class EPAbstractHandler<U extends AbstractArtefact> implements EPArtefactHandler<U> {

	private boolean enabled = true;
	
	public EPAbstractHandler() {
		//
	}
	
	/**
	 * @see org.olat.portfolio.EPArtefactHandler#prefillArtefactAccordingToSource(org.olat.portfolio.model.artefacts.AbstractArtefact, java.lang.Object)
	 */
	@Override
	public void prefillArtefactAccordingToSource(AbstractArtefact artefact, Object source) {
		if (source instanceof OLATResourceable){
			OLATResourceable ores = (OLATResourceable) source;
			artefact.setSource(ores.getResourceableTypeName());
		}
		artefact.setCollectionDate(new Date());		
	}
	/**
	 * @see org.olat.portfolio.EPArtefactHandler#getType()
	 */
	@Override
	public abstract String getType();
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @see org.olat.portfolio.EPArtefactHandler#getHandlerTranslator(org.olat.core.gui.translator.Translator)
	 */
	@Override
	public Translator getHandlerTranslator(Translator fallBackTrans){
		return Util.createPackageTranslator(this.getClass(), fallBackTrans.getLocale(), fallBackTrans);
	}
	
	@Override
	public Controller createDetailsController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact, boolean readOnlyMode){
		return null;
	}

	public abstract U createArtefact();

	/**
	 * @return Returns the providesSpecialMapViewController.
	 */
	@Override
	public boolean isProvidingSpecialMapViewController() {
		return false;
	}
	
	@Override
	public Controller getSpecialMapViewController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact) {
		return null;
	}

	@Override
	public OlatDocument getIndexerDocument(SearchResourceContext searchResourceContext, AbstractArtefact artefact, EPFrontendManager ePFManager) {
		
		OlatDocument document = new OlatDocument();

		Identity author = artefact.getAuthor();
		if(author != null) {
			document.setAuthor(author.getName());
		}
		
		Filter filter = FilterFactory.getHtmlTagAndDescapingFilter();
		
		document.setCreatedDate(artefact.getCreationDate());
  	document.setTitle(filter.filter(artefact.getTitle()));
  	document.setDescription(filter.filter(artefact.getDescription()));
		document.setResourceUrl(searchResourceContext.getResourceUrl());
		document.setDocumentType(searchResourceContext.getDocumentType());
		document.setCssIcon(artefact.getIcon());
		document.setParentContextType(searchResourceContext.getParentContextType());
		document.setParentContextName(searchResourceContext.getParentContextName());

		StringBuilder sb = new StringBuilder();
		if(artefact.getReflexion() != null) {
			sb.append(artefact.getReflexion()).append(' ');
		}
		getContent(artefact, sb, searchResourceContext, ePFManager);
		document.setContent(sb.toString());
		return document;
	}
	
	@SuppressWarnings("unused")
	protected void getContent(AbstractArtefact artefact, StringBuilder sb, SearchResourceContext context, EPFrontendManager ePFManager) {
		String content = ePFManager.getArtefactFullTextContent(artefact);
		if(content != null) {
			sb.append(content);
		}
	}
}
