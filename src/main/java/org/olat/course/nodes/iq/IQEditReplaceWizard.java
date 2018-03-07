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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package org.olat.course.nodes.iq;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.velocity.VelocityContext;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.WizardController;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailNotificationEditController;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.fileresource.DownloadeableMediaResource;
import org.olat.ims.qti.QTIResult;
import org.olat.ims.qti.editor.beecom.parser.ItemParser;
import org.olat.ims.qti.export.QTIExportEssayItemFormatConfig;
import org.olat.ims.qti.export.QTIExportFIBItemFormatConfig;
import org.olat.ims.qti.export.QTIExportFormatter;
import org.olat.ims.qti.export.QTIExportFormatterCSVType1;
import org.olat.ims.qti.export.QTIExportFormatterCSVType3;
import org.olat.ims.qti.export.QTIExportItemFormatConfig;
import org.olat.ims.qti.export.QTIExportKPRIMItemFormatConfig;
import org.olat.ims.qti.export.QTIExportMCQItemFormatConfig;
import org.olat.ims.qti.export.QTIExportManager;
import org.olat.ims.qti.export.QTIExportSCQItemFormatConfig;
import org.olat.ims.qti.export.helper.QTIItemObject;
import org.olat.ims.qti.export.helper.QTIObjectTreeBuilder;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.user.UserManager;

/**
 * 
 * Description:<br>
 * Wizard for replacement of an already linked test
 * 
 * <P>
 * Initial Date:  21.10.2008 <br>
 * @author skoeber
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class IQEditReplaceWizard extends WizardController {
	
	
	/** three steps: information + export results, search, mail */
	private static final int STEPS = 3;
	
	//needed objects
	private String resultExportFile;
	private File exportDir;
	private ICourse course;
	private CourseNode courseNode;
	private RepositoryEntry selectedRepositoryEntry;
	private List<Identity> learners;
	private List<QTIResult> results;
	private String[] types;

	// presentation
	private VelocityContainer vcStep1, vcStep2, vcStep3;
	private Link nextBtn, showFileButton;
	private MailNotificationEditController mailCtr;
	private ReferencableEntriesSearchController searchCtr;
	
	private MailManager mailManager;

	/**
	 * a number of identities with qti.ser entry
	 */
	private int numberOfQtiSerEntries;

	/**
	 * Standard constructor
	 * @param ureq
	 * @param wControl
	 * @param course
	 * @param courseNode
	 */
	public IQEditReplaceWizard(UserRequest ureq, WindowControl wControl, ICourse course, CourseNode courseNode,
			String[] types, List<Identity> learners, List<QTIResult> results, int numberOfQtiSerEntries) {
		super(ureq, wControl, STEPS);
		
		setBasePackage(IQEditReplaceWizard.class);
		mailManager = CoreSpringFactory.getImpl(MailManager.class);
		
		this.course = course;
		this.courseNode = courseNode;
		this.types = types;
		this.learners = learners;
		this.results = results;
		this.numberOfQtiSerEntries = numberOfQtiSerEntries;

		setWizardTitle(translate("replace.wizard.title"));
		doStep1(ureq);
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == nextBtn) {
			doStep3(ureq);
		} else if(source == showFileButton) {
			ureq.getDispatchResult().setResultingMediaResource(new DownloadeableMediaResource(new File(exportDir, resultExportFile)));
		} else if(event.getCommand().equals("cmd.wizard.cancel")) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == mailCtr && event == Event.DONE_EVENT) {
			MailTemplate mailTemplate = mailCtr.getMailTemplate();
			if(mailTemplate != null) {
				MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
				String metaId = UUID.randomUUID().toString().replace("-", "");
				MailerResult result = new MailerResult();
				MailBundle[] bundles = mailManager.makeMailBundles(context, learners, mailTemplate, getIdentity(), metaId, result);
				result.append(mailManager.sendMessage(bundles));
				if(mailTemplate.getCpfrom()) {
					MailBundle ccBundle = mailManager.makeMailBundle(context, getIdentity(), mailTemplate, getIdentity(), metaId, result);
					result.append(mailManager.sendMessage(ccBundle));
				}
			}
			fireEvent(ureq, Event.DONE_EVENT);
		} else if (source == searchCtr && event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
			selectedRepositoryEntry = searchCtr.getSelectedEntry();
			doStep2(ureq);
		}
	}
	
	private void doStep1(UserRequest ureq) {
		searchCtr = new ReferencableEntriesSearchController(getWindowControl(), ureq, types, translate("command.chooseTest"));
		searchCtr.addControllerListener(this);
		vcStep1 = createVelocityContainer("replacewizard_step1");
		vcStep1.put("search", searchCtr.getInitialComponent());
		setNextWizardStep(translate("replace.wizard.title.step1"), vcStep1);
	}
	
	private void doStep2(UserRequest ureq) {
		String nodeTitle = courseNode.getShortTitle();
		if (results != null && !results.isEmpty()) {
			exportDir = CourseFactory.getOrCreateDataExportDirectory(ureq.getIdentity(), course.getCourseTitle());
			UserManager um = UserManager.getInstance();
			String charset = um.getUserCharset(ureq.getIdentity());
			QTIExportManager qem = QTIExportManager.getInstance();
			Long repositoryRef = results.get(0).getResultSet().getRepositoryRef();
			List<QTIItemObject> qtiItemObjectList = new QTIObjectTreeBuilder().getQTIItemObjectList(repositoryRef);
			
			QTIExportFormatter formatter;
			if (courseNode instanceof IQTESTCourseNode) {
				formatter = new QTIExportFormatterCSVType1(ureq.getLocale(), "\t", "\"", "\r\n", false);
			} else if (courseNode instanceof IQSELFCourseNode) {
				formatter = new QTIExportFormatterCSVType1(ureq.getLocale(),"\t", "\"", "\r\n", false);
				((QTIExportFormatterCSVType1)formatter).setAnonymous(true);
			} else {
				formatter = new QTIExportFormatterCSVType3(ureq.getLocale(), null, "\t", "\"", "\r\n", false);
			}
			
			Map<Class<?>, QTIExportItemFormatConfig> qtiItemConfigs = getQTIItemConfigs(qtiItemObjectList);
			formatter.setMapWithExportItemConfigs(qtiItemConfigs);
			resultExportFile = qem.exportResults(formatter, results, qtiItemObjectList, courseNode.getShortTitle(), exportDir, charset, ".xls");
			vcStep2 = createVelocityContainer("replacewizard_step2");
			String[] args1 = new String[] { Integer.toString(learners.size()) };
			vcStep2.contextPut("information", translate("replace.wizard.information.paragraph1", args1));
			String[] args2 = new String[] { exportDir.getName(), resultExportFile };
			vcStep2.contextPut("information_par2", translate("replace.wizard.information.paragraph2", args2));
			vcStep2.contextPut("nodetitle", nodeTitle);
			showFileButton = LinkFactory.createButton("replace.wizard.showfile", vcStep2, this);
		} else {
			// it exists no result
			String[] args = new String[] { Integer.toString(numberOfQtiSerEntries) };
			vcStep2 = createVelocityContainer("replacewizard_step2");
			vcStep2.contextPut("information", translate("replace.wizard.information.empty.results", args));
			vcStep2.contextPut("nodetitle", nodeTitle);
		}
		nextBtn = LinkFactory.createButton("replace.wizard.next", vcStep2, this);
		setNextWizardStep(translate("replace.wizard.title.step2"), vcStep2);
	}

	private void doStep3(UserRequest ureq) {
		StringBuilder extLink = new StringBuilder();
		extLink.append(Settings.getServerContextPathURI());
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
		extLink.append("/url/RepositoryEntry/").append(re.getKey());
		extLink.append("/CourseNode/").append(courseNode.getIdent());
		
		String[] bodyArgs = new String[] {
				course.getCourseTitle(),
				extLink.toString()
				};
		
		String subject = translate("inform.users.subject", bodyArgs);
		String body = translate("inform.users.body", bodyArgs);
		
		MailTemplate mailTempl = new MailTemplate(subject, body, null) {
			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity identity) {
				// nothing to do
			}
		};
		
		removeAsListenerAndDispose(mailCtr);
		mailCtr = new MailNotificationEditController(getWindowControl(), ureq, mailTempl, false, false, true);
		listenTo(mailCtr);
	
		vcStep3 = createVelocityContainer("replacewizard_step3");
		vcStep3.put("mailform", mailCtr.getInitialComponent());
		setNextWizardStep(translate("replace.wizard.title.step3"), vcStep3);
	}
	
	/**
	 * @return the selected RepositoryEntry
	 */
	protected RepositoryEntry getSelectedRepositoryEntry() {
		return selectedRepositoryEntry;
	}
	
	private Map<Class<?>, QTIExportItemFormatConfig> getQTIItemConfigs(List<QTIItemObject> qtiItemObjectList){
		Map<Class<?>, QTIExportItemFormatConfig> itConfigs = new HashMap<>();
  	
		for (Iterator<QTIItemObject> iter = qtiItemObjectList.iterator(); iter.hasNext();) {
			QTIItemObject item = iter.next();
			if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_SCQ)){
				if (itConfigs.get(QTIExportSCQItemFormatConfig.class) == null){
					QTIExportSCQItemFormatConfig confSCQ = new QTIExportSCQItemFormatConfig(true, false, false, false);
					itConfigs.put(QTIExportSCQItemFormatConfig.class, confSCQ);
				}
			} else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_MCQ)){
				if (itConfigs.get(QTIExportMCQItemFormatConfig.class) == null){
					QTIExportMCQItemFormatConfig confMCQ = new QTIExportMCQItemFormatConfig(true, false, false, false);
					itConfigs.put(QTIExportMCQItemFormatConfig.class, confMCQ );
				}
			} else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_KPRIM)){
				if (itConfigs.get(QTIExportKPRIMItemFormatConfig.class) == null){
					QTIExportKPRIMItemFormatConfig confKPRIM = new QTIExportKPRIMItemFormatConfig(true, false, false, false);
					itConfigs.put(QTIExportKPRIMItemFormatConfig.class, confKPRIM);
				}
			} else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_ESSAY)){
				if (itConfigs.get(QTIExportEssayItemFormatConfig.class) == null){
					QTIExportEssayItemFormatConfig confEssay = new QTIExportEssayItemFormatConfig(true, false);
					itConfigs.put(QTIExportEssayItemFormatConfig.class, confEssay);
				}
			} else if (item.getItemIdent().startsWith(ItemParser.ITEM_PREFIX_FIB)){
				if (itConfigs.get(QTIExportFIBItemFormatConfig.class) == null){
					QTIExportFIBItemFormatConfig confFIB = new QTIExportFIBItemFormatConfig(true, false, false);
					itConfigs.put(QTIExportFIBItemFormatConfig.class, confFIB);
				}
			} else if (item.getItemType().equals(QTIItemObject.TYPE.A)){
				QTIExportEssayItemFormatConfig confEssay = new QTIExportEssayItemFormatConfig(true, false);
				itConfigs.put(QTIExportEssayItemFormatConfig.class, confEssay);
			} else if (item.getItemType().equals(QTIItemObject.TYPE.R)){
				QTIExportSCQItemFormatConfig confSCQ = new QTIExportSCQItemFormatConfig(true, false, false, false);
				itConfigs.put(QTIExportSCQItemFormatConfig.class, confSCQ);
			} else if (item.getItemType().equals(QTIItemObject.TYPE.C)){
				QTIExportMCQItemFormatConfig confMCQ = new QTIExportMCQItemFormatConfig(true, false, false, false);
				itConfigs.put(QTIExportMCQItemFormatConfig.class, confMCQ );
			} else if (item.getItemType().equals(QTIItemObject.TYPE.B)){
				QTIExportFIBItemFormatConfig confFIB = new QTIExportFIBItemFormatConfig(true, false, false);
				itConfigs.put(QTIExportFIBItemFormatConfig.class, confFIB);
			} else{
				throw new OLATRuntimeException(null,"Can not resolve QTIItem type", null);
			}
		}
		return itConfigs;
	}
}