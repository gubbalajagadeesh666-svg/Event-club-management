package com.campusflow.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Post/Redirect/Get flash notification helper.
 */
public final class FlashUtil {

    private static final String SESSION_FLASH_TYPE = "flashType";
    private static final String SESSION_FLASH_MESSAGE = "flashMessage";

    public static final String REQ_FLASH_TYPE = "uiFlashType";
    public static final String REQ_FLASH_MESSAGE = "uiFlashMessage";

    private FlashUtil() {
    }

    public static void setSuccess(HttpServletRequest request, String message) {
        set(request, "success", message);
    }

    public static void setError(HttpServletRequest request, String message) {
        set(request, "danger", message);
    }

    public static void setWarning(HttpServletRequest request, String message) {
        set(request, "warning", message);
    }

    public static void setInfo(HttpServletRequest request, String message) {
        set(request, "info", message);
    }

    private static void set(HttpServletRequest request, String type, String message) {
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_FLASH_TYPE, type);
        session.setAttribute(SESSION_FLASH_MESSAGE, message);
    }

    public static void consumeToRequest(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Object type = session.getAttribute(SESSION_FLASH_TYPE);
        Object msg = session.getAttribute(SESSION_FLASH_MESSAGE);
        if (type != null && msg != null) {
            request.setAttribute(REQ_FLASH_TYPE, type.toString());
            request.setAttribute(REQ_FLASH_MESSAGE, msg.toString());
            session.removeAttribute(SESSION_FLASH_TYPE);
            session.removeAttribute(SESSION_FLASH_MESSAGE);
        }
    }
}
