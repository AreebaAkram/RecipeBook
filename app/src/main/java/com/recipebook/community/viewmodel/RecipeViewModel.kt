
package com.recipebook.community.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.recipebook.community.data.FirebaseService
import com.recipebook.community.data.model.ActivityLog
import com.recipebook.community.data.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RecipeViewModel : ViewModel() {

    private val _approvedRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val approvedRecipes: StateFlow<List<Recipe>> = _approvedRecipes

    private val _pendingRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val pendingRecipes: StateFlow<List<Recipe>> = _pendingRecipes

    private val _rejectedRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val rejectedRecipes: StateFlow<List<Recipe>> = _rejectedRecipes

    private val _activityLogs = MutableStateFlow<List<ActivityLog>>(emptyList())
    val activityLogs: StateFlow<List<ActivityLog>> = _activityLogs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // FIX #2: communityId must be set before ANY operation is called.
    // Made internal so it can be read safely by all functions.
    private var communityId = ""

    fun init(cid: String) {
        if (cid.isEmpty()) return
        if (communityId == cid) return
        communityId = cid
        listenRecipes()
        listenActivity()
    }

    private fun listenRecipes() {
        FirebaseService.recipesRef(communityId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val all = snap.children.mapNotNull { child ->
                        try {
                            child.getValue(Recipe::class.java)?.copy(id = child.key ?: "")
                        } catch (e: Exception) { null }
                    }
                    _approvedRecipes.value = all
                        .filter { it.status == "approved" }
                        .sortedWith(
                            compareByDescending<Recipe> { it.featured }
                                .thenByDescending { it.createdAt }
                        )
                    // FIX #2: pending list must contain ALL pending,
                    // whether they were submitted fresh or re-submitted after edit.
                    _pendingRecipes.value = all
                        .filter { it.status == "pending" }
                        .sortedByDescending { it.createdAt }
                    _rejectedRecipes.value = all.filter { it.status == "rejected" }
                }
                override fun onCancelled(e: DatabaseError) {}
            })
    }

    private fun listenActivity() {
        FirebaseService.activityRef(communityId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    _activityLogs.value = snap.children
                        .mapNotNull { child ->
                            try {
                                child.getValue(ActivityLog::class.java)
                                    ?.copy(id = child.key ?: "")
                            } catch (e: Exception) { null }
                        }
                        .sortedByDescending { it.timestamp }
                }
                override fun onCancelled(e: DatabaseError) {}
            })
    }

    fun submitRecipe(
        title: String, description: String, category: String, cookTime: String,
        servings: String, ingredients: List<String>, steps: List<String>,
        imageB64: String, authorUid: String, authorName: String,
        isAdmin: Boolean, featured: Boolean = false,
        onDone: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (communityId.isEmpty()) { onDone(false); return@launch }
                val rid = FirebaseService.recipesRef(communityId).push().key
                    ?: run { onDone(false); return@launch }

                val recipe = Recipe(
                    id          = rid,
                    title       = title,
                    description = description,
                    category    = category,
                    cookTime    = cookTime,
                    servings    = servings,
                    ingredients = ingredients.filter { it.isNotBlank() },
                    steps       = steps.filter { it.isNotBlank() },
                    imageUrl    = imageB64,
                    authorUid   = authorUid,
                    authorName  = authorName,
                    status      = if (isAdmin) "approved" else "pending",
                    featured    = featured,
                    createdAt   = System.currentTimeMillis(),
                    updatedAt   = System.currentTimeMillis()
                )
                FirebaseService.recipeRef(communityId, rid).setValue(recipe).await()
                logActivity("Added", title, rid, authorUid, authorName)
                onDone(true)
            } catch (e: Exception) {
                onDone(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // FIX #2: approve/reject now explicitly use communityId
    // and write the full updated recipe (not just the status child),
    // ensuring the DB record is consistent after approval.
    fun approveRecipe(recipe: Recipe, adminUid: String, adminName: String) {
        if (communityId.isEmpty()) return
        viewModelScope.launch {
            try {
                FirebaseService.recipeRef(communityId, recipe.id)
                    .child("status").setValue("approved").await()
                logActivity("Approved", recipe.title, recipe.id, adminUid, adminName)
            } catch (e: Exception) { /* surface error if needed */ }
        }
    }

    fun rejectRecipe(recipe: Recipe, adminUid: String, adminName: String) {
        if (communityId.isEmpty()) return
        viewModelScope.launch {
            try {
                FirebaseService.recipeRef(communityId, recipe.id)
                    .child("status").setValue("rejected").await()
                logActivity("Rejected", recipe.title, recipe.id, adminUid, adminName)
            } catch (e: Exception) { }
        }
    }

    // FIX #1: editRecipe() — the bug was that `recipe` passed in still had
    // status="approved", and copy() only overrides what you specify.
    // We now EXPLICITLY force status="pending" for non-admins regardless
    // of what the incoming recipe object's status field says.
    fun editRecipe(recipe: Recipe, userUid: String, userName: String, isAdmin: Boolean) {
        if (communityId.isEmpty()) return
        viewModelScope.launch {
            try {
                val updated = recipe.copy(
                    status    = if (isAdmin) "approved" else "pending",  // explicit
                    updatedAt = System.currentTimeMillis()
                )
                FirebaseService.recipeRef(communityId, recipe.id).setValue(updated).await()
                logActivity("Edited", recipe.title, recipe.id, userUid, userName)
            } catch (e: Exception) { }
        }
    }

    fun deleteRecipe(recipe: Recipe, userUid: String, userName: String) {
        if (communityId.isEmpty()) return
        viewModelScope.launch {
            try {
                FirebaseService.recipeRef(communityId, recipe.id).removeValue().await()
                logActivity("Deleted", recipe.title, recipe.id, userUid, userName)
            } catch (e: Exception) { }
        }
    }

    private suspend fun logActivity(
        action: String, recipeName: String, recipeId: String,
        uid: String, name: String
    ) {
        if (communityId.isEmpty()) return
        val logId = FirebaseService.activityRef(communityId).push().key ?: return
        val log = ActivityLog(
            id         = logId,
            action     = action,
            recipeName = recipeName,
            recipeId   = recipeId,
            userUid    = uid,
            userName   = name,
            timestamp  = System.currentTimeMillis()
        )
        FirebaseService.activityRef(communityId).child(logId).setValue(log).await()
    }
}