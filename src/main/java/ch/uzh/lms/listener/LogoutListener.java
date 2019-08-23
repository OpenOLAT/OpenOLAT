package ch.uzh.lms.listener;

import org.olat.core.gui.UserRequest;
import org.springframework.stereotype.Component;

/**
 * This class can be extended to run code when the user logs out of OLAT.
 *
 * @author Christian Schweizer (christian.schweizer3@uzh.ch)
 */
@Component
public class LogoutListener {

    /**
     * This method will be executed when the user logs out of OLAT.
     *
     * @param userRequest The user request
     */
    public void onLogout(UserRequest userRequest) {
        // Do nothing here
    }
}
