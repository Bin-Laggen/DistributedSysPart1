package view;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import application.Client;
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
	private Client client;
	private VBox serverBox;
	private VBox localBox;
	
	private String[] lFiles;
	private String[] sFiles;
	private ArrayList<String> serverFiles;

	private final String GET_FILE_LIST = "1";
	private final String DOWNLOAD_FILE = "2";
	private final String UPLOAD_FILE = "3";
	private final String EXIT = "5";
	
	public ServerView(Stage primaryStage, Client client)
	{
		this.client = client;
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
		
		serverBox = new VBox(5);
		serverBox.setMinWidth(600);
		
		localBox = new VBox(5);
		localBox.setMinWidth(600);
		
		this.setMinWidth(600);
		this.setSpacing(5);
		
		this.getChildren().add(selectDestButton);		
		
		selectDestButton.setOnAction(e->{
			dest = dirChooser.showDialog(primaryStage);
			text.setText(output);
			if(dest != null)
			{
				text.setText(text.getText() + "\nSelected Location: " + dest.getPath());
				mon.setPath(dest.getPath());
				mon.threadStart();
				lFiles = mon.getNames();
				client.sendCommand(GET_FILE_LIST, null);
				this.getChildren().clear();
				this.getChildren().addAll(serverBox, localBox);
				sFiles = client.getServerFiles();
				drawServerFiles(sFiles);
				drawLocalFiles(mon.getNames());
				client.threadStart();
			}
		});
		
		uploadButton.setOnAction(e->{
			uploadFile = fileChooser.showOpenDialog(primaryStage);
			if(uploadFile != null)
			{
				client.sendCommand(UPLOAD_FILE, uploadFile);
			}
		});
		
		primaryStage.setOnCloseRequest(e->{
			mon.threadStop();
			client.sendCommand(EXIT, null);
			client.threadStop();
		});
	}
	
	public void drawServerFiles(String[] names)
	{		
		serverFiles = new ArrayList<String>();
		
		Platform.runLater(() -> serverBox.getChildren().clear());
		for(int i = 0; i < names.length; i++)
		{
			boolean found = false;
			for(int j = 0; j < lFiles.length; j++)
			{
				if(names[i].equals(lFiles[j]))
				{
					found = true;
				}
			}
			if(!found)
			{
				serverFiles.add(names[i]);
			}
		}
		for(int k = 0; k < serverFiles.size(); k++)
		{
			HBox tmpBox = new HBox(5);
			Label tmpText = new Label(serverFiles.get(k));
			tmpText.setPrefWidth(300);
			Button tmpDwnld = new Button("Download");
			tmpDwnld.setId(serverFiles.get(k));
			tmpDwnld.setMinWidth(50);
			
			tmpDwnld.setOnAction(e->{
				System.out.println("DOWNLOAD: " + tmpDwnld.getId());
				if(client.sendCommand(DOWNLOAD_FILE, tmpDwnld.getId()))
				{
					tmpDwnld.setDisable(true);
				}
			});
			
			tmpBox.getChildren().addAll(tmpText, tmpDwnld);
			Platform.runLater(() -> serverBox.getChildren().add(tmpBox));
		}
	}	

	private void drawLocalFiles(String[] names) {
		
		buttons = new ArrayList<Button>();
		
		Platform.runLater(() -> localBox.getChildren().clear());
		for(int i = 0; i < names.length; i++)
		{
			boolean found = false;
			for(int j = 0; j < sFiles.length; j++)
			{
				if(names[i].equals(sFiles[j]))
				{
					found = true;
				}
			}
			HBox tmpBox = new HBox(5);
			Label tmpText = new Label(names[i]);
			tmpText.setPrefWidth(300);
			Button tmpUpld = new Button("Upload");
			tmpUpld.setId(names[i]);
			tmpUpld.setMinWidth(50);
			Button tmpPlay = new Button("Play");
			tmpPlay.setId(names[i]);
			tmpPlay.setMinWidth(30);
			buttons.add(tmpPlay);
			
			if(found)
			{
				tmpUpld.setDisable(true);
			}
			else
			{
				tmpUpld.setDisable(false);
			}

			tmpUpld.setOnAction(e->{
				client.sendCommand(UPLOAD_FILE, new File(mon.getPath() + "\\" + tmpUpld.getId()));
				tmpUpld.setDisable(true);
			});
			
			tmpPlay.setOnAction(e->{
				playAction(tmpPlay);
			});
			
			tmpBox.getChildren().addAll(tmpText, tmpUpld, tmpPlay);
			Platform.runLater(() -> localBox.getChildren().add(tmpBox));
		}
		Platform.runLater(() -> localBox.getChildren().addAll(text, uploadButton));
	}

	private void playAction(Button tmpPlay)
	{
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
	}
	
	@Override
	public void update(Observable o, Object arg) 
	{
		System.out.println("Updating graphics...");
		sFiles = client.getServerFiles();
		lFiles = mon.getNames();
		drawServerFiles(sFiles);
		drawLocalFiles(lFiles);
		System.out.println("Where the graphics at?");
	}


}
