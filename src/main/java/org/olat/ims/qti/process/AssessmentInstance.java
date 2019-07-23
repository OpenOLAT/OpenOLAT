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

package org.olat.ims.qti.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.ims.qti.container.AssessmentContext;
import org.olat.ims.qti.container.SectionContext;
import org.olat.ims.qti.navigator.MenuItemNavigator;
import org.olat.ims.qti.navigator.MenuSectionNavigator;
import org.olat.ims.qti.navigator.Navigator;
import org.olat.ims.qti.navigator.NavigatorDelegate;
import org.olat.ims.qti.navigator.SequentialItemNavigator;
import org.olat.ims.qti.navigator.SequentialSectionNavigator;
import org.olat.modules.ModuleConfiguration;

/**
 * @author Felix Jost
 */
public class AssessmentInstance implements Serializable {

	private boolean closed;
	private transient Persister persister;
	private transient Resolver resolver;
	
	private long assessID; // the key given to this instance by the constructor; identifying the assessment within the olat context; needed after deserialisation to find the correct qti tree
	private long repositoryEntryKey; // the key to a repository entry
	private Navigator navigator; // optimise: make transient
	
	private long callingResId;
	private String callingResDetail;
	private List<String> sourceBankRefs;
	private String remoteAddr;
	private Identity assessedIdentity;
	
	private AssessmentContext assessmentContext;
	
	private boolean menu;
	private boolean displayTitles;
	private boolean autoEnum;
	private boolean memo;
	private int sequence;
	private int type;
	private int summaryType;
	
	private boolean resuming; // is the test resuming, meaning that it has been deserialized from disk and is now continued
	private boolean preview;
	
	// type identifiers
	protected static final int TYPE_ASSESS = 1;
	protected static final int TYPE_SELF = 2;
	protected static final int TYPE_SURVEY = 3;
	protected static final int TYPE_QUIZ = 4;

	// sequence identifiers
	protected static final int SEQUENCE_ITEM = 1;
	protected static final int SEQUENCE_SECTION = 2;
	
	// summary identifiers
	public static final int SUMMARY_NONE = 0;
	public static final int SUMMARY_COMPACT = 1;
	public static final int SUMMARY_DETAILED = 2;
	public static final int SUMMARY_SECTION = 3;
	
	// default values
	protected static final boolean MENU_DEFAULT = true;
	protected static final boolean DISPLAYTITLES_DEFAULT = true;
	protected static final boolean AUTOENUM_DEFAULT = false;
	protected static final boolean MEMO_DEFAULT = false;
	protected static final int TYPE_DEFAULT = TYPE_SELF;
	protected static final int SEQUENCE_DEFAULT = SEQUENCE_ITEM;
	protected static final int SUMMARY_DEFAULT = SUMMARY_COMPACT;
	
	// metadata field label/field values
	public static final String QMD_LABEL_TYPE = "qmd_assessmenttype";
	public static final String QMD_ENTRY_TYPE_ASSESS = "Assessment";
	public static final String QMD_ENTRY_TYPE_SELF = "Self-Assessment";
	public static final String QMD_ENTRY_TYPE_SURVEY = "Survey";


	public static final String QMD_LABEL_MENU = "qmd_navigatormenutype";
	public static final String QMD_ENTRY_MENU_YES = "menu";
	public static final String QMD_ENTRY_MENU_NO = "none";
	
	public static final String QMD_LABEL_SEQUENCE = "qmd_navigatorpagetype";
	public static final String QMD_ENTRY_SEQUENCE_SECTION = "sectionPage";
	public static final String QMD_ENTRY_SEQUENCE_ITEM = "itemPage";
	
	public static final String QMD_ENTRY_SUMMARY_NONE = "summaryNone";
	public static final String QMD_ENTRY_SUMMARY_COMPACT = "summaryCompact";
	public static final String QMD_ENTRY_SUMMARY_DETAILED = "summaryDetailed";
	public static final String QMD_ENTRY_SUMMARY_SECTION = "summarySection";

	
	
	/**
	 * Constructor for AssessmentInstance. 
	 * Needed for deserialisation
	 */
	public AssessmentInstance() {
		super();
	}

	/**
	 * 
	 * @param repositoryEntryKey
	 * @param assessID
	 * @param resolver
	 * @param persistor the Persistor, may be null
	 * @param modConfig
	 */
	public AssessmentInstance(Identity identity, String remoteAddr, long repositoryEntryKey, long assessID, long callingResId, String callingResDetail,
			Resolver resolver, Persister persistor, ModuleConfiguration modConfig, NavigatorDelegate delegate) {
		this.assessedIdentity = identity;
		this.remoteAddr = remoteAddr;
		this.callingResId = callingResId;
		this.callingResDetail = callingResDetail;
		this.repositoryEntryKey = repositoryEntryKey;
		this.assessID = assessID;
		this.resolver = resolver;
		this.persister = persistor;
		resuming = false;
		preview = false;
		closed = false;
		
		// find the olat extension of qti xml to find out of we should have menu and/or sectionPage. and to find out the type (assessment, survey, ...)

		// fetch display titles option
		displayTitles = DISPLAYTITLES_DEFAULT;
		Boolean confDisplayTitles = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_QUESTIONTITLE);
		if (confDisplayTitles != null) {
			displayTitles = confDisplayTitles.booleanValue();
		}
		
		// fetch auto enum choice options switch
		autoEnum = AUTOENUM_DEFAULT;
		Boolean confAutoEnum = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_AUTOENUM_CHOICES);
		if (confAutoEnum != null) {
			autoEnum = confAutoEnum.booleanValue();
		}
		
		// fetch auto enum choice options switch
		memo = MEMO_DEFAULT;
		Boolean confMemo = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_MEMO);
		if (confMemo != null) {
			memo = confMemo.booleanValue();
		}
		
		// fetch type of menu
		menu = MENU_DEFAULT;
		Boolean confEnableMenu = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_ENABLEMENU);
		if (confEnableMenu != null) {
			menu = confEnableMenu.booleanValue();
		}

		// fetch type of assessment
		type = TYPE_DEFAULT; // default to self-assessment
		String confMode = (String)modConfig.get(IQEditController.CONFIG_KEY_TYPE);
		if (confMode != null) {
			if (confMode.equals(QMD_ENTRY_TYPE_SELF)) type = TYPE_SELF;
			else if (confMode.equals(QMD_ENTRY_TYPE_ASSESS)) type = TYPE_ASSESS;
			else if (confMode.equals(QMD_ENTRY_TYPE_SURVEY)) type = TYPE_SURVEY;
		}

		// fetch type of sequence
		sequence = SEQUENCE_DEFAULT;
		String confSequence = (String)modConfig.get(IQEditController.CONFIG_KEY_SEQUENCE);
		if (confSequence != null) {
			if (confSequence.equals(QMD_ENTRY_SEQUENCE_ITEM)) sequence = SEQUENCE_ITEM;
			else if (confSequence.equals(QMD_ENTRY_SEQUENCE_SECTION)) sequence = SEQUENCE_SECTION;
		}
		
		// fetch type of summary
		summaryType = SUMMARY_DEFAULT;
		String confSummary = (String)modConfig.get(IQEditController.CONFIG_KEY_SUMMARY);
		if (confSummary != null) {
			if (confSummary.equals(QMD_ENTRY_SUMMARY_NONE)) summaryType = SUMMARY_NONE;
			else if (confSummary.equals(QMD_ENTRY_SUMMARY_COMPACT)) summaryType = SUMMARY_COMPACT;
			else if (confSummary.equals(QMD_ENTRY_SUMMARY_DETAILED)) summaryType = SUMMARY_DETAILED;
			else if (confSummary.equals(QMD_ENTRY_SUMMARY_SECTION)) summaryType = SUMMARY_SECTION;
		}
		
		assessmentContext = new AssessmentContext();
		assessmentContext.setUp(this);
		createNavigator(delegate);
	}

	public Identity getAssessedIdentity() {
		return assessedIdentity;
	}
	
	public void setAssessedIdentity(Identity assessedIdentity) {
		this.assessedIdentity = assessedIdentity;
	}

	public String getRemoteAddr() {
		return remoteAddr;
	}

	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	public long getCallingResId() {
		return callingResId;
	}
	
	public void setCallingResId(long callingResId) {
		this.callingResId = callingResId;
	}
	
	public String getCallingResDetail() {
		return callingResDetail;
	}
	
	public void setCallingResDetail(String callingResDetail) {
		this.callingResDetail = callingResDetail;
	}

	public int getType() { return type; }

	/**
	 * Return this instance's type as readable string
	 * @return String
	 */
	public String getFormattedType() {
		return getFormattedType(type);
	}
	
	/**
	 * Return the type code as readable string.
	 * @param iType
	 * @return String
	 */
	public static String getFormattedType(int iType) {
		switch (iType) {
			case TYPE_ASSESS: return QMD_ENTRY_TYPE_ASSESS;
			case TYPE_SELF: return QMD_ENTRY_TYPE_SELF;
			case TYPE_SURVEY: return QMD_ENTRY_TYPE_SURVEY;
		}
		return null; // error?
	}
	
	public boolean isAssess() { return type == TYPE_ASSESS;	}
	public boolean isSelfAssess() { return type == TYPE_SELF;	}
	public boolean isSurvey() { return type == TYPE_SURVEY;	}
	
	private void createNavigator(NavigatorDelegate delegate) {
		if (menu) {
			if (sequence == SEQUENCE_SECTION) {
				navigator = new MenuSectionNavigator(this, delegate);
			} 
			else {
				navigator = new MenuItemNavigator(this, delegate);
			}
		}
		else { // not menu
			if (sequence == SEQUENCE_SECTION) {
				navigator = new SequentialSectionNavigator(this, delegate);
			} 
			else {
				navigator = new SequentialItemNavigator(this, delegate);
			}
		}
	}
	
	
	public void resume() {
		// we have just been relived be ...deserialize and now need to grab the document at its correct ends.
		//createNavigator(); -> but status is missing!!
		//assessmentContext.resume();	
	}

	/**
	 * Returns the navigator.
	 * @return Navigator
	 */
	public Navigator getNavigator() {
		return navigator;
	}

	/**
	 * 
	 */
	public void start() {
		assessmentContext.start();
	}

	/**
	 * @return AssessmentContext
	 */
	public AssessmentContext getAssessmentContext() {
		return assessmentContext;
	}


	/**
	 * @return boolean
	 */
	public boolean isMenu() {
		return menu;
	}

	/**
	 * @return boolean
	 */
	public boolean isDisplayTitles() {
		return displayTitles;
	}
	
	/**
	 * @return boolean
	 */
	public boolean isAutoEnum() {
		return autoEnum;
	}
	
	/**
	 * @return boolean
	 */
	public boolean isSectionPage() {
		return (sequence == SEQUENCE_SECTION);
	}
	
	/**
	 * Get the summary type
	 * @return
	 */
	public int getSummaryType() { return summaryType; }
	
	/**
	 * Maps the configured summary to the summary types.
	 * @param confSummary
	 * @return the summaryType if valid input confSummary, else throws a AssertException!
	 */
	public static int getSummaryType(String confSummary) {
		boolean validSummary = false;
		int summaryType = SUMMARY_NONE;
		if (confSummary != null) {
			if (confSummary.equals(QMD_ENTRY_SUMMARY_NONE)) {
				summaryType = SUMMARY_NONE;
				validSummary = true;
			}
			else if (confSummary.equals(QMD_ENTRY_SUMMARY_COMPACT)) {
				summaryType = SUMMARY_COMPACT;
				validSummary = true;
			}
			else if (confSummary.equals(QMD_ENTRY_SUMMARY_DETAILED)) {
				summaryType = SUMMARY_DETAILED;
				validSummary = true;
			}
			else if (confSummary.equals(QMD_ENTRY_SUMMARY_SECTION)) {
				summaryType = SUMMARY_SECTION;
				validSummary = true;
			}
		}
		if(!validSummary) {
			throw new AssertException("wrong confSummary"); 
		}
		return summaryType;
	}

	/**
	 * Method close.
	 */
	public void stop() {
		closed = true;
		assessmentContext.stop();
		SectionContext sc = assessmentContext.getCurrentSectionContext();
		if (sc != null) sc.setCurrentItemContextPos(-1);
		assessmentContext.setCurrentSectionContextPos(-1);
	}
	
	public void cleanUp() {
		if (persister != null) {
			persister.cleanUp();
		}
	}

	/**
	 * @return Resolver
	 */
	public Resolver getResolver() {
		return resolver;
	}

	/**
	 * Sets the resolver.
	 * @param resolver The resolver to set
	 */
	public void setResolver(Resolver resolver) {
		this.resolver = resolver;
	}

	/**
	 * @return Persister
	 */
	public Persister getPersister() {
		return persister;
	}

	/**
	 * Sets the persistor.
	 * @param persistor The persistor to set
	 */
	public void setPersister(Persister persister) {
		this.persister = persister;
	}

	/**
	 * 
	 */
	public void persist() {
		if (!closed) { // ignore if assessment has already been closed
			Persister pers = getPersister();
			if (pers != null) { // can be null for e.g. the preview mode
				pers.persist(this, "assessment " + getAssessmentContext().getIdent());		
			}
		}
	}

	/**
	 * Returns the assessID.
	 * @return long
	 */
	public long getAssessID() {
		return assessID;
	}

	/**
	 * Returns the dlVersionID.
	 * @return long
	 */
	public long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}

	/**
	 * @param sourceBankRef
	 */
	public void addObjectBankRef(String sourceBankRef) {
		if (sourceBankRefs == null) {
			sourceBankRefs = new ArrayList<>(1);
		}
		sourceBankRefs.add(sourceBankRef);
	}
	
	

	/**
	 * @return List of all references to sourcebanks that have been used in this run of the assessment
	 */
	public List<String> getSourceBankRefs() {
		return sourceBankRefs;
	}

	/**
	 * @return
	 */
	public boolean isClosed() {
		return closed;
	}

	/**
	 * @return boolean
	 */
	public boolean isResuming() {
		return resuming;
	}

	/**
	 * Sets the resuming.
	 * @param resuming The resuming to set
	 */
	public void setResuming(boolean resuming) {
		this.resuming = resuming;
	}

	/**
	 * @return
	 */
	public boolean isPreview() {
		return preview;
	}

	/**
	 * @param b
	 */
	public void setPreview(boolean b) {
		preview = b;
	}
	
	public void setDelegate(NavigatorDelegate delegate) {
		if(navigator != null) {
			navigator.setDelegate(delegate);
		}
	}
	
	/*
	 * For marking/flagging question items ... OLAT-5807
	 */
	private Map <String,Boolean>marked = new HashMap<>();
	
	public void mark (String id, boolean b) {
		marked.put(id, Boolean.valueOf(b));
	}
	
	public boolean isMarked (String id) {
		Boolean rv = marked.get(id);
		return rv == null ? false : rv;
	}
	
	/*
	 * For memos/notes  ... OLAT-5809
	 */
	private Map <String,String>memos = new HashMap<>();
	
	public void setMemo (String id, String m) {
		memos.put(id, m);
	}
	
	public String getMemo (String id) {
		return memos.get(id);
	}
}
