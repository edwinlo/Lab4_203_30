package lab4_203_30.uwaterloo.ca.lab4_203_30;

import android.content.Context;
import android.widget.ImageView;


/**
 * Created by Edwin Lo on 3/15/2017.
 */


abstract class GameBlockTemplate extends ImageView {

    abstract public void setDestination(GameLoopTask.gameDirection dir);

    abstract public void move();

    public GameBlockTemplate(Context myContext){
        super(myContext);
    }

}
