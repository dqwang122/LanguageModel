import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ParaTask extends RecursiveAction  {
    private static final long serialVersionUID = -6196480027075657316L;
    private static final int THRESHOLD = 500;

    private String filepath;
    private int low;
    private int high;

    public ParaTask(String filepath, int low, int high) {
        this.filepath = filepath;
        this.low = low;
        this.high = high;
    }

    @Override
    protected void  compute() {
        if (high - low < THRESHOLD)
            DataProcess.CreateCandidate(filepath, low, high);
        else {
           int mid = (low + high) >>> 1;
           invokeAll(new ParaTask(filepath, low, mid),
                     new ParaTask(filepath, mid, high));
        }

    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        String filepath = "result/LM_Trigram_query_ah";

        // 1. 创建任务
        ParaTask sumTask = new ParaTask(filepath, 0, 4000);

        long begin = System.currentTimeMillis();

        // 2. 创建线程池
        ForkJoinPool forkJoinPool = new ForkJoinPool();

        // 3. 提交任务到线程池
        forkJoinPool.invoke(sumTask);


        long end = System.currentTimeMillis();

        System.out.println(String.format("Runtime: %sms", end - begin));
    }

}
