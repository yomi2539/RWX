package io.github.rwx.i18n

import java.util.Locale

// Auto-generated file - Do not modify
object I18n {
    const val defaultLocaleTag: String = "en"

    @Volatile
    var currentLocale: Locale = Locale.getDefault()
        private set

    val supportedLocaleTags: Set<String> = linkedSetOf(
        "en",
        "zh-CN",
    )

    val supportedLocales: List<Locale> = supportedLocaleTags.map(Locale::forLanguageTag)

    fun setLocale(locale: Locale) {
        currentLocale = locale
    }

    private fun entry(
        key: String,
        translations: Map<String, String>,
    ): I18nText = I18nText(
        key = key,
        translations = translations,
        localeProvider = { currentLocale },
        fallbackLocaleTag = defaultLocaleTag,
    )

    object battleroom {
        val addAI = entry(
            key = "battleroom.addAI",
            translations = linkedMapOf(
                "en" to "Add AI",
                "zh-CN" to "添加AI",
            ),
        )

        object admin {
            val addAiFailed = entry(
                key = "battleroom.admin.addAiFailed",
                translations = linkedMapOf(
                    "en" to "Unable to add AI: {0}",
                    "zh-CN" to "无法添加AI：{0}",
                ),
            )

            val addAiRequires = entry(
                key = "battleroom.admin.addAiRequires",
                translations = linkedMapOf(
                    "en" to "Add AI requires a hosted RW room",
                    "zh-CN" to "添加AI需要已创建的RW房间",
                ),
            )

            val applyOptionsFailed = entry(
                key = "battleroom.admin.applyOptionsFailed",
                translations = linkedMapOf(
                    "en" to "Unable to apply options: {0}",
                    "zh-CN" to "无法应用选项：{0}",
                ),
            )

            val applyPlayerFailed = entry(
                key = "battleroom.admin.applyPlayerFailed",
                translations = linkedMapOf(
                    "en" to "Unable to apply player config: {0}",
                    "zh-CN" to "无法应用玩家设置：{0}",
                ),
            )

            val chatFailed = entry(
                key = "battleroom.admin.chatFailed",
                translations = linkedMapOf(
                    "en" to "Unable to send chat: {0}",
                    "zh-CN" to "无法发送聊天：{0}",
                ),
            )

            val chatRequires = entry(
                key = "battleroom.admin.chatRequires",
                translations = linkedMapOf(
                    "en" to "Chat requires a hosted RW room",
                    "zh-CN" to "聊天需要已创建的RW房间",
                ),
            )

            val kick = entry(
                key = "battleroom.admin.kick",
                translations = linkedMapOf(
                    "en" to "Kick",
                    "zh-CN" to "踢出",
                ),
            )

            val kickFailed = entry(
                key = "battleroom.admin.kickFailed",
                translations = linkedMapOf(
                    "en" to "Unable to kick player: {0}",
                    "zh-CN" to "无法踢出玩家：{0}",
                ),
            )

            val optionsHint = entry(
                key = "battleroom.admin.optionsHint",
                translations = linkedMapOf(
                    "en" to "Configure this battle room before starting.",
                    "zh-CN" to "在开始前配置此战役室。",
                ),
            )

            val optionsRequires = entry(
                key = "battleroom.admin.optionsRequires",
                translations = linkedMapOf(
                    "en" to "Game options require a hosted RW room",
                    "zh-CN" to "游戏选项需要已创建的RW房间",
                ),
            )

            val playerHint = entry(
                key = "battleroom.admin.playerHint",
                translations = linkedMapOf(
                    "en" to "Set spawn point and ally team.",
                    "zh-CN" to "设置出生点和盟友队伍。",
                ),
            )

            val playerRequires = entry(
                key = "battleroom.admin.playerRequires",
                translations = linkedMapOf(
                    "en" to "Player config requires a hosted RW room",
                    "zh-CN" to "玩家设置需要已创建的RW房间",
                ),
            )

            val playerTitle = entry(
                key = "battleroom.admin.playerTitle",
                translations = linkedMapOf(
                    "en" to "Player Config",
                    "zh-CN" to "玩家设置",
                ),
            )

            val removeAi = entry(
                key = "battleroom.admin.removeAi",
                translations = linkedMapOf(
                    "en" to "Remove AI",
                    "zh-CN" to "移除AI",
                ),
            )

            val setTeamsFailed = entry(
                key = "battleroom.admin.setTeamsFailed",
                translations = linkedMapOf(
                    "en" to "Unable to set teams: {0}",
                    "zh-CN" to "无法设置队伍：{0}",
                ),
            )

            val setTeamsRequires = entry(
                key = "battleroom.admin.setTeamsRequires",
                translations = linkedMapOf(
                    "en" to "Set Teams requires a hosted RW room",
                    "zh-CN" to "设置队伍需要已创建的RW房间",
                ),
            )

        }

        val changeTeam = entry(
            key = "battleroom.changeTeam",
            translations = linkedMapOf(
                "en" to "Change Team",
                "zh-CN" to "更改队伍",
            ),
        )

        val chat = entry(
            key = "battleroom.chat",
            translations = linkedMapOf(
                "en" to "Chat",
                "zh-CN" to "聊天",
            ),
        )

        val close = entry(
            key = "battleroom.close",
            translations = linkedMapOf(
                "en" to "Close",
                "zh-CN" to "关闭",
            ),
        )

        val emptyChat = entry(
            key = "battleroom.emptyChat",
            translations = linkedMapOf(
                "en" to "No messages yet",
                "zh-CN" to "暂无消息",
            ),
        )

        object flag {
            val mods = entry(
                key = "battleroom.flag.mods",
                translations = linkedMapOf(
                    "en" to "Mods enabled",
                    "zh-CN" to "已启用模组",
                ),
            )

            val password = entry(
                key = "battleroom.flag.password",
                translations = linkedMapOf(
                    "en" to "Password required",
                    "zh-CN" to "需要密码",
                ),
            )

        }

        object heading {
            val name = entry(
                key = "battleroom.heading.name",
                translations = linkedMapOf(
                    "en" to "Name",
                    "zh-CN" to "名称",
                ),
            )

            val ping = entry(
                key = "battleroom.heading.ping",
                translations = linkedMapOf(
                    "en" to "Ping",
                    "zh-CN" to "延迟",
                ),
            )

            val spawn = entry(
                key = "battleroom.heading.spawn",
                translations = linkedMapOf(
                    "en" to "Spawn",
                    "zh-CN" to "出生点",
                ),
            )

            val team = entry(
                key = "battleroom.heading.team",
                translations = linkedMapOf(
                    "en" to "Team",
                    "zh-CN" to "队伍",
                ),
            )

        }

        val host = entry(
            key = "battleroom.host",
            translations = linkedMapOf(
                "en" to "Host",
                "zh-CN" to "房主",
            ),
        )

        val noPlayers = entry(
            key = "battleroom.noPlayers",
            translations = linkedMapOf(
                "en" to "No players",
                "zh-CN" to "暂无玩家",
            ),
        )

        object options : I18nNode(
            entry(
                key = "battleroom.options",
                translations = linkedMapOf(
                    "en" to "Game Options",
                    "zh-CN" to "游戏选项",
                ),
            )
        ) {
            object ai {
                val easy = entry(
                    key = "battleroom.options.ai.easy",
                    translations = linkedMapOf(
                        "en" to "Easy",
                        "zh-CN" to "简单",
                    ),
                )

                val hard = entry(
                    key = "battleroom.options.ai.hard",
                    translations = linkedMapOf(
                        "en" to "Hard",
                        "zh-CN" to "困难",
                    ),
                )

                val impossible = entry(
                    key = "battleroom.options.ai.impossible",
                    translations = linkedMapOf(
                        "en" to "Impossible",
                        "zh-CN" to "不可能",
                    ),
                )

                val medium = entry(
                    key = "battleroom.options.ai.medium",
                    translations = linkedMapOf(
                        "en" to "Medium",
                        "zh-CN" to "中等",
                    ),
                )

                val veryEasy = entry(
                    key = "battleroom.options.ai.veryEasy",
                    translations = linkedMapOf(
                        "en" to "Very Easy",
                        "zh-CN" to "非常简单",
                    ),
                )

                val veryHard = entry(
                    key = "battleroom.options.ai.veryHard",
                    translations = linkedMapOf(
                        "en" to "Very Hard",
                        "zh-CN" to "非常困难",
                    ),
                )

            }

            val aiDifficulty = entry(
                key = "battleroom.options.aiDifficulty",
                translations = linkedMapOf(
                    "en" to "AI difficulty",
                    "zh-CN" to "AI难度",
                ),
            )

            val auto = entry(
                key = "battleroom.options.auto",
                translations = linkedMapOf(
                    "en" to "Auto",
                    "zh-CN" to "自动",
                ),
            )

            object compat {
                val originalBlocked = entry(
                    key = "battleroom.options.compat.originalBlocked",
                    translations = linkedMapOf(
                        "en" to "Original multiplayer blocked",
                        "zh-CN" to "原版多人游戏已禁用",
                    ),
                )

                val p2pEnabled = entry(
                    key = "battleroom.options.compat.p2pEnabled",
                    translations = linkedMapOf(
                        "en" to "RWX P2P enabled",
                        "zh-CN" to "RWX P2P已启用",
                    ),
                )

                val singlePlayer = entry(
                    key = "battleroom.options.compat.singlePlayer",
                    translations = linkedMapOf(
                        "en" to "Single-player",
                        "zh-CN" to "单人游戏",
                    ),
                )

            }

            object detail {
                val fixedAlly = entry(
                    key = "battleroom.options.detail.fixedAlly",
                    translations = linkedMapOf(
                        "en" to "Fixed ally teams",
                        "zh-CN" to "固定盟友队伍",
                    ),
                )

                val fog = entry(
                    key = "battleroom.options.detail.fog",
                    translations = linkedMapOf(
                        "en" to "Fog: {0}",
                        "zh-CN" to "雾效：{0}",
                    ),
                )

                val income = entry(
                    key = "battleroom.options.detail.income",
                    translations = linkedMapOf(
                        "en" to "{0}X income",
                        "zh-CN" to "{0}倍收入",
                    ),
                )

                val noNukes = entry(
                    key = "battleroom.options.detail.noNukes",
                    translations = linkedMapOf(
                        "en" to "No nukes",
                        "zh-CN" to "禁用核武器",
                    ),
                )

                val requiredMods = entry(
                    key = "battleroom.options.detail.requiredMods",
                    translations = linkedMapOf(
                        "en" to "Required mods: {0}",
                        "zh-CN" to "需要模组：{0}",
                    ),
                )

                val roomLocked = entry(
                    key = "battleroom.options.detail.roomLocked",
                    translations = linkedMapOf(
                        "en" to "Room locked",
                        "zh-CN" to "房间已锁定",
                    ),
                )

                val sharedControl = entry(
                    key = "battleroom.options.detail.sharedControl",
                    translations = linkedMapOf(
                        "en" to "Shared control: On",
                        "zh-CN" to "共享控制：开",
                    ),
                )

                val startingCredits = entry(
                    key = "battleroom.options.detail.startingCredits",
                    translations = linkedMapOf(
                        "en" to "Starting Credits: {0}",
                        "zh-CN" to "初始资金：{0}",
                    ),
                )

                val startingUnits = entry(
                    key = "battleroom.options.detail.startingUnits",
                    translations = linkedMapOf(
                        "en" to "Starting Units: {0}",
                        "zh-CN" to "初始单位：{0}",
                    ),
                )

            }

            object fog : I18nNode(
                entry(
                    key = "battleroom.options.fog",
                    translations = linkedMapOf(
                        "en" to "Fog",
                        "zh-CN" to "雾效",
                    ),
                )
            ) {
                val basic = entry(
                    key = "battleroom.options.fog.basic",
                    translations = linkedMapOf(
                        "en" to "Basic",
                        "zh-CN" to "基础",
                    ),
                )

                val los = entry(
                    key = "battleroom.options.fog.los",
                    translations = linkedMapOf(
                        "en" to "Line of Sight",
                        "zh-CN" to "视线",
                    ),
                )

                val none = entry(
                    key = "battleroom.options.fog.none",
                    translations = linkedMapOf(
                        "en" to "No fog",
                        "zh-CN" to "无",
                    ),
                )

            }

            val income = entry(
                key = "battleroom.options.income",
                translations = linkedMapOf(
                    "en" to "Income",
                    "zh-CN" to "收入",
                ),
            )

            val maxPlayers = entry(
                key = "battleroom.options.maxPlayers",
                translations = linkedMapOf(
                    "en" to "Max players",
                    "zh-CN" to "最大玩家数",
                ),
            )

            val overrideAiDifficulty = entry(
                key = "battleroom.options.overrideAiDifficulty",
                translations = linkedMapOf(
                    "en" to "AI difficulty override",
                    "zh-CN" to "AI难度覆盖",
                ),
            )

            val overrideStartingUnits = entry(
                key = "battleroom.options.overrideStartingUnits",
                translations = linkedMapOf(
                    "en" to "Starting units override",
                    "zh-CN" to "初始单位覆盖",
                ),
            )

            val revealedMap = entry(
                key = "battleroom.options.revealedMap",
                translations = linkedMapOf(
                    "en" to "Revealed Map",
                    "zh-CN" to "显示全图",
                ),
            )

            val serverFallback = entry(
                key = "battleroom.options.serverFallback",
                translations = linkedMapOf(
                    "en" to "server",
                    "zh-CN" to "服务器",
                ),
            )

            val spawnPoint = entry(
                key = "battleroom.options.spawnPoint",
                translations = linkedMapOf(
                    "en" to "Spawn point",
                    "zh-CN" to "出生点",
                ),
            )

            val startingCredits = entry(
                key = "battleroom.options.startingCredits",
                translations = linkedMapOf(
                    "en" to "Starting credits",
                    "zh-CN" to "初始资金",
                ),
            )

            val startingUnits = entry(
                key = "battleroom.options.startingUnits",
                translations = linkedMapOf(
                    "en" to "Starting units",
                    "zh-CN" to "初始单位",
                ),
            )

            object team {
                val allVs2 = entry(
                    key = "battleroom.options.team.allVs2",
                    translations = linkedMapOf(
                        "en" to "All vs 2 (survival)",
                        "zh-CN" to "全体对2（生存）",
                    ),
                )

                val allVsAI = entry(
                    key = "battleroom.options.team.allVsAI",
                    translations = linkedMapOf(
                        "en" to "All vs AI",
                        "zh-CN" to "全体对AI",
                    ),
                )

                val ffa = entry(
                    key = "battleroom.options.team.ffa",
                    translations = linkedMapOf(
                        "en" to "FFA",
                        "zh-CN" to "自由混战",
                    ),
                )

                val noChange = entry(
                    key = "battleroom.options.team.noChange",
                    translations = linkedMapOf(
                        "en" to "No change",
                        "zh-CN" to "不变",
                    ),
                )

                val random = entry(
                    key = "battleroom.options.team.random",
                    translations = linkedMapOf(
                        "en" to "Random Team",
                        "zh-CN" to "随机队伍",
                    ),
                )

                val sides2 = entry(
                    key = "battleroom.options.team.sides2",
                    translations = linkedMapOf(
                        "en" to "2 Sides",
                        "zh-CN" to "2方",
                    ),
                )

                val sides3 = entry(
                    key = "battleroom.options.team.sides3",
                    translations = linkedMapOf(
                        "en" to "3 Sides",
                        "zh-CN" to "3方",
                    ),
                )

                val spectators = entry(
                    key = "battleroom.options.team.spectators",
                    translations = linkedMapOf(
                        "en" to "Spectators",
                        "zh-CN" to "旁观",
                    ),
                )

            }

            val teamLayout = entry(
                key = "battleroom.options.teamLayout",
                translations = linkedMapOf(
                    "en" to "Team layout",
                    "zh-CN" to "队伍布局",
                ),
            )

            object toggle {
                val allowSpectators = entry(
                    key = "battleroom.options.toggle.allowSpectators",
                    translations = linkedMapOf(
                        "en" to "Allow spectators",
                        "zh-CN" to "允许旁观",
                    ),
                )

                val fixedAlly = entry(
                    key = "battleroom.options.toggle.fixedAlly",
                    translations = linkedMapOf(
                        "en" to "Fixed ally teams",
                        "zh-CN" to "固定盟友队伍",
                    ),
                )

                val lockRoom = entry(
                    key = "battleroom.options.toggle.lockRoom",
                    translations = linkedMapOf(
                        "en" to "Lock room (no new players)",
                        "zh-CN" to "锁定房间（禁止新玩家）",
                    ),
                )

                val noNukes = entry(
                    key = "battleroom.options.toggle.noNukes",
                    translations = linkedMapOf(
                        "en" to "No nukes",
                        "zh-CN" to "禁用核武器",
                    ),
                )

                val sharedControl = entry(
                    key = "battleroom.options.toggle.sharedControl",
                    translations = linkedMapOf(
                        "en" to "Shared control",
                        "zh-CN" to "共享控制",
                    ),
                )

                val teamLock = entry(
                    key = "battleroom.options.toggle.teamLock",
                    translations = linkedMapOf(
                        "en" to "Team lock",
                        "zh-CN" to "锁定队伍",
                    ),
                )

            }

            object units {
                val custom = entry(
                    key = "battleroom.options.units.custom",
                    translations = linkedMapOf(
                        "en" to "Custom",
                        "zh-CN" to "自定义",
                    ),
                )

                val engineers3 = entry(
                    key = "battleroom.options.units.engineers3",
                    translations = linkedMapOf(
                        "en" to "3 Engineers",
                        "zh-CN" to "3个工程师",
                    ),
                )

                val engineersNoCC = entry(
                    key = "battleroom.options.units.engineersNoCC",
                    translations = linkedMapOf(
                        "en" to "3 Engineers (No Command Center)",
                        "zh-CN" to "3个工程师（无指挥中心）",
                    ),
                )

                val normal = entry(
                    key = "battleroom.options.units.normal",
                    translations = linkedMapOf(
                        "en" to "Normal (1 builder)",
                        "zh-CN" to "标准（1个建造者）",
                    ),
                )

                val smallArmy = entry(
                    key = "battleroom.options.units.smallArmy",
                    translations = linkedMapOf(
                        "en" to "Small Army",
                        "zh-CN" to "小型军队",
                    ),
                )

                val spider = entry(
                    key = "battleroom.options.units.spider",
                    translations = linkedMapOf(
                        "en" to "Experimental Spider",
                        "zh-CN" to "实验型蜘蛛",
                    ),
                )

            }

        }

        val players = entry(
            key = "battleroom.players",
            translations = linkedMapOf(
                "en" to "Players",
                "zh-CN" to "玩家",
            ),
        )

        val ready = entry(
            key = "battleroom.ready",
            translations = linkedMapOf(
                "en" to "Ready",
                "zh-CN" to "已准备",
            ),
        )

        val send = entry(
            key = "battleroom.send",
            translations = linkedMapOf(
                "en" to "Send",
                "zh-CN" to "发送",
            ),
        )

        val sendMessage = entry(
            key = "battleroom.sendMessage",
            translations = linkedMapOf(
                "en" to "Send a message",
                "zh-CN" to "发送消息",
            ),
        )

        val spectator = entry(
            key = "battleroom.spectator",
            translations = linkedMapOf(
                "en" to "Spec",
                "zh-CN" to "旁观",
            ),
        )

        val start = entry(
            key = "battleroom.start",
            translations = linkedMapOf(
                "en" to "Start",
                "zh-CN" to "开始游戏",
            ),
        )

        object status {
            val ai = entry(
                key = "battleroom.status.ai",
                translations = linkedMapOf(
                    "en" to "AI",
                    "zh-CN" to "AI",
                ),
            )

            val notReady = entry(
                key = "battleroom.status.notReady",
                translations = linkedMapOf(
                    "en" to "Not ready",
                    "zh-CN" to "未准备",
                ),
            )

            val ready = entry(
                key = "battleroom.status.ready",
                translations = linkedMapOf(
                    "en" to "Ready",
                    "zh-CN" to "已准备",
                ),
            )

            val spectator = entry(
                key = "battleroom.status.spectator",
                translations = linkedMapOf(
                    "en" to "Spectator",
                    "zh-CN" to "旁观",
                ),
            )

            val you = entry(
                key = "battleroom.status.you",
                translations = linkedMapOf(
                    "en" to "You",
                    "zh-CN" to "你",
                ),
            )

        }

        val title = entry(
            key = "battleroom.title",
            translations = linkedMapOf(
                "en" to "Battleroom",
                "zh-CN" to "战役室",
            ),
        )

        val unableToStartGame = entry(
            key = "battleroom.unableToStartGame",
            translations = linkedMapOf(
                "en" to "Unable to start game: {0}",
                "zh-CN" to "无法开始游戏：{0}",
            ),
        )

        val unavailable = entry(
            key = "battleroom.unavailable",
            translations = linkedMapOf(
                "en" to "Room unavailable",
                "zh-CN" to "房间不可用",
            ),
        )

        val waiting = entry(
            key = "battleroom.waiting",
            translations = linkedMapOf(
                "en" to "Waiting",
                "zh-CN" to "等待中",
            ),
        )

    }

    object common {
        val actionUnavailable = entry(
            key = "common.actionUnavailable",
            translations = linkedMapOf(
                "en" to "Action unavailable",
                "zh-CN" to "操作不可用",
            ),
        )

        val back = entry(
            key = "common.back",
            translations = linkedMapOf(
                "en" to "Back",
                "zh-CN" to "返回",
            ),
        )

        val cancel = entry(
            key = "common.cancel",
            translations = linkedMapOf(
                "en" to "Cancel",
                "zh-CN" to "取消",
            ),
        )

        val close = entry(
            key = "common.close",
            translations = linkedMapOf(
                "en" to "Close",
                "zh-CN" to "关闭",
            ),
        )

        val join = entry(
            key = "common.join",
            translations = linkedMapOf(
                "en" to "Join",
                "zh-CN" to "加入",
            ),
        )

        val no = entry(
            key = "common.no",
            translations = linkedMapOf(
                "en" to "No",
                "zh-CN" to "否",
            ),
        )

        val noPreview = entry(
            key = "common.noPreview",
            translations = linkedMapOf(
                "en" to "No preview",
                "zh-CN" to "无预览",
            ),
        )

        val ok = entry(
            key = "common.ok",
            translations = linkedMapOf(
                "en" to "OK",
                "zh-CN" to "确定",
            ),
        )

        val required = entry(
            key = "common.required",
            translations = linkedMapOf(
                "en" to "Required",
                "zh-CN" to "需要",
            ),
        )

        val retry = entry(
            key = "common.retry",
            translations = linkedMapOf(
                "en" to "Retry",
                "zh-CN" to "重试",
            ),
        )

        val save = entry(
            key = "common.save",
            translations = linkedMapOf(
                "en" to "Save",
                "zh-CN" to "保存",
            ),
        )

        val unknown = entry(
            key = "common.unknown",
            translations = linkedMapOf(
                "en" to "Unknown",
                "zh-CN" to "未知",
            ),
        )

    }

    object gamestart {
        val message = entry(
            key = "gamestart.message",
            translations = linkedMapOf(
                "en" to "A game is already running. Start a new game or continue the current game?",
                "zh-CN" to "已有游戏正在运行。开始新游戏或继续当前游戏？",
            ),
        )

        val startNew = entry(
            key = "gamestart.startNew",
            translations = linkedMapOf(
                "en" to "Start New",
                "zh-CN" to "开始新游戏",
            ),
        )

        val title = entry(
            key = "gamestart.title",
            translations = linkedMapOf(
                "en" to "Game in progress",
                "zh-CN" to "游戏正在进行",
            ),
        )

    }

    object ingame {
        object chat {
            val empty = entry(
                key = "ingame.chat.empty",
                translations = linkedMapOf(
                    "en" to "No chat messages yet.",
                    "zh-CN" to "暂无聊天消息。",
                ),
            )

            val hint = entry(
                key = "ingame.chat.hint",
                translations = linkedMapOf(
                    "en" to "Message",
                    "zh-CN" to "消息",
                ),
            )

            val teamChat = entry(
                key = "ingame.chat.teamChat",
                translations = linkedMapOf(
                    "en" to "Team Chat",
                    "zh-CN" to "队伍聊天",
                ),
            )

        }

        object exit {
            val message = entry(
                key = "ingame.exit.message",
                translations = linkedMapOf(
                    "en" to "Are you sure you want to exit this game?",
                    "zh-CN" to "确定要退出本局游戏吗？",
                ),
            )

        }

        object export {
            val hint = entry(
                key = "ingame.export.hint",
                translations = linkedMapOf(
                    "en" to "Map name",
                    "zh-CN" to "地图名",
                ),
            )

            val message = entry(
                key = "ingame.export.message",
                translations = linkedMapOf(
                    "en" to "Enter a name to export the map as.",
                    "zh-CN" to "输入导出地图名称。",
                ),
            )

            val title = entry(
                key = "ingame.export.title",
                translations = linkedMapOf(
                    "en" to "Export Map",
                    "zh-CN" to "导出地图",
                ),
            )

        }

        object map {
            val current = entry(
                key = "ingame.map.current",
                translations = linkedMapOf(
                    "en" to "Current",
                    "zh-CN" to "当前",
                ),
            )

            val message = entry(
                key = "ingame.map.message",
                translations = linkedMapOf(
                    "en" to "Select a linked map.",
                    "zh-CN" to "选择要切换的关联地图。",
                ),
            )

            val missing = entry(
                key = "ingame.map.missing",
                translations = linkedMapOf(
                    "en" to "Linked map missing: {0}",
                    "zh-CN" to "关联地图缺失：{0}",
                ),
            )

            val noActive = entry(
                key = "ingame.map.noActive",
                translations = linkedMapOf(
                    "en" to "No active map",
                    "zh-CN" to "当前没有地图",
                ),
            )

            val noLinked = entry(
                key = "ingame.map.noLinked",
                translations = linkedMapOf(
                    "en" to "No linked maps",
                    "zh-CN" to "没有关联地图",
                ),
            )

            val notFound = entry(
                key = "ingame.map.notFound",
                translations = linkedMapOf(
                    "en" to "Map not found: {0}",
                    "zh-CN" to "未找到地图：{0}",
                ),
            )

            val select = entry(
                key = "ingame.map.select",
                translations = linkedMapOf(
                    "en" to "Select",
                    "zh-CN" to "选择",
                ),
            )

            val sim = entry(
                key = "ingame.map.sim",
                translations = linkedMapOf(
                    "en" to "Sim: {0}",
                    "zh-CN" to "模拟：{0}",
                ),
            )

            val title = entry(
                key = "ingame.map.title",
                translations = linkedMapOf(
                    "en" to "RWX Maps",
                    "zh-CN" to "RWX地图",
                ),
            )

            val unavailable = entry(
                key = "ingame.map.unavailable",
                translations = linkedMapOf(
                    "en" to "This map is simulated by {0}.",
                    "zh-CN" to "此地图正由{0}模拟。",
                ),
            )

        }

        object players {
            val empty = entry(
                key = "ingame.players.empty",
                translations = linkedMapOf(
                    "en" to "No multiplayer players found.",
                    "zh-CN" to "未找到多人玩家。",
                ),
            )

        }

        object save {
            val hint = entry(
                key = "ingame.save.hint",
                translations = linkedMapOf(
                    "en" to "Save name",
                    "zh-CN" to "存档名",
                ),
            )

            val message = entry(
                key = "ingame.save.message",
                translations = linkedMapOf(
                    "en" to "Enter a name to save the game under.",
                    "zh-CN" to "输入存档名称。",
                ),
            )

            val title = entry(
                key = "ingame.save.title",
                translations = linkedMapOf(
                    "en" to "Save Game",
                    "zh-CN" to "保存游戏",
                ),
            )

        }

        object toast {
            val portalSent = entry(
                key = "ingame.toast.portalSent",
                translations = linkedMapOf(
                    "en" to "RWX portal transfer sent",
                    "zh-CN" to "RWX传送门传送已发送",
                ),
            )

            val transferred = entry(
                key = "ingame.toast.transferred",
                translations = linkedMapOf(
                    "en" to "Unit transferred to {0}",
                    "zh-CN" to "单位已传送至{0}",
                ),
            )

        }

    }

    object levelselect {
        object compatibility {
            val p2p = entry(
                key = "levelselect.compatibility.p2p",
                translations = linkedMapOf(
                    "en" to "RWX P2P",
                    "zh-CN" to "RWX联机",
                ),
            )

            val singlePlayer = entry(
                key = "levelselect.compatibility.singlePlayer",
                translations = linkedMapOf(
                    "en" to "Single-player",
                    "zh-CN" to "单人",
                ),
            )

        }

        val emptyNoMatch = entry(
            key = "levelselect.emptyNoMatch",
            translations = linkedMapOf(
                "en" to "No maps match the current filter",
                "zh-CN" to "没有符合筛选条件的地图",
            ),
        )

        val emptySavedGames = entry(
            key = "levelselect.emptySavedGames",
            translations = linkedMapOf(
                "en" to "No saved games found",
                "zh-CN" to "未找到存档",
            ),
        )

        object filter {
            val all = entry(
                key = "levelselect.filter.all",
                translations = linkedMapOf(
                    "en" to "All maps",
                    "zh-CN" to "全部地图",
                ),
            )

            val mapType = entry(
                key = "levelselect.filter.mapType",
                translations = linkedMapOf(
                    "en" to "Map Type",
                    "zh-CN" to "地图类型",
                ),
            )

            val rwxModes = entry(
                key = "levelselect.filter.rwxModes",
                translations = linkedMapOf(
                    "en" to "RWX modes",
                    "zh-CN" to "RWX模式",
                ),
            )

        }

        val loadError = entry(
            key = "levelselect.loadError",
            translations = linkedMapOf(
                "en" to "Unable to load maps",
                "zh-CN" to "无法加载地图",
            ),
        )

        object mode {
            val areaControl = entry(
                key = "levelselect.mode.areaControl",
                translations = linkedMapOf(
                    "en" to "Area Control",
                    "zh-CN" to "区域控制",
                ),
            )

            val mapLinks = entry(
                key = "levelselect.mode.mapLinks",
                translations = linkedMapOf(
                    "en" to "Linked Maps",
                    "zh-CN" to "关联地图",
                ),
            )

        }

        val players = entry(
            key = "levelselect.players",
            translations = linkedMapOf(
                "en" to "Players",
                "zh-CN" to "玩家数",
            ),
        )

        val scenario = entry(
            key = "levelselect.scenario",
            translations = linkedMapOf(
                "en" to "Scenario",
                "zh-CN" to "战役关",
            ),
        )

        val searchHint = entry(
            key = "levelselect.searchHint",
            translations = linkedMapOf(
                "en" to "Map name",
                "zh-CN" to "地图名",
            ),
        )

        object sort {
            val default = entry(
                key = "levelselect.sort.default",
                translations = linkedMapOf(
                    "en" to "Default",
                    "zh-CN" to "默认",
                ),
            )

            val name = entry(
                key = "levelselect.sort.name",
                translations = linkedMapOf(
                    "en" to "Name",
                    "zh-CN" to "名称",
                ),
            )

            val players = entry(
                key = "levelselect.sort.players",
                translations = linkedMapOf(
                    "en" to "Players",
                    "zh-CN" to "玩家数",
                ),
            )

            val title = entry(
                key = "levelselect.sort.title",
                translations = linkedMapOf(
                    "en" to "Sort",
                    "zh-CN" to "排序",
                ),
            )

        }

    }

    object loading {
        val loading = entry(
            key = "loading.loading",
            translations = linkedMapOf(
                "en" to "Loading...",
                "zh-CN" to "正在加载...",
            ),
        )

    }

    object mainmenu {
        object about : I18nNode(
            entry(
                key = "mainmenu.about",
                translations = linkedMapOf(
                    "en" to "About",
                    "zh-CN" to "关于",
                ),
            )
        ) {
            val discord = entry(
                key = "mainmenu.about.discord",
                translations = linkedMapOf(
                    "en" to "Discord",
                    "zh-CN" to "Discord",
                ),
            )

            val github = entry(
                key = "mainmenu.about.github",
                translations = linkedMapOf(
                    "en" to "GitHub",
                    "zh-CN" to "GitHub",
                ),
            )

            val license = entry(
                key = "mainmenu.about.license",
                translations = linkedMapOf(
                    "en" to "License",
                    "zh-CN" to "许可证",
                ),
            )

            val licenseValue = entry(
                key = "mainmenu.about.licenseValue",
                translations = linkedMapOf(
                    "en" to "GNU AGPL-3.0",
                    "zh-CN" to "GNU AGPL-3.0",
                ),
            )

            val message = entry(
                key = "mainmenu.about.message",
                translations = linkedMapOf(
                    "en" to "Rusted Warfare Extension\nRebuilding and extending Rusted Warfare as an open-source cross-platform RTS game.",
                    "zh-CN" to "Rusted Warfare Extension\n重建和扩展 Rusted Warfare 的开源跨平台 RTS 游戏。",
                ),
            )

            val openLinkFailed = entry(
                key = "mainmenu.about.openLinkFailed",
                translations = linkedMapOf(
                    "en" to "Unable to open link: {0}",
                    "zh-CN" to "无法打开链接：{0}",
                ),
            )

            val qq = entry(
                key = "mainmenu.about.qq",
                translations = linkedMapOf(
                    "en" to "QQ",
                    "zh-CN" to "QQ",
                ),
            )

            val title = entry(
                key = "mainmenu.about.title",
                translations = linkedMapOf(
                    "en" to "About RWX",
                    "zh-CN" to "关于 RWX",
                ),
            )

            val version = entry(
                key = "mainmenu.about.version",
                translations = linkedMapOf(
                    "en" to "Version",
                    "zh-CN" to "版本",
                ),
            )

            val versionCheckValue = entry(
                key = "mainmenu.about.versionCheckValue",
                translations = linkedMapOf(
                    "en" to "{0}({1}) CompatibleCore:{2} - click to check updates",
                    "zh-CN" to "{0}({1}) 兼容内核:{2} - 点击检查更新",
                ),
            )

        }

        val `continue` = entry(
            key = "mainmenu.continue",
            translations = linkedMapOf(
                "en" to "Continue",
                "zh-CN" to "继续",
            ),
        )

        val exit = entry(
            key = "mainmenu.exit",
            translations = linkedMapOf(
                "en" to "Exit",
                "zh-CN" to "退出",
            ),
        )

        val help = entry(
            key = "mainmenu.help",
            translations = linkedMapOf(
                "en" to "Help",
                "zh-CN" to "帮助",
            ),
        )

        val mods = entry(
            key = "mainmenu.mods",
            translations = linkedMapOf(
                "en" to "Mods",
                "zh-CN" to "模组",
            ),
        )

        val multiplayer = entry(
            key = "mainmenu.multiplayer",
            translations = linkedMapOf(
                "en" to "Multiplayer",
                "zh-CN" to "多人游戏",
            ),
        )

        val p2pMultiplayer = entry(
            key = "mainmenu.p2pMultiplayer",
            translations = linkedMapOf(
                "en" to "P2P Multiplayer",
                "zh-CN" to "P2P联机",
            ),
        )

        val resourceBrowser = entry(
            key = "mainmenu.resourceBrowser",
            translations = linkedMapOf(
                "en" to "Resource Browser",
                "zh-CN" to "资源浏览器",
            ),
        )

        val settings = entry(
            key = "mainmenu.settings",
            translations = linkedMapOf(
                "en" to "Settings",
                "zh-CN" to "设置",
            ),
        )

        val singlePlayer = entry(
            key = "mainmenu.singlePlayer",
            translations = linkedMapOf(
                "en" to "Single Player",
                "zh-CN" to "单人游戏",
            ),
        )

        val watchReplay = entry(
            key = "mainmenu.watchReplay",
            translations = linkedMapOf(
                "en" to "Watch Replay",
                "zh-CN" to "观看回放",
            ),
        )

    }

    object mapload {
        val message = entry(
            key = "mapload.message",
            translations = linkedMapOf(
                "en" to "Unable to load the selected map.",
                "zh-CN" to "无法加载所选地图。",
            ),
        )

        val title = entry(
            key = "mapload.title",
            translations = linkedMapOf(
                "en" to "Map Load Error",
                "zh-CN" to "地图加载错误",
            ),
        )

    }

    object modWindow {
        val back = entry(
            key = "modWindow.back",
            translations = linkedMapOf(
                "en" to "Back",
                "zh-CN" to "返回",
            ),
        )

        val emptyMessage = entry(
            key = "modWindow.emptyMessage",
            translations = linkedMapOf(
                "en" to "No mod window is currently open.",
                "zh-CN" to "当前没有打开模组窗口。",
            ),
        )

        val emptyTitle = entry(
            key = "modWindow.emptyTitle",
            translations = linkedMapOf(
                "en" to "Mod Window",
                "zh-CN" to "模组窗口",
            ),
        )

    }

    object modimport {
        val chooseFile = entry(
            key = "modimport.chooseFile",
            translations = linkedMapOf(
                "en" to "Choose file",
                "zh-CN" to "选择文件",
            ),
        )

        val chooseTitle = entry(
            key = "modimport.chooseTitle",
            translations = linkedMapOf(
                "en" to "Choose a mod file or directory",
                "zh-CN" to "选择模组文件或目录",
            ),
        )

        val message = entry(
            key = "modimport.message",
            translations = linkedMapOf(
                "en" to "Enter a mod, asset key, author trust certificate, or license path.",
                "zh-CN" to "输入模组、资产密钥、作者信任证书或许可证路径。",
            ),
        )

        val title = entry(
            key = "modimport.title",
            translations = linkedMapOf(
                "en" to "Import Mod",
                "zh-CN" to "导入模组",
            ),
        )

    }

    object mods {
        val apply = entry(
            key = "mods.apply",
            translations = linkedMapOf(
                "en" to "Apply",
                "zh-CN" to "应用",
            ),
        )

        val back = entry(
            key = "mods.back",
            translations = linkedMapOf(
                "en" to "Back",
                "zh-CN" to "返回",
            ),
        )

        object delete : I18nNode(
            entry(
                key = "mods.delete",
                translations = linkedMapOf(
                    "en" to "Delete",
                    "zh-CN" to "删除",
                ),
            )
        ) {
            val failed = entry(
                key = "mods.delete.failed",
                translations = linkedMapOf(
                    "en" to "Unable to delete mod",
                    "zh-CN" to "无法删除模组",
                ),
            )

        }

        val description = entry(
            key = "mods.description",
            translations = linkedMapOf(
                "en" to "Description",
                "zh-CN" to "描述",
            ),
        )

        val disable = entry(
            key = "mods.disable",
            translations = linkedMapOf(
                "en" to "Disable",
                "zh-CN" to "禁用",
            ),
        )

        val disableAll = entry(
            key = "mods.disableAll",
            translations = linkedMapOf(
                "en" to "Disable All",
                "zh-CN" to "全部禁用",
            ),
        )

        val disabled = entry(
            key = "mods.disabled",
            translations = linkedMapOf(
                "en" to "Disabled",
                "zh-CN" to "未启用",
            ),
        )

        val empty = entry(
            key = "mods.empty",
            translations = linkedMapOf(
                "en" to "No mods installed",
                "zh-CN" to "未安装模组",
            ),
        )

        val enable = entry(
            key = "mods.enable",
            translations = linkedMapOf(
                "en" to "Enable",
                "zh-CN" to "启用",
            ),
        )

        val enabled = entry(
            key = "mods.enabled",
            translations = linkedMapOf(
                "en" to "Enabled",
                "zh-CN" to "已启用",
            ),
        )

        val error = entry(
            key = "mods.error",
            translations = linkedMapOf(
                "en" to "Error",
                "zh-CN" to "错误",
            ),
        )

        val filter = entry(
            key = "mods.filter",
            translations = linkedMapOf(
                "en" to "Filter",
                "zh-CN" to "筛选",
            ),
        )

        val importFile = entry(
            key = "mods.importFile",
            translations = linkedMapOf(
                "en" to "Import",
                "zh-CN" to "导入",
            ),
        )

        val more = entry(
            key = "mods.more",
            translations = linkedMapOf(
                "en" to "More...",
                "zh-CN" to "更多...",
            ),
        )

        object reload : I18nNode(
            entry(
                key = "mods.reload",
                translations = linkedMapOf(
                    "en" to "Reload",
                    "zh-CN" to "重新加载",
                ),
            )
        ) {
            val done = entry(
                key = "mods.reload.done",
                translations = linkedMapOf(
                    "en" to "Mods reloaded",
                    "zh-CN" to "模组已重载",
                ),
            )

            val failed = entry(
                key = "mods.reload.failed",
                translations = linkedMapOf(
                    "en" to "Unable to reload mods: {0}",
                    "zh-CN" to "无法重载模组：{0}",
                ),
            )

            val message = entry(
                key = "mods.reload.message",
                translations = linkedMapOf(
                    "en" to "Loading custom unit data",
                    "zh-CN" to "正在加载自定义单位数据",
                ),
            )

            val title = entry(
                key = "mods.reload.title",
                translations = linkedMapOf(
                    "en" to "Reloading Mods",
                    "zh-CN" to "正在重载模组",
                ),
            )

        }

        val title = entry(
            key = "mods.title",
            translations = linkedMapOf(
                "en" to "Mods",
                "zh-CN" to "模组",
            ),
        )

    }

    object multiplayer {
        val configurePlayerName = entry(
            key = "multiplayer.configurePlayerName",
            translations = linkedMapOf(
                "en" to "Configure Player Name",
                "zh-CN" to "设置玩家名称",
            ),
        )

        val connectingTo = entry(
            key = "multiplayer.connectingTo",
            translations = linkedMapOf(
                "en" to "Connecting to {0}...",
                "zh-CN" to "正在连接到 {0}...",
            ),
        )

        object heading {
            val host = entry(
                key = "multiplayer.heading.host",
                translations = linkedMapOf(
                    "en" to "Host",
                    "zh-CN" to "房主",
                ),
            )

            val map = entry(
                key = "multiplayer.heading.map",
                translations = linkedMapOf(
                    "en" to "Map",
                    "zh-CN" to "地图",
                ),
            )

            val players = entry(
                key = "multiplayer.heading.players",
                translations = linkedMapOf(
                    "en" to "Players",
                    "zh-CN" to "人数",
                ),
            )

            val state = entry(
                key = "multiplayer.heading.state",
                translations = linkedMapOf(
                    "en" to "State",
                    "zh-CN" to "状态",
                ),
            )

            val transport = entry(
                key = "multiplayer.heading.transport",
                translations = linkedMapOf(
                    "en" to "Transport",
                    "zh-CN" to "传输",
                ),
            )

            val version = entry(
                key = "multiplayer.heading.version",
                translations = linkedMapOf(
                    "en" to "Version",
                    "zh-CN" to "版本",
                ),
            )

        }

        val hostFailed = entry(
            key = "multiplayer.hostFailed",
            translations = linkedMapOf(
                "en" to "Unable to host game: {0}",
                "zh-CN" to "无法创建游戏：{0}",
            ),
        )

        val hostGame = entry(
            key = "multiplayer.hostGame",
            translations = linkedMapOf(
                "en" to "Host Game",
                "zh-CN" to "创建游戏",
            ),
        )

        val hostGameInfo = entry(
            key = "multiplayer.hostGameInfo",
            translations = linkedMapOf(
                "en" to "Configure the server before opening the battle room.\nMap: {0}",
                "zh-CN" to "在打开战役室前配置服务器。\n地图：{0}",
            ),
        )

        val hostP2pGameInfo = entry(
            key = "multiplayer.hostP2pGameInfo",
            translations = linkedMapOf(
                "en" to "Configure the P2P room before opening the battle room.\nMap: {0}",
                "zh-CN" to "在打开战役室前配置P2P房间。\n地图：{0}",
            ),
        )

        val hostPassword = entry(
            key = "multiplayer.hostPassword",
            translations = linkedMapOf(
                "en" to "Password",
                "zh-CN" to "密码",
            ),
        )

        val hostPasswordHint = entry(
            key = "multiplayer.hostPasswordHint",
            translations = linkedMapOf(
                "en" to "Optional",
                "zh-CN" to "可选",
            ),
        )

        val hostStartP2p = entry(
            key = "multiplayer.hostStartP2p",
            translations = linkedMapOf(
                "en" to "Host P2P",
                "zh-CN" to "创建P2P房间",
            ),
        )

        val hostStartPrivate = entry(
            key = "multiplayer.hostStartPrivate",
            translations = linkedMapOf(
                "en" to "Host Private",
                "zh-CN" to "私有创建",
            ),
        )

        val hostStartPublic = entry(
            key = "multiplayer.hostStartPublic",
            translations = linkedMapOf(
                "en" to "Host Public",
                "zh-CN" to "公开创建",
            ),
        )

        val hostUseMods = entry(
            key = "multiplayer.hostUseMods",
            translations = linkedMapOf(
                "en" to "Use active mods",
                "zh-CN" to "使用已启用的模组",
            ),
        )

        val joinLastGame = entry(
            key = "multiplayer.joinLastGame",
            translations = linkedMapOf(
                "en" to "Rejoin Last Game",
                "zh-CN" to "重新加入上局",
            ),
        )

        val joinP2pRoom = entry(
            key = "multiplayer.joinP2pRoom",
            translations = linkedMapOf(
                "en" to "Join P2P Room",
                "zh-CN" to "加入P2P房间",
            ),
        )

        val joinP2pRoomInput = entry(
            key = "multiplayer.joinP2pRoomInput",
            translations = linkedMapOf(
                "en" to "Enter the P2P room id.",
                "zh-CN" to "输入P2P房间ID。",
            ),
        )

        val joinServer = entry(
            key = "multiplayer.joinServer",
            translations = linkedMapOf(
                "en" to "Join Server",
                "zh-CN" to "加入服务器",
            ),
        )

        val joinServerHint = entry(
            key = "multiplayer.joinServerHint",
            translations = linkedMapOf(
                "en" to "server address or room code",
                "zh-CN" to "服务器地址或房间代码",
            ),
        )

        val joinServerInput = entry(
            key = "multiplayer.joinServerInput",
            translations = linkedMapOf(
                "en" to "Enter the server address or room code.",
                "zh-CN" to "输入服务器地址或房间代码。",
            ),
        )

        val joinServerQuestion = entry(
            key = "multiplayer.joinServerQuestion",
            translations = linkedMapOf(
                "en" to "Join Server?",
                "zh-CN" to "加入服务器？",
            ),
        )

        val joiningRoom = entry(
            key = "multiplayer.joiningRoom",
            translations = linkedMapOf(
                "en" to "Joining Room",
                "zh-CN" to "正在加入房间",
            ),
        )

        val lobbySettings = entry(
            key = "multiplayer.lobbySettings",
            translations = linkedMapOf(
                "en" to "Multiplayer lobby settings",
                "zh-CN" to "多人房间大厅设置",
            ),
        )

        val missingJoinInput = entry(
            key = "multiplayer.missingJoinInput",
            translations = linkedMapOf(
                "en" to "Unable to join server: missing server address or room code",
                "zh-CN" to "无法加入服务器：缺少服务器地址或房间代码",
            ),
        )

        val missingPlayerName = entry(
            key = "multiplayer.missingPlayerName",
            translations = linkedMapOf(
                "en" to "Player name cannot be empty",
                "zh-CN" to "玩家名称不能为空",
            ),
        )

        val noRooms = entry(
            key = "multiplayer.noRooms",
            translations = linkedMapOf(
                "en" to "No rooms found",
                "zh-CN" to "未找到房间",
            ),
        )

        val p2pRoomIdHint = entry(
            key = "multiplayer.p2pRoomIdHint",
            translations = linkedMapOf(
                "en" to "room id",
                "zh-CN" to "房间ID",
            ),
        )

        val playerNameHint = entry(
            key = "multiplayer.playerNameHint",
            translations = linkedMapOf(
                "en" to "player name",
                "zh-CN" to "玩家名称",
            ),
        )

        val playerNamePrompt = entry(
            key = "multiplayer.playerNamePrompt",
            translations = linkedMapOf(
                "en" to "Enter the name shown to other players.",
                "zh-CN" to "输入向其他玩家显示的名称。",
            ),
        )

        object roomInfo {
            val host = entry(
                key = "multiplayer.roomInfo.host",
                translations = linkedMapOf(
                    "en" to "Host: {0}",
                    "zh-CN" to "主机：{0}",
                ),
            )

            val map = entry(
                key = "multiplayer.roomInfo.map",
                translations = linkedMapOf(
                    "en" to "Map: {0}",
                    "zh-CN" to "地图：{0}",
                ),
            )

            val mods = entry(
                key = "multiplayer.roomInfo.mods",
                translations = linkedMapOf(
                    "en" to "Mods: {0}",
                    "zh-CN" to "模组：{0}",
                ),
            )

            val password = entry(
                key = "multiplayer.roomInfo.password",
                translations = linkedMapOf(
                    "en" to "Password: {0}",
                    "zh-CN" to "密码：{0}",
                ),
            )

            val players = entry(
                key = "multiplayer.roomInfo.players",
                translations = linkedMapOf(
                    "en" to "Players: {0}",
                    "zh-CN" to "玩家：{0}",
                ),
            )

            val state = entry(
                key = "multiplayer.roomInfo.state",
                translations = linkedMapOf(
                    "en" to "State: {0}",
                    "zh-CN" to "状态：{0}",
                ),
            )

            val transport = entry(
                key = "multiplayer.roomInfo.transport",
                translations = linkedMapOf(
                    "en" to "Transport: {0}",
                    "zh-CN" to "传输：{0}",
                ),
            )

            val version = entry(
                key = "multiplayer.roomInfo.version",
                translations = linkedMapOf(
                    "en" to "Version: {0}",
                    "zh-CN" to "版本：{0}",
                ),
            )

        }

        val roomNoLongerListed = entry(
            key = "multiplayer.roomNoLongerListed",
            translations = linkedMapOf(
                "en" to "Unable to join server: room is no longer in the list",
                "zh-CN" to "无法加入服务器：房间已不在列表中",
            ),
        )

        val searching = entry(
            key = "multiplayer.searching",
            translations = linkedMapOf(
                "en" to "Searching for rooms...",
                "zh-CN" to "正在搜索房间...",
            ),
        )

        object state {
            val unknown = entry(
                key = "multiplayer.state.unknown",
                translations = linkedMapOf(
                    "en" to "Unknown",
                    "zh-CN" to "未知",
                ),
            )

        }

        val unableToJoinP2pRoom = entry(
            key = "multiplayer.unableToJoinP2pRoom",
            translations = linkedMapOf(
                "en" to "Unable to join P2P room",
                "zh-CN" to "无法加入P2P房间",
            ),
        )

        val unableToJoinServer = entry(
            key = "multiplayer.unableToJoinServer",
            translations = linkedMapOf(
                "en" to "Unable to join server",
                "zh-CN" to "无法加入服务器",
            ),
        )

        val unknown = entry(
            key = "multiplayer.unknown",
            translations = linkedMapOf(
                "en" to "Unknown",
                "zh-CN" to "未知",
            ),
        )

    }

    object pausemenu {
        val chat = entry(
            key = "pausemenu.chat",
            translations = linkedMapOf(
                "en" to "Chat",
                "zh-CN" to "聊天",
            ),
        )

        val exitGame = entry(
            key = "pausemenu.exitGame",
            translations = linkedMapOf(
                "en" to "Exit Game",
                "zh-CN" to "退出游戏",
            ),
        )

        val players = entry(
            key = "pausemenu.players",
            translations = linkedMapOf(
                "en" to "Players",
                "zh-CN" to "玩家",
            ),
        )

        val resume = entry(
            key = "pausemenu.resume",
            translations = linkedMapOf(
                "en" to "Resume",
                "zh-CN" to "继续",
            ),
        )

        val save = entry(
            key = "pausemenu.save",
            translations = linkedMapOf(
                "en" to "Save",
                "zh-CN" to "保存",
            ),
        )

        val settings = entry(
            key = "pausemenu.settings",
            translations = linkedMapOf(
                "en" to "Settings",
                "zh-CN" to "设置",
            ),
        )

        val surrender = entry(
            key = "pausemenu.surrender",
            translations = linkedMapOf(
                "en" to "Surrender",
                "zh-CN" to "投降",
            ),
        )

    }

    object replay {
        val empty = entry(
            key = "replay.empty",
            translations = linkedMapOf(
                "en" to "No replays found",
                "zh-CN" to "未找到回放",
            ),
        )

        val filter = entry(
            key = "replay.filter",
            translations = linkedMapOf(
                "en" to "Filter",
                "zh-CN" to "筛选",
            ),
        )

        val noMatches = entry(
            key = "replay.noMatches",
            translations = linkedMapOf(
                "en" to "No replays match the current filter",
                "zh-CN" to "没有符合筛选条件的回放",
            ),
        )

        val searchHint = entry(
            key = "replay.searchHint",
            translations = linkedMapOf(
                "en" to "replay name",
                "zh-CN" to "回放名称",
            ),
        )

        val title = entry(
            key = "replay.title",
            translations = linkedMapOf(
                "en" to "Watch Replay",
                "zh-CN" to "观看回放",
            ),
        )

        val unknownSize = entry(
            key = "replay.unknownSize",
            translations = linkedMapOf(
                "en" to "Unknown size",
                "zh-CN" to "大小未知",
            ),
        )

        val unknownTime = entry(
            key = "replay.unknownTime",
            translations = linkedMapOf(
                "en" to "Unknown time",
                "zh-CN" to "时间未知",
            ),
        )

    }

    object resourcebrowser {
        val author = entry(
            key = "resourcebrowser.author",
            translations = linkedMapOf(
                "en" to "Author: {0}",
                "zh-CN" to "作者：{0}",
            ),
        )

        val back = entry(
            key = "resourcebrowser.back",
            translations = linkedMapOf(
                "en" to "Back",
                "zh-CN" to "返回",
            ),
        )

        val download = entry(
            key = "resourcebrowser.download",
            translations = linkedMapOf(
                "en" to "Download",
                "zh-CN" to "下载",
            ),
        )

        val downloadDone = entry(
            key = "resourcebrowser.downloadDone",
            translations = linkedMapOf(
                "en" to "Download complete",
                "zh-CN" to "下载完成",
            ),
        )

        val downloadDoneMessage = entry(
            key = "resourcebrowser.downloadDoneMessage",
            translations = linkedMapOf(
                "en" to "The resource has been saved.",
                "zh-CN" to "资源已保存。",
            ),
        )

        val downloadFailed = entry(
            key = "resourcebrowser.downloadFailed",
            translations = linkedMapOf(
                "en" to "Download failed: {0}",
                "zh-CN" to "下载失败：{0}",
            ),
        )

        val downloading = entry(
            key = "resourcebrowser.downloading",
            translations = linkedMapOf(
                "en" to "Downloading",
                "zh-CN" to "正在下载",
            ),
        )

        val downloads = entry(
            key = "resourcebrowser.downloads",
            translations = linkedMapOf(
                "en" to "Downloads: {0}",
                "zh-CN" to "下载次数：{0}",
            ),
        )

        val empty = entry(
            key = "resourcebrowser.empty",
            translations = linkedMapOf(
                "en" to "No resources found",
                "zh-CN" to "没有找到资源",
            ),
        )

        val failed = entry(
            key = "resourcebrowser.failed",
            translations = linkedMapOf(
                "en" to "Unable to load resources: {0}",
                "zh-CN" to "无法加载资源：{0}",
            ),
        )

        val loadMore = entry(
            key = "resourcebrowser.loadMore",
            translations = linkedMapOf(
                "en" to "Load More",
                "zh-CN" to "加载更多",
            ),
        )

        val loading = entry(
            key = "resourcebrowser.loading",
            translations = linkedMapOf(
                "en" to "Loading resources...",
                "zh-CN" to "正在加载资源...",
            ),
        )

        val missingDownloadUrl = entry(
            key = "resourcebrowser.missingDownloadUrl",
            translations = linkedMapOf(
                "en" to "This resource does not provide a download link.",
                "zh-CN" to "此资源没有提供下载链接。",
            ),
        )

        val `open` = entry(
            key = "resourcebrowser.open",
            translations = linkedMapOf(
                "en" to "Open",
                "zh-CN" to "打开",
            ),
        )

        val refresh = entry(
            key = "resourcebrowser.refresh",
            translations = linkedMapOf(
                "en" to "Refresh",
                "zh-CN" to "刷新",
            ),
        )

        val search = entry(
            key = "resourcebrowser.search",
            translations = linkedMapOf(
                "en" to "Search",
                "zh-CN" to "搜索",
            ),
        )

        val searchHint = entry(
            key = "resourcebrowser.searchHint",
            translations = linkedMapOf(
                "en" to "keyword",
                "zh-CN" to "关键词",
            ),
        )

        val sourceSearch = entry(
            key = "resourcebrowser.sourceSearch",
            translations = linkedMapOf(
                "en" to "RTSBox Search",
                "zh-CN" to "铁锈盒子搜索",
            ),
        )

        val sourceWeekly = entry(
            key = "resourcebrowser.sourceWeekly",
            translations = linkedMapOf(
                "en" to "Weekly Downloads",
                "zh-CN" to "下载周榜",
            ),
        )

        val title = entry(
            key = "resourcebrowser.title",
            translations = linkedMapOf(
                "en" to "Resource Browser",
                "zh-CN" to "资源浏览器",
            ),
        )

        val typeMap = entry(
            key = "resourcebrowser.typeMap",
            translations = linkedMapOf(
                "en" to "Map",
                "zh-CN" to "地图",
            ),
        )

        val typeMod = entry(
            key = "resourcebrowser.typeMod",
            translations = linkedMapOf(
                "en" to "Mod",
                "zh-CN" to "模组",
            ),
        )

        val version = entry(
            key = "resourcebrowser.version",
            translations = linkedMapOf(
                "en" to "Version: {0}",
                "zh-CN" to "版本：{0}",
            ),
        )

    }

    object settings {
        object audio {
            val enableSounds = entry(
                key = "settings.audio.enableSounds",
                translations = linkedMapOf(
                    "en" to "Enable game sounds",
                    "zh-CN" to "启用游戏声音",
                ),
            )

            val gameVolume = entry(
                key = "settings.audio.gameVolume",
                translations = linkedMapOf(
                    "en" to "Game volume",
                    "zh-CN" to "游戏音效",
                ),
            )

            val interfaceVolume = entry(
                key = "settings.audio.interfaceVolume",
                translations = linkedMapOf(
                    "en" to "Interface volume",
                    "zh-CN" to "界面音效",
                ),
            )

            val masterVolume = entry(
                key = "settings.audio.masterVolume",
                translations = linkedMapOf(
                    "en" to "Master volume",
                    "zh-CN" to "主音量",
                ),
            )

            val musicVolume = entry(
                key = "settings.audio.musicVolume",
                translations = linkedMapOf(
                    "en" to "Music volume",
                    "zh-CN" to "音乐音量",
                ),
            )

        }

        val current = entry(
            key = "settings.current",
            translations = linkedMapOf(
                "en" to "Current language: {0}",
                "zh-CN" to "当前语言：{0}",
            ),
        )

        object display {
            val batterySaving = entry(
                key = "settings.display.batterySaving",
                translations = linkedMapOf(
                    "en" to "Battery saving mode (30 FPS limit)",
                    "zh-CN" to "省电模式（30 FPS限制）",
                ),
            )

            val edgeScrollSpeed = entry(
                key = "settings.display.edgeScrollSpeed",
                translations = linkedMapOf(
                    "en" to "Edge scroll speed",
                    "zh-CN" to "边缘滚动速度",
                ),
            )

            val experimentalAndroidOpenGlRenderer = entry(
                key = "settings.display.experimentalAndroidOpenGlRenderer",
                translations = linkedMapOf(
                    "en" to "Use experimental OpenGL renderer",
                    "zh-CN" to "使用实验性 OpenGL 渲染",
                ),
            )

            val fullscreen = entry(
                key = "settings.display.fullscreen",
                translations = linkedMapOf(
                    "en" to "Fullscreen",
                    "zh-CN" to "全屏",
                ),
            )

            val highRefreshRate = entry(
                key = "settings.display.highRefreshRate",
                translations = linkedMapOf(
                    "en" to "High refresh rate",
                    "zh-CN" to "高刷新率",
                ),
            )

            val renderClouds = entry(
                key = "settings.display.renderClouds",
                translations = linkedMapOf(
                    "en" to "Render clouds",
                    "zh-CN" to "渲染云层",
                ),
            )

            val renderDoubleScale = entry(
                key = "settings.display.renderDoubleScale",
                translations = linkedMapOf(
                    "en" to "Double scale rendering",
                    "zh-CN" to "渲染半分辨率",
                ),
            )

            val scrollSpeed = entry(
                key = "settings.display.scrollSpeed",
                translations = linkedMapOf(
                    "en" to "Scroll speed",
                    "zh-CN" to "滚动速度",
                ),
            )

            val shaderEffects = entry(
                key = "settings.display.shaderEffects",
                translations = linkedMapOf(
                    "en" to "Shader effects",
                    "zh-CN" to "着色器效果",
                ),
            )

            val showBackgroundBattleDemo = entry(
                key = "settings.display.showBackgroundBattleDemo",
                translations = linkedMapOf(
                    "en" to "Show background battle demo",
                    "zh-CN" to "显示背景战斗演示",
                ),
            )

            val showFps = entry(
                key = "settings.display.showFps",
                translations = linkedMapOf(
                    "en" to "Show FPS",
                    "zh-CN" to "显示FPS",
                ),
            )

            val showHpChanges = entry(
                key = "settings.display.showHpChanges",
                translations = linkedMapOf(
                    "en" to "Show HP changes",
                    "zh-CN" to "显示血量变化",
                ),
            )

            val showUnitHp = entry(
                key = "settings.display.showUnitHp",
                translations = linkedMapOf(
                    "en" to "Show unit HP",
                    "zh-CN" to "显示单位血量",
                ),
            )

            val showUnitIcons = entry(
                key = "settings.display.showUnitIcons",
                translations = linkedMapOf(
                    "en" to "Show unit icons",
                    "zh-CN" to "显示单位图标",
                ),
            )

            val showWarLogOnScreen = entry(
                key = "settings.display.showWarLogOnScreen",
                translations = linkedMapOf(
                    "en" to "Show war log on screen",
                    "zh-CN" to "屏幕显示战斗日志",
                ),
            )

            val showWaypoints = entry(
                key = "settings.display.showWaypoints",
                translations = linkedMapOf(
                    "en" to "Show unit waypoints",
                    "zh-CN" to "显示单位路径点",
                ),
            )

            val showZoomButton = entry(
                key = "settings.display.showZoomButton",
                translations = linkedMapOf(
                    "en" to "Show zoom button",
                    "zh-CN" to "显示缩放按钮",
                ),
            )

            val softFogFading = entry(
                key = "settings.display.softFogFading",
                translations = linkedMapOf(
                    "en" to "Soft fog fading",
                    "zh-CN" to "软雾渐变",
                ),
            )

            val teamShaders = entry(
                key = "settings.display.teamShaders",
                translations = linkedMapOf(
                    "en" to "Team color shaders",
                    "zh-CN" to "团队颜色着色器",
                ),
            )

            val useMinimapAllyColors = entry(
                key = "settings.display.useMinimapAllyColors",
                translations = linkedMapOf(
                    "en" to "Use minimap ally colors",
                    "zh-CN" to "使用小地图友军颜色",
                ),
            )

            val vsync = entry(
                key = "settings.display.vsync",
                translations = linkedMapOf(
                    "en" to "VSync",
                    "zh-CN" to "垂直同步",
                ),
            )

        }

        object gameplay {
            val autosaving = entry(
                key = "settings.gameplay.autosaving",
                translations = linkedMapOf(
                    "en" to "Autosaving",
                    "zh-CN" to "自动保存",
                ),
            )

            val doubleClickToAttackMove = entry(
                key = "settings.gameplay.doubleClickToAttackMove",
                translations = linkedMapOf(
                    "en" to "Double click to attack move",
                    "zh-CN" to "双击攻击移动",
                ),
            )

            val quickRally = entry(
                key = "settings.gameplay.quickRally",
                translations = linkedMapOf(
                    "en" to "Quick rally",
                    "zh-CN" to "快速集合",
                ),
            )

            val replaysShowRecordedChat = entry(
                key = "settings.gameplay.replaysShowRecordedChat",
                translations = linkedMapOf(
                    "en" to "Show recorded chat",
                    "zh-CN" to "显示聊天记录",
                ),
            )

            val saveMultiplayerReplays = entry(
                key = "settings.gameplay.saveMultiplayerReplays",
                translations = linkedMapOf(
                    "en" to "Save multiplayer replays",
                    "zh-CN" to "保存多人录像",
                ),
            )

            val showChatAndPingShortcuts = entry(
                key = "settings.gameplay.showChatAndPingShortcuts",
                translations = linkedMapOf(
                    "en" to "Show chat and ping shortcuts",
                    "zh-CN" to "显示聊天和信号快捷键",
                ),
            )

            val showMapPingsOnBattlefield = entry(
                key = "settings.gameplay.showMapPingsOnBattlefield",
                translations = linkedMapOf(
                    "en" to "Show map pings on battlefield",
                    "zh-CN" to "战场显示地图信号",
                ),
            )

            val showMapPingsOnMinimap = entry(
                key = "settings.gameplay.showMapPingsOnMinimap",
                translations = linkedMapOf(
                    "en" to "Show map pings on minimap",
                    "zh-CN" to "小地图显示地图信号",
                ),
            )

            val showPlayerChatInGame = entry(
                key = "settings.gameplay.showPlayerChatInGame",
                translations = linkedMapOf(
                    "en" to "Show player chat in game",
                    "zh-CN" to "游戏内显示玩家聊天",
                ),
            )

            val smartSelection = entry(
                key = "settings.gameplay.smartSelection",
                translations = linkedMapOf(
                    "en" to "Smart selection",
                    "zh-CN" to "智能选择",
                ),
            )

            val udpInMultiplayer = entry(
                key = "settings.gameplay.udpInMultiplayer",
                translations = linkedMapOf(
                    "en" to "UDP networking for lower lag (beta)",
                    "zh-CN" to "使用UDP协议降低网络延迟（实验性功能）",
                ),
            )

        }

        object `interface` {
            val classicInterface = entry(
                key = "settings.interface.classicInterface",
                translations = linkedMapOf(
                    "en" to "Classic interface",
                    "zh-CN" to "经典界面",
                ),
            )

            val forceEnglish = entry(
                key = "settings.interface.forceEnglish",
                translations = linkedMapOf(
                    "en" to "Force English",
                    "zh-CN" to "强制英文",
                ),
            )

            val gestureZoom = entry(
                key = "settings.interface.gestureZoom",
                translations = linkedMapOf(
                    "en" to "Enable 3 finger zoom gesture",
                    "zh-CN" to "启用3指缩放手势",
                ),
            )

            val immersiveFullScreen = entry(
                key = "settings.interface.immersiveFullScreen",
                translations = linkedMapOf(
                    "en" to "Immersive fullscreen",
                    "zh-CN" to "沉浸式全屏",
                ),
            )

            val keyboardSupport = entry(
                key = "settings.interface.keyboardSupport",
                translations = linkedMapOf(
                    "en" to "Enable keyboard support",
                    "zh-CN" to "启用键盘支持",
                ),
            )

            val mouseCapture = entry(
                key = "settings.interface.mouseCapture",
                translations = linkedMapOf(
                    "en" to "Mouse capture",
                    "zh-CN" to "鼠标捕获",
                ),
            )

            val mouseSupport = entry(
                key = "settings.interface.mouseSupport",
                translations = linkedMapOf(
                    "en" to "Enable extended mouse support",
                    "zh-CN" to "启用扩展鼠标支持",
                ),
            )

            val sendReports = entry(
                key = "settings.interface.sendReports",
                translations = linkedMapOf(
                    "en" to "Send crash reports",
                    "zh-CN" to "发送崩溃报告",
                ),
            )

            val showUnitGroups = entry(
                key = "settings.interface.showUnitGroups",
                translations = linkedMapOf(
                    "en" to "Show unit group interface",
                    "zh-CN" to "显示单位编组界面",
                ),
            )

            val unlockedScreenRotation = entry(
                key = "settings.interface.unlockedScreenRotation",
                translations = linkedMapOf(
                    "en" to "Allow portrait screen rotation",
                    "zh-CN" to "允许纵向屏幕旋转",
                ),
            )

            val useCircleSelect = entry(
                key = "settings.interface.useCircleSelect",
                translations = linkedMapOf(
                    "en" to "Hold touch to area select",
                    "zh-CN" to "启用长按选区模式",
                ),
            )

        }

        object keybindings {
            val clear = entry(
                key = "settings.keybindings.clear",
                translations = linkedMapOf(
                    "en" to "Clear",
                    "zh-CN" to "清除",
                ),
            )

            val conflict = entry(
                key = "settings.keybindings.conflict",
                translations = linkedMapOf(
                    "en" to "Conflicts with another binding",
                    "zh-CN" to "与其他绑定冲突",
                ),
            )

            val hint = entry(
                key = "settings.keybindings.hint",
                translations = linkedMapOf(
                    "en" to "Select a binding, then press a key. Ctrl / Shift / Alt are supported; Escape cancels.",
                    "zh-CN" to "选择绑定后按键，支持 Ctrl / Shift / Alt，按 Esc 取消。",
                ),
            )

            val none = entry(
                key = "settings.keybindings.none",
                translations = linkedMapOf(
                    "en" to "None",
                    "zh-CN" to "无",
                ),
            )

            val pressKey = entry(
                key = "settings.keybindings.pressKey",
                translations = linkedMapOf(
                    "en" to "Press a key…",
                    "zh-CN" to "按键",
                ),
            )

            val pressKeyFor = entry(
                key = "settings.keybindings.pressKeyFor",
                translations = linkedMapOf(
                    "en" to "Press a key for {0}",
                    "zh-CN" to "为{0}按键",
                ),
            )

            val primary = entry(
                key = "settings.keybindings.primary",
                translations = linkedMapOf(
                    "en" to "Primary",
                    "zh-CN" to "主要",
                ),
            )

            val secondary = entry(
                key = "settings.keybindings.secondary",
                translations = linkedMapOf(
                    "en" to "Secondary",
                    "zh-CN" to "次要",
                ),
            )

        }

        object language {
            val label = entry(
                key = "settings.language.label",
                translations = linkedMapOf(
                    "en" to "UI language",
                    "zh-CN" to "界面语言",
                ),
            )

        }

        object pages {
            val audio = entry(
                key = "settings.pages.audio",
                translations = linkedMapOf(
                    "en" to "Audio",
                    "zh-CN" to "音频",
                ),
            )

            val display = entry(
                key = "settings.pages.display",
                translations = linkedMapOf(
                    "en" to "Display & Input",
                    "zh-CN" to "显示与输入",
                ),
            )

            val gameplay = entry(
                key = "settings.pages.gameplay",
                translations = linkedMapOf(
                    "en" to "Gameplay",
                    "zh-CN" to "玩法",
                ),
            )

            val keybindings = entry(
                key = "settings.pages.keybindings",
                translations = linkedMapOf(
                    "en" to "Key Bindings",
                    "zh-CN" to "按键绑定",
                ),
            )

            val theme = entry(
                key = "settings.pages.theme",
                translations = linkedMapOf(
                    "en" to "Theme",
                    "zh-CN" to "主题",
                ),
            )

        }

        val saved = entry(
            key = "settings.saved",
            translations = linkedMapOf(
                "en" to "Settings saved",
                "zh-CN" to "设置已保存",
            ),
        )

        object storage {
            val `external` = entry(
                key = "settings.storage.external",
                translations = linkedMapOf(
                    "en" to "External storage",
                    "zh-CN" to "外部存储",
                ),
            )

            val `internal` = entry(
                key = "settings.storage.internal",
                translations = linkedMapOf(
                    "en" to "Internal storage",
                    "zh-CN" to "内部存储",
                ),
            )

            val location = entry(
                key = "settings.storage.location",
                translations = linkedMapOf(
                    "en" to "Storage location",
                    "zh-CN" to "存储位置",
                ),
            )

            val restartRequired = entry(
                key = "settings.storage.restartRequired",
                translations = linkedMapOf(
                    "en" to "Changing storage takes effect after restart.",
                    "zh-CN" to "切换存储位置后需重启生效。",
                ),
            )

        }

        object theme {
            val enableAnimations = entry(
                key = "settings.theme.enableAnimations",
                translations = linkedMapOf(
                    "en" to "Enable animations",
                    "zh-CN" to "启用动画",
                ),
            )

            val overlayOpacity = entry(
                key = "settings.theme.overlayOpacity",
                translations = linkedMapOf(
                    "en" to "UI background opacity",
                    "zh-CN" to "界面背景不透明度",
                ),
            )

        }

    }

    object singleplayer {
        val back = entry(
            key = "singleplayer.back",
            translations = linkedMapOf(
                "en" to "Back",
                "zh-CN" to "返回",
            ),
        )

        val campaign = entry(
            key = "singleplayer.campaign",
            translations = linkedMapOf(
                "en" to "Campaign",
                "zh-CN" to "战役",
            ),
        )

        val challenge = entry(
            key = "singleplayer.challenge",
            translations = linkedMapOf(
                "en" to "Challenge",
                "zh-CN" to "挑战",
            ),
        )

        val customMaps = entry(
            key = "singleplayer.customMaps",
            translations = linkedMapOf(
                "en" to "Custom Maps",
                "zh-CN" to "自定义地图",
            ),
        )

        val loadSave = entry(
            key = "singleplayer.loadSave",
            translations = linkedMapOf(
                "en" to "Load Save",
                "zh-CN" to "读取存档",
            ),
        )

        val sandbox = entry(
            key = "singleplayer.sandbox",
            translations = linkedMapOf(
                "en" to "Sandbox",
                "zh-CN" to "沙盒",
            ),
        )

        val skirmish = entry(
            key = "singleplayer.skirmish",
            translations = linkedMapOf(
                "en" to "Skirmish",
                "zh-CN" to "遭遇战",
            ),
        )

        val survival = entry(
            key = "singleplayer.survival",
            translations = linkedMapOf(
                "en" to "Survival",
                "zh-CN" to "生存",
            ),
        )

    }

    object update {
        val availableMessage = entry(
            key = "update.availableMessage",
            translations = linkedMapOf(
                "en" to "New version: {0}\n\nRelease notes:\n{1}",
                "zh-CN" to "检测到新版本：{0}\n\n更新内容：\n{1}",
            ),
        )

        val availableTitle = entry(
            key = "update.availableTitle",
            translations = linkedMapOf(
                "en" to "Update available",
                "zh-CN" to "发现新版本",
            ),
        )

        val checkingMessage = entry(
            key = "update.checkingMessage",
            translations = linkedMapOf(
                "en" to "Checking GitHub releases...",
                "zh-CN" to "正在检查 GitHub Release...",
            ),
        )

        val checkingTitle = entry(
            key = "update.checkingTitle",
            translations = linkedMapOf(
                "en" to "Checking for updates",
                "zh-CN" to "正在检查更新",
            ),
        )

        val failedMessage = entry(
            key = "update.failedMessage",
            translations = linkedMapOf(
                "en" to "Unable to check GitHub releases: {0}",
                "zh-CN" to "无法检查 GitHub Release：{0}",
            ),
        )

        val failedTitle = entry(
            key = "update.failedTitle",
            translations = linkedMapOf(
                "en" to "Update check failed",
                "zh-CN" to "检查更新失败",
            ),
        )

        val latestMessage = entry(
            key = "update.latestMessage",
            translations = linkedMapOf(
                "en" to "You are running the latest version: {0}",
                "zh-CN" to "当前已是最新版本：{0}",
            ),
        )

        val latestTitle = entry(
            key = "update.latestTitle",
            translations = linkedMapOf(
                "en" to "Already up to date",
                "zh-CN" to "已是最新版本",
            ),
        )

        val noReleaseNotes = entry(
            key = "update.noReleaseNotes",
            translations = linkedMapOf(
                "en" to "No release notes provided.",
                "zh-CN" to "此版本没有提供更新内容。",
            ),
        )

        val openRelease = entry(
            key = "update.openRelease",
            translations = linkedMapOf(
                "en" to "Open Release",
                "zh-CN" to "打开 Release",
            ),
        )

        val prerelease = entry(
            key = "update.prerelease",
            translations = linkedMapOf(
                "en" to "Prerelease",
                "zh-CN" to "预发布版本",
            ),
        )

        val publishedAt = entry(
            key = "update.publishedAt",
            translations = linkedMapOf(
                "en" to "Published",
                "zh-CN" to "发布时间",
            ),
        )

        val releasePage = entry(
            key = "update.releasePage",
            translations = linkedMapOf(
                "en" to "Release page",
                "zh-CN" to "Release 页面",
            ),
        )

    }

}
