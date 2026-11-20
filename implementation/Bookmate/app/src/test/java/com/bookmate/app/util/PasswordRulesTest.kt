package com.bookmate.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PasswordRulesTest {

    @Test
    fun validInputHasNoProblem() {
        assertNull(checkNewPassword("oldpass123", "newpass123", "newpass123"))
    }

    @Test
    fun emptyCurrentPasswordIsReportedFirst() {
        assertEquals(PasswordProblem.CURRENT_EMPTY, checkNewPassword("", "short", "other"))
    }

    @Test
    fun shortNewPasswordIsRejected() {
        assertEquals(PasswordProblem.TOO_SHORT, checkNewPassword("oldpass123", "abc1234", "abc1234"))
    }

    @Test
    fun differentConfirmationIsRejected() {
        assertEquals(PasswordProblem.MISMATCH, checkNewPassword("oldpass123", "newpass123", "newpass124"))
    }

    @Test
    fun samePasswordIsRejected() {
        assertEquals(PasswordProblem.SAME_AS_CURRENT, checkNewPassword("samepass123", "samepass123", "samepass123"))
    }
}