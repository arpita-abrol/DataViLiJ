package Algorithm;

public class Configuration {
    public int MaxInterval, IterationInterval, NumberOfClustering;
    public boolean continous;
    public AlgorithmType algorithm;
    public Configuration(AlgorithmType algorithm){
        this.algorithm =algorithm;
    }
    public Configuration(int MaxInterval, int IterationInterval, boolean continous){
        this.MaxInterval= MaxInterval;
        this.IterationInterval = IterationInterval;
        this.continous=continous;
    }
    public void setAlgorithm(AlgorithmType algorithm){
        this.algorithm = algorithm;
    }

}
