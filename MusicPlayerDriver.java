
/**
This class takes in a absolute path and creates a music player from the music in that directory, with options such as stop, play, list songs by artists and titles.
@author Candice Ninan
**/
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class MusicPlayerDriver {

	public static void main(
			String args[]) {
		
		if(args.length != 1) { 
			  System.out.println("Usage: java -cp '.;jaudiotagger-2.2.6-SNAPSHOT.jar;jl1.0.1.jar;' <absolutepath>"
		  ); System.exit(1); 
		}

		String filePath =  args[0];
		MusicPlayerOptions musicObject = new MusicPlayerOptions();
		ArrayList<String> fileNamesList = musicObject
				.findMP3Songs(filePath);
		while (true) {
			System.out.println("Menu:");
			System.out.println(
					"Choose 1 to list by title");
			System.out.println(
					"Choose 2 to list by artist");
			System.out.println(
					"Choose 3 to play a song");
			System.out.println(
					"Choose 4 to stop playing the song");
			System.out.println(
					"Choose 5 to exit");
			System.out.println(
					"Choose an option:");
			try {
				int option = Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());
				if (option == 1) {
					musicObject.listByTitle(fileNamesList);
				} else if (option == 2) {
					musicObject.listByArtist(fileNamesList);
				} else if (option == 3) {
					musicObject.playSong(fileNamesList);
				} else if (option == 4) {
					musicObject.stopSong();
				} else if (option == 5) {
					System.exit(0);
				}
			} catch (
					NumberFormatException
					| IOException e) {
				e.printStackTrace();
			}

		}
	}

}

//SongRecord holds the song name and the artist name for a song
class SongRecord {
	private String artistName;
	private String songName;

	public SongRecord(String artistName, String songName) {
		this.artistName = artistName;
		this.songName = songName;
	}

	public String getSongName() {
		//return song name of the song
		return songName;
	}

	public String getArtistName() {
		//return Artist name of the song
		return artistName;
	}

	public static Comparator<SongRecord> ArtistNameComparator = new Comparator<SongRecord>() {
		//comparator method to sort the songs based on the artistname
		public int compare(SongRecord songRecord1, SongRecord songRecord2) {
			return songRecord1.artistName.compareTo(songRecord2.artistName);
		}
	};
}


class MusicPlayerOptions{
	Player myCurrentPlayer;
	Thread myThread;

	public void stopSong() {
		//method to stop the currently playing song
		if (myCurrentPlayer != null) {
			myCurrentPlayer.close();
		}
	}

	public void playSong(
			ArrayList<String> fileNamesList) {
	//Shows a list of the available songs and pay the selected song
		try {
			System.out.println(
					"Please choose a song number:");
			int option;
			while (true) {
				for (int i = 0; i < fileNamesList
						.size(); i++) {
					System.out.println(
							(i + 1) + ". Name: "
									+ new File(fileNamesList.get(i)).getName());
				}
				option = Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());
				if (option < 1| option > fileNamesList.size())
					System.out.println("Please choose a valid song number:");
				else
					break;
			}

			String songPath = fileNamesList.get(option - 1);
			if (myCurrentPlayer != null)
				myCurrentPlayer.close();

			myCurrentPlayer = new Player(new FileInputStream(songPath));

			myThread = new Thread() {
				public void run() {
					try {
						myCurrentPlayer.play();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			myThread.start();

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (JavaLayerException
				| NumberFormatException
				| IOException
				| IndexOutOfBoundsException e) {
			e.printStackTrace();
		}

	}

	public void listByArtist(ArrayList<String> fileNamesList) {
		//a method to list the artists sorted in the alphabetical order based on artist name
		System.out.println("Listing by Artist: \n\n");

		ArrayList<SongRecord> sortedByArtist = new ArrayList<SongRecord>();

		for (int index = 0; index < fileNamesList.size(); index++) {
			sortedByArtist.add(new SongRecord(getSongArtist(fileNamesList.get(index)), new File(fileNamesList.get(index)).getName()));
		}
		Collections.sort(sortedByArtist, SongRecord.ArtistNameComparator);
		for (SongRecord songRecord : sortedByArtist) {
			System.out.println("Artist: "
							+ songRecord.getArtistName()
							+ " Name: "
							+ songRecord.getSongName());
		}

	}

	public String getSongArtist(String songPath) {
		//get the artistname for a song using JAudioTagger
		AudioFile file = null;
		try {
			file = AudioFileIO.read(
					new File(songPath));
		} catch (CannotReadException
				| IOException
				| TagException
				| ReadOnlyFileException
				| InvalidAudioFrameException e) {
			e.printStackTrace();
		}
		return file.getTag().getFirst(FieldKey.ARTIST);
		// return file.getTag().getFirst(FieldKey.COMPOSER);
	}

	public String getSongTitle(String songPath) {
		//get the title of the song using JAudiotagger
		AudioFile file = null;
		try {
			file = AudioFileIO.read(new File(songPath));
		} catch (CannotReadException
				| IOException
				| TagException
				| ReadOnlyFileException
				| InvalidAudioFrameException e) {
			e.printStackTrace();
		}
		return file.getTag().getFirst(FieldKey.TITLE);
	}

	public void listByTitle(ArrayList<String> fileNamesList) {
		//a method to list the artists sorted in the alphabetical order based on song title
		System.out.println("Listing by Song Title: \n\n");

		ArrayList<String> sortedByTitle = new ArrayList<String>();
		for (int index = 0; index < fileNamesList.size(); index++) {
			sortedByTitle.add(getSongTitle(fileNamesList.get(index)));
		}
		Collections.sort(sortedByTitle);
		for (int i = 0; i < sortedByTitle.size(); i++) {
			System.out.println("Name: "
					+ sortedByTitle.get(i));
		}

	}

	public void getSongs(File[] listofFiles, ArrayList<String> fileNamesList) {
		//recursive helper method to populate the songs
		for (File file : listofFiles) {
			if (file.isDirectory()) {
				File folder = new File(file.getPath());
				File[] listofFile = folder.listFiles();
				getSongs(listofFile,fileNamesList);
			} else {
				String fileName = file.getAbsolutePath();
				if (fileName.endsWith(".mp3") == true) {fileNamesList.add(fileName);
				}
			}
		}
	}

	public ArrayList<String> findMP3Songs(String filePath) {
		//find mp3 files located at a filepath
		System.out.println("Finding mp3s: ");

		File folder = new File(filePath);

		File[] listofFiles = folder.listFiles();

		ArrayList<String> fileNamesList = new ArrayList<String>();

		getSongs(listofFiles,fileNamesList);

		return fileNamesList;
	}
	
}
