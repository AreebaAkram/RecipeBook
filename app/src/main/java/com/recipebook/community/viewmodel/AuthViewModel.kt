package com.recipebook.community.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.recipebook.community.data.FirebaseService
import com.recipebook.community.data.model.Community
import com.recipebook.community.data.model.CommunityMember
import com.recipebook.community.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID


sealed class AuthState {
    object Loading        : AuthState()
    object Idle           : AuthState()
    object Authenticated  : AuthState()
    object NeedsCommunity : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val _authState  = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init { checkSession() }

    private fun checkSession() {
        val fbUser = FirebaseService.auth.currentUser
        if (fbUser == null) {
            _authState.value = AuthState.Idle
        } else {
            loadUser(fbUser.uid)
        }
    }

    private fun loadUser(uid: String) {
        FirebaseService.userRef(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val user = snap.getValue(User::class.java)
                if (user == null) { _authState.value = AuthState.Idle; return }
                _currentUser.value = user
                _authState.value   = if (user.communityId.isEmpty())
                    AuthState.NeedsCommunity else AuthState.Authenticated
            }
            override fun onCancelled(e: DatabaseError) {
                _authState.value = AuthState.Idle
            }
        })
    }

    fun signUp(name: String, email: String, password: String, avatarColor: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = FirebaseService.auth
                    .createUserWithEmailAndPassword(email, password).await()
                val uid  = result.user!!.uid
                val user = User(
                    uid         = uid,
                    displayName = name,
                    email       = email,
                    avatarColor = avatarColor,
                    avatarSeed  = "",
                    avatarStyle = "adventurer",
                    role        = "member",
                    joinedAt    = System.currentTimeMillis()
                )
                FirebaseService.userRef(uid).setValue(user).await()
                _currentUser.value = user
                _authState.value   = AuthState.NeedsCommunity
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign up failed")
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = FirebaseService.auth
                    .signInWithEmailAndPassword(email, password).await()
                loadUser(result.user!!.uid)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign in failed")
            }
        }
    }

    fun createCommunity(communityName: String) {
        viewModelScope.launch {
            val uid  = FirebaseService.currentUid ?: return@launch
            val user = _currentUser.value ?: return@launch
            val cid  = FirebaseService.communitiesRef().push().key ?: return@launch
            val code = generateInviteCode()

            val community = Community(
                id         = cid,
                name       = communityName,
                adminUid   = uid,
                inviteCode = code,
                createdAt  = System.currentTimeMillis()
            )
            val member = CommunityMember(
                uid         = uid,
                displayName = user.displayName,
                email       = user.email,
                role        = "admin",
                joinedAt    = System.currentTimeMillis(),
                avatarColor = user.avatarColor,
                avatarSeed  = user.avatarSeed,
                avatarStyle = user.avatarStyle
            )
            FirebaseService.communityRef(cid).child("info").setValue(community).await()
            FirebaseService.membersRef(cid).child(uid).setValue(member).await()
            FirebaseService.inviteCodesRef().child(code).setValue(cid).await()

            val updated = user.copy(communityId = cid, role = "admin")
            FirebaseService.userRef(uid).setValue(updated).await()
            _currentUser.value = updated
            _authState.value   = AuthState.Authenticated
        }
    }

    fun joinCommunity(inviteCode: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val uid  = FirebaseService.currentUid ?: return@launch
                val user = _currentUser.value ?: return@launch
                val snap = FirebaseService.inviteCodesRef()
                    .child(inviteCode.uppercase()).get().await()
                val cid  = snap.getValue(String::class.java)
                    ?: run { _authState.value = AuthState.Error("Invalid invite code"); return@launch }

                val member = CommunityMember(
                    uid         = uid,
                    displayName = user.displayName,
                    email       = user.email,
                    role        = "member",
                    joinedAt    = System.currentTimeMillis(),
                    avatarColor = user.avatarColor,
                    avatarSeed  = user.avatarSeed,
                    avatarStyle = user.avatarStyle
                )
                FirebaseService.membersRef(cid).child(uid).setValue(member).await()
                val updated = user.copy(communityId = cid, role = "member")
                FirebaseService.userRef(uid).setValue(updated).await()
                _currentUser.value = updated
                _authState.value   = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to join")
            }
        }
    }

    fun signOut() {
        FirebaseService.auth.signOut()
        _currentUser.value = null
        _authState.value   = AuthState.Idle
    }

    private fun generateInviteCode() =
        UUID.randomUUID().toString().replace("-", "").take(6).uppercase()
}