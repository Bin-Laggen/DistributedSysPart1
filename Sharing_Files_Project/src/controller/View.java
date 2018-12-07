package view;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import application.Client;
import controller.LocalMonitor;
import controller.Monitor;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class View extends GridPane implements Observer 
{
	private Monitor sharedMonitor;
	private LocalMonitor localMonitor;
	private Text dir;
	private DirectoryChooser newDir;
	private File dest;
	private File fileForUpload;
	private FileChooser fileChooser;
	private MediaPlayer mPlayer;
	private String[] localFiles;
	private VBox vBox1;
	private VBox vBox2;
	private VBox vBox3;
	private HBox optionButtons;
	private Label label1;
	private Label label2;
	private ArrayList<String> downloadedFiles;
	private ArrayList<Button> buttons;

	public View(Stage primaryStage, Client client)
	{
		sharedMonitor = Monitor.getInstance();
		sharedMonitor.addObserver(this);

		localMonitor = LocalMonitor.getInstance();
		localMonitor.addObserver(this);

		vBox1 = new VBox(5);
		vBox1.setMinWidth(500);

		newDir = new DirectoryChooser();
		newDir.setTitle("Select Location");

		fileChooser = new FileChooser();
		fileChooser.setTitle("Select File");

		dir = new Text();

		vBox2 = new VBox(5);
		vBox2.setMinWidth(500);

		vBox3 = new VBox(5);
		vBox3.setMinWidth(500);

		optionButtons = new HBox(10);

		Button selectDestBtm = new Button("Select Save Location");
		selectDestBtm.setStyle("-fx-font: 18 arial; -fx-base: #00cc00");
		selectDestBtm.setAlignment(Pos.CENTER);
		selectDestBtm.setOnAction(e->{
			dest = newDir.showDialog(primaryStage);
			if(dest != null)
			{
				dir.setText("\nSelected Location: " + dest.getPath());
				//sharedMonitor.threadStart();
				localMonitor.setLocalPath(dest.getPath());
				localMonitor.threadStart();
				displayDownloadButtons();
			}
		});

		Button uploadBtm = new Button("Upload to Server");
		uploadBtm.setStyle("-fx-font: 18 arial; -fx-base: #FF0000");
		uploadBtm.setMinWidth(100);
		uploadBtm.setOnAction(e->{
			fileForUpload = fileChooser.showOpenDialog(primaryStage);
			if(fileForUpload != null)
			{
				try 
				{
					sharedMonitor.copyFile(fileForUpload);
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
			}
		});

		optionButtons.getChildren().addAll(selectDestBtm,uploadBtm);
		vBox1.getChildren().addAll(optionButtons,dir);

		this.add(vBox1, 0, 0);
		this.add(vBox2, 0, 1);
		this.add(vBox3, 0, 2);

		primaryStage.setOnCloseRequest(e->{
			primaryStage.close();
			sharedMonitor.threadStop();
			localMonitor.threadStop();
			System.out.println("Threads STOP");
		});
	}

	public void displayDownloadButtons()
	{
		Platform.runLater(() -> vBox2.getChildren().clear());

		String[] file = sharedMonitor.getNames();

		buttons = new ArrayList<Button>();

		localFiles = dest.list();
		downloadedFiles = new ArrayList<String>(Arrays.asList(localFiles));

		label1 = new Label("Available for Download:");
		label1.setPrefWidth(350);
		label1.setStyle("-fx-font: 18 arial");
		label1.setAlignment(Pos.CENTER);
		Platform.runLater(() -> vBox2.getChildren().add(label1));

		for(int i = 0; i < file.length; i++)
		{
			HBox filesForDownload = new HBox(5);			

			Button btm = new Button(file[i]);
			btm.setPrefSize(500, 30);
			btm.setId(file[i]);

			boolean exists = false;

			for(int j = 0; j < localFiles.length; j++) // go through all the files
			{
				if(file[i].equals(localFiles[j])) // if the file is already in the local folder
				{
					exists = true;
				}
			}

			if(exists) // unable to download if file exists
			{
				btm.setDisable(true);
			}
			else
			{
				btm.setDisable(false);
			}

			btm.setOnAction(e->{
				try 
				{
					if(sharedMonitor.copyFile(new File(sharedMonitor.getServerPath() + "\\" + btm.getId())))
					{
						btm.setDisable(true);
						downloadedFiles.add(btm.getId());
						displayPlayButtons();
					}
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
			});

			filesForDownload.getChildren().add(btm);
			Platform.runLater(() -> vBox2.getChildren().add(filesForDownload));
		}

		displayPlayButtons();
	}

	private void displayPlayButtons() 
	{
		Platform.runLater(() -> vBox3.getChildren().clear());

		label2 = new Label("Downloaded:");
		label2.setStyle("-fx-font: 18 arial");
		label2.setAlignment(Pos.CENTER);
		Platform.runLater(() -> vBox3.getChildren().add(label2));

		for(int i = 0; i < downloadedFiles.size(); i++)
		{
			HBox downloaded = new HBox(5);

			Label text = new Label(downloadedFiles.get(i).toString());
			System.out.println("Downloaded: "+ downloadedFiles.get(i).toString());
			text.setPrefWidth(300);

			Button playBtm = new Button("Play \u23ef");
			playBtm.setStyle("-fx-font: 14 arial");
			playBtm.setId(downloadedFiles.get(i));
			playBtm.setMinWidth(100);

			buttons.add(playBtm);

			playBtm.setOnAction(e->{
				playBtm.setText("Pause \u23f8");
				playBtm.setStyle("-fx-font: 14 arial");

				for(Button b: buttons) // set all buttons to play except the one playing at the moment
				{
					if(!b.getId().equals(playBtm.getId())) 
					{
						b.setText("Play \u23ef");
					}
				}

				boolean ready = false;

				if(mPlayer == null)
				{
					mPlayer = new MediaPlayer(new Media(new File(dest + "/" + playBtm.getId()).toURI().toString()));
					System.out.println("Getting...: " + mPlayer.getMedia().getSource());
					System.out.println("Playing: " + playBtm.getId());
					ready = false;
				}
				else 
				{
					String nameURI = playBtm.getId().replaceAll(" ", "%20");

					if(!mPlayer.getMedia().getSource().endsWith(nameURI))
					{
						System.out.println("Getting...: " + mPlayer.getMedia().getSource());
						System.out.println("Playing: " + playBtm.getId());
						mPlayer.stop();
						mPlayer = new MediaPlayer(new Media(new File(dest + "/" + playBtm.getId()).toURI().toString()));
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
						public void run() 
						{
							player(playBtm);
						}
					});
					ready = true;
				}
				else
				{
					player(playBtm);
				}
			});

			downloaded.getChildren().addAll(text, playBtm);
			Platform.runLater(() -> vBox3.getChildren().add(downloaded));
		}
	}

	private void player(Button playBtm)
	{
		System.out.println("mPlayer "+mPlayer.getStatus());
		if(mPlayer.getStatus() == Status.READY || mPlayer.getStatus() == Status.STOPPED || mPlayer.getStatus() == Status.PAUSED)
		{
			mPlayer.play();	
			playBtm.setText("Pause \u23f8");
		}
		else if(mPlayer.getStatus() == Status.PLAYING)
		{								
			mPlayer.pause();
			playBtm.setText("Play \u23ef");
		}
	}

	@Override
	public void update(Observable arg0, Object arg1) 
	{
		displayDownloadButtons();	
	}
}

