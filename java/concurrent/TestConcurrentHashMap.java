import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

class Scratch
{
    /**
     * Test concurrentAddMetricIsCorrect for 100 times, during each test run it will try to
     * concurrently add metric for 100000 times
     *
     * @param args
     */
    public static void main(String[] args)
    {
        IntStream.range(0, 100).forEach(i -> {
            if (!concurrentAddMetricIsCorrect(times))
            {
                throw new RuntimeException("behavior incorrect");
            }
        });
    }

    /**
     * Test if add metrics concurrently for given times, the metric does get added for that much
     * times.
     *
     * @return
     */
    public static boolean concurrentAddMetricIsCorrect(int times)
    {
        try
        {
            Metrics metrics = new Metrics();
            ExecutorService executorService = Executors.newCachedThreadPool();

            IntStream.range(0, times).forEach(
                    i -> executorService.execute(() -> metrics.addMetric(name, 1)));

            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);

            return metrics.getMetricsData().get(name).size() == times;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static final class Metrics
    {
        public void addMetric(String metricName, long metricValue)
        {
            fMetricsData.compute(metricName, (key, metricData) -> {
                if (metricData == null)
                {
                    metricData = new ArrayList<>();
                }
                metricData.add(metricValue);
                return metricData;
            });
        }

        public Map<String, List<Long>> getMetricsData()
        {
            return fMetricsData;
        }

        private Map<String, List<Long>> fMetricsData = new ConcurrentHashMap<>();
    }

    public static final String name = "a";
    public static final int times = 1000;
}
