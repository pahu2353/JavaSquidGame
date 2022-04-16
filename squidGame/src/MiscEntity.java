/* 
 * MiscEntity.java
 * November 8, 2021
 * Represents miscellaneous entities (the doll and lights in game 1, the rope in game 2)
 */

public class MiscEntity extends Entity {

	private Game game; // the game in which the doll, the lights, and the rope exist

	/*
	 * construct a new doll, light, or rope entity
	 * input: game - the game in which the doll, light, or rope is being created 
	 * r - the image representing the doll, light, or rope
	 * x, y - initial location of doll, light, or rope
	 */
	
	public MiscEntity(Game g, String r, int newX, int newY) {
		super(r, newX, newY); // calls the constructor in Entity
		game = g;
	} // constructor

	/*
	 * collidedWith 
	 * input: other - the entity with which the doll, light, or rope has collided 
	 * purpose: notification that the entity has collided with something
	 */
	
	public void collidedWith(Entity other) {
		// nothing happens if collision
	} // collidedWith

} // MiscEntity