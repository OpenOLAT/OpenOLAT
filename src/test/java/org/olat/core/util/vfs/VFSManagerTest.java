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
*/
package org.olat.core.util.vfs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.olat.core.commons.services.vfs.VFSRepositoryService;

/**
 * 
 * <P>
 * Initial Date:  16.06.2009 <br>
 * @author patrickb
 */
public class VFSManagerTest {

	/**
	 * Test method for {@link org.olat.core.util.vfs.VFSManager#sanitizePath(java.lang.String)}.
	 */
	@Test 
	public void testSanitizePath() {
		/*
		 * Make sure we always have a path that starts with a "/".
		 */
		String path1 = null;// -> "/"
		String path1_expected = "/";
		assertTrue(VFSManager.sanitizePath(path1).equals(path1_expected));
		
		String path2 = "";// -> "/"
		String path2_expected = "/";
		assertTrue(VFSManager.sanitizePath(path2).equals(path2_expected));
		
		String path3 = "/";// -> "/"
		String path3_expected = "/";
		assertTrue(VFSManager.sanitizePath(path3).equals(path3_expected));
		
		String path4 = ".";// -> "/."
		String path4_expected = "/.";
		assertTrue(VFSManager.sanitizePath(path4).equals(path4_expected));
		
		String path5 = "./";// -> "/."
		String path5_expected = "/.";
		assertTrue(VFSManager.sanitizePath(path5).equals(path5_expected));
		
		String path6 = "cutTrailingSlash/";// -> "/cutTrailingSlash"
		String path6_expected = "/cutTrailingSlash";
		assertTrue(VFSManager.sanitizePath(path6).equals(path6_expected));
	}

	/**
	 * OO-9904: the size recorded in the metadata must be the size of the file
	 * after the output stream is closed.
	 */
	@Test
	public void copyContentRecordsSizeAfterClose() {
		SizeOnCloseLeaf target = new SizeOnCloseLeaf();
		AtomicLong recordedSize = new AtomicLong(-1l);
		VFSRepositoryService vfsRepositoryService = recordingService(recordedSize);

		InputStream in = new ByteArrayInputStream(new byte[1024]);
		boolean successful = VFSManager.copyContent(in, target.leaf(), null, vfsRepositoryService);

		assertTrue(successful);
		assertEquals(1024l, target.size.get());
		assertEquals(target.size.get(), recordedSize.get());
	}

	/**
	 * OO-9904: after a copy that fails midway, the metadata records the size
	 * of the partial file.
	 */
	@Test
	public void copyContentRecordsSizeAfterFailure() {
		SizeOnCloseLeaf target = new SizeOnCloseLeaf();
		AtomicLong recordedSize = new AtomicLong(-1l);
		VFSRepositoryService vfsRepositoryService = recordingService(recordedSize);

		InputStream in = new FailingInputStream(512);
		boolean successful = VFSManager.copyContent(in, target.leaf(), null, vfsRepositoryService);

		assertFalse(successful);
		assertEquals(512l, target.size.get());
		assertEquals(target.size.get(), recordedSize.get());
	}

	private static VFSRepositoryService recordingService(AtomicLong recordedSize) {
		VFSRepositoryService vfsRepositoryService = mock(VFSRepositoryService.class);
		doAnswer(invocation -> {
			VFSLeaf leaf = invocation.getArgument(0);
			recordedSize.set(leaf.getSize());
			return null;
		}).when(vfsRepositoryService).itemSaved(any(VFSLeaf.class), any());
		return vfsRepositoryService;
	}

	/**
	 * A leaf whose size changes only when its output stream is closed,
	 * as a file behind a buffer that is not flushed yet.
	 */
	private static class SizeOnCloseLeaf {

		private final AtomicLong size = new AtomicLong(0l);

		VFSLeaf leaf() {
			VFSLeaf leaf = mock(VFSLeaf.class);
			when(leaf.getName()).thenReturn("oo9904.txt");
			when(leaf.getSize()).thenAnswer(invocation -> size.get());
			when(leaf.getOutputStream(anyBoolean())).thenAnswer(invocation -> new OutputStream() {
				private long written = 0l;

				@Override
				public void write(int b) {
					written++;
				}

				@Override
				public void close() {
					size.set(written);
				}
			});
			return leaf;
		}
	}

	/**
	 * Delivers the given number of bytes, then throws an IOException.
	 */
	private static class FailingInputStream extends InputStream {

		private int remaining;

		FailingInputStream(int length) {
			this.remaining = length;
		}

		@Override
		public int read() throws IOException {
			if (remaining <= 0) {
				throw new IOException("Simulated read error");
			}
			remaining--;
			return 0;
		}
	}
}
