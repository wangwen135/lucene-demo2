package com.wwh.test.lucene.index;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ReadCSVFile {
    public static final String csvFile = "dataTable.csv";

    public static void main(String[] args) throws Exception {

        String path = ReadCSVFile.class.getClassLoader().getResource(csvFile).getFile();
        System.out.println(path);

        String path2 = ReadCSVFile.class.getClassLoader().getResource("").getPath();
        System.out.println(path2);

        List<String[]> list = getFileContent();

        list.forEach(x -> {
            for (int i = 0; i < x.length; i++) {
                System.out.print(x[i] + "--|--");
            }
            System.out.println();
        });

    }

    public static List<String[]> getFileContent() {
        List<String[]> rt = new ArrayList<String[]>();
        try {
            InputStream in = ReadCSVFile.class.getClassLoader().getResourceAsStream(csvFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("#")) {
                    continue;
                }
                if (line.trim().equals("")) {
                    continue;
                }
                String[] split = line.split(",");
                // 去除前后空格
                for (int i = 0; i < split.length; i++) {
                    split[i] = split[i].trim();
                }
                rt.add(split);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rt;
    }

}
