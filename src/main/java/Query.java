import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.*;

public class Query {

    public static void main(String[] args)throws IOException{

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String str = null;
        int precision = 0;

        System.out.println("Choose Prediction Mode: 1. (default)fast; 2. precision");
        str = br.readLine();
        if(str.equals("2")){
            precision = 1;
        }

        while(true) {
            System.out.println("Enter your Character:");
            str = br.readLine();

            if(str.equals("q") || str.equals("Q") || str.equals("exit")){
                break;
            }
            if (str.length() < 2) {
                System.out.println("Too short! Please enter more Character!");
                continue;
            }

            List<String> common = Arrays.asList("的", "一", "了", "是", "我", "不", "在", "人", "们", "有");

            long startTime = System.currentTimeMillis();    // start_time

            String prefix = str.substring(str.length()-2, str.length());
            List<String> candidates = ReadFromFile.ReadCandidateFromFile("result/LM_Trigram_query_candidate", prefix);
            HashMap<String, Double> candidateP = new HashMap<>();
            int index = 1;
            // do not index
            if(candidates.isEmpty()) {
                index = 0;
                candidates.addAll(ReadFromFile.ReadCandidateFromFile("result/LM_Trigram_query", prefix));
                if (candidates.size() < 10) {
                    candidates.addAll(ReadFromFile.ReadCandidateFromFile("result/LM_Bigram_query", prefix.substring(1, 2)));
                }
                // do not have any candidates
                if (candidates.isEmpty()) {
                    candidates.addAll(common);
                }
            }

            if(index == 0 && precision == 1) {
                for(String c : candidates){
                    KNS P = new KNS(prefix + c, "result/LM_Trigram", "result/LM_Trigram_end", "result/LM_Bigram");
                    candidateP.put(c, P.GetP());
                }
                List<Map.Entry<String, Double>> infoIds = new ArrayList<>(candidateP.entrySet());
                Collections.sort(infoIds, new Comparator<Map.Entry<String, Double>>() {
                    public int compare(Map.Entry<String, Double> o1,
                                       Map.Entry<String, Double> o2) {
                        return o2.getValue().compareTo(o1.getValue());
                    }
                });
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < 5 && i < candidateP.size(); i++) {
                    builder.append(i + 1).append(" ").append(infoIds.get(i).getKey()).append(";");
                }
                System.out.println(builder.toString());
            }
            else{
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < 5 && i < candidates.size(); i++) {
                    builder.append(i+1).append(" ").append(candidates.get(i)).append(";");
                }
                System.out.println(builder.toString());
            }


            long endTime = System.currentTimeMillis();    // end_time
            System.out.println("RunTime:" + (endTime - startTime) + "ms");    // runtime
        }

    }
}
