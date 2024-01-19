package org.datamining;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

public class Indexer {
    private FSDirectory indexDirectory;
    private StandardAnalyzer analyzer;
    private IndexWriter writer;

    public static void main(String[] args) {
        System.out.println("Building a new Index...");
        Indexer indexer = new Indexer();
        indexer.buildIndex();
    }

    /**
     * Iterates all pages from Jeopardy/src/main/resources/wikipediaPages and indexes each page separately
     * and it prepares the terms for indexing with the help of Lucerne`s StandardAnalyzer
     * */
    private void buildIndex() {
        try {
            indexDirectory = FSDirectory.open(Paths.get("Jeopardy\\src\\main\\resources\\index"));
            analyzer = new StandardAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            writer = new IndexWriter(indexDirectory, config);

            File folder = new File("..\\DataSets\\wikipediaPages");
            for (File fileEntry : folder.listFiles()) {
                System.out.println("Working on file: " + fileEntry.getName());
                indexFile(fileEntry);
            }
        } catch (IOException e) {
            System.err.println("Error while building the index: " + e.getMessage());
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing the index writer: " + e.getMessage());
            }
        }
        System.out.println("Finished building the index.");
    }

    /**
     * This method indexes a file
     *
     * @param fileEntry the file currently processed.
     *
     * */
    private void indexFile(File fileEntry) {
        try (Scanner inputScanner = new Scanner(fileEntry)) {
            String contents = "";
            String currentTitle = inputScanner.nextLine();
            currentTitle = removeBrackets(currentTitle);

            while (inputScanner.hasNextLine()) {
                String inputLine = inputScanner.nextLine();
                if (isNewDocument(inputLine)) {
                    addDocumentToIndex(currentTitle, contents);
                    currentTitle = removeBrackets(inputLine);
                    contents = "";
                } else {
                    contents += inputLine;
                }
            }
        } catch (IOException e) {
            System.err.println("Error processing file " + fileEntry.getName() + ": " + e.getMessage());
        }
    }

    /**
     * This function checks if a new page started
     *
     */
    private boolean isNewDocument(String input) {
        return input.startsWith("[[") && input.endsWith("]]");
    }

    /**
     * This method adds a page with its content to the index
     * */
    private void addDocumentToIndex(String title, String content) throws IOException {
        Document document = new Document();
        document.add(new StringField("title", title, Field.Store.YES));
        document.add(new TextField("tokens", content, Field.Store.YES));
        writer.addDocument(document);
    }

    /**
     * This function just removes square brackets from page titless
     * */
    private String removeBrackets(String input) {
        return input.replace("[", "").replace("]", "");
    }
}
