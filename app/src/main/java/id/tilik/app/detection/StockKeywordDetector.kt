package id.tilik.app.detection

object StockKeywordDetector {

    private val cashtagRegex = Regex("""[\$#]([A-Za-z]{4})\b""")
    private val tickerRegex = Regex("""\b[A-Z]{4}\b""")

    private val commonTickers = setOf(
        "BBRI", "BBCA", "BMRI", "BBNI", "ASII", "TLKM", "GOTO", "AMMN",
        "ADRO", "ANTM", "PGAS", "PTBA", "KLBF", "ICBP", "UNVR", "CPIN",
        "INCO", "MEDC", "BRIS", "BREN", "CUAN", "TPIA", "MBMA", "PGEO",
        "SMGR", "EXCL", "ISAT", "TOWR", "MDKA", "ARTO", "BRPT", "ACES",
        "MAPI", "BUKK", "HRUM", "ITMG", "AKRA", "UNTR", "INKP", "TKIM",
        "MYOR", "INDF", "MIKA", "HEAL", "SILO", "BSDE", "CTRA", "PWON",
        "SMRA", "JPFA", "MAIN", "CMRY", "MAPA", "ERAA", "ESSA", "ELSA",
        "RAJA", "DOID", "BUMI", "ENRG", "DEWA", "PANI", "WIFI", "AUTO",
        "AVIA", "NISP", "BDMN", "BTPS", "BBTN", "BIRD", "SSIA", "ELIT"
    )

    private val nonTickerBlacklist = setOf(
        "TIK", "TOK", "FYP", "LIVE", "LIKE", "VIDS", "POST", "NEWS", "INFO",
        "CALL", "SEND", "NEXT", "USER", "MORE", "LESS", "BACK", "PLAY", "CHAT",
        "EDIT", "DONE", "VIEW", "HOME", "SHOP", "SAVE", "TEXT", "LINK", "TRUE",
        "APPS", "PAGE", "FEED", "AUTO", "TIME", "DATE", "YEAR", "NAME", "ICON",
        "THIS", "THAT", "WITH", "FROM", "HAVE", "SOME", "WHAT", "WHEN", "WERE",
        "VERY", "GOOD", "BEST", "JUST", "WILL", "BEEN", "THEY", "THEM", "ALSO",
        "FALL", "DROP", "HIGH", "FAST", "SLOW", "REAL", "FREE", "COST", "RATE",
        "OPEN", "JOIN", "FORM", "HTTP", "DOCS", "READ", "HALO", "KAMI", "KITA",
        "YANG", "BISA", "AKAN", "DARI", "PADA", "SAYA", "KAMU", "BUAT", "JUGA",
        "BIAR", "SAMA", "TAPI", "DENG", "TIDAK", "TENTANG", "UNIV", "KOTA"
    )

    private val stockKeywords = setOf(
        "saham", "ihsg", "bursa", "emiten", "dividen", "cuan", "porto",
        "portofolio", "bandar", "foreign", "akumulasi", "distribusi",
        "cutloss", "bullish", "bearish", "ara", "arb", "idx", "bei",
        "lot", "haka", "haki", "net buy", "net sell", "gorengan",
        "valuasi", "laba", "per", "pbv", "roe", "der", "investasi",
        "market", "holding", "all-time", "rekor", "beli", "jual",
        "hold", "tp", "fundamental", "teknikal", "uptrend", "downtrend",
        "sideways", "breakout", "support", "resistance", "serok",
        "pompom", "swing", "scalping", "investor", "trading", "trader",
        "ipo", "buyback", "finansial", "kinerja", "kuartal", "profit",
        "rugi", "omset", "omzet", "pendapatan", "revenue", "analisa",
        "analisis", "rekomendasi", "target price"
    )

    private val slangStockMap = mapOf(
        "si ijo" to "GOTO",
        "si hijau" to "GOTO",
        "si merah" to "TLKM",
        "si biru" to "BBCA",
        "si kuning" to "ISAT",
        "bank rakyat" to "BBRI",
        "bank bca" to "BBCA",
        "bank mandiri" to "BMRI",
        "bank bni" to "BBNI",
        "telkom" to "TLKM",
        "astra" to "ASII",
        "amman" to "AMMN",
        "adaro" to "ADRO",
        "antam" to "ANTM"
    )

    fun extractPotentialTicker(text: String): String? {
        val lower = text.lowercase()

        // 1. Cek istilah/slang populer bursa saham (misal: "si ijo" -> GOTO)
        for ((slang, ticker) in slangStockMap) {
            if (lower.contains(slang)) {
                return ticker
            }
        }

        // 2. Prioritaskan cashtags seperti $BBRI atau #BBRI yang lazim di Threads / Stockbit
        val cashMatch = cashtagRegex.find(text)
        if (cashMatch != null) {
            val candidate = cashMatch.groupValues[1].uppercase()
            if (!nonTickerBlacklist.contains(candidate)) {
                return candidate
            }
        }

        // 3. Cek daftar emiten populer IDX secara case-insensitive (misal: "saham bbri" atau "goto")
        val words = text.split(Regex("""[^A-Za-z0-9]""")).filter { it.isNotBlank() }
        for (word in words) {
            val upper = word.uppercase()
            if (commonTickers.contains(upper)) {
                return upper
            }
        }

        // 3. Cek kode emiten 4 huruf kapital standard jika ada konteks bursa saham
        val hasContext = containsStockContext(text)
        val matches = tickerRegex.findAll(text)
        for (match in matches) {
            val candidate = match.value
            if (!nonTickerBlacklist.contains(candidate) && (hasContext || commonTickers.contains(candidate))) {
                return candidate
            }
        }

        return null
    }

    fun containsStockContext(text: String): Boolean {
        val lower = text.lowercase()
        for (keyword in stockKeywords) {
            if (lower.contains(keyword)) {
                return true
            }
        }
        return false
    }

    fun shouldTriggerAnalysis(text: String): Boolean {
        val hasContext = containsStockContext(text)
        val ticker = extractPotentialTicker(text)
        return hasContext || ticker != null
    }

    fun extractStockClaim(text: String): String? {
        val trimmed = text.trim()
        if (trimmed.length < 5) return null
        if (!shouldTriggerAnalysis(trimmed)) return null
        return if (trimmed.length > 300) {
            trimmed.substring(0, 300) + "..."
        } else {
            trimmed
        }
    }
}
