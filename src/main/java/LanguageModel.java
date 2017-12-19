
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

 private static class Combine extends Reducer<Text, MapWritable, Text, MapWritable> {

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

 public static class Reduce extends Reducer<Text, MapWritable, Text, Text> {

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