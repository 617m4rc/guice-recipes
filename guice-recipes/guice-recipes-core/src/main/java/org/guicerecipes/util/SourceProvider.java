package org.guicerecipes.util;

import java.util.*;

import com.google.common.collect.*;

import static com.google.common.collect.Iterables.*;

/**
 * Provides access to the calling line of code.
 * 
 * @author crazybob@google.com (Bob Lee)
 */
public final class SourceProvider {

	/** Indicates that the source is unknown. */
	public static final Object UNKNOWN_SOURCE = "[unknown source]";

	private final ImmutableSet<String> classNamesToSkip;

	public static final SourceProvider DEFAULT_INSTANCE = new SourceProvider(ImmutableSet.of(SourceProvider.class.getName()));

	private SourceProvider(Iterable<String> classesToSkip) {
		classNamesToSkip = ImmutableSet.copyOf(classesToSkip);
	}

	/** Returns a new instance that also skips {@code moreClassesToSkip}. */
	public SourceProvider plusSkippedClasses(Class<?>... moreClassesToSkip) {
		return new SourceProvider(concat(classNamesToSkip, asStrings(moreClassesToSkip)));
	}

	/** Returns the class names as Strings */
	private static List<String> asStrings(Class<?>... classes) {
		List<String> strings = Lists.newArrayList();
		for (Class<?> c : classes) {
			strings.add(c.getName());
		}
		return strings;
	}

	/**
	 * Returns the calling line of code. The selected line is the nearest to the top of the stack that is not skipped.
	 */
	public StackTraceElement get() {
		for (final StackTraceElement element : new Throwable().getStackTrace()) {
			String className = element.getClassName();
			if (!classNamesToSkip.contains(className)) {
				return element;
			}
		}
		throw new AssertionError();
	}
}
