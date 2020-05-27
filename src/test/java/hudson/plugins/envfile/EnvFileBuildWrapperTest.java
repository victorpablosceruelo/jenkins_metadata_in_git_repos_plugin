package hudson.plugins.envfile;

import org.jvnet.hudson.test.HudsonTestCase;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: kedar
 * Date: May 29, 2010
 * Time: 8:21:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class EnvFileBuildWrapperTest extends HudsonTestCase {

    private static final Logger LOGGER = Logger.getLogger(EnvFileBuildWrapperTest.class.getName());
    
    public void testBasic() throws IOException {
        LOGGER.warning("EnvFileBuildWrapperTest:testBasic");
    }
}
