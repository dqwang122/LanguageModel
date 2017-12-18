import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReadFromFile {
        /**
     * 功能：Java读取txt文件的内容 步骤：
     *      1：先获得文件句柄
     *      2：获得文件句柄当做是输入一个字节码流，需要对这个输入流进行读取
     *      3：读取到输入流后，需要读取生成字节流
     *      4：一行一行的输出。readline()。
     *      备注：需要考虑的是异常情况
     *
     * @param filePath
     *            文件路径[到达文件:如： D:\aa.txt]
     * @return 将这个文件按照每一行切割成数组存放到list中。
     */
    public static List<String> readTxtFileIntoStringArrList(String filePath)
    {
        List<String> list = new ArrayList<String>();
        try
        {
            String encoding = "UTF-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists())
            { // 判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;

                while ((lineTxt = bufferedReader.readLine()) != null)
                {
                    list.add(lineTxt);
                }
                bufferedReader.close();
                read.close();
            }
            else
            {
                System.out.println("找不到指定的文件");
            }
        }
        catch (Exception e)
        {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }

        return list;
    }

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
                        try {
                            bigram.put("total", Integer.parseInt(lineTxt));
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

}
