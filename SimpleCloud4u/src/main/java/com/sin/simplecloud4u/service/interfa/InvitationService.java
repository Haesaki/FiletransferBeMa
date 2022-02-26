package com.sin.simplecloud4u.service.interfa;

public interface InvitationService {
    String generateInvitationCode(String userName);

    String authorizeInvitationCode(String userName, String invitationCode);
}
