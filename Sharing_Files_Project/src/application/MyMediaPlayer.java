package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import view.ServerView;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;


public class MyMediaPlayer extends Application {
	
	private BorderPane root;
	private GridPane pane;
	private Scene scene;
	private VBox box;
	
	@Override
	public void start(Stage primaryStage) {
		
		try 
		{
			pane = new GridPane();
			box = new ServerView(primaryStage);
			root = new BorderPane(pane);
			pane.add(box, 0, 0);
			BorderPane.setMargin(pane, new Insets(20));
			scene = new Scene(root,800, 600);
			primaryStage.setScene(scene);
			primaryStage.show();
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
		}		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
