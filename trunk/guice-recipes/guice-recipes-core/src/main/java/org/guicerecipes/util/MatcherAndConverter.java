package org.guicerecipes.util;

import com.google.inject.*;
import com.google.inject.matcher.*;
import com.google.inject.spi.*;

import static com.google.common.base.Preconditions.*;

public final class MatcherAndConverter {

	private final Matcher<? super TypeLiteral<?>> typeMatcher;
	private final TypeConverter typeConverter;
	private final Object source;

	public MatcherAndConverter(Matcher<? super TypeLiteral<?>> typeMatcher, TypeConverter typeConverter, Object source) {
		this.typeMatcher = checkNotNull(typeMatcher, "type matcher");
		this.typeConverter = checkNotNull(typeConverter, "converter");
		this.source = source;
	}

	public TypeConverter getTypeConverter() {
		return typeConverter;
	}

	public Matcher<? super TypeLiteral<?>> getTypeMatcher() {
		return typeMatcher;
	}

	public Object getSource() {
		return source;
	}

	@Override
	public String toString() {
		return typeConverter + " which matches " + typeMatcher + " (bound at " + source + ")";
	}
}
