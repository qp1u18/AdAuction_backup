package backend;

import backend.Authentication.AuthenticationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationTest {

    @Test
    void clientLogin() throws AuthenticationException {
        Authentication.register("user1", "pass123");
        assertFalse(Authentication.login("user2", "pass123"));
        assertNull(Authentication.getCurrentUser());
        assertFalse(Authentication.login("user1", "pass1234"));
        assertNull(Authentication.getCurrentUser());
        assertTrue(Authentication.login("user1", "pass123"));
        assertEquals(Integer.valueOf(2), Authentication.getCurrentUser());
        Authentication.logout();
        assertNull(Authentication.getCurrentUser());
    }

    @Test
    void adminLogin() throws AuthenticationException {
        assertFalse(Authentication.login("admin", "admin1"));
        assertNull(Authentication.getCurrentUser());
        assertFalse(Authentication.login("admin1", "admin"));
        assertNull(Authentication.getCurrentUser());
        assertTrue(Authentication.login("admin", "admin"));
        assertEquals(Integer.valueOf(0), Authentication.getCurrentUser());
        Authentication.logout();
        assertNull(Authentication.getCurrentUser());
    }
}