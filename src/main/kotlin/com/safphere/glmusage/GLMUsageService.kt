package com.safphere.glmusage

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.openapi.diagnostic.Logger
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

data class GLMUsageData(
    val platform: String,
    val timestamp: Long,
    val totals: Totals,
    val quotas: Quotas,
    val history: List<HistoryPoint>,
    val error: String? = null
)

data class Totals(val calls: Long, val tokens: Long)
data class Quotas(val mcp: QuotaItem, val token5h: QuotaItem)
data class QuotaItem(val used: Double, val total: Double, val pct: Double)
data class HistoryPoint(val time: String, val calls: Int)

object GLMUsageService {
    private val LOG = Logger.getInstance(GLMUsageService::class.java)
    private val gson = Gson()

    // Initialize trust-all certificates (for development/testing)
    private fun initTrustAllCerts() {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
        } catch (e: Exception) {
            LOG.warn("Failed to initialize trust-all certificates", e)
        }
    }

    init {
        initTrustAllCerts()
    }

    fun fetchData(): GLMUsageData? {
        val settings = GLMSettingsState.instance

        // Return mock data if enabled
        if (settings.useMockData) {
            return generateMockData(settings.apiBaseUrl)
        }

        // Check for API key in settings or environment variable
        val apiKey = if (settings.apiKey.isNotEmpty()) {
            settings.apiKey
        } else {
            System.getenv("ANTHROPIC_AUTH_TOKEN") ?: ""
        }

        if (apiKey.isEmpty()) {
            LOG.warn("No API key configured")
            return GLMUsageData(
                "智谱AI",
                System.currentTimeMillis(),
                Totals(0, 0),
                Quotas(QuotaItem(0.0, 4000.0, 0.0), QuotaItem(0.0, 100.0, 0.0)),
                emptyList(),
                error = "未配置 API Token"
            )
        }

        // Log API key format (first 10 chars only for security)
        LOG.info("Using API key format: ${apiKey.substring(0, kotlin.math.min(apiKey.length, 10))}...")
        if (apiKey.contains(".")) {
            LOG.info("Detected id.secret format API key")
        } else if (apiKey.startsWith("sk-")) {
            LOG.info("Detected sk-xxx format API key")
        }

        try {
            val now = LocalDateTime.now()
            // Match VSCode: start.setDate(now.getDate() - 1); start.setMinutes(0); (Seconds preserved)
            val start = now.minusDays(1).withMinute(0)
            // Match VSCode: end.setMinutes(59); (Seconds preserved)
            val end = now.withMinute(59)

            val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val q = "?startTime=${encode(start.format(fmt))}&endTime=${encode(end.format(fmt))}"

            val baseUrl = settings.apiBaseUrl
            val platform = if (baseUrl.contains("z.ai")) "Z.AI" else "智谱AI"

            // Construct domain from baseUrl (remove /api/anthropic suffix)
            val base = java.net.URL(baseUrl)
            val domain = "${base.protocol}://${base.host}"

            LOG.info("Fetching data from $domain")

            // Match VSCode extension exactly: /api/monitor/usage/...
            val modelResp = request("$domain/api/monitor/usage/model-usage$q", apiKey)
            val toolResp = request("$domain/api/monitor/usage/tool-usage$q", apiKey) // Fetch but unused, for network identicality? VS Code fetches it.
            val quotaResp = request("$domain/api/monitor/usage/quota/limit", apiKey)

            return processData(modelResp, quotaResp, platform)

        } catch (e: Exception) {
            LOG.warn("Failed to fetch GLM usage: ${e.message}", e)
            return GLMUsageData(
                "智谱AI",
                System.currentTimeMillis(),
                Totals(0, 0),
                Quotas(QuotaItem(0.0, 4000.0, 0.0), QuotaItem(0.0, 100.0, 0.0)),
                emptyList(),
                error = "请求失败: ${e.message}"
            )
        }
    }

    fun generateMockData(baseUrl: String): GLMUsageData {
        val platform = if (baseUrl.contains("z.ai")) "Z.AI" else "智谱AI"
        val random = kotlin.random.Random

        // Generate mock history data (24 hours)
        val history = mutableListOf<HistoryPoint>()
        val now = LocalDateTime.now()
        for (i in 0..23) {
            val time = now.minusHours((23 - i).toLong())
            val timeLabel = String.format("%02d:00", time.hour)
            val calls = random.nextInt(100)
            history.add(HistoryPoint(timeLabel, calls))
        }

        // Generate mock totals
        val totalCalls: Long = (random.nextDouble() * 1000).toLong()
        val totalTokens: Long = (random.nextDouble() * 5000000).toLong()

        // Generate mock quotas
        val mcpPct = random.nextInt(100)
        val t5hPct = random.nextInt(100)

        return GLMUsageData(
            platform,
            System.currentTimeMillis(),
            Totals(totalCalls, totalTokens),
            Quotas(
                QuotaItem(mcpPct.toDouble() * 40.0, 4000.0, mcpPct.toDouble()),
                QuotaItem(0.0, 0.0, t5hPct.toDouble())
            ),
            history
        )
    }
    
    private fun request(urlString: String, apiKey: String): JsonObject {
        val settings = GLMSettingsState.instance
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection

        try {
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            conn.connectTimeout = settings.timeout
            conn.readTimeout = settings.timeout

            LOG.info("Requesting: $urlString")

            val responseCode = conn.responseCode
            LOG.info("Response code: $responseCode")

            if (responseCode >= 400) {
                val errorStream = conn.errorStream
                val errorMsg = if (errorStream != null) {
                    InputStreamReader(errorStream).use { it.readText() }
                } else {
                    "HTTP $responseCode"
                }
                LOG.error("API request failed: $responseCode - $errorMsg")
                throw RuntimeException("HTTP $responseCode: $errorMsg")
            }

            InputStreamReader(conn.inputStream).use { reader ->
                val response = reader.readText()
                LOG.info("Response length: ${response.length} bytes")

                // Check if response is empty
                if (response.isEmpty()) {
                    LOG.error("Empty response from API")
                    throw RuntimeException("Empty response from API")
                }

                // Parse JSON
                try {
                    val json = gson.fromJson(response, JsonObject::class.java)
                    LOG.info("Successfully parsed JSON response")
                    return json
                } catch (e: Exception) {
                    LOG.error("Failed to parse JSON response: $response", e)
                    throw RuntimeException("Invalid JSON response: ${e.message}")
                }
            }
        } catch (e: Exception) {
            LOG.error("Request exception: ${e.message}", e)
            throw e
        } finally {
            conn.disconnect()
        }
    }
    
    private fun encode(s: String) = URLEncoder.encode(s, StandardCharsets.UTF_8.toString()).replace("+", "%20")
    
    fun processData(model: JsonObject, quota: JsonObject, platform: String): GLMUsageData {
        LOG.info("Processing data for platform: $platform")

        try {
            val mData = model.getAsJsonObject("data") ?: JsonObject()
            val qData = quota.getAsJsonObject("data") ?: JsonObject()

            LOG.info("Model data keys: ${mData.keySet()}")
            LOG.info("Quota data keys: ${qData.keySet()}")

            // History - match VSCode extension exactly
            val history = ArrayList<HistoryPoint>()
            if (mData.has("x_time") && mData.get("x_time").isJsonArray) {
                val times = mData.getAsJsonArray("x_time")
                val calls = if (mData.has("modelCallCount")) mData.getAsJsonArray("modelCallCount") else null

                LOG.info("History times count: ${times.size()}")
                if (calls != null) {
                    LOG.info("History calls count: ${calls.size()}")
                }

                for (i in 0 until times.size()) {
                    try {
                        val tStr = times[i].asString // "2024-01-01 10:00:00"
                        // Match VSCode: time.split(' ')[1] || time
                        val timeLabel = if (tStr.contains(" ")) tStr.split(" ")[1] else tStr
                        val c = if (calls != null && calls.size() > i) calls[i].asInt else 0
                        history.add(HistoryPoint(timeLabel, c))
                    } catch (e: Exception) {
                        LOG.warn("Error processing history point $i: ${e.message}")
                    }
                }
            } else {
                LOG.warn("No x_time array found in model data")
            }

            // Totals
            var totalCalls = 0L
            var totalTokens = 0L
            if (mData.has("totalUsage")) {
                val tu = mData.getAsJsonObject("totalUsage")
                totalCalls = if (tu.has("totalModelCallCount")) tu.get("totalModelCallCount").asLong else 0L
                totalTokens = if (tu.has("totalTokensUsage")) tu.get("totalTokensUsage").asLong else 0L
            }
            LOG.info("Totals - calls: $totalCalls, tokens: $totalTokens")

            // Quotas - match VSCode extension exactly
            var mcp = QuotaItem(0.0, 4000.0, 0.0)
            var t5h = QuotaItem(0.0, 100.0, 0.0)

            if (qData.has("limits")) {
                val limits = qData.getAsJsonArray("limits")
                LOG.info("Quota limits count: ${limits.size()}")

                for (elem in limits) {
                    try {
                        val obj = elem.asJsonObject
                        if (!obj.has("type")) {
                            LOG.warn("Limit object missing 'type' field: $obj")
                            continue
                        }

                        val type = obj.get("type").asString
                        if (type == "TIME_LIMIT") {
                            val used = if (obj.has("currentValue")) obj.get("currentValue").asDouble else 0.0
                            val total = if (obj.has("usage")) obj.get("usage").asDouble else 4000.0
                            // VSCode: Math.round(l.percentage * 100)
                            val pct = if (obj.has("percentage")) (obj.get("percentage").asDouble * 100) else 0.0
                            mcp = QuotaItem(used, total, Math.round(pct).toDouble())
                            LOG.info("MCP quota - used: $used, total: $total, pct: ${Math.round(pct)}")
                        } else if (type == "TOKENS_LIMIT") {
                            // VSCode: parseFloat(l.percentage || 0) - No multiplication!
                            val pct = if (obj.has("percentage")) obj.get("percentage").asDouble else 0.0
                            t5h = QuotaItem(0.0, 0.0, pct)
                            LOG.info("Token quota - pct: $pct")
                        }
                    } catch (e: Exception) {
                        LOG.warn("Error processing quota limit: ${e.message}")
                    }
                }
            } else {
                LOG.warn("No limits array found in quota data")
            }

            val result = GLMUsageData(
                platform,
                System.currentTimeMillis(),
                Totals(totalCalls, totalTokens),
                Quotas(mcp, t5h),
                history
            )

            LOG.info("Successfully processed data: $result")
            return result

        } catch (e: Exception) {
            LOG.error("Error processing data: ${e.message}", e)
            throw e
        }
    }
}
