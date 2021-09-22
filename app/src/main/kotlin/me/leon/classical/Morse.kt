package me.leon.classical

val DEFAULT_MORSE =
    mapOf(
        'A' to ".-",
        'B' to "-...",
        'C' to "-.-.",
        'D' to "-..",
        'E' to ".",
        'F' to "..-.",
        'G' to "--.",
        'H' to "....",
        'I' to "..",
        'J' to ".---",
        'K' to "-.-",
        'L' to ".-..",
        'M' to "--",
        'N' to "-.",
        'O' to "---",
        'P' to ".--.",
        'Q' to "--.-",
        'R' to ".-.",
        'S' to "...",
        'T' to "-",
        'U' to "..-",
        'V' to "...-",
        'W' to ".--",
        'X' to "-..-",
        'Y' to "-.--",
        'Z' to "--..",
        '1' to ".----",
        '2' to "..---",
        '3' to "...--",
        '4' to "....-",
        '5' to ".....",
        '6' to "-....",
        '7' to "--...",
        '8' to "---..",
        '9' to "----.",
        '0' to "-----",
    )

val DEFAULT_MORSE_DECODE = mutableMapOf<String, Char>().apply {
    putAll(DEFAULT_MORSE.values.zip(DEFAULT_MORSE.keys))
}


fun String.morseEncrypt() =
    uppercase().replace("\\s".toRegex(), "").toList().joinToString(" ") { DEFAULT_MORSE[it]!! }

fun String.morseDecrypt() =
    split("\\s".toRegex()).joinToString("") { DEFAULT_MORSE_DECODE[it].toString() }