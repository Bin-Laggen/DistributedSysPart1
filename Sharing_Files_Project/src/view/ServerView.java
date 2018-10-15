package view;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import controller.Monitor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class ServerView extends VBox implements Observer {
	
	private Text text;
	private Button selectDestButton;
	private String output = "";
	private Monitor mon;
	private DirectoryChooser chooser;
	private File dest;
	private MediaPlayer mPlayer;
	
	public ServerView(Stage primaryStage)
	{
		selectDestButton = new Button("Select Local Folder");
		text = new Text(output);
		chooser = new DirectoryChooser();
		chooser.setTitle("Select Location");
		mon = Monitor.getInstance();
		mon.addObserver(this);
		this.setMinWidth(600);
		this.setSpacing(5);
		
		this.getChildren().add(selectDestButton);		
		
		selectDestButton.setOnAction(e->{
			dest = chooser.showDialog(primaryStage);
			text.setText(output);
			if(dest != null)
			{
				text.setText(text.getText() + "\nSelected Location: " + dest.getPath());

				drawFileButtons(mon.getNames());
			}
			if(mon.checkForChange())
			{
				drawFileButtons(mon.getNames());
			}
		});
		
		primaryStage.setOnCloseRequest(e->{
			mon.stopThread();
		});
	}
	
	public void drawFileButtons(String[] names)
	{
		System.out.println("---");
		System.out.println(this.getChildren().toString());
		this.getChildren().clear();
		System.out.println(this.getChildren().toString());
		System.out.println("a");
		for(int i = 0; i < names.length; i++)
		{
			System.out.println("b");
			boolean found = false;
			String[] localFiles = dest.list(new FilenameFilter() {
			    @Override
			    public boolean accept(File dir, String name) 
			    {
			    	System.out.println("c");
			        return name.endsWith(".mp3");
			    } 
			});
			System.out.println("d");
			for(int j = 0; j < localFiles.length; j++)
			{
				System.out.println("e");
				if(names[i].equals(localFiles[j]))
				{
					found = true;
				}
			}
			System.out.println("f");
			HBox tmpBox = new HBox(5);
			Label tmpText = new Label(names[i]);
			tmpText.setPrefWidth(300);
			Button tmpDwnld = new Button("Download");
			tmpDwnld.setId(names[i]);
			tmpDwnld.setMinWidth(50);
			Button tmpPlay = new Button("Play");
			tmpPlay.setId(names[i]);
			tmpPlay.setMinWidth(30);
			Button tmpLoad = new Button("Load");
			tmpLoad.setId(names[i]);
			tmpLoad.setMinWidth(30);
			System.out.println("g");
			if(found)
			{
				tmpDwnld.setDisable(true);
				tmpLoad.setDisable(false);
			}
			else
			{
				tmpDwnld.setDisable(false);
				tmpLoad.setDisable(true);
			}
			System.out.println("h");
			tmpPlay.setDisable(true);
			System.out.println("i");
			tmpDwnld.setOnAction(e->{
				try 
				{
					if(mon.copyFile(tmpDwnld.getId(), dest))
					{
						tmpLoad.setDisable(false);
						tmpDwnld.setDisable(true);
					}
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
			});
			
			tmpPlay.setOnAction(e->{
				System.out.println(mPlayer.getStatus());
				if(mPlayer.getStatus() == Status.READY || mPlayer.getStatus() == Status.STOPPED || mPlayer.getStatus() == Status.PAUSED)
				{
					mPlayer.play();
					tmpPlay.setText("Pause");
				}
				else if(mPlayer.getStatus() == Status.PLAYING)
				{
					mPlayer.pause();
					tmpPlay.setText("Play");
				}
			});
			
			tmpLoad.setOnAction(e->{
				mPlayer = new MediaPlayer(new Media(new File(dest + "/" + tmpPlay.getId()).toURI().toString()));
				mPlayer.stop();
				System.out.println(mPlayer.getMedia().getSource());
				tmpPlay.setDisable(false);
			});
			System.out.println("j");
			tmpBox.getChildren().addAll(tmpText, tmpDwnld, tmpLoad, tmpPlay);
			System.out.println("k");
			this.getChildren().add(tmpBox);
			System.out.println("l");
		}
		this.getChildren().addAll(text, selectDestButton);
		System.out.println("m");
	}

	@Override
	public void update(Observable o, Object arg) 
	{
		System.out.println("Updating graphics...");
		drawFileButtons(mon.getNames());
		System.out.println("Where the graphics at?");
	}

}
