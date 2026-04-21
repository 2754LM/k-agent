package com.kano.main_data.context;

public class SessionContext {
    private static class SessionData {
        private String sessionId;

        public SessionData(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getSessionId() {
            return sessionId;
        }
    }

     private static final ThreadLocal<SessionData> sessionData = new ThreadLocal<>();

    public static String getSessionId() {
        return sessionData.get().getSessionId();
    }
    public static void setSessionId(String sessionId) {
        sessionData.set(new SessionData(sessionId));
    }


}
