import java.util.*;


public class KNS {
    private String _str;
    private HashMap<String, Integer> bigram;
    private HashMap<String, Integer> trigram_end_with;
    private HashMap<String, Integer> trigram;

    private static double d = 0.75;



    public KNS(String str){
        _str = str;
        trigram = ReadFromFile.ReadTrigramFromFile("trigram.txt", str.substring(0,2));
        trigram_end_with = ReadFromFile.ReadTrigramEndFromFile("trigram_end_with.txt", str.substring(1,2));
        bigram = ReadFromFile.ReadBigramFromFile("bigram.txt", str.substring(1,2), str.substring(2,3));
        System.out.println(trigram);
        System.out.println(trigram_end_with);
        System.out.println(bigram);
    }


    /** cal the C_kn
     * if length = 3, C_kn(w1,w2,w3)
     * else
     *      if begin = 1, C_kn(w1,w2)
     *      else if length = 2, C_kn(w2,w3)
     *      else length = 1, C_kn(w2)
     * @param str: w1w2w3 or w1w2 or w2w3 or w2
     *        begin: w1w2 or w2w3
     * @return cnt: the number of C_kn
     **/
    private int Ckn(String str, int begin){
        if(str.length() == 3){
            String w3 = str.substring(2,3);
            return trigram.getOrDefault(w3, 0);
        }
        else {
            if(begin == 1){
                int cnt = 0;
                for(Integer num : trigram.values()){
                    cnt += num;
                }
                return cnt;
            }
            else if(str.length() == 2){
                return trigram_end_with.getOrDefault(str.substring(1,2), 0);
            }
            else if (str.length() == 1){
                int cnt = 0;
                String s = str.substring(0,1);
                for(int val : trigram_end_with.values()){
                    cnt += val;
                }
                return cnt;
            }
            else{
                return 0;
            }
        }
    }

    /** cal the discount lambda
     *
     * @param str: w1w2 or w2
     * @return ret: lambda
     */
    private double CalLambda(String str){
        double lambda = 0.0;
        if(str.length() == 2){
            lambda = (d/Ckn(str, 1)) * trigram.size();
        }
        else if(str.length() == 1){
            lambda = (d / Ckn(str, 0) * bigram.getOrDefault("start", 0));
        }
        else{
            return 0;
        }
        return lambda;
    }

    /** Cal the continue count Possibility for unigram w3
     *
     * @param w: w3
     * @return P
     */
    private double Pcontinue(String w){
        int val = bigram.getOrDefault("end", 0);
        int total = 0;
        double P;
        total = bigram.get("total");
        P = val / total;
        return P;
    }

    /** Cal P(w3|w1w2) with smoothing
     * len = 1 : unigram P(w3) = Pcontinue(w3)
     * len = 2 : bigram P(w3|w2)
     * len = 3 : trigram P(w3|w1w2)
     * @param w:w1w2w3
     * @return P
     */
    public double Pkn(String w){
        double P = 0.0;
        double discount;
        String w3 = w.substring(w.length()-1, w.length());
        switch(w.length()){
            case 1:
                P = Pcontinue(w);
                break;
            case 2:
                String w2 = w.substring(0, 1);
                double Pbigram = Math.max(0, Ckn(w, 0) - d) / Ckn(w2, 0);
                discount = CalLambda(w2) * Pkn(w3);
                P = Pbigram + discount;
                break;
            case 3:
                String w1w2 = w.substring(0, 2);
                double Ptrigram = Math.max(0, Ckn(w, 1) - d) / Ckn(w1w2, 1);
                discount = CalLambda(w1w2) * Pkn(w.substring(1, 3));
                P = Ptrigram + discount;
                break;
            default:
                return 0.0;
        }
        return P;
    }

    public double GetP(){
        return Pkn(_str);
    }
};

