package de.hhn.aib3.aufg3.gruppe11.utility;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import org.greenrobot.eventbus.EventBus;

import de.hhn.aib3.aufg3.gruppe11.R;
import de.hhn.aib3.aufg3.gruppe11.authentication.LoginActivity;
import de.hhn.aib3.aufg3.gruppe11.game.elements.game.Game;
import de.hhn.aib3.aufg3.gruppe11.game.enums.Orientation;


/**
 * Standard Utility class holding methods for various classes
 */
public class Utility {

    private static final int GAME_BOARD_SIZE = 10;
    public static final byte WATER = 10;
    public static final byte MISS = 11;
    public static final byte SHIP_2 = 20;
    public static final byte SHIP_2_HIT = 21;
    public static final byte SHIP_3 = 30;
    public static final byte SHIP_3_HIT = 31;
    public static final byte SHIP_4 = 40;
    public static final byte SHIP_4_HIT = 41;
    public static final byte SHIP_5 = 50;
    public static final byte SHIP_5_HIT = 51;

    private static final String DEBUGLOG_TAG = "DEBUGLOG-UTILITY";

    public static final String INITIATOR_TAB_TAG = "INITIATOR";
    public static final String PLAYER2_TAB_TAG = "PLAYER2";

    /**
     * Converts Plaintext user password into SHA-256 hash
     *
     * @param password Chosen user password
     * @return SHA-256 Hash formatted in HEXADECIMAL parsed as string
     */
    public static String hash(String password) {

        byte[] passBytes = password.getBytes();

        try {

            String hashString = new String(Hex.encodeHex(DigestUtils.sha256(passBytes)));
            Log.d(DEBUGLOG_TAG, "Password hashed: " + hashString);
            return hashString;

        } catch (Exception e) {
            Log.d(DEBUGLOG_TAG, "Password could not be hashed\nException: " + e.getMessage());
            return null;
        }

    }

    /**
     * Unregisters and registers EventBus
     *
     * @param context where the event bus should be refreshed
     */
    public static void refreshEventBus(Context context) {
        EventBus.getDefault().unregister(context);
        EventBus.getDefault().register(context);
    }

    /**
     * Removes all activities from stack
     * Starts LoginActivity as new task
     *
     * @param context active activity
     */
    public static void backToLogin(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    /**
     * Plays sound / tone
     *
     * @param resourceId resource id of tone to be played
     * @param context    active activity
     */
    public static void play(int resourceId, Context context) {
        MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(context, resourceId);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }

            }
        });
        Log.d(DEBUGLOG_TAG, "Played Sound");
    }

    /**
     * Returns the orientation of ships as they are positioned
     *
     * @param gameBoard Length must equal GAME_BOARD_SIZE squared
     * @param ship      type of ship
     *                  valid byte values:
     *                  20 - Ship of length 2
     *                  30 - Ship of length 3
     *                  40 - Ship of length 4
     *                  50 - Ship of length 5
     * @return Orientation.VERTICAL
     * Orientation.HORIZONTAL
     * Null - invalid param
     */
    public static Orientation getShipOrientation(byte[] gameBoard, byte ship) {
        int rep = gameBoard.length / GAME_BOARD_SIZE;

        for (int i = 0; i < gameBoard.length; i++) {
            if ((gameBoard[i] == ship) && (gameBoard[i + 1] == ship)) {
                Log.d(DEBUGLOG_TAG, "HORIZONTAL BOAT ORIENTATION");
                return Orientation.HORIZONTALLY;
            } else if ((gameBoard[i] == ship) && (gameBoard[i + rep] == ship)) {
                Log.d(DEBUGLOG_TAG, "VERTICAL BOAT ORIENTATION");
                return Orientation.VERTICALLY;
            }
        }
        return null;
    }

    /**
     * Counts the number of {@code occurrence} instances in {@code gameBoard}
     *
     * @param gameBoard  resembles the battlefield
     * @param occurrence resembles an element of the battlefield e.g. a ship
     * @return count of occurrences
     */
    public static int countOccurrence(byte[] gameBoard, byte occurrence) {
        int occurrences = 0;
        for (byte aGameBoard : gameBoard) {
            if (aGameBoard == occurrence) {
                occurrences++;
            }
        }
        return occurrences;
    }

    /**
     * Detects whether a ship was missed, hit or sunk and plays corresponding sound
     *
     * @param newGame instance of current game
     */
    public static void detectHit(Game originalGame, Game newGame, Context context) {

        Log.d("TEST--", "OK no Sound for what ever reason");
//        play(R.raw.water, context);

        for (int i = 0; i < GAME_BOARD_SIZE * GAME_BOARD_SIZE; i++) {
            if (originalGame.getBoardP1()[i] != newGame.getBoardP1()[i]) {
                if (newGame.getBoardP1()[i] == Utility.MISS) {
                    play(R.raw.water, context);
                } else if ((newGame.getBoardP1()[i] == Utility.SHIP_2_HIT)) {
                    if (countOccurrence(newGame.getBoardP1(), Utility.SHIP_2_HIT) == 2) {
                        play(R.raw.explosion, context);
                    } else {
                        play(R.raw.bomb, context);
                    }
                } else if ((newGame.getBoardP1()[i] == Utility.SHIP_3_HIT)) {
                    if (countOccurrence(newGame.getBoardP1(), Utility.SHIP_3_HIT) == 3) {
                        play(R.raw.explosion, context);
                    } else {
                        play(R.raw.bomb, context);
                    }
                } else if ((newGame.getBoardP1()[i] == Utility.SHIP_4_HIT)) {
                    if (countOccurrence(newGame.getBoardP1(), Utility.SHIP_4_HIT) == 4) {
                        play(R.raw.explosion, context);
                    } else {
                        play(R.raw.bomb, context);
                    }
                } else if ((newGame.getBoardP1()[i] == Utility.SHIP_5_HIT)) {
                    if (countOccurrence(newGame.getBoardP1(), Utility.SHIP_5_HIT) == 5) {
                        play(R.raw.explosion, context);
                    } else {
                        play(R.raw.bomb, context);
                    }
                }
            } else if (originalGame.getBoardP2()[i] != newGame.getBoardP2()[i]) {
                if (newGame.getBoardP2()[i] == Utility.MISS) {
                    play(R.raw.water, context);
                } else if ((newGame.getBoardP2()[i] == Utility.SHIP_2_HIT)) {
                    if (countOccurrence(newGame.getBoardP2(), Utility.SHIP_2_HIT) == 2) {
                        play(R.raw.explosion, context);
                    } else {
                        play(R.raw.bomb, context);
                    }
                } else if ((newGame.getBoardP2()[i] == Utility.SHIP_3_HIT)) {
                    if (countOccurrence(newGame.getBoardP2(), Utility.SHIP_3_HIT) == 3) {
                        play(R.raw.explosion, context);
                    } else {
                        play(R.raw.bomb, context);
                    }
                } else if ((newGame.getBoardP2()[i] == Utility.SHIP_4_HIT)) {
                    if (countOccurrence(newGame.getBoardP2(), Utility.SHIP_4_HIT) == 4) {
                        play(R.raw.explosion, context);
                    } else {
                        play(R.raw.bomb, context);
                    }
                } else if ((newGame.getBoardP2()[i] == Utility.SHIP_5_HIT)) {
                    if (countOccurrence(newGame.getBoardP2(), Utility.SHIP_5_HIT) == 5) {
                        play(R.raw.explosion, context);
                    } else {
                        play(R.raw.bomb, context);
                    }
                }
            }
        }
    }

}