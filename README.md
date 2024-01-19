# Jeopardy Question Answering System

## Overview
This project simulates IBM Watson's Jeopardy challenge by processing and answering Jeopardy-style questions. It uses Apache Lucene, a high-performance, full-featured text search engine library written in Java, as its Information Retrieval (IR) backbone.


## How to Build and Run the Application
- Indexing Wikipedia Pages:
	- Run **Indexer.java** first to build the index. This process reads Wikipedia pages from the specified directory, processes the text, and builds an index for efficient searching.
	- The index is stored in the _Jeopardy/src/main/resources/index_ directory.

- Answering Questions
	- After indexing is complete, run **Watson.java** to start processing questions.
	- The application reads questions from _Jeopardy/src/main/resources/questions.txt_, searches for answers using the built index, and evaluates the answers based on the expected results.
	- Performance metrics and error analysis are displayed in the console.


## Features
- **Indexing**: Builds an index from a collection of Wikipedia pages, with each page treated as a separate document.
- **Question Processing**: Parses and answers questions from a provided file, comparing the system's answers with the correct answers.
- **Performance Metrics**: Evaluates the system's performance using Precision at 1 (P@1) and Mean Reciprocal Rank (MRR).
- **Error Analysis**: Provides a detailed analysis of the system's correct and incorrect answers.


## Prerequisites
- Java JDK 8 or higher
- Apache Lucene (included in the project's dependencies)
- The project's source files, including Indexer.java and Watson.java


