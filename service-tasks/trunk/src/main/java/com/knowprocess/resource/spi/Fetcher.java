package com.knowprocess.resource.spi;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import com.knowprocess.resource.internal.ClasspathResource;
import com.knowprocess.resource.internal.MemRepository;
import com.knowprocess.resource.internal.UrlResource;
import com.knowprocess.resource.internal.gdrive.GDriveRepository;

/**
 * The main entry point for the library.
 * 
 * <p>
 * Fundamentally this class expects a <code>Resource</code> to be fetched into a
 * <code>Repository</code>.
 * 
 * @author timstephenson
 */
public class Fetcher implements JavaDelegate {

    private static final int MAX_VAR_LENGTH = 4000;
    public static final String PROTOCOL = "classpath://";

    public Fetcher() {
        super();
    }

    public String fetchToString(String resourceUrl) throws IOException {
        String repoUri = "mem://string";

        MemRepository repo = (MemRepository) getRepository(repoUri);
        fetchToRepo(resourceUrl,
                getResourceName(resourceUrl), repo);
        System.out.println("resource:" + repo.getString());
        return repo.getString();
    }

    public void fetchToRepo(String resourceUrl, String resourceName,
            Repository repo) throws IOException {
        Resource resource = getResource(resourceUrl);

        InputStream is = null;
        try {
            is = resource.getResource(resourceUrl);
            repo.write(resourceName, getMime(resourceUrl), new Date(), is);
        } finally {
            is.close();
        }

    }

    public void fetchToRepo(String resourceUrl, String repoUri)
            throws IOException {
        fetchToRepo(resourceUrl,
                getResourceName(resourceUrl),
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
            return "text/xml";
        } else if ("txt".equalsIgnoreCase(ext)) {
            return "text/plain";
        } else if ("jpg".equalsIgnoreCase(ext) || "jpeg".equalsIgnoreCase(ext)) {
            return "image/jpeg";
        } else if ("json".equalsIgnoreCase(ext)) {
            return "application/json";
        } else if ("mp3".equalsIgnoreCase(ext)) {
            return "audio/mpeg3";
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
                e.printStackTrace();
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
        if (resourceUrl.toLowerCase().startsWith("http")) {
            return new UrlResource();
        } else if (resourceUrl.toLowerCase().startsWith("classpath")) {
            return new ClasspathResource();
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
        String repoUri = checkRepoUri((String) execution.getVariable("repoUri"));

        Object obj = execution.getVariable("resources");
        String resource = (String) execution.getVariable("resource");
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
            execution.setVariable("resourceName", getResourceName(resource));
            String content = fetchToString(resource);
            if (content.length() > MAX_VAR_LENGTH) {
                // we have a problem, cannot store in the standard Activiti DB
                if (content.contains("<html")) {
                    // Take a chance on truncation
                    content = content.substring(0, MAX_VAR_LENGTH);
                } else {
                    throw new ActivitiException(
                            "Resource is too large to store as a process variable: "
                                    + resource);
                }
            }
            execution.setVariable("resource", content);
        } else {
            throw new IllegalStateException(
                    "You must specify resource(s) to fetch.");
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
