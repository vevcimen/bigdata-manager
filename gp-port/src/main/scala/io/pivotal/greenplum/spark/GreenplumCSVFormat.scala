package io.pivotal.greenplum.spark

import com.univocity.parsers.csv.{CsvParserSettings, UnescapedQuoteHandling}

object GreenplumCSVFormat {
  val ESCAPE              : Char    = '\\'
  val CHAR_DELIMITER      : Char    = ','
  val NEWLINE             : String  = "\n"
  val QUOTE               : Char    = '"'
  val QUOTE_STRING        : String  = "\""
  val VALUE_OF_NULL       : String  = ""
  val EMPTY_VALUE         : String  = ""
  val NULL_VALUE          : Null    = null
  val DEFAULT_ENCODING    : String  = "UTF-8"
  val STRICT_QUOTES       : Boolean = false
  val IGNORE_WHITESPACE   : Boolean = false
  val ESCAPED_QUOTE       : String  = "\"\""
  val inputBufferSize     : Int     = 1024 * 1024   // 1 MB
  val maxCharsPerColumn   : Int     = 4 * 1024 * 1024
  val maxErrorContentLength: Int    = 2048

  def DEFAULT: CsvParserSettings = {
    val settings = new CsvParserSettings()
    settings.getFormat.setDelimiter(CHAR_DELIMITER)
    settings.getFormat.setQuote(QUOTE)
    settings.getFormat.setQuoteEscape(ESCAPE)
    settings.getFormat.setLineSeparator(NEWLINE)
    settings.setIgnoreLeadingWhitespaces(IGNORE_WHITESPACE)
    settings.setIgnoreTrailingWhitespaces(IGNORE_WHITESPACE)
    settings.setNullValue(VALUE_OF_NULL)
    settings.setEmptyValue(EMPTY_VALUE)
    settings.setMaxCharsPerColumn(maxCharsPerColumn)
    settings.setInputBufferSize(inputBufferSize)
    settings.setUnescapedQuoteHandling(UnescapedQuoteHandling.STOP_AT_CLOSING_QUOTE)
    settings
  }
}
