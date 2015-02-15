package ab.learn;

/**
 * Created by Hitarth on 06-06-2014.
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class StructureParser {

    ArrayList<Level> levels = new ArrayList<Level>();
    public StructureParser(String filePath) {
        BufferedReader br = null;
        try {
            String currentLine;
            br = new BufferedReader(new FileReader(filePath));
            int _indx = 0;
            while ((currentLine = br.readLine()) != null) {
                String []elements = currentLine.split(",");
                HashMap<Double, Double> angleScore = new HashMap<Double, Double>();
                double [][][]structure = new double[4][4][4];
                double angle = Double.parseDouble(elements[1]);
                double score = Double.parseDouble(elements[3]);
                angleScore.put(angle, score);
                int z = 0;
                String []structureString = elements[2].split(" ");
                for (int i = 0; i < 4; i++)
                    for (int j = 0; j < 4; j++)
                        for (int k = 0; k < 4; k++)
                            structure[i][j][k] = Double.parseDouble(structureString[z++]);
                Level parseLevel = new Level(structure, Integer.parseInt(elements[0]), angleScore);
                double distance = getStructureDistance(parseLevel.structure);
                if (distance >= 19)
                    System.out.println(_indx + " - " + distance);
                if (distance >= 40) {
                    levels.add(parseLevel);
                } else {
                    Level nearestLevel = getNearestLevel(parseLevel);
                    nearestLevel.angleScore.put(angle, score);
                }
                _indx++;
            }
            System.out.println("structure - "+ levels.size());
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public double getStructureDistance(double[][][] structure)
    {
        double[][] distances = new double[levels.size()][2];
        for (int index = 0; index < levels.size(); index++) {
            distances[index][0] = levels.get(index).distance(structure);
            distances[index][1] = index;
        }
        Arrays.sort(distances, new Comparator<double[]>() {
            public int compare(final double num1[], final double num2[]) {
                if (num1[0] >= num2[0]) return 1;
                return -1;
            }
        });
        //for (int i = 0; i < distances.length; i++)
        //System.out.println("d - "+i+" "+distances[i][0]);
        if (distances.length == 0) return 100000;
        return distances[0][0];
    }

    public Level getNearestLevel(double[][][] structure)
    {
        double[][] distances = new double[levels.size()][2];
        for (int index = 0; index < levels.size(); index++) {
            distances[index][0] = levels.get(index).distance(structure);
            distances[index][1] = index;
        }
        Arrays.sort(distances, new Comparator<double[]>() {
            public int compare(final double num1[], final double num2[]) {
                if (num1[0] >= num2[0]) return 1;
                return -1;
            }
        });
        if (levels.size() == 0) return null;
        return levels.get((int)distances[0][1]);
    }

    public Level getNearestLevel(Level currentLevel) {
        double [][][]structure = currentLevel.structure;
        double[][] distances = new double[levels.size()][2];
        for (int index = 0; index < levels.size(); index++) {
            distances[index][0] = levels.get(index).distance(structure);
            distances[index][1] = index;
        }
        Arrays.sort(distances, new Comparator<double[]>() {
            public int compare(final double num1[], final double num2[]) {
                if (num1[0] >= num2[0]) return 1;
                return -1;
            }
        });
        if (levels.size() == 0) return currentLevel;
        return levels.get((int)distances[0][1]);
    }

    public static void main(String args[]) {
        new StructureParser(args[0]);
    }
}
