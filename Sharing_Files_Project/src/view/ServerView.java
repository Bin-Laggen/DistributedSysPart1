package view;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import controller.Monitor;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ServerView extends VBox implements Observer {
	
	private Text text;
	private Button selectDestButton;
	private String output = "";
	private Monitor mon;
	private DirectoryChooser dirChooser;
	private FileChooser fileChooser;
	private File dest;
	private File uploadFile;
	private MediaPlayer mPlayer;
	private Button uploadButton;
	private ArrayList<Button> buttons;
	
	public ServerView(Stage primaryStage)
	{
		selectDestButton = new Button("Select Local Folder");
		uploadButton = new Button("Select a file for upload");
		uploadButton.setMinWidth(100);
		text = new Text(output);
		dirChooser = new DirectoryChooser();
		dirChooser.setTitle("Select Location");
		fileChooser = new FileChooser();
		fileChooser.setTitle("Select File");
		mon = Monitor.getInstance();
		mon.addObserver(this);
		this.setMinWidth(600);
		this.setSpacing(5);
		
		this.getChildren().add(selectDestButton);		
		
		selectDestButton.setOnAction(e->{
			dest = dirChooser.showDialog(primaryStage);
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
		
		uploadButton.setOnAction(e->{
			uploadFile = fileChooser.showOpenDialog(primaryStage);
			if(uploadFile != null)
			{
				try 
				{
					mon.copyFile(uploadFile, new File(mon.getServerPath()));
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
			}
		});
		
		primaryStage.setOnCloseRequest(e->{
			mon.threadStop();
		});
	}
	
	public void drawFileButtons(String[] names)
	{
		buttons = new ArrayList<Button>();
		Platform.runLater(() -> this.getChildren().clear());
		for(int i = 0; i < names.length; i++)
		{
			boolean found = false;
			String[] localFiles = dest.list(new FilenameFilter() {
			    @Override
			    public boolean accept(File dir, String name) 
			    {
			        return name.endsWith(".mp3");
			    } 
			});
			for(int j = 0; j < localFiles.length; j++)
			{
				if(names[i].equals(localFiles[j]))
				{
					found = true;
				}
			}
			HBox tmpBox = new HBox(5);
			Label tmpText = new Label(names[i]);
			tmpText.setPrefWidth(300);
			Button tmpDwnld = new Button("Download");
			tmpDwnld.setId(names[i]);
			tmpDwnld.setMinWidth(50);
			Button tmpPlay = new Button("Play");
			tmpPlay.setId(names[i]);
			tmpPlay.setMinWidth(30);
			buttons.add(tmpPlay);
			
			if(found)
			{
				tmpDwnld.setDisable(true);
				tmpPlay.setDisable(false);
			}
			else
			{
				tmpDwnld.setDisable(false);
				tmpPlay.setDisable(true);
			}

			tmpDwnld.setOnAction(e->{
				try 
				{
					if(mon.copyFile(new File(mon.getServerPath() + "\\" + tmpDwnld.getId()), dest))
					{
						tmpDwnld.setDisable(true);
						tmpPlay.setDisable(false);
					}
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
			});
			
			tmpPlay.setOnAction(e->{
				for(int j = 0; j < buttons.size(); j++)
				{
					if(!buttons.get(j).getId().equals(tmpPlay.getId()))
					{
						buttons.get(j).setText("Play");
					}
				}
				boolean ready = false;
				if(mPlayer == null)
				{
					mPlayer = new MediaPlayer(new Media(new File(dest + "/" + tmpPlay.getId()).toURI().toString()));
					System.out.println("if");
					ready = false;
				}
				else 
				{
					String nameURI = tmpPlay.getId().replaceAll(" ", "%20");
					if(!mPlayer.getMedia().getSource().endsWith(nameURI))
					{
						System.out.println("mPlayer...: " + mPlayer.getMedia().getSource());
						System.out.println("tmpPlay.getId(): " + tmpPlay.getId());
						System.out.println("elif");
						mPlayer.stop();
						mPlayer = new MediaPlayer(new Media(new File(dest + "/" + tmpPlay.getId()).toURI().toString()));
						ready = false;
					}
					else
					{
						ready = true;
					}
				}
				if(!ready)
				{
					mPlayer.setOnReady(new Runnable() {
						@Override
						public void run() {
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
						}
						
					});
					ready = true;
				}
				else
				{
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
				}

			});
			
			tmpBox.getChildren().addAll(tmpText, tmpDwnld, tmpPlay);
			Platform.runLater(() -> this.getChildren().add(tmpBox));
		}
		Platform.runLater(() -> this.getChildren().addAll(text, uploadButton));
	}

	@Override
	public void update(Observable o, Object arg) 
	{
		System.out.println("Updating graphics...");
		drawFileButtons(mon.getNames());
		System.out.println("Where the graphics at?");
	}

}
