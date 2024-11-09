package util;

import org.apache.lucene.analysis.CharArraySet;
import java.util.Arrays;

public class StopWords {
    private CharArraySet stopWords = new CharArraySet(Arrays.asList(
            "a", "an", "the", "in", "on", "and", "or", "of", "for", "with", "to", "from",
            "by", "at", "about", "as", "is", "are", "were", "be", "been", "being", "which",
            "that", "those", "these", "such", "has", "have", "having", "into", "under",
            "between", "over", "more", "less", "one", "two", "three", "new", "study",
            "using", "based", "method", "methods", "toward", "towards", "this",
            "through", "via", "it", "its", "their", "our",
            "we", "can", "could", "would", "will", "may", "might", "do", "does", "did",
            "find", "findings", "show", "shows", "effect", "effects", "model", "models",
            "use", "uses", "evidence", "case", "cases", "some", "many", "several",
            "related", "associated", "impact", "impacts", "implications", "role", "roles",
            "perspective", "perspectives", "review", "reviews"), true);

    public CharArraySet getStopWords() {
        return stopWords;
    }

    public void setStopWords(CharArraySet stopWords) {
        this.stopWords = stopWords;
    }
}