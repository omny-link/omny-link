package com.knowprocess.jaxrs.test;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import javax.ws.rs.core.UriInfo;

public class MockUriInfo implements UriInfo {

	public class MockUriBuilder extends UriBuilder {

		private static final String BASE = "http://localhost:8080";
		private List<String> paths = new ArrayList<String>();

		@Override
		public UriBuilder clone() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UriBuilder uri(URI uri) throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UriBuilder scheme(String scheme) throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UriBuilder schemeSpecificPart(String ssp)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UriBuilder userInfo(String ui) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UriBuilder host(String host) throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UriBuilder port(int port) throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UriBuilder replacePath(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UriBuilder path(String path) throws IllegalArgumentException {
			paths.add(path);
			return this;
		}

		@Override
		public UriBuilder path(Class resource) throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UriBuilder path(Class resource, String method)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UriBuilder path(Method method) throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UriBuilder segment(String... segments)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UriBuilder replaceMatrix(String matrix)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UriBuilder matrixParam(String name, Object... values)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UriBuilder replaceMatrixParam(String name, Object... values)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UriBuilder replaceQuery(String query)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UriBuilder queryParam(String name, Object... values)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UriBuilder replaceQueryParam(String name, Object... values)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UriBuilder fragment(String fragment) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public URI buildFromMap(Map<String, ? extends Object> values)
				throws IllegalArgumentException, UriBuilderException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public URI buildFromEncodedMap(Map<String, ? extends Object> values)
				throws IllegalArgumentException, UriBuilderException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public URI build(Object... values) throws IllegalArgumentException,
				UriBuilderException {
			// Beware! This can go wrong in lots of ways but it's a simple mock
			// implementation
			StringBuilder sb = new StringBuilder(BASE);
			for (int i = 0; i < values.length; i++) {
				String path = paths.get(i);
				String placeholder = path.substring(path.indexOf('{'),
						path.indexOf('}') + 1);
				sb.append(paths.get(i).replace(placeholder,
						values[i].toString()));
			}
			try {
				return new URI(sb.toString());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException(e.getMessage(), e);
			}
		}

		@Override
		public URI buildFromEncoded(Object... values)
				throws IllegalArgumentException, UriBuilderException {
			// TODO Auto-generated method stub
			return null;
		}

	}

	private UriBuilder uriBuilder;

	public MockUriInfo() {
		this.uriBuilder = new MockUriBuilder();
	}

	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPath(boolean decode) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PathSegment> getPathSegments() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PathSegment> getPathSegments(boolean decode) {
		// TODO Auto-generated method stub
		return null;
	}

	public URI getRequestUri() {
		// TODO Auto-generated method stub
		return null;
	}

	public UriBuilder getRequestUriBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	public URI getAbsolutePath() {
		// TODO Auto-generated method stub
		return null;
	}

	public UriBuilder getAbsolutePathBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	public URI getBaseUri() {
		// TODO Auto-generated method stub
		return null;
	}

	public UriBuilder getBaseUriBuilder() {
		return uriBuilder;
	}

	public MultivaluedMap<String, String> getPathParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	public MultivaluedMap<String, String> getPathParameters(boolean decode) {
		// TODO Auto-generated method stub
		return null;
	}

	public MultivaluedMap<String, String> getQueryParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getMatchedURIs() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getMatchedURIs(boolean decode) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Object> getMatchedResources() {
		// TODO Auto-generated method stub
		return null;
	}

}
