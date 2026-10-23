package com.example.siuu

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Utility to parse CSV and JSON files located inside the Android assets/warehouse directory.
 */
object AssetWarehouseLoader {

    /**
     * Reads a CSV file from assets/warehouse/<fileName> and parses it into a List of Map<String, String>.
     */
    fun loadCsv(context: Context, fileName: String): List<Map<String, String>> {
        val rows = mutableListOf<Map<String, String>>()
        try {
            context.assets.open("warehouse/$fileName").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val headerLine = reader.readLine() ?: return rows
                    val headers = parseCsvLine(headerLine).map { it.trim().removePrefix("\uFEFF") }

                    var line: String? = reader.readLine()
                    while (line != null) {
                        if (line.isNotBlank()) {
                            val values = parseCsvLine(line)
                            val rowMap = headers.zip(values).toMap()
                            rows.add(rowMap)
                        }
                        line = reader.readLine()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return rows
    }

    /**
     * Reads an entire JSON file as String from assets/warehouse/<fileName>.
     */
    fun loadJson(context: Context, fileName: String): String {
        return try {
            context.assets.open("warehouse/$fileName").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Helper to parse a single line of CSV respecting quotes and commas.
     */
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    result.add(sb.toString().trim().trim('\"'))
                    sb.setLength(0)
                }
                else -> sb.append(ch)
            }
        }
        result.add(sb.toString().trim().trim('\"'))
        return result
    }
}
