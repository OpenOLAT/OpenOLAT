/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.components;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

import org.olat.modules.selectus.model.PositionAttributeDefinitionTypeEnum;
import org.olat.modules.selectus.model.attributes.SelectConfiguration;
import org.olat.modules.selectus.model.attributes.SeparatorConfiguration;
import org.olat.modules.selectus.model.attributes.TextConfiguration;
import org.olat.modules.selectus.ui.position.model.PositionAdditionalAttributeRow;

/**
 * 
 * Initial date: 12 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionAttributeDefinitionTypeCellRenderer implements FlexiCellRenderer {

	private final Translator translator;
	
	public PositionAttributeDefinitionTypeCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu, Translator transl) {
		if(cellValue instanceof PositionAttributeDefinitionTypeEnum) {
			PositionAttributeDefinitionTypeEnum type = (PositionAttributeDefinitionTypeEnum)cellValue;
			PositionAdditionalAttributeRow attrRow = (PositionAdditionalAttributeRow)source.getFormItem().getTableDataModel().getObject(row);
			if(type == PositionAttributeDefinitionTypeEnum.select) {
				SelectConfiguration config = attrRow.getAttributeDefinition().getConfiguration(SelectConfiguration.class);
				if(config != null && config.isMultiple()) {
					target.append(translator.translate("select.multi.type"));
				} else {
					target.append(translator.translate("select.single.type"));
				}
			} else if(type == PositionAttributeDefinitionTypeEnum.separator) {
				SeparatorConfiguration config = attrRow.getAttributeDefinition().getConfiguration(SeparatorConfiguration.class);
				if(config.isWithLine()) {
					target.append(translator.translate("separator.withline"));
				} else {
					target.append(translator.translate("separator.withoutline"));
				}
			} else if(type == PositionAttributeDefinitionTypeEnum.question) {
				TextConfiguration config = attrRow.getAttributeDefinition().getConfiguration(TextConfiguration.class);
				if(config != null && config.isMultiLine()) {
					target.append(translator.translate("custom.attribute.question.multi"));
				} else {
					target.append(translator.translate("custom.attribute.question"));
				}
			} else {
				target.append(translator.translate(type.i18nKey()));
			}
		}	
	}
}
