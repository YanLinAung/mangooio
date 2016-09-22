package io.mangoo.routing.bindings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import io.mangoo.routing.bindings.Session;

/**
 *
 * @author svenkubiak
 *
 */
public class SessionTest {
    private static final String BAR = "bar";
    private static final String FOO = "foo";

    @Test
    public void testNoContent() {
        //given
        final Session session = Session.build();

        //then
        assertThat(session.hasContent(), equalTo(false));
    }

    @Test
    public void testContent() {
        //given
        final Session session = Session.build();

        //when
        session.put(FOO, BAR);

        //then
        assertThat(session.hasContent(), equalTo(true));
        assertThat(session.get(FOO), equalTo(BAR));
        assertThat(session.hasChanges(), equalTo(true));
    }

    @Test
    public void testRemove() {
        //given
        final Session session = Session.build();

        //when
        session.put(FOO, BAR);
        session.remove(FOO);

        //then
        assertThat(session.hasContent(), equalTo(false));
        assertThat(session.get(FOO), equalTo(null));
        assertThat(session.hasChanges(), equalTo(true));
    }

    @Test
    public void testClear() {
        //given
        final Session session = Session.build();

        //when
        session.put(FOO, BAR);
        session.clear();

        //then
        assertThat(session.hasContent(), equalTo(false));
        assertThat(session.get(FOO), equalTo(null));
        assertThat(session.hasChanges(), equalTo(true));
    }

    @Test
    public void testInvalidCharacters() {
        //given
        final Session session = Session.build();

        //when
        session.put("|", FOO);
        session.put(":", FOO);
        session.put("&", FOO);
        session.put(" ", FOO);
        session.put(FOO, "|");
        session.put(FOO, ":");
        session.put(FOO, "&");
        session.put(FOO, " ");

        //then
        assertThat(session.hasContent(), equalTo(false));
    }
}