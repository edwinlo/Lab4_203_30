
/*******************************************************
 * GameBlock.java             Feb 18th 2017
 *
 * Group # 30
 *
 * Lab Members: Edwin Lo, Madeleine Wang, Yizhou Yang
 *
 * This class extends the ImageView class so that we can set the
 * X and Y coordinates in such a way to make it look like the
 * GameBlock is sliding across the screen. This is done by
 * using a Linear-pixel-Acceleration Motion.
 * Implemented look-ahead algorithm that determines the
 * destination of the block with respect to merging blocks
 * with the same block number.
 *
 *
 *******************************************************/


package lab4_203_30.uwaterloo.ca.lab4_203_30;

import android.content.Context;
import android.graphics.Color;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GameBlock extends GameBlockTemplate {

    private final float IMAGE_SCALE = 1.0f;

    public static final int UP_BOUNDARY = 14;
    public static final int DOWN_BOUNDARY = 464;
    public static final int LEFT_BOUNDARY = 14;
    public static final int RIGHT_BOUNDARY= 464;

    private TextView myTV;
    private int blockNumber;

    private final float GB_ACC = 4.0f;
    private int myVelocity = 0;

    private final int OFFSET = 35;

    private boolean isMovingUp = false;
    private boolean isMovingDown = false;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;
    public boolean done = false; //is done moving

    private boolean toBeRemoved = false;

    private GameLoopTask.gameDirection myDir = GameLoopTask.gameDirection.NO_MOVEMENT;

    private int myCoordX;
    private int myCoordY;

    private int targetCoordY;
    private int targetCoordX;

    private RelativeLayout myRL;
    private GameLoopTask myGL;
    private GameBlock nextBlock;

    //constructor
    public GameBlock(Context myContext , int coordX, int coordY, RelativeLayout layout, GameLoopTask glt) {
        super(myContext);

        this.setImageResource(R.drawable.gameblock);
        this.setScaleX(IMAGE_SCALE);
        this.setScaleY(IMAGE_SCALE);

        myCoordX = coordX;
        myCoordY = coordY;

        myGL = glt;
        myRL = layout;

        blockNumber = GameLoopTask.rand.nextInt(2);

        if (blockNumber == 0){  blockNumber = 2;    }
        else if (blockNumber == 1){   blockNumber = 4;    }

        myTV = new TextView(myContext);
        myTV.bringToFront();

        myTV.setText("" + blockNumber);

        myTV.setX(myCoordX + OFFSET);
        myTV.setY(myCoordY + OFFSET);
        myTV.setTextColor(Color.BLACK);
        myTV.setTextSize(22);

        myRL.addView(this);
        myRL.addView(myTV);
        this.setX(coordX);
        this.setY(coordY);

    }
    //set block direction
    public void setBlockDirection (GameLoopTask.gameDirection blockDir){
        if(!isMovingUp && !isMovingDown && !isMovingLeft && !isMovingRight){
            this.myDir = blockDir;
            setDestination(blockDir);
        }
    }


    @Override
    public void setDestination(GameLoopTask.gameDirection dir) {        //look ahead algorithm that sets the destination of the block

        int[] slotCheck = new int[4];

        myDir = dir;

        int testCoord;

        int sameBlocks = 0;
        int blockNumber = 0;
        int slotNumber = 0;
        int mergeNumber = 0;

        GameBlock temp = null;

        //in the cases of each direction
        switch (dir) {
            case UP:
                int counter = 0;

                testCoord = UP_BOUNDARY;
                blockNumber = 0;
                slotNumber = 0;
                mergeNumber = 0;

                while (testCoord != myCoordY) {
                    if (myGL.isOccupied(myCoordX, testCoord)) {
                        counter++;

                        //Log.d("testCoord", "" + testCoord);
                        blockNumber++;

                        temp = GameLoopTask.getBlockIndex(myCoordX, testCoord);
                        slotCheck[counter] = GameLoopTask.getBlockIndex(myCoordX, testCoord).getBlockNumber();

                        //records the number of blocks that share the same number
                        if (this.getBlockNumber() == slotCheck[counter]) {
                            sameBlocks++;
                        }
                    }
                    slotNumber++;

                    testCoord += GameLoopTask.SLOT_ISOLATION;
                }


                //setting the target coordinates
                targetCoordY = myCoordY - mergingLogic(temp, blockNumber, slotNumber, sameBlocks, mergeNumber,testCoord) * GameLoopTask.SLOT_ISOLATION;

                break;
            case DOWN:

                counter = 0;

                testCoord = DOWN_BOUNDARY;
                blockNumber = 0;
                slotNumber = 0;
                mergeNumber = 0;
                sameBlocks = 0;

                while (testCoord != myCoordY) {
                    if (myGL.isOccupied(myCoordX, testCoord)) {
                        counter++;

                        blockNumber++;

                        temp = GameLoopTask.getBlockIndex(myCoordX, testCoord);

                        slotCheck[counter] = temp.getBlockNumber();

                        //records the number of blocks that share the same number
                        if (this.getBlockNumber() == temp.getBlockNumber()) {
                            sameBlocks++;
                        }
                    }

                    slotNumber++;
                    testCoord -= GameLoopTask.SLOT_ISOLATION;
                }

                //setting the target coordinates
                targetCoordY = myCoordY + mergingLogic(temp, blockNumber, slotNumber, sameBlocks, mergeNumber,testCoord) * GameLoopTask.SLOT_ISOLATION;

                break;
            case LEFT:

                counter = 0;
                testCoord = LEFT_BOUNDARY;
                blockNumber = 0;
                slotNumber = 0;
                mergeNumber = 0;
                sameBlocks = 0;


                while (testCoord != myCoordX) {
                    if (myGL.isOccupied(testCoord, myCoordY)) {
                        counter++;
                        blockNumber++;

                        temp = GameLoopTask.getBlockIndex(testCoord, myCoordY);

                        slotCheck[counter] = temp.getBlockNumber();

                        //records the number of blocks that shares the same number
                        if (this.getBlockNumber() == temp.getBlockNumber()) {
                            sameBlocks++;
                        }
                    }

                    slotNumber++;
                    testCoord += GameLoopTask.SLOT_ISOLATION;
                }

                //setting the target coordinates
                targetCoordX = myCoordX - mergingLogic(temp, blockNumber, slotNumber, sameBlocks, mergeNumber, testCoord) * GameLoopTask.SLOT_ISOLATION;
                break;
            case RIGHT:
                counter = 0;
                testCoord = RIGHT_BOUNDARY;
                blockNumber = 0;
                slotNumber = 0;
                mergeNumber = 0;

                while (testCoord != myCoordX) {
                    if (myGL.isOccupied(testCoord, myCoordY)) {
                        counter++;
                        blockNumber++;

                        temp = GameLoopTask.getBlockIndex(testCoord, myCoordY);
                        slotCheck[counter] = temp.getBlockNumber();

                        //records the number of blocks that share the same number
                        if (this.getBlockNumber() == temp.getBlockNumber()) {
                            sameBlocks++;
                        }
                    }
                    slotNumber++;
                    testCoord -= GameLoopTask.SLOT_ISOLATION;
                }

                //setting the target coordinates
                targetCoordX = myCoordX + mergingLogic(temp, blockNumber, slotNumber, sameBlocks, mergeNumber, testCoord) * GameLoopTask.SLOT_ISOLATION;
                break;

        }
    }

    //merging logic for merging the blocks
    public int mergingLogic(GameBlock gb, int numBlocks, int slotNumber, int sameBlocks, int mergeNumber, int testCoord ){

        switch (slotNumber){

            //if the block is next to a boundary
            case 0:
                //doesn't merge with anything
                break;

            //if the block is 1 slot from the boundary
            case 1:
                if (numBlocks == 1){    //if there is 1 block in the path
                    if (this.getBlockNumber() == gb.getBlockNumber()){
                        mergeNumber++;
                        toBeRemoved = true; //merged and removed
                    }
                }
                break;

            //if the block is 2 slots from the boundary
            case 2:
                if (numBlocks == 0){
                   //doesn't merge if there are no blocks in the path
                }   else if (numBlocks == 1){           //if there is 1 block in the path
                    if (this.getBlockNumber() == gb.getBlockNumber()){
                        mergeNumber++;
                        toBeRemoved = true;
                    }
                }    else if (numBlocks == 2){      //if there are 2 blocks in the path
                    if (this.getBlockNumber() == gb.getBlockNumber()){
                        mergeNumber++;

                        if (sameBlocks == 2){       //if there are 2 blocks with the same number in the path
                            //doesn't merge with anything
                        }   else if (sameBlocks == 1) {
                            toBeRemoved = true;     //merges if there is 1 block with the same number
                        }
                    }
                }   else{
                    //doesn't merge with anything
                }
                break;

            //if the block is 3 slots from the boundary
            case 3:
                if (numBlocks == 0){        //if there are no blocks in the path
                    //doesn't merge with anything
                }    else if(numBlocks == 1){      //if there is 1 block in the path
                    if (this.getBlockNumber() == gb.getBlockNumber()){
                        mergeNumber++;
                        toBeRemoved = true;     //merges and removed
                    }
                }    else if (numBlocks == 2){      //if there are 2 blocks in the path

                    if (this.getBlockNumber() == gb.getBlockNumber()) {
                        if (sameBlocks == 1) {
                            mergeNumber++;
                            toBeRemoved = true;
                        }
                    }
                }    else if (numBlocks == 3){      //if there are 3 blocks in the path
                    if (sameBlocks == 1){       //in the case if 1 of them shares the same number
                        if (this.getBlockNumber() == gb.getBlockNumber()){
                            mergeNumber++;
                            toBeRemoved = true;     //merged and removed
                        }
                    }   else if(sameBlocks == 2){   //in the case if 2 of them shares the same number
                        mergeNumber++;

                        //checking if the block two blocks from the current block shares the same number or not
                        switch(myDir) {
                            case UP:
                                if (this.getBlockNumber() != GameLoopTask.getBlockIndex(myCoordX, testCoord - 2 * GameLoopTask.SLOT_ISOLATION).getBlockNumber()) {
                                    toBeRemoved = true; //merged and removed
                                }
                                break;
                            case DOWN:
                                if (this.getBlockNumber() != GameLoopTask.getBlockIndex(myCoordX, testCoord + 2 * GameLoopTask.SLOT_ISOLATION).getBlockNumber()) {
                                    toBeRemoved = true; //merged and removed
                                }
                                break;
                            case LEFT:
                                if (this.getBlockNumber() != GameLoopTask.getBlockIndex(testCoord - 2 * GameLoopTask.SLOT_ISOLATION, myCoordY).getBlockNumber()) {
                                    toBeRemoved = true; //merged and removed
                                }
                                break;
                            case RIGHT:
                                if (this.getBlockNumber() != GameLoopTask.getBlockIndex(testCoord + 2 * GameLoopTask.SLOT_ISOLATION, myCoordY).getBlockNumber()) {
                                    toBeRemoved = true;     //merged and removed
                                }
                        }

                    }   else if(sameBlocks == 3){   //in the case that 3 blocks share the same number
                        mergeNumber += 2;
                        toBeRemoved = true; //merged and removed
                    }
                }
        }

        //records the most recent block as the next block
        nextBlock = gb;

        //adds the block to the moving blocks linked list to indicate that it can be moved
        if ((slotNumber - numBlocks + mergeNumber) != 0){
            GameLoopTask.movingBlocks.add(this);
        }

        //Log.d("slot nUmber", ""+ slotNumber);
        //Log.d("block number", "" + numBlocks);
        //Log.d("mergeNumber", "" + mergeNumber);

        return (slotNumber - numBlocks + mergeNumber);
    }


    //move block with calcualed coordinates; increase velocity with acceleration every clock period
    public void move(){

        switch(myDir){
            case UP:

                //only runs if it is not already moving in a direction
                if (myCoordY > targetCoordY && (!isMovingDown && !isMovingRight && !isMovingLeft)){

                    isMovingUp = true;

                    //Linear-Pixel-Acceleration motion
                    if((myCoordY - myVelocity) <= targetCoordY){
                        myCoordY = targetCoordY;
                        myVelocity = 0;
                        isMovingUp = false;
                        done = true;
                        //myDir = GameLoopTask.gameDirection.NO_MOVEMENT;
                    }   else {
                        myCoordY -= myVelocity;
                        myVelocity += GB_ACC;
                    }
                }   else if(isMovingDown || isMovingRight || isMovingLeft){     //continues the motion that it was
                    if (isMovingDown){                                          //originally in if the block was
                        myDir = GameLoopTask.gameDirection.DOWN;                //already in motion
                    }   else if(isMovingRight){
                        myDir = GameLoopTask.gameDirection.RIGHT;
                    }   else if(isMovingLeft){
                        myDir = GameLoopTask.gameDirection.LEFT;
                    }
                    move();
                }

                break;
            case DOWN:

                //only runs if it is not already moving in a direction
                if(myCoordY < targetCoordY && (!isMovingUp && !isMovingRight && !isMovingLeft)){

                    isMovingDown = true;

                    //Linear-Pixel-Acceleration-motion
                    if((myCoordY + myVelocity) >= targetCoordY){
                        myCoordY = targetCoordY;
                        myVelocity = 0;
                        isMovingDown = false;
                        done = true;

                    }   else{
                        myCoordY += myVelocity;
                        myVelocity += GB_ACC;
                    }
                }   else if(isMovingUp || isMovingRight || isMovingLeft){           //continues in the motion that
                    if (isMovingUp){                                                // it was already moving in
                        myDir = GameLoopTask.gameDirection.UP;
                    }   else if(isMovingRight){
                        myDir = GameLoopTask.gameDirection.RIGHT;
                    }   else if(isMovingLeft){
                        myDir = GameLoopTask.gameDirection.LEFT;
                    }
                    move();
                }
                break;
            case LEFT:

                //only runs if the block is not already in motion
                if (myCoordX > targetCoordX && (!isMovingRight && !isMovingDown && !isMovingUp)){

                    isMovingLeft = true;

                    //Linear-Pixel-Acceleration Motion
                    if((myCoordX - myVelocity) <= targetCoordX){
                        myCoordX = targetCoordX;
                        myVelocity = 0;
                        isMovingLeft =false;
                        done = true;
                        //myDir = GameLoopTask.gameDirection.NO_MOVEMENT;
                    }   else{
                        myCoordX -= myVelocity;
                        myVelocity +=GB_ACC;
                    }
                }   else if(isMovingDown || isMovingRight || isMovingUp){          //continues in the motion that
                    if (isMovingDown){                                              // it is already moving in
                        myDir = GameLoopTask.gameDirection.DOWN;
                    }   else if(isMovingRight){
                        myDir = GameLoopTask.gameDirection.RIGHT;
                    }   else if(isMovingUp){
                        myDir = GameLoopTask.gameDirection.UP;
                    }
                    move();
                }
                break;
            case RIGHT:

                //targetCoordX = RIGHT_BOUNDARY;

                //only runs if the block is idle
                if (myCoordX < targetCoordX && (!isMovingDown && !isMovingLeft && !isMovingUp)){

                    isMovingRight = true;
                    //doneMoving = true;

                    //Linear-Pixel-Acceleration Motion
                    if ((myCoordX + myVelocity) >= targetCoordX){
                        myCoordX = targetCoordX;
                        myVelocity = 0;
                        isMovingRight = false;
                        done = true;
                        //myDir = GameLoopTask.gameDirection.NO_MOVEMENT;
                    }   else {
                        myCoordX +=myVelocity;
                        myVelocity += GB_ACC;
                    }
                }   else if(isMovingDown || isMovingUp || isMovingLeft){        //continues in the motion that it was
                    if (isMovingDown){                                          //already moving in
                        myDir = GameLoopTask.gameDirection.DOWN;
                    }   else if(isMovingUp){
                        myDir = GameLoopTask.gameDirection.UP;
                    }   else if(isMovingLeft){
                        myDir = GameLoopTask.gameDirection.LEFT;
                    }
                    move();
                }
                break;
            default:
                //continues in the motion that it was moving in
                if(isMovingUp || isMovingDown || isMovingRight || isMovingLeft){
                    if(isMovingUp){
                        myDir = GameLoopTask.gameDirection.UP;
                    }   else if (isMovingDown){
                        myDir = GameLoopTask.gameDirection.DOWN;
                    }   else if(isMovingRight){
                        myDir = GameLoopTask.gameDirection.RIGHT;
                    }   else if(isMovingLeft){
                        myDir = GameLoopTask.gameDirection.LEFT;
                    }

                    move();
                }
                break;
        }

        //setting the new X and Y coordinates to the ImageView
        this.setX(myCoordX);
        this.setY(myCoordY);
        myTV.setX(myCoordX + OFFSET);
        myTV.setY(myCoordY + OFFSET);

        if(myVelocity == 0)
            myDir = GameLoopTask.gameDirection.NO_MOVEMENT;

    }

    //method for checking white spaces
    public void checkWhiteSpaces(){

        switch(myDir) {
            case UP:
                for (;;){
                    //checks if the block next to it is null
                    if (GameLoopTask.getBlockIndex(myCoordX, myCoordY - GameLoopTask.SLOT_ISOLATION) == null) {
                        myCoordY = myCoordY - GameLoopTask.SLOT_ISOLATION;
                    } else if (myCoordY == UP_BOUNDARY) {
                        break;
                    }
                }
                break;

            case DOWN:

               for(;;) {
                    //checks if the block next to it is null
                    if (GameLoopTask.getBlockIndex(myCoordX, myCoordY + GameLoopTask.SLOT_ISOLATION) == null) {
                        myCoordY = myCoordY + GameLoopTask.SLOT_ISOLATION;
                    } else if (myCoordY == DOWN_BOUNDARY) {
                        break;
                    }
                }
                break;

            case RIGHT:

                for(;;) {
                    //checks if the block next to it is null
                    if (GameLoopTask.getBlockIndex(myCoordX + GameLoopTask.SLOT_ISOLATION, myCoordY) == null) {
                        myCoordX += GameLoopTask.SLOT_ISOLATION;
                    } else if (myCoordX == RIGHT_BOUNDARY){
                        break;
                    }
                }
                break;
            case LEFT:

                for(;;) {
                    //checks if the block next to it is null
                    if (GameLoopTask.getBlockIndex(myCoordX - GameLoopTask.SLOT_ISOLATION, myCoordY) == null) {
                        myCoordX -= GameLoopTask.SLOT_ISOLATION;
                    } else if (myCoordX == LEFT_BOUNDARY){
                        break;
                    }
                }
                break;
            default:
                break;
            }

        //setting the coordinates
        this.setX(myCoordX);
        this.setY(myCoordY);
        myTV.setX(myCoordX + OFFSET);
        myTV.setY(myCoordY + OFFSET);
    }

    public int getBlockNumber(){
        return this.blockNumber;
    }

    public int[] getCoord(){
        int coord[] = new int[2];
        coord[0] = myCoordX;
        coord[1] = myCoordY;

        return coord;
    }

    //method that returns whether the block needs to be removed or not
    public boolean getToBeRemoved(){
        return toBeRemoved;
    }

    //removes the block from the layout
    public void destroyMe(){
        myRL.removeView(this);
        myRL.removeView(myTV);
        doubleNumber();     //doubles the number of the next block
    }

    //method that sets the block number
    public void setBlockNumber(){
        blockNumber = blockNumber * 2;
    }

    //method the doubles the number of the next block
    private void doubleNumber (){
        nextBlock.setBlockNumber(); // increase number
        nextBlock.myTV.setText(Integer.toString(nextBlock.getBlockNumber()));
    }

}
