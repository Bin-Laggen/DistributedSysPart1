package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import view.ServerView;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;


public class MyMediaPlayer extends Application {
	
	private ScrollPane root;
	private GridPane pane;
	private Scene scene;
	private VBox box;
	
	@Override
	public void start(Stage primaryStage) {
		
		try 
		{
			pane = new GridPane();
			box = new ServerView(primaryStage, null);
			root = new ScrollPane(pane);
			pane.add(box, 0, 0);
			pane.setPadding(new Insets(20));
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
