/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.knowprocess.resource.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;

import com.knowprocess.resource.spi.Resource;

public class DeployedResource implements Resource {

	private RepositoryService repositoryService;

	public DeployedResource(RepositoryService repositoryService,
			String resourceUrl) {
		if (repositoryService == null) {
			throw new IllegalArgumentException(
					"Repository service may not be null");
		}
		this.repositoryService = repositoryService;
	}

	@Override
	public InputStream getResource(String uri) throws IOException {
		int start = uri.indexOf("://") + 3;
		int end = uri.indexOf("/", start);
		String deploymentName = uri.substring(start, end);

		String resource = uri.substring(end + 1);
		Deployment deployment = repositoryService.createDeploymentQuery()
				.processDefinitionKeyLike(deploymentName)
				.orderByDeploymenTime().desc().list().get(0);
		
		try {
			System.out
					.println(String
							.format("Seeking resource: %1$s for deployment id: %2$s using: %3$s",
					resource, deployment.getId(), repositoryService));
			InputStream is = repositoryService.getResourceAsStream(
					deployment.getId(),
					resource);
			// There is inconsistent behaviour here, sometimes the stream can be
			// null yet not throw an NPE so we must check manually.
			if (is == null) {
				throw new NullPointerException();
			} else {
				return is;
			}
		} catch (Throwable e) {
			try {
				System.out.println(e.getClass().getName() + ":"
						+ e.getMessage() + ", attempt recovery, looking for: "
						+ resource.substring(resource.lastIndexOf('/') + 1));
				return repositoryService.getResourceAsStream(
						deployment.getId(),
						resource.substring(resource.lastIndexOf('/') + 1));
			} catch (Throwable t) {
				System.err.println("exception during attempted recovery: "
						+ t.getClass().getName() + ":" + t.getMessage());
				throw new ActivitiException(e.getMessage(), e);
			}
		}
	}

    @Override
    public InputStream getResource(String sUrl, String method,
            Map<String, String> headers, Map<String, String> data)
            throws IOException {
        System.err
                .println("Ignoring method, headers and parameters - those are not yet implemented");
        return getResource(sUrl);
    }
}
