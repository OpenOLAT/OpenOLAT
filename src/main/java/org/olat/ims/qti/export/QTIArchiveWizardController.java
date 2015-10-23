/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.ims.qti.export;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.wizard.WizardController;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.LockResult;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.assessment.NodeTableDataModel;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.ims.qti.QTIResult;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.QTIResultSet;
import org.olat.ims.qti.editor.beecom.parser.ItemParser;
import org.olat.ims.qti.export.helper.QTIItemObject;
import org.olat.ims.qti.export.helper.QTIObjectTreeBuilder;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.user.UserManager;

import de.bps.onyx.plugin.OnyxExportManager;

/**
 * Initial Date: June 06, 2006 <br>
 * 
 * @author Alexander Schneider
 */
public class QTIArchiveWizardController extends BasicController {
	private static final String CMD_SELECT_NODE = "cmd.select.node";
	
	private boolean dummyMode;
	
	// Delimiters and file name suffix for the export file
	private String sep = "\\t"; // fields separated by
	private String emb = "\""; // fields embedded by
	private String esc = "\\"; // fields escaped by
	private String car = "\\r\\n"; // carriage return
	private String suf = ".csv"; // file name suffix
	
	private WizardController wc;
	
	private int steps = 4;
	private Panel main;
	
	private CourseNode currentCourseNode;
	
	private VelocityContainer nodeChooseVC;
	private VelocityContainer noResultsVC;
	private VelocityContainer optionsChooseVC;
	private VelocityContainer delimChooseVC;
	private VelocityContainer finishedVC;
	
	private OptionsChooseForm ocForm;
	private DelimChooseForm dcForm;
	private QTIExportFormatter formatter;
	
	private NodeTableDataModel nodeTableModel;
	
	private TableController nodeListCtr;
	private Long olatResource;
	private File exportDir;
	private String targetFileName;
	private int type;
	private Map<Class<?>, QTIExportItemFormatConfig> qtiItemConfigs;
	private List<QTIResult> results;
	private List<QTIItemObject> qtiItemObjectList;
	private Link showFileButton;
	private Link backLinkAtOptionChoose;
	private Link backLinkAtNoResults;
	private Link backLinkAtDelimChoose;
	private OLATResourceable ores;


	public QTIArchiveWizardController(boolean dummyMode, UserRequest ureq, List<AssessmentNodeData> assessmentNodeData, OLATResourceable ores, WindowControl wControl) {
		super(ureq, wControl);
		this.dummyMode = dummyMode;
		this.ores = ores;
		if(dummyMode)this.steps = 2;
		
		main = new Panel("main");
		nodeChooseVC = createVelocityContainer("nodechoose");
		
    //table configuraton
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(getTranslator().translate("nodesoverview.nonodes"));
		tableConfig.setDownloadOffered(false);
		tableConfig.setSortingEnabled(false);
		tableConfig.setDisplayTableHeader(true);
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		
		nodeListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(nodeListCtr);
		
		// table columns		
		nodeListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.node", 0, 
				null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, new IndentedNodeRenderer()));
		nodeListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.action.select", 1,
				CMD_SELECT_NODE, ureq.getLocale()));
		
		nodeTableModel = new NodeTableDataModel(assessmentNodeData, getTranslator());
		nodeListCtr.setTableDataModel(nodeTableModel);
		nodeChooseVC.put("nodeTable", nodeListCtr.getInitialComponent());
		
		wc = new WizardController(ureq, wControl, steps);
		listenTo(wc);
		
		wc.setWizardTitle(getTranslator().translate("wizard.nodechoose.title"));
		wc.setNextWizardStep(getTranslator().translate("wizard.nodechoose.howto"), nodeChooseVC);
		main.setContent(wc.getInitialComponent());
		putInitialPanel(main);
	}
	
	public String getAndRemoveWizardTitle() {
		return wc.getAndRemoveWizardTitle();
	}

	public void event(UserRequest ureq, Component source, Event event) {
		if (source == backLinkAtOptionChoose){
			wc.setWizardTitle(getTranslator().translate("wizard.nodechoose.title"));
			wc.setBackWizardStep(getTranslator().translate("wizard.nodechoose.howto"), nodeChooseVC);
		}
		else if (source == backLinkAtNoResults){
			wc.setWizardTitle(getTranslator().translate("wizard.nodechoose.title"));
			wc.setBackWizardStep(getTranslator().translate("wizard.nodechoose.howto"), nodeChooseVC);
		}
		else if (source == backLinkAtDelimChoose){
			wc.setWizardTitle(getTranslator().translate("wizard.optionschoose.title"));
			wc.setBackWizardStep(getTranslator().translate("wizard.optionschoose.howto"), optionsChooseVC);
		} else if (source == showFileButton ){
			MediaResource resource = new FileMediaResource(new File(exportDir, targetFileName), true);
			ureq.getDispatchResult().setResultingMediaResource(resource);
		}
	}

	/**
	 * This dispatches controller events...
	 * 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == ocForm){ 
			if (event == Event.DONE_EVENT) {
				delimChooseVC = createVelocityContainer("delimchoose");
				
				backLinkAtDelimChoose = LinkFactory.createLinkBack(delimChooseVC, this);
				delimChooseVC.contextPut("nodetitle", currentCourseNode.getShortTitle());
				removeAsListenerAndDispose(dcForm);
				dcForm = new DelimChooseForm(ureq, getWindowControl(), sep, emb, esc, car, suf);
				listenTo(dcForm);
				delimChooseVC.put("dcForm", dcForm.getInitialComponent());
				wc.setWizardTitle(getTranslator().translate("wizard.delimchoose.title"));
				wc.setNextWizardStep(getTranslator().translate("wizard.delimchoose.howto"), delimChooseVC);
			}
		}
		else if (source == nodeListCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals(CMD_SELECT_NODE)) {
					int rowid = te.getRowId();
					AssessmentNodeData nodeData = nodeTableModel.getObject(rowid);
					ICourse course = CourseFactory.loadCourse(ores);
					this.currentCourseNode = course.getRunStructure().getNode(nodeData.getIdent());

					boolean isOnyx = false;
					if (currentCourseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE_QTI) != null) {
						isOnyx = currentCourseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE_QTI).equals(IQEditController.CONFIG_VALUE_QTI2);
					}
					olatResource = course.getResourceableId();
					boolean success = false;
					String repositorySoftKey = (String) currentCourseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
				    Long repKey = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
				    if (isOnyx) {
				    	if (currentCourseNode.getClass().equals(IQSURVCourseNode.class)) {
				    			File surveyDir = new File(course.getCourseEnvironment().getCourseBaseContainer().getBasefile() + File.separator + currentCourseNode.getIdent()
									+ File.separator);
				    			if (surveyDir != null && surveyDir.exists() && surveyDir.listFiles().length > 0) {
				    				success = true;
				    			}
						} else {
							// <OLATBPS-498>
							QTIResultManager qrm = QTIResultManager.getInstance();
							success = false;
							List<QTIResultSet> resultSets = qrm.getResultSets(course.getResourceableId(), currentCourseNode.getIdent(), repKey, null);
							File fUserdataRoot = new File(WebappHelper.getUserDataRoot());
							for (QTIResultSet rs : resultSets) {
								String resultXml = OnyxExportManager.getInstance().getResultXml(rs.getIdentity().getName(),
										currentCourseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE).toString(), currentCourseNode.getIdent(),
										rs.getAssessmentID());
								File xml = new File(fUserdataRoot, resultXml);
								if (xml != null && xml.exists()) {
									// there is at least one result file
									// existing
									success = true;
									break;
								}
							}
							// </OLATBPS-498>
						}
				    } else {
						success = hasResultSets();
				    }
				    
				    if (success) {
						if (isOnyx) {
							finishedVC = createVelocityContainer("finished");
							showFileButton = LinkFactory.createButton("showfile", finishedVC, this);
							finishedVC.contextPut("nodetitle", currentCourseNode.getShortTitle());
							targetFileName = exportOnyx(ureq, course);
							finishedVC.contextPut("filename", targetFileName);
							wc.setWizardTitle(getTranslator().translate("wizard.finished.title"));
							wc.setNextWizardStep(getTranslator().translate("wizard.finished.howto"), finishedVC);
						} else {
				    
							QTIResultManager qrm = QTIResultManager.getInstance();
							results = qrm.selectResults(olatResource, currentCourseNode.getIdent(), repKey, null, type);
							if(results.isEmpty()) {
								success = false;
							} else {
								qtiItemObjectList = new QTIObjectTreeBuilder().getQTIItemObjectList(repKey);
								qtiItemConfigs = getQTIItemConfigs(qtiItemObjectList);
							
								if(dummyMode) {
									finishedVC = createVelocityContainer("finished");
									showFileButton = LinkFactory.createButton("showfile", finishedVC, this);
									showFileButton.setTarget("_blank");
									finishedVC.contextPut("nodetitle", currentCourseNode.getShortTitle());
								
									this.sep = convert2CtrlChars(sep);
									this.car = convert2CtrlChars(car);
								
									formatter = getFormatter(ureq.getLocale(), sep, emb, car, true);
									formatter.setMapWithExportItemConfigs(qtiItemConfigs);
								
									exportDir = CourseFactory.getOrCreateDataExportDirectory(ureq.getIdentity(), course.getCourseTitle());
									UserManager um = UserManager.getInstance();
									String charset = um.getUserCharset(ureq.getIdentity());
						    
									QTIExportManager qem = QTIExportManager.getInstance();
	
									targetFileName = qem.exportResults(formatter, results, qtiItemObjectList, currentCourseNode.getShortTitle(),exportDir, charset, suf);
									finishedVC.contextPut("filename", targetFileName);
									wc.setWizardTitle(getTranslator().translate("wizard.finished.title"));
									wc.setNextWizardStep(getTranslator().translate("wizard.finished.howto"), finishedVC);
								
								} else { // expert mode
									optionsChooseVC = createVelocityContainer("optionschoose");
									backLinkAtOptionChoose = LinkFactory.createLinkBack(optionsChooseVC, this);
									optionsChooseVC.contextPut("nodetitle", currentCourseNode.getShortTitle());
									removeAsListenerAndDispose(ocForm);
									ocForm = new OptionsChooseForm(ureq, getWindowControl(), qtiItemConfigs);
									listenTo(ocForm);
									optionsChooseVC.put("ocForm", ocForm.getInitialComponent());
								
									wc.setWizardTitle(getTranslator().translate("wizard.optionschoose.title"));
									wc.setNextWizardStep(getTranslator().translate("wizard.optionschoose.howto"), optionsChooseVC);
								}
							}
						}
					} 
				    
				    if(!success) { // no success
						noResultsVC = createVelocityContainer("noresults");
						backLinkAtNoResults = LinkFactory.createLinkBack(noResultsVC, this);
						noResultsVC.contextPut("nodetitle", currentCourseNode.getShortTitle());
						if (dummyMode){
							wc.setWizardTitle(getTranslator().translate("wizard.optionschoose.title"));
							wc.setNextWizardStep(getTranslator().translate("wizard.optionschoose.howto"), noResultsVC);
						} else { // expert mode
							wc.setWizardTitle(getTranslator().translate("wizard.finished.title"));
							wc.setNextWizardStep(getTranslator().translate("wizard.finished.howto"), noResultsVC);
						}
					}
				}
			}
		} else if (source == wc) {
			if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, event);
			}
		} else if (source == dcForm) {
			if (event == Event.DONE_EVENT) {
				finishedVC = createVelocityContainer("finished");
				showFileButton = LinkFactory.createButton("showfile", finishedVC, this);
				finishedVC.contextPut("nodetitle", currentCourseNode.getShortTitle());
				
				this.sep = dcForm.getSeparatedBy();
				this.emb = dcForm.getEmbeddedBy();
				this.esc = dcForm.getEscapedBy();
				this.car = dcForm.getCarriageReturn();
				this.suf = dcForm.getFileNameSuffix();

				this.sep = convert2CtrlChars(sep);
				this.car = convert2CtrlChars(car);
				
				formatter = getFormatter(ureq.getLocale(), sep, emb, car, dcForm.isTagless());
				formatter.setMapWithExportItemConfigs(qtiItemConfigs);
				
				ICourse course = CourseFactory.loadCourse(ores);
				exportDir = CourseFactory.getOrCreateDataExportDirectory(ureq.getIdentity(), course.getCourseTitle());
				UserManager um = UserManager.getInstance();
				String charset = um.getUserCharset(ureq.getIdentity());
		    
				QTIExportManager qem = QTIExportManager.getInstance();

				targetFileName = qem.exportResults(formatter, results, qtiItemObjectList, currentCourseNode.getShortTitle(),exportDir, charset, suf);
				finishedVC.contextPut("filename", targetFileName);
				wc.setWizardTitle(getTranslator().translate("wizard.finished.title"));
				wc.setNextWizardStep(getTranslator().translate("wizard.finished.howto"), finishedVC);
			}
		}
	}

	private String exportOnyx(UserRequest ureq, ICourse course) {
		String filename = "";
		exportDir = CourseFactory.getOrCreateDataExportDirectory(ureq.getIdentity(), course.getCourseTitle());
		OnyxExportManager onyxExportManager = OnyxExportManager.getInstance();
		if (currentCourseNode.getClass().equals(IQSURVCourseNode.class)) {
			// it is an onyx survey
			String surveyPath = course.getCourseEnvironment().getCourseBaseContainer().getBasefile() + File.separator + currentCourseNode.getIdent() + File.separator;
			filename = onyxExportManager.exportResults(surveyPath, exportDir, currentCourseNode);
		} else {

//			OnyxReporterConnector connector;
			File csvFile = null;
//			try {
//				connector = new OnyxReporterConnector();
//				File csvtmpFile = connector.collectResultsAsCSV(getIdentity(), currentCourseNode);
//				csvFileName = generateExportFileName(currentCourseNode.getShortTitle());
//				csvFile = new File(exportDir, csvFileName);
//				boolean success = FileUtils.copyFileToFile(csvtmpFile, csvFile, false);
//				if (!success) {
//					showCSVFileButton.setEnabled(false);
//				}
//			} catch (OnyxReporterException e) {
//				showCSVFileButton.setEnabled(false);
//				getWindowControl().setError("reporter.unavailable");
//			}

			String repositorySoftKey = (String) currentCourseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
			Long repKey = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
			QTIResultManager qrm = QTIResultManager.getInstance();
			List<QTIResultSet> resultSets = qrm.getResultSets(course.getResourceableId(), currentCourseNode.getIdent(), repKey, null);
			// <OLATCE-654>
			//			filename = onyxExportManager.exportResults(resultSets, exportDir, currentCourseNode);
			String repoSoftkey = (String) currentCourseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
			if (repoSoftkey != null) {
				RepositoryManager rm = RepositoryManager.getInstance();
				RepositoryEntry re = rm.lookupRepositoryEntryBySoftkey(repoSoftkey, false);
				RepositoryHandler repoHandler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(re);
				OLATResource res = OLATResourceManager.getInstance().findResourceable(re.getOlatResource());
				if (res != null) {
					boolean isAlreadyLocked = repoHandler.isLocked(res);
					LockResult lockResult = null;
					try {
						lockResult = repoHandler.acquireLock(res, ureq.getIdentity());
						if (lockResult == null || (lockResult != null && lockResult.isSuccess() && !isAlreadyLocked)) {
							MediaResource mr = repoHandler.getAsMediaResource(res, false);
							if (mr != null) {
								filename = onyxExportManager.exportAssessmentResults(resultSets, exportDir, mr, currentCourseNode, false, csvFile);
							}
						} else if (lockResult != null && lockResult.isSuccess() && isAlreadyLocked) {
							lockResult = null; // invalid lock, it was already locked
						}
					} finally {
						if ((lockResult != null && lockResult.isSuccess() && !isAlreadyLocked)) {
							repoHandler.releaseLock(lockResult);
							lockResult = null;
						}
					}
				}
			}
			// </OLATCE-654>

		}
		return filename;
	}

	private boolean hasResultSets() {
		if (currentCourseNode instanceof IQTESTCourseNode) {
			type = 1;
		} else if (currentCourseNode instanceof IQSELFCourseNode) {
			type = 2;
		} else {
			type = 3;
		}

		QTIResultManager qrm = QTIResultManager.getInstance();

		String repositorySoftKey = (String) currentCourseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);

		Long repKey = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
		boolean hasSets = qrm.hasResultSets(olatResource, currentCourseNode.getIdent(), repKey);

		if (hasSets) {
			return true;
		} else {
			return false;
		}
	}

	public static String convert2CtrlChars(String source) {
		if (source == null) return null;
		StringBuilder sb = new StringBuilder(300);
		int len = source.length();
		char[] cs = source.toCharArray();
		for (int i = 0; i < len; i++) {
			char c = cs[i];
			switch (c) {
				case '\\':
					// check on \\ first
					if (i < len - 1 && cs[i + 1] == 't') { // we have t as next char
						sb.append("\t");i++;
					}else if (i < len - 1 && cs[i + 1] == 'r') { // we have r as next char
						sb.append("\r");i++;
					}else if (i < len - 1 && cs[i + 1] == 'n') { // we have n as next char
						sb.append("\n");i++;
					} else {
						sb.append("\\");
					}
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public static Map<Class<?>, QTIExportItemFormatConfig> getQTIItemConfigs(List<QTIItemObject> itemList){
		Map<Class<?>, QTIExportItemFormatConfig> itConfigs = new HashMap<>();
  	
		for (Iterator<QTIItemObject> iter = itemList.iterator(); iter.hasNext();) {
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
			}
			//if cannot find the type via the ItemParser, look for the QTIItemObject type
			else if (item.getItemType().equals(QTIItemObject.TYPE.A)){
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
			} else {
				throw new OLATRuntimeException(null,"Can not resolve QTIItem type", null);
			}
		}
		return itConfigs;
	}
	
	private QTIExportFormatter getFormatter(Locale locale, String se, String em, String ca, boolean tagless){
		QTIExportFormatter frmtr = null;
		if (type == 1){
			frmtr = new QTIExportFormatterCSVType1(locale, se, em, ca, tagless);
		} else if (type == 2){
			frmtr = new QTIExportFormatterCSVType2(locale, null, se, em, ca, tagless);
		} else { // type == 3
			frmtr = new QTIExportFormatterCSVType3(locale, null, se, em, ca, tagless);
		}
		return frmtr;
	}
	
	@Override
	protected void doDispose() {
		//
	}
}
