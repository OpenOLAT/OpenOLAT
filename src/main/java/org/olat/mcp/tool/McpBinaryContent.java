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
package org.olat.mcp.tool;

/**
 * A binary part of a tool result: the registry sends an image as image
 * content and every other type as an embedded resource with a base64 blob.
 *
 * @param uri The resource URI the client sees, e.g. openolat://project/3/file/7
 * @param mimeType The MIME type, never null
 * @param data The bytes
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public record McpBinaryContent(String uri, String mimeType, byte[] data) {

	/** The image types a chat client renders inline */
	public static boolean isInlineImage(String mimeType) {
		return "image/png".equals(mimeType) || "image/jpeg".equals(mimeType)
				|| "image/gif".equals(mimeType) || "image/webp".equals(mimeType);
	}
}
