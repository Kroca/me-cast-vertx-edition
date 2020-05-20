package kroca.youcast;

import io.vertx.core.Launcher;
import io.vertx.core.logging.SLF4JLogDelegateFactory;


import static io.vertx.core.logging.LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME;
import static java.lang.System.setProperty;

public class Starter {
    public static void main(String[] args) {
        setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
        Launcher.executeCommand("run", MainVerticle.class.getName());
    }
}
