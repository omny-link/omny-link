package com.knowprocess.resource.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;

import com.knowprocess.resource.spi.Resource;

public class DeployedResource implements Resource {

	private String resource;
	private RepositoryService repositoryService;
	private String deploymentName;

	public DeployedResource(RepositoryService repositoryService,
			String resourceUrl) {
		if (repositoryService == null) {
			throw new IllegalArgumentException(
					"Repository service may not be null");
		}
		this.repositoryService = repositoryService;
		int start = resourceUrl.indexOf("://") + 3;
		int end = resourceUrl.indexOf("/", start);
		deploymentName = resourceUrl.substring(start, end);
		resource = resourceUrl.substring(end + 1);
	}

	@Override
	public InputStream getResource(String uri) throws IOException {
		Deployment deployment = repositoryService.createDeploymentQuery()
				.deploymentName(deploymentName).orderByDeploymenTime().desc()
				.list().get(0);
		System.out.println("Found deployment id: " + deployment.getId());
		List<String> resourceNames = repositoryService
				.getDeploymentResourceNames(deployment.getId());
		for (String resource : resourceNames) {
			System.out.println("resource: " + resource);
		}
		return repositoryService.getResourceAsStream(deployment.getId(),
				resource);
	}
}
