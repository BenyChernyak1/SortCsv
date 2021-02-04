import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class SortCsv {

    public static void main(String[] args) {
        FileOperations fileOperations = new FileOperations();
        String path = "resources/data.csv";
        String outputPath = "output";
        int columnNumber = 2;
        int maxLinesNumber = 5;

        try {
            String header = Files.lines(Paths.get(path))
                    .limit(1)
                    .collect(Collectors.toList()).get(0);

            int skipRows = 1; // for the header
            int fileNumber = 1;
            SortedMap<String, List<String>> collect;
            do {
                collect = Files.lines(Paths.get(path))
                        .skip(skipRows)
                        .limit(maxLinesNumber)
                        .collect(Collectors.groupingBy(
                                l -> String.valueOf(l.split(",")[columnNumber]),
                                TreeMap::new, Collectors.toList()));
                if (collect.size() != 0) {
                    fileOperations.writeToFile(collect, fileNumber++);
                    skipRows += maxLinesNumber;
                }
            } while(collect.size() != 0);



            List<String> filesToMerge = Files.walk(Paths.get(outputPath))
                    .filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".csv"))
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());

            while(filesToMerge.size() > 1) {
                String file1 = outputPath + File.separator + filesToMerge.get(0);
                String file2 = outputPath + File.separator + filesToMerge.get(1);
                if (filesToMerge.size() == 2) {
                    fileOperations.doMerge(file1, file2, columnNumber, header);
                } else {
                    fileOperations.doMerge(file1, file2, columnNumber, null);
                }
                // remove files
                Files.delete(Paths.get(file1));
                Files.delete(Paths.get(file2));

                filesToMerge = Files.walk(Paths.get(outputPath))
                        .filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".csv"))
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
