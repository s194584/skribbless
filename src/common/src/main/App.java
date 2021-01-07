package common.src.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jspace.FormalField;
import org.jspace.RandomSpace;
import org.jspace.Space;

public class App extends Application {

	public static void main(String[] args) {launch(args);}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("start.fxml"));
		Space inbox = new RandomSpace();
		inbox.put("Hello World!");
		Object[] tuple = inbox.get(new FormalField(String.class));
		System.out.println(tuple[0]);
		primaryStage.setTitle(tuple[0].toString());
		primaryStage.setScene(new Scene(root,300,275));
		primaryStage.show();
	}
}