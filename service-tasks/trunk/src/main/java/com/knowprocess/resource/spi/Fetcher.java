package com.knowprocess.resource.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import com.knowprocess.resource.internal.ClasspathResource;
import com.knowprocess.resource.internal.DeployedResource;
import com.knowprocess.resource.internal.JsonRepository;
import com.knowprocess.resource.internal.MemRepository;
import com.knowprocess.resource.internal.UrlResource;
import com.knowprocess.resource.internal.gdrive.GDriveRepository;

/**
 * The main entry point for the library.
 * 
 * <p>
 * Fundamentally this class expects one or more <code>Resource</code>s to be
 * fetched into a <code>Repository</code>. Variations include a resource being
 * passed in pre-fetched and / or storing the output in the process context
 * rather than an external repository.
 * 
 * @author timstephenson
 * 
 * @see ClasspathResource
 * @see UrlResource
 * @see GDriveRepository
 * @see JsonRepository
 */
public class Fetcher extends RestService implements JavaDelegate {

	public static final String MIME_XML = "text/xml";
	private static final String RESOURCE_KEY = "resource";
	private static final int MAX_VAR_LENGTH = 4000;
	public static final String PROTOCOL = "classpath://";

    private RepositoryService repositoryService;

	public Fetcher() {
		super();
	}

	public void setRepositoryService(RepositoryService repositoryService2) {
		this.repositoryService = repositoryService2;
	}

	// public String getGlobalResource() {
	// return globalResource;
	// }

	public String fetchToString(String resourceUrl) throws IOException {
		String repoUri = "mem://string";

		MemRepository repo = (MemRepository) getRepository(repoUri);
		fetchToRepo(resourceUrl, getResourceName(resourceUrl), repo);
		System.out.println("resource:" + repo.getString());
		return repo.getString();
	}

	public String fetchToString(InputStream is, String resourceName, String mime)
			throws IOException {
		MemRepository repo = (MemRepository) getRepository("mem://string");
		fetchToRepo(resourceName, mime, is, repo);
		return repo.getString();
	}

	public byte[] fetchToByteArray(String resourceUrl) throws IOException {
		String repoUri = "mem://bytes";

		MemRepository repo = (MemRepository) getRepository(repoUri);
		fetchToRepo(getResourceName(resourceUrl), "application/octet-stream",
				getResource(resourceUrl).getResource(resourceUrl), repo);
		return repo.getBytes();
	}

	public void fetchToRepo(String resourceUrl, String resourceName,
			Repository repo) throws IOException {
		Resource resource = getResource(resourceUrl);
		try {
			fetchToRepo(resourceName, getMime(resourceUrl),
					resource.getResource(resourceUrl), repo);
		} catch (Throwable e) {
			// TODO 
			System.err.println("***************");
			e.printStackTrace();
		}
	}

	protected void fetchToRepo(String resourceName, String mime,
			InputStream is, Repository repo) throws IOException {
		System.out.println(String.format("fetchToRepo(%1$s, %2$s, %3$s, %4$s)",
				is, resourceName, mime, repo));
		try {
			repo.write(resourceName, mime, new Date(), is);
		} finally {
			try {
				is.close();
			} catch (Exception e) {
				;
			}
		}
	}

	public void fetchToRepo(String resourceUrl, String repoUri)
			throws IOException {
		fetchToRepo(resourceUrl, getResourceName(resourceUrl),
				getRepository(repoUri));
	}

	private String getResourceName(String resourceUrl) {
		return resourceUrl.substring(resourceUrl.lastIndexOf('/') + 1);
	}

	public void fetchToRepo(String resourceUrl, String resourceName,
			String repoUri) throws IOException {
		fetchToRepo(resourceUrl, resourceName, getRepository(repoUri));
	}

	/**
	 * Detect the mime type of the resource based on the url's extension (which
	 * is obviously going to fail if there is not one, probably ought to look at
	 * that....).
	 * <p>
	 * Handy reference: http://www.webmaster-toolkit.com/mime-types.shtml.
	 * 
	 * @param resourceUrl
	 * @return
	 */
	private String getMime(String resourceUrl) {
		String ext = resourceUrl.substring(resourceUrl.lastIndexOf('.') + 1);
		if ("bpmn".equalsIgnoreCase(ext)) {
			return MIME_XML;
		} else if ("html".equalsIgnoreCase(ext)) {
			return "text/html";
		} else if ("txt".equalsIgnoreCase(ext)) {
			return "text/plain";
		} else if ("jpg".equalsIgnoreCase(ext) || "jpeg".equalsIgnoreCase(ext)) {
			return "image/jpeg";
		} else if ("json".equalsIgnoreCase(ext)) {
			return "application/json";
		} else if ("mp3".equalsIgnoreCase(ext)) {
			return "audio/mpeg3";
		} else if ("pdf".equalsIgnoreCase(ext)) {
			return "application/pdf";
		} else if ("png".equalsIgnoreCase(ext)) {
			return "image/png";
		} else {
			try {
				URL url = new URL(resourceUrl);
				if (url.getFile().length() == 0 || url.getFile().equals("/")) {
					// this is presumably going to be an html homepage
					// until proven otherwise!
				} else {
					System.out.println("Assuming html content from: "
							+ resourceUrl + ". Could be unsafe");
				}
				return "text/html";
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				System.out.println(e.getMessage());
				// catch all, treat as binary
				return "application/octet-stream";
			}
		}
	}

	private Repository getRepository(String repoUri) throws IOException {
		if (repoUri.toLowerCase().contains("docs.google.com")) {
			return new GDriveRepository();
		} else if (repoUri.toLowerCase().startsWith("mem://")) {
			return new MemRepository();
		} else {
			throw new IOException(
					"Currently the only target repositories supported are: "
							+ "Google Drive (https://docs.google.com/resourceName.type");
		}
	}

	/**
	 * Detect the best <code>Resource</code> implementation to fetch the
	 * resource's content.
	 * 
	 * <p>
	 * This is pretty easy right now as <code>UrlResource</code> is the only
	 * implementation :-).
	 * 
	 * @param resourceUrl
	 * @return <code>Resource</code> implementation.
	 * @throws IOException
	 *             If the resourceUrl cannot be supported.
	 */
	private Resource getResource(String resourceUrl) throws IOException {
		if (resourceUrl.toLowerCase().startsWith("http")
				&& resourceUsername != null && resourcePassword != null) {
			return new UrlResource(resourceUsername.getExpressionText(),
					resourcePassword.getExpressionText());
		} else if (resourceUrl.toLowerCase().startsWith("http")) {
			return new UrlResource();
		} else if (resourceUrl.toLowerCase().startsWith("classpath")) {
			return new ClasspathResource();
		} else if (resourceUrl.toLowerCase().startsWith("activiti")) {
			return new DeployedResource(repositoryService, resourceUrl);
		} else {
			throw new IOException(
					"Currently only http(s) and classpath resources are supported.");
		}
	}

	protected String[] pruneEmpty(String[] cols) {
		List<String> l = new ArrayList<String>();
		for (String col : cols) {
			if (col != null && col.trim().length() != 0) {
				l.add(col.trim());
			}
		}
		return (String[]) l.toArray(new String[l.size()]);
	}

	protected Object toCamelCase(String string) {
		StringBuffer sb = new StringBuffer();
		boolean upperCaseNext = false;
		for (int i = 0; i < string.length(); i++) {
			switch (i) {
			case 0:
				sb.append(Character.toLowerCase(string.charAt(i)));
				break;
			default:
				if (Character.isWhitespace(string.charAt(i))) {
					upperCaseNext = true;
				} else if (upperCaseNext) {
					sb.append(Character.toUpperCase(string.charAt(i)));
					upperCaseNext = false;
				} else {
					sb.append(string.charAt(i));
				}
			}
		}
		return sb.toString(); // .replaceAll("(", "").replaceAll(")", "");
	}

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		setRepositoryService(execution.getEngineServices()
				.getRepositoryService());
		String repoUri = checkRepoUri((String) execution.getVariable("repoUri"));
		Object obj = execution.getVariable("resources");
		String resource = (String) execution.getVariable(RESOURCE_KEY);
		// As a fallback check for a resource in the process definition (as
		// opposed to instance)
		if (resource == null) {
			resource = (String) globalResource.getValue(execution);
		}
		execution.setVariable("error", "");

		try {
			if (obj != null && obj instanceof Map<?, ?>) {
				Map<String, String> feedMap = (Map<String, String>) obj;
				for (Entry<String, String> entry : feedMap.entrySet()) {
					System.out.println("pushing " + entry.getKey() + " to "
							+ repoUri);
					fetchToRepo(entry.getValue(), entry.getKey(), repoUri);
				}
			} else if (resource != null && repoUri != null) {
				System.out.println("pushing " + resource + " to " + repoUri);
				fetchToRepo(resource, repoUri);
			} else if (resource != null) {
				System.out.println("fetching " + resource
						+ " into process context.");
				execution.setVariable("resourceUrl", resource);
				execution
						.setVariable("resourceName", getResourceName(resource));
				String content = fetchToString(resource);
				// TODO check and cleanup
				// if (content.length() > MAX_VAR_LENGTH) {
				// // we have a problem, cannot store in the standard Activiti
				// DB
				// if (content.contains("<html")) {
				// // Take a chance on truncation
				// content = content.substring(0, MAX_VAR_LENGTH);
				// } else {
				// String msg = "Resource is too large ("
				// + content.length()
				// + " bytes) to store as a process variable: "
				// + resource;
				// System.out.println(msg);
				// // throw new ActivitiException(msg);
				// }
				// }
				String resourceKey = (String) (outputVar == null ? RESOURCE_KEY
						: outputVar.getValue(execution));
				System.out.println(String.format("Setting %1$s to %2$s",
						resourceKey, content));
				if (getMime(resource).equals("application/json")) {
					System.out.println("Detected JSON");
					JsonReader reader = Json.createReader(new StringReader(
							content));
					JsonArray jsonObj = reader.readArray();
					System.out.println("obj" + jsonObj.toString());
					// TODO need to patch Activiti to recognise JSON
					// execution.setVariable(resourceKey, jsonObj.toString());
					execution.setVariable(resourceKey, content.getBytes());
				} else {
					System.out.println("Detected " + getMime(resource));
					execution.setVariable(resourceKey, content);
				}

			} else {
				throw new IllegalStateException(
						"You must specify resource(s) to fetch.");
			}
		} catch (Exception e) {
			String errorMsg = e.getClass().getName() + ":" + e.getMessage();
			System.out.println(errorMsg);
			execution.setVariable("error", errorMsg);
		}
	}

	private String checkRepoUri(String repoUri) {
		if (repoUri == null) {
			System.out
					.println("No repository specified, will fetch resource to process context.");
		} else if (repoUri != null
				&& !repoUri.startsWith("https://docs.google.com")) {
			throw new ActivitiIllegalArgumentException(
					String.format(
							"Repository URI '%1$s' is not supported, try 'https://docs.google.com'",
							repoUri));
		} else if (!repoUri.endsWith("/")) {
			repoUri += "/";
		}
		return repoUri;
	}
}
