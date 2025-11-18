/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.ims.qti21.resultexport;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.admin.user.imp.TransientIdentity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.pdf.PdfOutputOptions;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentTestHelper;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.ParentPartItemRefs;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.ui.assessment.CorrectionIdentityAssessmentItemPrintController;
import org.olat.ims.qti21.ui.assessment.CorrectionOverviewModel;
import org.olat.ims.qti21.ui.assessment.model.AssessmentItemCorrection;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;

/**
 * 
 * Initial date: Nov 13, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EssaysPdfMediaResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(EssaysPdfMediaResource.class);

	private final CorrectionOverviewModel model;
	private final AssessmentItemRef itemRef;
	private final ResolvedAssessmentTest resolvedAssessmentTest;
	private final ResolvedAssessmentItem resolvedAssessmentItem;
	private final AssessmentItem assessmentItem;
	private final boolean anonymous;

	private final Locale locale;
	private final Identity identity;
	private final WindowControl windowControl;
	private final UserRequest ureq;

	@Autowired
	private PdfService pdfService;
	@Autowired
	private QTI21Service qtiService;

	public EssaysPdfMediaResource(CorrectionOverviewModel model, AssessmentItemRef itemRef, boolean anonymous,
			Locale locale, Identity identity, WindowControl windowControl) {
		this.model = model;
		this.itemRef = itemRef;
		this.anonymous = anonymous;
		this.locale = locale;
		this.identity = identity;
		this.windowControl = windowControl;
		ureq = new SyntheticUserRequest(new TransientIdentity(), locale, new UserSession());
		ureq.getUserSession().setRoles(Roles.userRoles());
		
		resolvedAssessmentTest = model.getResolvedAssessmentTest();
		resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
		assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
		
		CoreSpringFactory.autowireObject(this);
	}

	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_NO_CACHE;
	}

	@Override
	public boolean acceptRanges() {
		return false;
	}

	@Override
	public String getContentType() {
		return "application/zip";
	}

	@Override
	public Long getSize() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		String urlEncodedLabel = StringHelper.urlEncodeUTF8(StringHelper.transformDisplayNameToFileSystemName("essays") + ".zip");
		hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + urlEncodedLabel);
		hres.setHeader("Content-Description", urlEncodedLabel);
		
		try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			zout.setLevel(9);
			
			String itemRefIdentifier = itemRef.getIdentifier().toString();
			List<AssessmentItemSession> allItemSessions = qtiService
					.getAssessmentItemSessions(model.getCourseEntry(), model.getSubIdent(), model.getTestEntry(), itemRefIdentifier);
			Map<AssessmentTestSession,AssessmentItemSession> testToItemSession = new HashMap<>();
			for(AssessmentItemSession itemSession:allItemSessions) {
				AssessmentTestSession testSession = itemSession.getAssessmentTestSession();
				testToItemSession.put(testSession, itemSession);
			}
			
			List<Identity> assessedIdentities = model.getAssessedIdentities();
			for(Identity assessedIdentity:assessedIdentities) {
				AssessmentTestSession testSession = model.getLastSessions().get(assessedIdentity);
				TestSessionState testSessionState = model.getTestSessionStates().get(assessedIdentity);
				if(testSession != null && testSessionState != null) {
					List<TestPlanNode> nodes = testSessionState.getTestPlan().getNodes(itemRef.getIdentifier());
					if(nodes != null) {
						AssessmentItemSession itemSession = testToItemSession.get(testSession);
						if(itemSession == null) {
							TestPlanNode itemNode = nodes.get(0);
							String stringuifiedIdentifier = itemNode.getKey().getIdentifier().toString();
							ParentPartItemRefs parentParts = AssessmentTestHelper.getParentSection(itemNode.getKey(),
									testSessionState, model.getResolvedAssessmentTest());
							itemSession = qtiService.getOrCreateAssessmentItemSession(testSession, parentParts,
									stringuifiedIdentifier, itemRef.getIdentifier().toString());
						}
						
						if (itemSession != null) {
							TestPlanNode itemNode = nodes.get(0);
							ItemSessionState itemSessionState = testSessionState.getItemSessionStates().get(itemNode.getKey());
							AssessmentItemCorrection itemCorrection = new AssessmentItemCorrection(assessedIdentity, 
									testSession, testSessionState, itemSession, itemSessionState,
									itemRef, itemNode);
							itemCorrection.setItemSession(itemSession);
							
							String userDisplayIdentifier = anonymous
									? userDisplayIdentifier = model.getAnonymizedName(itemCorrection.getAssessedIdentity())
									: null;
							
							String filename = createPdfFilename(assessmentItem, testSession, assessedIdentity, userDisplayIdentifier);
							createEssayPDF(zout, filename, itemCorrection, userDisplayIdentifier);
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Error during pdf export of essays", e);
		}
	}
	
	private void createEssayPDF(ZipOutputStream zout, String path, AssessmentItemCorrection itemCorrection, String userDisplayIdentifier) {
		try {
			ControllerCreator creator = (lureq, lwControl) -> {
				lureq = new SyntheticUserRequest(ureq.getIdentity(), locale, ureq.getUserSession());
				return new CorrectionIdentityAssessmentItemPrintController(lureq, lwControl, model.getTestEntry(),
						resolvedAssessmentTest, resolvedAssessmentItem, model, itemCorrection, userDisplayIdentifier);
			};
			
			zout.putNextEntry(new ZipEntry(path));
			pdfService.convert(identity, creator, windowControl, PdfOutputOptions.defaultOptions(), zout);
			zout.closeEntry();
		} catch(Exception e) {
			log.error("", e);
		}
	}
	
	public static final String createPdfFilename(AssessmentItem assessmentItem, AssessmentTestSession candidateSession,
			Identity assessedIdentity, String userDisplayIdentifier) {
		StringBuilder sb = new StringBuilder();
		sb.append(candidateSession.getKey());
		sb.append("_");
		sb.append(QTI21QuestionType.getType(assessmentItem).name());
		sb.append("_");
		if (StringHelper.containsNonWhitespace(assessmentItem.getTitle())) {
			sb.append(StringHelper.transformDisplayNameToFileSystemName(assessmentItem.getTitle()));
			sb.append("_");
		}
		if (StringHelper.containsNonWhitespace(userDisplayIdentifier)) {
			sb.append(userDisplayIdentifier);
		} else {
			User assessedUser = assessedIdentity.getUser();
			sb.append(StringHelper.transformDisplayNameToFileSystemName(assessedUser.getLastName()));
			sb.append("_");
			sb.append(StringHelper.transformDisplayNameToFileSystemName(assessedUser.getFirstName()));
			sb.append("_");
			sb.append(StringHelper.transformDisplayNameToFileSystemName(assessedUser.getProperty(UserConstants.NICKNAME)));
		}
		sb.append(".pdf");
		return sb.toString();
	}
	
	@Override
	public void release() {
		//
	}

}
