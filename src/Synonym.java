import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import java.util.ArrayList;
import java.util.List;

public class Synonym
{
    public static List<String> getTopSynonyms(final String word, final int count)
    {
        System.setProperty("wordnet.database.dir", "C:\\Program Files (x86)\\WordNet\\2.1\\dict");
        WordNetDatabase database = WordNetDatabase.getFileInstance();
        ArrayList<String> synonyms=new ArrayList<String>();
        Synset[] synsets = database.getSynsets(word);
        if (synsets.length > 0) {
            for (int i = 0; i < synsets.length; i++) {
                String[] wordForms = synsets[i].getWordForms();
                for (int j = 0; j < wordForms.length; j++) {
                    if(!synonyms.contains(wordForms[j])){
                        synonyms.add(wordForms[j]); }
                }
            }
        }
        if(synonyms.size() > count) return synonyms.subList(0, count-1);
        return synonyms;
    }
}
