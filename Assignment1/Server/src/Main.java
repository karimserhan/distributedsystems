import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Karim-pc1 on 9/13/2015.
 */
public class Main {
    public static void main(String[] args) throws FileNotFoundException {

        String filePath = args[0];
        FileReader inputFile = new FileReader(filePath);
        BufferedReader reader = new BufferedReader(inputFile);
        String line;

        try {
            while ((line = reader.readLine()) != null) {

                System.out.println(line);

            }
        }

        catch (IOException e){}

    }
}
