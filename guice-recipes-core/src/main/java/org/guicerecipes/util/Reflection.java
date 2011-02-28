package org.guicerecipes.util;

import java.lang.annotation.*;
import java.lang.reflect.*;

public class Reflection {

	public static final Method findMethodWithAnnotation(Class<?> type, Class<? extends Annotation> annotationType, boolean lookInSuperClass) {
		Class<?> currentClass = type;
		while (currentClass != Object.class && currentClass != null) {
			Method[] methods = currentClass.getDeclaredMethods();
			for (Method method : methods) {
				Annotation fromElement = method.getAnnotation(annotationType);
				if (fromElement != null) {
					return method;
				}
			}
			if (lookInSuperClass) {
				currentClass = currentClass.getSuperclass();
			} else {
				break;
			}
		}
		return null;
	}
}
