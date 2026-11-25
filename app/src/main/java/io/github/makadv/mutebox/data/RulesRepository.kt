package io.github.makadv.mutebox.data

import android.content.Context
import io.github.makadv.mutebox.domain.AppRule
import io.github.makadv.mutebox.domain.RuleMatcher
import io.github.makadv.mutebox.domain.RuleMode
import io.github.makadv.mutebox.domain.SeenRetention
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class SeenSender(val name: String, val lastSeen: Long)

/**
 * Regole per app e mittenti visti di recente, salvati come JSON in SharedPreferences.
 * I dati sono pochi: un database non serve.
 * I nomi visti si salvano solo per le app in modalità "alcune persone" e scadono dopo 30 giorni.
 */
class RulesRepository(context: Context) {
    private val prefs = context.getSharedPreferences("rules", Context.MODE_PRIVATE)

    private val _rules = MutableStateFlow(loadRules())
    val rules: StateFlow<Map<String, AppRule>> = _rules.asStateFlow()

    private val _seen = MutableStateFlow(loadSeen())
    val seen: StateFlow<Map<String, List<SeenSender>>> = _seen.asStateFlow()

    init {
        // Applica subito scadenza e minimizzazione ai nomi salvati dalle versioni precedenti.
        if (prefs.getString("seen", null) != null) {
            val kept = _seen.value.filterKeys { _rules.value[it]?.mode == RuleMode.SOME }
            _seen.value = kept
            saveSeen(kept)
        }
    }

    fun ruleFor(packageName: String): AppRule? = _rules.value[packageName]

    @Synchronized
    fun setMode(packageName: String, mode: RuleMode) {
        val old = _rules.value[packageName]
        putRule(AppRule(packageName, mode, old?.senders.orEmpty()))
        // I nomi visti servono solo per scegliere le persone: senza modalità "alcune persone" non vanno tenuti.
        if (mode != RuleMode.SOME && _seen.value.containsKey(packageName)) {
            val all = _seen.value - packageName
            _seen.value = all
            saveSeen(all)
        }
    }

    /** Cancella tutti i nomi visti nelle notifiche. */
    @Synchronized
    fun clearSeen() {
        _seen.value = emptyMap()
        prefs.edit().remove("seen").commit()
    }

    /** Cancella regole e nomi visti (diritto alla cancellazione). */
    @Synchronized
    fun clearAll() {
        _rules.value = emptyMap()
        _seen.value = emptyMap()
        prefs.edit().clear().commit()
    }

    /** Tutti i dati salvati, in JSON leggibile (diritto di accesso e portabilità). */
    fun exportJson(): JSONObject = JSONObject()
        .put("rules", rulesToJson(_rules.value))
        .put("recentlySeenSenders", seenToJson(_seen.value))

    @Synchronized
    fun addSender(packageName: String, name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        val old = _rules.value[packageName] ?: AppRule(packageName, RuleMode.SOME, emptyList())
        val norm = RuleMatcher.normalize(clean)
        if (old.senders.any { RuleMatcher.normalize(it) == norm }) return
        putRule(old.copy(senders = old.senders + clean))
    }

    @Synchronized
    fun removeSender(packageName: String, name: String) {
        val old = _rules.value[packageName] ?: return
        putRule(old.copy(senders = old.senders - name))
    }

    /** Ricorda i nomi visti nelle notifiche, per suggerirli nell'interfaccia. */
    @Synchronized
    fun recordSeen(packageName: String, names: List<String>) {
        if (names.isEmpty()) return
        val now = System.currentTimeMillis()
        val current = _seen.value[packageName].orEmpty()
        var changed = false
        val byName = current.associateBy { it.name }.toMutableMap()
        for (n in names) {
            val old = byName[n]
            // Scrive solo se il nome è nuovo o non visto da più di un'ora.
            if (old == null || now - old.lastSeen > 60 * 60 * 1000L) {
                byName[n] = SeenSender(n, now)
                changed = true
            }
        }
        if (!changed) return
        val list = SeenRetention.prune(byName.values.toList(), now) { it.lastSeen }
            .sortedByDescending { it.lastSeen }
            .take(MAX_SEEN_PER_APP)
        val all = _seen.value.toMutableMap()
        all[packageName] = list
        _seen.value = all
        saveSeen(all)
    }

    private fun putRule(rule: AppRule) {
        val next = _rules.value.toMutableMap()
        next[rule.packageName] = rule
        _rules.value = next
        saveRules(next)
    }

    private fun loadRules(): Map<String, AppRule> {
        val raw = prefs.getString("rules", null) ?: return emptyMap()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).associate { i ->
                val o = arr.getJSONObject(i)
                val senders = o.getJSONArray("senders").let { s -> (0 until s.length()).map { s.getString(it) } }
                val rule = AppRule(o.getString("pkg"), RuleMode.valueOf(o.getString("mode")), senders)
                rule.packageName to rule
            }
        }.getOrDefault(emptyMap())
    }

    private fun saveRules(rules: Map<String, AppRule>) {
        prefs.edit().putString("rules", rulesToJson(rules).toString()).apply()
    }

    private fun rulesToJson(rules: Map<String, AppRule>): JSONArray {
        val arr = JSONArray()
        rules.values.forEach { r ->
            arr.put(JSONObject().put("pkg", r.packageName).put("mode", r.mode.name).put("senders", JSONArray(r.senders)))
        }
        return arr
    }

    private fun loadSeen(): Map<String, List<SeenSender>> {
        val raw = prefs.getString("seen", null) ?: return emptyMap()
        val now = System.currentTimeMillis()
        return runCatching {
            val o = JSONObject(raw)
            o.keys().asSequence().associateWith { pkg ->
                val a = o.getJSONArray(pkg)
                val list = (0 until a.length()).map { i ->
                    val e = a.getJSONObject(i)
                    SeenSender(e.getString("n"), e.getLong("t"))
                }
                SeenRetention.prune(list, now) { it.lastSeen }
            }.filterValues { it.isNotEmpty() }
        }.getOrDefault(emptyMap())
    }

    private fun saveSeen(all: Map<String, List<SeenSender>>) {
        prefs.edit().putString("seen", seenToJson(all).toString()).apply()
    }

    private fun seenToJson(all: Map<String, List<SeenSender>>): JSONObject {
        val o = JSONObject()
        all.forEach { (pkg, list) ->
            o.put(pkg, JSONArray().apply { list.forEach { put(JSONObject().put("n", it.name).put("t", it.lastSeen)) } })
        }
        return o
    }

    private companion object {
        const val MAX_SEEN_PER_APP = 40
    }
}
