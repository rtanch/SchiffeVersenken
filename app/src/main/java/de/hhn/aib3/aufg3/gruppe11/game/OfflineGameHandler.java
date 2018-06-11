package de.hhn.aib3.aufg3.gruppe11.game;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.util.Arrays;

import de.hhn.aib3.aufg3.gruppe11.Exceptions.InvalidFiringPosition;
import de.hhn.aib3.aufg3.gruppe11.game.elements.game.Cell;
import de.hhn.aib3.aufg3.gruppe11.game.elements.game.Game;
import de.hhn.aib3.aufg3.gruppe11.game.enums.NextMove;
import de.hhn.aib3.aufg3.gruppe11.utility.Utility;
import okhttp3.internal.Util;

public class OfflineGameHandler {


    public OfflineGameHandler(){

    }



    public byte[] fireShot(byte[] resBoard, byte[] compBoard, Cell firingPos, Context context) throws InvalidFiringPosition{

        int pos = (firingPos.getRow()-1)*10+firingPos.getColumn()-1;

        switch(compBoard[pos]){
            case Utility.MISS:
                throw new InvalidFiringPosition();
            case Utility.SHIP_2_HIT:
                throw new InvalidFiringPosition();
            case Utility.SHIP_3_HIT:
                throw new InvalidFiringPosition();
            case Utility.SHIP_4_HIT:
                throw new InvalidFiringPosition();
            case Utility.SHIP_5_HIT:
                throw new InvalidFiringPosition();
        }


//        Game game1 = new Game("","");
//        game1.setBoardP1(compBoard);
//        game1.setBoardP2(compBoard);
//        Game game2 = new Game("","");
//        game2.setBoardP1(resBoard);
//        game2.setBoardP2(resBoard);
//        Utility.detectHit(game1, game2, context);

//        if(resBoard[0] == Utility.SHIP_2){
//            Log.d("TEST--","Resultboard as expected");
//        }else{
//            Log.d("TEST--","Resultboard unexpected");
//        }

        Log.d("TEST--","Pos: " + pos);

        switch(resBoard[pos]){
            case Utility.WATER:
                compBoard[pos] = Utility.MISS;
                Log.d("TEST--","S1");
                break;
            case Utility.SHIP_2:
                compBoard[pos] = Utility.SHIP_2_HIT;
                Log.d("TEST--","S2");
                break;
            case Utility.SHIP_3:
                compBoard[pos] = Utility.SHIP_3_HIT;
                Log.d("TEST--","S3");
                break;
            case Utility.SHIP_4:
                compBoard[pos] = Utility.SHIP_4_HIT;
                Log.d("TEST--","S4");
                break;
            case Utility.SHIP_5:
                compBoard[pos] = Utility.SHIP_5_HIT;
                Log.d("TEST--","S5");
                break;
        }

        return compBoard;
    }

    public String getNextMove(Game game, byte[] compBoard, Cell firingPos, String currentMove)  throws InvalidFiringPosition {

        Log.d("TEST--","Current Move is: " + currentMove);

        int pos = (firingPos.getRow()-1)*10+firingPos.getColumn()-1;
        String nextMove = null;

        byte[] resBoard;


        if(currentMove == null){
            Log.d("TEST--","Current Move is null");
            currentMove = NextMove.INITIATOR.toString();
//            return NextMove.INITIATOR.toString();
        }

        if(Arrays.equals(game.getBoardP1(), game.getBoardP2())){
            Log.d("TEST--","Arrays are Equal in Offline Game Handler");
        }
        if(currentMove.equals(NextMove.INITIATOR.toString())){
            resBoard = game.getBoardP1();
        }else{
            resBoard = game.getBoardP2();
        }



        switch(compBoard[pos]){
            case Utility.MISS:
                throw new InvalidFiringPosition();
            case Utility.SHIP_2_HIT:
                throw new InvalidFiringPosition();
            case Utility.SHIP_3_HIT:
                throw new InvalidFiringPosition();
            case Utility.SHIP_4_HIT:
                throw new InvalidFiringPosition();
            case Utility.SHIP_5_HIT:
                throw new InvalidFiringPosition();
        }

        if(resBoard[pos] == Utility.SHIP_5){
            Log.d("TEST--","As ExpectedS");
        }

        switch(resBoard[pos]){
            case Utility.WATER:
                Log.d("TEST--","HIT WATER <<<<<");
                if(currentMove.equals(NextMove.INITIATOR.toString())){
                    nextMove = NextMove.P2.toString();
                }else if(currentMove.equals(NextMove.P2.toString())){
                    nextMove = NextMove.INITIATOR.toString();
                }else{
                    throw new InvalidFiringPosition();
                }
                break;
            case Utility.SHIP_2:
                nextMove = currentMove;
                break;
            case Utility.SHIP_3:
                nextMove = currentMove;
                break;
            case Utility.SHIP_4:
                nextMove = currentMove;
                break;
            case Utility.SHIP_5:
                nextMove = currentMove;
                break;
        }

        Log.d("TEST--","Next Move is: " + nextMove);
        return nextMove;
    }


    public boolean isGameFinished(byte[] resBoard, byte[] compBoard){

        for(int i=0; i<resBoard.length; i++){

            if(resBoard[i] == Utility.SHIP_5 && compBoard[i] != Utility.SHIP_5_HIT){
                return false;
            }else if(resBoard[i] == Utility.SHIP_4 && compBoard[i] != Utility.SHIP_4_HIT){
                return false;
            }else if(resBoard[i] == Utility.SHIP_3 && compBoard[i] != Utility.SHIP_3_HIT){
                return false;
            }else if(resBoard[i] == Utility.SHIP_2 && compBoard[i] != Utility.SHIP_2_HIT){
                return false;
            }
        }

        return true;
    }







}
















//    public String getNextMove(byte[] resBoard, byte[] compBoard, Cell firingPos, String currentMove)  throws InvalidFiringPosition {
//
//        int pos = (firingPos.getRow()-1)*10+firingPos.getColumn()-1;
//        String nextMove = null;
//
//        if(currentMove == null){
////            currentMove = NextMove.INITIATOR.toString();
//            return NextMove.INITIATOR.toString();
//        }
//
//        switch(compBoard[pos]){
//            case Utility.MISS:
//                throw new InvalidFiringPosition();
//            case Utility.SHIP_2_HIT:
//                throw new InvalidFiringPosition();
//            case Utility.SHIP_3_HIT:
//                throw new InvalidFiringPosition();
//            case Utility.SHIP_4_HIT:
//                throw new InvalidFiringPosition();
//            case Utility.SHIP_5_HIT:
//                throw new InvalidFiringPosition();
//        }
//
//        Log.d("TEST--","Pos: " + pos);
//
//        switch(resBoard[pos]){
//            case Utility.WATER:
//                if(currentMove.equals(NextMove.INITIATOR.toString())){
//                    nextMove = NextMove.P2.toString();
//                }else if(currentMove.equals(NextMove.P2.toString())){
//                    nextMove = NextMove.INITIATOR.toString();
//                }else if(currentMove.equals(NextMove.NONE.toString())){
//                    nextMove = NextMove.NONE.toString();
//                }else{
//                    throw new InvalidFiringPosition();
//                }
//                break;
//            case Utility.SHIP_2:
//                nextMove = currentMove;
//                break;
//            case Utility.SHIP_3:
//                nextMove = currentMove;
//                break;
//            case Utility.SHIP_4:
//                nextMove = currentMove;
//                break;
//            case Utility.SHIP_5:
//                nextMove = currentMove;
//                break;
//        }
//
//        return nextMove;
//    }