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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.commons.services.license.ui.LicenseRenderer;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CSSIconFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemSecurityCallback;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionItemView.OrderBy;
import org.olat.modules.qpool.manager.QuestionPoolLicenseHandler;
import org.olat.modules.qpool.model.ItemWrapper;
import org.olat.modules.qpool.security.QPoolSecurityCallbackFactory;
import org.olat.modules.qpool.ui.QuestionItemDataModel.Cols;
import org.olat.modules.qpool.ui.events.QItemMarkedEvent;
import org.olat.modules.qpool.ui.events.QItemViewEvent;
import org.olat.modules.qpool.ui.metadata.ExtendedSearchController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public abstract class AbstractItemListController extends FormBasicController
	implements GenericEventListener, FlexiTableDataSourceDelegate<ItemRow>, FlexiTableComponentDelegate {

	public static final String CSS_ICON_READONLY = "o_icon_readonly";
	public static final String CSS_ICON_READWRITE = "o_icon_readwrite";

	private FlexiTableElement itemsTable;
	private QuestionItemDataModel model;
	private final QPoolSecurityCallback securityCallback;
	
	private final String prefsKey;
	protected final String restrictToFormat;
	private ExtendedSearchController extendedSearchCtrl;
	private QuestionItemPreviewController previewCtrl;
	private QuickViewMetadataController quickViewMetadataCtrl;
	
	@Autowired
	private MarkManager markManager;
	@Autowired
	protected QPoolService qpoolService;
	@Autowired
	private QPoolSecurityCallbackFactory qpoolSecurityCallbackFactory;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private QuestionPoolLicenseHandler licenseHandler;
	
	private EventBus eventBus;
	private QuestionItemsSource itemsSource;
	private final Roles roles;
	
	public AbstractItemListController(UserRequest ureq, WindowControl wControl, QPoolSecurityCallback securityCallback,
			QuestionItemsSource source, String key, boolean searchAllTaxonomyLevels) {
		this(ureq, wControl, securityCallback, source, null, key, searchAllTaxonomyLevels);
	}
	
	public AbstractItemListController(UserRequest ureq, WindowControl wControl, QPoolSecurityCallback securityCallback,
			QuestionItemsSource source, String key) {
		this(ureq, wControl, securityCallback, source, null, key, false);
	}
	
	public AbstractItemListController(UserRequest ureq, WindowControl wControl, QPoolSecurityCallback securityCallback,
			QuestionItemsSource source, String restrictToFormat, String key) {
		this(ureq, wControl, securityCallback, source, restrictToFormat, key, false);
	}
	
	public AbstractItemListController(UserRequest ureq, WindowControl wControl, QPoolSecurityCallback securityCallback,
			QuestionItemsSource source, String restrictToFormat, String key, boolean searchAllTaxonomyLevels) {
		super(ureq, wControl, "item_list");

		this.securityCallback = securityCallback;
		this.prefsKey = key;
		this.itemsSource = source;
		this.roles = ureq.getUserSession().getRoles();
		this.restrictToFormat = restrictToFormat;

		eventBus = ureq.getUserSession().getSingleUserEventCenter();
		eventBus.registerFor(this, getIdentity(), QuestionPoolMainEditorController.QITEM_MARKED);
		
		extendedSearchCtrl = new ExtendedSearchController(ureq, getWindowControl(), getSecurityCallback(), key,
				mainForm, searchAllTaxonomyLevels);
		extendedSearchCtrl.setEnabled(false);
		
		initForm(ureq);
		
		previewCtrl = new QuestionItemPreviewController(ureq, getWindowControl());
		quickViewMetadataCtrl = new QuickViewMetadataController(ureq, wControl, securityCallback);
	}

	@Override
	protected void doDispose() {
		eventBus.deregisterFor(this, QuestionPoolMainEditorController.QITEM_MARKED);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("quickview", "<i class='o_icon o_icon_quickview'> </i>", "quick-view"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.mark.i18nKey(), Cols.mark.ordinal(), true, OrderBy.marks.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.editable.i18nKey(), Cols.editable.ordinal(),
				false, null, FlexiColumnModel.ALIGNMENT_LEFT,
				new BooleanCellRenderer(
						new CSSIconFlexiCellRenderer(CSS_ICON_READWRITE),
						new CSSIconFlexiCellRenderer(CSS_ICON_READONLY))
		));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.key.i18nKey(), Cols.key.ordinal(), true, OrderBy.key.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.identifier.i18nKey(), Cols.identifier.ordinal(), true, OrderBy.identifier.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.title.i18nKey(), Cols.title.ordinal(), true, OrderBy.title.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.topic.i18nKey(), Cols.topic.ordinal(), true, OrderBy.topic.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.creationDate.i18nKey(), Cols.creationDate.ordinal(), true, OrderBy.creationDate.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lastModified.i18nKey(), Cols.lastModified.ordinal(), true, OrderBy.lastModified.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.keywords.i18nKey(), Cols.keywords.ordinal(), true, OrderBy.keywords.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.coverage.i18nKey(), Cols.coverage.ordinal(), true, OrderBy.coverage.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.additionalInfos.i18nKey(), Cols.additionalInfos.ordinal(), true,  OrderBy.additionalInformations.name()));
		if (getSecurityCallback().canUseTaxonomy()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.taxnonomyLevel.i18nKey(), Cols.taxnonomyLevel.ordinal(), true, OrderBy.taxonomyLevel.name()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.taxnonomyPath.i18nKey(), Cols.taxnonomyPath.ordinal(), true, OrderBy.taxonomyPath.name()));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.difficulty.i18nKey(), Cols.difficulty.ordinal(), true, OrderBy.difficulty.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.stdevDifficulty.i18nKey(), Cols.stdevDifficulty.ordinal(), true, OrderBy.stdevDifficulty.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.differentiation.i18nKey(), Cols.differentiation.ordinal(), true, OrderBy.differentiation.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.numOfAnswerAlternatives.i18nKey(), Cols.numOfAnswerAlternatives.ordinal(), true, OrderBy.numOfAnswerAlternatives.name()));
		// Disabled as a quick fix for LMSUZH-671
		//columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.usage.i18nKey(), Cols.usage.ordinal(), true, OrderBy.usage.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.type.i18nKey(), Cols.type.ordinal(), true, OrderBy.itemType.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.format.i18nKey(), Cols.format.ordinal(), true, OrderBy.format.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.rating.i18nKey(), Cols.rating.ordinal(), true, OrderBy.rating.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.numberOfRatings.i18nKey(), Cols.numberOfRatings.ordinal(), true, OrderBy.numberOfRatings.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.itemVersion.i18nKey(), Cols.itemVersion.ordinal(), true, OrderBy.itemVersion.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.status.i18nKey(), Cols.status.ordinal(), true, OrderBy.status.name(), new QuestionStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.statusLastModified.i18nKey(), Cols.statusLastModified.ordinal(), true, OrderBy.statusLastModified.name()));	
		if (licenseModule.isEnabled(licenseHandler)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, false, Cols.license.i18nKey(), null, Cols.license.ordinal(), "license", false, null, FlexiColumnModel.ALIGNMENT_LEFT,
					 new StaticFlexiCellRenderer("license", new LicenseRenderer(getLocale()))));
		}
		initActionColumns(columnsModel);
		
		model = new QuestionItemDataModel(columnsModel, this, getTranslator());
		itemsTable = uifactory.addTableElement(getWindowControl(), "items", model, 50, false, getTranslator(), formLayout);
		itemsTable.setWrapperSelector("qitems");
		itemsTable.setSelectAllEnable(true);
		itemsTable.setMultiSelect(true);
		itemsTable.setSearchEnabled(true);
		itemsTable.setSortSettings(new FlexiTableSortOptions(true));
		itemsTable.setExtendedSearch(extendedSearchCtrl);
		itemsTable.setColumnIndexForDragAndDropLabel(Cols.title.ordinal());
		itemsTable.setAndLoadPersistedPreferences(ureq, "qpool-list-" + prefsKey);
		listenTo(extendedSearchCtrl);
		
		VelocityContainer detailsVC = createVelocityContainer("item_list_details");
		itemsTable.setDetailsRenderer(detailsVC, this);
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(OrderBy.title.name(), true));
		itemsTable.setSortSettings(sortOptions);
		initButtons(ureq, formLayout);
		
		itemsTable.reloadData();
	}
	
	protected void initActionColumns(FlexiTableColumnModel columnsModel) {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("details", translate("details"), "select-item"));
	}
	
	protected abstract void initButtons(UserRequest ureq, FormItemContainer formLayout);

    @Override
	public Iterable<Component> getComponents(int rowIndex, Object rowObject) {
    		List<Component> components = new ArrayList<>(2);
		components.add(previewCtrl.getInitialComponent());
    		components.add(quickViewMetadataCtrl.getInitialComponent());
		return components;
	}

	protected void setSource(QuestionItemsSource source) {
        this.itemsSource = source;
    }
	
	protected FlexiTableElement getItemsTable() {
		return itemsTable;
	}
	
	protected QuestionItemDataModel getModel() {
		return model;
	}
	
	protected QPoolSecurityCallback getSecurityCallback() {
		return securityCallback;
	}
	
	protected String getTableFormDispatchId() {
		return itemsTable == null ? null : itemsTable.getFormDispatchId();
	}

	public void reset() {
		itemsTable.reset();
	}
	
	public void reloadData() {
		itemsTable.reloadData();
	}
	
	public QuestionItemsSource getSource() {
		return itemsSource;
	}
	
	public void updateSource(QuestionItemsSource source) {
		this.itemsSource = source;
		model.clear();
		itemsTable.reset();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(extendedSearchCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				String quickSearch = itemsTable.getQuickSearchString();
				if(StringHelper.containsNonWhitespace(quickSearch)) {
					itemsTable.quickSearch(ureq, quickSearch);
				} else {
					itemsTable.resetSearch(ureq);
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == itemsTable) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("rSelect".equals(se.getCommand())) {
					ItemRow row = model.getObject(se.getIndex());
					if(row != null) {
						doClick(ureq, row);
					}
				} else if("select-item".equals(se.getCommand())) {
					ItemRow row = getModel().getObject(se.getIndex());
					if(row != null) {
						doSelect(ureq, row);
					}
				} else if("quick-view".equals(se.getCommand())) {
					int rowIndex = se.getIndex();
					if(rowIndex >= 0) {
						if(itemsTable.isDetailsExpended(rowIndex)) {
							itemsTable.collapseDetails(rowIndex);
						} else {
							itemsTable.collapseAllDetails();
							ItemRow row = getModel().getObject(rowIndex);
							if(row != null) {
								itemsTable.expandDetails(rowIndex);
					    			QuestionItem item = qpoolService.loadItemById(row.getKey());
					    			previewCtrl.updateItem(ureq, item);
					    			quickViewMetadataCtrl.setItem(ureq, item);
							}
						}
					}
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("select".equals(link.getCmd())) {
				ItemRow row = (ItemRow)link.getUserObject();
				doSelect(ureq, row);
			} else if("mark".equals(link.getCmd())) {
				ItemRow row = (ItemRow)link.getUserObject();
				if(doMark(row)) {
					link.setIconLeftCSS(Mark.MARK_CSS_LARGE);
				} else {
					link.setIconLeftCSS(Mark.MARK_ADD_CSS_LARGE);
				}
				link.getComponent().setDirty(true);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//do nothing
	}

	@Override
	public void event(Event event) {
		if(event instanceof QItemMarkedEvent) {
			QItemMarkedEvent qime = (QItemMarkedEvent)event;
			ItemRow row = getRowByItemKey(qime.getKey());
			if(row != null) {
				row.setMark(qime.isMark());
			}
		}
	}

	public List<QuestionItemShort> getSelectedShortItems() {
		return getItemsTable().getMultiSelectedIndex().stream()
				.filter(index -> getModel().ensureLoaded(index.intValue(), itemsTable))
				.map(index -> getModel().getObject(index.intValue()))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}
	
	public List<QuestionItemView> getItemViews(Set<Integer> index) {
		List<QuestionItemView> items = new ArrayList<>();
		for(Integer i:index) {
			ItemRow row = model.getObject(i.intValue());
			if(row != null) {
				items.add(row);
			}
		}
		return items;
	}

	public QuestionItemShort getQuestionItemAt(int index) {
		ItemRow row = model.getObject(index);
		if(row != null) {
			return qpoolService.loadItemById(row.getKey());
		}
		return null;
	}
	
	public ItemRow getRowByItemKey(Long itemKey) {
		List<ItemRow> rows = model.getObjects();
		for(ItemRow row : rows) {
			if(row != null && row.getKey().equals(itemKey)) {
				return row;
			}
		}
		return null;
	}
	
	public List<Integer> getIndex(Collection<QuestionItem> items) {
		Set<Long> itemKeys = new HashSet<>();
		for(QuestionItem item:items) {
			itemKeys.add(item.getKey());
		}

		List<Integer> index = new ArrayList<>(items.size());
		for(int i=model.getObjects().size(); i-->0; ) {
			ItemRow row = model.getObject(i);
			if(row != null && itemKeys.contains(row.getKey())) {
				index.add(i);
			}
		}
		return index;
	}
	
	public Integer getIndex(Long itemKey) {
		for(int i=model.getObjects().size(); i-->0; ) {
			ItemRow row = model.getObject(i);
			if(row != null && itemKey.equals(row.getKey())) {
				return i;
			}
		}
		return null;
	}
	
	protected void doClick(UserRequest ureq, ItemRow row) {
		fireEvent(ureq, new QItemViewEvent("rSelect", row));
	}
	
	protected abstract void doSelect(UserRequest ureq, ItemRow row);
	
	protected boolean doMark(OLATResourceable item) {
		if(markManager.isMarked(item, getIdentity(), null)) {
			markManager.removeMark(item, getIdentity(), null);
			return false;
		} else {
			String businessPath = "[QuestionItem:" + item.getResourceableId() + "]";
			markManager.setMark(item, getIdentity(), null, businessPath);
			return true;
		}
	}

	@Override
	public int getRowCount() {
		return itemsSource.getNumOfItems();
	}

	@Override
	public List<ItemRow> reload(List<ItemRow> rows) {
		List<Long> itemToReload = new ArrayList<>();
		for(ItemRow row:rows) {
			itemToReload.add(row.getKey());
		}

		List<QuestionItemView> reloadedItems = itemsSource.getItems(itemToReload);
		List<ItemRow> reloadedRows = new ArrayList<>(reloadedItems.size());
		List<ResourceLicense> licenses = licenseService.loadLicenses(reloadedItems);
		for(QuestionItemView item:reloadedItems) {
			ItemRow reloadedRow = forgeRow(item, licenses);
			reloadedRows.add(reloadedRow);
		}
		return reloadedRows;
	}

	@Override
	public ResultInfos<ItemRow> getRows(String query, List<FlexiTableFilter> filters, List<String> condQueries, int firstResult, int maxResults, SortKey... orderBy) {
		ResultInfos<QuestionItemView> items = itemsSource.getItems(query, condQueries, firstResult, maxResults, orderBy);
		List<ItemRow> rows = new ArrayList<>(items.getObjects().size());
		List<ResourceLicense> licenses = licenseService.loadLicenses(items.getObjects());
		for(QuestionItemView item:items.getObjects()) {
			ItemRow row = forgeRow(item, licenses);
			rows.add(row);
		}
		return new DefaultResultInfos<>(items.getNextFirstResult(), items.getCorrectedRowCount(), rows);
	}
	
	protected ItemRow forgeRow(QuestionItemView item, List<ResourceLicense> licenses) {
		boolean marked = item.isMarked();
		QuestionItemSecurityCallback securityCallback = qpoolSecurityCallbackFactory
				.createQuestionItemSecurityCallback(item, getSource(), roles);
		ItemRow row = new ItemRow(item, securityCallback);
		
		// favorite
		FormLink markLink = uifactory.addFormLink("mark_" + row.getKey(), "mark", "&nbsp;", null, null, Link.NONTRANSLATED);
		markLink.setIconLeftCSS(marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
		markLink.setUserObject(row);
		row.setMarkLink(markLink);
		
		// license
		for (ResourceLicense license: licenses) {
			if (license.getResId().equals(item.getResourceableId()) && license.getResName().equals(item.getResourceableTypeName())) {
				row.setLicense(license);
			}
		}
		return row;
	}


	protected ItemRow wrapNewItem(QuestionItem item) {
		ItemWrapper itemWrapper = ItemWrapper.builder(item).setAuthor(true).create();
		QuestionItemSecurityCallback securityCallback = qpoolSecurityCallbackFactory
				.createQuestionItemSecurityCallback(itemWrapper, getSource(), roles);
		return new ItemRow(itemWrapper, securityCallback);
	}
}
