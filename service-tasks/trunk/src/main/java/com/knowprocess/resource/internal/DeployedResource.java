package com.knowprocess.resource.internal;

import java.io.IOException;
import java.io.InputStream;

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
}
