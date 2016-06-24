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
import org.olat.modules.portfolio.handler.AbstractMediaHandler;
import org.olat.modules.portfolio.manager.MediaDAO;
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
	public String getIconCssClass(Media media) {
		return "o_icon_certificate";
	}

	@Override
	public VFSLeaf getThumbnail(Media media, Size size) {
		return null;
	}

	@Override
	public Media createMedia(String title, String description, Object mediaObject, String businessPath, Identity author) {
		Media media = null;
		if (mediaObject instanceof EfficiencyStatement){
			EfficiencyStatement statement = (EfficiencyStatement) mediaObject;
			String xml = myXStream.toXML(statement); 
			media = mediaDao.createMedia(title, description, xml, EFF_MEDIA, businessPath, 90, author);
		}
		return media;
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
}
