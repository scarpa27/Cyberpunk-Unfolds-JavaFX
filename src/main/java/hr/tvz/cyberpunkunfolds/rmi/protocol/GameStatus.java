package hr.tvz.cyberpunkunfolds.rmi.protocol;

import java.io.Serializable;

/**
 * Game status for a room in the lobby.
 */
public enum GameStatus implements Serializable {
    /** Room is open, players can join and chat */
    WAITING,
    
    /** Host clicked start game, initializing server */
    STARTING,
    
    /** The game is in progress, no new joins allowed */
    IN_PROGRESS,
    
    /** The game has ended */
    FINISHED
}
