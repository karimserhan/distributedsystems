import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Main {
   public static void main(String[] args) {
       Scanner sc = new Scanner(System.in);
       System.out.println("Enter the file name: ");
       String fileName = sc.nextLine();
       try {
           InputReader inputReader = new InputReader();
           inputReader.readTrace(fileName);
           boolean[][] trueEvtGraph = inputReader.getGraph();
           List<Integer> trueInitials = inputReader.getInitialTrueStates();
           List<Integer> trueFinals = inputReader.getFinalTrueStates();

           PredicateControl.getShortestSequence(trueInitials, trueFinals, trueEvtGraph);
       } catch (IOException e) {
           System.out.println("Error reading file");
       }


   }
}
