import java.io.*;
import java.util.*;
import java.util.HashMap;

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

    public static void main(String[] args){
        CleanBigram("result/LM_Bigram_f");
        CleanTrigramEnd("result/LM_Trigram_end");
    }
}
