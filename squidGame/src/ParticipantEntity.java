/* 
 * ParticipantEntity.java
 * November 8, 2021
 * Represents one of the participants in each game
 */

public class ParticipantEntity extends Entity {

	private double verticalSpeed = -((Math.random() * 20) + 18); // vertical speed
	private double horizontalSpeed = -((Math.random() * -20 + 50)); // horizontal speed
	private boolean isStupid; // if participants move while light is red in game 1
	private boolean startMove; // if defenders have started moving in the second stage of game 4

	private Game game; // the game in which the participant exists

	/*
	 * construct a new participant 
	 * input: game - the game in which the alien is being created 
	 * r - the image representing the participant 
	 * x, y - initial location of alien
	 */
	
	public ParticipantEntity(Game g, String r, int newX, int newY) {
		super(r, newX, newY); // calls the constructor in Entity
		game = g;
		isStupid = false;
		startMove = false;
	} // constructor

	/*
	 * getIsStupid 
	 * input: none
	 * purpose: returns if the participant is stupid
	 */
	
	public boolean getIsStupid() {
		return isStupid;
	} // getIsStupid

	/*
	 * changeIntelligence 
	 * input: none
	 * purpose: switches the intelligence of the participant
	 */

	public void changeIntelligence() {
		isStupid = !isStupid;
	} // changeIntelligence

	/* 
	 * move
	 * input: delta - time elapsed since last move (ms)
	 * purpose: move the participant
	 */
	
	public void move(long delta) {

		if (game.getGameNumber() == 1) {

			int panic = 0; // simulates natural horizontal movement as participants panic 
			
			if (!isStupid) {
				
				// if participants have reached end of field stop moving
				if (y < 50) {
					this.setVerticalMovement(0);
					this.setVerticalMovement(0);
					
				} else if (game.lightIsGreen) {
					this.setVerticalMovement(verticalSpeed);

					// participant either moves left or right when panicking
					panic = (int) (Math.random() * 2); 
					if (panic == 1) {
						this.setHorizontalMovement(-horizontalSpeed);
					} else {
						this.setHorizontalMovement(horizontalSpeed);
					} // else

					// prevents the participants from moving off the screen
					if (x < 0) {
						this.setHorizontalMovement(-horizontalSpeed);
					} // if

					if (x > game.canvasX - 50) {
						this.setHorizontalMovement(horizontalSpeed);
					} // if

					super.move(delta); // calls the move method in Entity
			
				// if the light is red, stop moving
				} else {
					
					this.setVerticalMovement(0);
					this.setHorizontalMovement(0);
					super.move(delta); // calls the move method in Entity
			
				} // else

			// if the participant is stupid
			} else {
				
				if (y < 50) {
					// if participants have reached end of field stop moving
					this.setVerticalMovement(0);
					
				} else {
					// move if regardless of the light
					this.setVerticalMovement(verticalSpeed);
					super.move(delta); // calls the move method in Entity
	
				} // else
			} // else

		} // if

		if (game.getGameNumber() == 2) {
			super.move(delta); // calls the move method in Entity
	
		} // if

	} // move
	
	/* 
	 * overloaded move method for the third game
	 * input: delta - time elapsed since last move (ms)
	 * player - the player entity
	 * purpose: move the participant 
	 */
	
	public void move (long delta, Entity player) {
		
		// uses a random number to make participant movement more natural
		int random = 1 + (int) (Math.random() * ((5 - 1) + 1));

		
		// makes participant chase after players
		if (random == 5) {
			dx = player.getX() - x;
			dy = player.getY() - y;
			
		// move in a certain direction based on "random" variable
		} else if (random == 4) {
			this.setVerticalMovement(verticalSpeed);
			this.setHorizontalMovement(horizontalSpeed);
		} else if (random == 3) {
			this.setVerticalMovement(verticalSpeed);
			this.setHorizontalMovement(-horizontalSpeed);

		} else if (random == 2) {
			this.setVerticalMovement(-verticalSpeed);
			this.setHorizontalMovement(-horizontalSpeed);
		} else {
			this.setVerticalMovement(-verticalSpeed);
			this.setHorizontalMovement(horizontalSpeed);
		} // if else

		// prevents the participants from moving off the screen
		if (x < 0) {
			this.setHorizontalMovement(-horizontalSpeed);
		} // if

		if (x > game.canvasX - 50) {
			this.setHorizontalMovement(horizontalSpeed);
		} // if

		if (y < 50) {
			this.setVerticalMovement(-verticalSpeed);

		} // if

		if (y > game.canvasY - 50) {
			this.setVerticalMovement(verticalSpeed);
		} // if

		super.move(delta); // calls the move method in Entity
		
	} // move

	/* 
	 * overloaded move method for the fourth game
	 * input: delta - time elapsed since last move (ms)
	 * player - the player entity
	 * enemy1, enemy2 - the other 2 defenders
	 * entityNumber - the index of the participantArray that the method is being called on
	 * purpose: move the participant
	 */
	
	public void move(long delta, Entity player, Entity enemy1, Entity enemy2, int entityNumber) {

        if (game.getSquidgameStage() == 1) {

            // does not let two defenders collide and move through each other
            if (this.collidesWith(enemy1)) {
                if (x > enemy1.getX()) {
                    enemy1.setX(enemy1.getX() - 1);
                }
                if (x < enemy1.getX()) {
                    x--;
                }
                if (y > enemy1.getY()) {
                    enemy1.setY(enemy1.getY() - 1);
                }
                if (y < enemy1.getY()) {
                    y--;
                }

            } // if
            if (this.collidesWith(enemy2)) {
                if (x > enemy2.getX()) {
                    enemy2.setX(enemy2.getX() - 1);
                }
                if (x < enemy2.getX()) {
                    x--;
                }
                if (y > enemy2.getY()) {
                    enemy2.setY(enemy2.getY() - 1);
                }
                if (y < enemy2.getY()) {
                    y--;
                }

            } // if

            // does not let the defenders leave the square
            // stop at left of square
            if ((dx < 0) && (x < 760)) {
                x += 1;
                return;
            } // if

            // stop at right side of square
            if ((dx > 0) && (x > 1110)) {
                x -= 1;
                return;
            } // if

            // stop at bottom of the square
            if ((dy > 0) && (y > 900)) {
                y -= 1;
                return;
            }
            
            // defenders follow the player
            dx = player.getX() - x;
            dy = player.getY() - y;
            

            super.move(delta); // calls the move method in Entity 

        
        } else if (game.getSquidgameStage() == 2) {

            // allows time for the defenders to move to the triangle
            if ((450 - 75 * entityNumber) - y < -1) {

                game.cannotMove(); // player cannot move
                
                //defenders move to their designated spot
                dx = (game.canvasX / 2) - 40 - x;
                dy = (450 - 75 * entityNumber) - y;

                super.move(delta); // calls the move method in Entity

            } else {
                
                this.setVerticalMovement(0);

                game.canMove(); // player can move
                
                // defenders move left and right inside of the triangle
                if (x < (y - 1460) / -1.414) {
                    this.setHorizontalMovement(300);
                }// if

                if (x > (y + 1150) / 1.414) {
                    this.setHorizontalMovement(-300);
                } // if

                if (startMove == false) {
                    this.setHorizontalMovement(-300);
                    startMove = true;

                } // if

                super.move(delta); // calls the move method in Entity

            } // else

        } // else if

    } // move
	
	
	// changes the vertical and horizontal speed of participants for the third game
	public void setMovements() {
		verticalSpeed = -((Math.random() * -600 + 2000));
		horizontalSpeed = -((Math.random() * -600 + 2000));
	} // setMovements

	
	/*
	 * collidedWith 
	 * input: other - the entity with which the alien has collided
	 * purpose: notification that the alien has collided with something
	 */
	
	public void collidedWith(Entity other) {
	} // collidedWith

} // ParticipantEntity