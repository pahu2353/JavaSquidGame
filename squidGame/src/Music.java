/* 
 * Music.java
 * November 8, 2021
 * Plays music in the game
 */

import java.io.File;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Music {

	/*
	 * playMusic 
	 * input: name of the music file, which is edited to include a variety of songs from Squid Game
	 * purpose: plays the music
	 */

	public static void playMusic(String name) {
	
		try {
			
			// gets music file from audio folder
			URL music = ClassLoader.getSystemResource("audio/" + name + ".wav");
	
			// starts and plays music
			AudioInputStream stream = AudioSystem.getAudioInputStream(music);
			Clip sound = AudioSystem.getClip();
			sound.open(stream);
			sound.start();
			sound.loop(Clip.LOOP_CONTINUOUSLY); // loops music forever

		} catch (Exception ex) {
			// gets exact error message; can be used for testing
		} // try-catch
	} // playMusic

} // Music