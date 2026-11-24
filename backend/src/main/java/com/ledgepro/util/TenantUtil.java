package com.ledgepro.util;

public final class TenantUtil {

    private TenantUtil() {}

    /**
     * Resolves the active business ID for the current request.
     * Prioritizes the HTTP Header 'X-Business-Id', then the query/body param,
     * and finally defaults to 1L only if neither is provided.
     */
    public static Long resolveBusinessId(Long paramBusinessId, String headerBusinessId) {
        if (headerBusinessId != null && !headerBusinessId.trim().isEmpty()) {
            try {
                return Long.parseLong(headerBusinessId.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        if (paramBusinessId != null && paramBusinessId > 0) {
            return paramBusinessId;
        }
        return 1L;
    }
}
