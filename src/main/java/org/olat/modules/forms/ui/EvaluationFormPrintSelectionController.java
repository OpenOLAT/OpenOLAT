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
package org.olat.modules.forms.ui;

import java.util.Collection;
import java.util.stream.Stream;

import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.media.MediaResource;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormPrintSelection;
import org.olat.modules.forms.Figures;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.Form;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 01.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormPrintSelectionController extends FormBasicController {
	
	public enum Target { PRINT, PDF }
	
	private static final String OVERVIEW = "report.print.selection.overview";
	private static final String TABLES = "report.print.selection.tables";
	private static final String DIAGRAMS = "report.print.selection.diagrams";
	private static final String SESSIONS = "report.print.selection.sessions";
	private static final String[] REPORT_KEYS = new String[] {
			OVERVIEW,
			TABLES,
			DIAGRAMS,
			SESSIONS
	};

	private MultipleSelectionElement reportsEl;
	private Link printLink;
	private Link pdfLink;

	private final Form form;
	private final DataStorage storage;
	private final SessionFilter filter;
	private final Figures figures;
	private final ReportHelper reportHelper;
	private final Target target;
	private final EvaluationFormPrintSelection printSelection;
	private final String title;
	
	@Autowired
	private PdfModule pdfModule;
	@Autowired
	private PdfService pdfService;
	
	public EvaluationFormPrintSelectionController(UserRequest ureq, WindowControl wControl, Form form,
			DataStorage storage, SessionFilter filter, Figures figures, ReportHelper reportHelper, Target target) {
		this(ureq, wControl, form, storage, filter, figures, reportHelper, target, null);
	}

	public EvaluationFormPrintSelectionController(UserRequest ureq, WindowControl wControl, Form form,
			DataStorage storage, SessionFilter filter, Figures figures, ReportHelper reportHelper, Target target,
			String title) {
		super(ureq, wControl, "report_print_selection");
		this.form = form;
		this.storage = storage;
		this.filter = filter;
		this.figures = figures;
		this.reportHelper = reportHelper;
		this.title = title;
		this.target = target;
		this.printSelection = new EvaluationFormPrintSelection();
		this.printSelection.setOverview(true);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		reportsEl = uifactory.addCheckboxesVertical("report.print.selection", formLayout, REPORT_KEYS, translateKeys(REPORT_KEYS), 1);
		reportsEl.select(OVERVIEW, printSelection.isOverview());
		reportsEl.select(TABLES, printSelection.isTables());
		reportsEl.select(DIAGRAMS, printSelection.isDiagrams());
		reportsEl.select(SESSIONS, printSelection.isTables());
		reportsEl.addActionListener(FormEvent.ONCHANGE);
		
		if (Target.PRINT.equals(target)) {
			printLink = LinkFactory.createButtonSmall("report.print", flc.getFormItemComponent(), this);
			printLink.setIconLeftCSS("o_icon o_icon-fw o_icon_eva_print");
			printLink.setPopup(new LinkPopupSettings(950, 750, "report"));
		} else if (Target.PDF.equals(target)) {
			pdfLink = LinkFactory.createButtonSmall("report.pdf", flc.getFormItemComponent(), this);
			pdfLink.setIconLeftCSS("o_icon o_icon-fw o_icon_eva_pdf");
		}
	}
	
	private String[] translateKeys(String[] keys) {
		return Stream.of(keys)
				.map(key -> getTranslator().translate(key))
				.toArray(String[]::new);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == reportsEl) {
			doSelectReports();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link) {
			Link link = (Link) source;
			String cmd = link.getCommand();
			if (cmd.equals("report.print")) {
				doPrint(ureq);
			} else if (cmd.equals("report.pdf")) {
				doExportPdf(ureq);
			}
			fireEvent(ureq, Event.DONE_EVENT);
		}
		super.event(ureq, source, event);
	}

	private void doSelectReports() {
		Collection<String> selectedKeys = reportsEl.getSelectedKeys();
		boolean overview = selectedKeys.contains(OVERVIEW);
		printSelection.setOverview(overview);
		boolean tables = selectedKeys.contains(TABLES);
		printSelection.setTables(tables);
		boolean diagrams = selectedKeys.contains(DIAGRAMS);
		printSelection.setDiagrams(diagrams);
		boolean sessions = selectedKeys.contains(SESSIONS);
		printSelection.setSessions(sessions);
	}

	private void doPrint(UserRequest ureq) {
		ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createPrintPopupLayout(getControllerCreator());
		openInNewBrowserWindow(ureq, layoutCtrlr, true);
	}
	
	private void doExportPdf(UserRequest ureq) {
		if (!pdfModule.isEnabled()) {
			showWarning("report.pdf.warning.disabled");
			return;
		}
		MediaResource resource = pdfService.convert("report", getIdentity(), getControllerCreator(), getWindowControl());
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private ControllerCreator getControllerCreator() {
		return new EvaluationFormPrintControllerCreator(form, storage, filter, figures, reportHelper, printSelection, title);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
