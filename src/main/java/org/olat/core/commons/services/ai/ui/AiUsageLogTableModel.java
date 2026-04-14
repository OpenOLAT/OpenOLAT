/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.ai.ui;

import org.olat.core.commons.services.ai.AiUsageLog;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataSourceModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
/**
 * Initial date: 07.04.2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class AiUsageLogTableModel extends DefaultFlexiTableDataSourceModel<AiUsageLog> {

	private static final AiUsageLogCols[] COLS = AiUsageLogCols.values();

	public AiUsageLogTableModel(FlexiTableDataSourceDelegate<AiUsageLog> dataSource, FlexiTableColumnModel columnModel) {
		super(dataSource, columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		AiUsageLog log = getObject(row);
		return switch (COLS[col]) {
			case creationDate -> log.getCreationDate();
			case aiFeature -> log.getAiFeature();
			case usageContextType -> log.getUsageContextType();
			case usageContextId -> log.getUsageContextId();
			case identity -> log.getIdentity();
			case resourceType -> log.getResourceType();
			case resourceId -> log.getResourceId();
			case resourceSubId -> log.getResourceSubId();
			case locale -> log.getLocale();
			case durationMillis -> log.getDurationMillis();
			case status -> log.getStatus();
			case errorCode -> log.getErrorCode();
			case errorMessage -> log.getErrorMessage();
			case modelProvider -> log.getModelProvider();
			case requestModel -> log.getRequestModel();
			case requestTemperature -> log.getRequestTemperature();
			case requestTopP -> log.getRequestTopP();
			case requestMaxOutputTokens -> log.getRequestMaxOutputTokens();
			case invocationId -> log.getInvocationId();
			case serviceInterface -> log.getServiceInterface();
			case serviceMethod -> log.getServiceMethod();
			case responseId -> log.getResponseId();
			case responseModel -> log.getResponseModel();
			case responseFinishReason -> log.getResponseFinishReason();
			case inputTokens -> log.getInputTokens();
			case outputTokens -> log.getOutputTokens();
			case totalTokens -> log.getTotalTokens();
			case cachedInputTokens -> log.getCachedInputTokens();
			case reasoningTokens -> log.getReasoningTokens();
			case requestNumMessages -> log.getRequestNumMessages();
			case requestTextLength -> log.getRequestTextLength();
			case cacheCreationInputTokens -> log.getCacheCreationInputTokens();
		};
	}


	@Override
	public AiUsageLogTableModel createCopyWithEmptyList() {
		return new AiUsageLogTableModel(getSourceDelegate(), getTableColumnModel());
	}

	public enum AiUsageLogCols implements FlexiSortableColumnDef {
		creationDate("usagelog.col.creationDate", "creationDate"),
		aiFeature("usagelog.col.aiFeature", "aiFeature"),
		usageContextType("usagelog.col.usageContextType", "usageContextType"),
		usageContextId("usagelog.col.usageContextId", "usageContextId"),
		identity("usagelog.col.identity", null),
		resourceType("usagelog.col.resourceType", "resourceType"),
		resourceId("usagelog.col.resourceId", "resourceId"),
		resourceSubId("usagelog.col.resourceSubId", "resourceSubId"),
		locale("usagelog.col.locale", "locale"),
		durationMillis("usagelog.col.durationMillis", "durationMillis"),
		status("usagelog.col.status", "status"),
		errorCode("usagelog.col.errorCode", "errorCode"),
		errorMessage("usagelog.col.errorMessage", "errorMessage"),
		modelProvider("usagelog.col.modelProvider", "modelProvider"),
		requestModel("usagelog.col.requestModel", "requestModel"),
		requestTemperature("usagelog.col.requestTemperature", "requestTemperature"),
		requestTopP("usagelog.col.requestTopP", "requestTopP"),
		requestMaxOutputTokens("usagelog.col.requestMaxOutputTokens", "requestMaxOutputTokens"),
		invocationId("usagelog.col.invocationId", "invocationId"),
		serviceInterface("usagelog.col.serviceInterface", "serviceInterface"),
		serviceMethod("usagelog.col.serviceMethod", "serviceMethod"),
		responseId("usagelog.col.responseId", "responseId"),
		responseModel("usagelog.col.responseModel", "responseModel"),
		responseFinishReason("usagelog.col.responseFinishReason", "responseFinishReason"),
		inputTokens("usagelog.col.inputTokens", "inputTokens"),
		outputTokens("usagelog.col.outputTokens", "outputTokens"),
		totalTokens("usagelog.col.totalTokens", "totalTokens"),
		cachedInputTokens("usagelog.col.cachedInputTokens", "cachedInputTokens"),
		reasoningTokens("usagelog.col.reasoningTokens", "reasoningTokens"),
		requestNumMessages("usagelog.col.requestNumMessages", "requestNumMessages"),
		requestTextLength("usagelog.col.requestTextLength", "requestTextLength"),
		cacheCreationInputTokens("usagelog.col.cacheCreationInputTokens", "cacheCreationInputTokens");

		private final String i18nKey;
		private final String sortKey;

		AiUsageLogCols(String i18nKey, String sortKey) {
			this.i18nKey = i18nKey;
			this.sortKey = sortKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return sortKey != null;
		}

		@Override
		public String sortKey() {
			return sortKey;
		}
	}
}
