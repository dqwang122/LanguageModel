public class Query {
    public static void main(String[] args){
        long startTime = System.currentTimeMillis();    // start_time

        KNS P = new KNS("搞定你", "result/");
        System.out.println(P.GetP());

        long endTime = System.currentTimeMillis();    // end_time

        System.out.println("RunTime:" + (endTime - startTime) + "ms");    // runtime

    }
}
