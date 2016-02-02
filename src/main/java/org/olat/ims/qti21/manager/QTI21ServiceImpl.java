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
package org.olat.ims.qti21.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.components.form.flexible.impl.MultipartFileInfos;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21ContentPackage;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Module;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.CandidateItemEventType;
import org.olat.ims.qti21.model.CandidateTestEventType;
import org.olat.ims.qti21.model.ResponseLegality;
import org.olat.ims.qti21.model.jpa.CandidateEvent;
import org.olat.ims.qti21.ui.rendering.XmlUtilities;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import com.thoughtworks.xstream.XStream;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.JqtiExtensionPackage;
import uk.ac.ed.ph.jqtiplus.QtiConstants;
import uk.ac.ed.ph.jqtiplus.node.AssessmentObject;
import uk.ac.ed.ph.jqtiplus.node.AssessmentObjectType;
import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.result.AbstractResult;
import uk.ac.ed.ph.jqtiplus.node.result.AssessmentResult;
import uk.ac.ed.ph.jqtiplus.node.result.ItemResult;
import uk.ac.ed.ph.jqtiplus.node.result.ItemVariable;
import uk.ac.ed.ph.jqtiplus.node.result.OutcomeVariable;
import uk.ac.ed.ph.jqtiplus.notification.NotificationRecorder;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentObject;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.state.marshalling.ItemSessionStateXmlMarshaller;
import uk.ac.ed.ph.jqtiplus.state.marshalling.TestSessionStateXmlMarshaller;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.ResponseData.ResponseDataType;
import uk.ac.ed.ph.jqtiplus.value.BooleanValue;
import uk.ac.ed.ph.jqtiplus.value.NumberValue;
import uk.ac.ed.ph.jqtiplus.value.RecordValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;
import uk.ac.ed.ph.jqtiplus.value.Value;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ClassPathResourceLocator;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;
import uk.ac.ed.ph.jqtiplus.xmlutils.xslt.XsltSerializationOptions;
import uk.ac.ed.ph.jqtiplus.xmlutils.xslt.XsltStylesheetCache;
import uk.ac.ed.ph.jqtiplus.xmlutils.xslt.XsltStylesheetManager;
import uk.ac.ed.ph.qtiworks.mathassess.GlueValueBinder;
import uk.ac.ed.ph.qtiworks.mathassess.MathAssessConstants;
import uk.ac.ed.ph.qtiworks.mathassess.MathAssessExtensionPackage;

/**
 * 
 * Initial date: 12.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QTI21ServiceImpl implements QTI21Service, InitializingBean, DisposableBean {
	
	private static final OLog log = Tracing.createLoggerFor(QTI21ServiceImpl.class);
	
	private static XStream configXstream = XStreamHelper.createXStreamInstance();
	static {
		configXstream.alias("deliveryOptions", QTI21DeliveryOptions.class);
	}
	
	@Autowired
	private EventDAO eventDao;
	@Autowired
	private AssessmentTestSessionDAO testSessionDao;
	@Autowired
	private AssessmentItemSessionDAO itemSessionDao;
	@Autowired
	private AssessmentResponseDAO testResponseDao;
	@Autowired
	private QTI21Storage storage;
	@Autowired
	private QTI21Module qtiModule;
	@Autowired
	private CoordinatorManager coordinatorManager;

	private JqtiExtensionManager jqtiExtensionManager;
	private XsltStylesheetManager xsltStylesheetManager;
	private InfinispanXsltStylesheetCache xsltStylesheetCache;
	private CacheWrapper<File,ResolvedAssessmentObject<?>> assessmentTestsAndItemsCache;
	
	@Autowired
	public QTI21ServiceImpl(InfinispanXsltStylesheetCache xsltStylesheetCache) {
		this.xsltStylesheetCache = xsltStylesheetCache;
	}

    @Override
	public void afterPropertiesSet() throws Exception {
    	final List<JqtiExtensionPackage<?>> extensionPackages = new ArrayList<JqtiExtensionPackage<?>>();

        /* Enable MathAssess extensions if requested */
        if (qtiModule.isMathAssessExtensionEnabled()) {
            log.info("Enabling the MathAssess extensions");
            extensionPackages.add(new MathAssessExtensionPackage(xsltStylesheetCache));
        }
        jqtiExtensionManager = new JqtiExtensionManager(extensionPackages);
        xsltStylesheetManager = new XsltStylesheetManager(new ClassPathResourceLocator(), xsltStylesheetCache);
        
        jqtiExtensionManager.init();

        assessmentTestsAndItemsCache = coordinatorManager.getInstance().getCoordinator().getCacher().getCache("QTIWorks", "assessmentTestsAndItems");
	}

    @Override
	public void destroy() throws Exception {
		if(jqtiExtensionManager != null) {
			jqtiExtensionManager.destroy();
		}
	}

	@Override
	public XsltStylesheetCache getXsltStylesheetCache() {
		return xsltStylesheetCache;
	}

	@Override
    public XsltStylesheetManager getXsltStylesheetManager() {
    	return xsltStylesheetManager;
    }

    @Override
    public JqtiExtensionManager jqtiExtensionManager() {
        return jqtiExtensionManager;
    }
    
    @Override
    public QtiSerializer qtiSerializer() {
        return new QtiSerializer(jqtiExtensionManager());
    }
    
    @Override
    public QtiXmlReader qtiXmlReader() {
    	return new QtiXmlReader(jqtiExtensionManager());
    }
	
	@Override
	public QTI21DeliveryOptions getDeliveryOptions(RepositoryEntry testEntry) {
		FileResourceManager frm = FileResourceManager.getInstance();
		File reFolder = frm.getFileResourceRoot(testEntry.getOlatResource());
		File configXml = new File(reFolder, PACKAGE_CONFIG_FILE_NAME);
		
		QTI21DeliveryOptions config;
		if(configXml.exists()) {
			config = (QTI21DeliveryOptions)configXstream.fromXML(configXml);
		} else {
			//set default config
			config = new QTI21DeliveryOptions();
			setDeliveryOptions(testEntry, config);
		}
		return config;
	}

	@Override
	public void setDeliveryOptions(RepositoryEntry testEntry, QTI21DeliveryOptions options) {
		FileResourceManager frm = FileResourceManager.getInstance();
		File reFolder = frm.getFileResourceRoot(testEntry.getOlatResource());
		File configXml = new File(reFolder, PACKAGE_CONFIG_FILE_NAME);
		if(options == null) {
			if(configXml.exists()) {
				configXml.delete();
			}
		} else {
			try (OutputStream out = new FileOutputStream(configXml)) {
				configXstream.toXML(options, out);
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResolvedAssessmentObject<?> loadAndResolveAssessmentObject(File resourceDirectory) {
		ResolvedAssessmentObject<?> result;
		if(assessmentTestsAndItemsCache.containsKey(resourceDirectory)) {
			return assessmentTestsAndItemsCache.get(resourceDirectory);
		} else {
			QtiXmlReader qtiXmlReader = new QtiXmlReader(jqtiExtensionManager());
			ResourceLocator fileResourceLocator = new PathResourceLocator(resourceDirectory.toPath());
			final ResourceLocator inputResourceLocator = 
	        		ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
	        final URI assessmentObjectSystemId = createAssessmentObjectUri(resourceDirectory);
	        final AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, inputResourceLocator);
	        final AssessmentObjectType assessmentObjectType = AssessmentObjectType.ASSESSMENT_TEST;
	        
	        if (assessmentObjectType==AssessmentObjectType.ASSESSMENT_ITEM) {
	            result = assessmentObjectXmlLoader.loadAndResolveAssessmentItem(assessmentObjectSystemId);
	        } else if (assessmentObjectType==AssessmentObjectType.ASSESSMENT_TEST) {
	            result = assessmentObjectXmlLoader.loadAndResolveAssessmentTest(assessmentObjectSystemId);
	        } else {
	            throw new OLATRuntimeException("Unexpected branch " + assessmentObjectType, null);
	        }
	        
	        File resourceFile = new File(assessmentObjectSystemId);
	        ResolvedAssessmentObject<?> cachedResult = assessmentTestsAndItemsCache.putIfAbsent(resourceFile, result);
	        if(cachedResult != null) {
	        	result = cachedResult;
	        }
		}
        return result;
	}
	
	@Override
	public boolean updateAssesmentObject(File resourceFile, ResolvedAssessmentObject<?> resolvedAssessmentObject) {
		AssessmentObject assessmentObject;
		if(resolvedAssessmentObject instanceof ResolvedAssessmentItem) {
			assessmentObject = ((ResolvedAssessmentItem)resolvedAssessmentObject)
					.getItemLookup().getRootNodeHolder().getRootNode();
		} else if(resolvedAssessmentObject instanceof ResolvedAssessmentTest) {
			assessmentObject = ((ResolvedAssessmentTest)resolvedAssessmentObject)
					.getTestLookup().getRootNodeHolder().getRootNode();
		} else {
			return false;
		}

		return persistAssessmentObject(resourceFile, assessmentObject);
	}

	@Override
	public boolean persistAssessmentObject(File resourceFile, AssessmentObject assessmentObject) {
		try(FileOutputStream out = new FileOutputStream(resourceFile)) {
			qtiSerializer().serializeJqtiObject(assessmentObject, out);
			//TODO qti
			assessmentTestsAndItemsCache.remove(resourceFile);
			return true;
		} catch(Exception e) {
			log.error("", e);
			return false;
		}
	}

	@Override
	public URI createAssessmentObjectUri(File resourceDirectory) {
		File manifestPath = new File(resourceDirectory, "imsmanifest.xml");
		QTI21ContentPackage	cp = new QTI21ContentPackage(manifestPath.toPath());
		try {
			Path testPath = cp.getTest();
			return testPath.toUri();
		} catch (IOException e) {
			log.error("", e);
		}
		return null;
	}

	@Override
	public AssessmentTestSession createAssessmentTestSession(Identity identity, AssessmentEntry assessmentEntry,
			RepositoryEntry entry, String subIdent, RepositoryEntry testEntry, boolean authorMode) {
		return testSessionDao.createAndPersistTestSession(testEntry, entry, subIdent, assessmentEntry, identity, authorMode);
	}

	@Override
	public AssessmentTestSession getResumableAssessmentTestSession(Identity identity, RepositoryEntry entry, String subIdent, RepositoryEntry testEntry) {
		AssessmentTestSession session = testSessionDao.getLastTestSession(testEntry, entry, subIdent, identity);
		if(session == null || session.isExploded() || session.getTerminationTime() != null) {
			session = null;
		} else {
			File sessionFile = getTestSessionStateFile(session);
			if(sessionFile == null || !sessionFile.exists()) {
				session = null;
			}
		}
		return session;
	}
	
	@Override
	public AssessmentTestSession updateAssessmentTestSession(AssessmentTestSession session) {
		return testSessionDao.update(session);
	}

	@Override
	public List<AssessmentTestSession> getAssessmentTestSessions(RepositoryEntryRef courseEntry, String courseSubIdent, IdentityRef identity) {
		return testSessionDao.getUserTestSessions(courseEntry, courseSubIdent, identity);
	}

	@Override
	public TestSessionState loadTestSessionState(AssessmentTestSession candidateSession) {
        Document document = loadStateDocument(candidateSession);
        return document == null ? null: TestSessionStateXmlMarshaller.unmarshal(document.getDocumentElement());
    }
	
    private Document loadStateDocument(AssessmentTestSession candidateSession) {
        File sessionFile = getTestSessionStateFile(candidateSession);
        if(sessionFile.exists()) {
	        DocumentBuilder documentBuilder = XmlUtilities.createNsAwareDocumentBuilder();
	        try {
	            return documentBuilder.parse(sessionFile);
	        } catch (final Exception e) {
	            throw new OLATRuntimeException("Could not parse serailized state XML. This is an internal error as we currently don't expose this data to clients", e);
	        }
        }
        return null;
    }

	@Override
	public AssessmentItemSession getOrCreateAssessmentItemSession(AssessmentTestSession assessmentTestSession, String assessmentItemIdentifier) {
		AssessmentItemSession itemSession = itemSessionDao.getAssessmentItemSession(assessmentTestSession, assessmentItemIdentifier);
		if(itemSession == null) {
			itemSession = itemSessionDao.createAndPersistAssessmentItemSession(assessmentTestSession, assessmentItemIdentifier);
		}
		return itemSession;
	}

	@Override
	public AssessmentResponse createAssessmentResponse(AssessmentTestSession assessmentTestSession, AssessmentItemSession assessmentItemSession, String responseIdentifier,
			ResponseLegality legality, ResponseDataType type) {
		return testResponseDao.createAssessmentResponse(assessmentTestSession, assessmentItemSession,
				responseIdentifier, legality, type);
	}
	
	@Override
	public Map<Identifier, AssessmentResponse> getAssessmentResponses(AssessmentItemSession assessmentItemSession) {
		List<AssessmentResponse> responses = testResponseDao.getResponses(assessmentItemSession);
		Map<Identifier, AssessmentResponse> responseMap = new HashMap<>();
		for(AssessmentResponse response:responses) {
			responseMap.put(Identifier.assumedLegal(response.getResponseIdentifier()), response);
		}
		return responseMap;
	}

	@Override
	public void recordTestAssessmentResponses(Collection<AssessmentResponse> responses) {
		testResponseDao.save(responses);
	}

	@Override
	public AssessmentTestSession recordTestAssessmentResult(AssessmentTestSession candidateSession, TestSessionState testSessionState, AssessmentResult assessmentResult) {
		// First record full result XML to filesystem
        storeAssessmentResultFile(candidateSession, assessmentResult);
        // Then record test outcome variables to DB
        recordOutcomeVariables(candidateSession, assessmentResult.getTestResult());
        // Set duration
        candidateSession.setDuration(testSessionState.getDurationAccumulated());
        return testSessionDao.update(candidateSession);
	}

	@Override
	public AssessmentTestSession finishTestSession(AssessmentTestSession candidateSession, TestSessionState testSessionState, AssessmentResult assessmentResul, Date timestamp) {
		/* Mark session as finished */
        candidateSession.setFinishTime(timestamp);
        // Set duration
        candidateSession.setDuration(testSessionState.getDurationAccumulated());

        /* Also nullify LIS result info for session. These will be updated later, if pre-conditions match for sending the result back */
        //candidateSession.setLisOutcomeReportingStatus(null);
        //candidateSession.setLisScore(null);
        candidateSession = testSessionDao.update(candidateSession);

        /* Finally schedule LTI result return (if appropriate and sane) */
        //maybeScheduleLtiOutcomes(candidateSession, assessmentResult);
        return candidateSession;
	}
	
    private void recordOutcomeVariables(AssessmentTestSession candidateSession, AbstractResult resultNode) {
        for (final ItemVariable itemVariable : resultNode.getItemVariables()) {
            if (itemVariable instanceof OutcomeVariable) {
                
                OutcomeVariable outcomeVariable = (OutcomeVariable)itemVariable;
            	Identifier identifier = outcomeVariable.getIdentifier();
            	if(QtiConstants.VARIABLE_DURATION_IDENTIFIER.equals(identifier)) {
            		log.audit(candidateSession.getKey() + " :: " + itemVariable.getIdentifier() + " - " + stringifyQtiValue(itemVariable.getComputedValue()));
            	} else  if(QTI21Constants.SCORE_IDENTIFIER.equals(identifier)) {
            		Value value = itemVariable.getComputedValue();
            		if(value instanceof NumberValue) {
            			double score = ((NumberValue)value).doubleValue();
            			candidateSession.setScore(new BigDecimal(Double.toString(score)));
            		}
            	} else if(QTI21Constants.PASS_IDENTIFIER.equals(identifier)) {
            		Value value = itemVariable.getComputedValue();
            		if(value instanceof BooleanValue) {
            			boolean pass = ((BooleanValue)value).booleanValue();
            			candidateSession.setPassed(pass);
            		}
            	}
            }
        }
    }
    
    private String stringifyQtiValue(final Value value) {
        if (qtiModule.isMathAssessExtensionEnabled() && GlueValueBinder.isMathsContentRecord(value)) {
            /* This is a special MathAssess "Maths Content" variable. In this case, we'll record
             * just the ASCIIMath input form or the Maxima form, if either are available.
             */
            final RecordValue mathsValue = (RecordValue) value;
            final SingleValue asciiMathInput = mathsValue.get(MathAssessConstants.FIELD_CANDIDATE_INPUT_IDENTIFIER);
            if (asciiMathInput!=null) {
                return "ASCIIMath[" + asciiMathInput.toQtiString() + "]";
            }
            final SingleValue maximaForm = mathsValue.get(MathAssessConstants.FIELD_MAXIMA_IDENTIFIER);
            if (maximaForm!=null) {
                return "Maxima[" + maximaForm.toQtiString() + "]";
            }
        }
        /* Just convert to QTI string in the usual way */
        return value.toQtiString();
    }
    
    private void storeAssessmentResultFile(final AssessmentTestSession candidateSession, final QtiNode resultNode) {
        final File resultFile = getAssessmentResultFile(candidateSession);
        try(OutputStream resultStream = FileUtils.getBos(resultFile);) {
            qtiSerializer().serializeJqtiObject(resultNode, resultStream);
        } catch (final Exception e) {
            throw new OLATRuntimeException("Unexpected", e);
        }
    }
    
    private File getAssessmentResultFile(final AssessmentTestSession candidateSession) {
    	File myStore = storage.getDirectory(candidateSession.getStorage());
        return new File(myStore, "assessmentResult.xml");
    }

	@Override
	public CandidateEvent recordCandidateTestEvent(AssessmentTestSession candidateSession, CandidateTestEventType textEventType,
			TestSessionState testSessionState, NotificationRecorder notificationRecorder) {
		return recordCandidateTestEvent(candidateSession, textEventType, null, null, testSessionState, notificationRecorder);
	}

	@Override
	public CandidateEvent recordCandidateTestEvent(AssessmentTestSession candidateSession, CandidateTestEventType textEventType,
			CandidateItemEventType itemEventType, TestSessionState testSessionState, NotificationRecorder notificationRecorder) {
		CandidateEvent event = new CandidateEvent();
		event.setCandidateSession(candidateSession);
		event.setTestEventType(textEventType);
		return recordCandidateTestEvent(candidateSession, textEventType, itemEventType, null, testSessionState, notificationRecorder);
	}

	@Override
	public CandidateEvent recordCandidateTestEvent(AssessmentTestSession candidateSession, CandidateTestEventType textEventType,
			CandidateItemEventType itemEventType, TestPlanNodeKey itemKey, TestSessionState testSessionState, NotificationRecorder notificationRecorder) {
		CandidateEvent event = eventDao.create(candidateSession, textEventType, itemEventType, itemKey);
		storeTestSessionState(event, testSessionState);
		return event;
	}
	
	private void storeTestSessionState(CandidateEvent candidateEvent, TestSessionState testSessionState) {
		Document stateDocument = TestSessionStateXmlMarshaller.marshal(testSessionState);
		File sessionFile = getTestSessionStateFile(candidateEvent);
		storeStateDocument(stateDocument, sessionFile);
	}

    private File getTestSessionStateFile(CandidateEvent candidateEvent) {
    	AssessmentTestSession candidateSession = candidateEvent.getCandidateSession();
    	return getTestSessionStateFile(candidateSession);
    }
    
    private File getTestSessionStateFile(AssessmentTestSession candidateSession) {
    	File myStore = storage.getDirectory(candidateSession.getStorage());
        return new File(myStore, "testSessionState.xml");
    }
	
    @Override
	public CandidateEvent recordCandidateItemEvent(AssessmentTestSession candidateSession, CandidateItemEventType itemEventType,
			ItemSessionState itemSessionState) {
		return recordCandidateItemEvent(candidateSession, itemEventType, itemSessionState, null);
	}
		
	@Override
    public CandidateEvent recordCandidateItemEvent(AssessmentTestSession candidateSession, CandidateItemEventType itemEventType,
    		ItemSessionState itemSessionState, NotificationRecorder notificationRecorder) {
    	return eventDao.create(candidateSession, itemEventType);
    }
	
    public void storeItemSessionState(CandidateEvent candidateEvent, ItemSessionState itemSessionState) {
        Document stateDocument = ItemSessionStateXmlMarshaller.marshal(itemSessionState);
        File sessionFile = getItemSessionStateFile(candidateEvent);
        storeStateDocument(stateDocument, sessionFile);
    }
    
    private File getItemSessionStateFile(CandidateEvent candidateEvent) {
    	AssessmentTestSession candidateSession = candidateEvent.getCandidateSession();
    	File myStore = storage.getDirectory(candidateSession.getStorage());
        return new File(myStore, "itemSessionState.xml");
    }
    
	private void storeStateDocument(Document stateXml, File sessionFile) {
        XsltSerializationOptions xsltSerializationOptions = new XsltSerializationOptions();
        xsltSerializationOptions.setIndenting(true);
        xsltSerializationOptions.setIncludingXMLDeclaration(false);
        
        Transformer serializer = XsltStylesheetManager.createSerializer(xsltSerializationOptions);
        try(OutputStream resultStream = new FileOutputStream(sessionFile)) {
            serializer.transform(new DOMSource(stateXml), new StreamResult(resultStream));
        } catch (TransformerException | IOException e) {
            throw new OLATRuntimeException("Unexpected Exception serializing state DOM", e);
        }
    }

	@Override
	public AssessmentTestSession finishItemSession(AssessmentTestSession candidateSession, AssessmentResult assessmentResult, Date timestamp) {
		/* Mark session as finished */
        candidateSession.setFinishTime(timestamp);

        /* Also nullify LIS result info for session. These will be updated later, if pre-conditions match for sending the result back */
        //candidateSession.setLisOutcomeReportingStatus(null);
        //candidateSession.setLisScore(null);
        candidateSession = testSessionDao.update(candidateSession);

        /* Finally schedule LTI result return (if appropriate and sane) */
        //maybeScheduleLtiOutcomes(candidateSession, assessmentResult);
		return candidateSession;
	}

	@Override
	public void recordItemAssessmentResult(AssessmentTestSession candidateSession, AssessmentResult assessmentResult) {
		//do nothing for the mmoment
		List<ItemResult> itemResults = assessmentResult.getItemResults();
		for(ItemResult itemResult:itemResults) {
			for (final ItemVariable itemVariable : itemResult.getItemVariables()) {
	            if (itemVariable instanceof OutcomeVariable) {
	                
	                OutcomeVariable outcomeVariable = (OutcomeVariable)itemVariable;
	            	Identifier identifier = outcomeVariable.getIdentifier();
	            	if(QtiConstants.VARIABLE_DURATION_IDENTIFIER.equals(identifier)) {
	            		log.audit(candidateSession.getKey() + " :: " + itemVariable.getIdentifier() + " - " + stringifyQtiValue(itemVariable.getComputedValue()));
	            	} else  if(QTI21Constants.SCORE_IDENTIFIER.equals(identifier)) {
	            		Value value = itemVariable.getComputedValue();
	            		if(value instanceof NumberValue) {
	            			double score = ((NumberValue)value).doubleValue();
	            			candidateSession.setScore(new BigDecimal(Double.toString(score)));
	            			System.out.println("Score: " + score);
	            		}
	            	}
	            }
	        }
		}
	}
	

	@Override
	public String importFileSubmission(AssessmentTestSession candidateSession, MultipartFileInfos multipartFile) {
		File myStore = storage.getDirectory(candidateSession.getStorage());
        File submissionDir = new File(myStore, "submissions");
        if(!submissionDir.exists()) {
        	submissionDir.mkdir();
        }
        
        try {
			File submittedFile = new File(submissionDir, multipartFile.getFileName());
			Files.move(multipartFile.getFile().toPath(), submittedFile.toPath());
		} catch (IOException e) {
			log.error("", e);
		}

		return myStore.getAbsolutePath();
	}
}
