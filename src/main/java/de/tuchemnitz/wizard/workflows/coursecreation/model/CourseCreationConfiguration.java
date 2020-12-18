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
* <p>
* Initial code contributed and copyrighted by<br>
* Technische Universitaet Chemnitz Lehrstuhl Technische Informatik<br>
* <br>
* Author Marcel Karras (toka@freebits.de)<br>
* Author Norbert Englisch (norbert.englisch@informatik.tu-chemnitz.de)<br>
* Author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
*/

package de.tuchemnitz.wizard.workflows.coursecreation.model;

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.velocity.context.Context;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.gui.DefaultGlobalSettings;
import org.olat.core.gui.GlobalSettings;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.velocity.VelocityHelper;
import org.olat.core.gui.render.velocity.VelocityRenderDecorator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.repository.CatalogEntry;
import org.olat.repository.wizard.AccessAndProperties;

import de.tuchemnitz.wizard.workflows.coursecreation.CourseCreationHelper;

/**
 * Description:<br>
 * Course Creation Configuration model with data configured by workflow
 * controllers.
 * 
 * <P>
 * Initial Date: 07.07.2008 <br>
 * 
 * @author Marcel Karras (toka@freebits.de)
 * @author Norbert Englisch (norbert.englisch@informatik.tu-chemnitz.de)
 * @author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
 */
public class CourseCreationConfiguration {
	
	private static final Logger log = Tracing.createLoggerFor(CourseCreationConfiguration.class);

	public static final String ACL_GUEST = "acl_guest";
	public static final String ACL_OLAT = "acl_olat";
	public static final String ACL_UNI = "acl_uni";

	private final String extLink;
	private final String courseTitle;
	private String aclType = "";
	// contact form creation switch
	private boolean createContactForm = false;
	// download folder creation swith
	private boolean createDownloadFolder = false;
	// enrollment node creation switch
	private boolean createEnrollment = false;
	// forum creation switch
	private boolean createForum = false;
	// single page creation switch
	private boolean createSinglePage = false;
	// limit access
	private Boolean enableAccessLimit = false;
	// access limits on contact form
	private boolean enableAclContactForm = false;
	// access limits on download folder
	private boolean enableAclDownloadFolder = false;
	// access limits on forum
	private boolean enableAclForum = false;
	// access limits on single page
	private boolean enableAclSinglePage = false;
	// follow up
	private Boolean enableFollowup = true;
	// enable signout
	private Boolean enableSignout = true;
	// wait list
	private Boolean enableWaitlist = true;
	// group count
	private Integer groupCount = 1;
	// subscriber count
	private Integer subscriberCount = null;
	// publish the course
	private Boolean publish = true;
	// selected catalog entry
	private CatalogEntry selectedParent = null;
	
	private AccessAndProperties accessAndProperties;

	public CourseCreationConfiguration(final String courseTitle, final String extLink) {
		this.courseTitle = courseTitle;
		this.extLink = extLink;
	}
	
	public AccessAndProperties getAccessAndProperties() {
		return accessAndProperties;
	}

	public void setAccessAndProperties(AccessAndProperties accessAndProperties) {
		this.accessAndProperties = accessAndProperties;
	}

	/**
	 * @return Returns the publish.
	 */
	public final Boolean getPublish() {
		return publish;
	}

	/**
	 * @param publish The publish to set.
	 */
	public final void setPublish(Boolean publish) {
		this.publish = publish;
	}

	/**
	 * @return Returns the aclType.
	 */
	public String getAclType() {
		return aclType;
	}

	/**
	 * @return Returns the enableAccessLimit.
	 */
	public Boolean getEnableAccessLimit() {
		return enableAccessLimit;
	}

	/**
	 * @return Returns the enableFollowup.
	 */
	public Boolean getEnableFollowup() {
		return enableFollowup;
	}

	/**
	 * @return Returns the enableSignout.
	 */
	public Boolean getEnableSignout() {
		return enableSignout;
	}

	/**
	 * @return Returns the enableWaitlist.
	 */
	public Boolean getEnableWaitlist() {
		return enableWaitlist;
	}

	/**
	 * @return Returns the groupCount.
	 */
	public Integer getGroupCount() {
		return groupCount;
	}

	/**
	 * @return Returns the subscriberCount.
	 */
	public Integer getSubscriberCount() {
		return subscriberCount;
	}

	/**
	 * @return Returns the createContactForm.
	 */
	public boolean isCreateContactForm() {
		return createContactForm;
	}

	/**
	 * @return Returns the createDownloadFolder.
	 */
	public boolean isCreateDownloadFolder() {
		return createDownloadFolder;
	}

	/**
	 * @return Returns the createEnrollment.
	 */
	public boolean isCreateEnrollment() {
		return createEnrollment;
	}

	/**
	 * @return Returns the createForum.
	 */
	public boolean isCreateForum() {
		return createForum;
	}

	/**
	 * @return Returns the createSinglePage.
	 */
	public boolean isCreateSinglePage() {
		return createSinglePage;
	}

	/**
	 * @return Returns the enableAclContactForm.
	 */
	public boolean isEnableAclContactForm() {
		return enableAclContactForm;
	}

	/**
	 * @return Returns the enableAclDownloadFolder.
	 */
	public boolean isEnableAclDownloadFolder() {
		return enableAclDownloadFolder;
	}

	/**
	 * @return Returns the enableAclForum.
	 */
	public boolean isEnableAclForum() {
		return enableAclForum;
	}

	/**
	 * @return Returns the enableAclSinglePage.
	 */
	public boolean isEnableAclSinglePage() {
		return enableAclSinglePage;
	}

	/**
	 * @param aclType The aclType to set.
	 */
	public void setAclType(String aclType) {
		this.aclType = aclType;
	}

	/**
	 * @param createContactForm The createContactForm to set.
	 */
	public void setCreateContactForm(boolean createContactForm) {
		this.createContactForm = createContactForm;
	}

	/**
	 * @param createDownloadFolder The createDownloadFolder to set.
	 */
	public void setCreateDownloadFolder(boolean createDownloadFolder) {
		this.createDownloadFolder = createDownloadFolder;
	}

	/**
	 * @param createEnrollment The createEnrollment to set.
	 */
	public void setCreateEnrollment(boolean createEnrollment) {
		this.createEnrollment = createEnrollment;
	}

	/**
	 * @param createForum The createForum to set.
	 */
	public void setCreateForum(boolean createForum) {
		this.createForum = createForum;
	}

	/**
	 * @param createSinglePage The createSinglePage to set.
	 */
	public void setCreateSinglePage(boolean createSinglePage) {
		this.createSinglePage = createSinglePage;
	}

	/**
	 * @param enableAccessLimit The enableAccessLimit to set.
	 */
	public void setEnableAccessLimit(Boolean enableAccessLimit) {
		this.enableAccessLimit = enableAccessLimit;
	}

	/**
	 * @param enableAclContactForm The enableAclContactForm to set.
	 */
	public void setEnableAclContactForm(boolean enableAclContactForm) {
		this.enableAclContactForm = enableAclContactForm;
	}

	/**
	 * @param enableAclDownloadFolder The enableAclDownloadFolder to set.
	 */
	public void setEnableAclDownloadFolder(boolean enableAclDownloadFolder) {
		this.enableAclDownloadFolder = enableAclDownloadFolder;
	}

	/**
	 * @param enableAclForum The enableAclForum to set.
	 */
	public void setEnableAclForum(boolean enableAclForum) {
		this.enableAclForum = enableAclForum;
	}

	/**
	 * @param enableAclSinglePage The enableAclSinglePage to set.
	 */
	public void setEnableAclSinglePage(boolean enableAclSinglePage) {
		this.enableAclSinglePage = enableAclSinglePage;
	}

	/**
	 * @param enableFollowup The enableFollowup to set.
	 */
	public void setEnableFollowup(Boolean enableFollowup) {
		this.enableFollowup = enableFollowup;
	}

	/**
	 * @param enableSignout The enableSignout to set.
	 */
	public void setEnableSignout(Boolean enableSignout) {
		this.enableSignout = enableSignout;
	}

	/**
	 * @param enableWaitlist The enableWaitlist to set.
	 */
	public void setEnableWaitlist(Boolean enableWaitlist) {
		this.enableWaitlist = enableWaitlist;
	}

	/**
	 * @param groupCount The groupCount to set.
	 */
	public void setGroupCount(Integer groupCount) {
		this.groupCount = groupCount;
	}

	/**
	 * @param translator
	 * @return single page content
	 */
	public String getSinglePageText(Translator translator) {
		VelocityContainer vc = new VelocityContainer("singlePageTemplate", CourseCreationHelper.class, "singlePageTemplate", translator, null);
		vc.contextPut("coursetitle", courseTitle);
		
		//prepare rendering of velocity page for the content of the single page node
		GlobalSettings globalSettings = new DefaultGlobalSettings();
		
		Context context = vc.getContext();
		Renderer fr = Renderer.getInstance(vc, translator, null, new RenderResult(), globalSettings, "-");// static page
		try(StringOutput wOut = new StringOutput(10000);
			VelocityRenderDecorator vrdec = new VelocityRenderDecorator(fr, vc, wOut)) {			
			context.put("r", vrdec);
			VelocityHelper.getInstance().mergeContent(vc.getPage(), context, wOut, null);
			//free the decorator
			context.remove("r");
			return WysiwygFactory.createXHtmlFileContent(wOut.toString(), courseTitle);
		} catch(IOException e) {
			log.error("", e);
			return null;
		}
	}

	/**
	 * @param subscriberCount The subscriberCount to set.
	 */
	public void setSubscriberCount(Integer subscriberCount) {
		this.subscriberCount = subscriberCount;
	}
	
	public void setSelectedCatalogEntry(CatalogEntry selectedParent) {
		this.selectedParent = selectedParent;
	}

	public final String getCourseTitle() {
		return courseTitle;
	}

	public final String getExtLink() {
		return extLink;
	}
	
	public final CatalogEntry getSelectedCatalogEntry() {
		return this.selectedParent;
	}

}
