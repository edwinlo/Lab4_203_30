/*******************************************************
 * GameLoopTask.java             Feb 18th 2017
 *
 * Group # 30
 *
 * Lab Members: Edwin Lo, Madeleine Wang, Yizhou Yang
 *
 * This class contains the code for the GameLoopTask object. It implements a TimerTask
 * that has a period of 30ms. The task it runs is move() function from the GameBlock.java
 * class.
 *
 *******************************************************/


package lab4_203_30.uwaterloo.ca.lab4_203_30;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Random;
import java.util.TimerTask;


public class GameLoopTask extends TimerTask {

    enum gameDirection{UP , DOWN , LEFT , RIGHT , NO_MOVEMENT};
    public gameDirection currentGameDirection = gameDirection.NO_MOVEMENT;

    private Activity myActivity;
    private Context myContext;
    private RelativeLayout myRL;

    public static final int SLOT_ISOLATION = 150;

    public static LinkedList <GameBlock> myGBList = new LinkedList<GameBlock>();
    public static LinkedList <GameBlock> movingBlocks = new LinkedList<GameBlock>();

    private LinkedList<GameBlock> blocksToBeRemoved = new LinkedList<GameBlock>();

    private int numberOfEmptySlots;
    private int randCoord1;
    private int randCoord2;

    //flags
    private boolean endGame = false;
    private boolean doneMoving;

    private GameBlock newBlock;

    public static Random rand = new Random();

    private TextView myTV2;

    public GameLoopTask(Activity myActivity, RelativeLayout myRL, Context myContext) {
        this.myActivity = myActivity;
        this.myContext = myContext;
        this.myRL = myRL;

        myTV2 = new TextView(myContext);
        myRL.addView(myTV2);
        myTV2.setX(100);
        myTV2.setY(100);
        myTV2.setText("");
        myTV2.setTextSize(50);
        myTV2.setTextColor(Color.RED);
        myTV2.bringToFront();

        createBlock();
    }

    public void run() {

        this.myActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (endGame == false) {
                    for (int i = 0; i < myGBList.size(); i++) {
                        myGBList.get(i).move();
                    }

                    doneMoving = true; //checks if all block is done moving

                    if (movingBlocks.size() > 0) {
                        for (GameBlock gb : movingBlocks) {
                            if (!gb.done) {
                                doneMoving = false;
                            }
                        }
                    } else {
                        doneMoving = false;

                    }

                    //runs if all the blocks are done moving
                    if (doneMoving == true) {

                        //resetting the done moving flag
                        for (GameBlock gb : myGBList) {
                            gb.done = false;
                        }

                        //adding the blocks that are to be removed to the temp remove blocks list
                        for (GameBlock gb: myGBList){
                            if (gb.getToBeRemoved()){
                                blocksToBeRemoved.add(gb);
                            }
                        }


                        //destroying the merged blocks and removing them from the layout
                        for (GameBlock gb : blocksToBeRemoved) {
                            gb.destroyMe();
                            myGBList.remove(gb);
                        }

                        //clearing the temporary lists
                        blocksToBeRemoved.clear();
                        movingBlocks.clear();

                        //check possible white spaces after merging
                        for (GameBlock gb : myGBList){
                            gb.checkWhiteSpaces();
                        }

                        //checking if the user won
                        for (GameBlock gb : myGBList){
                            if (gb.getBlockNumber() == 256){
                                endGame = true;     //setting the endgame flag

                                //printing out a log message to ensure that the game is not frozen
                                Log.d("You won", "The game has ended.");

                                myTV2.setText("You Won!!");
                            }
                        }

                        //
                        createBlock();

                    }

                }
            }
        });
    }

    //method for creating new blocks
    private void createBlock(){

        boolean [][] isEmpty = new boolean[4][4];
        boolean notEmpty = false;

        numberOfEmptySlots = 16;

        //initially setting all the spots to be empty
        for (int a = 0; a < 4; a++){
            for (int b = 0; b < 4; b++){
                isEmpty[a][b] = true;
            }
        }

        //detecting if the spot is empty or not
        for (GameBlock gb: myGBList){
            int[] tempCoord = gb.getCoord();
            tempCoord[0] = (tempCoord[0] - gb.LEFT_BOUNDARY)/ SLOT_ISOLATION;
            tempCoord[1] = (tempCoord[1] - gb.UP_BOUNDARY)/ SLOT_ISOLATION;

            isEmpty[tempCoord[0]][tempCoord[1]] = false;            //setting the isEmpty to mark the spot as occupied
        }

        //counting the number of empty slots
        for (int c = 0; c < 4 ;c++){
            for(int d = 0; d < 4; d++){
                if (!isEmpty[c][d]){
                    numberOfEmptySlots--;
                }
            }
        }

        //if there are no mergeable blocks available
        if (numberOfEmptySlots == 1 ){ //&&!mergeableBlocks()

            myTV2.bringToFront();
            myTV2.setText("You lose !");

            endGame = true;         //setting the endgame flag

        }

        //Log.d("number of Empty slots", "" + numberOfEmptySlots);

        while (!notEmpty && numberOfEmptySlots != 0){
            int counter = 0;

            int randomInt = rand.nextInt(numberOfEmptySlots);
            //Log.d("random integer is: ", "" + randomInt);


            //cycles through the isEmpty array to place the random block
            for (int j = 0; j < 4; j++){
                for (int i = 0; i < 4; i++){
                    if (isEmpty[j][i] == true){

                        if (randomInt == counter){
                            isEmpty[j][i] = false;
                            randCoord1 = GameBlock.LEFT_BOUNDARY + (j* SLOT_ISOLATION);
                            randCoord2 = GameBlock.UP_BOUNDARY + (i* SLOT_ISOLATION);
                            notEmpty = true;
                        }

                        counter++;
                    }
                }
            }
        }

        //instantiating a new block
        newBlock = new GameBlock(myContext, randCoord1, randCoord2, myRL, this); //Or any (x,y) of your choice
        myGBList.add(newBlock);
        //myRL.addView(newBlock);

    }


    //method for setting the block direction
    public void setDirection(gameDirection newDirection) {
        Log.d("Direction is:", "" + newDirection);
        if (currentGameDirection != newDirection)       //settting the game direction
            currentGameDirection = newDirection;

        if (!endGame) {
            for (GameBlock newBlock : myGBList) {
                newBlock.setBlockDirection(newDirection);
            }
        }

    }

    //method for checking if a spot on the grid is occupied or not
    public boolean isOccupied(int x, int y){

        for (GameBlock newBlock : myGBList){
            if (newBlock.getCoord()[0] == x && newBlock.getCoord()[1] == y)
                return true;
        }

        return false;
    }

    //method for finding a gameblock at given x and y coordinates
    public static GameBlock getBlockIndex(int x, int y){

        for (GameBlock gb: myGBList){
            if (gb.getCoord()[0] == x && gb.getCoord()[1] == y){
                return gb;
            }
        }
        return null;
    }


    //method for checking if there are any mergeable blocks left at the end
    public boolean checkMergableBlocks() {
        int[] temp;

        GameBlock tempBlock = null;


        if (numberOfEmptySlots == 1) {
            for (GameBlock gb : myGBList) {
                temp = gb.getCoord();
                if (temp[1] != GameBlock.UP_BOUNDARY && temp[1] != GameBlock.DOWN_BOUNDARY) {           //checking the blocks in the middle 4
                    if (temp[0] != GameBlock.LEFT_BOUNDARY && temp[0] != GameBlock.RIGHT_BOUNDARY) {

                        tempBlock = getBlockIndex(temp[0] + SLOT_ISOLATION, temp[1]);      //checking to the right of ot
                        if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                            return true;
                        }

                        tempBlock = getBlockIndex(temp[0] - SLOT_ISOLATION, temp[1]);      //checing to the left of it
                        if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                            return true;
                        }

                        tempBlock = getBlockIndex(temp[0], temp[1] + SLOT_ISOLATION);      //checking below it
                        if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                            return true;
                        }

                        tempBlock = getBlockIndex(temp[0], temp[1] - SLOT_ISOLATION);      //checking above it
                        if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                            return true;
                        }

                    }
                } else if (temp[0] == GameBlock.LEFT_BOUNDARY && temp[1] != GameBlock.UP_BOUNDARY && temp[1] != GameBlock.DOWN_BOUNDARY) {        //if the block is located on the left boundary, and not the corners

                    tempBlock = getBlockIndex(temp[0], temp[1] + SLOT_ISOLATION);      //checking below it
                    if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                        return true;
                    }

                    tempBlock = getBlockIndex(temp[0], temp[1] - SLOT_ISOLATION);      //checking above it
                    if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                        return true;
                    }

                    tempBlock = getBlockIndex(temp[0] + SLOT_ISOLATION, temp[1]);      //checking to the right of it
                    if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                        return true;
                    }

                } else if (temp[0] == GameBlock.RIGHT_BOUNDARY && temp[1] != GameBlock.UP_BOUNDARY && temp[1] != GameBlock.DOWN_BOUNDARY) {       //if the block is located on the right boundary

                    tempBlock = getBlockIndex(temp[0], temp[1] + SLOT_ISOLATION);      //checking below it
                    if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                        return true;
                    }

                    tempBlock = getBlockIndex(temp[0], temp[1] - SLOT_ISOLATION);      //checking above it
                    if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                        return true;
                    }

                    tempBlock = getBlockIndex(temp[0] - SLOT_ISOLATION, temp[1]);      //chekcig to the left of it
                    if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                        return true;
                    }
                } else if (temp[1] == GameBlock.UP_BOUNDARY && temp[0] != GameBlock.LEFT_BOUNDARY && temp[0] != GameBlock.RIGHT_BOUNDARY) {

                    tempBlock = getBlockIndex(temp[0] + SLOT_ISOLATION, temp[1]);  //checking to the right
                    if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                        return true;
                    }

                    tempBlock = getBlockIndex(temp[0] - SLOT_ISOLATION, temp[1]);     //checing to the left
                    if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                        return true;
                    }

                    tempBlock = getBlockIndex(temp[0], temp[1] + SLOT_ISOLATION);      //checking below it
                    if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                        return true;
                    }
                } else if (temp[1] == GameBlock.DOWN_BOUNDARY && temp[0] != GameBlock.LEFT_BOUNDARY && temp[0] != GameBlock.RIGHT_BOUNDARY) {

                    tempBlock = getBlockIndex(temp[0] + SLOT_ISOLATION, temp[1]);  //checking to the right
                    if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                        return true;
                    }

                    tempBlock = getBlockIndex(temp[0] - SLOT_ISOLATION, temp[1]);     //checing to the left
                    if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                        return true;
                    }

                    tempBlock = getBlockIndex(temp[0], temp[1] - SLOT_ISOLATION); // checking above it
                    if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                        return true;
                    }
                } else if (temp[0] == GameBlock.LEFT_BOUNDARY && temp[1] == GameBlock.UP_BOUNDARY) {       //checking the top left corner block

                    tempBlock = getBlockIndex(temp[0] + SLOT_ISOLATION, temp[1]);  //cjecking to the right
                    if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                        return true;
                    }

                    tempBlock = getBlockIndex(temp[0], temp[1] + SLOT_ISOLATION);  //checking below it
                    if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                        return true;
                    }
                } else if (temp[0] == GameBlock.RIGHT_BOUNDARY && temp[1] == GameBlock.UP_BOUNDARY) {       //checking the top right corner block

                    tempBlock = getBlockIndex(temp[0] - SLOT_ISOLATION, temp[1]);  //cjecking to the left
                    if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                        return true;
                    }

                    tempBlock = getBlockIndex(temp[0], temp[1] + SLOT_ISOLATION);  //checking below it
                    if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                        return true;
                    }
                } else if (temp[0] == GameBlock.LEFT_BOUNDARY && temp[1] == GameBlock.DOWN_BOUNDARY) {       //checking the bottom left corner block

                    tempBlock = getBlockIndex(temp[0] + SLOT_ISOLATION, temp[1]);  //cjecking to the right
                    if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                        return true;
                    }

                    tempBlock = getBlockIndex(temp[0], temp[1] - SLOT_ISOLATION);  //checking above it
                    if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                        return true;
                    }
                } else if (temp[0] == GameBlock.RIGHT_BOUNDARY && temp[1] == GameBlock.DOWN_BOUNDARY) {       //checking the bottom right corner block

                    tempBlock = getBlockIndex(temp[0] - SLOT_ISOLATION, temp[1]);  //cjecking to the left
                    if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                        return true;
                    }

                    tempBlock = getBlockIndex(temp[0], temp[1] - SLOT_ISOLATION);  //checking above it
                    if (gb.getBlockNumber() == tempBlock.getBlockNumber() && tempBlock != null) {
                        return true;
                    }
                }

            }
        }
        return false;
    }
}


