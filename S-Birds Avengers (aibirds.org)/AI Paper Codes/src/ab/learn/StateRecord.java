package ab.learn;

/**
 * Created by Hitarth on 06-06-2014.
 */
import java.util.ArrayList;

public class StateRecord {
    int LENGTH = 12;
    public double [][][]state;
    public ArrayList record;

    public StateRecord() {
        state = new double[LENGTH][LENGTH][4];
        record = new ArrayList();
    }

    public StateRecord(double [][][]state) {
        this.state = state;
        record = new ArrayList();
    }

    public void addRecord(Object e) {
        record.add(e);
    }
}

