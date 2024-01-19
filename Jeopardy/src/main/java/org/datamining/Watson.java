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

/**
 * The Watson class simulates the IBM Watson's Jeopardy problem by processing
 * and answering questions from a provided file, and evaluating the answers.
 */
public class Watson {
    private final List<String[]> correctAnswers;
    private double answeredRight = 0.0;
    private double sumReciprocalRank = 0.0;
    private double answeredTotal = 0.0;
    private StandardAnalyzer analyzer;
    private FSDirectory index;

    /**
     * Constructs a Watson instance for answering questions and evaluating performance.
     *
     * @param indexDirectory Path to the directory containing the indexed data.
     * @param questionFilePath Path to the file containing the questions.
     */
    public Watson(String indexDirectory, String questionFilePath) throws IOException {
        this.index = FSDirectory.open(Paths.get(indexDirectory));
        this.analyzer = new StandardAnalyzer();
        this.correctAnswers = new ArrayList<>();
    }

    /**
     * The main entry point of the Watson class.
     *
     */
    public static void main(String[] args) {
        try {
            Watson watsonEngine = new Watson("Jeopardy/src/main/resources/index", "Jeopardy/src/main/resources/questions.txt");
            watsonEngine.parseQuestions();
            watsonEngine.printScore();
            System.out.println("Program completed, Thank you.");
        } catch (Exception ex) {
            System.out.println("Error occurred: " + ex.getMessage());
        }
    }

    /**
     * Processes and answers the questions from the file, updating performance metrics.
     */
    public void parseQuestions() throws IOException, ParseException {
        try (Scanner scanner = new Scanner(new File("Jeopardy/src/main/resources/questions.txt"))) {
            while (scanner.hasNextLine()) {
                String category = scanner.nextLine();
                String query = scanner.nextLine() + " " + category.replaceAll("\\r\\n", "");
                String answer = scanner.nextLine();
                scanner.nextLine(); // Skip the blank line

                answerQuestions(query, answer.split("\\|"));
            }
        }
    }

    /**
     * Answers the provided question using the indexed data, updating performance metrics.
     *
     * @param query The query representing the question.
     * @param expectedAnswers The expected answers for the question.
     */
    private void answerQuestions(String query, String[] expectedAnswers) throws IOException, ParseException {
        QueryParser parser = new QueryParser("tokens", analyzer);
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        Query luceneQuery = parser.parse(QueryParser.escape(query));
        TopDocs docs = searcher.search(luceneQuery, 1);

        for (ScoreDoc scoreDoc : docs.scoreDocs) {
            Document retrievedDoc = searcher.doc(scoreDoc.doc);
            evaluateAnswer(retrievedDoc.get("title").trim(), expectedAnswers);
        }
    }

    /**
     * Evaluates the retrieved answer against the expected answers, updating performance metrics.
     *
     * @param retrievedAnswer The answer retrieved by the system.
     * @param expectedAnswers The expected answers for the question.
     */
    private void evaluateAnswer(String retrievedAnswer, String[] expectedAnswers) {
        int currentRank = 1;
        for (String expectedAnswer : expectedAnswers) {
            if (expectedAnswer.trim().equals(retrievedAnswer)) {
                answeredRight++;
                sumReciprocalRank += 1.0 / currentRank;
                correctAnswers.add(new String[]{retrievedAnswer, expectedAnswer});
                break;
            }
            currentRank++;
        }
        answeredTotal++;
    }

    /**
     * Prints the precision and mean reciprocal rank (MRR) scores, along with the error analysis.
     */
    void printScore() {
        double precision = answeredRight / answeredTotal;
        double mrr = sumReciprocalRank / answeredTotal;

        System.out.println("\tPrecision: " + precision);
        System.out.println("\tMean Reciprocal Rank (MRR): " + mrr);
        System.out.println("\tTotal Questions Processed: " + answeredTotal);
        printErrorAnalysis();
    }

    /**
     * Prints the error analysis, detailing the number of correct and incorrect answers.
     */
    void printErrorAnalysis() {
        System.out.println("\nError Analysis:");
        System.out.println("Number of Correct Answers: " + correctAnswers.size());
        System.out.println("Number of Incorrect Answers: " + (answeredTotal - correctAnswers.size()));

        for (String[] correctAnswerInfo : correctAnswers) {
            System.out.println("Question: " + correctAnswerInfo[0]);
            System.out.println("Expected Answer: " + correctAnswerInfo[1]);
            System.out.println("Correct Answer Provided: " + correctAnswerInfo[0]);
            System.out.println("------");
        }
    }
}
