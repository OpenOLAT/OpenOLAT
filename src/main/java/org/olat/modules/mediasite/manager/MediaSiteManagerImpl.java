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
package org.olat.modules.mediasite.manager;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.olat.core.util.StringHelper;
import org.olat.course.nodes.MediaSiteCourseNode;
import org.olat.course.nodes.mediasite.MediaSiteRunController;
import org.olat.ims.lti13.LTI13Context;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.manager.LTI13ContextDAO;
import org.olat.ims.lti13.manager.LTI13ToolDAO;
import org.olat.ims.lti13.manager.LTI13ToolDeploymentDAO;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.mediasite.MediaSiteManager;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 25.10.2021<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@Service
public class MediaSiteManagerImpl implements MediaSiteManager {

	@Autowired
	private LTI13Service lti13Service;
	
	@Autowired
	private LTI13ContextDAO lti13ContextDao;
	
	@Autowired
	private LTI13ToolDeploymentDAO lti13ToolDeploymentDao;
	
	@Autowired
	private LTI13ToolDAO lti13ToolDao;
	
	@Override
	public String parseAlias(String identifier) {
		if (!StringHelper.containsNonWhitespace(identifier)) {
			return null;
		}
		
		identifier = identifier.replace(" ", "");
		
		try {
			List<String> elements = Arrays.asList(identifier.split("/"));
			
			if (elements.size() == 1) {
				return elements.get(0);
			}
			
			if (elements.contains("presentations")) {
				return elements.get(elements.indexOf("presentations") + 1);
			} else if (elements.contains("Play")) {
				return elements.get(elements.indexOf("Play") + 1);
			} else if (elements.contains("channels")) {
				return elements.get(elements.indexOf("channels") + 1);
			} 
			
			String mediaId = elements.stream().filter(el -> el.startsWith("Launch")).findFirst().get();
			return mediaId.replace("Launch?mediasiteId=", "");
			
		} catch (Exception e) {}
		
		return null;
	}
	
	@Override
	public Long copyLti13MediaSiteConfiguration(RepositoryEntry sourceEntry, RepositoryEntry targetEntry, String subIdent) {
		LTI13Context mainTargetContext = lti13Service.getContext(targetEntry, subIdent);
		if (mainTargetContext != null) return null;
		
		LTI13Context mainSourceContext = lti13Service.getContext(sourceEntry, subIdent);
		if (mainSourceContext == null) return null;

		LTI13ToolDeployment sourceDeployment = mainSourceContext.getDeployment();
		if (sourceDeployment == null) return null;

		LTI13Tool sourceTool = sourceDeployment.getTool();
		if (sourceTool == null) return null;
		
		// Clone the tool
		LTI13Tool clonedTool = lti13Service.createExternalTool(
				sourceTool.getToolName(), sourceTool.getToolUrl(), lti13Service.newClientId(),
				sourceTool.getInitiateLoginUrl(), sourceTool.getRedirectUrl(), sourceTool.getToolTypeEnum());
		clonedTool.setPublicKeyTypeEnum(sourceTool.getPublicKeyTypeEnum());
		if (sourceTool.getPublicKeyTypeEnum() == LTI13Tool.PublicKeyType.URL) {
			clonedTool.setPublicKeyUrl(sourceTool.getPublicKeyUrl());
		} else {
			clonedTool.setPublicKey(sourceTool.getPublicKey());
		}
		clonedTool.setDeepLinking(sourceTool.getDeepLinking());
		clonedTool = lti13Service.updateTool(clonedTool);
		
		// Clone the tool deployment
		LTI13ToolDeployment clonedToolDeployment = lti13Service.createToolDeployment(sourceDeployment.getTargetUrl(),
				sourceDeployment.getDeploymentType(), UUID.randomUUID().toString(), clonedTool);

		// No need to clone the tool contexts. They will be created on the fly in the MediaSiteRunController.
		List<LTI13Context> sourceContexts = lti13Service.getContextsByTool(sourceTool);
		for (LTI13Context sourceContext : sourceContexts) {
			LTI13Context targetContext = lti13Service.createContext(sourceContext.getTargetUrl(), clonedToolDeployment, 
					targetEntry, sourceContext.getSubIdent(), null);
			copyContextSettings(sourceContext, targetContext);
		}
		
		return clonedTool.getKey();
	}

	private void copyContextSettings(LTI13Context contextToCopy, LTI13Context clonedContext) {
		clonedContext.setSendUserAttributesList(contextToCopy.getSendUserAttributesList());
		clonedContext.setSendCustomAttributes(contextToCopy.getSendCustomAttributes());
		clonedContext.setParticipantRoles(contextToCopy.getParticipantRoles());
		clonedContext.setCoachRoles(contextToCopy.getCoachRoles());
		clonedContext.setAuthorRoles(contextToCopy.getAuthorRoles());
		lti13Service.updateContext(clonedContext);
	}

	@Override
	public LTI13Context createLti13Context(String targetUrl, LTI13ToolDeployment deployment, RepositoryEntry courseEntry, 
										   String subIdent, MediaSiteRunController mediaSiteRunController) {
		LTI13Context context = lti13Service.createContext(targetUrl, deployment, courseEntry, subIdent, null);
		context.setSendUserAttributesList(List.of("email", "firstName", "lastName"));
		context.setSendCustomAttributes("custom_id=$userprops_username");
		context.setParticipantRoles("Learner");
		context.setCoachRoles("Instructor,Mentor");
		context.setAuthorRoles("ContentDeveloper,Instructor,Mentor");
		return lti13Service.updateContext(context);
	}

	@Override
	public void deleteLti13MediaSiteConfiguration(RepositoryEntry entry, String ident, Long toolKey) {
		LTI13Tool tool = lti13ToolDao.loadToolByKey(toolKey);
		if (tool == null) return;

		List<LTI13Context> contexts = lti13ContextDao.loadContexts(tool);
		if (contexts.isEmpty()) return;

		LTI13ToolDeployment deployment = null;
		for (LTI13Context context : contexts) {
			deployment = context.getDeployment();
			lti13ContextDao.deleteContext(context);
		}
		if (deployment != null) {
			lti13ToolDeploymentDao.deleteToolDeployment(deployment);
		}

		lti13ToolDao.deleteTool(tool);
	}

	@Override
	public LTI13ToolDeployment resolveCourseDeployment(ModuleConfiguration config, LTI13Tool tool) {
		String deploymentKeyStr = config.getStringValue(MediaSiteCourseNode.CONFIG_LTI13_DEPLOYMENT_KEY);
		if (StringHelper.isLong(deploymentKeyStr)) {
			LTI13ToolDeployment deployment = lti13Service.getToolDeploymentByKey(Long.valueOf(deploymentKeyStr));
			if (deployment != null) {
				return deployment;
			}
		}
		// Backward compatibility: courses configured before the deployment key was stored explicitly.
		List<LTI13ToolDeployment> deployments = lti13Service.getToolDeploymentByTool(tool);
		return deployments.isEmpty() ? null : deployments.get(0);
	}
}
