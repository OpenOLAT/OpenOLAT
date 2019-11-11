package ch.uzh.lms.listener;

import org.olat.core.gui.UserRequest;
import org.springframework.stereotype.Component;


@Component
public class PrintingOnLogoutListener extends LogoutListener {

    @Override
    public void onLogout(UserRequest userRequest) {
        System.out.print("hello listener");
    }
}