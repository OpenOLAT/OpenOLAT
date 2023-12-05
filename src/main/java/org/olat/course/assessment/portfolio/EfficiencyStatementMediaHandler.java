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

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.gui.util.WindowControlMocker;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.EfficiencyStatement;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.course.assessment.ui.tool.IdentityAssessmentOverviewController;
import org.olat.course.certificate.ui.CertificateAndEfficiencyStatementController;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaInformations;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaLoggingAction;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.handler.AbstractMediaHandler;
import org.olat.modules.cemedia.manager.MediaDAO;
import org.olat.modules.cemedia.manager.MediaLogDAO;
import org.olat.modules.cemedia.ui.medias.StandardEditMediaController;
import org.olat.user.manager.ManifestBuilder;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 24.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EfficiencyStatementMediaHandler extends AbstractMediaHandler {
	
	private static final Logger log = Tracing.createLoggerFor(EfficiencyStatementMediaHandler.class);

	public static final String EFF_MEDIA = "EfficiencyStatement";
	
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private MediaLogDAO mediaLogDao;
	
	public EfficiencyStatementMediaHandler() {
		super(EFF_MEDIA);
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_certificate";
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.content;
	}
	
	@Override
	public boolean acceptMimeType(String mimeType) {
		return false;
	}

	@Override
	public VFSLeaf getThumbnail(MediaVersion media, Size size) {
		return null;
	}

	@Override
	public MediaInformations getInformations(Object mediaObject) {
		String title = null;
		if (mediaObject instanceof EfficiencyStatement statement) {
			title = statement.getCourseTitle();
		}
		return new Informations(title, null);
	}

	@Override
	public Media createMedia(String title, String description, String altText, Object mediaObject, String businessPath,
			Identity author, MediaLog.Action action) {
		Media media = null;
		if (mediaObject instanceof EfficiencyStatement statement) {
			String xml = EfficiencyStatementManager.toXML(statement); 
			media = mediaDao.createMediaAndVersion(title, description, altText, xml, EFF_MEDIA, businessPath, null, 90, author);
			ThreadLocalUserActivityLogger.log(MediaLoggingAction.CE_MEDIA_ADDED, getClass(),
					LoggingResourceable.wrap(media));
			mediaLogDao.createLog(action, null, media, author);
		}
		return media;
	}

	@Override
	public Controller getMediaController(UserRequest ureq, WindowControl wControl, MediaVersion version, RenderingHints hints) {
		String statementXml = version.getContent();
		EfficiencyStatement statement = null;
		if(StringHelper.containsNonWhitespace(statementXml)) {
			try {
				statement = EfficiencyStatementManager.fromXML(statementXml);
			} catch (Exception e) {
				log.error("Cannot load efficiency statement from artefact", e);
			}
		}
		CertificateAndEfficiencyStatementController ctrl =  new CertificateAndEfficiencyStatementController(wControl, ureq, statement);
		ctrl.disableMediaCollector();
		return ctrl;
	}

	@Override
	public Controller getEditMetadataController(UserRequest ureq, WindowControl wControl, Media media) {
		return new StandardEditMediaController(ureq, wControl, media);
	}

	@Override
	public void export(Media media, ManifestBuilder manifest, File mediaArchiveDirectory, Locale locale) {
		EfficiencyStatement statement = null;
		List<MediaVersion> versions = media.getVersions();
		if(!versions.isEmpty() && StringHelper.containsNonWhitespace(versions.get(0).getContent())) {
			try {
				statement = EfficiencyStatementManager.fromXML(versions.get(0).getContent());
			} catch (Exception e) {
				log.error("Cannot load efficiency statement from artefact", e);
			}
		}
		
		if(statement != null) {
			List<Map<String,Object>> assessmentNodes = statement.getAssessmentNodes();
			List<AssessmentNodeData> assessmentNodeList = AssessmentHelper.assessmentNodeDataMapToList(assessmentNodes);
			SyntheticUserRequest ureq = new SyntheticUserRequest(media.getAuthor(), locale);
			IdentityAssessmentOverviewController details = new IdentityAssessmentOverviewController(ureq, new WindowControlMocker(), assessmentNodeList, null, null);
			super.exportContent(media, details.getInitialComponent(), null, mediaArchiveDirectory, locale);
		}
	}
}
