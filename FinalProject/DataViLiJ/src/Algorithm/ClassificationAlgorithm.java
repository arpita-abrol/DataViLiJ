package Algorithm;




import settings.AppPropertyTypes;
import vilij.propertymanager.PropertyManager;

import java.util.ArrayList;

public class ClassificationAlgorithm implements AlgorithmType {
    private class Classification1 extends ClassificationAlgorithm{
        Configuration configuration;
        private Classification1(){
            configuration= new Configuration(this);
        }

        public Configuration getConfiguration() {
            return configuration;
        }

        @Override
        public String toString(){
            return "Classification Algorithm 1";
        }

    }
    private class Classification2 extends ClassificationAlgorithm{
        Configuration configuration;
        private Classification2(){
            configuration=new Configuration(this);
        }

        public Configuration getConfiguration() {
            return configuration;
        }

        @Override
        public String toString(){
            return "Classification Algorithm 2";
        }

    }

    private ArrayList<ClassificationAlgorithm> algorithmList;
    public ClassificationAlgorithm(){
        algorithmList= new ArrayList<>();
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    public ArrayList<ClassificationAlgorithm> getAlgorithmList(){
        return algorithmList;
    }

    @Override
    public void testAdd() {
        if(algorithmList.size()!=2){
            algorithmList.add(new Classification1());
            algorithmList.add(new Classification2());
        }

    }

    @Override
    public String toString(){
        return PropertyManager.getManager().getPropertyValue(AppPropertyTypes.CLASSIFICATION_TYPE.name());
    }
}
