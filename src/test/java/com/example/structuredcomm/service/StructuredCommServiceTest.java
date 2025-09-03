package com.example.structuredcomm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StructuredCommServiceTest {

    private StructuredCommService service;

    @BeforeEach
    void setUp() {
        service = new StructuredCommService();
    }

    @Test
    @DisplayName("Edge case: base % 97 == 0 should yield check = 97")
    void checkDigits_zeroModulo97_resultsIn97() {
        var r1 = service.validateNumeric("000000009797"); // base 97 -> check 97
        assertThat(r1.valid()).isTrue();
        assertThat(r1.structured()).isEqualTo("+++000/0000/9797+++");

        var r2 = service.validateNumeric("000000019497"); // base 194 -> check 97
        assertThat(r2.valid()).isTrue();
        assertThat(r2.structured()).isEqualTo("+++000/0001/9497+++");

        var r3 = service.validateNumeric("000000000097"); // base 0 -> check 97
        assertThat(r3.valid()).isTrue();
        assertThat(r3.structured()).isEqualTo("+++000/0000/0097+++");
    }

    @Test
    @DisplayName("Valid examples")
    void validExamples_pass() {
        var r1 = service.validateNumeric("123456789095"); // base %97=2 -> check 95
        assertThat(r1.valid()).isTrue();
        assertThat(r1.structured()).isEqualTo("+++123/4567/89095+++");

        var r2 = service.validateNumeric("111111111127"); // base %97=70 -> check 27
        assertThat(r2.valid()).isTrue();
        assertThat(r2.structured()).isEqualTo("+++111/1111/1127+++");

        var r3 = service.validateNumeric("999999999949"); // base %97=48 -> check 49
        assertThat(r3.valid()).isTrue();
        assertThat(r3.structured()).isEqualTo("+++999/9999/9949+++");
    }

    @Test
    @DisplayName("Invalid: wrong check digits should fail with expected reason")
    void invalidCheckDigits_fail() {
        var r = service.validateNumeric("123456789000"); // expected 95
        assertThat(r.valid()).isFalse();
        assertThat(r.reason()).contains("expected 95");
        assertThat(r.structured()).isEqualTo("+++123/4567/89000+++");
    }

    @Test
    @DisplayName("Structured format must be +++XXX/XXXX/XXXXX+++")
    void structuredFormat_enforced() {
        var bad1 = service.validateStructured("123/4567/89095");
        assertThat(bad1.valid()).isFalse();

        var bad2 = service.validateStructured("+++12/3456/789095+++");
        assertThat(bad2.valid()).isFalse();

        var good = service.validateStructured("+++123/4567/89095+++");
        assertThat(good.valid()).isTrue();
        assertThat(good.numeric()).isEqualTo("123456789095");
    }
}
