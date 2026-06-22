package com.apikeymanager.service;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Matches a source IP against an allowlist entry that's either an exact
 * address or a CIDR block (e.g. "10.0.0.0/8"). Pure, dependency-free, and
 * deliberately conservative: any parse failure or address-family mismatch
 * (IPv4 source vs. IPv6 entry, or vice versa) is treated as "no match"
 * rather than throwing, since this sits on a security-relevant path.
 */
final class IpMatcher {

    private IpMatcher() {
    }

    static boolean matches(String sourceIp, String allowedEntry) {
        if (sourceIp == null || allowedEntry == null) {
            return false;
        }

        int slashIndex = allowedEntry.indexOf('/');
        if (slashIndex < 0) {
            return sourceIp.equals(allowedEntry);
        }

        String network = allowedEntry.substring(0, slashIndex);
        int prefixLength;
        try {
            prefixLength = Integer.parseInt(allowedEntry.substring(slashIndex + 1));
        } catch (NumberFormatException e) {
            return false;
        }

        try {
            byte[] sourceBytes = InetAddress.getByName(sourceIp).getAddress();
            byte[] networkBytes = InetAddress.getByName(network).getAddress();

            if (sourceBytes.length != networkBytes.length) {
                return false; // IPv4 vs IPv6 mismatch
            }
            if (prefixLength < 0 || prefixLength > sourceBytes.length * 8) {
                return false;
            }

            int fullBytes = prefixLength / 8;
            int remainingBits = prefixLength % 8;

            for (int i = 0; i < fullBytes; i++) {
                if (sourceBytes[i] != networkBytes[i]) {
                    return false;
                }
            }

            if (remainingBits > 0) {
                int mask = 0xFF << (8 - remainingBits);
                if ((sourceBytes[fullBytes] & mask) != (networkBytes[fullBytes] & mask)) {
                    return false;
                }
            }

            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
