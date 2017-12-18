
import java.io.IOException;
import java.util.*;
import java.util.regex.*;


import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
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

 public static class Map extends Mapper<LongWritable, Text, Text, MapWritable> {
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

 private static class Combine extends Reducer<Text, MapWritable, Text, MapWritable> {

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

 public static class Reduce extends Reducer<Text, MapWritable, Text, Text> {

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

 public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();


    Job job = new Job(conf, "LanguageModel");
    job.setJarByClass(LanguageModel.class);

    System.out.println("Hello world!");

    // set the type of output (key, Value)
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(MapWritable.class);

    // set Mapper and Reducer
    job.setMapperClass(Map.class);
    job.setCombinerClass(Combine.class);
    job.setReducerClass(Reduce.class);

    // set Input and Output class
    job.setInputFormatClass(TextInputFormat.class);
    job.setOutputFormatClass(TextOutputFormat.class);

    // set Input and output path
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));

    job.waitForCompletion(true);
 }

}