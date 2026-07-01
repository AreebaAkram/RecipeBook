
package com.recipebook.community.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.recipebook.community.data.FirebaseService
import com.recipebook.community.data.model.Community
import com.recipebook.community.data.model.CommunityMember
import com.recipebook.community.data.model.Recipe
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

class CommunityViewModel : ViewModel() {

    private val _community = MutableStateFlow<Community?>(null)
    val community: StateFlow<Community?> = _community

    private val _members = MutableStateFlow<List<CommunityMember>>(emptyList())
    val members: StateFlow<List<CommunityMember>> = _members

    private val _pastMembers = MutableStateFlow<List<CommunityMember>>(emptyList())
    val pastMembers: StateFlow<List<CommunityMember>> = _pastMembers

    private val _removeMemberError = MutableStateFlow<String?>(null)
    val removeMemberError: StateFlow<String?> = _removeMemberError

    // FIX #3: store communityId internally so removeMember always has it
    private var communityId = ""

    fun init(cid: String) {
        if (cid.isEmpty()) return
        if (communityId == cid) return
        communityId = cid
        listenCommunityInfo(cid)
        listenMembers(cid)
    }

    private fun listenCommunityInfo(cid: String) {
        FirebaseService.communityRef(cid).child("info")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    _community.value = snap.getValue(Community::class.java)
                }
                override fun onCancelled(e: DatabaseError) {}
            })
    }

    private fun listenMembers(cid: String) {
        FirebaseService.membersRef(cid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val all = snap.children.mapNotNull { child ->
                        try {
                            child.getValue(CommunityMember::class.java)
                                ?.copy(uid = child.key ?: "")
                        } catch (e: Exception) { null }
                    }
                    _members.value     = all.filter { it.isActive }
                    _pastMembers.value = all.filterNot { it.isActive }
                }
                override fun onCancelled(e: DatabaseError) {}
            })
    }

    // FIX #3: no longer needs communityId passed from caller —
    // uses the internally stored one which is always set after init().
    fun removeMember(memberUid: String) {
        if (communityId.isEmpty()) {
            _removeMemberError.value = "Community not loaded yet. Please try again."
            return
        }
        viewModelScope.launch {
            try {
                FirebaseService.membersRef(communityId).child(memberUid)
                    .child("isActive").setValue(false).await()
                FirebaseService.membersRef(communityId).child(memberUid)
                    .child("removedAt").setValue(System.currentTimeMillis()).await()
                FirebaseService.userRef(memberUid)
                    .child("communityId").setValue("").await()
                _removeMemberError.value = null
            } catch (e: Exception) {
                _removeMemberError.value = e.message ?: "Failed to remove member"
            }
        }
    }

    fun clearRemoveMemberError() {
        _removeMemberError.value = null
    }

    fun regenerateInviteCode(communityId: String, oldCode: String) {
        viewModelScope.launch {
            val newCode = UUID.randomUUID().toString().replace("-", "").take(6).uppercase()
            FirebaseService.inviteCodesRef().child(oldCode).removeValue().await()
            FirebaseService.inviteCodesRef().child(newCode).setValue(communityId).await()
            FirebaseService.communityRef(communityId).child("info")
                .child("inviteCode").setValue(newCode).await()
        }
    }

    fun exportData(communityId: String, recipes: List<Recipe>, downloadsDir: File, onDone: (String) -> Unit) {
        viewModelScope.launch {
            val fileName = "recipe_export_${System.currentTimeMillis()}.json"
            val file     = File(downloadsDir, fileName)
            val json     = Gson().toJson(mapOf("communityId" to communityId, "recipes" to recipes))
            file.writeText(json)
            onDone(file.absolutePath)
        }
    }
}

