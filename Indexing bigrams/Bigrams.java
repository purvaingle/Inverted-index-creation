import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.util.HashMap;
import java.util.ArrayList;



public class Bigrams {
  public static class TokenizerMapper extends Mapper<Object, Text, Text, Text> {
      private Text bigram = new Text();

      public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
          String[] parts = value.toString().split("\t", 2);
          String doc = parts[0];
          String content = parts[1].toLowerCase().replaceAll("[^a-z\\s]", " ").replaceAll("\\s+", " ");
          StringTokenizer itr = new StringTokenizer(content);
          ArrayList<String> words = new ArrayList<>();

          while (itr.hasMoreTokens()) {
              words.add(itr.nextToken());
          }

          // bigrams
          for (int i = 0; i < words.size() - 1; i++) {
              String bigramText = words.get(i) + " " + words.get(i + 1);
              if (bigramText.equals("computer science") ||
                  bigramText.equals("information retrieval") ||
                  bigramText.equals("power politics") ||
                  bigramText.equals("los angeles") ||
                  bigramText.equals("bruce willis")) {
                  bigram.set(bigramText);
                  context.write(bigram, new Text(doc));
              }
          }
      }
  }


  public static class HReducer extends Reducer<Text, Text, Text, Text> {
    private Text result = new Text();

    // private IntWritable result = new IntWritable();
    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
      HashMap<String, Integer> wordFrequencies = new HashMap<>();

      for (Text word : values) {
          String wordString = word.toString();
          // Checking if the word is in HashMap
          if (wordFrequencies.containsKey(wordString)) {
              // If yes, count++
              wordFrequencies.put(wordString, wordFrequencies.get(wordString) + 1);
          } else {
              wordFrequencies.put(wordString, 1);
          }
      }

      StringBuilder combinedText = new StringBuilder(); 
      for (String word : wordFrequencies.keySet()) {
          combinedText.append(word)
                      .append(":")
                      .append(String.valueOf(wordFrequencies.get(word)))
                      .append("\t");
      }

      result.set(combinedText.substring(0, combinedText.length() - 1)); 
      context.write(key, result);

      // int sum = 0;
      // for (IntWritable val : values)
      // {
      // sum += val.get();
      // }
      // result.set(sum);
      // context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "word count");

    job.setJarByClass(WordCount.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setReducerClass(HReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);

    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));

    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}// WordCount
