package PickMe.PickMeDemo;
import java.util.*;
import java.util.stream.Collectors;

public class RandomUsers {

    private static List<Integer[]> addTwoElements(Integer f, Integer s){

        List<Integer[]> output = new ArrayList<>();
        for(int i = 0; i < 3; i++){
            Queue<Integer> elements = new LinkedList<>();
            Integer first = f;
            for(int j = i+1; j < 4; j++){
                Integer[] possible = new Integer[4];
                Arrays.fill(possible, 0);
                possible[i] = first;
                possible[j] = s;

                output.add(possible);
            }
        }
        return output;
    }
    private static List<Integer[]> addThreeElements(Integer f, Integer s, Integer t){

        List<Integer[]> output = new ArrayList<>();
        for(int i = 0; i < 2; i++){
            for(int j = i+1; j < 3; j++){
                for(int k = j + 1; k < 4; k++){
                    Integer[] possible = new Integer[4];
                    Arrays.fill(possible, 0);
                    possible[i] = f;
                    possible[j] = s;
                    possible[k] = t;

                    output.add(possible);
                }
            }
        }
        return output;
    }


    public static List<Integer[]> getOneInterests(){

        List<Integer[]> output = new ArrayList<>();

        for(int i = 0; i < 4; i++){
            Integer[] temp = new Integer[4];
            Arrays.fill(temp, 0);
            temp[i] = 4;
            output.add(temp);
        }

        return output;
    }
    public static List<Integer[]> getTwoInterests(){

        List<Integer[]> output = new ArrayList<>();

        output = addTwoElements(4, 3);
        output.addAll(addTwoElements(3, 4));

        return output;
    }
    public static List<Integer[]> getThreeInterests(){
        List<Integer[]> output = new ArrayList<>();

        output = addThreeElements(4, 3, 2);
        output.addAll(addThreeElements(4, 2, 3));
        output.addAll(addThreeElements(3, 4, 2));
        output.addAll(addThreeElements(3, 2, 4));
        output.addAll(addThreeElements(2, 4, 3));
        output.addAll(addThreeElements(2, 3, 4));

        return output;
    }
    public static List<Integer[]> getFourInterests(){
        List<Integer[]> temp = getThreeInterests();

        for(Integer[] o : temp){
            for(int i = 0; i < 4; i++){
                if(o[i] == 0){
                    o[i] = 1;
                }
            }
        }

        return temp;

    }


    public static List<Integer[]> getInterests(int i){
        if(i == 1){
            return getOneInterests();
        }
        else if(i == 2){
            return getTwoInterests();
        }
        else if(i == 3){
            return getThreeInterests();
        }
        else return getFourInterests();
    }

}
