package com.hm.demo.model

/**
 * 推荐角色信息
 */
data class RecommendCharacter(
    val characterId: Long? = null,
    val creatorGuid: Long? = null,
    val avatar: String? = null,
    val name: String? = null,
    val gender: String? = null,
    val chatUserCount: Int? = null,
    val identity: String? = null,
    val otherSetting: String? = null,
    val qurl: String? = null,
    val prologue: String? = null,
    val tagList: List<TagInfo>? = null,
    val isFollow: Boolean? = null,
    val avatarBack: String? = null,
    val androidAvatarBack: String? = null,
    val prologueAudio: String? = null,
    val audioDuration: Long? = null,
    val chatUrl: String? = null,
    val interactTestUrl: String? = null,
    val chatQurl: String? = null,
    val inviteQurl: String? = null,
    val inviteGuide: Boolean? = null,
    val type: Int? = null,
    val tag3d: String? = null,
    val videoUrl: String? = null,
    val firstFrameUrl: String? = null,
    val touchQurl: String? = null
)