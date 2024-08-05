package com.getlipa.eventstore.hydration.eventhandler;

import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@InterceptorBinding
@Target({ElementType.METHOD, TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Apply {

}
