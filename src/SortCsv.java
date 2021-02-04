import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class SortCsv {

    public static void main(String[] args) {
        FileOperations fileOperations = new FileOperations();
        String outputPath = "output";

        try {
            Files.createDirectories(Paths.get(outputPath));

            Properties properties = new Properties();
            properties.load(SortCsv.class.getClassLoader().getResourceAsStream("config.properties"));

            String path = properties.getProperty("path");
            int columnNumber = Integer.parseInt(properties.getProperty("columnNumber"));
            int maxLinesNumber = Integer.parseInt(properties.getProperty("maxLinesNumber"));

            String header = Files.lines(Paths.get(path))
                    .limit(1)
                    .collect(Collectors.toList()).get(0);

            writeToFiles(fileOperations, path, columnNumber, maxLinesNumber);

            mergeToFiles(fileOperations, outputPath, columnNumber, header);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void mergeToFiles(FileOperations fileOperations, String outputPath, int columnNumber, String header) throws IOException {
        List<String> filesToMerge = collectFilesToMerge(outputPath);

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

            filesToMerge = collectFilesToMerge(outputPath);
        }
    }

    private static List<String> collectFilesToMerge(String outputPath) throws IOException {
        return Files.walk(Paths.get(outputPath))
                .filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".csv"))
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }

    private static void writeToFiles(FileOperations fileOperations, String path, int columnNumber, int maxLinesNumber) throws Exception {
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
    }
}
