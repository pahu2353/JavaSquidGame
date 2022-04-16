/**********************************************************
 * Program: Squid Game
 * Authors: Nick Sima, Patrick Huang, Craig Gao
 * Date: November 8, 2021
 * Purpose: A game that aims to recreate 4 games or events 
 * from the popular Netflix series "Squid Game"
 * Note: No copyright was intended from this remake
 * of Squid Game. We do not claim to take credit
 * for anything related to Squid Game. 
 *********************************************************/


/* 
 * Game.java
 * November 8, 2021
 * Squid Game Main Program
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Game extends Canvas {

	private Graphics2D g;

	private BufferStrategy strategy; // take advantage of accelerated graphics
	public static boolean waitingForKeyPress = true; // true if game held up until
													 // a key is pressed
	
	private boolean leftPressed = false; 	// true if left arrow key currently pressed
	private boolean rightPressed = false;   // true if right arrow key currently pressed
	private boolean upPressed = false; 		// true if up arrow key currently pressed
	private boolean downPressed = false; 	// true if down arrow key currently pressed
	private boolean[] buttonPressed = new boolean[26]; // and index is true if a letter on the alphabet is pressed
	private boolean playerCanMove = true; 	// true if the player can move
	
	private int seconds = 0; // timer for both game 1 and 3
	long lastTime = System.currentTimeMillis(); // last loop time
	private boolean gameRunning = true;
	private int numberOfPlayers = 100; // number of players currently alive

	
	private Entity introScreen; // the first screen shown
	private boolean introScreenVisible = true; // true if the intro screen is being shown

	private String screenImage = ""; // the instruction screen that will be shown
	
	protected boolean lightIsGreen = true; // variable for the colour of the light
	
	private int pullKey = 0; // the key that needs to be pressed in game 2
	private int playerScore = 0; // the score of the player's team in game 2
	private int enemyScore = 0;  // the score of the opposing team in game 2
								// which team pulls harder is decided by the scores of both teams
	private long lastScore = System.currentTimeMillis(); // last time a score was added for the player's team
	
	private Entity foreground; // represents the flashing light in game 3
	
	private int squidgameStage = 1; // the stage of the fourth game

	private ArrayList entities = new ArrayList(); // list of entities
												  // in game
	private ArrayList removeEntities = new ArrayList(); // list of entities
														// to remove this loop
	
	ParticipantEntity[] participantArray = new ParticipantEntity[100]; // array of all the participants
	
	private Entity player; // the player
	private Entity doll;   // the doll in game 1
	private Entity light;  // the light in game 1
	private Entity rope;   // the rope in game 2
	
	private double moveSpeed = 500; // horizontal vel. of player (px/s)
	private double moveSpeedVert = 500; // vert. vel. of player (px/s)
	
	public int canvasY = 1080; // window height of the game
	public int canvasX = 1920; // window length of the game
	
	
	private int gameNumber = 1; // game currently being played

	/*
	 * Construct our game and set it running.
	 */
	public Game() {

		// create a frame to contain game
		JFrame container = new JFrame("Squid Game");

		// get hold the content of the frame
		JPanel panel = (JPanel) container.getContentPane();

		// set up the resolution of the game
		panel.setPreferredSize(new Dimension(canvasX, canvasY));
		panel.setLayout(null);

		// set up canvas size (this) and add to frame
		setBounds(0, 0, canvasX, canvasY);
		panel.add(this);

		// Tell AWT not to bother repainting canvas since that will
		// be done using graphics acceleration
		setIgnoreRepaint(true);

		// make the window visible
		container.pack();
		container.setResizable(false);
		container.setVisible(true);

		tryToSwitch();
		setKey();
		scoreEnemy();

		// if user closes window, shutdown game and jre
		container.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			} // windowClosing
		});

		// add key listener to this canvas
		addKeyListener(new KeyInputHandler());

		// request focus so key events are handled by this canvas
		requestFocus();

		// create buffer strategy to take advantage of accelerated graphics
		createBufferStrategy(2);
		strategy = getBufferStrategy();

		// initialize entities
		initEntities();
		animatePlayer();
		
		// set timer for first game
		seconds = 70;
		
		// set first instruction screen
		screenImage = "sprites/game1screen.jpg";

		// start the game
		gameLoop();
	} // constructor

	/*
	 * initEntities input: none 
	 * output: none 
	 * purpose: initialize the starting state
	 * of the player and participant entities. Each entity will be added to the
	 * array of entities in the game.
	 */
	private void initEntities() {

		if (gameNumber == 1) {

			doll = new MiscEntity(this, "sprites/doll.png", canvasX / 2, 0);
			entities.add(doll);

			light = new MiscEntity(this, "sprites/green.png", 0, 0);
			entities.add(light);

			// initializes all participants in game 1
			for (int i = 0; i < 100; i++) {
				participantArray[i] = new ParticipantEntity(this, "sprites/partup2.png", (canvasX / 100) * i,
						canvasY - 50);
				entities.add(participantArray[i]);
			} // for

			player = new PlayerEntity(this, "sprites/playerup2.png", canvasX / 2 - 40, canvasY - 50);
			entities.add(player);

		} else if (gameNumber == 2) {
			
			rope = new MiscEntity(this, "sprites/rope.jpg", 480, 360);
			entities.add(rope);
			
			player = new PlayerEntity(this, "sprites/playerpullright.png", 500, 350);
			entities.add(player);

			// initializes the participants on the player's team
			for (int i = 0; i < 4; i++) {
				participantArray[i] = new ParticipantEntity(this, "sprites/partpullright.png", 500 + (i + 1) * 50, 350);
				entities.add(participantArray[i]);
			} // for
			
			// initializes the participants on the opposing team
			for (int i = 0; i < 5; i++) {
				participantArray[8 - i] = new ParticipantEntity(this, "sprites/partpullleft.png",
						canvasX - (500 + (i) * 50), 350);
				entities.add(participantArray[8 - i]);
			} // for

		} else if (gameNumber == 3) {

			player = new PlayerEntity(this, "sprites/playerup2.png", canvasX / 2 - 40, canvasY - 50);
			entities.add(player);

			// initializes the remaing 25 players
			for (int i = 0; i < 25; i++) {
				participantArray[i] = new ParticipantEntity(this, "sprites/partup2.png",
						200 + (int) (Math.random() * ((canvasX - 200) + 1)),
						50 + (int) (Math.random() * ((canvasY - 50) + 1)));
				entities.add(participantArray[i]);

			} // for
			
		} else if (gameNumber == 4) {

			player = new PlayerEntity(this, "sprites/playerup2.png", (canvasX / 2) - 40, 950);
			entities.add(player);

			// initializes the 3 defenders
			participantArray[0] = new ParticipantEntity(this, "sprites/partup2.png", (canvasX / 2) - 40, 875);
			entities.add(participantArray[0]);
			participantArray[1] = new ParticipantEntity(this, "sprites/partup2.png", (canvasX / 2) - 100, 875);
			entities.add(participantArray[1]);
			participantArray[2] = new ParticipantEntity(this, "sprites/partup2.png", (canvasX / 2) + 20, 875);
			entities.add(participantArray[2]);

		} // else if

	} // initEntities

	
	// Remove an entity from the game. It will no longer be moved or drawn.
	public void removeEntity(Entity entity) {
		removeEntities.add(entity);
	} // removeEntity

	
	// Notification that the player has died.
	public void notifyDeath() {
		
		screenImage = "sprites/deathscreen.jpg";
		waitingForKeyPress = true;
		
		if (gameNumber == 4) {
			squidgameStage = 1;
		} // if

	} // notifyDeath

	// Notification that the player has won the game
	public void notifyWin() {

		if (gameNumber == 1) {
			screenImage = "sprites/game2screen.jpg";
			waitingForKeyPress = true;
			gameNumber++;
			return;

		} else if (gameNumber == 2) {
			screenImage = "sprites/game3screen.jpg";
			waitingForKeyPress = true;
			gameNumber++;
			return;

		} else if (gameNumber == 3) {
			screenImage = "sprites/game4screen.jpg";
			waitingForKeyPress = true;
			gameNumber++;
			return;

		} else if (gameNumber == 4) {
			screenImage = "sprites/winscreen.jpg";
			waitingForKeyPress = true;
			gameNumber++;
			return;

		} // else if

	} // notifyWin

	
	/*
	 * countdown input: none 
	 * output: none 
	 * purpose: continuously decrease the timer in games 1 and 3
	 * also continuously intialize and remove the foreground in game 3
	 */
	public void countdown() {

		if (gameNumber == 1) {

			// decrease timer by 1 second if 1 seond has passed since last decrease
			if (System.currentTimeMillis() > lastTime + 1000 && seconds > 0 && !waitingForKeyPress) {
				lastTime = System.currentTimeMillis();
				seconds -= 1;

			} // if
		} else if (gameNumber == 3) {

			// decrease timer by 1 second if 1 seond has passed since last decrease
			if (System.currentTimeMillis() > lastTime + 1000 && seconds > 0 && !waitingForKeyPress) {
				lastTime = System.currentTimeMillis();
				seconds -= 1;
				
				// initialize and remove foreground entity
				if (seconds % 2 == 0) {
					foreground = new ForegroundEntity(this, "sprites/black.jpg", 0, 0);
					entities.add(foreground);
					// System.out.println("flash!");
				} else {
					removeEntity(foreground);
				} // else 
			} // if
		} // else if

	} // countdown

	/*
	 * tryToSwitch input: none 
	 * output: none 
	 * purpose: continuously changes the colour of the light in game 1
	 * also changes the intelligence of a random number of participants each switch
	 */
	public void tryToSwitch() {

		Timer timer = new Timer();
		TimerTask task = new TimerTask() { // the task to be run

			public void run() {
				if (gameNumber == 1) {

					// if game is not being played light stays green
					if (waitingForKeyPress) {
						lightIsGreen = true;
					} // if

					
					if (!waitingForKeyPress) {
						lightIsGreen = !lightIsGreen; // change colour of light
						if (lightIsGreen) {
							light.setSprite("sprites/green.png");

						} else {
							light.setSprite("sprites/red.png");
						} // else

						int rand = (int) (Math.random() * 15) + 15; // random number that decides how many participants change intelligence

						for (int i = 0; i < 100; i++) {

							if (i % rand == 0 && numberOfPlayers > 49) {
								participantArray[i].changeIntelligence(); // change the intelligence
							} // if

							// if participant and thus moves while light is red, eliminate them
							if (!lightIsGreen && participantArray[i].getIsStupid()) {
								removeEntity(participantArray[i]);
								numberOfPlayers--;

							} // if

						} // for

					} // if

				} // if
			} // run

		};

		timer.scheduleAtFixedRate(task, 5000, 5000); // waits 5 seconds, then runs this task every 5 seconds
	} // tryToSwitch

	
	/*
	 * animatePlayer input: none 
	 * output: none 
	 * purpose: animate the sprite of the player during movement
	 */
	public void animatePlayer() {

		Timer timer = new Timer();
		TimerTask task = new TimerTask() { // the task to be run

			int imageNum = 1; // decides which sprite the entity will switch to

			public void run() {
				
				if (gameNumber != 2) { // no movement animation in game 2

					if (upPressed) { // upwards movement
	
						if (imageNum == 1) {
							player.setSprite("sprites/playerup1.png");
							imageNum++;
						} else if (imageNum == 2) {
							player.setSprite("sprites/playerup2.png");
							imageNum++;
						} else if (imageNum == 3) {
							player.setSprite("sprites/playerup3.png");
							imageNum = 1;
	
						} // else if
	
					} else if (leftPressed) { // leftwards movement
	
						if (imageNum == 1) {
							player.setSprite("sprites/playerleft1.png");
							imageNum++;
						} else if (imageNum == 2) {
							player.setSprite("sprites/playerleft2.png");
							imageNum++;
						} else if (imageNum == 3) {
							player.setSprite("sprites/playerleft3.png");
							imageNum = 1;
	
						} // else if
	
					} else if (rightPressed) { // rightwards movement
	
						if (imageNum == 1) {
							player.setSprite("sprites/playerright1.png");
							imageNum++;
						} else if (imageNum == 2) {
							player.setSprite("sprites/playerright2.png");
							imageNum++;
						} else if (imageNum == 3) {
							player.setSprite("sprites/playerright3.png");
							imageNum = 1;
	
						} // else if
	
					} else if (downPressed) { // downwards movement
	
						if (imageNum == 1) {
							player.setSprite("sprites/playerdown1.png");
							imageNum++;
						} else if (imageNum == 2) {
							player.setSprite("sprites/playerdown2.png");
							imageNum++;
						} else if (imageNum == 3) {
							player.setSprite("sprites/playerdown3.png");
							imageNum = 1;
	
						} // else if
	
					} // else if
	
				} // if
				
			} // run

		};

		timer.scheduleAtFixedRate(task, 300, 300); // waits 300 ms, then runs this task task every 300 ms
	} // animatePlayer

	/*
	 * animateParticiapant input: none 
	 * output: none 
	 * purpose: animate the sprite of a particiapnt during movement
	 */
	
	public void animateParticipant(int i) {

		Timer timer = new Timer();
		TimerTask task = new TimerTask() { // the task to be run

			int imageNum = 1;  // decides which sprite the entity will switch to

			public void run() {

				if (gameNumber != 2) { // no movement animation in game 2

					if (participantArray[i].getVerticalMovement() < 0) { // upwards movement

						if (imageNum == 1) {
							participantArray[i].setSprite("sprites/partup1.png");
							imageNum++;
						} else if (imageNum == 2) {
							participantArray[i].setSprite("sprites/partup2.png");
							imageNum++;
						} else if (imageNum == 3) {
							participantArray[i].setSprite("sprites/partup3.png");
							imageNum = 1;

						} // else if

					} else if (participantArray[i].getHorizontalMovement() < 0) { // leftwards movement

						if (imageNum == 1) {
							participantArray[i].setSprite("sprites/partleft1.png");
							imageNum++;
						} else if (imageNum == 2) {
							participantArray[i].setSprite("sprites/partleft2.png");
							imageNum++;
						} else if (imageNum == 3) {
							participantArray[i].setSprite("sprites/partleft3.png");
							imageNum = 1;

						} // else if

					} else if (participantArray[i].getHorizontalMovement() > 0) { // rightwards movement

						if (imageNum == 1) {
							participantArray[i].setSprite("sprites/partright1.png");
							imageNum++;
						} else if (imageNum == 2) {
							participantArray[i].setSprite("sprites/partright2.png");
							imageNum++;
						} else if (imageNum == 3) {
							participantArray[i].setSprite("sprites/partright3.png");
							imageNum = 1;

						} // else if

					} else if (participantArray[i].getVerticalMovement() > 0) { // downwards movement

						if (imageNum == 1) {
							participantArray[i].setSprite("sprites/partdown1.png");
							imageNum++;
						} else if (imageNum == 2) {
							participantArray[i].setSprite("sprites/partdown2.png");
							imageNum++;
						} else if (imageNum == 3) {
							participantArray[i].setSprite("sprites/partdown3.png");
							imageNum = 1;

						} // else if

					} // else if

				} // if

			} // run

		};

		timer.scheduleAtFixedRate(task, 300, 300); // waits 300 ms, then runs this task every 300 ms
	}// animateParticipant

	
	/*
	 * gameLoop input: none output: none purpose: Main game loop. Runs throughout
	 * game play. Responsible for the following activities: - calculates speed of
	 * the game loop to update moves - moves the game entities - draws the screen
	 * contents (entities, text) - updates game events - checks input
	 */
	public void gameLoop() {

		if (gameNumber == 1) {

			// animate every participant
			for (int i = 0; i < 100; i++) {
				animateParticipant(i);
			} // for

			long lastLoopTime = System.currentTimeMillis(); // the time of the last loop

			// keep loop running until game ends
			while (gameRunning) {

				countdown(); // counts down the timer of game 1

				// calc. time since last update, will be used to calculate
				// entities movement
				long delta = System.currentTimeMillis() - lastLoopTime;
				lastLoopTime = System.currentTimeMillis();

				// get graphics
				g = (Graphics2D) strategy.getDrawGraphics();
				g.setFont(new Font("default", Font.BOLD, 16)); // set the font
				
				// get the background image
				Toolkit tk = Toolkit.getDefaultToolkit();
				Image Pic;
				URL url = Game.class.getResource("sprites/background1.jpg");
				Pic = tk.getImage(url);
				g.drawImage(Pic, 0, 0, this); // draw the background

				// move each entity
				if (!waitingForKeyPress) {
					for (int i = 0; i < entities.size(); i++) {
						Entity entity = (Entity) entities.get(i);
						entity.move(delta);
					} // for
				} // if

				// draw all entities
				for (int i = 0; i < entities.size(); i++) {
					Entity entity = (Entity) entities.get(i);
					entity.draw(g);
				} // for

				// remove dead entities
				entities.removeAll(removeEntities);
				removeEntities.clear();

				// draw intro screen
				if (introScreenVisible) {
					URL backgroundURL = Game.class.getResource("sprites/introscreen.jpg");
					Pic = tk.getImage(backgroundURL);
					g.drawImage(Pic, 0, 0, this);
				} // if

				// if waitingForKeypress and introScreen is not being shown, draw instruction screen
				if (waitingForKeyPress && !introScreenVisible) {
					URL backgroundURL = Game.class.getResource(screenImage);
					Pic = tk.getImage(backgroundURL);
					g.drawImage(Pic, 0, 0, this);

				} // if

				// draw timer
				if (!waitingForKeyPress) {
					g.setColor(Color.white);
					g.drawString(seconds + " seconds left", canvasX / 2 - 30, 100);
				} // if

				// clear graphics and flip buffer
				g.dispose();
				strategy.show();

				// player should not move without user input
				player.setHorizontalMovement(0);
				player.setVerticalMovement(0);

				// respond to user moving player
				movePlayer();
				
				// if player is moving while light red, they lose
				if ((leftPressed || rightPressed || upPressed || downPressed) && !lightIsGreen) {
					notifyDeath();
				}// if
				
				// if player reaches the end of the field they win
				if (player.y < 50) {
					notifyWin();
					break;
				} // if

				// if player runs out of time, they lose
				if (seconds == 0) {
					notifyDeath();
				} // if

			} // while

		} // if

		if (gameNumber == 2) {
			long lastLoopTime = System.currentTimeMillis(); // the time of the last loop

			// keep loop running until game ends
			while (gameRunning) {

				// calc. time since last update, will be used to calculate
				// entities movement
				long delta = System.currentTimeMillis() - lastLoopTime;
				lastLoopTime = System.currentTimeMillis();

				// get graphics
				g = (Graphics2D) strategy.getDrawGraphics();
				g.setFont(new Font("default", Font.BOLD, 16)); // set the font
				
				// get the background image
				Toolkit tk = Toolkit.getDefaultToolkit();
				Image Pic;
				URL url = Game.class.getResource("sprites/background2.jpg");
				Pic = tk.getImage(url);
				g.drawImage(Pic, 0, 0, this); // draw the background

				// tell player what key to press
				g.setColor(Color.white);
				if (!waitingForKeyPress) {
					g.drawString("Press \"" + (char) (pullKey + 97) + "\" to pull!", canvasX / 2 - 65, 200);

				}// if

				// move all entities
				if (!waitingForKeyPress) {
					for (int i = 0; i < entities.size(); i++) {
						Entity entity = (Entity) entities.get(i);
						entity.move(delta);
					} // for
				} // if

				// draw all entities
				for (int i = 0; i < entities.size(); i++) {
					Entity entity = (Entity) entities.get(i);
					entity.draw(g);
				} // for

				//draw instruction screen
				if (waitingForKeyPress) {
					URL backgroundURL = Game.class.getResource(screenImage);
					Pic = tk.getImage(backgroundURL);
					g.drawImage(Pic, 0, 0, this);

				} // if

				// clear graphics and flip buffer
				g.dispose();
				strategy.show();

				// player scores if they press the right key
				if (buttonPressed[pullKey]) {
					tryToScore();
					buttonPressed[pullKey] = false;
				} // if


				//if player score is higher than opposing score, player is pulling harder
				if (playerScore > enemyScore) {
					player.setHorizontalMovement(-20);
					rope.setHorizontalMovement(-20);
					for (int i = 0; i < 9; i++) {
						participantArray[i].setHorizontalMovement(-20);
					} // for

				// if player score is lower than opposing score, enemy is pulling HARDER
				} else if (playerScore < enemyScore) {
					player.setHorizontalMovement(20);
					try {
						rope.setHorizontalMovement(20);
					} catch (Exception e) {
						// allow error testing
					} // catch
					
					for (int i = 0; i < 9; i++) {
						participantArray[i].setHorizontalMovement(20);
					} // for

				} // else if

				if (player.getX() == 370) {
					notifyWin();
					break;
				} else if (participantArray[8].getX() == (canvasX - 390)) {
					notifyDeath();
				} // else if

			} // while

		} // if

		if (gameNumber == 3) {
			
			long lastLoopTime = System.currentTimeMillis(); // the time of the last loop

			// keep loop running until game ends
			while (gameRunning) {

				countdown(); // counts down the timer of this game
							 // also initializes and removes the foreground entity

				// calc. time since last update, will be used to calculate
				// entities movement
				long delta = System.currentTimeMillis() - lastLoopTime;
				lastLoopTime = System.currentTimeMillis();
				
				
				// get graphics
				g = (Graphics2D) strategy.getDrawGraphics();
				g.setFont(new Font("default", Font.BOLD, 16)); // set the font
				
				// get the background image
				Toolkit tk = Toolkit.getDefaultToolkit();
				Image Pic;
				URL url = Game.class.getResource("sprites/background3.jpg");
				Pic = tk.getImage(url);
				g.drawImage(Pic, 0, 0, this); // draw the background

				// move all entities
				if (!waitingForKeyPress) {
					for (int i = 0; i < entities.size(); i++) {
						Entity entity = (Entity) entities.get(i);
						entity.move(delta);
						participantArray[i].move(delta, player);
					} // for
				} // if

				// draw all entities
				for (int i = 0; i < entities.size(); i++) {
					try {
						Entity entity = (Entity) entities.get(i);
						entity.draw(g);
					} catch (Exception e) {
						// makes sure that game doesn't create any errors
					} // catch
				} // for

				// brute force collisions, compare every entity
                // against every other entity. If any collisions
                // are detected notify both entities that it has
                // occurred
				if (!waitingForKeyPress) {
					for (int i = 0; i < entities.size(); i++) {
						for (int j = i + 1; j < entities.size(); j++) {
							Entity me = (Entity) entities.get(i);
							Entity him = (Entity) entities.get(j);
	
							if (me.collidesWith(him)) {
								me.collidedWith(him);
								him.collidedWith(me);
							} // if
						} // inner for
					} // outer for
				} // if

				// remove dead entities
				entities.removeAll(removeEntities);
				removeEntities.clear();

				// draw instruction screen
				if (waitingForKeyPress) {
					URL backgroundURL = Game.class.getResource(screenImage);
					Pic = tk.getImage(backgroundURL);
					g.drawImage(Pic, 0, 0, this);

				} // if

				// draw timer
				if (!waitingForKeyPress) {
					g.setColor(Color.white);
					g.drawString(seconds + " seconds left", canvasX / 2 - 30, 100);
				} // if

				// clear graphics and flip buffer
				g.dispose();
				strategy.show();

				// player should not move without user input
				player.setHorizontalMovement(0);
				player.setVerticalMovement(0);

				// respond to user moving player
				movePlayer();
				
				// if time runs out and player survives, they win
				if (seconds == 0) {
					seconds = 20;
					notifyWin();
					break;
				} // if

			} // while

		} // if

		if (gameNumber == 4) {
			
			long lastLoopTime = System.currentTimeMillis(); // the time of the last loop

			// keep loop running until game ends
			while (gameRunning) {

				// calc. time since last update, will be used to calculate
				// entities movement
				long delta = System.currentTimeMillis() - lastLoopTime;
				lastLoopTime = System.currentTimeMillis();

				// get graphics
				g = (Graphics2D) strategy.getDrawGraphics();
				g.setFont(new Font("default", Font.BOLD, 16)); // set the font
				
				// get the background image
				Toolkit tk = Toolkit.getDefaultToolkit();
				Image Pic;
				URL url = Game.class.getResource("sprites/background4.jpg");
				Pic = tk.getImage(url);
				g.drawImage(Pic, 0, 0, this); // draw the background

				// move all entities
				if (!waitingForKeyPress) {
					for (int i = 0; i < entities.size(); i++) {
						Entity entity = (Entity) entities.get(i);
						entity.move(delta);
					} // for
				} // if

				// move the defenders 
				if (!waitingForKeyPress) {
					participantArray[0].move(delta, player, participantArray[1], participantArray[2], 0);
					participantArray[1].move(delta, player, participantArray[0], participantArray[2], 1);
					participantArray[2].move(delta, player, participantArray[0], participantArray[1], 2);
				} // if

				// draw all entities
				for (int i = 0; i < entities.size(); i++) {
					Entity entity = (Entity) entities.get(i);
					entity.draw(g);
				} // for

				// brute force collisions, compare every entity
				// against every other entity. If any collisions
				// are detected notify both entities that it has
				// occurred
				if (!waitingForKeyPress) {
					for (int i = 0; i < entities.size(); i++) {
						for (int j = i + 1; j < entities.size(); j++) {
							Entity me = (Entity) entities.get(i);
							Entity him = (Entity) entities.get(j);

							if (me.collidesWith(him)) {
								me.collidedWith(him);
								him.collidedWith(me);
							} // if
						} // inner for
					} // outer for
				} // if

				// remove dead entities
				entities.removeAll(removeEntities);
				removeEntities.clear();

				// draw instruction screen
				if (waitingForKeyPress) {
					URL backgroundURL = Game.class.getResource(screenImage);
					Pic = tk.getImage(backgroundURL);
					g.drawImage(Pic, 0, 0, this);

				} // if

				// clear graphics and flip buffer
				g.dispose();
				strategy.show();

				// player should not move without user input
				player.setHorizontalMovement(0);
				player.setVerticalMovement(0);

				// if player is allowed to move, respond to user moving player
				if (playerCanMove) {
					movePlayer();
				} // if

				// if player makes it into the square, switch to stage 2
				if (player.getX() > 785 && player.getX() < 1090 && player.getY() < 885 && squidgameStage == 1) {
					squidgameStage = 2;
				} // if

				// if player makes it into the top circle, they win
				if (player.getX() < canvasX / 2 + 70 && player.getX() > canvasX / 2 - 110 && player.getY() > 60
						&& player.getY() < 180) {
					notifyWin();
				} // if

			} // while

		} // if

	} // gameLoop
	

	/*
	 * setKey input: none 
	 * output: none 
	 * purpose: decides on which key the player must press in game 2
	 */
	private void setKey() {

		Timer timer = new Timer();
		TimerTask task = new TimerTask() { // the task to be run
			
			public void run() {

				pullKey = (int) (Math.random() * 26); // chooses number from 0-25, which acts as a letter from a-z
				// resets both teams' scores each time the letter switches so that one team does not save up too many points
				playerScore = 0;
				enemyScore = 0;
			} // run

		};

		timer.scheduleAtFixedRate(task, 0, 3000); // waits 0 seconds, then runs this task every 3 seconds

	}// setKey

	/*
	 * scoreEnemy input: none 
	 * output: none 
	 * purpose: automatially raises the opposing team's score in game 2
	 */
	public void scoreEnemy() {

		Timer timer2 = new Timer();
		TimerTask task2 = new TimerTask() { // the task to be run

			public void run() {
				enemyScore++;
			} // run

		};

		timer2.scheduleAtFixedRate(task2, 500, 500); // waits 500 ms, then runs this task every 500 ms

	}// scoreEnemy

	/*
	 * tryToScore input: none 
	 * output: none 
	 * purpose: tries to increase the player's score based on how much time has passed since last score
	 */
	public void tryToScore() {
		
		// if more than 200 ms have passed since last score, player can increase his score
		if (System.currentTimeMillis() - lastScore > 200) {
			playerScore++;
			lastScore = System.currentTimeMillis();
		} // if

	}// tryToScore

	/*
	 * startGame input: none 
	 * output: none 
	 * purpose: start a fresh game and clear old data
	 */
	private void startGame() {

		if (gameNumber == 1) {

			// clear out any existing entities and initalize a new set
			entities.clear();
			initEntities();
			
			// resets the timer and light
			lightIsGreen = true;
			seconds = 70;
		

			// blank out any keyboard settings that might exist
			leftPressed = false;
			rightPressed = false;
			upPressed = false;
			downPressed = false;

		} else if (gameNumber == 2) {

			// clear out any existing entities and initalize a new set
			entities.clear();
			initEntities();

			// blank out any keyboard settings that might exist
			leftPressed = false;
			rightPressed = false;
			upPressed = false;
			downPressed = false;
			for (int i = 0; i < 26; i++) {
				buttonPressed[i] = false;
			} // for

		} else if (gameNumber == 3) {

			// change the player moveSpeeds
			moveSpeed = 150;
			moveSpeedVert = 150;
			
			// clear out any existing entities and initalize a new set
			entities.clear();
			initEntities();
			
			
			for (int i = 0; i < participantArray.length; i++) {
				participantArray[i].setMovements();
			} // for
			
			// reset timer
			seconds = 20;

			// blank out any keyboard settings that might exist
			leftPressed = false;
			rightPressed = false;
			upPressed = false;
			downPressed = false;
			
		} else if (gameNumber == 4) {

			// clear out any existing entities and initalize a new set
			entities.clear();
			initEntities();

			// blank out any keyboard settings that might exist
			leftPressed = false;
			rightPressed = false;
			upPressed = false;
			downPressed = false;
		} // else if
		
	} // startGame

	
	
	//inner class KeyInputHandler handles keyboard input from the user
	
	private class KeyInputHandler extends KeyAdapter {

		private int pressCount = 1; // the number of key presses since
									// waiting for 'any' key press

		/*
		 * The following methods are required for any class that extends the abstract
		 * class KeyAdapter. They handle keyPressed, keyReleased and keyTyped events.
		 */
		public void keyPressed(KeyEvent e) {

			// if waiting for keypress to start game, do nothing
			if (waitingForKeyPress) {
				return;
			} // if

			// respond to move left, right, up or down
			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				leftPressed = true;
			} // if

			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				rightPressed = true;
			} // if

			if (e.getKeyCode() == KeyEvent.VK_UP) {
				upPressed = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				downPressed = true;
			} // if
			
			// respond to any letter key pressed
			if (e.getKeyCode() == KeyEvent.VK_A) {
				buttonPressed[0] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_B) {
				buttonPressed[1] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_C) {
				buttonPressed[2] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_D) {
				buttonPressed[3] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_E) {
				buttonPressed[4] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_F) {
				buttonPressed[5] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_G) {
				buttonPressed[6] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_H) {
				buttonPressed[7] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_I) {
				buttonPressed[8] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_J) {
				buttonPressed[9] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_K) {
				buttonPressed[10] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_L) {
				buttonPressed[11] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_M) {
				buttonPressed[12] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_N) {
				buttonPressed[13] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_O) {
				buttonPressed[14] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_P) {
				buttonPressed[15] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_Q) {
				buttonPressed[16] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_R) {
				buttonPressed[17] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_S) {
				buttonPressed[18] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_T) {
				buttonPressed[19] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_U) {
				buttonPressed[20] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_V) {
				buttonPressed[21] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_W) {
				buttonPressed[22] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_X) {
				buttonPressed[23] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_Y) {
				buttonPressed[24] = true;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_Z) {
				buttonPressed[25] = true;
			} // if


		} // keyPressed

		public void keyReleased(KeyEvent e) {
			// if waiting for keypress to start game, do nothing
			if (waitingForKeyPress) {
				return;
			} // if

			// respond to move left, right, up or down
			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				leftPressed = false;
			} // if

			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				rightPressed = false;
			} // if

			if (e.getKeyCode() == KeyEvent.VK_UP) {
				upPressed = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				downPressed = false;
			} // if
			
			// respond to any letter key pressed
			
			if (e.getKeyCode() == KeyEvent.VK_A) {
				buttonPressed[0] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_B) {
				buttonPressed[1] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_C) {
				buttonPressed[2] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_D) {
				buttonPressed[3] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_E) {
				buttonPressed[4] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_F) {
				buttonPressed[5] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_G) {
				buttonPressed[6] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_H) {
				buttonPressed[7] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_I) {
				buttonPressed[8] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_J) {
				buttonPressed[9] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_K) {
				buttonPressed[10] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_L) {
				buttonPressed[11] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_M) {
				buttonPressed[12] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_N) {
				buttonPressed[13] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_O) {
				buttonPressed[14] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_P) {
				buttonPressed[15] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_Q) {
				buttonPressed[16] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_R) {
				buttonPressed[17] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_S) {
				buttonPressed[18] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_T) {
				buttonPressed[19] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_U) {
				buttonPressed[20] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_V) {
				buttonPressed[21] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_W) {
				buttonPressed[22] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_X) {
				buttonPressed[23] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_Y) {
				buttonPressed[24] = false;
			} // if
			if (e.getKeyCode() == KeyEvent.VK_Z) {
				buttonPressed[25] = false;
			} // if

		} // keyReleased

		public void keyTyped(KeyEvent e) {

			// if waiting for key press to start game
			if (waitingForKeyPress) {
				// if introScreenVisible does not start game and only removes introScreen
				if (introScreenVisible) {
					if (e.getKeyChar() == 32) {
						introScreenVisible = false;
					} // if
					
				// if game is won, exits the program
				} else if (gameNumber == 5) {
					if (e.getKeyChar() == 32) {
						System.exit(0);
					} // if
				
				// starts the game
				} else {
					if (e.getKeyChar() == 32) {
						waitingForKeyPress = false;
						startGame();
						pressCount = 0;

					} else {
						pressCount++;
					} // else
				} // else
			} // if waitingForKeyPress

			// if escape is pressed, end game
			if (e.getKeyChar() == 27) {
				System.exit(0);
			} // if escape pressed

		} // keyTyped

	} // class KeyInputHandler

	// returns the current game number
	public int getGameNumber() {
		return gameNumber;
	} // getGameNumber
	
	// returns the current squid game stage
	public int getSquidgameStage() {
		return squidgameStage;
	} // getSquidgameStage

	// makes the player unable to move
	public void cannotMove() {
		playerCanMove = false;
	} // cannotMove

	// makes the player able to move
	public void canMove() {
		playerCanMove = true;
	} // canMove
	
	// lets the playerEntity respond to user inputs
	public void movePlayer() {
		if (leftPressed) {
			player.setHorizontalMovement(-moveSpeed);
		} // if
		
		if (rightPressed) {
			player.setHorizontalMovement(moveSpeed);
		} // if
		
		if (upPressed) {
			player.setVerticalMovement(-moveSpeedVert);
		} // if

		if (downPressed) {
			player.setVerticalMovement(moveSpeedVert);
		} // if
	} // movePlayer
	
	public static void main(String[] args) {
		
		//play music
		Music.playMusic("finalmusic");
		
		// instantiate this object
		Game game = new Game();

	} // main

} // Game