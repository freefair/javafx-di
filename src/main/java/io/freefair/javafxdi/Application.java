package io.freefair.javafxdi;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

@SuppressWarnings({"SameParameterValue", "WeakerAccess", "unused"})
@Slf4j
public class Application extends javafx.application.Application {
	private static String applicationName;
	private static String mainFxml;
	private static String[] args;

	private int width;
	private int height;

	@Getter
	private AnnotationConfigApplicationContext context;

	@Getter
	private ConfigurableEnvironment environment;

	private Reflections reflections;

	@Getter
	private static Application instance;

	protected static void run(String applicationName, String mainFxml, String args[]) {
		Application.applicationName = applicationName;
		Application.mainFxml = mainFxml;
		Application.args = args;

		launch(args);
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
		environment = new StandardEnvironment();
		initEnvironment(environment);
		context = new AnnotationConfigApplicationContext();
		context.setEnvironment(environment);
		initContext(context);
		context.refresh();
		initParams();
		Parent rootNode = JavaFXHelper.loadFile(mainFxml);

		Scene scene = new Scene(rootNode, width, height);
		initScene(scene);

		stage.setTitle(applicationName);
		stage.setMinWidth(width);
		stage.setMinHeight(height);
		stage.setScene(scene);
		initStage(stage);

		stage.show();
	}

	private void initEnvironment(ConfigurableEnvironment environment) {
		//environment.getPropertySources().addFirst(new ResourcePropertySource());
	}

	private void initContext(AnnotationConfigApplicationContext context) {
		Reflections reflections = getReflections();
		Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(Configuration.class);
		typesAnnotatedWith.forEach(i -> {
			if(!i.getTypeName().startsWith("org.springframework"))
				context.register(i);
		});
		context.setClassLoader(Thread.currentThread().getContextClassLoader());
		context.setResourceLoader(new DefaultResourceLoader(Thread.currentThread().getContextClassLoader()));
	}

	private Reflections getReflections() {
		if(this.reflections == null) {
			Collection<URL> urls = new ArrayList<>(ClasspathHelper.forClassLoader(Thread.currentThread().getContextClassLoader()));
			urls.addAll(ClasspathHelper.forClassLoader(Application.class.getClassLoader()));
			urls.addAll(ClasspathHelper.forManifest());
			org.reflections.Configuration configuration = new ConfigurationBuilder()
					.addClassLoader(Thread.currentThread().getContextClassLoader())
					.setScanners(new SubTypesScanner(false), new TypeAnnotationsScanner(), new MethodAnnotationsScanner())
					.setUrls(urls);
			this.reflections = new Reflections(configuration);
		}
		return this.reflections;
	}

	@Override
	public void stop() {

	}

	protected void initParams() {
	}

	protected void initStage(Stage stage) {
	}

	protected void initScene(Scene scene) {
	}
}