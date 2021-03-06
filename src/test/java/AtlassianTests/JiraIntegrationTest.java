//JiraIntegrationTest.java
package AtlassianTests;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.integration.rest.service.factory.RestCrowdClientFactory;
import com.atlassian.crowd.model.authentication.CookieConfiguration;
import com.atlassian.crowd.model.authentication.Session;
import com.atlassian.crowd.model.authentication.UserAuthenticationContext;
import com.atlassian.crowd.model.authentication.ValidationFactor;
import com.atlassian.crowd.service.client.CrowdClient;
import com.atlassian.jira.rest.client.AuthenticationHandler;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.JiraRestClientFactory;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.ProgressMonitor;
import com.atlassian.jira.rest.client.ProjectRestClient;
import com.atlassian.jira.rest.client.SessionRestClient;
import com.atlassian.jira.rest.client.UserRestClient;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.User;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.Filterable;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.*;
import javax.ws.rs.core.NewCookie;
import junit.framework.TestCase;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpState;
import org.junit.Test;

/**
 * Tests of the Jira Rest API. These tests expect a local instance of Jira
 * running at port 8080 and a local instance of Crowd running at 8095. In
 * addition there is an application configured in crowd called crowd-auth-webapp
 * with the password "password". Also a user in crowd with the credentials
 * test/test.
 */
public class JiraIntegrationTest extends TestCase {

    /**
     * This test case attempts to authenticate with the local Crowd instance and
     * use that authentication token to access Jira. This currently fails when
     * we attempt to access Jira. It seems to access Crowd without a problem,
     * but haven't been able to access Jira yet using that token.
     *
     * @see <a
     * href="https://answers.atlassian.com/questions/288694/integrating-crowd-authentication-with-jira-rest-api">Atlassian
     * Community Question</a>
     */
    @Test
    public void testCrowdAuthentication() throws Exception {
        System.out.println("\n\n****TestCrowdAuthentication****");
        ValidationFactor[] factors = new ValidationFactor[]{
            new ValidationFactor("remote_address", "127.0.0.1")
        };
        RestCrowdClientFactory factory = new RestCrowdClientFactory();
        CrowdClient client = factory.newInstance("http://127.0.0.1:8095/crowd",
                "test-app", "test");
        assertNotNull("Client should not be null", client);
        final CookieConfiguration cookieConfig = client
                .getCookieConfiguration();
        assertNotNull("Cookie Config should not be null", cookieConfig);
        System.out.printf("Cookie Config: %1$s, %2$s\n",
                cookieConfig.getDomain(), cookieConfig.getName());
        UserAuthenticationContext userAuthCtx = new UserAuthenticationContext();
        userAuthCtx.setName("test");
        userAuthCtx.setCredential(new PasswordCredential("test"));
        userAuthCtx.setValidationFactors(factors);
        final String token = client.authenticateSSOUser(userAuthCtx);
        assertNotNull("Token should not be null", token);
        final Session session = client.validateSSOAuthenticationAndGetSession(
                token, Arrays.asList(factors));
        assertNotNull("Session should not be null", session);
        assertEquals("Session token and original token should be equal",
                session.getToken(), token);
        System.out.printf("Session Token: %s %n", session.getToken());
        System.out.printf("Session Expires: %tc %n", session.getExpiryDate());

        JiraRestClientFactory jiraFactory = new JerseyJiraRestClientFactory();
        JiraRestClient jira = jiraFactory.create(new URI(
                "http://localhost:8080"), new AuthenticationHandler() {

                    public void configure(ApacheHttpClientConfig config) {
                        config.getProperties().put(
                                ApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES, true);
                        Cookie cookie = new Cookie();
                        cookie.setName(cookieConfig.getName());
                        cookie.setValue(token);
                        cookie.setDomain("localhost");
                        cookie.setPath("/");
                        config.getState().getHttpState().addCookie(cookie);
                    }

                    public void configure(Filterable filterable, Client client) {
                        //Do Nothing
                    }

                });

        TestJiraClient(jira);
    }

    /**
     * This test case uses basic authentication and it works.
     */
    @Test
    public void testBasicAuthentication() throws Exception {
        System.out.println("\n\n****TestBasicAuthentication****");
        JiraRestClientFactory jiraFactory
                = new com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory();
        JiraRestClient jira = jiraFactory.create(new URI(
                "http://127.0.0.1:8080"), new BasicHttpAuthenticationHandler(
                        "test", "test"));
        TestJiraClient(jira);
    }

    /**
     * Attempts to do some basic access of Jira using the supplied client.
     *
     * @param jira
     */
    private void TestJiraClient(JiraRestClient jira) {
        ProgressMonitor pm = new NullProgressMonitor();
        SessionRestClient sessions = jira.getSessionClient();
        com.atlassian.jira.rest.client.domain.Session jiraSession = sessions
                .getCurrentSession(pm);
        assertNotNull(jiraSession);
        ProjectRestClient projects = jira.getProjectClient();
        System.out.println("getAllProjects:");
        List<BasicProject> projs = StreamSupport.stream(projects.getAllProjects(pm).spliterator(), false).collect(Collectors.toList());

        for (BasicProject project : projs) {
            System.out.println("\t" + project.getName());
        }
        UserRestClient users = jira.getUserClient();
        User user = users.getUser("test", pm);
        System.out.println("getUser:");
        System.out.println("\t" + user.getName());
    }
}
