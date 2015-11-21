import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Main {
   public static void main(String[] args) {
       HashMap<Integer, List<Integer>> map = new HashMap<>();
       map.put(1, new LinkedList<Integer>());
       map.get(1).add(1);
       map.get(1).add(2);

       List<Integer> l = map.get(1);
       for (int i : l) {
           System.out.println(i);
       }
   }
}
