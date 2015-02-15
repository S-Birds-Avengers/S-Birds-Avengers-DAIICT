package ab.MyHeuristic;

import ab.utils.ABUtil;
import ab.vision.ABObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hitarth on 29-07-2014.
 */
public class ListOfSubStructure
{

    public ArrayList<ABObject> structureObjects;
    public static List<ABObject> allObjects;
    public double maxStructureY;
    public double minStructureY;
    public double maxStructureX;
    public double minStructureX;
    public ListOfSubStructure()
    {
        //this.allObjects = allObjects;
        structureObjects = new ArrayList<ABObject>();

    }

    public void addBlocks(ABObject obj) {
        List<ABObject> supports = ABUtil.getSupporters(obj, allObjects);
        List<ABObject> supportees = ABUtil.getSupportees(obj, allObjects);
        structureObjects.add(obj);
        allObjects.remove(obj);
        //boolean check1 = true;
        //boolean check2 = true;
        for (ABObject o2 : supports) {
            if (!structureObjects.contains(o2)) {
                //System.out.println("added :"+o2);
                //structureObjects.add(o2);
                //allObjects.remove(o2);
                addBlocks(o2);
                //check1 = false;
            }
        }

        for (ABObject o2 : supportees)
        {
            if(!structureObjects.contains(o2))
            {
                //System.out.println("added :"+o2);
                //structureObjects.add(o2);
                //allObjects.remove(o2);
                addBlocks(o2);
            }
        }

    }

    public boolean contains(ABObject block)
    {
        return structureObjects.contains(block);
    }

    public void addSupport(ABObject obj)
    {
        List<ABObject> supports = ABUtil.getSupporters(obj, allObjects);


    }

    public double getMaxStructureY()
    {
        double max = 0;
        if(maxStructureY==0)
        {
            for (ABObject o:structureObjects)
            {
                if(o.getCenterY()>max)
                {
                    max = o.getCenterY();
                }
            }
            maxStructureY = max;
        }
        return maxStructureY;
    }

    public double getMinStructureY()
    {
        double min = 100000;
        if(minStructureY==0)
        {
            for (ABObject o:structureObjects)
            {
                if(o.getCenterY()<min)
                {
                    min = o.getCenterY();
                }
            }
            minStructureY = min;
        }
        return minStructureY;
    }

    public double getMaxStructureX()
    {
        double max = 0;
        if(maxStructureX==0)
        {
            for (ABObject o:structureObjects)
            {
                if(o.getCenterX()>max)
                {
                    max = o.getCenterX();
                }
            }
            maxStructureX = max;
        }
        return maxStructureX;
    }

    public double getMinStructureX()
    {
        double min = 100000;
        if(minStructureX==0)
        {
            for (ABObject o:structureObjects)
            {
                if(o.getCenterX()<min)
                {
                    min = o.getCenterX();
                }
            }
            minStructureX = min;
        }
        return minStructureX;
    }

    public void addSupportee(ABObject obj)
    {
        List<ABObject> supportees = ABUtil.getSupportees(obj, allObjects);
    }

}
