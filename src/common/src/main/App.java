package common.src.main;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

public class App extends Application {
	// To run, add VM options: --module-path $JAVAFX_11$ --add-modules javafx.swing,javafx.graphics,javafx.fxml,javafx.media,javafx.controls
	public static Space inbox;
	public static void main(String[] args) throws InterruptedException {
		inbox = new SequentialSpace();
		inbox.put("startssssss");
//		inbox.put("title1");
		launch(args);
	}



	@Override
	public void start(Stage primaryStage) throws Exception {
//		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("start.fxml"));
//		fxmlLoader.setController(new StartController(inbox));
//		Parent root = fxmlLoader.load();
//		primaryStage.setScene(new Scene(root,300,275));
//		primaryStage.setTitle("Titles");
//		primaryStage.show();
	}
}