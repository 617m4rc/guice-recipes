package org.guicerecipes.util;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import com.google.common.base.*;
import com.google.common.collect.*;

/**
 * Creates stack trace elements for members.
 * 
 * @author crazybob@google.com (Bob Lee)
 */
public class StackTraceElements {

	/* if[AOP] */
	static final Map<Class<?>, LineNumbers> lineNumbersCache = new MapMaker().weakKeys().softValues().makeComputingMap(new Function<Class<?>, LineNumbers>() {
		public LineNumbers apply(Class<?> key) {
			try {
				return new LineNumbers(key);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	});

	/* end[AOP] */

	public static Object forMember(Member member) {
		if (member == null) {
			return SourceProvider.UNKNOWN_SOURCE;
		}

		Class<?> declaringClass = member.getDeclaringClass();

		/* if[AOP] */
		LineNumbers lineNumbers = lineNumbersCache.get(declaringClass);
		String fileName = lineNumbers.getSource();
		Integer lineNumberOrNull = lineNumbers.getLineNumber(member);
		int lineNumber = lineNumberOrNull == null ? lineNumbers.getFirstLine() : lineNumberOrNull;
		/* end[AOP] */
		/*
		 * if[NO_AOP] String fileName = null; int lineNumber = -1; end[NO_AOP]
		 */

		String memberName = member instanceof Constructor<?> ? "<init>" : member.getName();
		return new StackTraceElement(declaringClass.getName(), memberName, fileName, lineNumber);
	}

	public static Object forType(Class<?> implementation) {
		/* if[AOP] */
		LineNumbers lineNumbers = lineNumbersCache.get(implementation);
		int lineNumber = lineNumbers.getFirstLine();
		String fileName = lineNumbers.getSource();
		/* end[AOP] */
		/*
		 * if[NO_AOP] String fileName = null; int lineNumber = -1; end[NO_AOP]
		 */

		return new StackTraceElement(implementation.getName(), "class", fileName, lineNumber);
	}
}
