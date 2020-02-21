package io.freefair.javafxdi;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import java.net.URL;
import java.util.Enumeration;
import java.util.Objects;
import java.util.ResourceBundle;

public class JavaFXHelper {

	private static FXMLLoader loader() {
		return loader(new FXMLLoader());
	}

	private static FXMLLoader loader(URL url) {
		return loader(new FXMLLoader(url));
	}

	private static FXMLLoader loader(FXMLLoader loader) {
		loader.setControllerFactory(param -> Application.getInstance().getContext().getBean(param));
		return loader;
	}

	public static Parent loadFile(String filename) {
		try {
			return loader().load(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void loadAndSet(String filename, AnchorPane controlNode) {
		FXMLLoader fxmlLoader = loader(Thread.currentThread().getContextClassLoader().getResource(filename));
		fxmlLoader.setRoot(controlNode);
		fxmlLoader.setController(controlNode);
		try {
			fxmlLoader.load();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void addToAnchorFull(AnchorPane pane, Node child) {
		pane.getChildren().add(child);
		AnchorPane.setBottomAnchor(child, 0.0);
		AnchorPane.setTopAnchor(child, 0.0);
		AnchorPane.setLeftAnchor(child, 0.0);
		AnchorPane.setRightAnchor(child, 0.0);
	}

	public static void replaceRoot(AnchorPane rootPane, String s) {
		rootPane.getChildren().clear();
		addToAnchorFull(rootPane, loadFile(s));
	}
}
