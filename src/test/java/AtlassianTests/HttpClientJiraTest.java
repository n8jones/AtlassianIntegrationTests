package AtlassianTests;

import java.io.IOException;
import java.util.Date;
import junit.framework.TestCase;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import static org.hamcrest.CoreMatchers.*;
import org.json.JSONArray;
import org.json.JSONObject;

import static org.junit.Assert.*;
import org.junit.Test;
/**
 * Testing the jira rest api with crowd authentication using simple libraries.
 */
public class HttpClientJiraTest extends TestCase {
        @Test
	public void testHttpClientCrowdAuth() throws Exception{
		
		String projectsJson = Request.Get("http://localhost:8080/rest/api/2/project")
				.addHeader("Cookie", "crowd.token_key=vgKB98h6VqP7nSxj2oERIg00")
				.execute()
				.returnContent()
				.asString();
		System.out.println("Projects JSON:");
		System.out.println("\t" + projectsJson);
		JSONArray projects = new JSONArray(projectsJson);
		assertTrue("There should be at least 1 project", projects.length() > 0);
	}
}
