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
package org.olat.modules.qpool.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CSSIconFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.ExportFormatOptions;
import org.olat.modules.qpool.QPoolSPI;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.manager.QuestionPoolLicenseHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19.01.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CreateTestOverviewController extends FormBasicController {
	
	private static final String[] groupByKeys = new String[] { "on" };

	private final boolean withLicenses;
	private final boolean withTaxonomy;
	private final ExportFormatOptions format;
	
	private QItemDataModel itemsModel;
	private MultipleSelectionElement groupByEl;
	
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private QuestionPoolLicenseHandler licenseHandler;
	
	public CreateTestOverviewController(UserRequest ureq, WindowControl wControl, List<QuestionItemShort> items,
			ExportFormatOptions format, QPoolSecurityCallback secCallback) {
		super(ureq, wControl, "create_test");
		this.format = format;
		withTaxonomy = secCallback.canUseTaxonomy();
		withLicenses = licenseModule.isEnabled(licenseHandler);
		initForm(ureq);
		loadModel(items);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, "export.overview.accept", Cols.accept.ordinal(),
				false, null, FlexiColumnModel.ALIGNMENT_LEFT,
				new BooleanCellRenderer(
						new CSSIconFlexiCellRenderer("o_icon_accept"),
						new CSSIconFlexiCellRenderer("o_icon_failed"))
		));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.topic));
		if(withTaxonomy) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.taxonomyLevel));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.taxonomyPath));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.format));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.status, new QuestionStatusCellRenderer(getTranslator())));
		if(withLicenses) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.license));
		}
		itemsModel = new QItemDataModel(columnsModel, format, getLocale());
		uifactory.addTableElement(getWindowControl(), "shares", itemsModel, getTranslator(), formLayout);
		
		String[] groupByValues = new String[] { translate("group.by.taxonomy.level") };
		groupByEl = uifactory.addCheckboxesHorizontal("group.by", null, formLayout, groupByKeys, groupByValues);
		groupByEl.setVisible(withTaxonomy);
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("create.test", formLayout);
	}
	
	private void loadModel(List<QuestionItemShort> items) {
		List<ResourceLicense> resourceLicenses = licenseService.loadLicenses(items);
		Map<Long,ResourceLicense> resourceLicensesMap = resourceLicenses.stream()
			.collect(Collectors.toMap(ResourceLicense::getResId, l -> l, (u, v) -> u));
		List<QuestionRow> rows = items.stream()
				.map(item -> new QuestionRow(item, resourceLicensesMap.get(item.getKey())))
				.collect(Collectors.toList());
		itemsModel.setObjects(rows);
		if(withLicenses) {
			Set<LicenseType> licenseTypes = resourceLicenses.stream()
					.map(ResourceLicense::getLicenseType)
					.collect(Collectors.toSet());
			if(licenseTypes.size() > 1) {
				flc.contextPut("licenseWarning", Boolean.TRUE);
			}
		}
	}
	
	public String getResourceTypeFormat() {
		return format.getResourceTypeFormat();
	}
	
	public LicenseType getLicenseType() {
		List<QuestionRow> items = getExportableQuestionRows();
		List<LicenseType> licenseTypes = items.stream()
				.filter(item -> item.getLicense() != null)
				.map(QuestionRow::getLicense)
				.map(ResourceLicense::getLicenseType)
				.collect(Collectors.toList());
		Collections.sort(licenseTypes, new LicenseRestrictionComparator());
		return licenseTypes.isEmpty() ? null : licenseTypes.get(0);
	}

	public List<QuestionItemShort> getExportableQuestionItems() {
		List<QuestionRow> rows =getExportableQuestionRows();
		return rows.stream().map(QuestionRow::getQuestion).collect(Collectors.toList());
	}
	
	public List<QuestionRow> getExportableQuestionRows() {
		List<QuestionRow> exportableItems = new ArrayList<>(itemsModel.getRowCount());
		for(int i=0; i<itemsModel.getRowCount(); i++) {
			if(Boolean.TRUE.equals(itemsModel.getValueAt(i, Cols.accept.ordinal()))) {
				exportableItems.add(itemsModel.getObject(i));
			}
		}
		return exportableItems;
	}
	
	public boolean isGroupByTaxonomyLevel() {
		return groupByEl.isVisible() && groupByEl.isAtLeastSelected(1);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private static class LicenseRestrictionComparator implements Comparator<LicenseType> {
		
		@Override
		public int compare(LicenseType o1, LicenseType o2) {
			int s1 = getLicenseScore(o1);
			int s2 = getLicenseScore(o2);
			return Integer.compare(s2, s1);// descendant
		}
	
		private int getLicenseScore(LicenseType type) {
			int score = 0;
			if(type != null && type.getName() != null) {
				if(!type.isPredefined()) {
					score = 2000;
				} else if("all rights reserved".equals(type.getName())) {
					score = 1800;
				} else if("freetext".equals(type.getName())) {
					score = 1700;
				} else if(type.getName().startsWith("CC")) {
					score =  type.getName().length() * 100;
				} else if("public domain".equals(type.getName())) {
					score = 100;
				} else if("no.license".equals(type.getName())) {
					score = 100;
				} else {
					score = 50;
				}	
			}
			return score;
		}
	}
	
	private static class QuestionRow {
		
		private final QuestionItemShort question;
		private final ResourceLicense license;
		
		public QuestionRow(QuestionItemShort question, ResourceLicense license) {
			this.question = question;
			this.license = license;
		}
		
		public String getTitle() {
			return question.getTitle();
		}
		
		public String getTopic() {
			return question.getTopic();
		}
		
		public String getTaxonomyLevelName() {
			return question.getTaxonomyLevelName();
		}
		
		public String getTaxonomyPath() {
			return question.getTaxonomicPath();
		}
		
		public String getFormat() {
			return question.getFormat();
		}
		
		public ResourceLicense getLicense() {
			return license;
		}
		
		public QuestionStatus getQuestionStatus() {
			return question.getQuestionStatus();
		}
		
		public String getItemType() {
			String type = question.getItemType();
			if(type == null) {
				return "";
			}
			return type;
		}
	
		public QuestionItemShort getQuestion() {
			return question;
		}
	}

	private static class QItemDataModel extends DefaultFlexiTableDataModel<QuestionRow> {
		private final Locale locale;
		private final ExportFormatOptions format;
		private final QuestionPoolModule qpoolModule;

		public QItemDataModel(FlexiTableColumnModel columnModel, ExportFormatOptions format, Locale locale) {
			super(columnModel);
			this.locale = locale;
			this.format = format;
			qpoolModule = CoreSpringFactory.getImpl(QuestionPoolModule.class);
		}

		@Override
		public QItemDataModel createCopyWithEmptyList() {
			return new QItemDataModel(getTableColumnModel(), format, locale);
		}

		@Override
		public Object getValueAt(int row, int col) {
			QuestionRow share = getObject(row);
			switch(Cols.values()[col]) {
				case accept:{
					String itemFormat = share.getFormat();
					QPoolSPI itemProvider = qpoolModule.getQuestionPoolProvider(itemFormat);
					if(itemProvider != null && itemProvider.getTestExportFormats().contains(format)) {
						return Boolean.TRUE;
					}
					return Boolean.FALSE;	
				} 
				case title: return share.getTitle();
				case topic: return share.getTopic();
				case taxonomyLevel: return share.getTaxonomyLevelName();
				case taxonomyPath: return share.getTaxonomyPath();
				case format: return share.getFormat();
				case type: return share.getItemType();
				case status: return share.getQuestionStatus();
				case license: return shortenedLicense(share);
				default : return share;
			}
		}
		
		private String shortenedLicense(QuestionRow share) {
			String text = null;
			ResourceLicense license = share.getLicense();
			if(license != null) {
				if(license.getLicenseType() != null) {
					text = LicenseUIFactory.translate(license.getLicenseType(), locale);
				}
				if(StringHelper.containsNonWhitespace(license.getFreetext())) {
					text = license.getFreetext();
				}
			}
			
			if(text != null && text.length() > 32) {
				text = Formatter.truncate(text, 32);
			}
			return text;
		}
	}
	
	private enum Cols implements FlexiSortableColumnDef {
		accept("export.overview.accept"),
		title("general.title"),
		topic("general.topic"),
		taxonomyLevel("classification.taxonomy.level"),
		taxonomyPath("classification.taxonomic.path"),
		format("technical.format"),
		status("lifecycle.status"),
		type("question.type"),
		license("rights.license");
		
		private final String i18nKey;
		
		private Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
