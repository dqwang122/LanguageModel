import java.io.*;
import java.util.*;
import java.util.HashMap;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class DataProcess {

    public static void CleanTrigramEnd(String filePath){
        HashMap<String, Integer> trigram_end = new HashMap<>();
        try
        {
            String encoding = "UTF-8";
            File file = new File(filePath);
            File outfile = new File(filePath + "_out");
            if (file.isFile() && file.exists())
            {
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);// 考虑到编码格式
                OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(outfile), encoding);
                BufferedReader bufferedReader = new BufferedReader(read);
                BufferedWriter bufferedWriter = new BufferedWriter(write);
                String lineTxt = null;


                while ((lineTxt = bufferedReader.readLine()) != null) {
                    String[] parts = lineTxt.split("\t");
                    String[] wlist = parts[1].split(";");
                    for(String str : wlist){
                        if(str!=null){
                            String[] keyvalue = str.split(":");
                            trigram_end.put(keyvalue[0], keyvalue[1].length());
                        }
                    }

                    StringBuilder builder = new StringBuilder(parts[0]+"\t");
                    for(String key : trigram_end.keySet()){
                        builder.append(key).append(":").append(trigram_end.get(key)).append(";");
                    }


                    bufferedWriter.write(builder.toString());
                    bufferedWriter.newLine();
                }

                bufferedReader.close();
                read.close();

                bufferedWriter.close();
                write.close();
            }
            else
            {
                System.out.println("File doesn't exist!");
            }
        }
        catch (Exception e)
        {
            System.out.println("Read From Trigram End Wrong!");
            e.printStackTrace();
        }
    }

    public static void CleanBigram(String filePath){
        List<String> context = new ArrayList<>();
        HashMap<String, Integer> bigram = new HashMap<>();
        int start_sum = 0;
        int end_sum = 0;
        try
        {
            String encoding = "UTF-8";
            File file = new File(filePath);
            File outfile = new File(filePath + "_out");
            if (file.isFile() && file.exists())
            {
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(outfile), encoding);
                BufferedWriter bufferedWriter = new BufferedWriter(write);

                String lineTxt = null;

                while ((lineTxt = bufferedReader.readLine()) != null) {
                    String[] parts = lineTxt.split("\t");
                    String[] wlist = parts[1].split(";");
                    StringBuilder line = new StringBuilder(parts[0] + "\t");
                    for(String str : wlist){
                        if(str.startsWith("start")){
                            String[] keyvalue = str.split(":");
                            start_sum += keyvalue[1].length();
                            line.append(keyvalue[0]).append(":").append(keyvalue[1].length()).append(";");
                        }
                        if(str.startsWith("start")){
                            String[] keyvalue = str.split(":");
                            end_sum += keyvalue[1].length();
                            line.append(keyvalue[0]).append(":").append(keyvalue[1].length()).append(";");
                        }
                    }
                    context.add(line.toString());
                }
                bufferedReader.close();
                read.close();

                StringBuilder builder = new StringBuilder();
                builder.append(start_sum).append("\t").append(end_sum);
                bufferedWriter.write(builder.toString());
                bufferedWriter.newLine();
                for(String str : context){
                    bufferedWriter.write(str);
                    bufferedWriter.newLine();
                }
                bufferedWriter.close();
                write.close();
            }
            else
            {
                System.out.println("File doesn't exist!");
            }
        }
        catch (Exception e)
        {
            System.out.println("Read From Bigram File Wrong!");
            e.printStackTrace();
        }
    }

    public static void CreateCandidate(String filePath, int low, int high){
        int linecnt = 0;
        int cnt = 0;

        String encoding = "UTF-8";
        File file = new File(filePath);
        File outfile = new File(filePath + "_candidate");

        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;

        try
        {
            InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);// 考虑到编码格式
            OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(outfile,true), encoding);

            bufferedReader = new BufferedReader(read);
            bufferedWriter = new BufferedWriter(write);

            if (file.isFile() && file.exists())
            {

                String lineTxt = null;


                while (linecnt < high && ((lineTxt = bufferedReader.readLine()) != null)) {
                    if(linecnt < low){
                        linecnt ++;
                        continue;
                    }
                    linecnt ++;

                    HashMap<String, Double> trigram = new HashMap<>();
                    String[] parts = lineTxt.split("\t");
                    List<String> wlist = Arrays.asList(parts[1].split(";"));

                    for(String str : wlist){
                        if(str!=null){
                            String[] keyvalue = str.split("=");

                            KNS P = new KNS(parts[0] + keyvalue[0], "result/LM_Trigram", "result/LM_Trigram_end", "result/LM_Bigram");
                            trigram.put(keyvalue[0], P.GetP());
                        }
                    }

                    if(wlist.size() < 10){
                        List<String> bigramCandidate = ReadFromFile.ReadCandidateFromFile(
                                "result/LM_Bigram_query",
                                parts[0].substring(1,2));
                        for(int i = 0; i < 10 - wlist.size() && i < bigramCandidate.size(); i ++){
                            KNS P = new KNS(parts[0] + bigramCandidate.get(i), "result/LM_Trigram", "result/LM_Trigram_end", "result/LM_Bigram");
                            trigram.put(bigramCandidate.get(i), P.GetP());
                        }
                    }

                    List<Map.Entry<String, Double>> infoIds = new ArrayList<>(trigram.entrySet());

                    Collections.sort(infoIds, new Comparator<Map.Entry<String, Double>>() {
                        public int compare(Map.Entry<String, Double> o1,
                                           Map.Entry<String, Double> o2) {
                            return o2.getValue().compareTo(o1.getValue());
                        }
                    });

                    StringBuilder builder = new StringBuilder(parts[0]+"\t");
                    for(int i = 0; i < 10 && i < trigram.size(); i++){
                        builder.append(infoIds.get(i)).append(";");
                    }


                    if(cnt % 1000 == 0){
                        System.out.println(builder.toString());
                    }
                    cnt ++;

                    bufferedWriter.write(builder.toString());
//                    bufferedWriter.write("Hello world!");
                    bufferedWriter.newLine();
                }

                bufferedReader.close();
                read.close();

                bufferedWriter.close();
                write.close();
            }
            else
            {
                System.out.println("File doesn't exist!");
            }
        }
        catch (Exception e)
        {
            System.out.println("Read From Trigram End Wrong!");
            e.printStackTrace();
        }

    }

    public static void main(String[] args){
//        CleanBigram("result/LM_Bigram_f");
//        CleanTrigramEnd("result/LM_Trigram_end");
        CreateCandidate("result/LM_Trigram_query_ah", 0, 4000);
    }
};


