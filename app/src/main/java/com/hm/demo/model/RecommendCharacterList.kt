package com.hm.demo.model

/**
 * 推荐角色列表数据类
 */
data class RecommendCharacterList(
    val characters: List<RecommendCharacter> = emptyList()
) {
    
    companion object {
        /**
         * 根据提供的JSON数据创建推荐角色列表
         */
        fun createSampleData(): RecommendCharacterList {
            val characters = listOf(
                RecommendCharacter(
                    characterId = 4895455236L,
                    creatorGuid = 0L,
                    avatar = "https://zmdcharactercdn-new.zhumengdao.com/test/customAvatar/20260107/dee922ef-0d2f-4475-9497-b03e03b93c16/a9009029e5c5be6ba368c3d8a0f9692a.jpg",
                    name = "凌意",
                    gender = null,
                    chatUserCount = null,
                    identity = "你的天降未婚夫，对抗路同居搭子",
                    otherSetting = "走到home页兜底的逻辑了。 ",
                    qurl = null,
                    prologue = null,
                    tagList = listOf(
                        TagInfo("信仰崩塌"),
                        TagInfo("复仇式爱人")
                    ),
                    isFollow = null,
                    avatarBack = "https://zmdcharactercdn-new.zhumengdao.com/character/1748490064416.png",
                    androidAvatarBack = "https://zmdcharactercdn-new.zhumengdao.com/character/1748490064416.png",
                    prologueAudio = "https://zmdcharactercdn-new.zhumengdao.com/test/voice/20260107/mp3/53746002591385190434.mp3",
                    audioDuration = 16602L,
                    chatUrl = "ywdreamer://nativepage/virtualCharacter/chat?characterId=4895455236&imSource=18&imFrom=6",
                    interactTestUrl = "ywdreamer://nativepage/home/invitedCharacter?characterId=4895455236&imSource=13",
                    chatQurl = "ywdreamer://nativepage/virtualCharacter/chat?characterId=4895455236&imSource=18&imFrom=6",
                    inviteQurl = "ywdreamer://nativepage/home/invitedCharacter?characterId=4895455236&imSource=13&characterType=1",
                    inviteGuide = false,
                    type = 1,
                    tag3d = "3D互动",
                    videoUrl = "https://zmdcharactercdn.zhumengdao.com/video/7893aa456719393642ad3f814f049466/7893aa456719393642ad3f814f049466.mp4",
                    firstFrameUrl = "https://imgservices-1252317822.image.myqcloud.com/coco/b12202024/9f8a46fc.1klwjd.png",
                    touchQurl = "ywdreamer://nativepage/home/character3D?characterId=4895455236"
                ),
                RecommendCharacter(
                    characterId = 2973110933L,
                    creatorGuid = 0L,
                    avatar = "https://zmdcharactercdn-new.zhumengdao.com/test/character/20250422/9d688930-bdfa-4ffe-9b9f-3e75e305d405/e245f977c18e866d3bb9a7c835505f0b.jpg",
                    name = "马力",
                    gender = null,
                    chatUserCount = null,
                    identity = "姿容绝美但蠢萌的憨憨美人",
                    otherSetting = "txf 没有选择意图b组推荐人物。",
                    qurl = null,
                    prologue = null,
                    tagList = listOf(
                        TagInfo("黑皮糙汉"),
                        TagInfo("欲望困兽")
                    ),
                    isFollow = null,
                    avatarBack = "https://zmdcharactercdn-new.zhumengdao.com/character/1747391093014.jpg",
                    androidAvatarBack = "https://zmdcharactercdn-new.zhumengdao.com/character/1747391093014.jpg",
                    prologueAudio = "https://zmdcharactercdn-new.zhumengdao.com/test/voice/20250422/96e8ec2e-9561-40eb-8107-6ca0d5c30184/mp3/44315431469800243293.mp3",
                    audioDuration = 27351L,
                    chatUrl = "ywdreamer://nativepage/virtualCharacter/chat?characterId=2973110933&imSource=18&imFrom=6",
                    interactTestUrl = "ywdreamer://nativepage/home/invitedCharacter?characterId=2973110933&imSource=13",
                    chatQurl = "ywdreamer://nativepage/virtualCharacter/chat?characterId=2973110933&imSource=18&imFrom=6",
                    inviteQurl = "ywdreamer://nativepage/home/invitedCharacter?characterId=2973110933&imSource=13&characterType=0",
                    inviteGuide = false,
                    type = 0,
                    tag3d = null,
                    videoUrl = null,
                    firstFrameUrl = null,
                    touchQurl = null
                ),
                RecommendCharacter(
                    characterId = 8290015148L,
                    creatorGuid = 0L,
                    avatar = "https://zmdcharactercdn.zhumengdao.com/bb21e2b1c15854a1c9c377661b0e2e64.png",
                    name = "世界观人物001",
                    gender = null,
                    chatUserCount = null,
                    identity = "歌手",
                    otherSetting = "穿着西装的疯子，能一边念诉状，一边笑着压你跪下喊他\"任律师\"。",
                    qurl = null,
                    prologue = null,
                    tagList = listOf(
                        TagInfo("天生贵胄"),
                        TagInfo("审判式爱人")
                    ),
                    isFollow = null,
                    avatarBack = "https://zmdcharactercdn-new.zhumengdao.com/character/1748490100317.png",
                    androidAvatarBack = "https://zmdcharactercdn-new.zhumengdao.com/character/1748490100317.png",
                    prologueAudio = null,
                    audioDuration = 0L,
                    chatUrl = "ywdreamer://nativepage/virtualCharacter/chat?characterId=8290015148&imSource=18&imFrom=6",
                    interactTestUrl = "ywdreamer://nativepage/home/invitedCharacter?characterId=8290015148&imSource=13",
                    chatQurl = "ywdreamer://nativepage/virtualCharacter/chat?characterId=8290015148&imSource=18&imFrom=6",
                    inviteQurl = "ywdreamer://nativepage/home/invitedCharacter?characterId=8290015148&imSource=13&characterType=0",
                    inviteGuide = false,
                    type = 0,
                    tag3d = null,
                    videoUrl = null,
                    firstFrameUrl = null,
                    touchQurl = null
                ),
                RecommendCharacter(
                    characterId = 4686327891L,
                    creatorGuid = 0L,
                    avatar = "https://zmdcharactercdn.zhumengdao.com/46F20637D6C85BE5A066584A0BE39E0C.jpg",
                    name = "小",
                    gender = null,
                    chatUserCount = null,
                    identity = "将军",
                    otherSetting = "焊着止咬器的狼狗，低声乞求存在价值：请使用我，哪怕只为你沾血一次。",
                    qurl = null,
                    prologue = null,
                    tagList = listOf(
                        TagInfo("废墟忠犬"),
                        TagInfo("危险实验体")
                    ),
                    isFollow = null,
                    avatarBack = "https://zmdcharactercdn-new.zhumengdao.com/character/1748490110163.png",
                    androidAvatarBack = "https://zmdcharactercdn-new.zhumengdao.com/character/1748490110163.png",
                    prologueAudio = null,
                    audioDuration = 0L,
                    chatUrl = "ywdreamer://nativepage/virtualCharacter/chat?characterId=4686327891&imSource=18&imFrom=6",
                    interactTestUrl = "ywdreamer://nativepage/home/invitedCharacter?characterId=4686327891&imSource=13",
                    chatQurl = "ywdreamer://nativepage/virtualCharacter/chat?characterId=4686327891&imSource=18&imFrom=6",
                    inviteQurl = "ywdreamer://nativepage/home/invitedCharacter?characterId=4686327891&imSource=13&characterType=0",
                    inviteGuide = false,
                    type = 0,
                    tag3d = null,
                    videoUrl = null,
                    firstFrameUrl = null,
                    touchQurl = null
                ),
                RecommendCharacter(
                    characterId = 4247234696L,
                    creatorGuid = 0L,
                    avatar = "https://zmdcharactercdn.zhumengdao.com/450b3e4761b768a3b99d690109f139e4.png",
                    name = "比比东",
                    gender = null,
                    chatUserCount = null,
                    identity = "记者，记者，记者",
                    otherSetting = "「男人婆」拴在身边，这桩冲喜婚姻，是他挥金如土人生唯一的败笔。",
                    qurl = null,
                    prologue = null,
                    tagList = listOf(
                        TagInfo("娇妻保镖"),
                        TagInfo("189怨偶")
                    ),
                    isFollow = null,
                    avatarBack = "https://zmdcharactercdn-new.zhumengdao.com/character/1747391111775.jpg",
                    androidAvatarBack = "https://zmdcharactercdn-new.zhumengdao.com/character/1747391111775.jpg",
                    prologueAudio = null,
                    audioDuration = 0L,
                    chatUrl = "ywdreamer://nativepage/virtualCharacter/chat?characterId=4247234696&imSource=18&imFrom=6",
                    interactTestUrl = "ywdreamer://nativepage/home/invitedCharacter?characterId=4247234696&imSource=13",
                    chatQurl = "ywdreamer://nativepage/virtualCharacter/chat?characterId=4247234696&imSource=18&imFrom=6",
                    inviteQurl = "ywdreamer://nativepage/home/invitedCharacter?characterId=4247234696&imSource=13&characterType=0",
                    inviteGuide = false,
                    type = 0,
                    tag3d = null,
                    videoUrl = null,
                    firstFrameUrl = null,
                    touchQurl = null
                ),
                RecommendCharacter(
                    characterId = 6737979434L,
                    creatorGuid = 0L,
                    avatar = "https://zmdcharactercdn.zhumengdao.com/2833403E075A58062AACDE1A5B21783B.jpg",
                    name = "zyk2232,白色花",
                    gender = null,
                    chatUserCount = null,
                    identity = "公主",
                    otherSetting = "把恨意都发泄在你身上，却在你熬夜陪他退烧时偷攥你衣角。",
                    qurl = null,
                    prologue = null,
                    tagList = listOf(
                        TagInfo("扭曲恨意"),
                        TagInfo("隐秘关心")
                    ),
                    isFollow = null,
                    avatarBack = "https://zmdcharactercdn-new.zhumengdao.com/character/1747391131594.jpg",
                    androidAvatarBack = "https://zmdcharactercdn-new.zhumengdao.com/character/1747391131594.jpg",
                    prologueAudio = null,
                    audioDuration = 0L,
                    chatUrl = "ywdreamer://nativepage/virtualCharacter/chat?characterId=6737979434&imSource=18&imFrom=6",
                    interactTestUrl = "ywdreamer://nativepage/home/invitedCharacter?characterId=6737979434&imSource=13",
                    chatQurl = "ywdreamer://nativepage/virtualCharacter/chat?characterId=6737979434&imSource=18&imFrom=6",
                    inviteQurl = "ywdreamer://nativepage/home/invitedCharacter?characterId=6737979434&imSource=13&characterType=0",
                    inviteGuide = false,
                    type = 0,
                    tag3d = null,
                    videoUrl = null,
                    firstFrameUrl = null,
                    touchQurl = null
                )
            )
            
            return RecommendCharacterList(characters)
        }
    }
}