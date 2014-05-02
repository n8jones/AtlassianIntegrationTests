package AtlassianTests;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.TestCase;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.archive.ScatteredArchive;

/**
 *
 * @author nate.jones
 */
public class TestCustomRestCall extends TestCase {

    private final URL baseUri;
    private GlassFish glassfish;

    public TestCustomRestCall() throws MalformedURLException {
        baseUri = new URL("http://localhost:9999/AtlassianIntegrationTests/index.jsp");
    }

    public void testApp() throws Exception {
        HttpResponse res = Request.Get("http://localhost:8084/AtlassianIntegrationTests/rest/authorize")
                //.addHeader("User-Agent", "curl/7.36.0")
                //.addHeader("Host", "localhost:9999")
                //.addHeader("Accept", "*/*")
                //.addHeader("Connection", "Keep-Alive")
                //.version(HttpVersion.HTTP_1_1)
                //.setCacheControl("no-cache, no-store, no-transform")
                //.viaProxy(new HttpHost("127.0.0.1", 8888))
                .execute().returnResponse();
        System.out.println("Headers: ");
        for (Header header : res.getAllHeaders()) {
            System.out.println("\t" + header);
        }
    }

    private static String readString(InputStream input) throws IOException {
        final Reader reader = new BufferedReader(new InputStreamReader(input));
        final StringBuilder ret = new StringBuilder();
        final char[] buffer = new char[1024];
        int count;
        while ((count = reader.read(buffer)) > 0) {
            ret.append(buffer, 0, count);
        }
        return ret.toString();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GlassFishRuntime runtime = GlassFishRuntime.bootstrap();
        GlassFishProperties glassfishProperties = new GlassFishProperties();
        glassfishProperties.setPort("http-listener", baseUri.getPort());
        glassfish = runtime.newGlassFish(glassfishProperties);
        glassfish.start();

        Deployer deployer = glassfish.getDeployer();
        ScatteredArchive archive = new ScatteredArchive("AtlassianIntegrationTests",
                ScatteredArchive.Type.WAR, new File("src/main/webapp"));
        archive.addClassPath(new File("target/classes"));
        archive.addMetadata(new File("src/main/webapp/WEB-INF/web.xml"));
        deployer.deploy(archive.toURI());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        glassfish.stop();
        glassfish = null;
    }
}
