package org.guicerecipes.util;

import com.google.inject.*;

public interface CachingProvider<T> extends Provider<T>, CachedValue {

}
