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

package org.olat.course.assessment.portfolio;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.assessment.EfficiencyStatement;
import org.olat.course.certificate.ui.CertificateAndEfficiencyStatementController;
import org.olat.portfolio.EPAbstractHandler;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.search.service.SearchResourceContext;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Description:<br>
 * EvidenceArtefactHandler
 * 
 * <P>
 * Initial Date:  7 oct. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EfficiencyStatementArtefactHandler extends EPAbstractHandler<EfficiencyStatementArtefact> {

	private static final Logger log = Tracing.createLoggerFor(EfficiencyStatementArtefactHandler.class);
	
	private final XStream myXStream = XStreamHelper.createXStreamInstance();
	
	@Override
	public String getType() {
		return EfficiencyStatementArtefact.ARTEFACT_TYPE;
	}

	@Override
	public EfficiencyStatementArtefact createArtefact() {
		return new EfficiencyStatementArtefact();
	}
	
	/**
	 * @see org.olat.portfolio.EPAbstractHandler#prefillArtefactAccordingToSource(org.olat.portfolio.model.artefacts.AbstractArtefact, java.lang.Object)
	 */
	@Override
	public void prefillArtefactAccordingToSource(AbstractArtefact artefact, Object source) {
		super.prefillArtefactAccordingToSource(artefact, source);

		if (source instanceof EfficiencyStatement){
			EfficiencyStatement statement = (EfficiencyStatement) source;
			if(artefact.getTitle() == null) {
				artefact.setTitle(statement.getCourseTitle());
			}
			String efficiencyStatementX = myXStream.toXML(statement); 
			artefact.setSource(statement.getCourseTitle());
			artefact.setFulltextContent(efficiencyStatementX);
			artefact.setSignature(90);
		}
	}

	@Override
	public Controller createDetailsController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact, boolean readOnlyMode) {
		EPFrontendManager ePFMgr = CoreSpringFactory.getImpl(EPFrontendManager.class);
		String statementXml = ePFMgr.getArtefactFullTextContent(artefact);
		EfficiencyStatement statement = null;
		if(StringHelper.containsNonWhitespace(statementXml)) {
			try {
				statement = (EfficiencyStatement)myXStream.fromXML(statementXml);
			} catch (Exception e) {
				log.error("Cannot load efficiency statement from artefact", e);
			}
		}
		CertificateAndEfficiencyStatementController efficiencyCtrl = new CertificateAndEfficiencyStatementController(wControl, ureq, statement);
		return new LayoutMain3ColsController(ureq, wControl, efficiencyCtrl);
	}

	@Override
	protected void getContent(AbstractArtefact artefact, StringBuilder sb, SearchResourceContext context, EPFrontendManager ePFManager) {
		String statementXml = ePFManager.getArtefactFullTextContent(artefact);
		if(!StringHelper.containsNonWhitespace(statementXml)) return;
		
	  try {
			EfficiencyStatement statement = (EfficiencyStatement)myXStream.fromXML(statementXml);
			sb.append(statement.getCourseTitle()).append(' ');
			sb.append(statement.getDisplayableUserInfo()).append(' ');
	  } catch(Exception ex) {
	  	log.error("Error while parsing " + artefact, ex);
	  }
	}
}
