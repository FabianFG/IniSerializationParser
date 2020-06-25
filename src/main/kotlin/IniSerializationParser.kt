
object IniSerializationParser {

    class IniParserException(override val message : String?, override val cause: Throwable? = null) : Exception()

    fun parse(d : String) = parseMultiple(d).first()

    fun parseMultiple(d : String) : List<Any> {
        val items = mutableListOf<Any>()
        var (rest, data) = parseI(d)
        rest.trim()
        items.add(data)
        while (rest.isNotBlank()) {
            if (rest.startsWith(',')) {
                rest = rest.substring(1).trim()
                val res = parseI(rest)
                rest = res.first.trim()
                items.add(res.second)
            } else throw IniParserException("Illegal Rest, must start with ',', rest '$rest'")
        }
        return items
    }

    private fun parseI(d : String): Pair<String,Any> {
        var data = d.trimIndent()
        if (data[0] != '(')
            throw IniParserException("Invalid loc text fix data")
        data = data.substring(1)
        while (data[0] == ' ')
            data = data.substring(1)
        return if (data.takeWhile { it != '(' && it != ',' && it != ')' }.contains('='))
            parseObjectI(d)
        else
            parseArrayI(d)
    }

    fun parseArray(d : String) : List<Any> {
        val (rest, data) = parseArrayI(d)
        if (rest.isNotBlank())
            throw IniParserException("Data not parsed properly, rest '$rest'")
        return data
    }

    private fun parseArrayI(d : String) : Pair<String,List<Any>> {
        var data = d.trimIndent()
        val list = mutableListOf<Any>()
        if (data[0] != '(')
            throw IniParserException("Invalid loc text fix data")
        data = data.substring(1)
        loop@ while (true) {
            while (data[0] == ' ')
                data = data.substring(1)
            when (data[0]) {
                '(' -> {
                    val (rest, entry) = parseI(data)
                    list.add(entry)
                    data = rest
                }
                '"' -> {
                    data = data.substring(1)
                    val entry = data.takeWhile { it != '"' }
                    list.add(entry)
                    data = data.substring(entry.length + 1)
                }
                else -> {
                    val entry = data.takeWhile { it != ',' && it != ')' }
                    list.add(entry)
                    data = data.substring(entry.length)
                }
            }
            while (data[0] == ' ')
                data = data.substring(1)
            val next = data[0]
            data = data.substring(1)
            when(next) {
                ',' -> continue@loop
                ')' -> break@loop
                else -> throw IniParserException("Invalid loc text fix data")
            }
        }
        return data to list
    }

    fun parseObject(d : String) : Map<String, Any> {
        val (rest, data) = parseObjectI(d)
        if (rest != "")
            throw IniParserException("Data not parsed properly, rest '$rest'")
        return data
    }

    private fun parseObjectI(d : String) : Pair<String, Map<String, Any>>{
        var data = d.trimIndent()
        val map = mutableMapOf<String, Any>()
        if (data[0] != '(')
            throw IniParserException("Invalid loc text fix data")
        data = data.substring(1)
        loop@ while (true) {
            while (data[0] == ' ')
                data = data.substring(1)
            val key = data.takeWhile { it != '=' }
            data = data.substring(key.length + 1)
            while (data[0] == ' ')
                data = data.substring(1)
            when (data[0]) {
                '(' -> {
                    val (rest, entry) = parseI(data)
                    map[key] = entry
                    data = rest
                }
                '"' -> {
                    data = data.substring(1)
                    val entry = data.takeWhile { it != '"' }
                    map[key] = entry
                    data = data.substring(entry.length + 1)
                }
                else -> {
                    val entry = data.takeWhile { it != ',' && it != ')' }
                    map[key] = entry
                    data = data.substring(entry.length)
                }
            }
            while (data[0] == ' ')
                data = data.substring(1)
            val next = data[0]
            data = data.substring(1)
            when(next) {
                ',' -> continue@loop
                ')' -> break@loop
                else -> throw IniParserException("Invalid loc text fix data")
            }
        }
        return data to map
    }
}