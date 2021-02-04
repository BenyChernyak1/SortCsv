import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.ThreadLocalRandom;

public class FileOperations {

    public void doMerge(String filePath1, String filePath2, int columnNumber, String header) {
        try (FileWriter writer = new FileWriter("output/sorted_data_" + ThreadLocalRandom.current().nextInt(1000000, Integer.MAX_VALUE) + ".csv");
             BufferedReader reader1 = new BufferedReader(new FileReader(filePath1));
             BufferedReader reader2 = new BufferedReader(new FileReader(filePath2))) {

            String line1 = reader1.readLine();
            String line2 = reader2.readLine();

            //header
            if (header != null) {
                writer.write(header);
                writer.write("\n");
            }

            while(line1 != null || line2 != null) {
                if(line1 == null) {
                    line2 = writeRowFromFile(writer, reader2, line2);
                } else if (line2 == null) {
                    line1 = writeRowFromFile(writer, reader1, line1);
                } else {
                    String key1 = getField(line1, columnNumber);
                    String key2 = getField(line2, columnNumber);
                    if (key1.compareTo(key2) < 0) {
                        line1 = writeRowFromFile(writer, reader1, line1);
                    } else {
                        line2 = writeRowFromFile(writer, reader2, line2);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String writeRowFromFile(FileWriter writer, BufferedReader reader, String line) throws IOException {
        writer.write(line);
        writer.write("\n");
        line = reader.readLine();
        return line;
    }

    private  String getField(String line, int columnNumber) {
        return line.split(",")[columnNumber]; // extract the value you want to sort on
    }

    public void writeToFile(SortedMap<String, List<String>> collect, int fileNumber) throws Exception {
        try(FileWriter writer = new FileWriter("output/sorted_data_" + fileNumber + ".csv")) {
            for (List<String> list : collect.values()) {
                for (String val : list) {
                    writer.write(val);
                    writer.write("\n");
                }
            }
        }
    }
}