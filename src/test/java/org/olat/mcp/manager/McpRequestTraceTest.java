/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.mcp.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.olat.mcp.tool.McpToolScope;

/**
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpRequestTraceTest {

	@Test
	public void listAndInitializeAreReads() {
		McpRequestTrace trace = new McpRequestTrace();
		trace.setJsonRpcMethod("tools/list");
		Assertions.assertThat(trace.isReadOperation()).isTrue();
		Assertions.assertThat(trace.getAuditStatus()).isEqualTo(200);
	}

	@Test
	public void readToolSuccessIsRead() {
		McpRequestTrace trace = new McpRequestTrace();
		trace.toolCall("course_query", McpToolScope.READ, Map.of("action", "list"));
		Assertions.assertThat(trace.isReadOperation()).isTrue();
		Assertions.assertThat(trace.getAction()).isEqualTo("list");
		Assertions.assertThat(trace.getAuditStatus()).isEqualTo(200);
	}

	@Test
	public void writeToolIsNeverRead() {
		McpRequestTrace trace = new McpRequestTrace();
		trace.toolCall("todo_modify", McpToolScope.WRITE, Map.of("action", "create"));
		Assertions.assertThat(trace.isReadOperation()).isFalse();
	}

	@Test
	public void failedReadIsNotRead() {
		McpRequestTrace trace = new McpRequestTrace();
		trace.toolCall("course_query", McpToolScope.READ, Map.of());
		trace.setOutcome(McpRequestTrace.Outcome.toolError);
		Assertions.assertThat(trace.isReadOperation()).isFalse();
		Assertions.assertThat(trace.getAuditStatus()).isEqualTo(422);

		trace.setOutcome(McpRequestTrace.Outcome.denied);
		Assertions.assertThat(trace.getAuditStatus()).isEqualTo(403);
		trace.setOutcome(McpRequestTrace.Outcome.serverError);
		Assertions.assertThat(trace.getAuditStatus()).isEqualTo(500);
		Assertions.assertThat(trace.getAction()).isNull();
	}

	@Test
	public void longStringArgumentsAreCutInTheTrace() {
		String content = "a".repeat(5000);
		Map<String,Object> args = new HashMap<>();
		args.put("action", "create");
		args.put("content", content);
		args.put("short", "b".repeat(McpRequestTrace.MAX_ARGUMENT_LENGTH));
		args.put("size", Integer.valueOf(5000));

		McpRequestTrace trace = new McpRequestTrace();
		trace.toolCall("file_modify", McpToolScope.WRITE, args);

		String traced = (String)trace.getArguments().get("content");
		Assertions.assertThat(traced)
			.startsWith("a".repeat(McpRequestTrace.MAX_ARGUMENT_LENGTH))
			.endsWith(" \u2026 [5000 chars]")
			.hasSize(McpRequestTrace.MAX_ARGUMENT_LENGTH + " \u2026 [5000 chars]".length());
		Assertions.assertThat(trace.getArguments().get("short")).isEqualTo("b".repeat(McpRequestTrace.MAX_ARGUMENT_LENGTH));
		Assertions.assertThat(trace.getArguments().get("size")).isEqualTo(Integer.valueOf(5000));
		Assertions.assertThat(trace.getAction()).isEqualTo("create");
		// the map of the tool is untouched
		Assertions.assertThat(args.get("content")).isSameAs(content);
		Assertions.assertThat(trace.getArguments()).isNotSameAs(args);
	}

	@Test
	public void nestedStructuresAreCutInTheTrace() {
		String longValue = "x".repeat(3000);
		Map<String,Object> nested = Map.of("data", longValue, "name", "file.txt");
		List<Object> list = List.of(longValue, nested, Integer.valueOf(1));
		Map<String,Object> args = Map.of("action", "create", "files", list, "meta", nested);

		McpRequestTrace trace = new McpRequestTrace();
		trace.toolCall("file_modify", McpToolScope.WRITE, args);

		String cut = "x".repeat(McpRequestTrace.MAX_ARGUMENT_LENGTH) + " \u2026 [3000 chars]";
		Map<?,?> meta = (Map<?,?>)trace.getArguments().get("meta");
		Assertions.assertThat(meta.get("data")).isEqualTo(cut);
		Assertions.assertThat(meta.get("name")).isEqualTo("file.txt");
		List<?> files = (List<?>)trace.getArguments().get("files");
		Assertions.assertThat(files).hasSize(3);
		Assertions.assertThat(files.get(0)).isEqualTo(cut);
		Assertions.assertThat(((Map<?,?>)files.get(1)).get("data")).isEqualTo(cut);
		Assertions.assertThat(files.get(2)).isEqualTo(Integer.valueOf(1));
		// the original structures are untouched
		Assertions.assertThat(nested.get("data")).isSameAs(longValue);
		Assertions.assertThat(list.get(0)).isSameAs(longValue);
	}

	@Test
	public void cutDoesNotSplitASurrogatePair() {
		String value = "a".repeat(McpRequestTrace.MAX_ARGUMENT_LENGTH - 1) + "\uD83D\uDE00" + "b".repeat(10);
		String cut = McpRequestTrace.boundedString(value);
		Assertions.assertThat(cut).startsWith("a".repeat(McpRequestTrace.MAX_ARGUMENT_LENGTH - 1) + " \u2026");
	}

	@Test
	public void nullArguments() {
		McpRequestTrace trace = new McpRequestTrace();
		trace.toolCall("whoami", McpToolScope.READ, null);
		Assertions.assertThat(trace.getArguments()).isNull();
		Assertions.assertThat(trace.getAction()).isNull();
	}
}
