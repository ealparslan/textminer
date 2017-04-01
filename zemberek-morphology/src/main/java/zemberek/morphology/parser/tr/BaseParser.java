package zemberek.morphology.parser.tr;

import zemberek.core.turkish.TurkishAlphabet;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public abstract class BaseParser {

	public static final TurkishAlphabet alphabet = new TurkishAlphabet();

	static final Locale TR = new Locale("tr");
	static final Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

	public String normalize(String input) {
		StringBuilder sb = new StringBuilder(input.length());
		input = input.toLowerCase(TR);
		for (char c : input.toCharArray()) {
			if (alphabet.isValid(c))
				sb.append(c);
			else /* Problem with non-turkic letters -- Ahmet Canik */
				sb.append(deAccent(c + ""));
		}
		return sb.toString();
	}

	public String deAccent(String str) {
		String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
		return pattern.matcher(nfdNormalizedString).replaceAll("");
	}

}
