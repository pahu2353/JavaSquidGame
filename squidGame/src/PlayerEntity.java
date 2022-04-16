/* 
 * PlayerEntity.java
 * November 8, 2021
 * Represents the player
 */

public class PlayerEntity extends Entity {

	private Game game; // the game in which the ship exists

	/*
	 * construct the player's ship 
	 * input: game - the game in which the player is being created 
	 * ref - a string with the name of the image associated to the sprite for the ship 
	 * x, y - initial location of ship
	 */

	public PlayerEntity(Game g, String r, int newX, int newY) {
		super(r, newX, newY); // calls the constructor in Entity
		game = g;

	} // constructor

	/*
	 * move 
	 * input: delta - time elapsed since last move (ms) 
	 * purpose: move ship
	 */

	public void move(long delta) {

		// stop at left side of screen
		if ((dx < 0) && (x < 10)) {
			return;
		} // if
			// stop at right side of screen
		if ((dx > 0) && (x > game.canvasX - 50)) {
			return;
		} // if

		// stop at bottom of the screen
		if ((dy > 0) && (y > game.canvasY - 50)) {
			return;
		} // if

		// stop at top of the screen
		if ((dy < 0) && (y < 10)) {
			return;
		} // if

		if (game.getGameNumber() == 4 && game.getSquidgameStage() == 1) {
			if ((dy < 0) && (y < game.canvasY / 2 - 10)) {
				return;
			} // if

		} else if (game.getGameNumber() == 4 && game.getSquidgameStage() == 2) {

			// triangle boundaries
			if (y < 460 && y > 250) {
				// die outside of left edge
				if (((dx < 0) && (x < (y - 1430) / -1.414)) || ((dy < 0) && (y < -1.414 * x + 1430))) {
					game.notifyDeath();
				} // if

				// die outside of right edge
				if (((dx > 0) && (x > (y + 1150) / 1.414)) || ((dy < 0) && (y < 1.414 * x - 1150))) {
					game.notifyDeath();
				} // if

			// square boundaries
			} else {

				// die outside of left edge
				if ((dx < 0) && (x < 690)) {
					game.notifyDeath();
				}

				// die outside of right edge
				if ((dx > 0) && (x > 1150)) {
					game.notifyDeath();
				} // if

				// die outside of bottom edge
				if ((dy > 0) && (y > 935)) {
					game.notifyDeath();
				} // if
			} // else

		} // else if
 
		super.move(delta); // calls the move method in Entity

	} // move

	/*
	 * collidedWith 
	 * input: other - the entity with which the ship has collided
	 * purpose: notification that the player's ship has collided with something
	 */
	
	public void collidedWith(Entity other) {

		if (other instanceof ParticipantEntity && game.getGameNumber() == 4
				|| (other instanceof ParticipantEntity && game.getGameNumber() == 3)) {
			game.notifyDeath();

		} // if

	} // collidedWith

} // PlayerEntity