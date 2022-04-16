/* 
 * ForegroundEntity.java
 * November 8, 2021
 * Represents the light turning off in game 3
 */

public class ForegroundEntity extends Entity {

	private Game game; // the game in which the foreground exists

	/*
	 * construct a new foreground
	 * input: game - the game in which the foreground is being created 
	 * r - the foreground image
	 * x, y - initial location of foreground
	 */
	
	public ForegroundEntity(Game g, String r, int newX, int newY) {
		super(r, newX, newY); // calls the constructor in Entity
		game = g;
	} // constructor

	/*
	 * collidedWith input: other - the entity with which the foreground has collided 
	 * purpose: notification that the entity has collided with something
	 */
	
	public void collidedWith(Entity other) {
		// nothing happens if collision
	} // collidedWith

} // ForegroundEntity