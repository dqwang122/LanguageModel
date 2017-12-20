
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;


import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.fs.*;

import java.net.URI;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class LanguageModel {
    public static String GetChineseWord(String str_text) {

    StringBuilder builder = new StringBuilder();

    // 0-9 or chinese words or Punctuation
    String reg="([\uFF10-\uFF19|\u4e00-\u9fa5|\u3002\uff1b\uff0c\uff1a\u201c\u201d\uff08\uff09\u3001\uff1f\u300a\u300b]+)";
    Matcher matcher = Pattern.compile(reg).matcher(str_text);
    while(matcher.find()){
        builder.append(matcher.group());
    }

    return builder.toString();
}

    public static List<String> ReadFromFile (String uri) throws Exception {
        List<String> lines = new ArrayList<>();
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(uri), conf);
        InputStream fin = null;
        BufferedReader bufferedReader = null;
        try {
            fin = fs.open(new Path(uri));
            bufferedReader = new BufferedReader(new InputStreamReader(fin, "UTF-8"));
            String lineTxt = null;

            while((lineTxt = bufferedReader.readLine()) != null){
                lines.add(lineTxt);
            }

        }
        catch (Exception e) {
            System.out.println("Read From Bigram File Wrong!");
            e.printStackTrace();
        }
        finally {
            if(bufferedReader != null) {
                bufferedReader.close();
            }
        }
        return lines;
    }

    public static HashMap<String, HashMap<String,Integer>> GetTrigramFromFile(String uri) throws Exception{
        HashMap<String, HashMap<String, Integer>> TrigramTable = new HashMap<>();

        String encoding = "UTF-8";
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(uri), conf);
        InputStream fin = null;
        BufferedReader bufferedReader = null;
        try {
            fin = fs.open(new Path(uri));
            bufferedReader = new BufferedReader(new InputStreamReader(fin, encoding));
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

        }
        catch (Exception e)
        {
            System.out.println("Read From Trigram File Wrong!");
            e.printStackTrace();
        }
        finally {
            if(bufferedReader != null) {
                bufferedReader.close();
            }
        }
        return TrigramTable;
    }

    public static HashMap<String, HashMap<String,Integer>> GetTrigramEndFromFile(String uri) throws  Exception{
        HashMap<String, HashMap<String,Integer>> TrigramEndTable = new HashMap<>();

        String encoding = "UTF-8";
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(uri), conf);
        InputStream fin = null;
        BufferedReader bufferedReader = null;
        try {
            fin = fs.open(new Path(uri));
            bufferedReader = new BufferedReader(new InputStreamReader(fin, encoding));
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
        }
        catch (Exception e)
        {
            System.out.println("Read From Trigram End Wrong!");
            e.printStackTrace();
        }
        finally {
            if(bufferedReader != null) {
                bufferedReader.close();
            }
        }
        return TrigramEndTable;
    }

    public static HashMap<String, HashMap<String,Integer>> GetBigramFromFile(String uri) throws  Exception{
        HashMap<String, HashMap<String,Integer>> BigramTable = new HashMap<>();
        int start = 1;

        String encoding = "UTF-8";
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(uri), conf);
        InputStream fin = null;
        BufferedReader bufferedReader = null;
        try {
            fin = fs.open(new Path(uri));
            bufferedReader = new BufferedReader(new InputStreamReader(fin, encoding));
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
        }
        catch (Exception e)
        {
            System.out.println("Read From Bigram File Wrong!");
            e.printStackTrace();
        }
        finally {
            if(bufferedReader != null) {
                bufferedReader.close();
            }
        }
        return BigramTable;
    }

    public static class Map_B extends Mapper<LongWritable, Text, Text, MapWritable> {
//    private final static IntWritable one = new IntWritable(1);
//    private Text word = new Text();

    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
        String words = GetChineseWord(value.toString());
        HashMap<String, MapWritable> stripes  = new HashMap<>();    // HashTable for (W2)

        for(int i = 0; i < words.length(); i++){
            String w = words.substring(i, i + 1);    // W2
            MapWritable wList;     // (W2) -> {start:list of w1, end: list of w1,....}
            if(!stripes.containsKey(w)){
                wList = new MapWritable();
                stripes.put(w, wList);
            }
            else{
                wList = stripes.get(w);
            }

            // start
            if(i != words.length() - 1) {
                String w3 = words.substring(i + 1, i + 2);
                String start = w3;
                Text skey = new Text("start");
                if (wList.containsKey(skey)) {
                    start = ((Text)wList.get(skey)).toString();
                    if(!start.contains(w3)){
                        start += w3;
                    }
                }
                wList.put(skey, new Text(start));
            }

            // end
            if(i != 0) {
                String w1 = words.substring(i - 1, i);
                String end = w1;
                Text ekey = new Text("end");
                if (wList.containsKey(ekey)) {
                    end = ((Text)wList.get(ekey)).toString();
                    if(!end.contains(w1)){
                        end += w1;
                    }
                }
                wList.put(ekey, new Text(end));
            }
        }

        // emit
        for(String w2 : stripes.keySet()){
            context.write(new Text(w2), stripes.get(w2));
        }
    }
 }
    private static class Combine_B extends Reducer<Text, MapWritable, Text, MapWritable> {

    public void reduce(Text key, Iterable<MapWritable> value, Context context)
            throws IOException, InterruptedException {
        MapWritable stripe = new MapWritable();

        // for combining local docs, each doc has a MapWritable with the same key (W2)
        for (MapWritable val : value) {
            if(!val.isEmpty()) {
                // for start and end
                for (Writable w : val.keySet()) {   //w = "start"
                    String new_w1 = ((Text)val.get(w)).toString();
                    if(stripe.containsKey((w))) {
                        String all_w1 = ((Text)stripe.get(w)).toString();
                        StringBuilder builder = new StringBuilder(all_w1);
                        for(String s : new_w1.split("")){
                            if(!all_w1.contains(s)){
                                builder.append(s);
                            }
                        }
                        stripe.put(w, new Text(builder.toString()));
                    }
                    else {
                        stripe.put(w, new Text(new_w1));
                    }
                }
            }
        }

        context.write(key, stripe);
    }
}
    public static class Reduce_B extends Reducer<Text, MapWritable, Text, Text> {

    public void reduce(Text key, Iterable<MapWritable> value, Context context)
            throws IOException, InterruptedException {
        HashMap<String, String> stripe = new HashMap<>();

        // for combining different mapper with the same key (W2)
        for (MapWritable val : value) {
            if(!val.isEmpty()) {
                // for start and key
                for (Writable w : val.keySet()) {
                    String new_w1 = ((Text)val.get(w)).toString();
                    String wstr = w.toString();
                    if(stripe.containsKey((wstr))) {
                        String all_w1 = stripe.get(wstr);
                        StringBuilder builder = new StringBuilder(all_w1);
                        for(String s : new_w1.split("")){
                            if(!all_w1.contains(s)){
                                builder.append(s);
                            }
                        }
                        stripe.put(wstr, builder.toString());
                    }
                    else {
                        stripe.put(wstr, new_w1);
                    }

                }
            }
        }

        StringBuilder builder = new StringBuilder();
        if(stripe.size() > 0) {
            for (HashMap.Entry<String, String> e : stripe.entrySet()) {
                builder.append(e.getKey()).append(":").append(e.getValue()).append(";");
            }
        }

        context.write(key, new Text(builder.toString()));
    }

 }

    public static class Map_P extends Mapper<LongWritable, Text, Text, MapWritable> {
//    private final static IntWritable one = new IntWritable(1);
//    private Text word = new Text();

    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
        String words = GetChineseWord(value.toString());
        HashMap<String, MapWritable> stripes  = new HashMap<>();    // HashTable for (W1,W2)

        for(int i = 0; i < words.length() - 2; i++){
            String bigram = words.substring(i, i+2);    // (W1,W2)
            Text ch = new Text(words.substring(i+2, i+3));  // W3
            MapWritable bigramList;     // (W1,W2) -> {W3:1,W3:3,....}
            if(!stripes.containsKey(bigram)){
                bigramList = new MapWritable();
                stripes.put(bigram, bigramList);
            }
            else{
                bigramList = stripes.get(bigram);
            }
            IntWritable cnt_tmp = new IntWritable(1);
            if(bigramList.containsKey(ch)){
                IntWritable cnt = (IntWritable) bigramList.get(ch);
                cnt_tmp.set(cnt.get() + 1);
            }
            bigramList.put(ch, cnt_tmp);
        }

        // emit
        for(String bigram : stripes.keySet()){
            context.write(new Text(bigram), stripes.get(bigram));
        }
    }
 }
    private static class Combine_P extends Reducer<Text, MapWritable, Text, MapWritable> {

    public void reduce(Text key, Iterable<MapWritable> value, Context context)
            throws IOException, InterruptedException {
        MapWritable stripe = new MapWritable();

        // for combining local docs, each doc has a MapWritable with the same key (W1,W2)
        for (MapWritable val : value) {
            if(!val.isEmpty()) {
                // for each element W3
                for (Writable w : val.keySet()) {
                    IntWritable cnt = (IntWritable)val.get(w);
                    if(stripe.containsKey((w))) {
                        stripe.put(w, new IntWritable(cnt.get() + ((IntWritable)stripe.get(w)).get()));
                    }
                    else {
                        stripe.put(w, cnt);
                    }
                }
            }
        }

        context.write(key, stripe);
    }
}
    public static class Reduce_P extends Reducer<Text, MapWritable, Text, Text> {

    public void reduce(Text key, Iterable<MapWritable> value, Context context)
            throws IOException, InterruptedException {
        HashMap<String, Integer> stripe = new HashMap<>();
        double sum = 0;

        // for combining different mapper with the same key (W1, W2)
        for (MapWritable val : value) {
            if(!val.isEmpty()) {
                // for W3
                for (Writable w : val.keySet()) {
                    int cnt = ((IntWritable)val.get(w)).get();
                    String wstr = (w).toString();

                    // record the total number of (W1,W2)
                    sum += cnt;

                    if(stripe.containsKey((wstr))) {
                        cnt += stripe.get(wstr);
                    }
                    stripe.put(wstr, cnt);


                }
            }
        }

        StringBuilder builder = new StringBuilder();
        if(stripe.size() > 0) {
            for (HashMap.Entry<String, Integer> e : stripe.entrySet()) {
                builder.append(e.getKey()).append(":").append(e.getValue()/sum).append(";");
            }
        }

        context.write(key, new Text(builder.toString()));
    }

 }

    public static class Map_T extends Mapper<LongWritable, Text, Text, MapWritable> {
//    private final static IntWritable one = new IntWritable(1);
//    private Text word = new Text();

    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
        String words = GetChineseWord(value.toString());
        HashMap<String, MapWritable> stripes  = new HashMap<>();    // HashTable for (W1,W2)

        for(int i = 0; i < words.length() - 2; i++){
            String bigram = words.substring(i, i+2);    // (W1,W2)
            Text ch = new Text(words.substring(i+2, i+3));  // W3
            MapWritable bigramList;     // (W1,W2) -> {W3:1,W3:3,....}
            if(!stripes.containsKey(bigram)){
                bigramList = new MapWritable();
                stripes.put(bigram, bigramList);
            }
            else{
                bigramList = stripes.get(bigram);
            }
            IntWritable cnt_tmp = new IntWritable(1);
            if(bigramList.containsKey(ch)){
                IntWritable cnt = (IntWritable) bigramList.get(ch);
                cnt_tmp.set(cnt.get() + 1);
            }
            bigramList.put(ch, cnt_tmp);
        }

        // emit
        for(String bigram : stripes.keySet()){
            context.write(new Text(bigram), stripes.get(bigram));
        }
    }
 }
    private static class Combine_T extends Reducer<Text, MapWritable, Text, MapWritable> {

    public void reduce(Text key, Iterable<MapWritable> value, Context context)
            throws IOException, InterruptedException {
        MapWritable stripe = new MapWritable();

        // for combining local docs, each doc has a MapWritable with the same key (W1,W2)
        for (MapWritable val : value) {
            if(!val.isEmpty()) {
                // for each element W3
                for (Writable w : val.keySet()) {
                    IntWritable cnt = (IntWritable)val.get(w);
                    if(stripe.containsKey((w))) {
                        stripe.put(w, new IntWritable(cnt.get() + ((IntWritable)stripe.get(w)).get()));
                    }
                    else {
                        stripe.put(w, cnt);
                    }
                }
            }
        }

        context.write(key, stripe);
    }
}
    public static class Reduce_T extends Reducer<Text, MapWritable, Text, Text> {

    public void reduce(Text key, Iterable<MapWritable> value, Context context)
            throws IOException, InterruptedException {
        HashMap<String, Integer> stripe = new HashMap<>();
//        double sum = 0;

        // for combining different mapper with the same key (W1, W2)
        for (MapWritable val : value) {
            if(!val.isEmpty()) {
                // for W3
                for (Writable w : val.keySet()) {
                    int cnt = ((IntWritable)val.get(w)).get();
                    String wstr = (w).toString();

                    // record the total number of (W1,W2)
//                    sum += cnt;

                    if(stripe.containsKey((wstr))) {
                        cnt += stripe.get(wstr);
                    }
                    stripe.put(wstr, cnt);


                }
            }
        }

        StringBuilder builder = new StringBuilder();
        if(stripe.size() > 0) {
            for (HashMap.Entry<String, Integer> e : stripe.entrySet()) {
                builder.append(e.getKey()).append(":").append(e.getValue()).append(";");
            }
        }

        context.write(key, new Text(builder.toString()));
    }

 }

    public static class Map_TE extends Mapper<LongWritable, Text, Text, MapWritable> {
//    private final static IntWritable one = new IntWritable(1);
//    private Text word = new Text();

    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
        String words = GetChineseWord(value.toString());
        HashMap<String, MapWritable> stripes  = new HashMap<>();    // HashTable for (W2)

        for(int i = 0; i < words.length() - 2; i++){
            String w1 = words.substring(i, i+1);    //w1
            String w2 = words.substring(i + 1, i + 2);    // W2
            Text w3 = new Text(words.substring(i+2, i+3));  // W3
            MapWritable w2List;     // (W2) -> {W3:list of w1, W3: list of w1,....}
            if(!stripes.containsKey(w2)){
                w2List = new MapWritable();
                stripes.put(w2, w2List);
            }
            else{
                w2List = stripes.get(w2);
            }
            String all_w1 = w1;
            if(w2List.containsKey(w3)){
                all_w1 = ((Text)w2List.get(w3)).toString();
                if(!all_w1.contains(w1)){
                    all_w1 += w1;
                }
            }
            w2List.put(w3, new Text(all_w1));
        }

        // emit
        for(String w2 : stripes.keySet()){
            context.write(new Text(w2), stripes.get(w2));
        }
    }
 }
    private static class Combine_TE extends Reducer<Text, MapWritable, Text, MapWritable> {
        public void reduce(Text key, Iterable<MapWritable> value, Context context)
            throws IOException, InterruptedException {
        MapWritable stripe = new MapWritable();

        // for combining local docs, each doc has a MapWritable with the same key (W2)
        for (MapWritable val : value) {
            if(!val.isEmpty()) {
                // for each element W3
                for (Writable w : val.keySet()) {
//                    IntWritable cnt = (IntWritable)val.get(w);
                    String new_w1 = ((Text)val.get(w)).toString();
                    if(stripe.containsKey((w))) {
                        String all_w1 = ((Text)stripe.get(w)).toString();
                        StringBuilder builder = new StringBuilder(all_w1);
                        for(String s : new_w1.split("")){
                            if(!all_w1.contains(s)){
                                builder.append(s);
                            }
                        }
                        stripe.put(w, new Text(builder.toString()));
                    }
                    else {
                        stripe.put(w, new Text(new_w1));
                    }
                }
            }
        }

        context.write(key, stripe);
    }
}
    public static class Reduce_TE extends Reducer<Text, MapWritable, Text, Text> {
        public void reduce(Text key, Iterable<MapWritable> value, Context context)
                throws IOException, InterruptedException {
            HashMap<String, String> stripe = new HashMap<>();
    //        double sum = 0;

            // for combining different mapper with the same key (W2)
            for (MapWritable val : value) {
                if(!val.isEmpty()) {
                    // for W3
                    for (Writable w : val.keySet()) {
                        String new_w1 = ((Text)val.get(w)).toString();
                        String wstr = w.toString();
                        if(stripe.containsKey((wstr))) {
                            String all_w1 = stripe.get(wstr);
                            StringBuilder builder = new StringBuilder(all_w1);
                            for(String s : new_w1.split("")){
                                if(!all_w1.contains(s)){
                                    builder.append(s);
                                }
                            }
                            stripe.put(wstr, builder.toString());
                        }
                        else {
                            stripe.put(wstr, new_w1);
                        }

                    }
                }
            }

            StringBuilder builder = new StringBuilder();
            if(stripe.size() > 0) {
                for (HashMap.Entry<String, String> e : stripe.entrySet()) {
                    builder.append(e.getKey()).append(":").append(e.getValue()).append(";");
                }
            }

            context.write(key, new Text(builder.toString()));
        }

 }

    public static class Map_BC extends Mapper<LongWritable, Text, Text, MapWritable> {
//    private final static IntWritable one = new IntWritable(1);
//    private Text word = new Text();

    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
        String words = GetChineseWord(value.toString());
        HashMap<String, MapWritable> stripes  = new HashMap<>();    // HashTable for (W1,W2)

        for(int i = 0; i < words.length() - 1; i++){
            String w1 = words.substring(i, i+1);    // (W1)
            Text w2 = new Text(words.substring(i+1, i+2));  // W2
            MapWritable wList;     // (W1,W2) -> {W3:1,W3:3,....}
            if(!stripes.containsKey(w1)){
                wList = new MapWritable();
                stripes.put(w1, wList);
            }
            else{
                wList = stripes.get(w1);
            }
            IntWritable cnt_tmp = new IntWritable(1);
            if(wList.containsKey(w2)){
                IntWritable cnt = (IntWritable) wList.get(w2);
                cnt_tmp.set(cnt.get() + 1);
            }
            wList.put(w2, cnt_tmp);
        }

        // emit
        for(String bigram : stripes.keySet()){
            context.write(new Text(bigram), stripes.get(bigram));
        }
    }
 }
    private static class Combine_BC extends Reducer<Text, MapWritable, Text, MapWritable> {

    public void reduce(Text key, Iterable<MapWritable> value, Context context)
            throws IOException, InterruptedException {
        MapWritable stripe = new MapWritable();

        // for combining local docs, each doc has a MapWritable with the same key (W1)
        for (MapWritable val : value) {
            if(!val.isEmpty()) {
                // for each element W2
                for (Writable w : val.keySet()) {
                    IntWritable cnt = (IntWritable)val.get(w);
                    if(stripe.containsKey((w))) {
                        stripe.put(w, new IntWritable(cnt.get() + ((IntWritable)stripe.get(w)).get()));
                    }
                    else {
                        stripe.put(w, cnt);
                    }
                }
            }
        }

        context.write(key, stripe);
    }
}
    public static class Reduce_BC extends Reducer<Text, MapWritable, Text, Text> {

        public void reduce(Text key, Iterable<MapWritable> value, Context context)
                throws IOException, InterruptedException {
            HashMap<String, Integer> stripe = new HashMap<>();

            // for combining different mapper with the same key (W1)
            for (MapWritable val : value) {
                if (!val.isEmpty()) {
                    // for W2
                    for (Writable w : val.keySet()) {
                        int cnt = ((IntWritable) val.get(w)).get();
                        String wstr = (w).toString();

                        if (stripe.containsKey((wstr))) {
                            cnt += stripe.get(wstr);
                        }
                        stripe.put(wstr, cnt);


                    }
                }
            }

            List<Map.Entry<String, Integer>> infoIds = new ArrayList<>(stripe.entrySet());

            Collections.sort(infoIds, new Comparator<Map.Entry<String, Integer>>() {
                public int compare(Map.Entry<String, Integer> o1,
                                   Map.Entry<String, Integer> o2) {
                    return (o2.getValue()- o1.getValue());
                }
            });

            StringBuilder builder = new StringBuilder();
            if (stripe.size() > 0) {
                for (int i = 0; i < 10 && i < stripe.size(); i++) {
                    builder.append(infoIds.get(i)).append(";");
                }
            }

            context.write(key, new Text(builder.toString()));
        }

    }

    public static class Map_Q extends Mapper<LongWritable, Text, Text, Text> {

        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            String[] lines = value.toString().split("\n");
            for(String lineTxt : lines) {
                HashMap<String, Integer> trigram = new HashMap<>();
                String[] parts = lineTxt.split("\t");
                String[] wlist = parts[1].split(";");
                for (String str : wlist) {
                    if (str != null) {
                        String[] keyvalue = str.split(":");
                        trigram.put(keyvalue[0], Integer.parseInt(keyvalue[1]));
                    }
                }

                List<Map.Entry<String, Integer>> infoIds = new ArrayList<>(trigram.entrySet());

                Collections.sort(infoIds, new Comparator<Map.Entry<String, Integer>>() {
                    public int compare(Map.Entry<String, Integer> o1,
                                       Map.Entry<String, Integer> o2) {
                        return (o2.getValue() - o1.getValue());
                    }
                });

                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < 10 && i < trigram.size(); i++) {
                    builder.append(infoIds.get(i)).append(";");
                }

                // emit
                context.write(new Text(parts[0]), new Text(builder.toString()));
            }


        }
 }
    public static class Reduce_Q extends Reducer<Text, Text, Text, Text> {

        public void reduce(Text key, Iterable<Text> value, Context context)
                throws IOException, InterruptedException {
            HashMap<String, Integer> stripe = new HashMap<>();
            // for combining different mapper with the same key (W1)
            for (Text val : value) {
                context.write(key, val);
            }
        }

    }

    public static class Map_C extends Mapper<LongWritable, Text, Text, Text> {
        private HashMap<String, HashMap<String, Integer>> BigramTable;
        private HashMap<String, HashMap<String, Integer>> TrigramEndTable;
        private HashMap<String, HashMap<String, Integer>> TrigramTable;
        public void setup(Context context) throws IOException{
            try{
                BigramTable = GetBigramFromFile("hdfs://10.141.200.205:9000/user/14307130360/resources/LM_Bigram");
                TrigramEndTable = GetTrigramEndFromFile("hdfs://10.141.200.205:9000/user/14307130360/resources/LM_Trigram_end");
                TrigramTable = GetTrigramFromFile("hdfs://10.141.200.205:9000/user/14307130360/resources/LM_Trigram");

            }
            catch (Exception e){
                System.out.println("Read From File Wrong!");
                e.printStackTrace();
            }
        }

        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            String[] lines = value.toString().split("\n");
            for(String lineTxt : lines) {
                HashMap<String, Double> trigram = new HashMap<>();
                String[] parts = lineTxt.split("\t");
                List<String> wlist = Arrays.asList(parts[1].split(";"));

                for(String str : wlist){
                    if(str!=null){
                        String[] keyvalue = str.split("=");

                        KNS P = new KNS(parts[0] + keyvalue[0], TrigramTable,TrigramEndTable,BigramTable);
                        trigram.put(keyvalue[0], P.GetP());
                    }
                }

//                if(wlist.size() < 10){
//                    // add bigram
//                    List<String> bigramCandidate = ReadFromFile.ReadBigramBCFromFile(
//                            "result/LM_Bigram_query",
//                            parts[0].substring(1,2));
//                    for(int i = 0; i < 10 - wlist.size() && i < bigramCandidate.size(); i ++){
//                        KNS P = new KNS(parts[0] + bigramCandidate.get(i), TrigramTable,TrigramEndTable,BigramTable);
//                        trigram.put(bigramCandidate.get(i), P.GetP());
//                    }
//                }

                List<Map.Entry<String, Double>> infoIds = new ArrayList<>(trigram.entrySet());

                Collections.sort(infoIds, new Comparator<Map.Entry<String, Double>>() {
                    public int compare(Map.Entry<String, Double> o1,
                                       Map.Entry<String, Double> o2) {
                        return o2.getValue().compareTo(o1.getValue());
                    }
                });

                StringBuilder builder = new StringBuilder();
                for(int i = 0; i < 10 && i < trigram.size(); i++){
                    builder.append(infoIds.get(i)).append(";");
                }

                context.write(new Text(parts[0]), new Text(builder.toString()));



            }

        }
    }
    public static class Reduce_C extends Reducer<Text, Text, Text, Text> {

        public void reduce(Text key, Iterable<Text> value, Context context)
                throws IOException, InterruptedException {
            // for combining different mapper with the same key (W1)
            for (Text val : value) {
                context.write(key, val);
            }
        }

    }


    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();    // start_time

        Configuration conf = new Configuration();

        Job job = new Job(conf, "LanguageModel");
        job.setJarByClass(LanguageModel.class);

        System.out.println("Hello world!");

        FileSystem fs = FileSystem.get(conf);
        if (fs.exists(new Path(args[1])))
            fs.delete(new Path(args[1]), true);

        // set the type of output (key, Value)
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(MapWritable.class);

        // set Mapper and Reducer
        job.setMapperClass(Map_P.class);
//        job.setCombinerClass(Combine_P.class);
        job.setReducerClass(Reduce_P.class);

        // set Input and Output class
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        // set Input and output path
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);

        long endTime = System.currentTimeMillis();    // end_time

        System.out.println("RunTime:" + (endTime - startTime) + "ms");    // runtime
    }

}