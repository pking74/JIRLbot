package at.rovo.crawler.bean;

import at.rovo.common.UnitTest;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

/**
 *
 */
@Category(UnitTest.class)
public class RobotsTxtTest
{
    @Test
    public void robotsTxtParsingTest() throws Exception
    {
        URL path = this.getClass().getResource("/test_robots.txt");
        String robotsTxtContent = new String(Files.readAllBytes(Paths.get(path.toURI())));

        RobotsTxt robotsTxt = new RobotsTxt(robotsTxtContent);

        assertThat(robotsTxt.getRecords().size(), is(equalTo(3)));
        assertThat(robotsTxt.getRecords().keySet(), contains("Sidewinder", "Microsoft.URL.Control", "*"));

        assertThat(robotsTxt.checkRobotRules("*", "http://some.site.com/default.html"), is(equalTo(false)));
        assertThat(robotsTxt.checkRobotRules("*", "http://some.site.com/index.html"), is(equalTo(true)));
        assertThat(robotsTxt.checkRobotRules("*", "http://some.site.com/test/1234/index.html"), is(equalTo(false)));
        assertThat(robotsTxt.checkRobotRules("*", "http://some.site.com/test/junit/index.html"), is(equalTo(true)));
    }

    @Test
    public void robotsTxt_wikipedia() throws Exception
    {
        URL path = this.getClass().getResource("/wikipedia.org_robots.txt");
        String robotsTxtContent = new String(Files.readAllBytes(Paths.get(path.toURI())));

        RobotsTxt robotsTxt = new RobotsTxt(robotsTxtContent);
        Map<String, RobotsTxt.RobotsTxtRecord> records = robotsTxt.getRecords();
        records.values().forEach(RobotsTxt.RobotsTxtRecord::getAllowedPathSegments);
    }
}
