package ab.MyHeuristic;

import ab.vision.ABObject;

import java.util.ArrayList;

/**
 * Created by Hitarth on 27-06-2014.
 */
public class SubStructure {

    ArrayList<ArrayList<ABObject>> structure;
    ArrayList<ABObject> outerBlocks;
    private final int minimumGapBetweenBlocks = 4;
    ArrayList<ABObject> pigs;
    public boolean doesContain(ABObject pig)
    {
        return true;
    }

    public void addPig(ABObject pig)
    {
        pigs.add(pig);
    }

    public void addBlocks(ArrayList<ABObject> objects)
    {

    }





}
