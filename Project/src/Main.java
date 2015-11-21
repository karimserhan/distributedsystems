import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
   public static void main(String[] args) {
       Scanner sc = new Scanner(System.in);
       System.out.print("Enter the file name: ");
       String fileName = sc.nextLine();

       // generatre trace file
       try {
           TraceFileGenerator generator = new TraceFileGenerator(2, 2, 4, 5, 2, 2);
           generator.generatreTraceFile(fileName);
       } catch (FileNotFoundException e) {
           System.out.println("Error writing to file");
       }

       // process trace file
       try {
           InputReader inputReader = new InputReader();
           inputReader.readTrace(fileName);
           boolean[][] trueEvtGraph = inputReader.getGraph();
           List<Integer> trueInitials = inputReader.getInitialTrueStates();
           List<Integer> trueFinals = inputReader.getFinalTrueStates();

           ArrayList<Integer> shortestSequence = PredicateControl
                   .getShortestSequence(trueInitials, trueFinals, trueEvtGraph);

           if (shortestSequence == null) {
               System.out.println("Predicate cannot be controlled");
           } else {
               System.out.print("Predicate controlled along path: ");
               for (int i = 0; i < shortestSequence.size(); i++) {
                   System.out.print(shortestSequence.get(i) + " ");
               }
               System.out.println("");
           }

       } catch (IOException e) {
           System.out.println("Error reading file");
       }


   }
}
