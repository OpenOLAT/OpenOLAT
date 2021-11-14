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
package org.olat.course.nodes.iq;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.ZipOutputStream;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.QTICourseNode;
import org.olat.repository.RepositoryEntry;

/**
 * This controller will make an archive of the current test entry results
 * and propose some informations.
 * 
 * 
 * Initial date: 24 mai 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmChangeResourceController extends FormBasicController {
	
	private FormLink downloadButton;
	
	private final ICourse course;
	private final File downloadArchiveFile;
	private final QTICourseNode courseNode;
	private final RepositoryEntry newTestEntry;
	private final RepositoryEntry currentTestEntry;
	private final int numOfAssessedIdentities;
	
	public ConfirmChangeResourceController(UserRequest ureq, WindowControl wControl, ICourse course, QTICourseNode courseNode,
			RepositoryEntry newTestEntry, RepositoryEntry currentTestEntry, int numOfAssessedIdentities) {
		super(ureq, wControl, "confirm_change");
		this.course = course;
		this.courseNode = courseNode;
		this.newTestEntry = newTestEntry;
		this.currentTestEntry = currentTestEntry;
		this.numOfAssessedIdentities = numOfAssessedIdentities;
		downloadArchiveFile = prepareArchive(ureq);
		initForm(ureq);
	}
	
	public RepositoryEntry getNewTestEntry() {
		return newTestEntry;
	}
	
	public RepositoryEntry getCurrentTestEntry() {
		return currentTestEntry;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("infos1", translate("confirmation.change.warning.1", new String[]{ Integer.toString(numOfAssessedIdentities) }));
			layoutCont.contextPut("infos2", translate("confirmation.change.warning.2"));
			String[] archiveArgs = new String[] { downloadArchiveFile.getParentFile().getName(), downloadArchiveFile.getName() };
			layoutCont.contextPut("infos3", translate("confirmation.change.warning.3", archiveArgs));
		}
		
		downloadButton = uifactory.addFormLink("download", downloadArchiveFile.getName(), null, formLayout, Link.LINK | Link.NONTRANSLATED);
		downloadButton.setIconLeftCSS("o_icon o_icon_downloads");
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("ok", formLayout);
	}
	
	private File prepareArchive(UserRequest ureq) {
		File exportDir = CourseFactory.getOrCreateDataExportDirectory(ureq.getIdentity(), course.getCourseTitle());
		String label = StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName())
				+ "_" + Formatter.formatDatetimeWithMinutes(new Date()) + ".zip";
		String urlEncodedLabel = StringHelper.urlEncodeUTF8(label);
		File archiveFile = new File(exportDir, urlEncodedLabel);
		
		try(OutputStream out= new FileOutputStream(archiveFile);
				ZipOutputStream exportStream = new ZipOutputStream(out)) {
			courseNode.archiveNodeData(getLocale(), course, null, exportStream, "", "UTF8");
			return archiveFile;
		} catch(Exception e) {
			logError("", e);
			return null;
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(downloadButton == source) {
			doDownload(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doDownload(UserRequest ureq) {
		ureq.getDispatchResult()
			.setResultingMediaResource(new FileMediaResource(downloadArchiveFile, true));
	}
}