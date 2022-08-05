package de.hierlmeier.paxweb8test.itests;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.karaf.itests.KarafTestSupport;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class SessionCookieTest extends KarafTestSupport {

    public static final String SESSION_COOKIE_NAME = UUID.randomUUID().toString();

    @Configuration
    public Option[] config() {
        Option[] options = new Option[]{
                KarafDistributionOption.editConfigurationFilePut("org.ops4j.pax.web.cfg", "org.ops4j.pax.web.session.cookie.name", SESSION_COOKIE_NAME),
                KarafDistributionOption.features(CoreOptions.maven("org.ops4j.pax.web", "pax-web-features").type("xml")
                        .classifier("features").versionAsInProject(), "pax-web-specs", "pax-web-http-whiteboard", "pax-web-karaf"),
                CoreOptions.bundle(CoreOptions.maven("org.apache.httpcomponents", "httpcore-osgi").versionAsInProject().getURL()),
                CoreOptions.bundle(CoreOptions.maven("org.apache.httpcomponents", "httpclient-osgi").versionAsInProject().getURL())

        };
        return Stream.of(super.config(), options).flatMap(Stream::of).toArray(Option[]::new);
    }

    @Test(timeout = 20_000)
    public void test() throws Exception {

        Hashtable<String, Object> properties = new Hashtable<>();

        assertFeatureInstalled("pax-web-http-whiteboard");

        properties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, new String [] { "/sample" });
        properties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT, "(osgi.http.whiteboard.context.path=/)");

        ServiceRegistration<Servlet> serviceRegistration = bundleContext.registerService(Servlet.class, new SampleServlet(), properties);
        try {
            BasicCookieStore cookieStore = new BasicCookieStore();
            try(CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build()) {

                HttpGet get = new HttpGet("http://localhost:" + getHttpPort() + "/sample");

                waitUntilSampleServletIsAvailable();

                HttpResponse response = httpClient.execute(get);

                Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());

                System.out.println("Cookies: " + cookieStore.getCookies().stream().map(c -> c.getName() + ":" + c.getValue()).collect(Collectors.joining()));
                Assert.assertFalse("The default value for the session cookie name is still active",cookieStore.getCookies().stream().anyMatch(c -> "JSESSIONID".equals(c.getName())));
                Assert.assertTrue("Unexpected session cookie name", cookieStore.getCookies().stream().anyMatch(c -> SESSION_COOKIE_NAME.equals(c.getName())));
            }
        } finally {
            serviceRegistration.unregister();
        }
    }

    private void waitUntilSampleServletIsAvailable() throws InterruptedException {

        while(true) {
            String result = executeCommand("web:servlet-list");
            System.out.println(result);
            if(result != null && result.contains("/sample")) {
                break;
            }

            Thread.sleep(1000);
        }
    }



    public static class SampleServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

            HttpSession session = req.getSession(true);

            try (PrintWriter pw = resp.getWriter()) {
                pw.println("Session: " + session.getId());
            }

        }
    }


}
