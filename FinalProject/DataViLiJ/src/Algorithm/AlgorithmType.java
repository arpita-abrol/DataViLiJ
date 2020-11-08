package Algorithm;


import java.util.ArrayList;

public interface AlgorithmType {
    ArrayList<? extends AlgorithmType> getAlgorithmList();
    void testAdd();
    Configuration getConfiguration();
}
