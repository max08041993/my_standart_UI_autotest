package example;


import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

//import org.junit.platform.suite.api.Suite;

//@Suite
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("example")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "example")
public class RunCucumberTests {
    // This class must be empty
    // Above Cucumber configuration parameters also can be set in junit-platform.properties or from command line.
    // priority order: ConfigurationParameter > command line System variables -D... > junit-platform.properties
}
