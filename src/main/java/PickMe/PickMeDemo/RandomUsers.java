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
            Integer[] temp1 = new Integer[4];
            Integer[] temp2 = new Integer[4];
            Integer[] temp3 = new Integer[4];
            Integer[] temp4 = new Integer[4];

            Arrays.fill(temp1, 0);
            Arrays.fill(temp2, 0);
            Arrays.fill(temp3, 0);
            Arrays.fill(temp4, 0);

            temp1[i] = 4;
            temp2[i] = 3;
            temp3[i] = 2;
            temp4[i] = 1;

            output.add(temp1);
            output.add(temp2);
            output.add(temp3);
            output.add(temp4);

        }
        for(int i = 0; i < 4; i++){
            Integer[] temp = new Integer[4];
            Arrays.fill(temp, 0);
            temp[i] = 3;
            output.add(temp);
        }
        for(int i = 0; i < 4; i++){
            Integer[] temp = new Integer[4];
            Arrays.fill(temp, 0);
            temp[i] = 2;
            output.add(temp);
        }
        for(int i = 0; i < 4; i++){
            Integer[] temp = new Integer[4];
            Arrays.fill(temp, 0);
            temp[i] = 1;
            output.add(temp);
        }

        return output;
    }
    public static List<Integer[]> getTwoInterests(){

        List<Integer[]> output = new ArrayList<>();

        output = addTwoElements(4, 3);
        output.addAll(addTwoElements(3, 4));
        output.addAll(addTwoElements(3, 2));
        output.addAll(addTwoElements(2, 3));
        output.addAll(addTwoElements(2, 1));
        output.addAll(addTwoElements(1, 2));


        output.addAll(addTwoElements(4, 2));
        output.addAll(addTwoElements(2, 4));
        output.addAll(addTwoElements(3, 1));
        output.addAll(addTwoElements(1, 3));

        output.addAll(addTwoElements(4, 1));
        output.addAll(addTwoElements(1, 4));







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

        output.addAll(addThreeElements(4, 3, 1));
        output.addAll(addThreeElements(4, 1, 3));
        output.addAll(addThreeElements(3, 4, 2));
        output.addAll(addThreeElements(3, 1, 4));
        output.addAll(addThreeElements(1, 4, 3));
        output.addAll(addThreeElements(1, 3, 4));

        output.addAll(addThreeElements(4, 2, 1));
        output.addAll(addThreeElements(4, 1, 2));
        output.addAll(addThreeElements(2, 1, 4));
        output.addAll(addThreeElements(1, 4, 2));
        output.addAll(addThreeElements(1, 2, 4));

        output.addAll(addThreeElements(3, 2, 1));
        output.addAll(addThreeElements(3, 1, 2));
        output.addAll(addThreeElements(2, 3, 1));
        output.addAll(addThreeElements(2, 1, 3));
        output.addAll(addThreeElements(1, 3, 2));
        output.addAll(addThreeElements(1, 2, 3));



        return output;
    }
    public static List<Integer[]> getThreeInterestsWithout(Integer i){
        List<Integer[]> output = new ArrayList<>();

        if(i == 1){
            output = addThreeElements(4, 3, 2);
            output.addAll(addThreeElements(4, 2, 3));
            output.addAll(addThreeElements(3, 4, 2));
            output.addAll(addThreeElements(3, 2, 4));
            output.addAll(addThreeElements(2, 4, 3));
            output.addAll(addThreeElements(2, 3, 4));
        }
        else if(i == 2){

            output.addAll(addThreeElements(4, 3, 1));
            output.addAll(addThreeElements(4, 1, 3));
            output.addAll(addThreeElements(3, 4, 2));
            output.addAll(addThreeElements(3, 1, 4));
            output.addAll(addThreeElements(1, 4, 3));
            output.addAll(addThreeElements(1, 3, 4));

        }
        else if(i == 3){
            output.addAll(addThreeElements(4, 2, 1));
            output.addAll(addThreeElements(4, 1, 2));
            output.addAll(addThreeElements(2, 1, 4));
            output.addAll(addThreeElements(1, 4, 2));
            output.addAll(addThreeElements(1, 2, 4));
        }
        else{
            output = addThreeElements(3, 2, 1);
            output.addAll(addThreeElements(3, 1, 2));
            output.addAll(addThreeElements(2, 3, 1));
            output.addAll(addThreeElements(2, 1, 3));
            output.addAll(addThreeElements(1, 3, 2));
            output.addAll(addThreeElements(1, 2, 3));

        }

        return output;
    }

    public static List<Integer[]> add(Integer num, List<Integer[]> arr){
        List<Integer[]> copy = new ArrayList<>();
        copy.addAll(arr);
        for(Integer[] o : copy){
            for(int i = 0; i < 4; i++){
                if(o[i] == 0){
                    o[i] = num;
                }
            }
        }
        return copy;
    }
    public static List<Integer[]> getFourInterests(){
        List<Integer[]> temp = new ArrayList<>();
        List<Integer[]> out = new ArrayList<>();

        for(int i = 1; i <= 4; i++){
            temp = getThreeInterestsWithout(i);
            out.addAll(add(i, temp));
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
