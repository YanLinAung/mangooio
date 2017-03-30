package io.mangoo.interfaces;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

/**
 *
 * @author svenkubiak
 *
 */
@FunctionalInterface
public interface MangooFilter {
    Response execute(Request request, Response response);
}