package com.apikeymanager.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IpMatcherTest {

    @Test
    void exactMatchOnAnIpv4Address() {
        assertThat(IpMatcher.matches("192.168.1.1", "192.168.1.1")).isTrue();
        assertThat(IpMatcher.matches("192.168.1.2", "192.168.1.1")).isFalse();
    }

    @Test
    void cidrMatchOnAnIpv4Block() {
        assertThat(IpMatcher.matches("10.1.2.3", "10.0.0.0/8")).isTrue();
        assertThat(IpMatcher.matches("11.1.2.3", "10.0.0.0/8")).isFalse();
        assertThat(IpMatcher.matches("192.168.1.5", "192.168.1.0/24")).isTrue();
        assertThat(IpMatcher.matches("192.168.2.5", "192.168.1.0/24")).isFalse();
    }

    @Test
    void slashZeroMatchesEverything() {
        assertThat(IpMatcher.matches("8.8.8.8", "0.0.0.0/0")).isTrue();
        assertThat(IpMatcher.matches("1.2.3.4", "0.0.0.0/0")).isTrue();
    }

    @Test
    void exactAndCidrMatchOnIpv6() {
        assertThat(IpMatcher.matches("::1", "::1")).isTrue();
        assertThat(IpMatcher.matches("2001:db8::1", "2001:db8::/32")).isTrue();
        assertThat(IpMatcher.matches("2001:db9::1", "2001:db8::/32")).isFalse();
    }

    @Test
    void mismatchedAddressFamiliesNeverMatch() {
        assertThat(IpMatcher.matches("192.168.1.1", "::1/128")).isFalse();
    }

    @Test
    void malformedEntriesReturnFalseRatherThanThrowing() {
        assertThat(IpMatcher.matches("192.168.1.1", "not-an-ip")).isFalse();
        assertThat(IpMatcher.matches("192.168.1.1", "192.168.1.0/abc")).isFalse();
        assertThat(IpMatcher.matches(null, "192.168.1.0/24")).isFalse();
        assertThat(IpMatcher.matches("192.168.1.1", null)).isFalse();
    }
}
