package Algorithm;

import settings.AppPropertyTypes;
import vilij.propertymanager.PropertyManager;

import java.util.ArrayList;

public class ClusteringAlgorithm implements AlgorithmType {
    private class Cluster1 extends ClusteringAlgorithm{
        Configuration configuration;
        private Cluster1(){
            configuration=new Configuration(this);
        }

        public Configuration getConfiguration() {
            return configuration;
        }

        public String toString(){
            return " Cluster Algorithm 1";
        }
    }
    private class Cluster2 extends ClusteringAlgorithm{
        Configuration configuration;
        private Cluster2(){
            configuration=new Configuration(this);
        }

        public Configuration getConfiguration() {
            return configuration;
        }

        public String toString(){
            return " Cluster Algorithm 2";
        }
    }
    private ArrayList<ClusteringAlgorithm> algorithmList;

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    public ClusteringAlgorithm() {
        algorithmList= new ArrayList<>();
    }
    public void testAdd(){
        if(algorithmList.size()!=2) {
            algorithmList.add(new Cluster1());
            algorithmList.add(new Cluster2());
        }
        }


    public ArrayList<ClusteringAlgorithm> getAlgorithmList(){
        return algorithmList;
    }

    @Override
    public String toString(){
        return PropertyManager.getManager().getPropertyValue(AppPropertyTypes.CLUSTERING_TYPE.name());
    }
}
