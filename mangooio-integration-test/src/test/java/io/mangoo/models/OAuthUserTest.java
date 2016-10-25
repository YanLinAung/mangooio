package io.mangoo.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

/**
 * 
 * @author svenkubiak
 *
 */
public class OAuthUserTest {

    public void testCreateOAuthUser() {
        //given
        OAuthUser oAuthUser = new OAuthUser("1", "foo", "bar", "foobar");
        
        //then
        assertThat(oAuthUser, not(nullValue()));
        assertThat(oAuthUser.getId(), equalTo("1"));
        assertThat(oAuthUser.getOAuthResponse(), equalTo("foo"));
        assertThat(oAuthUser.getPicture(), equalTo("foobar"));
        assertThat(oAuthUser.getUsername(), equalTo("bar"));
    }
}