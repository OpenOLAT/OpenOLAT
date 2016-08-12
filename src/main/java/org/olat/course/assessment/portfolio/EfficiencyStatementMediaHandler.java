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

import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.assessment.EfficiencyStatement;
import org.olat.course.certificate.ui.CertificateAndEfficiencyStatementController;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaInformations;
import org.olat.modules.portfolio.MediaLight;
import org.olat.modules.portfolio.handler.AbstractMediaHandler;
import org.olat.modules.portfolio.manager.MediaDAO;
import org.olat.modules.portfolio.ui.media.StandardEditMediaController;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Initial date: 24.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EfficiencyStatementMediaHandler extends AbstractMediaHandler {
	
	private static final OLog log = Tracing.createLoggerFor(EfficiencyStatementMediaHandler.class);
	private static final XStream myXStream = XStreamHelper.createXStreamInstance();
	
	public static final String EFF_MEDIA = "EfficiencyStatement";
	
	@Autowired
	private MediaDAO mediaDao;
	
	public EfficiencyStatementMediaHandler() {
		super(EFF_MEDIA);
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_certificate";
	}
	
	@Override
	public boolean acceptMimeType(String mimeType) {
		return false;
	}

	@Override
	public VFSLeaf getThumbnail(MediaLight media, Size size) {
		return null;
	}

	@Override
	public MediaInformations getInformations(Object mediaObject) {
		String title = null;
		if (mediaObject instanceof EfficiencyStatement) {
			title = ((EfficiencyStatement)mediaObject).getCourseTitle();
		}
		return new Informations(title, null);
	}

	@Override
	public Media createMedia(String title, String description, Object mediaObject, String businessPath, Identity author) {
		Media media = null;
		if (mediaObject instanceof EfficiencyStatement) {
			EfficiencyStatement statement = (EfficiencyStatement) mediaObject;
			String xml = myXStream.toXML(statement); 
			media = mediaDao.createMedia(title, description, xml, EFF_MEDIA, businessPath, null, 90, author);
		}
		return media;
	}

	@Override
	public Media createMedia(AbstractArtefact artefact) {
		String title = artefact.getTitle();
		String description = artefact.getDescription();
		String xml = artefact.getFulltextContent();
		String businessPath = artefact.getBusinessPath();
		if(businessPath == null) {
			businessPath = "[PortfolioV2:0][MediaCenter:0]";
		}
		return mediaDao.createMedia(title, description, xml, EFF_MEDIA, businessPath, artefact.getKey().toString(), artefact.getSignature(), artefact.getAuthor());
	}

	@Override
	public Controller getMediaController(UserRequest ureq, WindowControl wControl, Media media) {
		String statementXml = media.getContent();
		EfficiencyStatement statement = null;
		if(StringHelper.containsNonWhitespace(statementXml)) {
			try {
				statement = (EfficiencyStatement)myXStream.fromXML(statementXml);
			} catch (Exception e) {
				log.error("Cannot load efficiency statement from artefact", e);
			}
		}
		CertificateAndEfficiencyStatementController ctrl =  new CertificateAndEfficiencyStatementController(wControl, ureq, statement);
		ctrl.disableMediaCollector();
		return ctrl;
	}

	@Override
	public Controller getEditMediaController(UserRequest ureq, WindowControl wControl, Media media) {
		return new StandardEditMediaController(ureq, wControl, media);
	}
}
