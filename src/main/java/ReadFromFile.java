import org.apache.hadoop.util.hash.Hash;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReadFromFile {

    public static HashMap<String, Integer> ReadTrigramFromFile(String filePath, String prefix){
        HashMap<String, Integer> trigram = new HashMap<>();
        try
        {
            String encoding = "UTF-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists())
            {
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;

                while ((lineTxt = bufferedReader.readLine()) != null) {
                    if(lineTxt.startsWith(prefix)){
                        String[] parts = lineTxt.split("\t");
                        String[] wlist = parts[1].split(";");
                        for(String str : wlist){
                            if(str!=null){
                                String[] keyvalue = str.split(":");
                                trigram.put(keyvalue[0], Integer.parseInt(keyvalue[1]));
                            }
                        }
                        break;
                    }
                }
                bufferedReader.close();
                read.close();
            }
            else
            {
                System.out.println("File doesn't exist!");
            }
        }
        catch (Exception e)
        {
            System.out.println("Read From Trigram File Wrong!");
            e.printStackTrace();
        }
        return trigram;
    }

    public static HashMap<String, Integer> ReadTrigramEndFromFile(String filePath, String suffix){
        HashMap<String, Integer> trigram_end = new HashMap<>();
        try
        {
            String encoding = "UTF-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists())
            {
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;


                while ((lineTxt = bufferedReader.readLine()) != null) {
                    String[] parts = lineTxt.split("\t");
                    if(parts[0].equals(suffix)){
                        String[] wlist = parts[1].split(";");
                        for(String str : wlist){
                            if(str!=null){
                                String[] keyvalue = str.split(":");
                                trigram_end.put(keyvalue[0], Integer.parseInt(keyvalue[1]));
                            }
                        }
                        break;
                    }
                }
                bufferedReader.close();
                read.close();
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
        return trigram_end;
    }

    public static HashMap<String, Integer> ReadBigramFromFile(String filePath, String prefix, String suffix){
        HashMap<String, Integer> bigram = new HashMap<>();
        int start = 1;
        int end = 0;
        try
        {
            String encoding = "UTF-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists())
            {
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;

                while ((lineTxt = bufferedReader.readLine()) != null) {
                    if(start == 1){
                        String[] sum = lineTxt.split("\t");
                        if(!sum[0].equals(sum[1])){
                            System.out.println("Start_sum doesn't equal with end_sum");
                        }
                        try {
                            bigram.put("total", Integer.parseInt(sum[0]));
                        }
                        catch(Exception e){
                            System.out.println("Lose the total number of bigrams");
                            e.printStackTrace();
                        }
                        start = 0;
                        continue;
                    }
                    String[] parts = lineTxt.split("\t");
                    if(parts[0].equals(prefix)){
                        String[] wlist = parts[1].split(";");
                        for(String str : wlist){
                            String[] keyvalue = str.split(":");
                            if(keyvalue[0].equals("start")){
                                bigram.put("start",Integer.parseInt(keyvalue[1]));
                            }
                        }
                        end ++;
                    }
                    if(parts[0].equals(suffix)){
                        String[] wlist = parts[1].split(";");
                        for(String str : wlist){
                            String[] keyvalue = str.split(":");
                            if(keyvalue[0].equals("end")){
                                bigram.put("end",Integer.parseInt(keyvalue[1]));
                            }
                        }
                        end ++;
                    }
                    if(end==2) {
                        break;
                    }
                }
                bufferedReader.close();
                read.close();
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
        return bigram;
    }

    public static HashMap<String, HashMap<String,Integer>> GetTrigramFromFile(String filePath){
        HashMap<String, HashMap<String, Integer>> TrigramTable = new HashMap<>();
        try
        {
            String encoding = "UTF-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists())
            {
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;

                while ((lineTxt = bufferedReader.readLine()) != null) {
                    HashMap<String, Integer> trigram = new HashMap<>();
                    String[] parts = lineTxt.split("\t");

                    TrigramTable.put(parts[0], trigram);

                    String[] wlist = parts[1].split(";");
                    for(String str : wlist){
                        if(str!=null){
                            String[] keyvalue = str.split(":");
                            trigram.put(keyvalue[0], Integer.parseInt(keyvalue[1]));
                        }
                    }

                }

                System.out.println("Read From Trigram File Success!");

                bufferedReader.close();
                read.close();
            }
            else
            {
                System.out.println("File doesn't exist!");
            }
        }
        catch (Exception e)
        {
            System.out.println("Read From Trigram File Wrong!");
            e.printStackTrace();
        }
        return TrigramTable;
    }

    public static HashMap<String, HashMap<String,Integer>> GetTrigramEndFromFile(String filePath){
        HashMap<String, HashMap<String,Integer>> TrigramEndTable = new HashMap<>();
        try
        {
            String encoding = "UTF-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists())
            {
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;


                while ((lineTxt = bufferedReader.readLine()) != null) {
                    HashMap<String, Integer> trigram_end = new HashMap<>();
                    String[] parts = lineTxt.split("\t");

                    TrigramEndTable.put(parts[0], trigram_end);

                    String[] wlist = parts[1].split(";");
                    for(String str : wlist){
                        if(str!=null){
                            String[] keyvalue = str.split(":");
                            trigram_end.put(keyvalue[0], Integer.parseInt(keyvalue[1]));
                        }
                    }
                }

                System.out.println("Read From Trigram End Success!");

                bufferedReader.close();
                read.close();
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
        return TrigramEndTable;
    }

    public static HashMap<String, HashMap<String,Integer>> GetBigramFromFile(String filePath){
        HashMap<String, HashMap<String,Integer>> BigramTable = new HashMap<>();
        int start = 1;
        try
        {
            String encoding = "UTF-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists())
            {
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;

                while ((lineTxt = bufferedReader.readLine()) != null) {
                    HashMap<String, Integer> bigram = new HashMap<>();
                    if(start == 1){
                        String[] sum = lineTxt.split("\t");
                        if(!sum[0].equals(sum[1])){
                            System.out.println("Start_sum doesn't equal with end_sum");
                        }
                        try {
                            BigramTable.put("Head", bigram);
                            bigram.put("total", Integer.parseInt(sum[0]));
                        }
                        catch(Exception e){
                            System.out.println("Lose the total number of bigrams");
                            e.printStackTrace();
                        }
                        start = 0;
                        continue;
                    }
                    String[] parts = lineTxt.split("\t");

                    BigramTable.put(parts[0], bigram);

                    String[] wlist = parts[1].split(";");
                    for(String str : wlist){
                        String[] keyvalue = str.split(":");
                        bigram.put(keyvalue[0],Integer.parseInt(keyvalue[1]));
                    }

                }

                System.out.println("Read From Bigram File Success!");

                bufferedReader.close();
                read.close();
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
        return BigramTable;
    }


    public static List<String> ReadCandidateFromFile(String filePath, String prefix){
        List<String> bigram = new ArrayList<>();
        try
        {
            String encoding = "UTF-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists())
            {
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;

                 while ((lineTxt = bufferedReader.readLine()) != null) {
                    if(lineTxt.startsWith(prefix)){
                        String[] parts = lineTxt.split("\t");
                        String[] wlist = parts[1].split(";");
                        for(String str : wlist){
                            if(str!=null){
                                String[] keyvalue = str.split("=");
                                bigram.add(keyvalue[0]);
                            }
                        }
                        break;
                    }
                }
                bufferedReader.close();
                read.close();
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
        return bigram;
    }



}
