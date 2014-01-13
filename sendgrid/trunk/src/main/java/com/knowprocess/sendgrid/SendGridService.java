package com.knowprocess.sendgrid;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SendGridService {

	public enum ActionType {
		POST, GET, DELETE;

		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
	}

	public enum FormatType {
		JSON, XML;

		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
	}

	private static final String API = "https://api.sendgrid.com/api/%1$s/%2$s.%3$s";
	private DateFormat iso = new SimpleDateFormat("yyyy-MM-dd");

	protected String addTemplate(FormatType format, String urlParameters)
			throws IOException {
		try {
			URL url = new URL(String.format(API, "newsetter", "add",
					format.toString()));
			return getContent(url, ActionType.POST, urlParameters);
		} finally {
			// try {
			// is.close();
			// } catch (NullPointerException e) {
			// // Good chance this is a Jenkins environment calling localhost
			// throw new IllegalStateException(
			// "Cannot connect to SendGrid for: " + action
			// + " and format: " + format, e);
			// }
		}
	}

	protected String getTemplate(ActionType action, FormatType format,
			String urlParameters) throws IOException {
		try {
			URL url = new URL(String.format(API, "newsletter",
					action.toString(), format.toString()));
			return getContent(url, action, urlParameters);
		} finally {
			// try {
			// is.close();
			// } catch (NullPointerException e) {
			// // Good chance this is a Jenkins environment calling localhost
			// throw new IllegalStateException(
			// "Cannot connect to SendGrid for: " + action
			// + " and format: " + format, e);
			// }
		}
	}

	/**
	 * This endpoint allows you to retrieve and delete entries in the Blocks
	 * list.
	 * 
	 * @param days
	 *            If specified, must be an integer greater than 0 Number of days
	 *            in the past for which to retrieve blocks (includes today)
	 * @param limit
	 *            Some integer Optional field to limit the number of results
	 *            returned.
	 * @param offset
	 *            Beginning point in the list to retrieve from.
	 * @return
	 */
	public String getBlocks(int days, int limit, int offset)
			throws IOException {
		return getResponseList("blocks", days, limit, offset);
	}

	/**
	 * This endpoint allows you to retrieve and delete entries in the Blocks
	 * list.
	 * 
	 * @param startDate
	 *            Date must be in YYYY-MM-DD format and be earlier than the
	 *            end_date parameter. The start of the date range for which to
	 *            retrieve blocks.
	 * @param endDate
	 *            Date must be in YYYY-MM-DD format and be later than the
	 *            start_date parameter. The end of the date range for which to
	 *            retrieve blocks.
	 * @param limit
	 *            some integer Optional field to limit the number of results
	 *            returned.
	 * @param offset
	 *            optional beginning point in the list to retrieve from.
	 * @return
	 */
	public String getBlocks(Date startDate, Date endDate, int limit, int offset)
			throws IOException {
		return getResponseList("blocks", startDate, endDate, limit, offset);
	}

	/**
	 * This endpoint allows you to retrieve and delete entries in the Bounces
	 * list.
	 * 
	 * @param days
	 *            If specified, must be an integer greater than 0 Number of days
	 *            in the past for which to retrieve blocks (includes today)
	 * @param limit
	 *            Some integer Optional field to limit the number of results
	 *            returned.
	 * @param offset
	 *            Beginning point in the list to retrieve from.
	 * @return
	 */
	public String getBounces(int days, int limit, int offset)
			throws IOException {
		return getResponseList("bounces", days, limit, offset);
	}


	/**
	 * This endpoint allows you to retrieve and delete entries in the Bounces
	 * list.
	 * 
	 * @param startDate
	 *            Date must be in YYYY-MM-DD format and be earlier than the
	 *            end_date parameter. The start of the date range for which to
	 *            retrieve blocks.
	 * @param endDate
	 *            Date must be in YYYY-MM-DD format and be later than the
	 *            start_date parameter. The end of the date range for which to
	 *            retrieve blocks.
	 * @param limit
	 *            some integer Optional field to limit the number of results
	 *            returned.
	 * @param offset
	 *            optional beginning point in the list to retrieve from.
	 * @return
	 */
	public String getBounces(Date startDate, Date endDate, int limit,
			int offset) throws IOException {
		return getResponseList("bounces", startDate, endDate, limit, offset);
	}

	/**
	 * This endpoint allows you to retrieve and delete entries in the Invalid
	 * Emails list.
	 * 
	 * @param days
	 *            If specified, must be an integer greater than 0 Number of days
	 *            in the past for which to retrieve blocks (includes today)
	 * @param limit
	 *            Some integer Optional field to limit the number of results
	 *            returned.
	 * @param offset
	 *            Beginning point in the list to retrieve from.
	 * @return
	 */
	public String getInvalidEmails(int days, int limit, int offset)
			throws IOException {
		return getResponseList("invalidemails", days, limit, offset);
	}

	/**
	 * This endpoint allows you to retrieve and delete entries in the Invalid
	 * Emails list.
	 * 
	 * @param startDate
	 *            Date must be in YYYY-MM-DD format and be earlier than the
	 *            end_date parameter. The start of the date range for which to
	 *            retrieve blocks.
	 * @param endDate
	 *            Date must be in YYYY-MM-DD format and be later than the
	 *            start_date parameter. The end of the date range for which to
	 *            retrieve blocks.
	 * @param limit
	 *            some integer Optional field to limit the number of results
	 *            returned.
	 * @param offset
	 *            optional beginning point in the list to retrieve from.
	 * @return
	 */
	public String getInvalidEmails(Date startDate, Date endDate, int limit,
			int offset) throws IOException {
		return getResponseList("invalidemails", startDate, endDate, limit,
				offset);
	}

	/**
	 * This endpoint allows you to retrieve and delete entries in the
	 * Unsubscribes list.
	 * 
	 * @param days
	 *            If specified, must be an integer greater than 0 Number of days
	 *            in the past for which to retrieve blocks (includes today)
	 * @param limit
	 *            Some integer Optional field to limit the number of results
	 *            returned.
	 * @param offset
	 *            Beginning point in the list to retrieve from.
	 * @return
	 */
	public String getUnsubscribes(int days, int limit, int offset)
			throws IOException {
		return getResponseList("bounces", days, limit, offset);
	}

	/**
	 * This endpoint allows you to retrieve and delete entries in the
	 * Unsubscribes list.
	 * 
	 * @param startDate
	 *            Date must be in YYYY-MM-DD format and be earlier than the
	 *            end_date parameter. The start of the date range for which to
	 *            retrieve blocks.
	 * @param endDate
	 *            Date must be in YYYY-MM-DD format and be later than the
	 *            start_date parameter. The end of the date range for which to
	 *            retrieve blocks.
	 * @param limit
	 *            some integer Optional field to limit the number of results
	 *            returned.
	 * @param offset
	 *            optional beginning point in the list to retrieve from.
	 * @return
	 */
	public String getUnsubscribes(Date startDate, Date endDate, int limit,
			int offset) throws IOException {
		return getResponseList("unsubscribes", startDate, endDate, limit,
				offset);
	}

	protected String getResponseList(String module, int days, int limit,
			int offset) throws IOException {
		try {
			URL url = new URL(String.format(API, module, "get", FormatType.JSON
					.name().toLowerCase()));
			String urlParameters = String.format(
					"date=1&days=%1$d&limit=%2$d&offset=%3$d", days, limit,
					offset);
			return getContent(url, ActionType.GET, urlParameters);
		} finally {
			// try {
			// is.close();
			// } catch (NullPointerException e) {
			// // Good chance this is a Jenkins environment calling localhost
			// throw new IllegalStateException(
			// "Cannot connect to SendGrid for: " + action
			// + " and format: " + format, e);
			// }
		}
	}

	protected String getResponseList(String module, Date startDate, Date endDate,
			int limit, int offset) throws IOException {
		try {
			URL url = new URL(String.format(API, module, "get",
					FormatType.JSON.name().toLowerCase()));
			String urlParameters = String
					.format("date=1&start_date=%1$s&end_date=%2$s&limit=%3$d&offset=%4$d",
							iso.format(startDate), iso.format(endDate), limit,
							offset);
			return getContent(url, ActionType.GET, urlParameters);
		} finally {
			// try {
			// is.close();
			// } catch (NullPointerException e) {
			// // Good chance this is a Jenkins environment calling localhost
			// throw new IllegalStateException(
			// "Cannot connect to SendGrid for: " + action
			// + " and format: " + format, e);
			// }
		}
	}

	protected String getContent(URL url, ActionType action, String urlParameters)
			throws IOException {
		System.out.println(action + " to " + url + " with params: "
				+ urlParameters);
		InputStream is = null;
		StringBuffer response = new StringBuffer();
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setInstanceFollowRedirects(false);
			connection.setRequestMethod(action.name());
			urlParameters = urlParameters + "&api_user=" + "johnnymb"
					+ "&api_key=" + "J3p3BtTfAb";
			// connection.setRequestProperty("api_user", "johnnymb");
			// connection.setRequestProperty("api_key", "J3p3BtTfAb");
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("Content-Length",
					"" + Integer.toString(urlParameters.getBytes().length));
			connection.setUseCaches(false);

			DataOutputStream wr = new DataOutputStream(
					connection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			byte[] b = new byte[1024];
			is = (InputStream) connection.getContent();
			while (is.read(b) != -1) {
				response.append(new String(b).trim());
			}
			connection.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				is.close();
			} catch (NullPointerException e2) {
				throw new IOException(e2);
			}
		}
		return response.toString();
	}
}
