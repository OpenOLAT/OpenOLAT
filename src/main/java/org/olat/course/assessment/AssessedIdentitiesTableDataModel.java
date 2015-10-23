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

package org.olat.course.assessment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;

import org.olat.core.gui.components.table.BooleanColumnDescriptor;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.model.CertificateLightPack;
import org.olat.course.certificate.ui.DownloadCertificateCellRenderer;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.ta.StatusForm;
import org.olat.course.nodes.ta.StatusManager;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.properties.Property;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial Date:  Jun 23, 2004
 * @author gnaegi
 */
public class AssessedIdentitiesTableDataModel extends DefaultTableDataModel<AssessedIdentityWrapper> {

	private int colCount;
	private AssessableCourseNode courseNode;
	
	private static final String COL_NAME = "name";
	private static final String COL_DETAILS = "details";
	private static final String COL_ATTEMPTS = "attempts";
	private static final String COL_SCORE = "score";
	//fxdiff VCRP-4: assessment overview with max score
	private static final String COL_MINSCORE = "minScore";
	private static final String COL_MAXSCORE = "maxScore";
	private static final String COL_PASSED = "passed";
	private static final String COL_STATUS = "status";
	private static final String COL_INITIAL_LAUNCH = "initialLaunch";
	private static final String COL_LAST_SCORE_DATE = "lastScoreDate";
	private static final String COL_CERTIFICATE = "certificate";

	private List<String> colMapping;	
	private List<String> userPropertyNameList;
	private final boolean isAdministrativeUser;
	private final List<UserPropertyHandler> userPropertyHandlers;
	public static final String usageIdentifyer = AssessedIdentitiesTableDataModel.class.getCanonicalName();
	private final Translator translator;
	
	private ConcurrentMap<Long, CertificateLight> certificates;

	/**
	 * 
	 * @param objects
	 * @param courseNode
	 * @param locale
	 * @param isAdministrativeUser
	 */
	public AssessedIdentitiesTableDataModel(List<AssessedIdentityWrapper> objects, ConcurrentMap<Long, CertificateLight> certificates,
			AssessableCourseNode courseNode, Locale locale, boolean isAdministrativeUser) {
		super(objects);
		this.courseNode = courseNode;		
		this.setLocale(locale);
		this.translator = Util.createPackageTranslator(this.getClass(), locale);
		this.certificates = certificates;
		
		this.isAdministrativeUser = isAdministrativeUser;
		userPropertyHandlers = UserManager.getInstance().getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		
		colCount = 0; // default
		colMapping = new ArrayList<String>();
		// store all configurable column positions in a lookup array
		if(isAdministrativeUser) {
			colMapping.add(colCount++, COL_NAME);	
		}
		
		Iterator <UserPropertyHandler> propHandlerIterator =  userPropertyHandlers.iterator();
		userPropertyNameList = new ArrayList<String>();
		while (propHandlerIterator.hasNext()) {
			String propHandlerName = propHandlerIterator.next().getName();
			userPropertyNameList.add(propHandlerName);
			colMapping.add(colCount++, propHandlerName);			
		}
	
		if (courseNode != null) {
			if (courseNode.hasDetails()) {
				colMapping.add(colCount++, COL_DETAILS);				
			}
			if (courseNode.hasAttemptsConfigured()) {
				colMapping.add(colCount++, COL_ATTEMPTS);				
			}
			if (courseNode.hasScoreConfigured()) {
				colMapping.add(colCount++, COL_SCORE);
				colMapping.add(colCount++, COL_MINSCORE);
				colMapping.add(colCount++, COL_MAXSCORE);
			}
			if (courseNode.hasStatusConfigured()) { 
				colMapping.add(colCount++, COL_STATUS);				
			}
			if (courseNode.hasPassedConfigured()) {
				colMapping.add(colCount++, COL_PASSED);			
			}
			colMapping.add(colCount++, COL_INITIAL_LAUNCH);
			colMapping.add(colCount++, COL_LAST_SCORE_DATE);
		}

		colMapping.add(colCount++, COL_CERTIFICATE);
	}
	
	public void setCertificates(ConcurrentMap<Long, CertificateLight> certificates) {
		this.certificates = certificates;
	}
	
	public void putCertificate(CertificateLight certificate) {
		if(certificates != null) {
			certificates.put(certificate.getIdentityKey(), certificate);
		}
	}
	
	public boolean replaceWrapper(AssessedIdentityWrapper wrappedIdentity) {
		boolean replaced = false;
		int index = getObjects().indexOf(wrappedIdentity);
		if(index >= 0 && index < getObjects().size()) {
			getObjects().set(index, wrappedIdentity);
			replaced = true;
		}
		return replaced;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return colCount;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int col) {
		AssessedIdentityWrapper wrappedIdentity = getObject(row);
		Identity identity = wrappedIdentity.getIdentity();

		// lookup the column name first and 
		// deliver value based on the column name
		String colName = colMapping.get(col);
		if (colName.equals(COL_NAME)) {
			return identity.getName();
		} else if (userPropertyNameList.contains(colName)) {
			return identity.getUser().getProperty(colName, getLocale());
		} else if (colName.equals(COL_DETAILS)) {
			return wrappedIdentity.getDetailsListView();
		} else if (colName.equals(COL_ATTEMPTS)) {
			return wrappedIdentity.getNodeAttempts();
		} else if (colName.equals(COL_SCORE)) {
			ScoreEvaluation scoreEval = wrappedIdentity.getUserCourseEnvironment().getScoreAccounting().evalCourseNode(courseNode);
			if (scoreEval == null) scoreEval = new ScoreEvaluation(null, null);
			return scoreEval.getScore();
		} else if (colName.equals(COL_MINSCORE)) {
			if(!(courseNode instanceof STCourseNode)) {
				return courseNode.getMinScoreConfiguration();
			}
			return "";
		} else if (colName.equals(COL_MAXSCORE)) {
			if(!(courseNode instanceof STCourseNode)) {
				return courseNode.getMaxScoreConfiguration();
			}
			return "";
		}	else if (colName.equals(COL_STATUS)) {
			return getStatusFor(courseNode, wrappedIdentity);
		} else if (colName.equals(COL_PASSED)) {
			ScoreEvaluation scoreEval = wrappedIdentity.getUserCourseEnvironment().getScoreAccounting().evalCourseNode(courseNode);
			if (scoreEval == null) scoreEval = new ScoreEvaluation(null, null);
			return scoreEval.getPassed();
		} else if (colName.equals(COL_INITIAL_LAUNCH)) {
			return wrappedIdentity.getInitialLaunchDate();
		} else if (colName.equals(COL_LAST_SCORE_DATE)) {
			return wrappedIdentity.getLastModified();
		} else if(colName.equals(COL_CERTIFICATE)) {
			if(certificates != null) {
				CertificateLight certificate = certificates.get(identity.getKey());
				if(certificate != null) {
					return new CertificateLightPack(certificate, identity);
				}
			}
			return null;
		} else {
			return "error";
		}
	}

	/**
	 * Return task Status (not_ok,ok,working_on) for a certain user and course
	 * @param courseNode
	 * @param wrappedIdentity
	 * @return
	 */
	private String getStatusFor(AssessableCourseNode courseNode, AssessedIdentityWrapper wrappedIdentity) {
		
	  CoursePropertyManager cpm = wrappedIdentity.getUserCourseEnvironment().getCourseEnvironment().getCoursePropertyManager();
		Property statusProperty;
		Translator trans = Util.createPackageTranslator(StatusForm.class, getLocale());
		statusProperty = cpm.findCourseNodeProperty(courseNode, wrappedIdentity.getIdentity(), null, StatusManager.PROPERTY_KEY_STATUS);
		if (statusProperty == null) {
			String value = trans.translate(StatusForm.PROPERTY_KEY_UNDEFINED);
			return value;
		} else {
			String value = trans.translate(StatusForm.STATUS_LOCALE_PROPERTY_PREFIX + statusProperty.getStringValue());
			return value;
	  }
	}
	
	/**
	 * Adds all ColumnDescriptors to the userListCtr. 
	 * @param userListCtr
	 * @param actionCommand
	 * @param isNodeOrGroupFocus
	 */
	public void addColumnDescriptors(TableController userListCtr, String actionCommand, boolean isNodeOrGroupFocus, boolean certificate) {
		String editCmd = null;
		if (courseNode == null || courseNode.isEditableConfigured()) {
			editCmd = actionCommand; // only selectable if editable
		}
		int colCount = 0;
		
		if(isAdministrativeUser) {
			userListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.name", colCount++, editCmd, getLocale()));
		}
		
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			userListCtr.addColumnDescriptor(visible, userPropertyHandler.getColumnDescriptor(colCount++, editCmd, getLocale()));	
		}		
		if ( (courseNode != null) && isNodeOrGroupFocus) {			
			if (courseNode.hasDetails()) {
				String headerKey = courseNode.getDetailsListViewHeaderKey();
				userListCtr.addColumnDescriptor((headerKey == null ? false : true), 
						new DefaultColumnDescriptor(headerKey == null ? "table.header.details" : headerKey, colCount++, null, getLocale()));
			}
			if (courseNode.hasAttemptsConfigured()) {				
				userListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.attempts", colCount++, null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT));
			}
			if (courseNode.hasScoreConfigured()) {				
				userListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.score", colCount++, null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT,
						new ScoreCellRenderer()));
				userListCtr.addColumnDescriptor(false, new CustomRenderColumnDescriptor("table.header.min", colCount++, null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT,
						new ScoreCellRenderer()));
				userListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.max", colCount++, null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT,
						new ScoreCellRenderer()));
			}
			if (courseNode.hasStatusConfigured()) {				
				userListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.status", colCount++, null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT));
			}
			if (courseNode.hasPassedConfigured()) {				
				userListCtr.addColumnDescriptor(new BooleanColumnDescriptor("table.header.passed", colCount++, translator.translate("passed.true"), 
						translator.translate("passed.false")));
			}
			userListCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.initialLaunchDate", colCount++, null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT));
			userListCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.lastScoreDate", colCount++, null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT));
		}
		
		if(certificate) {
			CustomRenderColumnDescriptor statColdesc = new CustomRenderColumnDescriptor("table.header.certificate", colCount++, null, getLocale(),
					ColumnDescriptor.ALIGNMENT_LEFT, new DownloadCertificateCellRenderer());
			userListCtr.addColumnDescriptor(statColdesc);
		}
	}
}