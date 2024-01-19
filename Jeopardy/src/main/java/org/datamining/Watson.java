package org.datamining;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Watson {

    private final List<String[]> correctAnswers;
    double answeredRight = 0.0;
    double sumReciprocalRank = 0.0;
    double answeredTotal = 0.0;
    String inputDirectory = "";
    String questionFile = "";
    StandardAnalyzer analyzer;
    IndexWriterConfig config;
    IndexWriter writer;
    FSDirectory index;

    public Watson(String directoryName, String questionFilePath) throws IOException {
        inputDirectory = directoryName;
        questionFile = questionFilePath;
        analyzer = new StandardAnalyzer();
        config = new IndexWriterConfig(analyzer);
        config.setOpenMode(OpenMode.CREATE_OR_APPEND);
        index = FSDirectory.open(Paths.get("Jeopardy/src/main/resources/index"));
        correctAnswers = new ArrayList<>();
    }

    public static void main(String[] args) {
        try {
            String directory = "../DataSets/wikipediaPages";
            String questionFile = "../DataSets/questions.txt";
            Watson jEngine = new Watson(directory, questionFile);

            jEngine.parseQuestions(questionFile);
            jEngine.printScore();
            System.out.println("Program completed, Thank you.");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void parseQuestions(String questionFile) throws java.io.FileNotFoundException, java.io.IOException, ParseException {
        Scanner scanner = new Scanner(new File(questionFile));
        int currentRank = 1;
        while (scanner.hasNextLine()) {
            String category = scanner.nextLine();
            String query = scanner.nextLine();
            String answer = scanner.nextLine();
            scanner.nextLine();
            String[] answerArr = answer.split("\\|");
            query = query + " " + category;
            query = query.replaceAll("\\r\\n", "");
            Query q = new QueryParser("tokens", analyzer).parse(QueryParser.escape(query));
            IndexReader reader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs docs = searcher.search(q, 1);
            ScoreDoc[] hits = docs.scoreDocs;
            for (ScoreDoc s : hits) {
                Document answerDoc = searcher.doc(s.doc);
                for (String ans : answerArr) {
                    ans = ans.trim();
                    if (ans.equals(answerDoc.get("title"))) {
                        answeredRight++;
                        sumReciprocalRank += 1.0 / currentRank;
                        correctAnswers.add(new String[]{query, ans, answerDoc.get("title")});
                        break;
                    }
                }
                currentRank++;
            }
            answeredTotal++;
        }
        scanner.close();
    }

    void printScore() {
        double precision = answeredRight / answeredTotal;
        System.out.println("\tPrecision: " + precision);

        double mrr = sumReciprocalRank / answeredTotal;
        System.out.println("\tMean Reciprocal Rank (MRR): " + mrr);

        System.out.println("\tTotal Questions Processed: " + answeredTotal);
        printErrorAnalysis();
    }

    void printErrorAnalysis() {
        System.out.println("\nError Analysis:");
        System.out.println("Number of Correct Answers: " + correctAnswers.size());
        System.out.println("Number of Incorrect Answers: " + (int)(answeredTotal-correctAnswers.size()));

        for (String[] error : correctAnswers) {
            System.out.println("Question: " + error[0]);
            System.out.println("Expected Answer: " + error[1]);
            System.out.println("Correct Answer Provided: " + error[2]);
            System.out.println("------");
        }
    }

}