package com.example.pebblenote

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

/**
 * Super-simple local persistence for demo notes.
 * Stores a JSON array in a dedicated SharedPreferences file so logout doesn't clear it.
 */
object LocalNotesStore {
    private const val PREFS_NOTES = "pebble_notes_store"
    private const val KEY_NOTES = "notes_json"

    fun load(context: Context): List<AdminNote> {
        val prefs = context.getSharedPreferences(PREFS_NOTES, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_NOTES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            val list = ArrayList<AdminNote>(arr.length())
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.optInt("id", 0)
                val title = obj.optString("title", "Untitled")
                val price = obj.optDouble("price", 0.0)
                val pdfStr = obj.optString("pdfUri", "").ifBlank { null }
                val pdfUri = pdfStr?.let { runCatching { Uri.parse(it) }.getOrNull() }
                val previews = mutableListOf<Uri>()
                val pArr = obj.optJSONArray("previewImageUris") ?: JSONArray()
                for (j in 0 until pArr.length()) {
                    val s = pArr.optString(j).orEmpty()
                    if (s.isNotBlank()) runCatching { Uri.parse(s) }.getOrNull()?.let { previews.add(it) }
                }
                val category = obj.optString("category", "General")
                val description = obj.optString("description", "")
                val enabled = obj.optBoolean("enabled", true)
                list.add(AdminNote(id, title, price, pdfUri, previews, category, description, enabled))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, notes: List<AdminNote>) {
        val arr = JSONArray()
        notes.forEach { n ->
            val obj = JSONObject()
            obj.put("id", n.id)
            obj.put("title", n.title)
            obj.put("price", n.price)
            obj.put("pdfUri", n.pdfUri?.toString() ?: "")
            val pArr = JSONArray()
            n.previewImageUris.forEach { pArr.put(it.toString()) }
            obj.put("previewImageUris", pArr)
            obj.put("category", n.category)
            obj.put("description", n.description)
            obj.put("enabled", n.enabled)
            arr.put(obj)
        }
        val prefs = context.getSharedPreferences(PREFS_NOTES, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_NOTES, arr.toString()).apply()
    }
}
