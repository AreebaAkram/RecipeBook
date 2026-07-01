package com.recipebook.community.data

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.google.firebase.storage.storage

object FirebaseService {
    val auth     by lazy { Firebase.auth }
    val database by lazy { Firebase.database.reference }
    val storage  by lazy { Firebase.storage.reference }

    val currentUid get() = auth.currentUser?.uid

    fun communitiesRef()                    = database.child("communities")
    fun communityRef(cid: String)           = database.child("communities").child(cid)
    fun recipesRef(cid: String)             = database.child("communities").child(cid).child("recipes")
    fun recipeRef(cid: String, rid: String) = recipesRef(cid).child(rid)
    fun activityRef(cid: String)            = database.child("communities").child(cid).child("activity")
    fun membersRef(cid: String)             = database.child("communities").child(cid).child("members")
    fun usersRef()                          = database.child("users")
    fun userRef(uid: String)                = database.child("users").child(uid)
    fun inviteCodesRef()                    = database.child("inviteCodes")
}