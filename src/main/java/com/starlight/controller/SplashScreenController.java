package com.starlight.controller;

import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Splash screen controller backing the splashScreen.fxml.
 * Previously declared as an interface causing FXML instantiation failure (NoSuchMethodException).
 */
public class SplashScreenController implements Initializable {
	private static final Logger logger = Logger.getLogger(SplashScreenController.class.getName());

	private SplashScreenController() {}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// No initialization logic yet – placeholder for potential progress animation.
		logger.fine("SplashScreen initialized");
	}
}
