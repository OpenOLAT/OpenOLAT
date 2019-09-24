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
package org.olat.restapi.system;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.olat.admin.sysinfo.manager.ThreadInfosManager;
import org.olat.admin.sysinfo.model.ThreadView;
import org.olat.core.CoreSpringFactory;
import org.olat.restapi.system.vo.ThreadVO;
import org.olat.restapi.system.vo.ThreadVOes;
import org.olat.restapi.system.vo.ThreadsVO;

/**
 * 
 * <h3>Description:</h3>
 * 
 * Initial Date:  21 juin 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class ThreadsWebService implements Sampler {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response getThreads() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		ThreadsVO vo = new ThreadsVO(bean);
		return Response.ok(vo.toString()).build();
	}
	
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getThreadsXml() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		ThreadsVO vo = new ThreadsVO(bean);
		return Response.ok(vo).build();
	}
	
	@GET
	@Path("cpu")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public synchronized Response getThreadsCpu() {
		List<ThreadView> threadViews = CoreSpringFactory.getImpl(ThreadInfosManager.class).getThreadViews();
		List<ThreadVO> threads = new ArrayList<>(threadViews.size());
		for(ThreadView view: threadViews) {
			threads.add(new ThreadVO(view));
		}

		Collections.sort(threads);
		ThreadVO[] threadVos = threads.toArray(new ThreadVO[threads.size()]);
		ThreadVOes voes = new ThreadVOes();
		voes.setThreads(threadVos);
		voes.setTotalCount(threadVos.length);
		return Response.ok(voes).build();
	}
	
  @Override
	public void takeSample() {
  	ThreadInfosManager manager = CoreSpringFactory.getImpl(ThreadInfosManager.class);
		if(manager != null) {//check if the manager is loaded
			manager.takeSample();
		}
	}
}