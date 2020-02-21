package io.freefair.javafxdi;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.Resource;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.ResourcePropertySource;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"SameParameterValue", "WeakerAccess", "unused"})
@Slf4j
public abstract class Application extends javafx.application.Application {
	private static String applicationName;
	private static String mainFxml;
	private static String[] args;

	private int width;
	private int height;

	@Getter
	private AnnotationConfigApplicationContext context;

	@Getter
	private ConfigurableEnvironment environment;

	private ScanResult classGraphScanResult;

	@Getter
	private static Application instance;

	private List<String> styles = new ArrayList<>();

	protected static void run(Class<? extends Application> clazz, String applicationName, String mainFxml, String args[]) {
		Application.applicationName = applicationName;
		Application.mainFxml = mainFxml;
		Application.args = args;

		launch(clazz, args);
	}

	private static void initSlf4J(String level) {
		Level threshold = Level.toLevel(level);

		ConsoleAppender console = new ConsoleAppender(); //create appender
		//configure the appender
		String PATTERN = "%d [%p|%c|%C{1}] %m%n";

		ConsoleAppender appender = new ConsoleAppender();
		appender.setLayout(new PatternLayout(PATTERN));
		appender.setTarget(ConsoleAppender.SYSTEM_OUT);
		appender.setThreshold(threshold);
		appender.setName("ConsoleAppender");
		appender.activateOptions();

		//add appender to any Logger (here is root)
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(appender);
		Logger.getRootLogger().setLevel(threshold);
	}

	public Application() {
		if(instance != null) throw new RuntimeException("Only one application is allowed!");
		instance = this;
	}

	protected void setWidth(int width) {
		this.width = width;
	}

	protected void setHeight(int height) {
		this.height = height;
	}

	@Override
	public void start(Stage stage) {
		initSlf4J("INFO");
		environment = new StandardEnvironment();
		initEnvironment(environment);
		String property = environment.getProperty("logging.level", "INFO");
		initSlf4J(property);
		context = new AnnotationConfigApplicationContext();
		context.setEnvironment(environment);
		initContext(context);
		context.refresh();
		initParams();
		Parent rootNode = JavaFXHelper.loadFile(mainFxml);

		Scene scene = new Scene(rootNode, width, height);
		scene.getStylesheets().addAll(styles);
		initScene(scene);

		stage.setTitle(applicationName);
		stage.setMinWidth(width);
		stage.setMinHeight(height);
		stage.setScene(scene);
		initStage(stage);

		stage.show();
	}

	private void initEnvironment(ConfigurableEnvironment environment) {
		ResourceList properties = getScanResult().getResourcesWithExtension("properties");
		for (Resource resource :
				properties) {
			try {
				log.debug("Adding resource " + resource + " to environment.");
				environment.getPropertySources().addFirst(new ResourcePropertySource(resource.getPath()));
			} catch (Exception ex) {
				log.error("Error in environment initialization", ex);
			}
		}
	}

	private void initContext(AnnotationConfigApplicationContext context) {
		ClassInfoList typesAnnotatedWith = getScanResult().getClassesWithAnnotation(Configuration.class.getCanonicalName());
		typesAnnotatedWith.forEach(i -> {
			if(!i.getPackageName().startsWith("org.springframework"))
				context.register(i.loadClass());
		});
		context.setClassLoader(Thread.currentThread().getContextClassLoader());
		context.setResourceLoader(new DefaultResourceLoader(Thread.currentThread().getContextClassLoader()));
	}

	private ScanResult getScanResult() {
		if(classGraphScanResult == null) {
			classGraphScanResult = new ClassGraph()
					.enableAllInfo()
					.scan();
		}
		return classGraphScanResult;
	}

	@Override
	public void stop() {

	}

	protected void addStylesheet(String stylesheet) {
		log.debug("Adding stylesheet: " + stylesheet);
		URL resource = Thread.currentThread().getContextClassLoader().getResource(stylesheet);
		if(resource == null) throw new IllegalArgumentException("Stylesheet not found!");
		styles.add(resource.toExternalForm());
	}

	protected void initParams() {
	}

	protected void initStage(Stage stage) {
	}

	protected void initScene(Scene scene) {
	}
}
