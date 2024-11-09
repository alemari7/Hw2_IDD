package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class HTMLSearcher {
    private IndexSearcher searcher;
    private QueryParser titleParser;
    private QueryParser abstractParser;
    private QueryParser authorParser;
    private long totalDocs;

    public HTMLSearcher(String indexPath) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(indexPath));
        IndexReader reader = DirectoryReader.open(dir);
        searcher = new IndexSearcher(reader);
        totalDocs = reader.numDocs(); 

        Analyzer customAnalyzer = CustomAnalyzer.builder()
            .withTokenizer(WhitespaceTokenizerFactory.class)
            .addTokenFilter(LowerCaseFilterFactory.class)
            .addTokenFilter(WordDelimiterGraphFilterFactory.class)
            .build();

        Analyzer authorAnalyzer = CustomAnalyzer.builder()
            .withTokenizer(WhitespaceTokenizerFactory.class)
            .addTokenFilter(LowerCaseFilterFactory.class)
            .build();

        titleParser = new QueryParser("title", customAnalyzer);
        abstractParser = new QueryParser("abstract", new StandardAnalyzer());
        authorParser = new QueryParser("author", authorAnalyzer);
    }

    public void search(String queryStr) throws Exception {
        long startTime = System.nanoTime();
        
        BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
        String[] terms = queryStr.split(",");
        
        boolean titleSearched = false, abstractSearched = false, authorSearched = false;
    
        for (String term : terms) {
            String[] parts = term.split(":", 2);
            if (parts.length == 2) {
                String field = parts[0].trim();
                String fieldQuery = parts[1].trim();
    
                Query query = null;
                switch (field) {
                    case "title":
                        query = titleParser.parse(fieldQuery);
                        titleSearched = true;
                        break;
                    case "abstract":
                        query = abstractParser.parse(fieldQuery);
                        abstractSearched = true;
                        break;
                    case "author":
                        query = authorParser.parse(fieldQuery);
                        authorSearched = true;
                        break;
                    default:
                        System.out.println("Campo non valido. I campi validi sono: title, abstract, author.");
                        return;
                }
    
                booleanQueryBuilder.add(query, BooleanClause.Occur.MUST);
            } else {
                System.out.println("Formato della query non valido. Usa 'title:term', 'abstract:term', o 'author:term'.");
                return;
            }
        }
    
        BooleanQuery finalQuery = booleanQueryBuilder.build();
        TopDocs results = searcher.search(finalQuery, 10);
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
    
        System.out.println("Trovati " + results.totalHits.value() + " documenti.");
    
        int relevantDocumentsFound = 0;
        long totalDocumentsReturned = results.totalHits.value();
    
        if (results.totalHits.value() > 0) {
            for (ScoreDoc hit : results.scoreDocs) {
                Document doc = searcher.getIndexReader().storedFields().document(hit.doc);
                
                // Stampa solo i campi richiesti
                System.out.println("Title: " + (doc.get("title") != null ? doc.get("title") : "N/A"));
                System.out.println("Author: " + (doc.get("author") != null ? doc.get("author") : "N/A"));
                System.out.println("Abstract: " + (doc.get("abstract") != null ? doc.get("abstract") : "N/A"));
                System.out.println("Content: " + (doc.get("content") != null ? doc.get("content") : "N/A"));
                System.out.println("Score: " + hit.score);
                System.out.println("------------");
        
                // Gestione del campo rilevanza
                String relevantValue = doc.get("relevant");
                boolean isRelevant = relevantValue != null && Boolean.parseBoolean(relevantValue);
        
                if (isRelevant) {
                    relevantDocumentsFound++;
                }
            }
        } else {
            System.out.println("Nessun documento trovato per la query.");
        }
    
        // Debug per valori di relevantDocumentsFound e totalDocumentsReturned
        System.out.println("Documenti rilevanti trovati: " + relevantDocumentsFound);
        System.out.println("Documenti totali restituiti: " + totalDocumentsReturned);
        System.out.println("Documenti totali nell'indice: " + totalDocs);
    
        // Calcolo della precisione e del richiamo
        double precision = (totalDocumentsReturned == 0) ? 0 : (double) relevantDocumentsFound / totalDocumentsReturned;
        double recall = (totalDocs == 0) ? 0 : (double) relevantDocumentsFound / totalDocs;
    
        // Statistiche finali
        System.out.println("\nStatistiche:");
        System.out.println("Tempo di risposta: " + (duration / 1_000_000.0) + " ms");
        System.out.println("Precisione: " + precision);
        System.out.println("Richiamo: " + recall);
    
        // Copertura dei campi interrogati
        System.out.println("\nCopertura dei campi interrogati:");
        System.out.println("Title interrogato: " + titleSearched);
        System.out.println("Abstract interrogato: " + abstractSearched);
        System.out.println("Author interrogato: " + authorSearched);
    }
    
    
    public void close() throws IOException {
        searcher.getIndexReader().close();
    }

    public static void main(String[] args) {
        try {
            String indexPath = "indexDir";
            HTMLSearcher searcher = new HTMLSearcher(indexPath);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Inserisci la tua query (es. 'title:term', 'author:\"nome\"', 'abstract:term').");

            String line;
            while (!(line = br.readLine()).equalsIgnoreCase("exit")) {
                searcher.search(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
