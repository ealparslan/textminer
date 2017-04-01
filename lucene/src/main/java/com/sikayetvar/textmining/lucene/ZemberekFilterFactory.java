package com.sikayetvar.textmining.lucene;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;

public class ZemberekFilterFactory extends TokenFilterFactory implements ResourceLoaderAware {

	public ZemberekFilterFactory(Map<String, String> args) {
		super(args);
	}

	public void inform(ResourceLoader loader) throws IOException {

	}

	@Override
	public TokenStream create(TokenStream input) {
		return null;
	}

}
