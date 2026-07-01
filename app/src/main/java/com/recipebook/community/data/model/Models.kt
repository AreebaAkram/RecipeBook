package com.recipebook.community.data.model

data class Recipe(
    val id          : String = "",
    val title       : String = "",
    val description : String = "",
    val category    : String = "",
    val cookTime    : String = "",
    val servings    : String = "",
    val ingredients : List<String> = emptyList(),
    val steps       : List<String> = emptyList(),
    val imageUrl    : String = "",
    val authorUid   : String = "",
    val authorName  : String = "",
    val status      : String = "pending",
    val createdAt   : Long   = 0L,
    val updatedAt   : Long   = 0L,
    val featured    : Boolean = false

    )

data class User(
    val uid         : String = "",
    val displayName : String = "",
    val email       : String = "",
    val avatarColor : String = "#1D9E75",
    val avatarSeed   : String = "",
    val avatarStyle  : String = "adventurer",
    val role        : String = "member",
    val communityId : String = "",
    val joinedAt    : Long   = 0L,
    val recipesAdded   : Int = 0,
    val editsMade      : Int = 0,
    val recipesDeleted : Int = 0
)

data class Community(
    val id         : String = "",
    val name       : String = "",
    val adminUid   : String = "",
    val inviteCode : String = "",
    val createdAt  : Long   = 0L
)

data class CommunityMember(
    val uid         : String = "",
    val displayName : String = "",
    val email       : String = "",
    val role        : String = "member",
    val joinedAt    : Long   = 0L,
    val avatarColor : String = "#1D9E75",
    val avatarStyle : String = "adventurer",
    val avatarSeed  : String = "",
    val isActive    : Boolean = true,   // ← NEW: false = past member
    val removedAt   : Long    = 0L
)

data class ActivityLog(
    val id         : String = "",
    val action     : String = "",
    val recipeName : String = "",
    val recipeId   : String = "",
    val userUid    : String = "",
    val userName   : String = "",
    val timestamp  : Long   = 0L
)
