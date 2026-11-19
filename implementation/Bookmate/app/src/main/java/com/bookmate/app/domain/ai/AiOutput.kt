package com.bookmate.app.domain.ai

/** Cleans AI output before it is shown. */
object AiOutput {
    private val TEXT_TAGS = Regex("</?text>", RegexOption.IGNORE_CASE)

    // A tag that is still being streamed, like "<te" or "</tex", at the very end.
    private val PARTIAL_TAG_AT_END = Regex("</?(t(e(x(t)?)?)?)?$", RegexOption.IGNORE_CASE)

    /** Removes the <text> tags that the model may copy from the prompt, and trims the result. */
    fun clean(raw: String): String =
        PARTIAL_TAG_AT_END.replace(TEXT_TAGS.replace(raw, ""), "").trim()
}